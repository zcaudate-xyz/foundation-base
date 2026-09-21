(ns melbourne.ui-toggle-switch-test
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
             [melbourne.ui-toggle-switch :as ui-toggle-switch]]
   :export [MODULE]})

^{:refer melbourne.ui-toggle-switch/ToggleSwitch :added "0.1"}
(fact  "creates the toggle switch"
  ^:hidden
  
  (defn.js ToggleSwitchDemo
    []
    (var [s0 setS0] (r/local false))
    (var [s1 setS1] (r/local true))
    (var [s2 setS2] (r/local true))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-toggle-switch/ToggleSwitch"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-toggle-switch/ToggleSwitch
         #{[:design {:type "light"}
            :selected s1
            :onPress (fn:> (setS1 (not s1)))
            ]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-toggle-switch/ToggleSwitch
         #{[:design {:type "dark"}
            :selected s2
            :onPress (fn:> (setS2 (not s2)))]}]]])))

  (def.js MODULE (!:module))
  )
