(ns js.react-native.ui-form-walkthrough-test
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
             [js.react-native.helper-color :as c]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [js.react-native.animate :as a]
             [js.react-native.physical-base :as physical-base]
             [js.react-native.model-roller :as model-roller]
             [js.react-native.ui-button :as ui-button]
             [js.react-native.ui-input :as ui-input]
             [xt.lang.base-lib :as k]]
   })

^{:refer js.react-native.ui-form/LoginFormWalkthrough
  :adopt true
  :added "0.1"}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js placeHolderBottom
    [placeholder]
    (return [{:component n/Text
              :key "placeholder"
              :style {:position "absolute"
                      :fontSize 20
                      :top 0
                      :zIndex -100
                      :opacity 0}
              :children [placeholder]
              :transformations
              (fn [#{emptying
                     focusing}]
                (var active (j/max (- 1 emptying)
                                   focusing))
                (return {:style {:opacity 1
                                 :fontSize   (k/mix 20  12  active)
                                 :fontWeight (k/mix 400 800 active)
                                 :textShadowColor "black"
                                 :color (c/toHSL (c/interpolate
                                                  ["#999" "#555"]
                                                  active))
                                 :transform
                                 [{:translateY (k/mix 15 60 active)}
                                  {:translateX (k/mix 10 -5  active)}]}}))}]))
  
  (defn.js useLoginForm
    []
    (var [login setLogin]       (r/local ""))
    (var [password setPassword] (r/local ""))
    (var [step setStep]         (r/local 0))
    (var [waiting setWaiting]   (r/local (fn:> false)))
    (return #{login setLogin
              password setPassword
              waiting setWaiting
              step setStep}))
  
  (defn.js LoginFormReset
    [#{login setLogin
       password setPassword
       waiting setWaiting
       onSubmit}]
    (return [:% n/Row
              {:style {:justifyContent "center"
                       :alignItems "center"}}
              [:% ui-button/Button
               {:text "RESET"
                :onPress onSubmit
                :theme {:fgNormal "#333"
                        :bgNormal "#ccc"
                        :bgDisabled "#ccc"
                        :bgHovered "#555"}
                :styleText {:paddingTop 10
                            :fontSize 13
                            :fontWeight "600"
                            :borderRadius 3
                            :flex 1
                            :width 100
                            :height 35
                            :textAlign "center"}
                :style {:width 100
                        :height 35
                        :margin 10
                        :alignItems "center"
                        :justifyContent "center"}}]]))
  
  (defn.js LoginForm
    [#{login setLogin
       waiting
       onSubmit
       submitDisabled
       inputDisabled}]
    (return [:% n/View
             [:% n/Row
              [:% ui-input/Input
               {:disabled inputDisabled 
                :selectionColor "white"
                :theme {:bgNormal "#ccc"
                        :bgHovered "#555"}
                :value login
                :onChangeText setLogin
                :style {:height 50
                        :paddingLeft 8
                        :fontSize 20}
                
                :styleContainer {:flex 1
                                 :borderRadius 10
                                 :height 50
                                 :zIndex 100}
                :inner (-/placeHolderBottom "Enter your Username or Email")}]]
             [:% n/Row
              #_{:style {:flexDirection "row"}}
              [:% ui-button/Button
               {:text (:? waiting
                          [:% n/ActivityIndicator
                           {:style {:top -3}
                            :color "#555"}]
                          "NEXT")
                :onPress onSubmit
                :theme {:fgNormal "#444"
                        :bgNormal "palegreen"
                        :fgDisabled "#ddd"
                        :bgDisabled "#fff"
                        :bgHovered "#555"}
                :styleText {:paddingTop 10
                            :fontSize 13
                            :fontWeight "600"
                            :borderRadius 3
                            :flex 1
                            :width 100
                            :height 35
                            :textAlign "center"}
                :style {:right 0
                        :width 100
                        :height 35
                        :margin 10
                        :alignItems "center"
                        :justifyContent "center"}
                :disabled submitDisabled}]]]))
  
  (defn.js PasswordForm
    [#{password setPassword
       waiting
       onSubmit
       submitDisabled
       inputDisabled}]
    (return [:% n/View
             [:% n/Row
              [:% ui-input/Input
               {:disabled inputDisabled 
                :selectionColor "white"
                :theme {:bgNormal "#ddd"
                        :bgHovered "#555"}
                :value password
                :onChangeText setPassword
                :style {:height 50
                        :paddingLeft 8
                        :fontSize 20}
                
                :styleContainer {:flex 1
                                 :borderRadius 10
                                 :height 50
                                 :zIndex 100}
                :inner (-/placeHolderBottom "Enter your Password")}]]
             [:% n/Row
              {:style {:flexDirection "row-reverse"}}
              [:% ui-button/Button
               {:text (:? waiting
                          [:% n/ActivityIndicator
                           {:style {:top -3}
                            :color "#555"}]
                          "SUBMIT")
                :onPress onSubmit
                :theme {:fgNormal "#333"
                        :bgNormal "palegreen"
                        :bgDisabled "#ccc"
                        :bgHovered "#555"}
                :styleText {:paddingTop 10
                            :fontSize 13
                            :fontWeight "600"
                            :borderRadius 3
                            :flex 1
                            :width 100
                            :height 35
                            :textAlign "center"}
                :style {:width 100
                        :height 35
                        :margin 10
                        :alignItems "center"
                        :justifyContent "center"}
                :disabled submitDisabled}]]]))

  (defn.js LoginFormWalkthroughDemo
    []
    (var form (-/useLoginForm))
    (var #{login setLogin
           password setPassword
           waiting setWaiting
           step setStep} form)
    (var submitLogin
         (fn []
           (setWaiting true)
           (j/setTimeout (fn []
                           (setWaiting false)
                           (setStep 1))
                         400)))
    (var submitPassword
         (fn []
           (setWaiting true)
           (j/setTimeout (fn []
                           (setWaiting false)
                           (setStep 2))
                         400)))
    (var submitReset
         (fn []
           (setLogin "")
           (setPassword "")
           (setStep 0)))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-form-walkthrough-test/LoginFormWalkthroughCarosel"} 
