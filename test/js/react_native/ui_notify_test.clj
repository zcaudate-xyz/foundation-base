(ns js.react-native.ui-notify-test
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
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.ui-notify :as ui-notify]
             ]
   })

^{:refer js.react-native.ui-notify/NotifyInner :added "4.0" :unchecked true}
(fact "creates the inner notification element")

^{:refer js.react-native.ui-notify/Notify :added "4.0" :unchecked true}
(fact "creates a Notify"
  ^:hidden
  
  (defn.js NotifyPane
    [#{visible
       position
       transition
       setVisible}]
    (return
     [:% ui-notify/Notify
      {:visible visible
       :margin 20
        :position position
        :transition transition}
      [:% n/View
       [:% n/Text
        {:style {:backgroundColor "yellow"}}
        "HELLO"]]]))
  
  (defn.js NotifyDemo
    []
    (var [visible setVisible] (r/local true))
    (var [position setPosition] (r/local "centered"))
    (var [transition setTransition] (r/local "from_top"))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-notify/Notify"
       :style {}} 
[:% n/Row
       [:% n/Button
        {:title "T"
         :onPress (fn:> (setVisible (not visible)))}]] 
[:% n/Tabs
       {:data ["centered"
               "top"
               "left"
               "bottom"
               "right"
               "top_right"
               "top_left"
               "bottom_right"
               "bottom_left"]
        :value position
        :setValue setPosition}] 
[:% n/Tabs
       {:data ["from_top"
               "from_bottom"
               "from_left"
               "from_right"]
        :value transition
        :setValue setTransition}] 
[:% n/Row
       {:style {:backgroundColor "#eee"}}
       [:% n/PortalProvider
        [:% n/PortalSink
         {:style {:height 400
                  :width 400
                  :backgroundColor "black"}}
         [:% -/NotifyPane
          #{visible
            position
            transition
            setVisible}]]]] 
[:% n/TextDisplay
       {:content (n/format-entry #{visible
                                   position
                                   transition})}])))
  
  (def.js MODULE
    (do (:# (!:uuid))
        (!:module)))
  
  )
