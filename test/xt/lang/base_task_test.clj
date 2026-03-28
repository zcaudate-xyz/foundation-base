(ns xt.lang.base-task-test
  (:require [std.lang :as l]
            [xt.lang.base-task :as task])
  (:use code.test))

^{:refer xt.lang.base-task/task-run :added "4.0"}
(fact "emits task-run"
  (l/emit-as :js
    '(task/task-run (fn [] (+ 1 2))))
  => #"x\\.taskRun|x:task-run|Promise")

^{:refer xt.lang.base-task/task-then :added "4.0"}
(fact "emits task-then"
  (l/emit-as :js
    '(task/task-then t (fn [x] x)))
  => #"then|x:task-then")

^{:refer xt.lang.base-task/task-status :added "4.0"}
(fact "emits task-status"
  (l/emit-as :js
    '(task/task-status t))
  => #"status|x:task-status")
