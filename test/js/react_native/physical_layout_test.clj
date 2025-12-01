(ns js.react-native.physical-layout-test
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
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             ]
   :export [MODULE]})

^{:refer js.react-native.physical-layout/Grid
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a PanResponder"
  ^:hidden
  
  (defn.js GridDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native.physical-layout/Grid"} 
[:% n/View
       {:style {:flexDirection "row"
                :flexWrap "wrap"
                :align-content "space-between"}}
       (j/map
        [1 2 3 4 5 6 7 8 9]
        (fn:> [i]
          [:% n/View
           {:key i
            :style {:flex 1
                    :margin 5
                    :height 80
                    :backgroundColor "blue"
                    :minWidth 150
                    :maxWidth 180}}]))
       (j/map
        [1 2 3 4 5 6]
        (fn:> [i]
          [:% n/View
           {:key i
            :style {:flex 1
                    :marginHorizontal 5
                    :minWidth 150
                    :maxWidth 180}}]))]))))

^{:refer js.react-native.physical-layout/FlexWrap
  :adopt true
  :added "4.0" :unchecked true}
(fact "creates a set of edit points"
  ^:hidden
  
  (defn.js FlexWrapDemo
    []
    (let [[active setActive]  (r/local 1)
          ind    (a/useBinaryIndicator
                  active
                  {:default {:type "timing"
                             :duration 200
                             :easing a/linear}})]
      (return
       (n/EnclosedCode 
{:label "js.react-native.physical-layout/FlexWrap"} 
[:% n/Row
         [:% n/Button
          {:title "PUSH"
           :onPress (fn [] (setActive (not active)))}]
         [:% n/Fill]
         [:% physical-base/Tag
          {:indicator ind}]]))))

  (def.js MODULE (!:module))
  
  )

