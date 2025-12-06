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

(def +add-param-task+
  (task/task :default "add-param"
             {:main {:fn (fn [x params _]
                           (+ x (:add params)))}}))

(def +complex-calc-task+
  (task/task :default "complex"
             {:main {:fn (fn [x params _]
                           (-> x
                               (* (:factor params 1))
                               (+ (:offset params 0))))}}))

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

^{:refer std.pipe/chain :added "4.1"}
(fact "chains tasks with parameters"

  (pipe [+inc-task+ +add-param-task+]
        10
        {:add 5})
  => 16  ;; (inc 10) -> 11 -> (+ 11 5) -> 16

  (pipe [+complex-calc-task+ +double-task+]
        5
        {:factor 3 :offset 2})
  => 34  ;; (5 * 3 + 2) -> 17 -> (17 * 2) -> 34
  )

^{:refer std.pipe/chain :added "4.1"}
(fact "chains mixed task types (functions and task records)"
  (let [fn-task (fn [x _ _ _] (* x 10))]
    (pipe [+inc-task+ fn-task] 1)
    => 20

    (pipe [fn-task +inc-task+] 1)
    => 11))
