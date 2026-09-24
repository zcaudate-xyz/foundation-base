(ns pune.trade.form-order
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
             [js.lib.datetime :as dt]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-button :as ui-button]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-spinner :as ui-spinner]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-section :as ui-section]
             [melbourne.slim :as slim]
             [iberia.table.common-display :as common-display]
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
   :styleDecimalText {:fontSize 30
                      :fontWeight 600}
   :styleDigit {:height 36
                :width 18}
   :styleDigitText {:fontSize 30
                    :fontWeight 600
                    :width 20
                    :height 36
                    :textAlign "center"}})

(def.js styleMediumDigit
  {:styleDecimal {:height 24
                  :width 10}
   :styleDecimalText {:fontSize 24
                      :fontWeight 600}
   :styleDigit {:height 30
                :width 15}
   :styleDigitText {:fontSize 24
                    :fontWeight 600
                    :width 20
                    :height 30}})

;;
;;
;;

(defn.js TradeRateForm
  [props]
  (var #{[design
          mini
          control
          (:= label "PRICE")]} props)
  (var [init setInit] (r/local))
  (r/init []
    (jc/future-delayed [500] (setInit true)))
  (var #{decimal
         trade
         prediction
         allotment
         setRate
         rate}  control)
  (var fgColor "neutral")
  (var fg {:key fgColor})
  (var controlVariant (:? (== trade "buy")
                          (ui-text/minorButtonTheme
                           fg {:key "background"})
                          (ui-text/minorButtonTheme
                           {:key "background"} fg)))
  (var pickerVariant  (ui-text/minorButtonTheme
                       {:key "background"
                        :tone "sharpen"}
                       {:key "neutral"}))
  (return
   [:% n/View
    [:% n/Row
     {:style {:flexDirection "row-reverse"}}
     [:% ui-text/H6
      {:design design
       :variant #{fg}}
      label]]
    (:? (and allotment decimal)
        [:% n/Row
         {:key (+ allotment "." decimal)
          :style {:alignItems "center"
                  :flexDirection "row-reverse"}}
         [:% n/View
          {:style {:width 120
                   :flexDirection "row-reverse"}}
          (:? init
            [:% ui-spinner/Spinner
             #{[:key trade
                :design design
                :variant pickerVariant
                :step 1
                :min 1
                :max (- allotment 1)
                :decimal decimal
                :value rate
                :setValue setRate
                #_#_:style {:marginLeft 10}
                :panDirection (:? mini
                                  "horizontal"
                                  "vertical")
                :panStride  15
                (:.. -/styleLargeDigit)]}])]
         (:? (not mini)
             [:% ui-spinner/SpinnerControls
              {:key trade
               :design design
               :min 1
               :variant controlVariant
               :value rate
               :setValue setRate
               :max (- allotment 1)
               :decimal decimal
               :leftProps
               {:style {:borderRadius 0 :textAlign "center"}
                :text "-"}
               
               :rightProps
               {:style {:borderRadius 0
                        :textAlign "center"}
                :text "+"}
               :step 1}]
             [:% ui-text/Icon
              {:design design}])])]))

