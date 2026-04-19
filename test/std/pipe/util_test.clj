(ns std.pipe.util-test
  (:require [std.pipe.util :refer :all])
  (:use code.test))

^{:refer std.pipe.util/main-function :added "4.1"}
(fact "creates a main function to be used for execution"
  (let [[single args-single?] (main-function (fn [input]
                                               (inc input))
                                             1)
        [paired args-paired?] (main-function (fn [input params]
                                               (+ input (:offset params)))
                                             2)]
    [(single 1 {} {} {})
     args-single?
     (paired 1 {:offset 3} {} {})
     args-paired?])
  => [2 false 4 false])

^{:refer std.pipe.util/select-filter :added "4.1"}
(fact "matches given a range of filters"
  (select-filter #"pipe" 'std.pipe)
  => true

  (select-filter :std :std.pipe)
  => true

  (boolean (select-filter #{42} 42))
  => true

  (select-filter '(std #"pipe") 'std.pipe)
  => true

  (boolean (select-filter [#"missing" :std] :std.pipe))
  => true

  (select-filter {:bad true} :id)
  => (throws-info {:selector {:bad true}}))

^{:refer std.pipe.util/select-inputs :added "4.1"}
(fact "selects inputs based on matches"
  (let [task {:item {:list (fn [_ _]
                             [:std.pipe :std.task :other])}}]
    [(vec (select-inputs task {} {} :all))
     (vec (select-inputs task {} {} :std))
     (vec (select-inputs task {} {} [:missing #"task$"]))])
  => [[:std.pipe :std.task :other]
      [:std.pipe :std.task]
      [:std.task]])

^{:refer std.pipe.util/wrap-execute :added "4.1"}
(fact "enables execution of task with transformations"
  (let [task {:item {:pre inc
                     :post dec
                     :output str}}
        wrapped (wrap-execute (fn [input params lookup env & args]
                                (+ input
                                   (:delta params 0)
                                   (reduce + 0 args)))
                              task)
        [key result] (wrapped 1 {:bulk true :delta 2} {} {} 3)]
    [(wrapped 1 {:delta 2} {} {} 3)
     key
     (:status result)
     (:data result)])
  => ["6" 2 :return 6])

^{:refer std.pipe.util/task-inputs :added "4.1"}
(fact "constructs inputs to the task given a set of parameters"
  (let [task {:name "sample"
              :construct {:input (fn [_]
                                   :default-input)
                          :env (fn [{:keys [name extra]}]
                                 {:env [name extra]})
                          :lookup (fn [task merged]
                                     {:lookup [(:name task)
                                               (:env merged)]})}}]
    (let [defaulted   (task-inputs task)
          from-params (task-inputs task {:extra 5})
          manual      (task-inputs task :manual {:flag true} {:lookup :manual} {:env :override})]
      [[(first defaulted)
        (-> defaulted second :name)
        (nth defaulted 2)
        (nth defaulted 3)]
       from-params
       manual]))
  => [[:default-input
       "sample"
       {:lookup ["sample" ["sample" nil]]}
       {:env ["sample" nil]}]
      [:default-input
       {:extra 5}
       {:lookup ["sample" ["sample" 5]]}
       {:env ["sample" 5]}]
      [:manual
       {:flag true}
       {:lookup :manual}
       {:env :override}]])
