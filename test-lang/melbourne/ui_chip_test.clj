(ns melbourne.ui-chip-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n]
             [js.react :as r]
             [melbourne.ui-static :as ui-static]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-chip :as ui-chip]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-chip/Chip :added "4.0"}
(fact "creates a static div"
  ^:hidden
  
  (defn.js ChipDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-chip/Chip"} 
[:% n/Row
       [:% ui-chip/Chip
        {:text "hello"
         :onClose (fn:>)}]
       
       [:% ui-chip/Chip
        {:text "world"
         :variant {:bg {:key "neutral"}}}]] 
[:% ui-static/Div
       {:design {:type "dark"}
        :style {:flexDirection "row"}}
       [:% ui-chip/Chip
        {:design {:type "dark"}
         :text "hello"
         :onClose (fn:>)}]
       
       [:% ui-chip/Chip
        {:design {:type "dark"}
         :variant {:bg {:key "neutral"}}
         :text "world"}]])))
  
  (def.js MODULE (!:module)))
