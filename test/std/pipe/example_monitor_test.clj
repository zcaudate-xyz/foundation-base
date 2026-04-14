(ns std.pipe.example-monitor-test
  (:require [std.pipe :as pipe]
            [std.pipe.example-monitor :refer :all])
  (:use code.test))

^{:refer std.pipe.example-monitor/long-running-pipeline :added "4.1"}
(fact "creates the monitored long-running pipeline configuration"
  (let [captured (atom nil)]
    (with-out-str
      (with-redefs [pipe/pipe (fn [task input opts]
                                (reset! captured {:task task
                                                  :input input
                                                  :opts opts})
                                :ok)]
        (long-running-pipeline)))
    (let [{:keys [task input opts]} @captured]
      (and (some? task)
           (= input :list)
           (= (select-keys opts [:bulk :monitor :parallel])
              {:bulk true
               :monitor true
               :parallel true}))))
  => true)
