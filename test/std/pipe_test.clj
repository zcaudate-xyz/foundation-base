(ns std.pipe-test
  (:use code.test)
  (:require [std.pipe :refer :all]
            [std.pipe.util :as ut]
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

^{:refer std.pipe.util/main-function :added "4.0"}
(fact "creates a main function to be used for execution"
  ^:hidden

  (let [[main args?] (ut/main-function ns-aliases 1)]
    (fn? main) => true
    args? => false))

^{:refer std.pipe.util/select-filter :added "4.0"}
(fact "matches given a range of filters"
  ^:hidden

  (ut/select-filter #"ello" 'hello)
  => true
  (ut/select-filter #"^ello" 'hello)
  => false)

^{:refer std.pipe.util/select-inputs :added "4.0"}
(fact "selects inputs based on matches"
  ^:hidden

  (ut/select-inputs {:item {:list (fn [_ _] ['code.test 'spirit.common])}}
                 {}
                 {}
                 ['code])
  => ['code.test])

^{:refer std.pipe.util/wrap-execute :added "3.0"}
(fact "enables execution of task with transformations"

  ((ut/wrap-execute process-test-fn +task+)
   1 {} {} {})
  => 3)

;; ^{:refer std.pipe/wrap-input :added "3.0"}
;; (fact "enables execution of task with single or multiple inputs"
;;   ((wrap-input process-test-fn +task+)
;;    1 {} {} {})
;;   => 2

;;   (let [task (assoc-in +task+ [:item :list] (constantly [1 2 3]))
;;         f    (wrap-execute process-test-fn task)
;;         res  ((wrap-input f task) :all {} {} {})]
;;     (get res 2) => 3
;;     (get res 3) => 5
;;     (get res 4) => 7))

^{:refer std.pipe.util/task-inputs :added "4.0"}
(fact "constructs inputs to the task given a set of parameters"
  ^:hidden

  (ut/task-inputs (task/task :default "ns-interns"
                          {:construct {:env (fn [_] {})
                                       :lookup (fn [_ _] '{std.task [a b c]})}
                           :main {:fn ns-interns}})
               'std.task)
  => '[std.task {} {std.task [a b c]} {}])

^{:refer std.pipe/pipe :added "4.0"}
(fact "executes the task, given functions and parameters"
  ^:hidden

  (keys (pipe (task/task :default "ns-interns"
                         {:construct {:env (fn [_] {})
                                      :lookup (fn [_ _] '{std.task []})}
                          :main {:fn (fn [ns _ _]
                                       (ns-interns ns))}})
              'std.pipe-test))
  => (contains '[process-test-fn] :in-any-order :gaps-ok)

  (pipe (task/task :default "bulk-test"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _] (* x 10))}
                    :result {:keys [[:val identity]]}})
        :list
        {:bulk true})
  => (contains {1 10
                2 20
                3 30
                4 40})

  (pipe (task/task :default "bulk-test-parallel"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _] (* x 10))}})
        :list
        {:bulk true :parallel true})
  => (contains {1 10 2 20 3 30 4 40})

  (pipe (task/task :default "bulk-test-fifo"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _] (* x 10))}})
        :list
        {:bulk true :mode :fifo})
  => (contains {1 10 2 20 3 30 4 40})

  (pipe [(task/task :default "chain-1"
                    {:main {:fn (fn [x _ _] (+ x 1))}})
         (task/task :default "chain-2"
                    {:main {:fn (fn [x _ _] (* x 2))}})]
        1)
  => 4)
