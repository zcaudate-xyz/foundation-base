(ns pune.harness.ui-ladder-test
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
             [pune.harness.ui-ladder :as ui-ladder]]
   :export [MODULE]})

^{:refer pune.harness.ui-ladder/MarketLadderText :added "0.1"}
(fact "text view for market ladder")

^{:refer pune.harness.ui-ladder/MarketLadderRow :added "0.1"}
(fact "displays the market ladder row"
  ^:hidden
  
  (defn.js MarketLadderRowDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-ladder/MarketLadderRow"} 
[:% ui-ladder/MarketLadderRow
       {:design {:type "dark"}
        :rate 60
        :amount 10
        :fraction 0.01
        :decimal 2}]))))

^{:refer pune.harness.ui-ladder/MarketLadder :added "0.1"}
(fact "displays the market ladder"
  ^:hidden
  
  (defn.js MarketLadderDemo
    []
    (var market
         {:meta {},
          :frame 7, :spread [8 500 508],
          :ask {:volume [[508 4] [509 10]],
                :total 2,
                :range [508 509]},
          :bid {:volume [[500 2]],
                :total 1,
                :range [500 500]}})
    (var book {:decimal 2
               :frac 0.01
               :max 10
               :allotment 1000})
    (return
     (n/EnclosedCode 
{:label "pune.harness.ui-ladder/MarketLadder"} 
[:% ui-ladder/MarketLadder
       #{market
         {:allotment 1000
          :decimal 2
          :prediction "yes"}}])))
  
  (def.js MODULE (!:module)))
