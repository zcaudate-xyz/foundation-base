(ns melbourne.ui-input-xl-test
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
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-input-xl :as ui-input-xl]]
   :export [MODULE]})

^{:refer melbourne.ui-input-xl/inputPlaceHolder :added "0.1"}
(fact "Creates the input placeholder"
  ^:hidden
  
  (defn.js InputPlaceHolderDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-input-xl/inputPlaceHolder"} 
[:% n/View
       {:style {:backgroundColor "#eee"
                :flex 1
                :padding 20}}]))))

^{:refer melbourne.ui-input-xl/InputXL :added "0.1"}
(fact "creates the large input"
  ^:hidden
  
  (defn.js InputXLDemo
    []
    (var [value setValue] (r/local))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-input-xl/InputXL"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-input-xl/InputXL
         {:value value
          :onChangeText setValue
          :placeholder "Enter your Name"}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-input-xl/InputXL
         {:value value
          :onChangeText setValue
          :design {:type "dark"}
          :placeholder "Enter your Name"}]]])))
  
  (def.js MODULE (!:module)))
