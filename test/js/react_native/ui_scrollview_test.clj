(ns js.react-native.ui-scrollview-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react-native :as n :include [:fn]]
             [js.react-native.ui-scrollview :as ui-scrollview]
             ]
   :export [MODULE]})

^{:refer js.react-native.ui-scrollview/ScrollViewImpl :added "4.0" :unchecked true}
(fact "creates a non global enhanced scrollview")

^{:refer js.react-native.ui-scrollview/ScrollView :added "4.0" :unchecked true}
(fact "creates a scrollview"
  ^:hidden
  
  (defn.js ScrollViewDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-scrollview/ScrollView"} 
[:% ui-scrollview/ScrollView
       {:style {:height 300
                :width 400}}
       [:% n/View
        {:style {:height 500
                 :backgroundColor "yellow"}}]])))
  
  (def.js MODULE (!:module)))
