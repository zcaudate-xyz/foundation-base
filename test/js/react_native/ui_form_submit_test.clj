(ns js.react-native.ui-form-submit-test
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
             [js.react-native.ui-button :as ui-button]
             [js.react-native.ui-input :as ui-input]
             [xt.lang.base-lib :as k]]
   })

^{:refer js.react-native.ui-form/FavoriteFormSubmit
  :adopt true
  :added "0.1"}
(fact  "creates a slim switch box"
  ^:hidden

  (defn.js placeHolderText
    []
    (return [{:component n/Text
              :key "placeholder"
              :style {:position "absolute"
                      :fontSize 20
                      :top 13
                      :left 10
                      :zIndex -100
                      :color "#999"}
              :children ["What is your Favorite Food?"]
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
              :children ["Favorite"]
              :transformations
              (fn [#{emptying
                     focusing}]
                (var active (j/min (- 1 emptying)
                                   focusing))
                (return {:style {:opacity active}}))}]))
  
  (defn.js placeHolderBottom
    []
    (return [{:component n/Text
              :key "placeholder"
              :style {:position "absolute"
                      :fontSize 20
                      :top 0
                      :zIndex -100
                      :opacity 0}
              :children ["What is Your Favorite Food?"]
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

  (defn.js useFavoriteForm
    []
    (var [favorite setFavorite] (r/local "ice cream"))
    (var [expected setExpected] (r/local "ok"))
    (var [done setDone]         (r/local false))
    (var [waiting setWaiting]   (r/local (fn:> false)))
    (return #{favorite setFavorite
              expected setExpected
              waiting setWaiting
              done setDone}))
  
  (defn.js FaroriteFormReset
    [#{favorite setFavorite
       expected setExpected
       waiting setWaiting
       done setDone}]
    (return [:% n/Row
              {:style {:justifyContent "center"
                       :alignItems "center"}}
              [:% ui-button/Button
               {:text "RESET"
                :onPress (fn:> [] (setDone false))
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
  
  (defn.js FavoriteForm
    [#{favorite setFavorite
       expected setExpected
       waiting setWaiting
       done setDone}]
    (var submitFn (fn []
                    (setFavorite "")
                    (setWaiting true)
                    (j/setTimeout (fn []
                                    (setWaiting false)
                                    (alert (+ "You can submitted: "
                                              favorite))
                                    (setDone true))
                                  300)))
    (return [:% n/View
             [:% n/Row
              [:% ui-input/Input
               {:disabled waiting
                :selectionColor "white"
                :theme {:bgNormal "#ddd"
                        :bgHovered "#555"}
                :value favorite
                :onChangeText setFavorite
                :style {:height 50
                        :paddingLeft 8
                        :fontSize 20}
                
                :styleContainer {:flex 1
                                 :borderRadius 10
                                 :height 50
                                 :zIndex 100}
                :inner (-/placeHolderBottom)}]]
             [:% n/Row
              {:style {:flexDirection "row-reverse"}}
              [:% ui-button/Button
               {:text (:? waiting
                          [:% n/ActivityIndicator
                           {:style {:top -3}
                            :color "#555"}]
                          "SUBMIT")
                :onPress submitFn
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
                        :justifyContent "center"}
                :disabled waiting}]]]))
  
  (defn.js FavoriteFormSubmitDemo
    []
    (var form (-/useFavoriteForm))
    (var #{favorite setFavorite
           expected setExpected
           waiting setWaiting
           done setDone} form)
    (return
     (n/EnclosedCode 
{:label "js.react-native.ui-form-submit-test/FavoriteForm"} 
[:% n/View
       {:style {:justifyContent "center"
                :alignItems "center"}}
       [:% n/View
        {:style {:width 400
                 :height 150
                 :borderRadius 3
                 :backgroundColor "limegreen"
                 :padding 20
                 :margin 10}}
        (:? done
            [[:% -/FaroriteFormReset
              #{[(:.. form)]}]
             [:% -/FavoriteForm
              #{[(:.. form)]}]])]] 
[:% n/Row
       [:% n/Button
        {:title "WAITING"
         :onPress (fn:> (setWaiting (not waiting)))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "DONE"
         :onPress (fn:> (setDone (not done)))}]] 
[:% n/Caption
       {:text (n/format-entry #{favorite
                                waiting
                                expected})
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

