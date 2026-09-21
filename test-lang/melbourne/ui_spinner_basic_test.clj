(ns melbourne.ui-spinner-basic-test
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
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [melbourne.ui-spinner-basic :as ui-spinner-basic]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-input :as ui-input]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer melbourne.ui-spinner-basic/SpinnerBasicControls :added "0.1"}
(fact "creates spinner controls"
  ^:hidden

  (defn.js SpinnerBasicControlsDemo
    []
    (var [value setValue] (r/local 155))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
      {:label "melbourne.ui-spinner-basic/SpinnerBasicControls"} 
      [:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner-basic/SpinnerBasicControls
         #{value setValue max min step decimal 
           {:design {:type "light"
                     }}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner-basic/SpinnerBasicControls
         #{value setValue max min step decimal 
           {:design {:type "dark"
                     }}}]]]))))

^{:refer melbourne.ui-spinner-basic/SpinnerBasic :added "0.1"}
(fact "Creates a spinner"
  ^:hidden
  
  (defn.js SpinnerBasicDemo
    []
    (var [value setValue] (r/local 955))
    (var [max min step decimal] [1000 0 2 2])
    (return
     (n/EnclosedCode 
      {:label "melbourne.ui-spinner-basic/SpinnerBasic"} 
      [:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal 
           {:design {:type "light"}
            :style {:width 100}}}]
        [:% n/Text " "]
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal
           {:design {:type "light"
                     :mode "secondary"}}}]
        [:% n/Text " "]
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal
           {:design {:type "light"}}}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal
           {:design {:type "dark"}}}]
        [:% n/Text " "]
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal
           {:design {:type "dark"
                     :mode "secondary"}}}]
        [:% n/Text ""]
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue max min step decimal
           {:design {:type "dark"}}}]]]))))

^{:refer melbourne.ui-spinner-basic/SpinnerBasicEdit :added "0.1"}
(fact "Creates a spinner"
  ^:hidden
  
  (defn.js SpinnerBasicEditDemo
    []
    (var [value setValue] (r/local 955))
    (var [max min step decimal] [10000 0 2 2])
    (var [editShow  setEditShow] (r/local true))
    (var [editText  setEditText] (r/local
                                  (j/toFixed (/ value
                                                (Math.pow 10 decimal))
                                             decimal)))
    (var setEditTextNumber
         (fn [v]
           (var hasDot (== "." (data/last v)))
           (var num (Number.parseFloat v))
           (cond (data/is-empty? v)
                 (setEditText v)
                 
                 (lib/not-nil? num)
                 (setEditText
                  (+ (j/toString num)
                     (:? hasDot "." "")))

                 :else
                 (setEditText
                  (+ editText
                     (:? hasDot "." ""))))))
    (var refInput (r/ref))
    (r/watch [editShow]
      (cond editShow
            (setEditText
             (j/toFixed (/ value
                           (Math.pow 10 decimal))
                        decimal))
            :else
            (setValue
             (math/round
              (* (lib/to-number editText)
                 (Math.pow 10 decimal))))))
    (return
     (n/EnclosedCode 
      {:label "melbourne.ui-spinner-basic/SpinnerBasicEdit"} 
      [:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10
                 :height 50}}
        (:? (not editShow)
            [:% ui-spinner-basic/SpinnerBasic
             #{value setValue max min step decimal 
               {:design {:type "light"}
                :styleDigitText {:fontSize 15
                                 :paddingTop 3
                                 :marginRight 5}
                :style {:borderWidth 0
                        :borderRadius 3
                        :justifyContent "flex-end"
                        :paddingVertical 0
                        :alignContent "center"
                        :marginTop 2
                        :marginLeft 2
                        :marginRight 2
                        :height 26
                        :marginVertical 0
                        :width 100}}}]
            [:% ui-input/Input
             {;;:refLink refInput
              :value   editText
              :onChangeText setEditTextNumber
              :onBlur  (fn []
                         (setEditShow false))
              :design {:type "light"}
              :variant {:bg   {:key "background"
                               :tone "darken"
                               :ratio 1}}
              :autoFocus true
              :styleContainer {:flex nil
                               :borderWidth 0
                               :width 100}
              :style {:textAlign "right"
                      :fontSize 15
                      :marginTop 0}}])
        [:% ui-text/ButtonAccent
         {:style {:marginBottom 2
                  :paddingHorizontal 5
                  :paddingVertical 4}
          :onPress (fn []
                     (setEditShow (not editShow)))
          :icon {:name (:? editShow "check" "edit")}}]]])))
  
  (def.js MODULE (!:module)))
