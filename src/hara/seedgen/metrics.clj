(ns hara.seedgen.metrics
  "Stable JSON records used by XTBench CI and the documentation dashboard."
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.json :as json]))

(def +schema-version+ 1)
(def +history-limit+ 100)

(defn utc-now
  "returns the current time in ISO-8601 UTC format"
  {:added "4.1"}
  []
  (.toString (java.time.Instant/now)))

(defn- env
  [name]
  (let [value (System/getenv name)]
    (when (seq value) value)))

(defn- parse-long-safe
  [value]
  (when (seq value)
    (try (Long/parseLong value)
         (catch Throwable _ nil))))

(defn- selector-name
  [selector]
  (if (sequential? selector)
    (str/join "," (map str selector))
    (str selector)))

(defn- conclusion
  [summary errored]
  (if (pos? (+ (or (:failed summary) 0)
               (or (:throw summary) 0)
               (or (:timeout summary) 0)
               (count errored)))
    "failure"
    "success"))

(defn- github-context
  []
  (let [workflow   (or (env "XTBENCH_WORKFLOW") (env "GITHUB_WORKFLOW") "local")
        repository (or (env "GITHUB_REPOSITORY") "zcaudate-xyz/foundation-base")
        run-id     (env "GITHUB_RUN_ID")]
    {:workflow-key workflow
     :workflow-name (or (env "GITHUB_WORKFLOW") workflow)
     :git {:sha (env "GITHUB_SHA")
           :ref (env "GITHUB_REF")
           :event (env "GITHUB_EVENT_NAME")}
     :run {:id (parse-long-safe run-id)
           :number (parse-long-safe (env "GITHUB_RUN_NUMBER"))
           :attempt (or (parse-long-safe (env "GITHUB_RUN_ATTEMPT")) 1)
           :url (when run-id
                  (str "https://github.com/" repository "/actions/runs/" run-id))}}))

(defn test-record
  "creates a JSON-safe XTBench result record"
  {:added "4.1"}
  [selector langs {:keys [generated summary failures errored spec]}]
  (let [lang       (first langs)
        errored    (vec (or errored (:errored summary) []))
        context    (github-context)]
    (merge context
     {:schema-version +schema-version+
     :kind "xtbench-job"
     :metrics-status "complete"
     :suite (selector-name selector)
     :language (some-> lang name)
     :status (conclusion summary errored)
     :generated-count (reduce + 0 (map count (vals generated)))
     :tests {:passed (or (:passed summary) 0)
             :failed (or (:failed summary) 0)
             :throw (or (:throw summary) 0)
             :timeout (or (:timeout summary) 0)
             :skipped (or (:skipped summary) 0)
             :errored (count errored)}
     :failing-count (+ (or (:failed summary) 0)
                       (or (:throw summary) 0)
                       (or (:timeout summary) 0)
                       (count errored))
     :failure-counts {:failed (or (:failed summary) 0)
                      :throw (or (:throw summary) 0)
                      :timeout (or (:timeout summary) 0)
                      :errored (count errored)}
     :failures (vec (or failures []))
     :spec (when spec
             (select-keys spec [:model-count :test-count :coverage
                                :spec-feature-count :spec-implemented
                                :spec-abstract :spec-missing]))
     :recorded-at (utc-now)})))

(defn write-json!
  "writes pretty JSON, creating its parent directory"
  {:added "4.1"}
  [path value]
  (when-let [parent (fs/parent path)]
    (fs/create-directory parent))
  (spit path (str (json/write-pp value) "\n"))
  path)

(defn write-test-record!
  "writes one seedgen test result to disk"
  {:added "4.1"}
  [path selector langs result]
  (write-json! path (test-record selector langs result)))

(defn write-error-record!
  "writes a failed record when seed generation aborts unexpectedly"
  {:added "4.1"}
  [path selector langs error]
  (let [record (test-record selector langs
                            {:generated {}
                             :summary {:passed 0 :failed 0 :throw 0 :timeout 0
                                       :errored [(or (ex-message error) (str error))]}
                             :failing {}
                             :errored [(or (ex-message error) (str error))]})]
    (write-json! path (assoc record :error {:message (or (ex-message error) (str error))}))))

(defn read-json
  "reads JSON using keyword keys"
  {:added "4.1"}
  [path]
  (json/read (slurp path) json/+keyword-mapper+))

