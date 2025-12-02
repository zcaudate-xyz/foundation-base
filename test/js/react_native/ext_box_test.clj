(ns js.react-native.ext-box-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]
            [js.cell.playground :as browser]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-box :as ext-box]
             [xt.lang.event-box :as event-box]]
   :export [MODULE]})

^{:refer js.react.ext-box/useBox :adopt true :added "4.0" :unchecked true}
(fact "uses an async entry"
  ^:hidden
  
  (defn.js UseBoxDemo
    []
    (var box (ext-box/createBox
              (fn:> {"account" "hello"})))
    (var getCount (r/useGetCount))
    (var [value setValue] (ext-box/useBox box ["account"]))
    (return
     (n/EnclosedCode 
      {:label "js.react.ext-box/useBox"} 
      [:% n/Row
       [:% n/TextInput
        {:value value
         :onChangeText setValue}]
       [:% n/Button
        {:title "Hello"
         :onPress (fn:> (setValue "hello"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "World"
         :onPress (fn:> (setValue "world"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "Other"
         :onPress (fn:> (event-box/set-data
                         box
                         ["other"]
                         (j/random)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "SAME"
         :onPress (fn:> (event-box/set-data
                         box
                         ["account"]
                         (event-box/get-data
                          box
                          ["account"])))}]] 
      [:% n/TextDisplay
       {:content (n/format-entry {:data (event-box/get-data box)
                                  :value value
                                  :counter (getCount)})}])))
  
  (def.js MODULE (!:module)))
  
