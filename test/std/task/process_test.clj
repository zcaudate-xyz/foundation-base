(ns std.task.process-test
  (:use code.test)
  (:require [std.task.process :refer :all]
            [std.lib.result :as res]
            [std.task :as task]
            [std.lib :as h]))

(defn- process-test-fn
  ([input params lookup env & args]
   (* 2 input)))

(def +task+
  {:item {:pre inc :post dec}
   :main {:fn process-test-fn}
   :result {:keys [[:data identity]]
            :columns [{:key :data}]}})

^{:refer std.task.process/main-function :added "4.0"}
(fact "creates a main function to be used for execution"
  ^:hidden
  
  (main-function ns-aliases 1)
  => (contains [h/vargs? false]))

^{:refer std.task.process/select-filter :added "4.0"}
(fact "matches given a range of filters"
  ^:hidden
  
  (select-filter #"ello" 'hello)
  => true
  (select-filter #"^ello" 'hello)
  => false)

^{:refer std.task.process/select-inputs :added "4.0"}
(fact "selects inputs based on matches"
  ^:hidden

  (select-inputs {:item {:list (fn [_ _] ['code.test 'spirit.common])}}
                 {}
                 {}
                 ['code])
  => ['code.test])

^{:refer std.task.process/wrap-execute :added "3.0"}
(fact "enables execution of task with transformations"
  
  ((wrap-execute process-test-fn +task+)
   1 {} {} {})
  => 3)

^{:refer std.task.process/wrap-input :added "3.0"}
(fact "enables execution of task with single or multiple inputs"
  ((wrap-input process-test-fn +task+)
   1 {} {} {})
  => 2

  (let [task (assoc-in +task+ [:item :list] (constantly [1 2 3]))
        f    (wrap-execute process-test-fn task)
        res  ((wrap-input f task) :all {} {} {})]
    (get res 2) => 3
    (get res 3) => 5
    (get res 4) => 7))

^{:refer std.task.process/task-inputs :added "4.0"}
(fact "constructs inputs to the task given a set of parameters"
  ^:hidden
  
  (task-inputs (task/task :default "ns-interns"
                          {:construct {:env (fn [_] {})
                                       :lookup (fn [_ _] '{std.task [a b c]})}
                           :main {:fn ns-interns}})
               'std.task)
  => '[std.task {} {std.task [a b c]} {}])

^{:refer std.task.process/invoke :added "4.0"}
(fact "executes the task, given functions and parameters"
  ^:hidden
  
  (keys (invoke (task/task :default "ns-interns"
                           {:construct {:env (fn [_] {})
                                        :lookup (fn [_ _] '{std.task []})}
                            :main {:fn (fn [ns _ _ _]
                                         (ns-interns ns))}})
                'std.task.process-test))
  => (contains '[process-test-fn] :in-any-order :gaps-ok))
