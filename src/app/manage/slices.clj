(ns app.manage.slices
  "Evidence-backed vertical slices shared by the management API, CLI and UI."
  (:require [app.manage :as manage] [clojure.edn :as edn] [clojure.java.io :as io])
  (:import [java.security MessageDigest]))

(defn- read-state [project path]
  (let [file (manage/project-file project path)]
    (if (.isFile file) (edn/read-string (slurp file)) {})))

(defn- write-state! [project path value]
  (let [file (manage/project-file project path)]
    (io/make-parents file)
    (let [temp (java.io.File/createTempFile "slice-" ".edn" (.getParentFile file))]
      (try (spit temp (pr-str value))
           (java.nio.file.Files/move (.toPath temp) (.toPath file)
             (into-array java.nio.file.CopyOption [java.nio.file.StandardCopyOption/REPLACE_EXISTING
                                                   java.nio.file.StandardCopyOption/ATOMIC_MOVE]))
           (finally (.delete temp))))))

(defn fingerprint
  ([project targets] (fingerprint project targets true))
  ([project targets outputs?]
  (let [order (manage/plan project targets)
        roots (concat (:project-sources project)
                      (mapcat #(concat (get-in project [:targets % :sources])
                                (when outputs? (get-in project [:targets % :outputs]))) order))
        files (sort-by str (distinct (mapcat (fn [path]
                  (let [f (manage/project-file project path)] (if (.isDirectory f) (file-seq f) [f]))) roots)))
        digest (MessageDigest/getInstance "SHA-256")]
    (.update digest (.getBytes (pr-str (mapv #(select-keys (get-in project [:targets %])
                                                [:sources :outputs :depends]) order)) "UTF-8"))
    (doseq [file files]
      (.update digest (.getBytes (str file) "UTF-8"))
      (when (.isFile file) (.update digest (java.nio.file.Files/readAllBytes (.toPath file)))))
    (format "%064x" (java.math.BigInteger. 1 (.digest digest))))))

(defn stage [project slice-id stage-id]
  (or (some #(when (= (name (:id %)) (name stage-id)) %) (get-in project [:slices (keyword slice-id) :stages]))
      (throw (ex-info "Unknown slice stage" {:http-status 400 :slice slice-id :stage stage-id}))))

(defn targets [project slice-id stage-id]
  (let [slice (or (get-in project [:slices (keyword slice-id)])
                  (throw (ex-info "Unknown slice" {:http-status 400})))]
    (if stage-id (:targets (stage project slice-id stage-id))
        (vec (distinct (mapcat :targets (remove :not-applicable (:stages slice))))))))

(defn record! [project selected result before]
  (let [path ".cache/app.manage/slice-evidence.edn"
        state (read-state project path)
        planned (set (manage/plan project selected))
        generation (or (:generation result) (when (:written result) result))
        verified (filter #(= :ok (:status %)) (or (get-in result [:verification :checks]) (:checks result)))]
    (write-state! project path
      (reduce (fn [out [slice-id slice]]
                (reduce (fn [out spec]
                  (let [key [slice-id (:id spec)] ts (:targets spec)
                        signature (fingerprint project ts)
                        stable? (= (get before key) (fingerprint project ts false))
                        generated? (and (seq ts) generation (every? planned ts))
                        checked? (and (seq (:checks spec))
                                      (every? (set (map :id verified)) (:checks spec)))]
                    (if (and stable? (or generated? checked?))
                      (assoc out key {:fingerprint signature :time (System/currentTimeMillis)
                                      :generated (or generated? (get-in out [key :generated]))
                                      :failed (boolean (some #(and (some #{(:id %)} (:checks spec)) (= :error (:status %)))
                                          (or (get-in result [:verification :checks]) (:checks result))))
                                      :verified (boolean checked?)}) out))) out (:stages slice)))
              state (:slices project)))))

(defn fingerprints [project]
  (into {} (for [[id slice] (:slices project) spec (:stages slice)]
             [[id (:id spec)] (fingerprint project (:targets spec) false)])))

(defn status [project]
  (let [evidence (read-state project ".cache/app.manage/slice-evidence.edn")
        work (read-state project ".app-manage/work.edn")]
    (mapv (fn [[id slice]]
      (assoc slice :id id :revision (get work :revision 0) :work (get-in work [:slices id])
        :stages (mapv (fn [spec]
          (let [e (get evidence [id (:id spec)])
                fingerprint (fingerprint project (:targets spec))
                output-files (mapcat #(get-in project [:targets % :outputs]) (:targets spec))
                present? (and (seq output-files) (every? #(.exists (manage/project-file project %)) output-files))]
            (assoc spec :evidence e :fingerprint fingerprint
              :status (cond (:not-applicable spec) :not-applicable
                            (:blocker spec) :blocked
                            (and e (not= fingerprint (:fingerprint e))) :stale
                            (:failed e) :failed
                            (:verified e) :verified
                            (:generated e) :generated
                            present? :unverified
                            :else :missing)))) (:stages slice)))) (sort-by key (:slices project)))))

(defn update-work! [project {:keys [slice revision note state]}]
  (when-not (get-in project [:slices (keyword slice)]) (throw (ex-info "Unknown slice" {:http-status 400})))
  (when-not (#{"planned" "active" "blocked"} state) (throw (ex-info "Invalid work state" {:http-status 400})))
  (when-not (and (string? note) (<= (count note) 16000)) (throw (ex-info "Invalid note" {:http-status 400})))
  (let [path ".app-manage/work.edn" current (read-state project path)]
    (when-not (= revision (get current :revision 0)) (throw (ex-info "Work notes changed; refresh first" {:http-status 409})))
    (let [next (-> current (assoc :revision (inc revision))
                          (assoc-in [:slices (keyword slice)] {:state state :note note}))]
      (write-state! project path next) next)))

(defn -main [project-var & [operation slice-id stage-id]]
  (let [project (var-get (requiring-resolve (symbol project-var)))]
    (try
      (if (or (nil? operation) (= operation "status"))
        (prn (status project))
        (let [selected (targets project slice-id stage-id)
              before (fingerprints project)
              result (case operation
                       "generate" (manage/generate project {:targets selected :write true})
                       "build" (manage/build project {:targets selected})
                       "check" (manage/check project selected)
                       (throw (ex-info "Use status, generate, build or check" {})))]
          (record! project selected result before)
          (prn result)))
      (finally (shutdown-agents)))))
