(ns std.pipe-test
  (:use code.test)
  (:require [std.pipe :refer :all]
            [std.pipe.util :as ut]
            [std.lib.result :as res]
            [std.task :as task]
            [std.lib :as h]
            [std.lib.signal :as signal]))

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

^{:refer std.pipe/wrap-input :added "3.0"}
(fact "enables execution of task with single or multiple inputs"
  ((wrap-input process-test-fn +task+)
   1 {} {} {})
  => 2

  (let [task (-> +task+
                 (assoc-in [:item :list] (constantly [1 2 3]))
                 (assoc-in [:main :argcount] 4))
        f    (ut/wrap-execute process-test-fn task)
        res  ((wrap-input f task) :all {} {} {})]
    ;; Note: wrap-execute modifies inputs via :pre (inc), so keys are shifted
    (get res 2) => 3
    (get res 3) => 5
    (get res 4) => 7))

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
                          :main {:fn (fn [ns _ _ & _]
                                       (ns-interns ns))
                                 :argcount 4}}) ;; Explicitly set argcount 4
              'std.pipe-test))
  => (contains '[process-test-fn] :in-any-order :gaps-ok)

  (pipe (task/task :default "bulk-test"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _ & _] (* x 10))
                           :argcount 4} ;; Explicitly set argcount 4
                    :result {:keys [[:val identity]]}})
        :list
        {:bulk true})
  => (contains {1 10
                2 20
                3 30
                4 40})

  (pipe (task/task :default "bulk-test-parallel"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _ & _] (* x 10))
                           :argcount 4}}) ;; Explicitly set argcount 4
        :list
        {:bulk true :parallel true})
  => (contains {1 10 2 20 3 30 4 40})

  (pipe (task/task :default "bulk-test-fifo"
                   {:item {:list (constantly [1 2 3 4])}
                    :main {:fn (fn [x _ _ & _] (* x 10))
                           :argcount 4}}) ;; Explicitly set argcount 4
        :list
        {:bulk true :mode :fifo})
  => (contains {1 10 2 20 3 30 4 40})

  (pipe [(task/task :default "chain-1"
                    {:main {:fn (fn [x _ _ & _] (+ x 1))
                            :argcount 4}}) ;; Explicitly set argcount 4
         (task/task :default "chain-2"
                    {:main {:fn (fn [x _ _ & _] (* x 2))
                            :argcount 4}})] ;; Explicitly set argcount 4
        1)
  => 4)

^{:refer std.pipe/pipe-signal :added "4.0"}
(fact "executes with signal reporting"
  (let [signals (atom [])]
    (signal/signal:with-temp
      [{:type :task} (fn [data] (swap! signals conj data))]
      (pipe (task/task :default "bulk-signal-test"
                       {:item {:list (constantly [1])}
                        :main {:fn (fn [x _ _ & _] (* x 10))
                               :argcount 4}}) ;; Explicitly set argcount 4
            :list
            {:bulk true}))
    (count @signals) => 2
    (-> @signals first :status) => :start
    (-> @signals second :status) => :return
    (-> @signals first :context) => nil))

^{:refer std.pipe/pipe-context :added "4.0"}
(fact "executes with context passing"
  (let [signals (atom [])]
    (signal/signal:with-temp
      [{:type :task} (fn [data] (swap! signals conj data))]
      (pipe (task/task :default "bulk-context-test"
                       {:item {:list (constantly [1])}
                        :main {:fn (fn [x _ _ & _] (* x 10))
                               :argcount 4}}) ;; Explicitly set argcount 4
            :list
            {:bulk true :context {:foo :bar}}))
    (-> @signals first :context :foo) => :bar
    (-> @signals second :context :foo) => :bar))