(defn.js TradeAmountForm
  [#{[design
      mini
      control
      (:= max 99999)
      (:= label "AMOUNT")]}]
  (var [init setInit] (r/local))
  (r/init []
    (jc/future-delayed [500] (setInit true)))
  (var #{setAmount
         amount
         trade
         prediction} control)
  
  (var fgColor "neutral" #_(:? (== prediction "no")
                               "neutral"
                               "primary"))
  (var fg {:key fgColor})
  (var controlVariant (:? (== trade "buy")
                          (ui-text/minorButtonTheme
                           fg {:key "background"})
                          (ui-text/minorButtonTheme
                           {:key "background"} fg)))
  (var pickerVariant  (ui-text/minorButtonTheme
                       {:key "background"
                        :tone "sharpen"}
                       {:key "neutral"}))
  (return
   [:% n/View
    [:% n/Row
     {:style {:flexDirection "row-reverse"}}
     [:% ui-text/H6
      {:design design
       :variant #{fg}}
      label]]
    [:% n/Row
     {:style {:alignItems "center"
              :flexDirection "row-reverse"}}
     [:% n/View
      {:style {:width 120
               :flexDirection "row-reverse"}}
      (:? init
          [:% ui-spinner/Spinner
           #{[:key trade
              :design design
              :variant pickerVariant
              :step 1
              :min 1
              :max max
              :decimal 0
              :value amount
              :setValue setAmount
              :panDirection (:? mini
                                "horizontal"
                                "vertical")
              #_#_:style {:marginLeft 10}
              (:.. -/styleLargeDigit)]}])]
     (:? (not mini)
         [:% ui-spinner/SpinnerControls
          {:key trade
           :min 1
           :variant controlVariant
           :value amount
           :setValue setAmount
           :max max
           :decimal 0
           :leftProps
           {:style {:borderRadius 0
                    :textAlign "center"}
            :text "-"}
           :design design
           :rightProps
           {:style {:borderRadius 0
                    :textAlign "center"}
            :text "+"}
           :step 1}])]]))

;;
;; Odds
;;

(defn.js getOdds
  [allotment rate]
  (when (and (lib/is-number? allotment)
             (lib/is-number? rate))
    (var lcm (math/lcm (- allotment rate) rate))
    (var under (/ lcm (- allotment rate)))
    (var over  (/ lcm rate))
    (return
     (+ over "/" under)))
  (return " - "))

(defn.js getMoneyLine
  [allotment rate]
  (when (and (lib/is-number? allotment)
             (lib/is-number? rate))
    (var lcm (math/lcm (- allotment rate) rate))
    (var under (/ lcm (- allotment rate)))
    (var over  (/ lcm rate))
    (return
     (Math.round
      (:? (< over under)
          (- (* 100 (/ under over)))
          (* 100 (/ over under))))))
  (return " - "))

(comment
  (-/getMoneyLine 100 20)
  (-/getMoneyLine 100 80)
  (-/getOdds 100 20))

;;
;; Summary
;;

(defn.js TradeLiveSummary
  [props]
  (var #{design
         control} props)
  (var #{trade
         prediction
         rate
         allotment
         decimal
         fraction
         amount
         rate-curr
         rate-lo-ask
         rate-hi-bid} control)
  (when (== prediction "no")
    (:= rate-curr (- allotment rate-curr))
    (:= rate-lo-ask (- allotment (. control rate-hi-bid)))
    (:= rate-hi-bid (- allotment (. control rate-lo-ask))))

  (var isClearing (:? (== trade "buy")
                      (>= rate rate-lo-ask)
                      (<= rate rate-hi-bid)))

  (var isAtMarket (:? (== trade "buy")
                      (== rate rate-lo-ask)
                      (== rate rate-hi-bid)))

  (var offerText (:? (== trade "buy")
                     (:? (lib/nil? rate-lo-ask)
                         "No Offers"
                         (+ "@ "
                            (j/toFixed (* fraction rate-lo-ask)
                                       decimal)
                            " (" (j/toFixed (* fraction (- rate rate-lo-ask))
                                            decimal)
                            ")"))
                     (:? (lib/nil? rate-hi-bid)
                         "No Offers"
                         (+ "@ "
                            (j/toFixed (* fraction rate-hi-bid)
                                       decimal)
                            " (" (j/toFixed (* fraction (- rate rate-hi-bid))
                                            decimal)
                            ")"))))
  
  (var clearText (:? isClearing
                     (:? isAtMarket
                         "Market Price"
                         (:? (== trade "buy")
                             "Above Market"
                             "Below Market"))
                     (:? (== trade "buy")
                         "Below Market"
                         "Above Market")))

  (var estimateText (:? isClearing
                        "Immediate"
                        "Queue"))
  
  (return
   (slim/entry
    props
    {:type "v"
     :body [{:type "title"
             :variant {:fg {:key "background"
                            :tone "sharpen"}
                       :bg {:key "background"
                            :tone "sharpen"}}
             :template "ESTIMATE"}
            {:type "v"
             :variant {:bg {:key "background"}}
             :style {:paddingHorizontal 10
                     :paddingVertical 5}
             :body [{:type "bold"
                     :template clearText}
                    {:type "v" :style {:height 5}}
                    {:style {:fontFamily "monospace"}
                     :template offerText}
                    {:type "v" :style {:height 5}}
                    {:type "title"
                     #_#_:style {:textAlign "right"}
                     :template estimateText}]}]})))

