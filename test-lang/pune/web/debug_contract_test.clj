(ns pune.web.debug-contract-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn]]
             [pune.web.debug-contract :as debug-contract]]
   :export [MODULE]})

(def +traded-yes+
  [{"escrow" 0,
    "latestPrice" nil,
    "order"
    {"buy"
     [[35
       {"time_updated" 1648004152005630,
        "count" 2,
        "escrow" 7,
        "rate" 35,
        "prediction" "yes",
        "amount" 20,
        "price" "0.35",
        "trade" "buy",
        "unfilled" 20,
        "decimal" 2}]
      [36
       {"time_updated" 1648004152005630,
        "count" 1
        "escrow" 7,
        "rate" 36
        "prediction" "yes",
        "amount" 15,
        "price" "0.36",
        "trade" "buy",
        "unfilled" 15
        "decimal" 2}]]
     "sell"
     [[40
       {"time_updated" 1648004152005630,
        "count" 1,
        "escrow" 7,
        "rate" 40,
        "prediction" "yes",
        "amount" 13
        "price" "0.40",
        "trade" "sell",
        "unfilled" 13,
        "decimal" 2}]
      [41
       {"time_updated" 1648004152005630,
        "count" 1
        "escrow" 7,
        "rate" 41
        "prediction" "yes",
        "amount" 15,
        "price" "0.41",
        "trade" "sell",
        "unfilled" 15
        "decimal" 2}]]},
    "prediction" "yes",
    "contract"
    {"balance" 0,
     "book_id" "a4719675-519d-444c-b03b-8ad6da2fa1b1",
     "currency_id" "STC",
     "portfolio_id" "7c1d325f-e274-4b74-a191-3d9351e97910",
     "time_updated" 1648004151937191,
     "id" "65cd0f53-1448-40b4-a655-cc07bf3559e9",
     "escrow" 0,
     "asset_id" nil,
     "status" "active",
     "prediction" "yes",
     "time_created" 1648004151937191,
     "spend" 0},
    "code" "NBA/MVP-2022/S.CURRY"}])

(def +traded-no+
  [{"escrow" 0,
    "latestPrice" nil,
    "order"
    {"buy"
     [[35
       {"time_updated" 1648004152005630,
        "count" 2,
        "escrow" 7,
        "rate" 35,
        "prediction" "no",
        "amount" 20,
        "price" "0.35",
        "trade" "buy",
        "unfilled" 20,
        "decimal" 2}]
      [36
       {"time_updated" 1648004152005630,
        "count" 1
        "escrow" 7,
        "rate" 36
        "prediction" "no",
        "amount" 15,
        "price" "0.36",
        "trade" "buy",
        "unfilled" 15
        "decimal" 2}]]
     "sell"
     [[40
       {"time_updated" 1648004152005630,
        "count" 1,
        "escrow" 7,
        "rate" 40,
        "prediction" "no",
        "amount" 13
        "price" "0.40",
        "trade" "sell",
        "unfilled" 13,
        "decimal" 2}]
      [41
       {"time_updated" 1648004152005630,
        "count" 1
        "escrow" 7,
        "rate" 41
        "prediction" "no",
        "amount" 15,
        "price" "0.41",
        "trade" "sell",
        "unfilled" 15
        "decimal" 2}]]},
    "prediction" "no",
    "contract"
    {"balance" 0,
     "book_id" "a4719675-519d-444c-b03b-8ad6da2fa1b1",
     "currency_id" "STC",
     "portfolio_id" "7c1d325f-e274-4b74-a191-3d9351e97910",
     "time_updated" 1648004151937191,
     "id" "65cd0f53-1448-40b4-a655-cc07bf3559e9",
     "escrow" 0,
     "asset_id" nil,
     "status" "active",
     "prediction" "no",
     "time_created" 1648004151937191,
     "spend" 0},
    "code" "NBA/MVP-2022/S.CURRY"}])

^{:refer pune.web.debug-contract/ContractSingleHeader :added "0.1"}
(fact "constructs a single header"
  ^:hidden
  
  (defn.js ContractSingleHeaderDemo
    []
    (var props-yes (@! (dissoc (first +traded-yes+)
                               "order")))
    (var props-no  (@! (dissoc (first +traded-no+)
                               "order")))
    (return
     (n/EnclosedCode 
{:label "pune.web.debug-contract/ContractSingleHeader"} 
[:% n/Row
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#eee"
                 :padding 10}}
        [:% debug-contract/ContractSingleHeader
         #{[:design {:type "light"}
            (:.. props-yes)]}]
        [:% debug-contract/ContractSingleHeader
         #{[:design {:type "light"}
            (:.. props-no)]}]]
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#333"
                 :padding 10}}
        [:% debug-contract/ContractSingleHeader
         #{[:design {:type "dark"}
            (:.. props-yes)]}]
        [:% debug-contract/ContractSingleHeader
         #{[:design {:type "dark"}
            (:.. props-no)]}]]]))))

^{:refer pune.web.debug-contract/ContractSingleLine :added "0.1"}
(fact "constructs a single line"
  ^:hidden
  
  (defn.js ContractSingleLineDemo
    []
    (var line-yes {"time_updated" 1648004152005630,
                   "count" 1,
                   "escrow" 7,
                   "rate" 40,
                   "prediction" "no",
                   "amount" 13
                   "price" "0.40",
                   "trade" "sell",
                   "unfilled" 13,
                   "decimal" 2})
    (var line-no  {"time_updated" 1648004152005630,
                    "count" 1,
                    "escrow" 7,
                    "rate" 40,
                    "prediction" "yes",
                    "amount" 13
                    "price" "0.40",
                    "trade" "buy",
                    "unfilled" 13,
                    "decimal" 2})
    
    (return
     (n/EnclosedCode 
{:label "pune.web.debug-contract/ContractSingleLine"} 
[:% n/Row
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#eee"
                 :padding 10}}
        [:% debug-contract/ContractSingleLine
         #{[:design {:type "light"}
            :line line-yes]}]
        [:% debug-contract/ContractSingleLine
         #{[:design {:type "light"}
            :line line-no]}]]
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#333"
                 :padding 10}}
        [:% debug-contract/ContractSingleLine
         #{[:design {:type "dark"}
            :line line-yes]}]
        [:% debug-contract/ContractSingleLine
         #{[:design {:type "dark"}
            :line line-no]}]]]))))

^{:refer pune.web.debug-contract/ContractSingle :added "0.1"}
(fact "constructs a single contract"
  ^:hidden
  
  (defn.js ContractSingleDemo
    []
    (var props-yes (@! (first +traded-yes+)))
    (var props-no (@! (first +traded-no+)))
    (return
     (n/EnclosedCode 
{:label "pune.web.debug-contract/ContractSingle"} 
[:% n/Row
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#eee"
                 :padding 10}}
        [:% debug-contract/ContractSingle
         #{[:design {:type "light"}
            (:.. props-yes)]}]
        [:% n/Padding {:style {:height 3}}]
        [:% debug-contract/ContractSingle
         #{[:design {:type "light"}
            (:.. props-no)]}]]
       [:% n/View
        {:style {:flex 1
                 :backgroundColor "#333"
                 :padding 10}}
        [:% debug-contract/ContractSingle
         #{[:design {:type "dark"}
            (:.. props-yes)]}]
        [:% n/Padding {:style {:height 3}}]
        [:% debug-contract/ContractSingle
         #{[:design {:type "dark"}
            (:.. props-no)]}]]])))
  
  (def.js MODULE (!:module))
  )
