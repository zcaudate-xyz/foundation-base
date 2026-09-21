(ns pune.ui-market-ladder-swap
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [melbourne.slim :as slim]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-section :as ui-section]
             [pune.common.data-market :as base-market]
             [pune.common.data-swap :as base-swap]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js MarketLadderRow
  "market ladder row"
  {:added "0.1"}
  [props]
  (var #{design
         control
         amount
         amountMax
         position
         side
         onPressRow} props)
  (var #{[(:= fraction 1)
          allotment
          (:= decimal 0)]} control)
  (var [prevAmount setPrevAmount] (r/local amount))
  (var isMounted (r/useIsMounted))
  (r/watch [amount]
    (j/delayed [1000]
      (when (isMounted)
        (setPrevAmount amount))))
  (var price (base-swap/position-to-fstr position))  
  (return
   (slim/entry
    props
    {:type "control_layout"
     :submit (fn []
               (when onPressRow
                 (onPressRow
                  #{position
                    side})))
     :body [{:type "h"
             :style {:alignItems "center"
                     :height 13}
             :body [{:type "bold"
                     :style {:fontSize 8
                             :padding 1
                             :opacity 0.7}
                     :variant (:? (not= amount prevAmount)
                                  {:font "h6"
                                   :fg {:key "background"}
                                   :bg {:key "primary"}}
                                  {:font "h6"
                                   :bg {:key "background"}})
                     
                     :template price}
                    {:type "fill"}
                    {:type "title"
                     :style    {:fontSize 7
                                :borderRadius 2
                                :textAlign "right"
                                :minWidth (+ 30 (* 30 (/ amount
                                                         amountMax)))
                                :padding 2
                                :paddingHorizontal 5}
                     :variant {:bg  {:key "primary"
                                     #_#_:mix "background"
                                     #_#_:ratio 4}
                               :fg  {:key "background"}}
                     :template (or (string/to-fixed (* 0.001 amount)
                                               1)
                                   "")}]}]})))

(defn.js MarketLadder
  "market ladder row"
  {:added "0.1"}
  [props]
  (var #{[design
          market
          control
          (:= steps 15)
          onPressRow]} props)
  (var #{[position
          setPosition]} control)
  (var offers   (base-market/live-offers-rate market
                                              nil
                                              "yes"
                                              steps))
  (var amountMax (Math.max (:.. (data/arr-map (. offers buy) data/second))
                        (:.. (data/arr-map (. offers sell) data/second))))
  (var lineFn
       (fn [side]
         (return
          (fn [[position amount] i]
            (return
             [:% -/MarketLadderRow
              #{{:key position}
                amount
                amountMax
                position
                design
                control
                side
                onPressRow}])))))
  (return
   [:% n/View
    {:style {:padding 3
             :flex 1}}
    [:% n/View
     {:style {:minHeight 60
              :flex 1
              :flexDirection "column-reverse"
              :overflow "hidden"}}
     (j/map (j/reverse [(:.. (. offers buy))])
            (lineFn "ask"))]
    [:% ui-section/SectionSeparator
     {:design design
      :variant {:fg {:key "background"
                     :mix "primary"
                     :ratio 2}}
      :style {:marginVertical 3}}]
    [:% n/View
     {:style {:minHeight 60
              :flex 1
              :flexDirection "column"
              :overflow "hidden"}}
     (j/map (. offers sell)
            (lineFn "bid"))]]))

(def.js MODULE (!:module))

(comment
  #_
  (defn.js MarketLadder
    "market ladder row"
    {:added "0.1"}
    [#{[design
        market
        position
        (:= allotment 100)
        (:= decimal 0)
        (:= trade "buy")
        (:= prediction "yes")]}]
    (return
     [:% n/TextDisplay
      #{design
        market
        position
        allotment
        decimal
        trade
        prediction}])))
