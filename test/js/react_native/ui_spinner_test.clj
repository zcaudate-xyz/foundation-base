(ns js.react-native.ui-spinner-test
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
             [js.react-native.ui-spinner :as ui-spinner]
             ]
   })

^{:refer js.react-native.ui-spinner/spinnerTheme :added "4.0" :unchecked true}
(fact "creates the spinner theme")

^{:refer js.react-native.ui-spinner/SpinnerStatic :added "0.1"}
(fact "creates the spinner padding"
  ^:hidden
  
  (defn.js SpinnerStaticDemo
    []
    (var [text setText] (r/local "."))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-spinner/SpinnerStatic"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerStatic
         #{text}]]]))))

^{:refer js.react-native.ui-spinner/SpinnerDigit :added "4.0" :unchecked true}
(fact  "creates the spinner digit"
  ^:hidden
  
  (defn.js SpinnerDigitDemo
    []
    (var [index setIndex] (r/local 5))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-spinner/SpinnerDigit"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerDigit
         #{index}]]
       [:% n/Row
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setIndex (+ index 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setIndex (- index 1)))}]]]))))

^{:refer js.react-native.ui-spinner/SpinnerValues :added "4.0" :unchecked true}
(fact "creates the spinner values"
  ^:hidden
  
  (defn.js SpinnerValuesDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [100 0 1 2])
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-spinner/SpinnerValues"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/SpinnerValues
         #{value max min step decimal}]]
       [:% n/Row
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setValue (+ value 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setValue (- value 1)))}]]]))))

^{:refer js.react-native.ui-spinner/useSpinnerPosition :added "4.0" :unchecked true}
(fact "helper function to connect spinner position")

^{:refer js.react-native.ui-spinner/Spinner :added "0.1"}
(fact "creates the spinner value"
  ^:hidden
  
  (defn.js SpinnerDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [100 0 1 2])
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-spinner/Spinner"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner/Spinner
         #{value setValue max min step decimal}]]
       [:% n/Row
        [:% n/Button
         {:title "+1"
          :onPress (fn:> (setValue (+ value 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setValue (- value 1)))}]]])))
  
  
  
  )
