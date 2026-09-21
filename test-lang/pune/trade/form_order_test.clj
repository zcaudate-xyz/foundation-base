(ns pune.trade.form-order-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-page
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn]]
             [js.react :as r]
             [melbourne.ui-section :as ui-section]
             [pune.trade.form-order :as form-order]]
   :export [MODULE]})

^{:refer pune.trade.form-order/TradeOddsDisplay :added "0.1"}
(fact "displays the odds"
  ^:hidden

  (defn.js TradeOddsDisplayDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeOddsDisplay"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeOddsDisplay
        #{{:design {:type "dark"}
           :allotment 100
           :rate 40}}]]))))

^{:refer pune.trade.form-order/TradeSummaryDisplay :added "0.1"}
(fact "displays the summary"
  ^:hidden

  (defn.js TradeSummaryDisplayDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeSummaryDisplay"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeSummaryDisplay
        #{{:design {:type "dark"}
           :allotment 100
           :decimal 2
           :fraction 0.01
           :amount 50
           :rate 40}}]]))))

^{:refer pune.trade.form-order/TradeSidebarView :added "0.1"}
(fact "creates the trade sidebar")

^{:refer pune.trade.form-order/TradeRateForm :added "0.1"}
(fact "creates the trade rate form"
  ^:hidden

  (defn.js TradeRateFormDemo
    []
    (var [rate setRate]     (r/local 43))
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeRateForm"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% n/Row
        [:% form-order/TradeRateForm
         #{{:design {:type "dark"}
            :allotment 100
            :decimal 2
            :currency "TT"}
           rate
           setRate}]]] 
[:% n/TextDisplay
       #{rate}]))))

^{:refer pune.trade.form-order/TradeAmountForm :added "0.1"}
(fact "creates the trade amount form"
  ^:hidden

  (defn.js TradeAmountFormDemo
    []
    (var [amount setAmount]     (r/local 12))
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeAmountForm"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% n/Row
        [:% form-order/TradeAmountForm
         #{{:design {:type "dark"}
            :allotment 100
            :decimal 2
            :currency "TT"}
           amount
           setAmount}]]] 
[:% n/TextDisplay
       #{amount}]))))

^{:refer pune.trade.form-order/getOdds :added "0.1"}
(fact "helper function for odds display")

^{:refer pune.trade.form-order/getMoneyLine :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeLiveSummary :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/get-rake-fee :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeTotalSummary :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeSellSummary :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeOddsSummary :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeFundsSummary :added "0.1"}
(fact "displays the trade funds"
  ^:hidden
  
  (defn.js TradeFundsSummaryDemo
    []
    (var asset {:id "02feb5b8-3c80-4169-bbfa-6027e1cd67d9"
                :balance 998445.54                
                :currency_id "STC"
                :escrow 761.97
                :time_created 1652078841585270
                :time_updated 1652102853855907})
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeFundsSummary"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeFundsSummary
        {:design {:type "dark"}
         :asset asset}]]))))

^{:refer pune.trade.form-order/TradeContractsSummary :added "0.1"}
(fact "displays the trade contracts"
  ^:hidden
  
  (defn.js TradeContractsSummaryDemo
    []
    (var contracts
       [{:balance 38
         :escrow 0
         :id "266df18d-da79-4e56-a041-4ceecd673790"
         :prediction "yes"
         :spend 169.48
         :status "active"
         :time_created 1652080876981401
         :time_updated 1652081864406028}
        {:balance 69
         :escrow 0
         :id "96c477f9-e55c-4dc1-ad60-2114ed3c5069"
         :prediction "no"
         :spend 382.68
         :status "active"
         :time_created 1652079241742355
         :time_updated 1652101086767947}])
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeContractsSummary"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeContractsSummary
        {:design {:type "dark"}
         :contracts contracts
         :prediction "yes"}]]))))

