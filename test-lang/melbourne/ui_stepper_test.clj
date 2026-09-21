(ns melbourne.ui-stepper-test
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
             [melbourne.ui-stepper :as ui-stepper]]
   :export [MODULE]})

^{:refer melbourne.ui-stepper/StepperTabs :added "0.1"}
(fact "creates the stepper tabs"
  ^:hidden
  
  (defn.js StepperTabsDemo
    []
    (var [index setIndex] (r/local 0))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-stepper/StepperTabs"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-stepper/StepperTabs
         #{[:design {:type "light"}
            :total 5
            index
            setIndex
            :itemProps [{}
                        {}]]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-stepper/StepperTabs
         #{[:design {:type "dark"}
            :total 5
            index
            setIndex
            :itemProps [{}
                        {}]]}]]]))))

^{:refer melbourne.ui-stepper/stepperOffset :added "0.1"}
(fact "offset function for the stepper")

^{:refer melbourne.ui-stepper/Stepper :added "0.1"}
(fact "creates a stepper"
  ^:hidden
  
  (defn.js StepperDemo
    []
    (var [index setIndex] (r/local (fn:> 0)))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-stepper/Stepper"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-stepper/StepperTabs
         #{[:design {:type "light"}
            :total 4
            index
            setIndex
            :itemProps [{}
                        {}]]}]
        [:% ui-stepper/Stepper
         #{[index
            setIndex
            :style {:height 200
                    :width 200}
            :pageStyle {:height 200
                        :width 200}
            :pages
            [[:% n/View
              {:style {:flex 1
                       :backgroundColor "blue"}}
              [:% n/Button
               {:title "PRESS 1"
                :onPress (fn:> (alert "1"))}]]
             [:% n/View
              {:style {:flex 1
                       :backgroundColor "red"}}
              [:% n/Button
               {:title "PRESS 2"
                :onPress (fn:> (alert "2"))}]]
             [:% n/View
              {:style {:flex 1
                       :backgroundColor "yellow"}}
              [:% n/Button
               {:title "PRESS 3"
                :onPress (fn:> (alert "3"))}]]
             [:% n/View
              {:style {:flex 1
                       :backgroundColor "green"}}
              [:% n/Button
               {:title "PRESS 4"
                :onPress (fn:> (alert "4"))}]]]]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}]])))
  
  (def.js MODULE (!:module))
  )