(defn.js get-rake-fee
  [entry trade spend amount fraction]
  (var rake (data/get-in entry ["book" 0 "rake" 0]))
  (var #{buy-type buy-value
         sell-type sell-value} (or rake {}))
  (var fee 0)
  (when (== trade "buy")
    (cond (== buy-type "per_trade")
          (:= fee (* buy-value fraction))

          (== buy-type "per_contract")
          (:= fee (* buy-value amount fraction))

          (== buy-type "percentage")
          (:= fee (/ (* buy-value spend) 100)))
    (return fee))
  
  (when (== trade "sell")
    (cond (== sell-type "per_trade")
          (:= fee (* sell-value fraction))

          (== sell-type "per_contract")
          (:= fee (* sell-value amount fraction))

          (== sell-type "percentage")
          (:= fee (/ (* sell-value spend) 100)))
    (return fee)))

(defn.js TradeTotalSummary
  [#{design
     entry
     control}]
  (var #{currencyId
         prediction
         trade
         allotment
         fraction
         decimal
         rate
         amount} control)
  (var fgColor (:? (== prediction "no")
                   "error"
                   "primary"))
  (var fg {:key fgColor
           :tone "flatten"})
  (var spend (* amount
                rate
                fraction))
  
  (var fee (-/get-rake-fee entry trade spend amount fraction))
  (return
   (slim/entry
    #{design}
    {:type "v"
     :style {:flex 1}
     :body
     [{:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template (:? (== trade "buy")
                             "Spend"
                             "Gain")}
              {:style {:fontFamily "monospace"}
               :template (+ (j/toFixed
                             spend
                             decimal)
                            " "
                            currencyId)}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Fee"}
              {:style {:fontFamily "monospace"}
               :template (:? (== fee 0)
                             " - "
                             (+ (:? (== trade "sell")
                                    "-"
                                    "")
                                (j/toFixed
                                 fee
                                 decimal)
                                " "
                                currencyId))}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Total"}
              {:style {:fontFamily "monospace"}
               :template (+ (j/toFixed
                             (:? (== trade "buy")
                                 (+ spend fee)
                                 (- spend fee))
                             decimal)
                            " "
                            currencyId)}]}]})))

(defn.js TradeSellSummary
  [#{design
     contracts
     control}]
  (var contractLu (data/arr-juxt (or contracts [])
                              (data/key-fn "prediction")
                              lib/identity))
  
  (var #{currencyId
         prediction
         allotment
         fraction
         decimal
         rate
         amount} control)
  (var fgColor (:? (== prediction "no")
                   "error"
                   "primary"))
  (var fg {:key fgColor
           :tone "flatten"})
  (var #{balance spend} (or (data/get-in contractLu [prediction])
                            {}))
  (var avgCost (:? (and balance spend)
                   (/ spend balance)))
  (return
   (slim/entry
    #{design}
    {:type "v"
     :style {:flex 1}
     :body
     [{:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Avg Cost"}
              {:style {:fontFamily "monospace"}
               :template (:? avgCost
                             (j/toFixed
                              avgCost
                              decimal)
                             " - ")}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "P/L"}
              {:style {:fontFamily "monospace"}
               :template (:? avgCost
                             (j/toFixed
                              (* amount (- avgCost (* rate fraction)))
                              decimal)
                             " - ")}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "New Spend"}
              {:style {:fontFamily "monospace"}
               :template (:? spend
                             (j/toFixed
                              (- spend (* amount rate fraction))
                              2)
                             " - ")}]}]})))