^{:refer pune.trade.form-order/TradeOrdersSummary :added "0.1"}
(fact "displays the trade orders"
  ^:hidden
  
  (defn.js TradeOrdersSummaryDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeOrdersSummary"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeOrdersSummary
        {:design {:type "dark"}
         :buy  [[455 {:price "4.55"
                      :unfilled 10}]
                [454 {:price "4.54"
                      :unfilled 3}]]
         :sell [[460 {:price "4.60"
                      :unfilled 10}]
                [459 {:price "4.59"
                      :unfilled 3}]]}]]))))

^{:refer pune.trade.form-order/TradeOrderDetail :added "0.1"}
(fact "TODO")

^{:refer pune.trade.form-order/TradeOrdersView :added "0.1"}
(fact "displays the trade orders"
  ^:hidden
  
  (defn.js TradeOrdersViewDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeOrdersView"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeOrdersView
        {:design {:type "dark"}
         :buy  [[455 {:price "4.55"
                      :unfilled 10}]
                [454 {:price "4.54"
                      :unfilled 3}]]
         :sell [[460 {:price "4.60"
                      :unfilled 10}]
                [459 {:price "4.59"
                      :unfilled 3}]]}]]))))

^{:refer pune.trade.form-order/TradeSubmitButton :added "0.1"}
(fact "creates the trade submit button"
  ^:hidden
  
  (defn.js TradeSubmitButtonDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeSubmitButton"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeSubmitButton
        {:design {:type "dark"}
         :trade "sell"
         :predction "yes"}]]))))

^{:refer pune.trade.form-order/TradeShowToggle :added "0.1"}
(fact "trade show toggle"
  ^:hidden
  
  (defn.js TradeShowToggleDemo
    []
    (var [trade setTrade] (r/local "sell"))
    (var [showTrade
          setShowTrade] (r/local false))
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeShowToggle"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% form-order/TradeShowToggle
        #{trade setTrade
          showTrade setShowTrade
          {:design {:type "dark"}}}]])))

  (def.js MODULE (!:module)))

(comment
  ^{:refer pune.trade.form-order/TradeRateSlider :added "0.1"}
  (fact "creates the trade rate slider"
    ^:hidden

    (defn.js TradeRateSliderDemo
      []
      (var [rate setRate]     (r/local 43))
      (return
       (n/EnclosedCode 
{:label "pune.trade.form-order/TradeRateSlider"} 
[:% ui-section/SectionBase
         {:design {:type "dark"}}
         [:% n/Row
          [:% form-order/TradeRateSlider
           #{{:design {:type "dark"}
              :allotment 100
              :decimal 2}
             rate
             setRate}]]] 
[:% n/TextDisplay
         #{rate}]))))
  ^{:refer pune.trade.form-order/TradeAmountSlider :added "0.1"}
  (fact "creates the trade amount slider"
    ^:hidden

    (defn.js TradeAmountSliderDemo
      []
      (var [amount setAmount]     (r/local 12))
      (return
       (n/EnclosedCode 
{:label "pune.trade.form-order/TradeAmountSlider"} 
[:% ui-section/SectionBase
         {:design {:type "dark"}}
         [:% n/Row
          [:% form-order/TradeAmountSlider
           #{{:design {:type "dark"}
              :allotment 100
              :decimal 2}
             amount
             setAmount}]]] 
[:% n/TextDisplay
         #{amount}])))))

(comment

  ^{:refer pune.trade.form-order/TradeSidebarView :added "0.1"}
  (fact "creates the trade sidebar view"
    ^:hidden
    
    (defn.js TradeSidebarViewDemo
    []
    (var tradeProps (form-order/useTradeState
                     {:allotment 100}))
    (return
     (n/EnclosedCode 
{:label "pune.trade.form-order/TradeSidebarView"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}
        :style {:width 160}}
       [:% form-order/TradeSidebarView
        #{{:design {:type "dark"}
           :allotment 100
           :decimal 2
           :fraction 0.01
           :tradeProps tradeProps}}]])))))