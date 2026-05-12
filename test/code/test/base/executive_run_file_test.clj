(ns code.test.base.executive-run-file-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [code.test.base.context :as context]
            [code.test.base.executive :as executive]
            [code.test.base.runtime :as rt]
            [std.fs :as fs])
  (:use code.test))

^{:refer code.test.base.executive/run-file-params :added "4.1"}
(fact "removes internal and default params from saved repl runs"
  (executive/run-file-params
   {:title "TEST PROJECT"
    :run-command 'code.test/run
    :save-run true
    :print {:item true :result true :summary true}
    :return :summary
    :test-paths ["test"]
    :test {:order :random}})
  => {:test {:order :random}})

^{:refer code.test.base.executive/run-file-form :added "4.1"}
(fact "builds a copy-pastable repl form"
  (executive/run-file-form
   'demo.core
   {:save-run true
    :run-command 'code.test/run
    :test {:order :random}})
  => '(do
        (require (quote code.test))
         (code.test/run
          (quote demo.core)
          {:test {:order :random}})))

^{:refer code.test.base.executive/run-file-path :added "4.1"}
(fact "places the repl helper next to the report by default"
  (executive/run-file-path
   "/tmp/demo/.hara/runs/run-10.edn"
   {:save-run true})
  => "/tmp/demo/.hara/runs/run-10.run.edn"

  (executive/run-file-path
   nil
   {:save-run "tmp/demo.run.edn"})
  => #".*/tmp/demo\.run\.edn$")

^{:refer code.test.base.executive/history-file-path :added "4.1"}
(fact "places run history next to the saved helper by default or at a custom path"
  (executive/history-file-path
   {:run-path "/tmp/demo/.hara/runs/run-10.run.edn"}
   {:save-run true})
  => "/tmp/demo/.hara/runs/run-history.csv"

  (binding [context/*root* "/tmp/demo"]
    (executive/history-file-path
     {:run-path "/tmp/demo/tmp/demo.run.edn"}
     {:save-run {:path "tmp/demo.run.edn"
                 :history "tmp/history.csv"}}))
  => "/tmp/demo/tmp/history.csv")

^{:refer code.test.base.executive/save-report :added "4.1"}
(fact "writes the repl helper through the report save flow"
  (let [root (str (fs/create-tmpdir "executive-run-file"))]
    (try
      (binding [context/*root* root]
        (let [path (executive/save-report
                    {:passed []
                     :failed []
                     :throw []
                     :timeout []}
                    'demo.core
                    {:save-run true
                     :run-command 'code.test/run
                     :test {:order :random}})
              form (edn/read-string (slurp path))]
          [(boolean (re-find #"\.hara/runs/run-\d+\.run\.edn$" path))
           (.endsWith path ".run.edn")
            (first form)
            (-> form second first)
            (-> form (nth 2) first)
            (-> form last last)]))
      (finally
        (fs/delete root))))
  => [true
      true
      'do
      'require
      'code.test/run
      {:test {:order :random}}])

^{:refer code.test.base.executive/save-report :added "4.1"}
(fact "appends saved run metadata to csv history"
  (let [root    (str (fs/create-tmpdir "executive-run-history"))
       params   {:save-run {:path "tmp/demo.run.edn"
                            :history "tmp/history.csv"}
                 :run-command 'code.test/run}
       history  (str (fs/path root "tmp/history.csv"))]
    (try
      (binding [context/*root* root]
       (executive/save-report
        {:passed []
         :failed [{:meta {:ns 'demo.core-test}}]
         :throw []
         :timeout []}
        'demo.core
        params)
       (executive/save-report
        {:passed []
         :failed []
         :throw []
         :timeout []}
        'demo.core
        params)
       (let [lines (str/split-lines (slurp history))]
         [(count lines)
          (first lines)
          (every? #(re-find #"code\.test/run" %) (rest lines))
          (boolean (re-find #",1,.*run-\d+\.edn" (second lines)))
          (boolean (re-find #",0,$" (last lines)))]))
      (finally
       (fs/delete root))))
  => [3
      "time,command,failures,report"
      true
      true
      true])

^{:refer code.test.base.executive/test-namespace :added "4.1"}
(fact "routes single namespace helper saving through save-report"
  (let [captured (atom nil)]
    (with-redefs [rt/all-facts (fn [_] {})
                  executive/accumulate (fn [& _] [])
                  executive/interim (fn [_] {})
                  rt/get-global (fn [& _] nil)
                  executive/save-report (fn [items selector params]
                                          (reset! captured [items selector params]))]
      (executive/test-namespace 'demo.core
                                {:save-run true
                                 :run-command 'code.test/run}
                                nil
                                {:root "."})
      @captured))
  => [{:queued ()}
      'demo.core
      {:save-run true
        :run-command 'code.test/run}])

^{:refer code.test.base.executive/test-namespace :added "4.1"}
(fact "skips per-namespace report saving for bulk runs"
  (let [captured (atom nil)]
    (with-redefs [rt/all-facts (fn [_] {})
                  executive/accumulate (fn [& _] [])
                  executive/interim (fn [_] {})
                  rt/get-global (fn [& _] nil)
                  executive/save-report (fn [items selector params]
                                          (reset! captured [items selector params]))]
      (executive/test-namespace 'demo.core
                                {:bulk true
                                 :save-run true
                                 :run-command 'code.test/run}
                                nil
                                {:root "."})
      @captured))
  => nil)

^{:refer code.test.base.executive/summarise-bulk :added "4.1"}
(fact "routes bulk helper saving through save-report"
  (let [captured (atom nil)]
    (binding [context/*settings* {:save-run true
                                  :run-command 'code.test/run}]
      (with-redefs [executive/save-report (fn [items selector params]
                                            (reset! captured [items selector params]))
                    executive/summarise (fn [_] :summary)]
        [(executive/summarise-bulk nil
                                   {'demo.alpha-test {:data {:passed []
                                                             :failed []
                                                             :throw []
                                                             :timeout []}}
                                    'demo.beta-test  {:data {:passed []
                                                             :failed []
                                                             :throw []
                                                              :timeout []}}}
                                   nil)
         @captured])))
  => [:summary
      [{:passed []
        :failed []
        :throw []
        :timeout []}
       ['demo.alpha-test 'demo.beta-test]
       {:save-run true
        :run-command 'code.test/run}]])

^{:refer code.test.base.executive/summarise-bulk :added "4.1"}
(fact "defers bulk save notices until after summary printing"
  (binding [context/*settings* {:save-run true
                                :run-command 'code.test/run}]
    (with-redefs [executive/save-report-paths (fn [& _]
                                                {:report-path "tmp/report.edn"
                                                 :run-path "tmp/report.run.edn"
                                                 :history-path "tmp/run-history.csv"})]
      (-> (executive/summarise-bulk nil
                                    {'demo.alpha-test {:data {:passed []
                                                              :failed []
                                                              :throw []
                                                              :timeout []}}}
                                    nil)
          meta
          :std.task/after-summary)))
  => ["Report saved to tmp/report.edn"
      "Run helper saved to tmp/report.run.edn"
      "Run history saved to tmp/run-history.csv"])
