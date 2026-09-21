(ns pune.harness.ui-tradebox-test
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
             [pune.harness.ui-tradebox :as ui-tradebox]]
   :export [MODULE]})

^{:refer pune.harness.ui-tradebox/MarketTradeRate :added "0.1"}
(fact "gets the trade rate"
  ^:hidden
  
  (defn.js MarketTradeRateDemo
    []
    (var [rate setRate]     (r/local 43))
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-tradebox/MarketTradeRate"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% n/Row
        [:% ui-tradebox/MarketTradeRate
         #{{:design {:type "dark"}
            :allotment 100
            :decimal 2}
           rate
           setRate}]]] 
[:% n/TextDisplay
       #{rate}]))))

^{:refer pune.harness.ui-tradebox/MarketTradeAmount :added "0.1"}
(fact "gets the trade amount"
  ^:hidden
  
  (defn.js MarketTradeAmountDemo
    []
    (var [amount setAmount]     (r/local 43))
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-tradebox/MarketTradeAmount"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% n/Row
        [:% ui-tradebox/MarketTradeAmount
         #{{:design {:type "dark"}}
           amount
           setAmount}]]] 
[:% n/TextDisplay
       #{amount}]))))

^{:refer pune.harness.ui-tradebox/MarketTradeLine :added "0.1"}
(fact "shows the market trade line"
  ^:hidden
  
  (defn.js MarketTradeLineDemo
    []
    (var [amount setAmount] (r/local 5))
    (var [rate setRate]     (r/local 43))
    (var [prediction setPrediction] (r/local "yes"))
    (var [trade setTrade] (r/local "buy"))
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-tradebox/MarketTradeLine"} 
[:% ui-section/SectionBase
       {:design {:type "dark"}}
       [:% ui-tradebox/MarketTradeLine
        #{{:design {:type "dark"}
           :allotment 100
           :decimal 2}
          rate
          setRate
          amount
          setAmount
          prediction
          trade}]] 
[:% n/TextDisplay
       #{rate prediction trade}]))))

^{:refer pune.harness.ui-tradebox/MarketTradeBox :added "0.1"}
(fact "shows the market trade box"
  ^:hidden
  
  (defn.js MarketTradeBoxDemo
    []
    #_#_
    (var prospect
         {:background nil
          :currency_id "STC"
          :code_full "root/TTN001/defaultX0001"
          :content nil
          :description nil
          :detail "{\"decimal\":2,\"allotment\":1000}"
          :id "2abc3f9e-3b79-4993-ad33-65c9ee2be918"
          :name "defaultX0001"
          :picture nil
          :promotion nil
          :rank nil
          :result "progressing"
          :status "started"
          :style "cash"
          :time_created 1651479097484576
          :time_updated 1651479097484576
          :title ""})
    (var user
         {:id "9369ed97-dc18-412e-ab39-095bb7f485d6"
          :is_active true
          :is_official false
          :is_super false
          :is_suspended false
          :is_verified true
          :nickname "test00018"
          :password_updated 1651727833764370
          :time_created 1651727833764370
          :time_updated 1651727833764370})
    (var [rate setRate] (r/local 3))
    (var [amount setAmount] (r/local 3))
    (var [prediction setPrediction] (r/local "yes"))
    (var [trade setTrade] (r/local "buy"))
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-tradebox/MarketTradeBox"})))
  
  (def.js MODULE (!:module)))
