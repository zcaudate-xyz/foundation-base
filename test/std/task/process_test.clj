(ns std.task.process-test
  (:use code.test)
  (:require [std.task.process :refer :all]
            [std.lib.result :as res]))

(defn- process-test-fn
  ([input params lookup env & args]
   (* 2 input)))

(def +task+
  {:item {:pre inc :post dec}
   :main {:fn process-test-fn}
   :result {:keys [[:data identity]]
            :columns [{:key :data}]}})

^{:refer std.task.process/wrap-execute :added "3.0"}
(fact "enables execution of task with transformations"

  ((wrap-execute process-test-fn +task+)
   1 {} {} {})
  => 3)

^{:refer std.task.process/wrap-input :added "3.0"}
(fact "enables execution of task with single or multiple inputs")
