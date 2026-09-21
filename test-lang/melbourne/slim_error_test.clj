(ns melbourne.slim-error-test
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
   :require [[js.react-native :as n :include [:fn]]
             [melbourne.slim-error :as slim-error]]
   :export [MODULE]})

^{:refer melbourne.slim-error/ErrorInfo :added "0.1"}
(fact "constructs a error info demo"
  ^:hidden
  
  (defn.js ErrorInfoDemo
    []
    (return
     [:% n/PortalSink
      (n/EnclosedCode 
{:label "melbourne.slim-error/ErrorInfo"} 
[:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :flex 1
                  :padding 10}}
         [:% slim-error/ErrorInfo
          {:brand {:type "light"}}]
         [:% n/Padding {:style {:height 5}}]
         [:% slim-error/ErrorInfo
          {:brand {:type "light"}
           :result {:tag "user.account/incorrect_password"}}]
         [:% n/Padding {:style {:height 5}}]
         [:% n/Row
          [:% slim-error/ErrorInfo
           {:brand {:type "light"}}]
          [:% n/Padding {:style {:width 5}}]
          [:% slim-error/ErrorInfo
           {:brand {:type "light"}}]]]
        [:% n/View
         {:style {:backgroundColor "#333"
                  :flex 1
                  :padding 10}}
         [:% slim-error/ErrorInfo
          {:brand  {:type "dark"}}]
         [:% n/Padding {:style {:height 5}}]
         [:% slim-error/ErrorInfo
          {:brand {:type "dark"}
           :result {:tag "user.account/incorrect_password"}}]
         [:% n/Padding {:style {:height 5}}]
         [:% n/Row
          [:% slim-error/ErrorInfo
           {:brand {:type "dark"}}]
          [:% n/Padding {:style {:width 5}}]
          [:% slim-error/ErrorInfo
           {:brand {:type "dark"}}]]]])]))
  
  (def.js MODULE (!:module))
  )
