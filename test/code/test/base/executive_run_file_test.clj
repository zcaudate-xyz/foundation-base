(ns code.test.base.executive-run-file-test
  (:require [clojure.edn :as edn]
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
