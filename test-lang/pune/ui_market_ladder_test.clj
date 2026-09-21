(ns pune.ui-market-ladder-test
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.core.style :as css]
             [js.react-native.helper-color :as c]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as ui]
             [pune.ui-market-ladder :as market-ladder]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(def.js CHART
  (@! (std.json/read
       (h/sys:resource-content
        "pune/test_order_depthladder.json"))))

^{:refer pune.ui-market-ladder/MarketLadderText :added "0.1"}
(fact "market ladder text"
  ^:hidden
  
  (defn.js MarketLadderTextDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.ui-market-ladder/MarketLadderText"
       :style {:height 150}} 
(r/% market-ladder/MarketLadderText -/CHART)))))

^{:refer pune.ui-market-ladder/MarketLadderRow :added "0.1"}
(fact "market ladder row"
  ^:hidden
  
  (defn.js MarketLadderRowDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.ui-market-ladder/MarketLadderRow"} 
(r/% market-ladder/MarketLadderRow
           {"amount" 60,
            "rate" 66
            "design" {"type" "light",
                      "color" "blue"},
            "control"
            {"decimal" 2,
             "fraction" 0.01,
             "prediction" "yes"}})))))

^{:refer pune.ui-market-ladder/MarketLadder :added "0.1"}
(fact "market ladder row"
  ^:hidden
  
  (defn.js MarketLadderDemo
    []
    (return
     (n/EnclosedCode 
{:label "pune.ui-market-ladder/MarketLadder"} 
[:% n/Row
       (r/% market-ladder/MarketLadder -/CHART)] 
[:% n/View {:style {:height 330}}
       (r/% n/TextDisplay -/CHART)])))

  (def.js MODULE (!:module)))