(defn.js TradeOddsSummary
  [#{design
     control}]
  (var #{currencyId
         prediction
         allotment
         fraction
         decimal
         rate
         amount} control)
  (var fgColor (:? (== prediction "no")
                   "error"
                   "primary"))
  (var fg {:key fgColor
           :tone "flatten"})
  (return
   (slim/entry
    #{design}
    {:type "v"
     :style {:flex 1}
     :body
     [{:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Payout"}
              {:style {:fontFamily "monospace"}
               :template (+ (j/toFixed
                             (* amount
                                allotment
                                fraction)
                             decimal)
                            " "
                            currencyId)}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Yield"}
              {:style {:fontFamily "monospace"}
               :template (+ (j/toFixed
                             (* 100 (/ (- allotment rate)
                                       rate))
                             0)
                            "%")}]}
      {:type "h"
       :style {:justifyContent "space-between"}
       :body [{:type "bold"
               :template "Odds"}
              {:style {:fontFamily "monospace"}
               :template
               (+ (-/getOdds allotment rate)
                  " "
                  "(" (-/getMoneyLine allotment rate) ")")}]}]})))

;;
;; Asset
;;

(defn.js TradeFundsSummary
  [#{design
     control
     asset}]
  (var #{prediction
         currencyId} control)
  (var fgColor "neutral" #_(:? (== prediction "no")
                               "neutral" 
                               "primary"))
  (var staticProps #{design
                     {:variant {:fg {:key fgColor
                                     :tone "flatten"}}}})
  (return
   (slim/entry
    staticProps
    {:type "v"
     :style {:minWidth 120
             :flex 1
             :marginBottom 5}
     :body [{:type "h"
             :style {:height 20
                     :alignItems "center"}
             :body [{:type "bold"
                     :template "FUNDS"}
                    {:type "fill"}
                    {:type "bold"
                     :template (+  "(" currencyId ")" )}]}
            {:type "separator"
             :variant {:fg {:key "neutral"}}
             :style {:marginHorizontal -5}}
            {:type "h"
             :style {:alignItems "center"
                     :paddingVertical 2}
             :body [{:type "bold"
                     :template "B"}
                    {:type "fill"}
                    {:style {:fontFamily "monospace"
                             }
                     :template (data/get-in asset ["balance"])}]}
            {:type "h"
             :style {:alignItems "center"}
             :body [{:type "bold"
                     :template "E"}
                    {:type "fill"}
                    {:style {:fontFamily "monospace"}
                     :template (data/get-in asset ["escrow"])}]}]})))

;;
;; Contract
;;

