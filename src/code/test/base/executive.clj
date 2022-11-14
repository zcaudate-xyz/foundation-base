(ns code.test.base.executive
  (:require [std.lib :as h]
            [std.fs :as fs]
            [std.task :as task]
            [code.project :as project]
            [code.test.checker.common :as checker]
            [code.test.base.runtime :as rt]
            [code.test.base.listener :as listener]
            [code.test.base.print :as print]))

(defonce +latest+ (atom {}))

(defn accumulate
  "helper function for accumulating results over disparate facts and files"
  {:added "3.0"}
  ([func id]
   (let [sink (atom [])
         source rt/*accumulator*]
     (add-watch source id (fn [_ _ _ n]
                            (if (= (:id n) id)
                              (swap! sink conj n))))
     (binding [rt/*run-id* id]
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
         thrown  (filter #(-> % :type (= :exception)) forms)
         passed  (filter checker/succeeded? checks)
         failed  (filter (comp not checker/succeeded?) checks)
         facts   (filter (comp not empty? :results) facts)
         files   (->> checks
                      (map (comp :path :meta))
                      (frequencies)
                      (keys))]
     {:files  files
      :thrown thrown
      :facts  facts
      :checks checks
      :passed passed
      :failed failed})))

(defn retrieve-line
  "returns the line of the test"
  {:added "3.0"}
  ([key results]
   (->> (mapv (fn [result]
                (let [refer (-> result :meta :refer)
                      line (-> result :meta :line)]
                  [line (if refer (-> refer name symbol))]))
              (get results key)))))

(defn summarise
  "creates a summary of given results"
  {:added "3.0"}
  ([items]
   (let [summary (h/map-vals count items)]
     (when (:print-bulk print/*options*)
       (doseq [failed (:failed items)]
         (-> failed
             (listener/summarise-verify)
             (print/print-failure)))
       (doseq [thrown (:thrown items)]
         (-> thrown
             (listener/summarise-evaluate)
             (print/print-thrown)))
       (if (seq summary)
         (print/print-summary summary)))
     (swap! +latest+ assoc
            :failed (:failed items)
            :thrown (:thrown items))
     summary)))

(defn summarise-bulk
  "creates a summary of all bulk results"
  {:added "3.0"}
  ([_ items _]
   (let [_ (reset! +latest+ {})
         all-items (reduce (fn [out [id item]]
                             (reduce (fn [out [k data]]
                                       (update-in out [k] concat data))
                                     out
                                     (:data item)))
                           {}
                           (remove (fn [[ns item]]
                                     (when (= :error (:status item))
                                       (swap! +latest+ update-in [:errored] conj ns)
                                       true))
                                   items))]
     (summarise all-items))))

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
             rt/*eval-mode* false]
     (let [test-ns (unload-namespace ns nil lookup project)
           _       (when-let [path (or (lookup test-ns)
                                       (lookup ns))]
                     (load-file path))]
       test-ns))))

(defn test-namespace
  "runs a loaded namespace"
  {:added "3.0"}
  ([ns {:keys [run-id test] :as params} lookup project]
   (binding [rt/*root*     (:root project)
             rt/*errors*   (atom {})
             rt/*settings* (merge rt/*settings* params)]
     (let [run-id  (or run-id (h/uuid))
           test-ns (if rt/*eval-current-ns*
                     ns
                     (project/test-ns ns))
           sort-fn (case (:order test)
                     :random shuffle
                     (partial sort-by :line))
           tests   (->> (rt/all-facts test-ns)
                        (vals)
                        (sort-fn))
           facts   (accumulate (fn []
                                 (binding [*ns* (the-ns test-ns)]
                                   (let [_       (eval (rt/get-global ns :prelim))
                                         _       (eval (rt/get-global ns :setup))
                                         map-fn  (if (:parallel test)
                                                   pmap
                                                   map)
                                         output  (doall (map-fn #(%) tests))
                                         _       (eval (rt/get-global ns :teardown))]
                                     output)))
                               run-id)
           _       (rt/get-global ns :teardown)
           results (interim facts)]
       results))))

(defn run-namespace
  "loads and run the namespace"
  {:added "3.0"}
  ([ns params lookup project]
   (when (not std.task.process/*interrupt*)
     (load-namespace ns params lookup project)
     (let [results (test-namespace ns params lookup project)]
       (unload-namespace ns params lookup project)
       results))))

(defn run-current
  "runs the current namespace (which can be a non test namespace)"
  {:added "4.0"}
  ([ns params lookup project]
   (binding [rt/*eval-current-ns* true]
     (test-namespace ns params lookup project))))

(defn eval-namespace
  "evals the namespace"
  {:added "3.0"}
  ([ns {:keys [run-id] :as params} lookup project]
   (binding [*warn-on-reflection* false
             rt/*root*     (:root project)
             rt/*errors*   (atom {})
             rt/*settings* (merge rt/*settings* params)]
     (let [run-id (or run-id (h/uuid))
           test-ns (if rt/*eval-current-ns*
                     ns
                     (project/test-ns ns))
           facts   (accumulate (fn []
                                 (when-let [path (or (lookup test-ns)
                                                     (lookup ns))]
                                   (load-file path)))
                               run-id)
           results (interim facts)]
       results))))
