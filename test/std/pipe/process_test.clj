(ns std.pipe.process-test
  (:use code.test)
  (:require [std.pipe.process :refer :all]
            [std.lib.result :as res]
            [std.pipe :as pipe]))

(defn- test-task [f]
  (pipe/task {:main {:fn f :argcount 1}}))

^{:refer std.pipe.process/wrap-bulk :added "4.1"}
(fact "wraps the function to handle bulk execution"

  (let [t (pipe/task {:main {:fn inc :argcount 1}})
        f (wrap-bulk (fn [x & _] [x (res/->result x (inc x))]) t)]
    (f [1 2 3] {:bulk true} {} {})
    => {1 2, 2 3, 3 4}))

^{:refer std.pipe.process/wrap-input :added "4.1"}
(fact "enables execution of task with single or multiple inputs"

  (let [t (pipe/task {:main {:fn inc :argcount 1}})
        f (wrap-input (fn [x & _] (inc x)) t)]
    (f 1 {} {} {}) => 2))

^{:refer std.pipe.process/wrap-main :added "4.1"}
(fact "wraps the main function for the task"

  (let [t (pipe/task {:main {:fn inc :argcount 1}})
        f (wrap-main t)]
    (f 1 {} {} {}) => 2))

^{:refer std.pipe.process/invoke :added "4.1"}
(fact "executes the task"

  (invoke (pipe/task {:main {:fn inc :argcount 1}}) 1)
  => 2)
