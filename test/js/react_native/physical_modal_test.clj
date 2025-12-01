(ns js.react-native.physical-modal-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             ]
   :export [MODULE]})

^{:refer js.react-native.physical-modal/getPosition :adopt true :added "4.0" :unchecked true}
(fact "gets the position of a component"

  (defn.js GetPositionDemo
    []
    (var boxRef (r/ref))
    (var [display setDisplay] (r/local nil))
    (var [width setWidth]   (r/local 100))
    (var [height setHeight] (r/local 50))
    (r/init []
      (n/measureRef boxRef
                 setDisplay))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-modal-test/GetPositionDemo"} 
[:% n/Row
       {:style {:marginBottom 10}}
       [:% n/Button
        {:title "RAND"
         :onPress
         (fn []
           (setWidth (j/floor (* 100 (j/random))))
           (setHeight (j/floor (* 50 (j/random))))
           (j/future-delayed [100]
             (n/measureRef boxRef
                        setDisplay)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "MEASURE"
         :onPress
         (fn:> (n/measureRef boxRef
                          setDisplay))}]] 
[:% n/Row
       {:style {:height height
                 :backgroundColor "green"}}] 
[:% n/Row
       [:% n/View
        {:style {:width width
                 :backgroundColor "green"}}]
       [:% n/View
        {:ref boxRef
         :style {:height 100
                 :width 100
                 :backgroundColor "red"}}]
       [:% n/TextDisplay
        {:content (n/format-obj display)}]]))))

^{:refer js.react-native.physical-modal/displayModal :adopt true :added "4.0" :unchecked true}
(fact "gets the position of a component"

  (defn.js DisplayModalDemo
    []
    (var boxRef (r/ref))
    (var [showModal setShowModal] (r/local false))
    (var [display setDisplay] (r/local {:width  100,
                                        :height 100,
                                        :px 0,
                                        :py 0}))
    
    (r/init []
      (n/measureRef boxRef
                      setDisplay)
      (setShowModal false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-modal-test/displayModalDemo"} 
[:% n/Row
       {:style {:marginBottom 10}}
       [:% n/Button
        {:title "DISPLAY"
         :onPress
         (fn []
           (n/measureRef boxRef
                      setDisplay)
           (setShowModal true))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "MEASURE"
         :onPress
         (fn:> (n/measureRef boxRef
                          setDisplay))}]] 
[:% n/View
        {:ref boxRef
         :style {:height 100
                 :width 100
                 :backgroundColor "red"}}] 
[:% n/TextDisplay
       {:content (n/format-obj display)}] 
[:% n/Modal
       {:visible showModal
        :animationType "fade"
        :transparent true
        :onRequestClose (fn:> (setShowModal false))}
       [:% n/TouchableWithoutFeedback
        {:style {:flex 1}
         :onPress (fn:> (setShowModal false))}
        [:% n/View
         {:style {:flex 1
                  ;;:height 100
                  ;;:width 100
                  ;;:backgroundColor "blue"
                  }}
         [:% n/View
          {:style {:position "absolute"
                   :top display.py
                   :left (+ display.px display.width)}}
          [:% n/Button
           {:title "MODAL"
            :onPress (fn:>)}]]]]])))
  
  (def.js MODULE (!:module))

  )
