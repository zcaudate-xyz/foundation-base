(ns xt.lang.common-task-test
  (:require [std.lang :as l]
            [xt.lang.common-task :as task])
  (:use code.test))

^{:refer xt.lang.common-task/task-run :added "4.0"}
(fact "emits task-run"
  (l/emit-as :js
    '(xt.lang.common-task/task-run (fn [] (+ 1 2))))
  => #"task_run|taskRun|x:task-run|Promise")

^{:refer xt.lang.common-task/task-then :added "4.0"}
(fact "emits task-then"
  (l/emit-as :js
    '(xt.lang.common-task/task-then t (fn [x] x)))
  => #"task_then|then|x:task-then")

^{:refer xt.lang.common-task/task-status :added "4.0"}
(fact "emits task-status"
  (l/emit-as :js
    '(xt.lang.common-task/task-status t))
  => #"task_status|status|x:task-status")
