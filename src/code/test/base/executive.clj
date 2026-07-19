(ns code.test.base.executive
  (:require [clojure.string :as str]
            [code.project :as project]
            [code.test.base.context :as context]
            [code.test.base.listener :as listener]
            [code.test.base.print :as print]
            [code.test.base.process :as process]
            [code.test.base.runtime :as rt]
            [code.test.checker.common :as checker]
            [std.fs :as fs]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.print :as output]
            [std.print.ansi :as ansi]
            [std.lib.time :as t]
            [std.task :as task]))

(defonce +latest+ (atom {}))

(def ^:private +run-dir+
  ".hara/runs")

(def ^:private +run-history-file+
  "run-history.csv")

(def ^:private +run-history-columns+
  ["time" "command" "failures" "report"])

(defn report-edn-safe?
  "checks if a value can be written as EDN without coercion"
  {:added "4.1"}
  [x]
  (or (nil? x)
      (boolean? x)
      (string? x)
      (char? x)
      (keyword? x)
      (symbol? x)
      (number? x)
      (instance? java.util.UUID x)
      (instance? java.util.Date x)))

(defn report-edn
  "coerces a report value into an EDN-safe shape"
  {:added "4.1"}
  [x]
  (cond (report-edn-safe? x)
        x

        (instance? Throwable x)
        (cond-> {:tag :throwable
                 :class (.getName (class x))
                 :message (.getMessage ^Throwable x)}
          (ex-data x) (assoc :data (report-edn (ex-data x))))

        (map? x)
        (reduce-kv (fn [out k v]
                     (assoc out
                            (report-edn k)
                            (report-edn v)))
                   {}
                   x)

        (vector? x)
        (mapv report-edn x)

        (set? x)
        (into #{} (map report-edn) x)

        (seq? x)
        (apply list (map report-edn x))

        (sequential? x)
        (mapv report-edn x)

        :else
        (str x)))

(defn report-file-path
  "returns the output path for a saved test report"
  {:added "4.1"}
  []
  (str (fs/path context/*root* +run-dir+ (str "run-" (t/system-ms) ".edn"))))

(defn report->run-path
  "returns the repl helper path for a saved test report"
  {:added "4.1"}
  [report-path]
  (str/replace report-path #"\.edn$" ".run.edn"))

(defn run-file-path
  "returns the output path for a saved repl run helper"
  {:added "4.1"}
  [report-path params]
  (let [save-run (:save-run params)]
    (cond (string? save-run)
          (str (fs/path context/*root* save-run))

          (map? save-run)
          (if-let [path (:path save-run)]
            (str (fs/path context/*root* path))
            (report->run-path (or report-path (report-file-path))))

          save-run
          (report->run-path (or report-path (report-file-path))))))

(defn history-file-path
  "returns the output path for saved run history"
  {:added "4.1"}
  [{:keys [run-path]} params]
  (let [save-run (:save-run params)]
    (cond
      (map? save-run)
      (if-let [path (:history save-run)]
        (str (fs/path context/*root* path))
        (when run-path
          (str (fs/path (fs/parent run-path) +run-history-file+))))

      run-path
      (str (fs/path (fs/parent run-path) +run-history-file+)))))

(defn run-file-params
  "removes internal/default params from the saved repl run helper"
  {:added "4.1"}
  [params]
  (let [params (dissoc params
                       :title
                       :run-command
                       :save-run
                       :no-exit
                       :ns)]
    (cond-> params
      (= ["test"] (:test-paths params))
      (dissoc :test-paths)

      (= :summary (:return params))
      (dissoc :return)

      (= {:item true :result true :summary true} (:print params))
      (dissoc :print)

      (nil? (:test params))
      (dissoc :test))))

(defn run-file-form
  "creates the repl helper form for rerunning tests"
  {:added "4.1"}
  [selector params]
  (let [command (or (:run-command params)
                    'code.test/run)
        params  (run-file-params params)]
    (list 'do
          (list 'require (list 'quote 'code.test))
          (cond-> (list command
                        (list 'quote selector))
            (seq params)
            (concat [params])))))

(defn- csv-cell
  [value]
  (let [s (str (or value ""))]
    (if (re-find #"[\",\n\r]" s)
      (str "\"" (str/replace s #"\"" "\"\"") "\"")
      s)))

(defn- csv-row
  [values]
  (str (str/join "," (map csv-cell values)) "\n"))

(defn- failure-count
  [items]
  (+ (count (:failed items))
     (count (:throw items))
     (count (:timeout items))))

(defn save-run-history
  "appends the saved run metadata to csv history"
  {:added "4.1"}
  [items selector params artifacts]
  (let [history-path (history-file-path artifacts params)]
    (when history-path
      (let [history-file (fs/path history-path)
           header?      (not (fs/exists? history-file))
           timestamp    (str (java.time.Instant/ofEpochMilli (t/system-ms)))
           command      (when selector
                          (pr-str (run-file-form selector params)))
           report-path  (:report-path artifacts)]
        (fs/create-directory (fs/parent history-file))
        (spit history-path
             (str (when header?
                    (csv-row +run-history-columns+))
                  (csv-row [timestamp
                            command
                            (failure-count items)
                            report-path]))
             :append true)
        history-path))))

(defn save-artifact
  "saves a generated test artifact"
  {:added "4.1"}
  [_ path content]
  (when path
    (fs/create-directory (fs/parent path))
    (spit path content)
    path))

(defn artifact-notices
  "returns any save notices for written artifacts"
  {:added "4.1"}
  [{:keys [report-path run-path history-path]}]
  (cond-> []
    report-path
    (conj (str "Report saved to " report-path))

    run-path
    (conj (str "Run helper saved to " run-path))

    history-path
    (conj (str "Run history saved to " history-path))))

(defn save-report-paths
  "writes report artifacts and returns the written paths"
  {:added "4.1"}
  [items selector params]
  (let [process (fn [type item]
                  (case type
                    :failed (listener/summarise-verify item)
                    :throw  (if (= :verify (:from item))
                              (listener/summarise-verify item)
                              (listener/summarise-evaluate item))
                    :timeout (listener/summarise-evaluate item)))
        failures (reduce (fn [out k]
                           (let [data (map (comp report-edn (partial process k))
                                           (get items k))]
                             (if (seq data)
                               (assoc out k data)
                               out)))
                         {}
                         [:failed :throw :timeout])
        errored  (when (sequential? selector)
                   (seq (:errored @+latest+)))
        failures (if errored
                   (assoc failures :errored
                          (mapv #(report-edn {:ns %}) errored))
                   failures)
        report-path (when (seq failures)
                      (report-file-path))
        run-path    (run-file-path report-path params)]
    (when report-path
      (save-artifact "Report"
                     report-path
                     (with-out-str (clojure.pprint/pprint failures))))
    (when (and run-path selector)
      (save-artifact "Run helper"
                     run-path
                     (with-out-str
                       (clojure.pprint/pprint (run-file-form selector params)))))
    (let [artifacts {:report-path report-path
                     :run-path run-path}
          history-path (save-run-history items selector params artifacts)]
      (assoc artifacts :history-path history-path))))

(defn announce-artifacts
  "prints save notices for written artifacts"
  {:added "4.1"}
  [artifacts]
  (doseq [line (artifact-notices artifacts)]
    (output/println line))
  artifacts)

(defn load-report
  "loads a saved test report edn file"
  {:added "4.1"}
  ([path]
   (let [file (fs/path path)]
     (when-not (fs/exists? file)
       (throw (ex-info "Report file not found" {:path path})))
     (-> (slurp path)
         (read-string)))))

(defn report-failed-facts
  "groups failed/throw/timeout/errored report entries by namespace,
   returning a map of namespace -> set of refer symbols"
  {:added "4.1"}
  ([report]
   (let [by-function (->> (concat (:failed report)
                                  (:throw report)
                                  (:timeout report))
                          (group-by :ns)
                          (reduce (fn [out [ns entries]]
                                    (assoc out ns (->> entries
                                                       (map :function)
                                                       (remove nil?)
                                                       (set))))
                                  {}))]
     (reduce (fn [out entry]
               (let [ns (:ns entry)]
                 (assoc out ns #{})))
             by-function
             (:errored report)))))

(defn accumulate
  "accumulates test results from various facts and files into a single data structure"
  {:added "3.0"}
  ([func id]
   (let [sink (atom [])
         source context/*accumulator*]
     (add-watch source id (fn [_ _ _ n]
                            (if (= (:id n) id)
                              (swap! sink conj n))))
     (binding [context/*run-id* id]
       (func))
     (remove-watch source id)
     @sink)))

(defn interim
  "summary function for accumulated results"
  {:added "3.0"}
  ([facts]
   (let [results (mapcat :results facts)
         checks  (filter #(-> % :from (= :verify))    results)
         forms   (filter #(-> % :from (= :evaluate))  results)

         timeout      (filter #(-> % :form (= :timeout)) forms)
         thrown-forms  (filter #(and (-> % :status (= :exception))
                                     (not= :timeout (:form %))) forms)
         thrown-checks (filter #(or (and (-> % :actual :status (= :exception))
                                         (not (checker/succeeded? %)))
                                    (-> % :status (= :exception))) checks)
         thrown        (concat thrown-forms thrown-checks)

         passed  (filter checker/succeeded? checks)
         failed  (filter #(and (not (checker/succeeded? %))
                               (not= :exception (:status %))
                               (not= :exception (-> % :actual :status))) checks)

         facts   (filter (comp not empty? :results) facts)
         files   (->> checks
                      (map (comp :path :meta))
                      (frequencies)
                      (keys))]
     {:files  files
      :throw thrown
      :timeout timeout
      :facts  facts
      :checks checks
      :passed passed
      :failed failed})))

(defn retrieve-line
  "returns the line of the test"
  {:added "3.0"}
  ([key results]
    (let [items (or (get-in results [:data key])
                    (get results key)
                    (get-in (meta results) [:data key]))]
      (->> (mapv (fn [result]
                   (let [function-sym (listener/result-function result)
                         line (-> result :meta :line)]
                     [line (if function-sym (-> function-sym name symbol))]))
                 items)))))

(defn summarise
  "creates a summary of given results"
  {:added "3.0"}
  ([items]
   (let [skipped-ns (:skipped-ns items)
         summary (merge {:failed 0 :throw 0 :timeout 0}
                        (collection/map-vals count (dissoc items :skipped-ns)))
         summary (cond-> summary
                   skipped-ns (assoc :skipped-ns true))]
     #_(when (:print-bulk context/*print*)
       (doseq [item  (:failed items)]
         (-> item
             (listener/summarise-verify)
             (print/print-failed)))
       (doseq [item (:throw items)]
         (if (= :verify (:from item))
           (-> item
               (listener/summarise-verify)
               (print/print-throw))
           (-> item
               (listener/summarise-evaluate)
               (print/print-throw))))
       (doseq [item (:timeout items)]
         (-> item
             (listener/summarise-evaluate)
             (print/print-timeout)))
       (if (seq summary)
         (print/print-summary summary)))
     (swap! +latest+ assoc
            :failed  (:failed items)
            :throw   (:throw items)
            :timeout (:timeout items))
     (with-meta summary {:data (dissoc items :skipped-ns)}))))

(defn save-report
  "saves the report to .hara/runs"
  {:added "3.0"}
  ([items]
   (save-report items nil context/*settings*))
  ([items selector params]
    (-> (save-report-paths items selector params)
        (announce-artifacts)
        (:run-path))))

(def ^:private +bulk-report-keys+
  [:passed :failed :throw :timeout])

(defn- bulk-report-data
  "extracts detailed bulk report collections from an item result"
  {:added "4.1"}
  [item]
  (let [summary (:data item)
        details (or (some-> summary meta :data)
                    (:data (meta item))
                    summary)]
    (if (map? details)
      (select-keys details +bulk-report-keys+)
      {})))

(defn- merge-bulk-report-data
  [out item]
  (reduce (fn [out k]
            (let [data (get (bulk-report-data item) k)]
              (if (coll? data)
                (update out k (fnil into []) data)
                out)))
          out
          +bulk-report-keys+))

(defn- print-skipped-namespaces
  "prints a list of skipped namespaces"
  {:added "4.1"}
  [skipped]
  (when (seq skipped)
    (output/println)
    (output/println (str (ansi/style "SKIPPED" #{:bold :yellow})
                         " (" (count skipped) ")"))
    (doseq [ns (sort skipped)]
      (output/println (str "  " ns)))))

(defn summarise-bulk
  "creates a summary of all bulk results"
  {:added "3.0"}
  ([_ items _]
   (let [_           (reset! +latest+ {})
         item-entries (if (map? items) items (seq items))
         skipped-ns   (->> item-entries
                           (filter (fn [[ns item]]
                                     (let [summary (:data item)]
                                       (or (:skipped-ns summary)
                                           (:skipped-ns (meta summary))))))
                           (mapv first))
         all-items    (reduce (fn [out [id item]]
                                (merge-bulk-report-data out item))
                              {}
                              (remove (fn [[ns item]]
                                        (when (= :error (:status item))
                                          (swap! +latest+ update-in [:errored] conj ns)
                                          true))
                                      item-entries))]
       (print-skipped-namespaces skipped-ns)
       (let [artifacts (save-report-paths all-items
                                          (mapv first item-entries)
                                          context/*settings*)
             notices   (artifact-notices artifacts)]
         (cond-> (summarise all-items)
           (seq skipped-ns)
           (assoc :skipped (count skipped-ns))

           (seq notices)
           (vary-meta assoc :std.task/after-summary notices))))))

(defn unload-namespace
  "unloads a given namespace for testing"
  {:added "3.0"}
  ([ns _ lookup project]
   (let [test-ns (project/test-ns ns)
         links   (rt/list-links test-ns)
         _       (doseq [l links]
                   (unload-namespace l nil lookup project))
         _       (rt/purge-all test-ns)]
     test-ns)))

(defn load-namespace
  "loads a given namespace for testing"
  {:added "3.0"}
  ([ns _ lookup project]
   (binding [*warn-on-reflection* false
             context/*eval-mode* false]
     (let [test-ns (unload-namespace ns nil lookup project)
           _       (when-let [path (or (lookup test-ns)
                                       (lookup ns))]
                     (load-file path))]
       test-ns))))

(defn test-namespace
  "runs a loaded namespace"
  {:added "3.0"}
  ([ns {:keys [run-id test] :as params} lookup project]
   (binding [context/*root*     (:root project)
             context/*errors*   (atom {})
             context/*settings* (merge context/*settings* params)]
     (let [run-id       (or run-id (f/uuid))
           test-ns      (if context/*eval-current-ns*
                          ns
                          (project/test-ns ns))
           sort-fn      (case (:order test)
                          :random shuffle
                          (partial sort-by :line))
           filter-refs  (when-let [fact-filter (:filter params)]
                          (get fact-filter test-ns))
           fact-pred    (fn [fact]
                          (or (nil? filter-refs)
                              (empty? filter-refs)
                              (filter-refs (:refer fact))))
           tests        (->> (rt/all-facts test-ns)
                             (vals)
                             (filter fact-pred)
                             (sort-fn))
           skip?       (when-let [skip (rt/get-global ns :skip)]
                         (rt/eval-in-ns test-ns skip))
           ns-timeout  (or (:timeout-ns params) context/*timeout-ns-global*)
           run-facts   (fn []
                         (accumulate (fn []
                                       (binding [*ns* (the-ns test-ns)]
                                         (if skip?
                                           (doall (mapv #(process/skip-check %) tests))
                                           (let [_       (rt/eval-in-ns test-ns (rt/get-global ns :prelim))
                                                 _       (rt/eval-in-ns test-ns (rt/get-global ns :setup))
                                                 map-fn  (if (:parallel test)
                                                           pmap
                                                           map)
                                                 output  (doall (map-fn #(%) tests))
                                                 _       (rt/eval-in-ns test-ns (rt/get-global ns :teardown))]
                                             output))))
                                     run-id))
           facts       (if ns-timeout
                         (let [p       (promise)
                               thread  (doto (Thread. #(try
                                                         (deliver p (run-facts))
                                                         (catch Throwable t
                                                           (deliver p t))))
                                         (.setDaemon true)
                                         (.setName (str "code.test.namespace/" test-ns))
                                         (.start))]
                           (.join thread (long ns-timeout))
                           (if (.isAlive thread)
                             (do
                               (.interrupt thread)
                               (output/println
                                (str "NAMESPACE TIMEOUT: " test-ns
                                     " after " (t/format-ms ns-timeout)))
                               [{:results [{:from :evaluate
                                            :form :timeout
                                            :status :exception
                                            :actual {:status :timeout
                                                     :data ns-timeout}
                                            :meta {:ns test-ns}}]}])
                             (let [v @p]
                               (if (instance? Throwable v)
                                 (throw v)
                                 v))))
                         (run-facts))
            _           (rt/get-global ns :teardown)
            results     (-> (interim facts)
                            (assoc :queued (repeat (count tests) true))
                            (cond-> skip? (assoc :skipped-ns true)))]
        (when-not (:bulk params)
          (save-report results ns params))
        results))))

(defn run-namespace
  "loads and run the namespace"
  {:added "3.0"}
  ([ns params lookup project]
   (when (not std.task.process/*interrupt*)
     (load-namespace ns params lookup project)
     (binding [context/*print* (conj context/*print* :no-beep)]
       (let [results (test-namespace ns params lookup project)]
         (unload-namespace ns params lookup project)
         results)))))

(defn run-current
  "runs the current namespace (which can be a non test namespace)"
  {:added "4.0"}
  ([ns params lookup project]
   (binding [context/*eval-current-ns* true]
     (test-namespace ns params lookup project))))

(defn eval-namespace
  "evaluates the code within a specified namespace"
  {:added "3.0"}
  ([ns {:keys [run-id] :as params} lookup project]
   (binding [*warn-on-reflection* false
             context/*root*     (:root project)
             context/*errors*   (atom {})
             context/*settings* (merge context/*settings* params)]
     (let [run-id (or run-id (f/uuid))
           test-ns (if context/*eval-current-ns*
                     ns
                     (project/test-ns ns))
           facts   (accumulate (fn []
                                 (when-let [path (or (lookup test-ns)
                                                     (lookup ns))]
                                   (load-file path)))
                               run-id)
           results (interim facts)]
       results))))
