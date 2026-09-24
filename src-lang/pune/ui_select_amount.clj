(ns pune.ui-select-amount
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-input :as ui-input]
             [melbourne.ui-spinner-basic :as ui-spinner-basic]
             [pune.common.data-swap :as data-swap]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js SelectAmount
  [props]
  (var #{design
         variant
         theme
         value
         setValue
         setValueEdit
         max
         min
         step
         decimal} props)
  (var [editShow  setEditShow] (r/local))
  (var [editText  setEditText] (r/local
                                (j/toFixed (/ value
                                              (Math.pow 10 decimal))
                                           (Math.max 0 decimal))))
  (var calcValue
       (fn [num]
         (var val (math/round
                   (* num 
                      (Math.pow 10 decimal))))
         (:= val (:? (lib/not-nil? min)
                     (Math.max min val)
                     val))
         (:= val (:? (lib/not-nil? max)
                     (Math.min max val)
                     val))
         (return val)))
  
  (var setEditTextNumber
       (fn [v]
         (var isEnding (data/first (or (j/match v #"\.0+$")
                                    [])))
         (var isStarting (data/first (or (j/match v #"^\.")
                                    [])))
         (var hasDot (== "." (data/last v)))
         (var isZero (or (lib/nil? v)
                         (data/not-empty? )))
         (var num (Number.parseFloat v))
         (cond (data/is-empty? v)
               (setEditText "0")

               (or isEnding isStarting)
               (setEditText v)

               (lib/nil? num)
               (setEditText editText)
               
               (lib/not-nil? num)
               (do (setEditText
                    (+ (j/toString num)
                       (:? hasDot "." "")))
                   (setValue (calcValue num)))
               
               :else
               (setEditText
                (+ editText
                   (:? hasDot "." ""))))))
  (r/watch [editShow]
    (cond setValueEdit
          (do (var out (lib/to-number editText))
              (when (not (Number.isNaN out))
                (setValueEdit out editShow)))

          editShow
          (setEditText
           (j/toFixed (/ value
                         (Math.pow 10 decimal))
                      (Math.max 0 decimal)))

          :else
          (do (var out (lib/to-number editText))
              (when (not (Number.isNaN out))
                (setValue
                 (calcValue out))))))
  (return
   [:% n/Row
    {:style {:alignItems "center"
             :height 40}}
    (:? (not editShow)
        [:% ui-spinner-basic/SpinnerBasic
         #{value setValue step max min decimal
           design
           variant
           theme
           {:styleDigitText {:fontSize 24
                             :marginRight 5}
            :style {:borderWidth 0
                    :borderRadius 3
                    :justifyContent "flex-end"
                    :paddingVertical 0
                    :alignContent "center"
                    :marginTop  2
                    :marginLeft 2
                    :marginRight 2
                    :height 40
                    :marginVertical 0
                    :width 130}}}]
        [:% ui-input/Input
         #{design
           variant
           theme
           {;;:refLink refInput
            :value   editText
            :onChangeText setEditTextNumber
            :onBlur  (fn []
                       (setEditShow false))
            :design {:type "light"}
            :variant {:bg   {:key  "background"
                             :tone "darken"
                             :ratio 1}}
            :autoFocus true
            :styleContainer {#_#_:flex nil
                             :borderWidth 0
                             :width  130
                             :height 40}
            :style {:textAlign "right"
                    :fontSize 24
                    #_#_:fontWeight 600
                    :marginTop 0
                    :marginBottom 0}}}])
    [:% ui-text/ButtonAccent
     {:key editShow
      :style {:marginBottom 2
              :paddingHorizontal 5
              :paddingVertical 4}
      :variant (:? editShow
                   {:bg {:key "background"
                         :mix "neutral"
                         :ratio 3}}
                   {:bg {:key "neutral"}})
      :onPress (fn []
                 (setEditShow (not editShow)))
      :icon {:name "edit"}}]]))

(defn.js SelectPosition
  [props]
  (var #{design
         variant
         theme
         position
         setPosition
         onPress} props)
  (var [editShow  setEditShow] (r/local))
  (var [editText  setEditText] (r/local
                                (data-swap/position-to-fstr position)))
  (r/watch [position]
    (var posText (data-swap/position-to-fstr position))
    (when (not= editText posText)
      (setEditText posText)))
  (var setEditTextNumber
       (fn []
         (var num (Number.parseFloat editText))
         (cond (or (lib/nil? num)
                   (== 0 num)
                   (Number.isNaN num)
                   (data/is-empty? editText))
               (setEditText (data-swap/position-to-fstr position))

               :else
               (do (var p (data-swap/fprice-to-position num))
                   (setPosition p)
                   (setEditText (data-swap/position-to-fstr p))))))
  (return
   [:% n/Row
    {:style {:alignItems "center"
             :height 40}}
    [:% ui-input/Input
     #{design
       variant
       theme
       {:value   editText
        :onChangeText setEditText
        :onBlur setEditTextNumber
        :variant {:bg   {:key  "background"
                         :tone "darken"
                         :ratio 1}}
        :autoFocus true
        :styleContainer {#_#_:flex nil
                         :borderWidth 0
                         :width  130
                         :height 40}
        :style {:textAlign "right"
                :fontSize 24
                #_#_:fontWeight 600
                :marginTop 0
                :marginBottom 0}}}]
    [:% ui-text/ButtonAccent
     {:style {:marginBottom 2
              :paddingHorizontal 5
              :paddingVertical 4}
      :variant {:bg {:key "neutral"}}
      :onPress onPress
      :icon {:name "cycle"}}]]))

(def.js MODULE (!:module))
