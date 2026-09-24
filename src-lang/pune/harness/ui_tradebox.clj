(ns pune.harness.ui-tradebox
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit   {:native {:suppress true}
                     :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [melbourne.ui-spinner :as ui-spinner]
             [melbourne.slim-submit :as slim-submit]
             [melbourne.ui-text :as ui-text]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(def.js styleLargeDigit
  {:styleDecimal {:height 36
                  :width 10}
   :styleDecimalText {:fontSize 30}
   :styleDigit {:height 36
                :width 19}
   :styleDigitText {:fontSize 30
                    :width 22
                    :height 36}})

(def.js styleMediumDigit
  {:styleDecimal {:height 24
                  :width 10}
   :styleDecimalText {:fontSize 24}
   :styleDigit {:height 30
                :width 15}
   :styleDigitText {:fontSize 24
                    :width 20
                    :height 30}})
#_
(defn.js MarketTradeRate
  [#{design
     decimal
     allotment
     setRate
     rate}]
  (return
   [:% n/Row
    {:style  {:padding 5}}
    [:% n/View
     {:style {:flex 1}}
     [:% n/Row
      [:% n/View
       {:style {:justifyContent "center"}}
       [:% ui-text/H6 #{design} "PRICE"]]
      [:% n/Fill]
      [:% ui-spinner/SpinnerValues
       #{[:design design
          :step 1
          :min 1
          :max (- allotment 1)
          :decimal decimal
          :value rate
          :setValue setRate
          (:.. -/styleLargeDigit)]}]]
     [:% n/Row
      
      #_[:% n/Fill]
      [:% ui-spinner/SpinnerControls
       {:design design
        :step 1
        :min 1
        :max (- allotment 1)
        :decimal decimal
        :value rate
        :setValue setRate
        :leftProps {:text "-"
                   :style {:padding 0
                           :borderRadius 0
                           :textAlign "center"}}
       :rightProps {:text "+"
                    :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}}]]]]))

(defn.js MarketTradeRate
  [#{design
     decimal
     allotment
     setRate
     rate}]
  (return
   [:% n/View
    {:style {:padding 10}}
    [:% n/Row
     [:% ui-text/H5
      #{design
        {:variant {:fg {:key "neutral"}}}}
      "PRICE"]
     [:% n/Fill]
     [:% ui-spinner/SpinnerControls
      {:design design
       :step 1
       :min 1
       :max (- allotment 1)
       :decimal decimal
       :value rate
       :setValue setRate
       :leftProps {:text "-"
                   :style {:padding 0
                           :borderRadius 0
                           :textAlign "center"}}
       :rightProps {:text "+"
                    :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}}]]
    [:% n/Row
     {:style {:marginTop  5
              :alignItems "center"}}
     [:% ui-text/ButtonMinor
      {:design design
       :text "Edit"
       :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}]
     [:% n/Fill]
     [:% ui-spinner/SpinnerValues
      #{[:design design
         :variant {:fg {:key "neutral"}}
         :step 1
         :min 1
         :max (- allotment 1)
         :decimal decimal
         :value rate
         :setValue setRate
         (:.. -/styleMediumDigit)]}]]]))

(defn.js MarketTradeAmount
  [#{design
     setAmount
     amount}]
  (return
   [:% n/View
    {:style {:padding 10}}
    [:% n/Row
     [:% ui-text/H5
      #{design
        {:variant {:fg {:key "neutral"}}}}
      "AMOUNT"]
     [:% n/Fill]
     [:% ui-spinner/SpinnerControls
      {:design design
       :step 1
       :min 1
       :max 999
       :decimal 0
       :value amount
       :setValue setAmount
       :leftProps {:text "-"
                   :style {:padding 0
                           :borderRadius 0
                           :textAlign "center"}}
       :rightProps {:text "+"
                    :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}}]]
    
    [:% n/Row
     {:style {:marginTop  5
              :alignItems "center"}}
     [:% ui-text/ButtonMinor
      {:design design
       #_#_:variant (ui-text/minorButtonTheme)
       :text "Edit"
       :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}]
     [:% n/Fill]
     [:% ui-spinner/SpinnerValues
      #{[:design design
         :variant {:fg {:key "neutral"}}
         :step 1
         :min 1
         :max 99
         :decimal 0
         :value amount
         :setValue setAmount
         (:.. -/styleLargeDigit)]}]]]))

