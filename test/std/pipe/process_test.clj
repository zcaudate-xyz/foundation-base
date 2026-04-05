(ns std.pipe.process-test
  (:require [std.lib.result :as res]
            [std.pipe :as pipe]
            [std.pipe.process :refer :all])
  (:use code.test))

(defn- process-test-fn
  [input params lookup env & args]
  (+ input
     (:delta params 0)
     (:bonus env 0)
     (reduce + 0 args)))

^{:refer std.pipe.process/wrap-bulk :added "4.1"}
(fact "wraps the function to handle bulk execution"
  (let [task {:item {:display identity}
              :result {:keys [[:value identity]]
                       :columns [{:key :value}]}
              :summary {:aggregate {:sum [#(:value %) + 0]}}}
        f    (fn [input params lookup env & args]
               [input (res/result {:status :return :data (* 2 input)})])
        wrapped (wrap-bulk f task)
        [single-key single-result] (wrapped 2 {:print {}} {} {})
        bulk-result (wrapped [1 2 3]
                             {:bulk true
                              :print {}
                              :return :results}
                             {} {})]
    [(and (= single-key 2)
          (= (:status single-result) :return)
          (= (:data single-result) 4))
     bulk-result])
  => [true {1 2
            2 4
            3 6}])

^{:refer std.pipe.process/wrap-main :added "4.1"}
(fact "wraps the main function for the task"
  (let [task {:item {:pre inc
                     :post dec
                     :output str
                     :display identity}
              :main {:fn process-test-fn
                     :argcount 4}
              :result {:keys [[:value identity]]
                       :columns [{:key :value}]}
              :summary {:aggregate {:sum [#(:value %) + 0]}}}
        wrapped (wrap-main task)]
    [(wrapped 1 {:delta 2} {} {:bonus 1} 3)
     (wrapped [1 2]
              {:bulk true
               :print {}
               :return :results
               :delta 1}
              {}
              {:bonus 0})])
  => ["7" {2 2
           3 3}])

^{:refer std.pipe.process/invoke :added "4.1"}
(fact "executes the task"
  (let [bulk-task (pipe/task :default "double"
                             {:item {:list (fn [_ _] [1 2 3])}
                              :main {:fn (fn [input _ _ _]
                                           (* input 2))
                                     :argcount 4}
                              :result {:keys [[:value identity]]
                                       :columns [{:key :value}]}})
        args-task (pipe/task :default "needs-extra"
                             {:main {:fn (fn [input _ _ _ extra]
                                           (+ input extra))
                                     :argcount 4}})]
    (invoke bulk-task :list {:bulk true :print {} :return :results})
    => {1 2
        2 4
        3 6}

    (invoke args-task 1)
    => (throws-info {:input '(1)})

    (invoke args-task 1 :args 5)
    => 6))

^{:refer std.pipe.process/resolve-input :added "4.1"}
(fact "resolves inputs for bulk execution"
  (let [task {:item {:list (fn [_ _]
                             [:alpha :beta :gamma])}}]
    [(resolve-input task :list {} {} {})
     (resolve-input task :a {} {} {})
     (resolve-input task 42 {:bulk true} {} {})])
  => [[[:alpha :beta :gamma] {:bulk true}]
      [[:alpha] {:bulk true}]
      [42 {:bulk true}]])
