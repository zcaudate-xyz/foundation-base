(ns js.react-native.ui-modal-test
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
             [js.react-native.ui-modal :as ui-modal]
             ]
   })

^{:refer js.react-native.ui-modal/ModalBackdrop :added "4.0" :unchecked true}
(fact "constructs the modal backdrop")

^{:refer js.react-native.ui-modal/useMeasureContent :added "4.0" :unchecked true}
(fact "measures the modal content")

^{:refer js.react-native.ui-modal/ModalElementAbsolute :added "4.0" :unchecked true}
(fact "originates the modal from the screen")

^{:refer js.react-native.ui-modal/ModalElementHost :added "4.0" :unchecked true}
(fact "allows the modal to originate from an element")

^{:refer js.react-native.ui-modal/Modal :added "4.0" :unchecked true}
(fact "creates a Modal"
  ^:hidden
  
  (defn.js ModalDemo
    []
    (var [visible setVisible] (r/local))
    (var [position setPosition] (r/local "centered"))
    (var [transition setTransition] (r/local "from_top"))
    (r/watch [transition]
      (when (== false visible)
        (setVisible true)))
    (r/init []
      (setVisible false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-modal/Modal"} 
[:% n/Row
       [:% n/Button
        {:title "T"
         :onPress (fn:> (setVisible true))}]] 
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
[:% ui-modal/Modal
       {:visible visible
        :position position
        :transition transition
        :onClose (fn:> (setVisible false))
        :styleBackdrop {:backgroundColor "#222"}}
       [:% n/View
        {:style {:width 420
                 :height 200}}
        [:% n/Text
         {:style {:flex 1
                  :backgroundColor "yellow"}}
         "HELLO"]]] 
[:% n/Caption
       {:text (n/format-obj #{visible})
        :style {:marginTop 10}}])))
  
  )
