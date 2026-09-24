(ns pune.harness.ui-ladder
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]
             [js.react :as r]
             [js.react-native :as n]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-section :as ui-section]
             [pune.common.data-market :as base-market]]
   :export [MODULE]})

(defn.js MarketLadderText
  [#{[market
      allotment
      decimal
      prediction]}]
  (var frac (Math.pow 10 (- decimal)))
  (var offers   (base-market/live-offers-rate market
                                              allotment
                                              prediction 6))
  (return
   [:% ui-static/ScrollView
    [:% n/Row
     [:% n/TextDisplay
      #{market}]
     [:% n/TextDisplay
      #{offers}]]]))

(defn.js MarketLadderRow
  [#{design
     rate
     amount
     fraction
     prediction
     decimal}]
  (var [prevAmount setPrevAmount] (r/local amount))
  (var isMounted (r/useIsMounted))
  (r/watch [amount]
    (j/delayed [1000]
      (when (isMounted)
        (setPrevAmount amount))))
  (return
   [:% n/Row
    {:style {:marginHorizontal 5}}
    [:% ui-static/Text
     {:design design
      :variant (:? (not= amount prevAmount)
                   {:font "h6"
                    :fg {:key "background"}
                    :bg {:key (:? (== "no" prediction)
                                  "error"
                                  "primary")}}
                   {:font "h6"})}
     (j/toFixed (* rate fraction) decimal)]
    [:% n/Fill]
    [:% ui-static/Text
     {:design design}
     amount]]))

(defn.js MarketLadder
  [#{[design
      market
      (:= allotment 1000)
      (:= decimal 0)
      trade
      prediction
      rate
      setRate]}]
  (var fraction (Math.pow 10 (- decimal)))
  (var offers   (base-market/live-offers-rate market
                                              allotment
                                              prediction 15))
  (var segment   (base-market/segment-price rate offers))
  (var lineFn
       (fn [[rate amount] i]
         (return
          [:% -/MarketLadderRow
           #{{:key rate}
             design
             rate
             prediction
             amount
             fraction
             decimal}])))
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
            lineFn)]
    [:% ui-section/SectionSeparator
     {:design design
      :variant {:fg {:key "neutral"}}
      :style {:marginVertical 3}}]
    [:% n/View
     {:style {:minHeight 60
              :flex 1
              :flexDirection "column"
              :overflow "hidden"}}
     (j/map (. offers sell)
            lineFn)]]))


(def.js MODULE (!:module))