[:% n/View
       {:style {:justifyContent "center"
                :alignItems "center"}}
       [:% n/View
        {:style {:width 400
                 :height 150
                 :borderRadius 3
                 :backgroundColor "yellow"
                 :padding 20
                 :margin 10}}
        (:? (== step 0)
            [:% -/LoginForm
             #{[:onSubmit submitLogin
                :submitDisabled (k/is-empty? login)
                :inputDisabled  waiting
                (:.. form)]}]
            (== step 1)
            [:% -/PasswordForm
             #{[:onSubmit submitPassword
                :submitDisabled (k/is-empty? password)
                :inputDisabled  waiting
                (:.. form)]}]
            :else
            [:% -/LoginFormReset
             #{[:onSubmit submitReset
                (:.. form)]}])]] 
[:% n/Row
       [:% n/Button
        {:title "WAITING"
         :onPress (fn:> (setWaiting (not waiting)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "STEP"
         :onPress (fn:> (setStep (mod (+ 1 step) 3)))}]] 
[:% n/Caption
       {:text (n/format-entry #{login
                                step
                                waiting
                                password})
        :style {:marginTop 10}}]))))



^{:refer js.react-native.ui-form/LoginFormStepper
  :adopt true
  :added "0.1"}
(fact "adding a carosel stepper"
  ^:hidden
  
  (defn.js LoginFormWalkthroughStepperDemo
    []
    (var form (-/useLoginForm))
    (var [session setSession] (r/local (j/random)))
    (var #{login setLogin
           password setPassword
           waiting setWaiting
           step setStep} form)
    (var ioffset0   (a/useCircularIndicator
                     step
                     {:default {:type "timing"
                                :duration 500
                                :easing a/linear}}
                     
                     "chainedAll"
                     nil
                     nil
                     3))
    (var modelFn (r/const (model-roller/roller-model 3 10)))
    (var submitLogin
         (fn []
           (setWaiting true)
           (j/setTimeout (fn []
                           (setWaiting false)
                           (setStep 1))
                         400)))
    (var submitPassword
         (fn []
           (setWaiting true)
           (j/setTimeout (fn []
                           (setWaiting false)
                           (setStep 2))
                         400)))
    (var submitReset
         (fn []
           (setSession (j/random))
           (setLogin "")
           (setPassword "")
           (setStep 0)))
    (var offsetFn
         (fn [offset index]
           (var v (- offset index))
           (var #{translate
                  scale
                  visible} (modelFn v))
           (return
            {:style {:opacity (:? visible
                                  (k/mix -2 1 scale)
                                  0)
                     :zIndex (* 100 scale)
                     :transform
                     [{:translateX (* -30 translate)}]}})))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-form-walkthrough-test/LoginFormStepper"} 
[:% n/View
       {:style {:justifyContent "center"
                :alignItems "center"}}
       [:% n/View
        {:style {:borderRadius 3
                 :backgroundColor "limegreen"
                 :padding 20
                 :margin 10}}
        [:% n/View
         {:style {:width 400
                  :height 150
                  :borderRadius 3
                  :backgroundColor "yellow"
                  :margin 20
                  :overflow "hidden"}}
         [:% physical-base/Box
          {:style {:position "absolute"
                   :width 360}
           :indicators {:offset ioffset0}
           :children [[:% -/LoginForm
                       #{[:key session
                          :onSubmit submitLogin
                          :submitDisabled (or (not= step 0)
                                              (k/is-empty? login))
                          :inputDisabled  (or (not= step 0)
                                              waiting)
                          (:.. form)]}]]
           :transformations
           {:offset (fn [offset]
                      (return (offsetFn offset 0)))}}]
         [:% physical-base/Box
          {:style {:position "absolute"
                   :width 360}
           :indicators {:offset ioffset0}
           :children [[:% -/PasswordForm
                       #{[:key session
                          :onSubmit submitPassword
                          :submitDisabled (or (not= step 1)
                                              (k/is-empty? password))
                          :inputDisabled  (or (not= step 1)
                                              waiting)
                          (:.. form)]}]]
           :transformations
           {:offset (fn [offset]
                      (return (offsetFn offset 1)))}}]
         [:% physical-base/Box
          {:style {:position "absolute"
                   :width 360}
           :indicators {:offset ioffset0}
           :children [[:% -/LoginFormReset
                       #{[:onSubmit submitReset
                          (:.. form)]}]]
           :transformations
           {:offset (fn [offset]
                      (return (offsetFn offset 2)))}}]]]] 
[:% n/Row
       [:% n/Button
        {:title "WAITING"
         :onPress (fn:> (setWaiting (not waiting)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "STEP"
         :onPress (fn:> (setStep (mod (+ step 1) 3)))}]] 
[:% n/Caption
       {:text (n/format-entry #{login
                                step
                                waiting
                                password})
        :style {:marginTop 10}}])))
  

  (def.js MODULE
    (do (:# (!:uuid))
        (!:module)))
  
  )
