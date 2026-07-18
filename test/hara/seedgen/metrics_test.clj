(ns hara.seedgen.metrics-test
  (:require [hara.seedgen.metrics :refer :all])
  (:use code.test))

^{:refer hara.seedgen.metrics/aggregate-records :added "4.1"}
(fact "aggregates suite and language job records semantically"
  (let [record (aggregate-records
                "core"
                [{:workflow-name "XTBench Core"
                  :suite "xt.lang" :language "lua" :status "success"
                  :tests {:passed 5 :failed 0 :throw 0 :timeout 0 :skipped 1 :errored 0}
                  :run {:number 4 :attempt 1}}
                 {:workflow-name "XTBench Core"
                  :suite "xt.event" :language "lua" :status "failure"
                  :tests {:passed 3 :failed 1 :throw 0 :timeout 0 :skipped 0 :errored 0}
                  :run {:number 4 :attempt 1}}])]
    (select-keys record [:workflow-key :status :tests]))
  => {:workflow-key "core"
      :status "failure"
      :tests {:passed 8 :failed 1 :throw 0 :timeout 0 :skipped 1 :errored 0}}

  (select-keys (aggregate-records "core" [])
               [:workflow-key :workflow-name :status :tests :jobs])
  => {:workflow-key "core"
      :workflow-name "core"
      :status "failure"
      :tests {:passed 0 :failed 0 :throw 0 :timeout 0 :skipped 0 :errored 0}
      :jobs []})

^{:refer hara.seedgen.metrics/update-index :added "4.1"}
(fact "keeps rerun attempts distinct and applies retention"
  (let [record (fn [n attempt]
                 {:workflow-key "core" :status "success" :tests {}
                  :run {:number n :attempt attempt}})
        index  (-> {}
                   (update-index "runs/core/1-1.json" (record 1 1) 2)
                   (update-index "runs/core/2-1.json" (record 2 1) 2)
                   (update-index "runs/core/2-2.json" (record 2 2) 2))]
    (mapv (juxt #(get-in % [:run :number])
                #(get-in % [:run :attempt]))
          (get-in index [:workflows "core" :runs])))
  => [[2 2] [2 1]])
