(ns std.pipe.chain-test
  (:use code.test)
  (:require [std.pipe :refer :all]
            [std.task :as task]))

(def +inc-task+
  (task/task :default "inc"
             {:main {:fn (fn [x _ _] (inc x))}}))

(def +double-task+
  (task/task :default "double"
             {:main {:fn (fn [x _ _] (* x 2))}}))

(def +str-task+
  (task/task :default "str"
             {:main {:fn (fn [x _ _] (str "val: " x))}}))

^{:refer std.pipe/pipe :added "4.0"}
(fact "executes a chain of tasks where output feeds into input"

  (pipe [+inc-task+ +double-task+] 10)
  => 22

  (pipe [+double-task+ +inc-task+] 10)
  => 21

  (pipe [+inc-task+ +double-task+ +str-task+] 10)
  => "val: 22")

^{:refer std.pipe/pipe :added "4.0"}
(fact "executes a chain of tasks with bulk input"

  (pipe [+inc-task+ +double-task+]
        [1 2 3]
        {:bulk true})
  => (contains {1 4
                2 6
                3 8})

  (pipe [+inc-task+ +double-task+]
        [10 20]
        {:bulk true})
  => (contains {10 22
                20 42}))