#_
(defn.js MarketTradeAmount
  [#{design
     setAmount
     amount}]
  (return
   [:% n/Row
    {:style  {:alignItems "center"
              :justifyContent "center"}}
    [:% ui-spinner/SpinnerValues
     #{[:design design
        :step 1
        :min 1
        :max 99
        :decimal 0
        :value amount
        :setValue setAmount
        (:.. -/styleLargeDigit)]}]
    [:% n/View
     [:% ui-spinner/SpinnerControls
      {:design design
       :step 1
       :min 1
       :max 999
       :decimal 0
       :value amount
       :setValue setAmount
       :leftProps {:text "-1"
                   :style {:padding 0
                           :borderRadius 0
                           :textAlign "center"}}
       :rightProps {:text "+1"
                    :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}}]]
    [:% n/Text " "]
    [:% n/View
     [:% ui-spinner/SpinnerControls
      {:design design
       :step 5
       :min 1
       :max 999
       :decimal 0
       :value amount
       :setValue setAmount
       :leftProps {:text "-5"
                   :style {:padding 0
                           :borderRadius 0
                           :textAlign "center"}}
       :rightProps {:text "+5"
                    :style {:padding 0
                            :borderRadius 0
                            :textAlign "center"}}}]]
    ]))

(defn.js MarketTradeLine
  [#{[design
      decimal
      allotment
      rate
      setRate
      amount
      setAmount
      trade
      prediction
      onSubmit
      onSuccess
      onError
      onResult
      submitProps
      (:= showSlider true)]}]
  (var __submitProps (Object.assign (r/useSubmitResult
                                #{onSubmit
                                  onSuccess
                                  onError
                                  onResult})
                               submitProps))
  (return
   [:% n/Row
    [:% n/Row
     {:style {:marginHorizontal 10
              :marginVertical 5}}
     [:% slim-submit/SubmitButton
      #{[:design design
         :variant {:pressed {:fg {:key "primary"
                                  :tone "sharpen"}
                             :bg {:key "neutral"}}}
         :text (j/toUpperCase (+ trade " " prediction))
         :style {:width 110}
         :onPress (. __submitProps onActionPress)
         (:.. __submitProps)]}]]
    [:% n/Row
     {:style {:marginHorizontal 10
              :marginVertical 5}}
     [:% -/MarketTradeRate
      #{design
        decimal
        allotment
        setRate
        rate}]]
    [:% n/Row
     {:style {:marginHorizontal 10
              :marginVertical 5}}
     [:% -/MarketTradeAmount
      #{design
        setAmount
        amount}]]]))

(defn.js MarketTradeBox
  [#{[design
      
      market
      decimal
      allotment

      rate
      setRate
      prediction
      setPrediction
      
      setTrade
      trade
      setAmount
      amount
      onSubmit
      onSuccess
      onError]}]
  (var ratio (Math.pow 10 (- decimal)))
  (var min (* 1 ratio))
  (var max (* (- allotment 1) ratio))
  (return
   [:% n/View
    {:key (+ decimal "." allotment)}
    [:% n/Row
     [:% n/View
      {:style {:marginHorizontal 10}}]
     [:% ui-text/TabsAccent
      {:design design
       :data ["buy" "sell"]
       :value trade
       :setValue (fn [s]
                   (trace/LOG! s)
                   (setTrade s))}]
     [:% ui-text/TabsAccent
      {:design design
       :data ["yes" "no"]
       :value prediction
       :setValue setPrediction}]]]))

(def.js MODULE (!:module))
