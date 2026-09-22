(ns foundation-deployment.task-xtbench
  (:require [clojure.string :as string]
            [code.project :as project]
            [lang.seedgen.cli :as seedgen]
            [lang.seedgen.metrics :as metrics]
            [std.fs :as fs]
            [std.lib.git :as git]))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

(def ^:dynamic *env*
  (fn [key]
    (System/getenv key)))

(def ^:dynamic *git*
  (fn [args opts]
    (git/git args opts)))

(def +metrics-dir+ "target/xtbench-metrics")
(def +artifacts-dir+ "target/xtbench-artifacts")
(def +branch-dir+ "target/xtbench-branch")

(def +suites+
  {"core" {:workflow "core"
            :jobs [{:name "lang" :selector 'xt.lang :languages [:dart :python :lua]}
                   {:name "event" :selector 'xt.event :languages [:dart :python :lua]}
                   {:name "net" :selector 'xt.net :languages [:dart :python :lua]}
                   {:name "substrate" :selector 'xt.substrate :languages [:dart :python :lua]}
                   {:name "db" :selector 'xt.db :languages [:dart :python :lua]}]}
   "core-kmi" {:workflow "core-kmi"
                :jobs [{:name "kmi" :selector 'kmi.lang :languages [:dart :python :lua]}]}
   "annex" {:workflow "annex"
            :jobs [{:name "lang" :selector 'xt.lang :languages [:php :r]}
                   {:name "event" :selector 'xt.event :languages [:php]}
                   {:name "kmi" :selector 'kmi.lang :languages [:php]}]}
   "tbd" {:workflow "tbd"
          :jobs [{:name "lang" :selector 'xt.lang :languages [:scheme :elisp :julia]}
                 {:name "event" :selector 'xt.event :languages [:elisp :julia]}
                 {:name "kmi" :selector 'kmi.lang :languages [:elisp :julia]}]}
   "active" {:workflow "active"
             :jobs [{:name "lang" :selector 'xt.lang :languages [:ruby]}
                    {:name "event" :selector 'xt.event :languages [:ruby]}
                    {:name "substrate" :selector 'xt.substrate :languages [:ruby]}
                    {:name "db" :selector 'xt.db :languages [:ruby]}
                    {:name "kmi" :selector 'kmi.lang :languages [:ruby]}]}})

^{:public true}
(defn suite-config
  "Returns the XTBench job matrix for a named workflow suite."
  [suite]
  (let [suite-key (if (keyword? suite) (name suite) (str suite))]
    (or (get +suites+ suite-key)
        (throw (ex-info "Unknown XTBench suite"
                        {:suite suite
                         :suites (sort (keys +suites+))})))))

^{:public true}
(defn test
  "Runs every job in an XTBench suite and writes one JSON artifact per language."
  ([suite] (test suite {}))
  ([suite opts]
   (let [{:keys [workflow jobs]} (suite-config suite)
         selected-job (some-> (:job opts) str)
         jobs         (if selected-job
                        (filter #(= selected-job (:name %)) jobs)
                        jobs)
         _            (when (and selected-job (empty? jobs))
                        (throw (ex-info "Unknown XTBench job"
                                        {:suite suite
                                         :job selected-job})))
         project      (or (:project opts) (project/project))
         root         (:root project)
         metrics-dir (str (or (:metrics-dir opts)
                               (fs/path root +metrics-dir+)))
         results      (vec
                       (mapcat (fn [{:keys [name selector languages]}]
                                 (map (fn [language]
                                        (let [path (str (fs/path metrics-dir
                                                                 (str name "-" (clojure.core/name language) ".json")))]
                                          (try
                                            (let [result (seedgen/seedgen-test selector [language] project)]
                                              (metrics/write-test-record! path selector [language] result)
                                              {:job name
                                               :language language
                                               :path path
                                               :exit (or (:exit result) 1)})
                                            (catch Throwable error
                                              (metrics/write-error-record! path selector [language] error)
                                              {:job name
                                               :language language
                                               :path path
                                               :exit 1
                                               :error (or (ex-message error) (str error))}))))
                                      languages))
                               jobs))]
     {:suite (str suite)
      :workflow workflow
      :results results
      :exit (if (every? #(zero? (:exit %)) results) 0 1)})))

^{:public true}
(defn merge
  "Merges XTBench JSON artifacts into the metrics branch directory."
  ([suite] (merge suite {}))
  ([suite opts]
   (let [{:keys [workflow]} (suite-config suite)
         input-dir  (or (:input-dir opts) +artifacts-dir+)
         output-dir (or (:output-dir opts) +branch-dir+)]
     (metrics/merge-directory! input-dir output-dir workflow))))

^{:public true}
(defn publish
  "Merges and publishes an XTBench suite to the metrics branch with retries."
  ([suite] (publish suite {}))
  ([suite opts]
   (let [{:keys [workflow]} (suite-config suite)
         project      (or (:project opts) (project/project))
         root         (str (or (:root opts) (:root project) "."))
         artifacts    (str (or (:artifacts-dir opts) (fs/path root +artifacts-dir+)))
         output       (str (or (:output-dir opts) (fs/path root +branch-dir+)))
         worktree     (str (or (:worktree opts) output))
         branch       (or (:branch opts) "xtbench-metrics")
         remote       (or (:remote opts) "origin")
         max-attempts (or (:max-attempts opts) 5)
         token        (*env* "GITHUB_TOKEN")
         repository   (*env* "GITHUB_REPOSITORY")
         push-target  (if (and (seq token) (seq repository))
                        (str "https://x-access-token:" token "@github.com/" repository ".git")
                        remote)
         remote-ref   (str remote "/" branch)
         run-git      (fn [args & [git-opts]]
                        (apply *git* [args (or git-opts {:root root})]))
         _            (when-let [parent (fs/parent worktree)]
                        (fs/create-directory parent))
         _            (when (fs/exists? worktree)
                        (run-git ["worktree" "remove" "--force" worktree] {:root root}))
         _            (run-git ["fetch" remote branch] {:root root})
         remote?      (zero? (:exit (run-git ["show-ref" "--verify" "--quiet"
                                               (str "refs/remotes/" remote "/" branch)]
                                              {:root root})))
         _            (run-git (if remote?
                                ["worktree" "add" "--detach" worktree remote-ref]
                                ["worktree" "add" "--detach" worktree "HEAD"])
                               {:root root})
         _            (run-git (if remote?
                                ["switch" "-C" branch remote-ref]
                                ["switch" "--orphan" branch])
                               {:root worktree})
         _            (when-not remote?
                        (run-git ["rm" "-rf" "."] {:root worktree}))
         merge-opts   {:input-dir artifacts
                       :output-dir worktree
                       :root root}]
     (try
       (loop [attempt 1]
         (merge suite merge-opts)
         (run-git ["add" "index.json" "latest" "runs"] {:root worktree})
         (let [staged (run-git ["diff" "--cached" "--quiet"] {:root worktree})]
           (if (zero? (:exit staged))
             {:suite (str suite)
              :workflow workflow
              :status :unchanged
              :attempt attempt}
             (do
               (run-git ["config" "user.name" "github-actions[bot]"] {:root worktree})
               (run-git ["config" "user.email"
                         "41898282+github-actions[bot]@users.noreply.github.com"]
                        {:root worktree})
               (run-git ["commit" "-m"
                         (str "xtbench " workflow " run "
                              (or (*env* "GITHUB_RUN_NUMBER") "0")
                              " attempt "
                              (or (*env* "GITHUB_RUN_ATTEMPT") "1"))]
                        {:root worktree})
               (let [pushed (run-git ["push" push-target (str "HEAD:" branch)]
                                     {:root worktree})]
                 (if (zero? (:exit pushed))
                   {:suite (str suite)
                    :workflow workflow
                    :status :published
                    :attempt attempt}
                   (if (>= attempt max-attempts)
                     (throw (ex-info "Unable to publish XTBench metrics"
                                     {:suite suite
                                      :workflow workflow
                                      :attempts attempt
                                      :result pushed}))
                     (do
                       (run-git ["fetch" remote branch] {:root root})
                       (run-git ["reset" "--hard" remote-ref] {:root worktree})
                       (recur (inc attempt))))))))))
       (finally
         (run-git ["worktree" "remove" "--force" worktree] {:root root}))))))

^{:public true}
(defn task-run
  "Dispatches an XTBench task and accepts optional artifact/output paths."
  [task suite & args]
  (let [opts (cond-> {}
               (first args) (assoc :input-dir (first args)
                                   :artifacts-dir (first args))
               (second args) (assoc :output-dir (second args)))]
    (case task
      "test" (test suite opts)
      "merge" (merge suite opts)
      "publish" (publish suite opts)
      (throw (ex-info "Unknown XTBench task"
                      {:task task
                       :tasks ["test" "merge" "publish"]
                       :suite suite})))))

^{:public true}
(defn -main
  "Runs an XTBench task and exits with a shell status."
  [& [task suite & args]]
  (try
    (let [result (apply task-run task suite args)]
      (*exit* (if (= 1 (:exit result)) 1 0)))
    (catch Throwable _
      (*exit* 1))))
