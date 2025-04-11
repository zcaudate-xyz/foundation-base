(ns std.task.process-test
  (:use code.test)
  (:require [std.task.process :refer :all]
            [std.task :as task]
            [jvm.namespace :as ns]
            [std.lib :as h]))

^{:refer std.task.process/main-function :added "3.0"}
(fact "creates a main function to be used for execution"

  (main-function ns-aliases 1)
  => (contains [h/vargs? false])

  (main-function ns-unmap 1)
  => (contains [h/vargs? true]))

^{:refer std.task.process/select-filter :added "4.0"}
(fact "matches given a range of filters"

  (select-filter #"ello" 'hello)
  => true

  (select-filter #"^ello" 'hello)
  => false

  (select-filter 'code 'code.test) => true

  (select-filter 'hara 'spirit.common) => false)

^{:refer std.task.process/select-inputs :added "3.0"}
(fact "selects inputs based on matches"

  (select-inputs {:item {:list (fn [_ _] ['code.test 'spirit.common])}}
                 {}
                 {}
                 ['code])
  => ['code.test])

^{:refer std.task.process/wrap-execute :added "3.0"}
(fact "enables execution of task with transformations")

^{:refer std.task.process/wrap-input :added "3.0"}
(fact "enables execution of task with single or multiple inputs")

^{:refer std.task.process/task-inputs :added "3.0"}
(fact "constructs inputs to the task given a set of parameters"

  (task-inputs (task/task :namespace "ns-interns" ns-interns)
               'std.task)
  => '[std.task {} {} {}]

  (task-inputs (task/task :namespace "ns-interns" ns-interns)
               {:bulk true})
  => '[std.task.process-test {:bulk true} {} {}])

^{:refer std.task.process/invoke :added "3.0"}
(fact "executes the task, given functions and parameters"

  (def world nil)

  (invoke (task/task :namespace "ns-interns" ns-interns))
  => {'world #'std.task.process-test/world,
      '*last* #'std.task.process-test/*last*})
