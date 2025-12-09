(ns js.react-native.ui-picker-test
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
             [js.react-native.physical-addon :as physical-addon]
             [js.react-native.ui-picker :as ui-picker]
             ]
   })

^{:refer js.react-native.ui-picker/pickerTheme :added "4.0" :unchecked true}
(fact "creates the picker theme")

^{:refer js.react-native.ui-picker/usePickerPosition :added "4.0" :unchecked true}
(fact "helper function to connect picker position")

^{:refer js.react-native.ui-picker/PickerValues :added "4.0" :unchecked true}
(fact "creates non editable picker values for display only")

^{:refer js.react-native.ui-picker/PickerIndexed :added "0.1"}
(fact "creates a slim picker"
  ^:hidden
  
  (defn.js PickerIndexedDemo
    []
    (var [first setFirst] (r/local 5))
    (var [highlighted setHighlighted] (r/local false))
    (var [disabled setDisabled] (r/local false))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-picker/PickerIndexed"} 
[:% n/Row
       #_{:style {:alignItems "center"
                :justifyContent "center"}}
       [:% n/Text
        {:style {:width 80
                 :fontSize 12
                 :color "#333"
                 :fontWeight "700"}}
        "Picker"]
       [:% ui-picker/PickerIndexed
        {:highlighted highlighted
         :disabled disabled
         :theme {:bgNormal "red"
                 :fgNormal "blue"
                 :bgHovered 0.7
                 :fgHovered 0.7
                 :bgPressed -0.2
                 :fgPressed 0.1}
         :index first
         :setIndex setFirst
         :items ["A-1" "A-2" "A-3" "A-4" "A-5" "A-6"]
         :addons [(physical-addon/tagAll
                   {:style {:paddingHorizontal 20
                            :height 80
                            :width 100
                            :flex 1}})]}]] 
[:% n/Row
       [:% n/Button
        {:title "+1"
         :onPress (fn:> (setFirst (+ first 1)))}]
       [:% n/Button
        {:title "-1"
         :onPress (fn:> (setFirst (- first 1)))}]
       [:% n/Button
        {:title "H"
         :onPress (fn:> (setHighlighted (not highlighted)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "D"
         :onPress (fn:> (setDisabled (not disabled)))}]
       [:% n/Text
        (n/format-entry #{first disabled highlighted})]])))
  
  
  
  )
