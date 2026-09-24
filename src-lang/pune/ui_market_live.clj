(ns pune.ui-market-live
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as ui-base]
             [melbourne.ui-text :as ui-text]
             [melbourne.slim :as slim]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-section :as ui-section]
             [pune.common.data-market :as base-market]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js live-priority-rate
  "gets the priority rate"
  {:added "0.1"}
  [market
   allotment 
   prediction]
  (var rate-fn
       (fn [pair]
         (var [pos vol] pair)
         (return [(base-market/position-to-rate prediction allotment pos)
                  vol])))
  (var #{ask bid} (Object.assign {:ask []
                             :bid []}
                            market))
  (var [buy sell] (:? (== prediction "yes")
                      [bid ask]
                      [ask bid]))
  (return {:buy  (data/arr-reverse (data/arr-map sell rate-fn))
           :sell (data/arr-reverse (data/arr-map buy rate-fn))}))

(def.js ORDER_IMPL
  {:type "v"
   :body [{:type "h"
           :body [{:type "p"
                   :variant {:fg {:key "neutral"}}
                   :template ["id"]}]}]})

(defn.js MarketLiveOrder
  [#{design
     orderId
     orderFn
     orderLookup
     amount}]
  (var [changed setChanged] (r/local false))
  (var [prev  setPrev]      (r/local amount))
  (var changing (a/useBinaryIndicator changed))
  (r/watch [amount]
    (when (not= prev amount)
      (setChanged true)
      (setTimeout (fn []
                      (setChanged false))
                    600)))
  (return
   [:% ui-base/Box
    {:indicators #{changing}
     :transformations
     {:changing (fn [v]
                  (return
                   {:style {:opacity (- 1 (* 0.7 v))}}))}}
    [:% ui-text/ButtonAccent
     {:design design
      :text  (+ "" amount)
      :onPress  (fn []
                  (when orderFn
                    (orderFn orderId
                             orderLookup)))
      :style {:paddingVertical 0
              :paddingHorizontal 0
              :marginHorizontal 2
              :marginVertical 2
              :borderWidth 0
              :width 40
              :textAlign "center"}}]]))

(defn.js MarketLiveRow
  "market live row"
  {:added "0.1"}
  [#{design
     control
     priority
     lookup
     rate
     orderFn}]
  (var #{fraction
         prediction
         decimal} control)
  (return
   [:% n/Row
    {:style {:alignItems "center"
             :marginHorizontal 5
             :marginVertical 5}}
    [:% ui-static/Text
     {:design design
      :variant {:font "h6"}
      :style {:width 50}}
     (j/toFixed (* rate fraction) decimal)]
    [:% n/View
     {:style {:flex 1}}
     [:% n/Row
      {:style {:flexWrap "wrap"}}
      (j/map priority
             (fn [[order-id amount]]
               (return
                [:% -/MarketLiveOrder
                 {:design design
                  :amount amount
                  :orderId order-id
                  :orderFn orderFn
                  :orderLookup lookup
                  :key order-id}])))]]]))

(defn.js MarketLive
  "market live"
  {:added "0.1"}
  [#{[design
      market
      control
      orderFn
      published]}]
  (var #{[(:= allotment 100)
          (:= decimal 2)
          (:= trade "buy")
          (:= prediction "yes")
          rate
          setRate]} control)
  (var lookup   (data/arr-juxt published data/id-fn lib/identity))
  (var fraction (Math.pow 10 (- decimal)))
  (var priorities    (-/live-priority-rate market
                                           allotment
                                           prediction))
  (var lineFn
       (fn [[rate priority] i]
         (return
          [:% -/MarketLiveRow
           #{{:key rate}
             design
             control
             rate
             priority
             lookup
             orderFn}])))
  (return
   [:% n/View
    {:style {:padding 3
             :flex 1
             :overflow "auto"}}
    [:% n/View
     {:style {:minHeight 60
              :flexDirection "column-reverse"}}
     (j/map (j/reverse [(:.. (. priorities buy))])
            lineFn)]
    [:% ui-section/SectionSeparator
     {:design design
      :variant {:fg {:key "neutral"}}
      :style {:marginVertical 3}}]
    [:% n/View
     {:style {:minHeight 60
              :flexDirection "column"}}
     (j/map (. priorities sell)
            lineFn)]]))

(def.js MODULE (!:module))
