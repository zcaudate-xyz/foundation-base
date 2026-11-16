(ns app.manage
  "Clojure application management: dependency plans, source impact, reproducible
   artifact generation and checks. Renderers return {relative-path content};
   configured compilers load trusted DSL namespaces; generation never installs SQL."
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(def ^:dynamic *on-event*
  "Optional progress listener shared by CLI, watcher and management frontend."
  nil)

(defn emit!
  [event]
  (when *on-event* (*on-event* (assoc event :time (System/currentTimeMillis)))))

(defn project-root [project]
  (.getCanonicalFile (io/file (or (:root project) "."))))

(defn project-file
  "Resolves a relative project path, rejecting traversal and symlink escapes."
  [project path]
  (when-not (and (string? path) (not (str/blank? path))
                 (not (str/includes? path "\\"))
                 (not-any? #{"" "." ".."} (str/split path #"/" -1)))
    (throw (ex-info "Expected a normalized relative project path" {:path path})))
  (let [root (.toPath (project-root project))
        file (io/file (str path))
        resolved (.getCanonicalFile (io/file (.toFile root) (str path)))]
    (when (or (.isAbsolute file) (not (.startsWith (.toPath resolved) root))
              (= (.toPath resolved) root))
      (throw (ex-info "Expected a path inside the project" {:path path})))
    resolved))

(defn plan
  "Returns a stable dependency-first target plan, rejecting unknown targets/cycles."
  ([project] (plan project (or (:default-targets project) (sort (keys (:targets project))))))
  ([project selected]
   (let [targets (:targets project) visited (atom #{}) output (atom [])]
     (when-not (seq targets) (throw (ex-info "Application has no targets" {})))
     (letfn [(visit [key chain]
               (when (some #{key} chain)
                 (throw (ex-info "Application dependency cycle" {:cycle (conj chain key)})))
               (when-not (contains? @visited key)
                 (let [target (or (get targets key)
                                  (throw (ex-info "Unknown application target" {:target key})))]
                   (doseq [dependency (:depends target)] (visit dependency (conj chain key)))
                   (swap! visited conj key)
                   (swap! output conj key))))]
       (doseq [key selected] (visit key []))
       @output))))

(defn- path-under? [path root]
  (let [root (str/replace root #"/+$" "")]
    (or (= path root) (str/starts-with? path (str root "/")))))

(defn affected
  "Selects changed source owners and their downstream dependants."
  [project paths]
  (let [order (plan project (sort (keys (:targets project))))]
    (if (some (fn [path] (some #(path-under? path %) (:project-sources project))) paths)
      order
      (vec (filter
          (reduce (fn [selected key]
                    (let [{:keys [sources depends]} (get-in project [:targets key])]
                      (if (or (some selected depends)
                              (some (fn [path] (some #(path-under? path %) sources)) paths))
                        (conj selected key) selected))) #{} order)
          order)))))

(defn- renderer [value]
  (cond (symbol? value) (requiring-resolve value)
        (ifn? value) value
        :else (throw (ex-info "Target needs a renderer function or symbol" {:render value}))))

(defn- validate-outputs!
  [project selected]
  (reduce (fn [owners key]
            (reduce (fn [owners path]
                      (project-file project path)
                      (doseq [[owned owner] owners]
                        (when (or (path-under? path owned) (path-under? owned path))
                          (throw (ex-info "Target output ownership overlaps"
                                          {:target key :owner owner :path path :overlap owned}))))
                      (assoc owners path key))
                    owners (:outputs (get-in project [:targets key]))))
          {} selected))

(defn render
  "Renders fresh artifacts in memory; target dependencies receive earlier results."
  ([project] (render project (plan project)))
  ([project selected]
   (let [order (plan project selected)]
     (validate-outputs! project order)
     (reduce
      (fn [{:keys [artifacts results] :as state} key]
        (let [started (System/currentTimeMillis)]
          (emit! {:type :target/started :target key})
          (try
           (let [target (get-in project [:targets key])
              files ((renderer (:render target))
                     {:project project :root (project-root project)
                      :target key :results results})]
          (when-not (map? files)
            (throw (ex-info "Renderer must return an artifact map" {:target key})))
          (doseq [[path content] files]
            (project-file project path)
            (when-not (and (string? path) (string? content)
                           (some #(path-under? path %) (:outputs target)))
              (throw (ex-info "Artifact is outside its declared outputs or is not text"
                              {:target key :path path})))
            (when (contains? artifacts path)
              (throw (ex-info "Multiple targets own one artifact" {:target key :path path}))))
          (emit! {:type :target/finished :target key :status :ok :files (count files)
                  :duration-ms (- (System/currentTimeMillis) started)})
          (assoc state :artifacts (into artifacts files) :results (assoc results key files)))
           (catch Exception error
             (emit! {:type :target/finished :target key :status :error :message (.getMessage error)
                     :duration-ms (- (System/currentTimeMillis) started)})
             (throw error)))))
      {:artifacts (sorted-map) :results {}}
      order))))

(defn- actual-paths [project selected]
  (let [root (.toPath (project-root project))]
    (set
     (mapcat
      (fn [key]
        (mapcat
         (fn [path]
           (let [file (project-file project path)]
             (if-not (.exists file)
               []
               ;; Files.walk does not follow directory symlinks or recurse into cycles.
               (with-open [stream (java.nio.file.Files/walk
                                   (.toPath file) (make-array java.nio.file.FileVisitOption 0))]
                 (vec
                  (for [entry (iterator-seq (.iterator stream))
                        :when (java.nio.file.Files/isRegularFile
                               entry (make-array java.nio.file.LinkOption 0))
                        :let [relative (str/replace (str (.relativize root entry)) "\\" "/")]]
                    (do (project-file project relative) relative)))))))
         (:outputs (get-in project [:targets key])))) selected))))

(defn check
  "Compares fresh renderer output with disk, including missing and orphaned files."
  ([project] (check project (plan project)))
  ([project selected]
   (let [order (plan project selected)
         {:keys [artifacts]} (render project order)
         expected (set (keys artifacts)) actual (actual-paths project order)
         missing (sort (remove actual expected))
         unexpected (sort (remove expected actual))
         changed (sort (filter #(and (contains? actual %)
                                     (not= (get artifacts %) (slurp (project-file project %)))) expected))]
     {:status (if (every? empty? [missing unexpected changed]) :ok :error)
      :targets order :files (count artifacts)
      :missing (vec missing) :unexpected (vec unexpected) :changed (vec changed)})))

(defn generate
  "Returns the proposed changes by default; :write true writes fresh artifacts.
   Orphaned files are reported, never silently deleted. All renders finish before writes."
  ([project] (generate project {}))
  ([project {:keys [write targets]}]
   (let [order (plan project (or targets (plan project)))
         {:keys [artifacts]} (render project order)
         changed (vec (for [[path content] artifacts
                            :let [file (project-file project path)]
                            :when (or (not (.isFile file)) (not= content (slurp file)))] path))]
     (when write
       (doseq [path changed :let [file (project-file project path)]]
         (io/make-parents file) (spit file (get artifacts path))))
     {:status :ok :targets order :written (boolean write) :changed changed
      :unexpected (vec (sort (remove (set (keys artifacts)) (actual-paths project order))))})))

(defn analyse
  "Reports configuration/render failures as structured findings suitable for repair tools."
  [project]
  (try
    (let [order (plan project) {:keys [artifacts]} (render project order)]
      {:status :ok :project (:id project) :targets order
       :artifacts (vec (keys artifacts)) :findings []})
    (catch Exception error
      {:status :error :project (:id project)
       :findings [{:kind :application-invalid :message (.getMessage error)
                   :data (ex-data error)}]})))

(defn verify
  "Runs declared checks relevant to a completed target plan. Check failures are data."
  [project {:keys [targets checks] :or {targets (plan project)}}]
  (let [selected (set targets)
        entries (if checks
                  (map (fn [key] [key (or (get-in project [:checks key])
                                         (throw (ex-info "Unknown project check" {:check key})))]) checks)
                  (sort-by key (:checks project)))
        results (mapv
                 (fn [[key check]]
                   (let [started (System/currentTimeMillis)]
                    (emit! {:type :check/started :check key})
                    (let [result (try
                     (let [result (if-let [command (:command check)]
                                    ((requiring-resolve 'app.manage.process/run)
                                     (assoc check :root (project-root project)))
                                    ((renderer (:run check))
                                     {:project project :root (project-root project) :targets targets}))]
                       (when-not (#{:ok :error} (:status result))
                         (throw (ex-info "Check must return :status :ok or :error" {:check key})))
                       (assoc result :id key))
                     (catch Exception error
                       {:id key :status :error :message (.getMessage error) :data (ex-data error)}))]
                      (emit! {:type :check/finished :check key :status (:status result)
                              :duration-ms (- (System/currentTimeMillis) started)})
                      result)))
                 (filter #(or checks (every? selected (:targets (second %)))) entries))]
    {:status (if (every? #(= :ok (:status %)) results) :ok :error) :checks results}))

(defn build
  "Generates selected targets and runs their declared verification checks."
  ([project] (build project {}))
  ([project {:keys [targets tests?] :or {tests? true}}]
   (let [generated (generate project {:targets targets :write true})
         verified (if tests? (verify project {:targets (:targets generated)}) {:status :ok :checks []})]
     {:status (if (seq (:unexpected generated)) :error (:status verified))
      :generation generated :verification verified})))

(defn test-project
  "Builds prerequisites for all declared checks and runs them; refuses an empty test suite."
  [project]
  (when-not (seq (:checks project))
    (throw (ex-info "Project has no verification checks" {:project (:id project)})))
  (let [checks (sort (keys (:checks project)))
        targets (vec (distinct (mapcat #(get-in project [:checks % :targets]) checks)))
        generated (generate project {:targets targets :write true})]
    (assoc (verify project {:targets (:targets generated) :checks checks}) :generation generated)))

(defn -main
  "Usage: app.manage PROJECT-VAR analyse|plan|check|generate|build|test|watch [options]"
  [project-var operation & options]
  (try
    (let [allowed (case operation "generate" #{"--write"} "build" #{"--no-tests"}
                               "watch" #{"--no-tests" "--watch-only"} #{})
          _ (when (some #(not (allowed %)) options)
              (throw (ex-info "Unknown management option" {:operation operation :options options})))
          project (var-get (requiring-resolve (symbol project-var)))
          result (case operation
                   "analyse" (analyse project)
                   "plan" (plan project)
                   "check" (check project)
                   "generate" (generate project {:write (boolean (some #{"--write"} options))})
                   "build" (build project {:tests? (not (some #{"--no-tests"} options))})
                   "test" (test-project project)
                   "watch" ((requiring-resolve 'app.manage.watch/watch-cli)
                            (symbol project-var)
                            {:tests? (not (some #{"--no-tests"} options))
                             :watch-only? (boolean (some #{"--watch-only"} options))})
                   (throw (ex-info "Unknown application management operation" {:operation operation})))]
      (pprint/pprint result)
      (shutdown-agents)
      (System/exit (if (= :error (:status result)) 1 0)))
    (catch Exception error
      (binding [*out* *err*] (println (.getMessage error)) (prn (ex-data error)))
      (shutdown-agents)
      (System/exit 1))))
