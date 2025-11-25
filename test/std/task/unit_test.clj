(ns std.task.unit-test
  (:use code.test)
  (:require [std.task.unit :as unit]
            [std.lib.result :as res]))

(fact "process-item with a simple function"
  (let [output (volatile! nil)]
    (unit/process-item {:f (fn [input & _] [input (res/result {:status :return :data (* input 2)})])
                        :idx 0
                        :total 1
                        :input 5
                        :output output
                        :display {}
                        :display-fn identity
                        :print {:item false}
                        :params {:bulk true}
                        :lookup {}
                        :env {}
                        :args []})
    (let [[k v] @output]
      (and (= k 5)
           (= (:status v) :return)
           (= (:data v) 10))) => true))
