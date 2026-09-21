(ns melbourne.slim-dialog-test
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
             [melbourne.slim-dialog :as slim-dialog]]
   :export [MODULE]})

^{:refer melbourne.slim-dialog/Dialog :added "0.1"}
(fact "creates the dialog"
  ^:hidden
  
  (defn.js DialogDemo
    []
    (var [current setCurrent] (r/local true))
    (var [visible setVisible] (r/local false))
    (var buttonRef (r/ref))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-dialog/Dialog"} 
[:% n/Row
       {:style {:marginTop 30}}
       [:% n/Button
        {:ref buttonRef
         :title "ON"
         :onPress (fn:> (setVisible true))}]] 
[:% slim-dialog/Dialog
       {:design {:type "light"}
        :title "Confirm Delete"
        #_#_:titleRight [:% ui-button/Button
                         {:design {:type "light"
                                  :mode ["minor"]}
                          :text "OFF"
                          :onPress (fn:> (setVisible false))}]
        :body [:<>
               "Are you sure you wish to delete?"]
        :modalProps {;;:hostRef buttonRef
                     #_#_#_#_:transition "none"
                     :effect {:fade 0.1
                              :zoom 0.1}}
        :onSubmit (fn:> (setVisible false))
        :onCancel (fn:> (setVisible false))
        :visible visible}])))
  
  (def.js MODULE (!:module)))