(defn aggregate-records
  "combines per-job records into a workflow run record"
  {:added "4.1"}
  ([workflow records]
   (aggregate-records workflow records (github-context)))
  ([workflow records context]
  (let [records (vec (sort-by (juxt :suite :language) records))
        totals  (reduce (fn [out record]
                          (merge-with + out (:tests record)))
                        {:passed 0 :failed 0 :throw 0 :timeout 0 :skipped 0 :errored 0}
                        records)
        failure-counts (reduce (fn [out record]
                                 (merge-with + out (:failure-counts record)))
                               {:failed 0 :throw 0 :timeout 0 :errored 0}
                               records)
        failed? (or (empty? records)
                    (some #(not= "success" (:status %)) records))
        sample  (first records)
        missing? (empty? records)
        meaningful (fn [m]
                     (into {} (remove (comp nil? val)) m))
        sample-workflow (:workflow-name sample)]
    {:schema-version +schema-version+
     :kind "xtbench-run"
     :metrics-status (if missing? "missing-artifacts" "complete")
     :workflow-key workflow
     :workflow-name (or (when-not (= "local" sample-workflow)
                          sample-workflow)
                        (:workflow-name context)
                        workflow)
     :status (if failed? "failure" "success")
     :tests totals
     :failure-counts failure-counts
     :jobs records
     :error (when missing?
              {:type "missing-metrics-artifacts"
               :message "No XTBench job metrics artifacts were published."})
     :git (if (get-in sample [:git :sha])
            (merge (:git context) (meaningful (:git sample)))
            (:git context))
     :run (if (get-in sample [:run :id])
            (merge (:run context) (meaningful (:run sample)))
            (:run context))
     :recorded-at (utc-now)})))

(defn history-entry
  "returns the compact entry stored in index.json"
  {:added "4.1"}
  [path record]
  (let [compact-jobs (mapv #(dissoc % :failures) (:jobs record))]
    (select-keys (assoc record :path path :jobs compact-jobs)
                 [:path :workflow-key :workflow-name :status :metrics-status
                  :tests :failure-counts :jobs :error :git :run :recorded-at])))

(defn update-index
  "adds a workflow record to an index and retains the newest entries"
  {:added "4.1"}
  ([index path record]
   (update-index index path record +history-limit+))
  ([index path record limit]
   (let [workflow (:workflow-key record)
         entry    (history-entry path record)
         old      (get-in index [:workflows workflow :runs] [])
         runs     (->> (cons entry old)
                       (reduce (fn [out item]
                                 (assoc out [(get-in item [:run :number])
                                             (get-in item [:run :attempt])] item))
                               {})
                       vals
                       (sort-by (juxt #(or (get-in % [:run :number]) -1)
                                      #(or (get-in % [:run :attempt]) -1))
                                #(compare %2 %1))
                       (take limit)
                       vec)]
     (-> (or index {})
         (assoc :schema-version +schema-version+
                :kind "xtbench-index"
                :updated-at (utc-now))
         (assoc-in [:workflows workflow]
                   {:latest (first runs)
                    :runs runs})))))

(defn merge-directory!
  "aggregates JSON job artifacts and updates a metrics branch directory"
  {:added "4.1"}
  [input-dir output-dir workflow]
  (let [files  (->> (file-seq (java.io.File. input-dir))
                    (filter #(.isFile ^java.io.File %))
                    (filter #(str/ends-with? (.getName ^java.io.File %) ".json")))
        jobs   (mapv (comp read-json str) files)
        record (aggregate-records workflow jobs)
        number (or (get-in record [:run :number]) 0)
        attempt (or (get-in record [:run :attempt]) 1)
        rel    (str "runs/" workflow "/" number "-" attempt ".json")
        index-path (str output-dir "/index.json")
        index  (if (.exists (java.io.File. index-path)) (read-json index-path) {})
        updated (update-index index rel record)
        retained (set (map :path (get-in updated [:workflows workflow :runs])))
        expired  (remove retained (map :path (get-in index [:workflows workflow :runs])))]
    (write-json! (str output-dir "/" rel) record)
    (write-json! (str output-dir "/latest/" workflow ".json") record)
    (doseq [path expired
            :let [file (java.io.File. output-dir path)]
            :when (.exists file)]
      (fs/delete (str file)))
    (write-json! index-path updated)
    record))