(defn.js TradeContractsSummary
  [#{[design
      control
      entry
      (:= orders [])
      contracts]}]
  (var #{prediction
         setPrediction
         setBuyRate
         setSellRate
         trade
         home-name
         away-name} control)
  (var fgColor "neutral")
  (var staticProps #{design
                     {:variant {:fg {:key fgColor
                                     :tone "flatten"}}}})
  (var contractLu (data/arr-juxt (or contracts [])
                              (data/key-fn "prediction")
                              lib/identity))
  (var yesContracts (or (data/get-in contractLu ["yes" "balance"])
                        0))
  (var yesEscrow (or (data/get-in contractLu ["yes" "escrow"])
                     0))
  (var yesOrderEscrow (data/arr-foldl orders
                                     (fn:> [acc order]
                                       (:? (and (== (. order ["prediction"]) "yes")
                                                (== (. order ["trade"]) "buy"))
                                           (+ acc (. order ["contract_received"]))
                                           
                                           (and (== (. order ["prediction"]) "yes")
                                                (== (. order ["trade"]) "sell"))
                                           (+ acc (. order ["contract_sent"]))
                                           
                                           :else
                                           acc))
                                     0))
  (var noContracts (or (data/get-in contractLu ["no" "balance"])
                       0))
  (var noEscrow    (or (data/get-in contractLu ["no" "escrow"])
                       0))
  (var noOrderEscrow (data/arr-foldl orders
                                  (fn:> [acc order]
                                    (:? (and (== (. order ["prediction"]) "no")
                                             (== (. order ["trade"]) "buy"))
                                        (+ acc (. order ["contract_received"]))
                                        
                                        (and (== (. order ["prediction"]) "no")
                                             (== (. order ["trade"]) "sell"))
                                        (+ acc (. order ["contract_sent"]))
                                        
                                        :else
                                        acc))
                                  0))
  (r/watch [prediction]
    (:? (== trade "buy")
        (setBuyRate)
        (setSellRate)))
  (return
   (slim/entry
    staticProps
    {:type "v"
     :style {:minWidth 120
             :flex 1
             :marginBottom 5}
     :body [{:type "h"
             :style {:height 20
                     :alignItems "center"}
             :body [{:type "bold"
                     :template "CONTRACTS"}]}
            {:type "separator"
             :variant {:fg {:key "neutral"}}
             :style {:marginHorizontal -5}}
            {:type "control_layout"
             :submit (fn []
                       (setPrediction "yes"))
             :body [{:type "h"
                     :variant (:? (== prediction "yes")
                                  {:bg {:key "primary"
                                        :mix "background"
                                        :ratio 7}})
                     :style {:alignItems "center"
                             :borderRadius 3
                             :paddingVertical 2
                             :marginHorizontal -4
                             :paddingHorizontal 4}
                     :body [{:type "bold"
                             :template (or home-name "Y")}
                            {:type "fill"}
                            {:template (j/toString yesContracts)
                             :style {:width 55
                                     :textAlign "right"
                                     :fontFamily "monospace"}}
                            {:template (+ "(" (+ yesEscrow yesOrderEscrow) ")")
                             :style {:width 55
                                     :textAlign "right"
                                     :fontFamily "monospace"}}]}]}
            {:type "control_layout"
             :submit (fn []
                       (setPrediction "no"))
             :body [{:type "h"
                     :variant (:? (== prediction "no")
                                  {:bg {:key "error"
                                        :mix "background"
                                        :ratio 7}})
                             :style {:alignItems "center"
                                     :borderRadius 3
                                     :paddingVertical 2
                                     :marginHorizontal -4
                                     :paddingHorizontal 4}
                     :body [{:type "bold"
                             :template (or away-name "N")}
                            {:type "fill"}
                            {:template (j/toString noContracts)
                             :style {:width 55
                                     :textAlign "right"
                                     :fontFamily "monospace"}}
                            {:template (+ "(" (+  noEscrow noOrderEscrow) ")")
                             :style {:width 55
                                     :textAlign "right"
                                     :fontFamily "monospace"}}]}]}]})))

;;
;; Order
;;

