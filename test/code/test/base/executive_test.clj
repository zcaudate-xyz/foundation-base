(ns code.test.base.executive-test
  (:use [code.test :exclude [run]])
  (:require [code.test.base.executive :as executive]
            [code.test.base.context :as ctx]
            [code.test.base.print :as print]
            [code.test.base.listener :as listener]
            [clojure.string :as string]))

(fact "interim function correctly categorizes results"
  (let [facts [{:results [{:from :verify, :status :success, :data true, :meta {:path "a.clj"}}
                          {:from :verify, :status :success, :data false, :meta {:path "b.clj"}}
                          {:from :evaluate, :status :exception}
                          {:from :evaluate, :form :timeout}]}]]
    (executive/interim facts)
    => (contains {:files ["a.clj" "b.clj"],
                  :thrown [{:from :evaluate, :status :exception}],
                  :timedout [{:from :evaluate, :form :timeout}],
                  :facts facts,
                  :checks [{:from :verify, :status :success, :data true, :meta {:path "a.clj"}}
                           {:from :verify, :status :success, :data false, :meta {:path "b.clj"}}],
                  :passed [{:from :verify, :status :success, :data true, :meta {:path "a.clj"}}],
                  :failed [{:from :verify, :status :success, :data false, :meta {:path "b.clj"}}]})))

(fact "retrieve-line function extracts line numbers and references"
  (let [results {:passed [{:meta {:refer 'a, :line 1}}
                          {:meta {:refer 'b, :line 2}}]}]
    (executive/retrieve-line :passed results) => [[1 'a] [2 'b]]))

(defn- create-raw-items []
  {:failed [{:from :verify, :status :success, :data false,
             :actual {:data 3, :form '(+ 1 1)},
             :checker {:form '=>, :expect 2},
             :meta {:line 1, :path "fail.clj", :ns 'fail-ns, :desc "failing test"}}]
   :thrown [{:from :evaluate, :status :exception,
             :data (Exception. "err"),
             :form '(throw (Exception. "err")),
             :meta {:line 2, :path "throw.clj", :ns 'throw-ns, :desc "throwing test"}}]
   :passed [{:from :verify, :status :success, :data true,
             :actual {:data 2, :form '(+ 1 1)},
             :checker {:form '=>, :expect 2},
             :meta {:line 3, :path "pass.clj", :ns 'pass-ns, :desc "passing test"}}]})

(fact "summarise function prints a summary and returns counts"
  (let [items (create-raw-items)]
    (binding [print/*options* {:print-bulk true}]
      (let [output (with-out-str (executive/summarise items))]
        (and (string/includes? output "Failure")
             (string/includes? output "Thrown"))))
    => true))

(defn- run-accumulate-test []
  (ctx/with-context {:accumulator (atom nil)}
    (executive/accumulate (fn []
                            (ctx/notify {:id :my-test :data 1})
                            (ctx/notify {:id :my-test :data 2}))
                          :my-test)))

(fact "accumulate function gathers results from context notifications"
  (run-accumulate-test) => [{:id :my-test :data 1} {:id :my-test :data 2}])

(fact "summarise-bulk aggregates results and prints summary"
  (let [items {:ns1 {:data (create-raw-items)}
               :ns2 {:data {:timedout []}}
               :ns3 {:status :error}}]
    (binding [print/*options* {:print-bulk true}]
      (let [output (with-out-str (executive/summarise-bulk nil items nil))]
        (and (string/includes? output "Failure")
             (string/includes? output "Thrown"))))
    => true
    (executive/summarise-bulk nil items nil) => {:passed 1, :failed 1, :thrown 1, :timedout 0}))

^{:refer code.test.base.executive/unload-namespace :added "3.0"}
(fact "unloads a given namespace for testing")

^{:refer code.test.base.executive/load-namespace :added "3.0"}
(fact "loads a given namespace for testing")

^{:refer code.test.base.executive/test-namespace :added "3.0"}
(fact "runs a loaded namespace")

^{:refer code.test.base.executive/run-namespace :added "3.0"}
(fact "loads and run the namespace")

^{:refer code.gtest.base.executive/run-current :added "4.0"}
(fact "runs the current namespace (which can be a non test namespace)")

^{:refer code.test.base.executive/eval-namespace :added "3.0"}
(fact "evaluates the code within a specified namespace")
