(ns std.pipe.display-test
  (:require [std.lib.result :as res]
            [std.pipe.display :refer :all])
  (:use code.test))

(def +task+
  {:item {:output inc}
   :result {:keys [[:label #(str "item-" %)]
                   [:value identity]]
            :columns [{:key :label}
                      {:key :value}]
            :output dec
            :ignore #(= % 99)}
   :summary {:aggregate {:total [#(:value %) + 0]}
             :finalise (fn [summary _ results]
                         (assoc summary :completed (count results)))}})

^{:refer std.pipe.display/bulk-display :added "4.1"}
(fact "constructs bulk display options"
  (bulk-display 5 8)
  => {:padding 1
      :spacing 1
      :columns [{:id :index :length 5
                 :color #{:blue}
                 :align :right}
                {:id :input :length 8}
                {:id :data :length 60 :color #{:white}}
                {:id :time :length 10 :color #{:bold}}]})

^{:refer std.pipe.display/prepare-columns :added "4.1"}
(fact "prepares columns for printing"
  (prepare-columns [{:key :label}
                    {:key :value}]
                   [{:label "Chris"
                     :value "1"}
                    {:label "Bob"
                     :value "100"}])
  => [{:key :label :id :label :length 7}
      {:key :value :id :value :length 5}])

^{:refer std.pipe.display/bulk-warnings :added "4.1"}
(fact "outputs warnings that have been processed"
  (bulk-warnings {:print {}}
                 [[1 {:status :warn}]
                  [2 {:status :return}]
                  [3 {:status :warn}]])
  => [[1 {:status :warn}]
      [3 {:status :warn}]])

^{:refer std.pipe.display/bulk-errors :added "4.1"}
(fact "outputs errors that have been processed"
  (bulk-errors {:print {}}
               [[1 {:status :error}]
                [2 {:status :critical}]
                [3 {:status :return}]])
  => [[1 {:status :error}]
      [2 {:status :critical}]])

^{:refer std.pipe.display/bulk-results :added "4.1"}
(fact "outputs results that have been processed"
  (->> [[2 (res/result {:status :return :data 20})]
        [1 (res/result {:status :warn :data 10})]
        [3 (res/result {:status :return :data 5})]
        [4 (res/result {:status :return :data 99})]]
       (bulk-results +task+ {:print {} :order-by :value})
       (mapv #(select-keys % [:key :label :value])))
  => [{:key 3 :label "item-5" :value 5}
      {:key 2 :label "item-20" :value 20}])

^{:refer std.pipe.display/bulk-summary :added "4.1"}
(fact "outputs summary of processed results"
  (bulk-summary +task+
                {:print {}}
                [[1 {:time 10 :status :return :data 1}]
                 [2 {:time 20 :status :warn :data 2}]
                 [3 {:time 30 :status :error :data 3}]]
                [{:value 5}
                 {:value 20}]
                [[2 {:status :warn}]]
                [[3 {:status :error}]]
                100)
  => {:errors 1
      :warnings 1
      :items 3
      :results 2
      :total 25
      :completed 2
      :cumulative 60
      :elapsed 100})

^{:refer std.pipe.display/bulk-package :added "4.1"}
(fact "packages results for return"
  (bulk-package +task+
                {:items [[1 {:data 1}]
                         [2 {:data 2}]]
                 :results [{:key 1 :data 10}
                           {:key 2 :data 20}]
                 :warnings [:warn]
                 :errors [:error]
                 :summary {:done true}}
                :all
                :map)
  => {:items {1 2
              2 3}
      :results {1 9
                2 19}
      :warnings [:warn]
      :errors [:error]
      :summary {:done true}})