(defn.js TradeOrdersSummary
  [#{design
     control
     buy
     sell}]
  (var #{prediction} control)
  (var fgColor (:? (== prediction "no")
                   "error"
                   "primary"))
  (var staticProps #{design
                     {:variant {:fg {:key fgColor
                                     :tone "flatten"}}}})
  (return
   [:% n/View
    {:style {:flex 1
             :minWidth 100}}
    [:% n/View
     {:style {:margin 6}}
     (:? (data/not-empty? sell)
         [:<>
          (r/% ui-section/SectionSeparator staticProps)
          [:% n/Row
           [:% n/Fill]
           (r/% ui-text/Bold staticProps "SELLING")]
          (r/% ui-section/SectionSeparator staticProps)
          (j/map sell (fn:> [[rate e]]
                        [:% n/Row {:key rate :style {:marginHorizontal 5}}
                         [:% ui-text/P #{design} (. e price)]
                         [:% n/Fill]
                         [:% ui-text/P #{design} (. e unfilled)]]))])
     (:? (data/not-empty? buy)
         [:<> (r/% ui-section/SectionSeparator staticProps)
          [:% n/Row [:% n/Fill] (r/% ui-text/Bold staticProps "BUYING")]
          (r/% ui-section/SectionSeparator staticProps)
          (j/map buy (fn:> [[rate e]]
                       [:% n/Row {:key rate :style {:marginHorizontal 5}}
                        [:% ui-text/P #{design}
                         (. e price)]
                        [:% n/Fill]
                        [:% ui-text/P #{design} (. e unfilled)]]))])]]))


(defn.js TradeOrderDetail
  [props]
  (var #{design
         actions
         orders
         control
         onCancel} props)
  (var #{prediction
         fraction
         decimal
         trade
         currentOrder
         setCurrentOrder} control)
  
  (when (not currentOrder)
    (return (slim/entry {:type "v"})))
  (var filtered (j/filter orders (fn:> [o] (== prediction (. o ["prediction"])))))
  (var order    (j/find filtered (fn:> [o] (== currentOrder (. o  id)))))
  (when (not order)
    (return (slim/entry {:type "v"})))

  (var fg {:key (:? (== prediction "yes")
                    "primary"
                    "error")})
  (return
   (slim/entry
    props
    {:type "v"
     :style {:minWidth 120
             :flex 1
             :marginTop 3
             :marginBottom 5}
     :body [{:type "h"
             :body [{:type "bold"
                     :template "SELECTED ORDER"}]}
            {:type "separator"
             :variant {:fg {:key "neutral"}}
             :style {:marginVertical 3
                     :marginHorizontal -5}}
            {:type "v"
             :style {:padding 5}
             :body [{:type "h"
                     :style {:alignItems "center"}
                     :body [(common-display/log-entry-order-tags order)
                            {:type "fill"}
                            {:type "h"
                             :style {:flexDirection "row-reverse"}
                             :body [{:type "action"
                                     :key prediction
                                     :confirm "tooltip"
                                     :confirmText "CANCEL"
                                     :variant (ui-text/minorButtonTheme
                                               {:key "background"
                                                :tone "sharpen"}
                                               fg)
                                     :tooltip
                                     {:overlay
                                      {:variant
                                       (ui-text/accentButtonTheme
                                        fg
                                        {:key "neutral"})}
                                      :arrow
                                      {:variant
                                       {:fg {:key "neutral"}
                                        :bg fg}}}
                                     :icon {:name "block"}
                                     :style {:paddingVertical 2}
                                     :submit (fn []
                                               (when onCancel
                                                 (onCancel order)))}]}]}
                    {:type "bold"
                     :style {:fontSize 15
                             :fontFamily "monospace"
                             :marginVertical 5}
                     :template
                     (+ (. order contract-sent)
                        (. order contract-received)
                        " of " (. order amount)
                        " @ " (j/toFixed (* fraction (. order rate))
                                         decimal))}
                    {:template (. order ["time_created"])
                     :style {:fontSize 9}
                     :format (fn:> [t]
                               (+  (j/toLocaleDateString (new Date (/ t 1000)))
                                   " "
                                   (j/toLocaleTimeString (new Date (/ t 1000)))
                                   "\n"
                                   (dt/agoVerbose t)))}]}
            
            
            
            #_{:type "h"
             :style {:alignItems "center"}
             :body [
                    (common-display/log-entry-order-amount order)
                    {:type "fill"}
                    {:variant {:bg {:key (:? (==  "active" (. order status))
                                             "primary"
                                             "neutral")}
                               :fg {:key "background"}} 
                     :style {:paddingHorizontal 5
                             :width 80
                             :textAlign "center"}
                     :template
                     (+ 
                      (+ (. order contract-sent)
                         (. order contract-received))
                      " of " (. order amount))}]}]})))

(comment
  
  (def.js DISPLAY_PROSPECT_ORDER
    {:props
     (fn [order]
       (return
        (common-display/log-container
         {:type "v"
          :body [(common-display/log-entry-header-container
                  "time_updated"
                  (common-display/log-entry-order-tags order)
                  {:style {:width 85
                           :textAlign "center"}})
                 {:type "h"
                  :style {:alignItems "center"}
                  :body [(common-display/log-entry-order-amount order)
                         {:type "fill"}
                         {:variant {:bg {:key (:? (==  "active" (. order status))
                                                  "primary"
                                                  "neutral")}
                                    :fg {:key "background"}} 
                          :style {:paddingHorizontal 5
                                  :width 80
                                  :textAlign "center"}
                          :template
                          (+ 
                           (+ (. order contract-sent)
                              (. order contract-received))
                           " of " (. order amount))}]}
                 {:style {:fontFamily "monospace"
                          :fontSize 9}
                  :template (. order id)}
                 {:style {:fontFamily "monospace"
                          :fontSize 9}
                  :template (data/get-in order ["account" 0 "nickname"])}]})))}))

(defn.js TradeOrdersView
  [props]
  (var #{design
         actions
         orders
         control} props)
  (var #{prediction
         fraction
         decimal
         allotment
         trade
         currentOrder
         setCurrentOrder
         rate-lo-ask
         rate-hi-bid} control)
  (when (data/is-empty? orders)
    (return (slim/entry
             props
             {:type "h"
              :style {:height 100
                      :justifyContent "center"
                      :alignItems "center"}
              :body [{:type "bold"
                      :template "No Active Orders"}]})))
  
  
  (var fgColor "neutral")
  (var staticProps #{design
                     {:variant {:fg {:key fgColor
                                     :tone "flatten"}}}})
  (var filtered
       (-> orders
           (data/arr-filter (fn:> [o] (== prediction (. o ["prediction"]))))
           (data/arr-group-by (data/key-fn "trade") lib/identity)
           (data/obj-pairs)
           (data/arr-map (fn:> [[trade arr]]
                        (:? (== trade "buy")
                            [trade (sort-by/sort-by arr [(fn:> [o] (- (. o rate))) "time_created"])]
                            [trade (sort-by/sort-by arr [(fn:> [o] (- (. o rate)))
                                                   "time_created"])])))
           (data/obj-from-pairs)))
  (var orderFn
       (fn [o]
         (var price (j/toFixed (* (. o rate) fraction)
                               (. o decimal)))
         (var unfilled (- (. o amount)
                          (. o contract_received)
                          (. o contract_sent)))
         (var filled   (+ (. o contract_received)
                          (. o contract_sent)))
         (return
          (slim/entry
           props
           {:type "control_layout"
            :key (. o id)
            :submit (fn []
                      (setCurrentOrder (. o id)))
            :body [{:type "h"
                    :variant (:? (== currentOrder (. o id))
                                 {:bg {:key (:? (== prediction "yes")
                                                "primary"
                                                "error")
                                       :mix "background"
                                       :ratio 7}})
                    :style {:alignItems "center"
                            :borderRadius 3
                            :paddingVertical 2
                            :marginHorizontal -4
                            :paddingHorizontal 4}
                    :body [{:type "bold"
                            :style {:width 44}
                            :template price}
                           {:template (+ " (" (:? (== prediction "yes")
                                                  (:? (== (. o trade) "buy")
                                                      (j/toFixed (* (- (. o rate) rate-lo-ask)
                                                                    fraction)
                                                                 decimal)
                                                      (+ "+" (j/toFixed (* (- (. o rate) rate-hi-bid)
                                                                           fraction)
                                                                        decimal)))
                                                  (:? (== (. o trade) "buy")
                                                      (j/toFixed (* (- rate-hi-bid (- allotment (. o rate)))
                                                                    fraction)
                                                                 decimal)
                                                      (+ "+" (j/toFixed (* (-  rate-lo-ask (- allotment (. o rate)))
                                                                           fraction)
                                                                        decimal))))
                                         ")")
                            :style {:fontFamily "monospace"}}
                           {:type "fill"}
                           {:template (+ filled " / " (. o amount))
                            :style {:width 55
                                    :textAlign "right"
                                    :fontFamily "monospace"}}]}]}))))
  (return
   (slim/entry
    props
    {:type "v"
     :body [(:? (data/not-empty?
                 (. filtered ["sell"]))
                
                {:type "v"
                 :style {:marginBottom 15}
                 :body [{:type "h"
                         :body [{:type "fill"}
                                {:type "bold"
                                 :template "SELL ORDERS"}]}
                        {:type "separator"
                         :variant {:fg {:key "neutral"}}
                         :style {:marginHorizontal -5
                                 :marginVertical 3}}
                        (:.. (j/map (. filtered ["sell"]) orderFn))]})
            (:? (data/not-empty?
                 (. filtered ["buy"]))
                {:type "v"
                 :body [{:type "h"
                         :body [{:type "fill"}
                                {:type "bold"
                                 :template "BUY ORDERS"}]}
                        {:type "separator"
                         :variant {:fg {:key "neutral"}}
                         :style {:marginHorizontal -5
                                 :marginVertical 3}}
                        (:.. (j/map (. filtered ["buy"])
                                    orderFn))]})]})))



;;
;; Submit
;;


(defn.js TradeSubmitButton
  [#{[design
      control
      data
      (:.. rprops)]}]
  (var #{trade
         fraction
         rate
         amount
         prediction
         balanceAsset
         balanceContracts
         home-name
         away-name
         waiting} control)
  (var bg (:? (== prediction "no")
              {:key "error"}
              {:key "primary"}))
  (var fg {:key "background"
           :tone "sharpen"})
  (var variant (:? (== trade "sell")
                   (ui-text/minorButtonTheme
                    fg bg)
                   (ui-text/accentButtonTheme
                    bg fg)))
  (var Component (:? (== trade "sell")
                     ui-text/ButtonMinor
                     ui-text/ButtonAccent))
  (var buttonDisabled
       (:? (== trade "sell")
           (< (- (or (data/get-in balanceContracts [prediction "balance"])
                     0)
                 amount)
              0)
           (< (- balanceAsset
                 (* amount rate fraction))
              0)))
  (return
   [:% Component
    #{[:design design
       :variant variant
       :style {:borderRadius 0
               :textAlign "center"
               :fontSize 24
               :fontFamily "impact"}
       :disabled buttonDisabled
       :transformations {:bg nil}
       :text (:? waiting
                 [:% ui-text/ActivityIndicator
                  #{design}]
                 (j/toUpperCase
                  (+ trade
                     " "
                     (:? (== prediction "yes")
                         (or home-name prediction)
                         (or away-name prediction)))))
       (:.. rprops)]}]))

(defn.js TradeShowToggle
  [#{[design
      trade
      setTrade
      showTrade
      setShowTrade
      (:= textBuy  "BUY")
      (:= textSell "SELL")]}]
  (return
   [:% n/Row
    [:% ui-toggle-button/ToggleButton
     #{design
       {#_#_:variant (ui-text/accentToggleTheme)
        :text textBuy
        :transformations {:bg nil}
        :selected (and (== "buy" trade)
                       showTrade)
        :onPress (fn []
                   (if (== "buy" trade)
                     (setShowTrade (not showTrade))
                     (do (setTrade "buy")
                         (setShowTrade true))))
        :style {:borderRadius 0
                :textAlign "center"}
        :styleContainer {:flex 1}}}]
    [:% n/Padding {:style {:width 6}}]
    [:% ui-toggle-button/ToggleButton
     #{design
       {#_#_:variant (ui-text/accentToggleTheme)
        :text textSell
        :transformations {:bg nil}
        :selected (and (== "sell" trade)
                       showTrade)
        :onPress (fn []
                   (if (== "sell" trade)
                     (setShowTrade (not showTrade))
                     (do (setTrade "sell")
                         (setShowTrade true))))
        :style {:borderRadius 0
                :textAlign "center"}
        :styleContainer {:flex 1}}}]]))

(def.js MODULE (!:module))

