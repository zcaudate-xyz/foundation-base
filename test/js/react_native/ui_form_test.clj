(ns js.react-native.ui-form-test
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
             [js.react-native.ui-input :as ui-input]
             [xt.lang.base-lib :as k]]
   })

^{:refer js.react-native.ui-form-test/LoginForm
  :adopt true
  :added "0.1"}
(fact  "creates a slim switch box"
  ^:hidden

 (defn.js toTop
    [v]
   (return
    [{:translateY (k/mix 13 -20 v)}
     {:translateX (k/mix 10 -5 v)}]))
  
  (defn.js toRight
    [v]
    (return
     [{:translateX (k/mix 10 -5 v)}
      {:translateY (k/mix 13 -20 v)}]))
  
  (defn.js LoginFormDemo
    []
    (var [login setLogin] (r/local "oeu"))
    (var [password  setPassword]  (r/local "" #_"World"))
    (var refLink (r/ref))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-core/LoginForm"} 
[:% n/Row
       [:% ui-input/Input
        {:refLink refLink
         :selectionColor "white"
         :theme {:bgNormal "#ddd"
                 :bgHovered "#555"}
         :value login
         :onChangeText setLogin
         :style {:height 50
                 :borderRadius 3
                 :paddingLeft 8
                 :fontSize 20}
         
         :styleContainer {:flex 1
                          :maxWidth 400
                          :height 50
                          :zIndex 100}
         :inner [{:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :fontSize 20
                          :top 13
                          :left 10
                          :zIndex -100
                          :color "#999"}
                  :children ["Enter your Login"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (return {:style {:opacity emptying}}))}
                 {:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :top 10
                          :right 10
                          :zIndex -100
                          :opacity 0
                          :fontSize   14
                          :fontWeight "800"
                          :color "#999"}
                  :children ["Login"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (var active (j/min (- 1 emptying)
                                        focusing))
                     (return {:style {:opacity active}}))}]}]] 
[:% n/Caption
       {:text (n/format-entry #{login})
        :style {:marginTop 10}}]))))


^{:refer js.react-native.ui-form-test/LoginFormTop
  :adopt true
  :added "0.1"}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js toTop
    [v]
   (return
    [{:translateY (k/mix 13 -20 v)}
     {:translateX (k/mix 10 -5 v)}]))
  
  (defn.js LoginFormTopDemo
    []
    (var [login  setLogin]  (r/local "" #_"World"))
    (var refLink (r/ref))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-core/LoginFormTop"} 
[:% n/Row
       {:style {:paddingTop 20}}
       [:% ui-input/Input
        {
         :theme {:bgNormal "#ddd"
                 :bgHovered "#555"}
         :value login
         :onChangeText setLogin
         :style {:height 50
                 :borderRadius 3
                 :paddingLeft 8
                 :fontSize 20}
         :styleContainer {:flex 1
                          :maxWidth 400
                          :height 50
                          :zIndex 100}
         :secureTextEntry true
         :inner [{:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :fontSize 20
                          :top 0
                          :zIndex -100
                          :opacity 0}
                  :children ["Login"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (var active (j/max (- 1 emptying)
                                        focusing))
                     (return {:style {:opacity 1
                                      :fontSize   (k/mix 20  14 (- 1 emptying))
                                      :fontWeight (k/mix 400 800 (- 1 emptying))
                                      :color (c/toHSL (c/interpolate
                                                       ["#999" "#555e" ]
                                                       (- 1 emptying)))
                                      :transform
                                      (-/toTop (- 1 emptying)
                                               #_active)}}))}]}]] 
[:% n/Caption
       {:text (n/format-entry #{login})
        :style {:marginTop 10}}]))))

^{:refer js.react-native.ui-form-test/PasswordForm
  :adopt true
  :added "0.1"}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js PasswordFormDemo
    []
    (var [password setPassword] (r/local "oeu"))
    (var refLink (r/ref))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-core/PasswordForm"} 
[:% n/Row
       [:% ui-input/Input
        {:refLink refLink
         :selectionColor "white"
         :theme {:bgNormal "#ddd"
                 :bgHovered "#555"}
         :value password
         :onChangeText setPassword
         :style {:height 50
                 :borderRadius 3
                 :paddingLeft 8
                 :fontSize 20}
         :secureTextEntry true
         :styleContainer {:flex 1
                          :maxWidth 400
                          :height 50
                          :zIndex 100}
         :inner [{:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :fontSize 20
                          :top 13
                          :left 10
                          :zIndex -100
                          :color "#999"}
                  :children ["Enter your Password"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (return {:style {:opacity emptying}}))}
                 {:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :top 10
                          :right 10
                          :zIndex -100
                          :opacity 0
                          :fontSize   14
                          :fontWeight "800"
                          :color "#999"}
                  :children ["Password"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (var active (j/min (- 1 emptying)
                                        focusing))
                     (return {:style {:opacity active}}))}]}]] 
[:% n/Caption
       {:text (n/format-entry #{password})
        :style {:marginTop 10}}])))

  
  
  )


(comment

  [:% n/Row
       {:style {}}
       [:% ui-input/Input
        {
         :theme {:bgNormal "#ddd"
                 :bgHovered "#555"}
         :value password
         :onChangeText setPassword
         :style {:height 50
                 :borderRadius 3
                 :paddingLeft 8
                 :fontSize 20}
         :styleContainer {:flex 1
                          :maxWidth 400
                          :height 50
                          :zIndex 100}
         :secureTextEntry true
         :inner [{:component n/Text
                  :key "placeholder"
                  :style {:position "absolute"
                          :fontSize 20
                          :top 0
                          :zIndex -100
                          :opacity 0}
                  :children ["Password"]
                  :transformations
                   (fn [#{emptying
                          focusing}]
                     (var active (j/max (- 1 emptying)
                                        focusing))
                     (return {:style {:opacity 1
                                      :fontSize   (k/mix 20  14 (- 1 emptying))
                                      :fontWeight (k/mix 400 800 (- 1 emptying))
                                      :color (c/toHSL (c/interpolate
                                                       ["#999" "#555e" ]
                                                       (- 1 emptying)))
                                      :transform
                                      (-/toTop (- 1 emptying)
                                               #_active)}}))}]}]])

