(ns code.test.base.executive-test
  (:use [code.test :exclude [run]])
  (:require [code.test.base.executive :as executive]
            [code.test.base.print :as print]
            [code.project :as project]
            [std.string :as str]
            [std.lib :as h]
            [code.test.base.runtime :as rt]))

(defn notify [data]
  (reset! rt/*accumulator* data))
                     
^{:refer code.test.base.executive/accumulate :added "3.0"}
(fact "accumulates test results from various facts and files into a single data structure"
  ^:hidden
  
  (let [result (rt/with-new-context {:accumulator (atom nil)}
                 (executive/accumulate (fn []
                                         (notify {:id :my-test :data 1})
                                         (notify {:id :my-test :data 2}))
                                       :my-test))]
    result)
  => [{:id :my-test :data 1} {:id :my-test :data 2}])

^{:refer code.test.base.executive/interim :added "3.0"}
(fact "summary function for accumulated results"
  (let [res (executive/interim [{:results [{:from :verify :status :success :data true :meta {:path "path"}}]}])]
    (:passed res))
  => (contains [{:from :verify :status :success :data true :meta {:path "path"}}]))

^{:refer code.test.base.executive/retrieve-line :added "3.0"}
(fact "returns the line of the test"
  (executive/retrieve-line :passed {:passed [{:meta {:line 1 :refer 'test}}]})
  => [[1 'test]])

^{:refer code.test.base.executive/summarise :added "3.0"}
(fact "creates a summary of given results"
  (binding [print/*options* #{:print-bulk}]
    (str/includes? (h/with-out-str
                     (executive/summarise {:passed [] :failed [] :throw [] :timeout []}))
                   "Summary"))
  => true)

^{:refer code.test.base.executive/summarise-bulk :added "3.0"}
(fact "creates a summary of all bulk results"
  (binding [print/*options* #{:print-bulk}]
    (str/includes? (h/with-out-str
                     (executive/summarise-bulk nil {:id {:data {:passed [] :failed [] :throw [] :timeout []}}} nil))
                   "Summary"))
  => true)

^{:refer code.test.base.executive/unload-namespace :added "3.0"}
(fact "unloads a given namespace for testing"
  (with-redefs [code.project/test-ns (fn [ns] ns)
                rt/list-links (fn [_] [])
                rt/purge-all (fn [_] nil)]
    (executive/unload-namespace 'my.ns nil nil nil))
  => 'my.ns)

^{:refer code.test.base.executive/load-namespace :added "3.0"}
(fact "loads a given namespace for testing"
  (with-redefs [executive/unload-namespace (fn [ns & _] ns)
                clojure.core/load-file (fn [_] nil)]
    (executive/load-namespace 'my.ns nil (fn [_] "path") nil))
  => 'my.ns)

^{:refer code.test.base.executive/test-namespace :added "3.0"}
(fact "runs a loaded namespace"
  (with-redefs [rt/all-facts (fn [_] {})
                executive/accumulate (fn [f id] [])
                executive/interim (fn [_] {})
                rt/get-global (fn [& _] nil)]
    (executive/test-namespace 'my.ns {} (fn [_] "path") {:root "."}))
  => {})

^{:refer code.test.base.executive/run-namespace :added "3.0"}
(fact "loads and run the namespace"
  (with-redefs [executive/load-namespace (fn [& _] nil)
                executive/test-namespace (fn [& _] {})
                executive/unload-namespace (fn [& _] nil)]
    (executive/run-namespace 'my.ns {} nil nil))
  => {})

^{:refer code.test.base.executive/run-current :added "4.0"}
(fact "runs the current namespace (which can be a non test namespace)"
  (with-redefs [executive/test-namespace (fn [& _] {})]
    (executive/run-current 'my.ns {} nil nil))
  => {})

^{:refer code.test.base.executive/eval-namespace :added "3.0"}
(fact "evaluates the code within a specified namespace"
  (with-redefs [code.project/test-ns (fn [ns] ns)
                executive/accumulate (fn [f id] [])
                executive/interim (fn [_] {})]
    (executive/eval-namespace 'my.ns {} (fn [_] "path") {:root "."}))
  => {})

(comment

  (def res (project/in-context (run 'std.lib-test)))

  (->> (:passed res)
       (count)))

(comment
  (->> (common/all-functions 'platform.storage-test)
       (sort-by (comp :line second))
       (map (juxt first (comp :line second))))

  (project/in-context (run 'std.lib))
  (project/in-context (load-namespace 'std.lib))
  (time (do
          (dotimes [i 10]
            (project/in-context (test-namespace 'std.lib {:test {:order :random
                                                                   ;;:parallel true
                                                                 }})))
          nil))
  (project/in-context (run 'platform.storage)))
