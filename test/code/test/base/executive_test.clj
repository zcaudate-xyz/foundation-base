(ns code.test.base.executive-test
  (:require [clojure.edn :as edn]
             [clojure.string]
             [code.project :as project]
             [code.test.base.context :as context]
             [code.test.base.executive :as executive]
             [code.test.base.print :as print]
             [code.test.base.runtime :as rt]
             [std.lib.env :as env]
             [std.print])
  (:use [code.test :exclude [run]]))

(defn notify [data]
  (reset! context/*accumulator* data))

^{:refer code.test.base.executive/report-edn-safe? :added "4.1"}
(fact "checks whether a value can be serialized to EDN without coercion"

  (executive/report-edn-safe? :hello)
  => true

  (executive/report-edn-safe? (atom 1))
  => false)

^{:refer code.test.base.executive/report-edn :added "4.1"}
(fact "coerces values into an EDN-safe representation"

  (executive/report-edn :ok)
  => :ok

  (executive/report-edn [(ex-info "boom" {:x 1}) #{1 2}])
  => [{:tag :throwable
       :class "clojure.lang.ExceptionInfo"
       :message "boom"
       :data {:x 1}}
      #{1 2}])

^{:refer code.test.base.executive/report-file-path :added "4.1"}
(fact "generates a timestamped report path under the run directory"

  (binding [context/*root* "/tmp/proj"]
    (with-redefs [std.lib.time/system-ms (fn [] 12345)]
      (executive/report-file-path)))
  => "/tmp/proj/.hara/runs/run-12345.edn")

^{:refer code.test.base.executive/report->run-path :added "4.1"}
(fact "derives the repl helper path from a report path"

  (executive/report->run-path "/tmp/.hara/runs/run-1.edn")
  => "/tmp/.hara/runs/run-1.run.edn")

^{:refer code.test.base.executive/run-file-path :added "4.1"}
(fact "resolves the repl helper path from params"

  (binding [context/*root* "/tmp/proj"]
    (executive/run-file-path "/tmp/proj/.hara/runs/run-1.edn" {:save-run true}))
  => "/tmp/proj/.hara/runs/run-1.run.edn"

  (binding [context/*root* "/tmp/proj"]
    (executive/run-file-path nil {:save-run "custom.run.edn"}))
  => "/tmp/proj/custom.run.edn")

^{:refer code.test.base.executive/run-file-params :added "4.1"}
(fact "strips internal params while preserving custom settings"

  (executive/run-file-params {:title "X"
                              :run-command 'code.test/run
                              :ns 'demo.core
                              :test {:parallel true}})
  => {:test {:parallel true}})

^{:refer code.test.base.executive/run-file-form :added "4.1"}
(fact "defaults to code.test/run when no run command is supplied"

  (executive/run-file-form 'demo.core {})
  => '(do (require (quote code.test))
          (code.test/run (quote demo.core))))

^{:refer code.test.base.executive/save-artifact :added "4.1"}
(fact "creates parent directories and writes content to the given path"

  (let [calls (atom [])]
    (with-redefs [std.fs/create-directory (fn [path]
                                            (swap! calls conj [:mkdir (str path)]))
                  clojure.core/spit (fn [path content & _]
                                      (swap! calls conj [:spit path content]))]
      (executive/save-artifact "Artifact" "/tmp/art/demo.txt" "hello"))
    @calls)
  => [[:mkdir "/tmp/art"]
      [:spit "/tmp/art/demo.txt" "hello"]])

^{:refer code.test.base.executive/artifact-notices :added "4.1"}
(fact "returns formatted notices for saved artifact paths"

  (executive/artifact-notices {:report-path "a.edn"
                               :run-path "a.run.edn"})
  => ["Report saved to a.edn"
      "Run helper saved to a.run.edn"])

^{:refer code.test.base.executive/save-report-paths :added "4.1"}
(fact "writes a run helper even when there are no failures"

  (let [captured (atom [])]
    (binding [context/*root* "/tmp/proj"]
      (with-redefs [executive/save-artifact (fn [label path _]
                                              (swap! captured conj [label path]))
                    executive/save-run-history (fn [& _]
                                                 "/tmp/proj/.hara/runs/run-history.csv")
                    std.lib.time/system-ms (fn [] 1)]
        [(executive/save-report-paths {:passed [] :failed [] :throw [] :timeout []}
                                      'demo.core
                                      {:save-run true})
         (count @captured)
         (first @captured)])))
  => [{:report-path nil
       :run-path "/tmp/proj/.hara/runs/run-1.run.edn"
       :history-path "/tmp/proj/.hara/runs/run-history.csv"}
      1
      ["Run helper" "/tmp/proj/.hara/runs/run-1.run.edn"]])

^{:refer code.test.base.executive/announce-artifacts :added "4.1"}
(fact "prints notices and returns the artifact map"

  (let [out (atom [])]
    (with-redefs [std.print/println (fn [& args]
                                      (swap! out conj (apply str args)))]
      [(executive/announce-artifacts {:report-path "r.edn"})
       @out]))
  => [{:report-path "r.edn"}
      ["Report saved to r.edn"]])

^{:refer code.test.base.executive/accumulate :added "3.0"}
(fact "accumulates test results from various facts and files into a single data structure"

  (let [result (context/with-new-context {:accumulator (atom nil)}
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
  => [[1 'test]]

  (executive/retrieve-line :passed {:passed [{:meta {:line 2 :function 'xtgen/generate-common-lib}}]})
  => [[2 'generate-common-lib]])

^{:refer code.test.base.executive/summarise :added "3.0"}
(fact "creates a summary of given results"
  (executive/summarise {:passed [] :failed [] :throw [] :timeout []})
  => (contains {:passed 0
                :failed 0
                :throw 0
                :timeout 0}))

^{:refer code.test.base.executive/save-report :added "4.1"}
(fact "writes EDN-safe run reports"
  (let [report  (atom nil)
        wrapped (reify Object
                  (toString [_]
                    "#<Wrapped@1: \"oops\">"))]
    (binding [context/*root* "."]
      (with-redefs [std.fs/create-directory (fn [_] :created)
                    clojure.core/spit (fn [_ content]
                                        (reset! report content))
                    std.print/println (fn [& _] :printed)]
        (executive/save-report
         {:failed [{:status :success
                    :data false
                    :from :verify
                    :meta {:path "path"
                           :function 'demo/fn
                           :ns 'demo.core-test
                           :line 10
                           :desc "demo"}
                    :checker {:tag :satisfies
                              :form :ok
                              :fn inc}
                    :actual {:type :code/test
                             :status :success
                             :data [wrapped (ex-info "bad" {:demo true})]
                             :form '(demo)
                             :from :evaluate}}]}))
      (let [entry (-> @report edn/read-string :failed first)]
        [(string? (get-in entry [:checker :fn]))
         (get-in entry [:actual :data 0])
         (get-in entry [:actual :data 1 :tag])
         (get-in entry [:actual :data 1 :message])])))
  => [true "#<Wrapped@1: \"oops\">" :throwable "bad"])

^{:refer code.test.base.executive/summarise-bulk :added "4.1"}
(fact "aggregates detailed failures from wrapped namespace summaries"
  (let [captured (atom nil)
        summary-a (with-meta {:passed 2 :failed 1 :throw 0 :timeout 0}
                    {:data {:passed [:ok-a :ok-b]
                            :failed [{:meta {:line 10 :ns 'demo.alpha-test}}]
                            :throw []
                            :timeout []}})
        summary-b (with-meta {:passed 1 :failed 0 :throw 1 :timeout 0}
                    {:data {:passed [:ok-c]
                            :failed []
                            :throw [{:meta {:line 20 :ns 'demo.beta-test}}]
                            :timeout []}})]
    (binding [context/*settings* {:save-run false}]
      (with-redefs [executive/save-report-paths (fn [items selector params]
                                                  (reset! captured [items selector params])
                                                  {})]
        (executive/summarise-bulk nil
                                  {'demo.alpha-test {:data summary-a}
                                   'demo.beta-test  {:data summary-b}}
                                  nil)
        @captured)))
  => [{:failed [{:meta {:line 10 :ns 'demo.alpha-test}}]
       :throw [{:meta {:line 20 :ns 'demo.beta-test}}]
       :timeout []}
      ['demo.alpha-test 'demo.beta-test]
      {:save-run false}])

^{:refer code.test.base.executive/summarise-bulk :added "4.1"}
(fact "prints and counts skipped namespaces"
  (let [output   (atom [])
        summary-a (with-meta {:passed 2 :failed 0 :throw 0 :timeout 0 :skipped-ns true}
                    {:data {:passed [:ok-a :ok-b]
                            :failed []
                            :throw []
                            :timeout []}})
        summary-b (with-meta {:passed 1 :failed 0 :throw 0 :timeout 0}
                    {:data {:passed [:ok-c]
                            :failed []
                            :throw []
                            :timeout []}})]
    (binding [context/*settings* {:save-run false}]
      (with-redefs [executive/save-report-paths (fn [& _] {})
                    std.print/println (fn [& args] (swap! output conj (apply str args)))]
        (let [result (executive/summarise-bulk nil
                                               {'demo.skipped-test {:data summary-a}
                                                'demo.normal-test  {:data summary-b}}
                                               nil)]
          [(:skipped result)
           (boolean (some #(re-find #"SKIPPED" %) @output))
           (boolean (some #(re-find #"demo\.skipped-test" %) @output))]))))
  => [1 true true])

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
  => '{:queued ()})

^{:refer code.test.base.executive/test-namespace :added "4.1"}
(fact "skips all facts when :skip form evaluates to true"

  (let [executed (atom false)
        fact-obj (fn [] (reset! executed true) :ran)]
    (with-redefs [rt/all-facts (fn [_] {'test-fact fact-obj})
                  rt/get-global (fn [ns k]
                                  (case k
                                    :skip '(not false)
                                    nil))
                  project/test-ns (fn [ns] 'user)
                  executive/accumulate (fn [f id] (f))
                  executive/interim (fn [facts] {:facts facts})]
      [(executive/test-namespace 'my.ns {:bulk true} (fn [_] "path") {:root "."})
       @executed]))
  => [{:facts [:skipped] :queued '(true) :skipped-ns true} false])

^{:refer code.test.base.executive/test-namespace :added "4.1"}
(fact "runs facts normally when :skip form evaluates to false"

  (let [executed (atom false)
        fact-obj (fn [] (reset! executed true) :ran)]
    (with-redefs [rt/all-facts (fn [_] {'test-fact fact-obj})
                  rt/get-global (fn [ns k]
                                  (case k
                                    :skip '(not true)
                                    nil))
                  project/test-ns (fn [ns] 'user)
                  executive/accumulate (fn [f id] (f))
                  executive/interim (fn [facts] {:facts facts})]
      [(executive/test-namespace 'my.ns {:bulk true} (fn [_] "path") {:root "."})
       @executed]))
  => [{:facts [:ran] :queued '(true)} true])

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


^{:refer code.test.base.executive/history-file-path :added "4.1"}
(fact "returns nil when no run path and no custom history path is provided"

  (executive/history-file-path nil {:save-run {:path "x.run.edn"}})
  => nil)

^{:refer code.test.base.executive/save-run-history :added "4.1"}
(fact "appends a csv row describing the run"

  (let [calls (atom [])]
    (with-redefs [std.fs/exists? (fn [_] false)
                  std.fs/create-directory (fn [path]
                                            (swap! calls conj [:mkdir (str path)]))
                  clojure.core/spit (fn [path content & _]
                                      (swap! calls conj [:spit path content]))
                  std.lib.time/system-ms (fn [] 123456789)]
      [(executive/save-run-history {:failed [{:meta {}}] :throw [] :timeout []}
                                   'demo.core
                                   {:save-run true :run-command 'code.test/run}
                                   {:run-path "/tmp/.hara/runs/run-1.run.edn"})
       (let [[mkdir [_ _ content]] @calls]
         [mkdir
          (boolean (re-find #"time,command,failures,report" content))
          (boolean (re-find #",1," content))
          (clojure.string/ends-with? content "\n")])]))
  => ["/tmp/.hara/runs/run-history.csv"
      [[:mkdir "/tmp/.hara/runs"] true true true]])

^{:refer code.test.base.executive/load-report :added "4.1"}
(fact "reads EDN from an existing report file"

  (with-redefs [std.fs/exists? (fn [_] true)
                clojure.core/slurp (fn [_] "{:failed [1 2]}")]
    (executive/load-report "/tmp/report.edn"))
  => {:failed [1 2]}

  (with-redefs [std.fs/exists? (fn [_] false)]
    (executive/load-report "/tmp/missing.edn"))
  => (throws))

^{:refer code.test.base.executive/report-failed-facts :added "4.1"}
(fact "groups failed, throw, timeout and errored entries by namespace"

  (executive/report-failed-facts
   {:failed [{:ns 'demo.core-test :function 'demo/f}
             {:ns 'demo.core-test :function 'demo/g}]
    :throw [{:ns 'demo.other-test :function 'demo/h}]
    :timeout [{:ns 'demo.other-test}]
    :errored [{:ns 'demo.err-test}]})
  => {'demo.core-test #{'demo/f 'demo/g}
      'demo.other-test #{'demo/h}
      'demo.err-test #{}})