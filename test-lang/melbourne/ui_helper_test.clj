(ns melbourne.ui-helper-test
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
             [js.react-native :as n]
             [melbourne.ui-helper :as ui-helper]]
   :export [MODULE]})

^{:refer melbourne.ui-helper/HelperControl :added "0.1"}
(fact "creates a pair of left/right helper control"
  ^:hidden
  
  (defn.js HelperControlDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-helper/HelperControl"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-helper/HelperControl
         {:design {:type "light"}
          :onLeft  (fn:> (setIndex (- index 1)))
          :onRight (fn:> (setIndex (+ index 1)))}
         [:% n/Text (n/format-entry #{index})]]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-helper/HelperControl
         {:design {:type "dark"}
          :onLeft  (fn:> (setIndex (- index 1)))
          :onRight (fn:> (setIndex (+ index 1)))}
         [:% n/Text (n/format-entry #{index})]]]])))
  
  (def.js MODULE (!:module))
  )
