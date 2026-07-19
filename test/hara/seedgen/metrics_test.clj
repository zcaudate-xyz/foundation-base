(ns hara.seedgen.metrics-test
  (:require [hara.seedgen.metrics :refer :all])
  (:use code.test))

^{:refer hara.seedgen.metrics/test-record :added "4.1"}
(fact "serializes supplied test results without consulting runtime audit state"
  (let [path   "target/xtbench-metrics/test-record.json"
        record (test-record '[xt.lang] [:python]
                            {:generated {:python ['xtbench.python.lang-test]}
                             :summary {:passed 7 :failed 0 :throw 0 :timeout 0 :skipped 1}
                             :failing {}
                             :spec {:model-count 3 :test-count 2 :coverage 0.5
                                    :spec-feature-count 4 :spec-implemented 3
                                    :spec-abstract 0 :spec-missing 1}})]
    (write-json! path record)
    (select-keys (read-json path)
                 [:language :status :metrics-status :generated-count :tests :spec]))
  => {:language "python"
      :status "success"
      :metrics-status "complete"
      :generated-count 1
      :tests {:passed 7 :failed 0 :throw 0 :timeout 0 :skipped 1 :errored 0}
      :spec {:model-count 3 :test-count 2 :coverage 0.5
             :spec-feature-count 4 :spec-implemented 3
             :spec-abstract 0 :spec-missing 1}})

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
               [:workflow-key :status :metrics-status :tests :jobs :error])
  => {:workflow-key "core"
      :status "failure"
      :metrics-status "missing-artifacts"
      :tests {:passed 0 :failed 0 :throw 0 :timeout 0 :skipped 0 :errored 0}
      :jobs []
      :error {:type "missing-metrics-artifacts"
              :message "No XTBench job metrics artifacts were published."}})

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
  => [[2 2] [2 1]]

  (let [legacy {:schema-version 1
                :workflows {"core" {:runs [{:path "runs/core/0-1.json"
                                             :run nil}]}}}
        current {:workflow-key "core" :status "failure"
                 :metrics-status "missing-artifacts"
                 :error {:type "missing-metrics-artifacts"}
                 :tests {} :jobs [] :run {:number 9 :attempt 1}}
        updated (update-index legacy "runs/core/9-1.json" current)]
    (mapv (juxt :path :metrics-status #(get-in % [:error :type]))
          (get-in updated [:workflows "core" :runs])))
  => [["runs/core/9-1.json" "missing-artifacts" "missing-metrics-artifacts"]
      ["runs/core/0-1.json" nil nil]])
