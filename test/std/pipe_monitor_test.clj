(ns std.pipe-monitor-test
  (:use code.test)
  (:require [std.pipe :as pipe]
            [std.pipe.monitor :as monitor]
            [std.task :as task]
            [std.lib :as h]))

(fact "monitor test"
  (pipe/pipe (task/task :default "monitor-test"
                   {:item {:list (constantly (range 10))}
                    :main {:fn (fn [x _ _ _]
                                 (Thread/sleep 50)
                                 (* x 10))}})
        :list
        {:bulk true
         :monitor true})
  => (contains {0 0, 1 10, 2 20, 3 30, 4 40, 5 50, 6 60, 7 70, 8 80, 9 90}))

(fact "unit test monitor"
  (let [inputs (range 5)
        m (monitor/create-monitor inputs identity)]
    @(:total m) => 5
    (count @(:pending m)) => 5
    (count @(:running m)) => 0

    (monitor/update-monitor m 0 :start)
    (count @(:pending m)) => 4
    (count @(:running m)) => 1

    (monitor/update-monitor m 0 :complete {:data "done"})
    (count @(:running m)) => 0
    (count @(:completed m)) => 1
    (count @(:results m)) => 1))
