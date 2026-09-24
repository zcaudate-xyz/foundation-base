(ns melbourne.ui-chip-input
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r]
             [js.react-native :as n]
             [js.react-native.ui-util :as ui-util]
             [melbourne.ui-input :as ui-input]
             [melbourne.ui-chip :as ui-chip]
             [melbourne.ui-button :as ui-button]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js ChipInput
  "constructs a themed input"
  {:added "0.1"}
  [#{[design
      variant
      theme
      styleContainer
      style
      values
      setValues
      (:.. rprops)]}]
  (when (lib/is-string? values)
    (:= values (xt/x:json-decode values)))
  (when (data/is-empty? values)
    (:= values []))
  (var [showInput setShowInput] (r/local false))
  (var [currentText setCurrentText] (r/local ""))
  (var refInput (r/ref))
  
  (var visibleInput (or showInput
                        (data/is-empty? values)))
  (return
   [:% n/Row
    {:style styleContainer}
    [:% n/View
     [:% ui-util/Fold
      {:visible visibleInput}
      [:% ui-input/Input
       {:design design
        :refLink refInput
        :value currentText
        :onFocus (fn:> (setShowInput true))
        :onBlur  (fn:> (setShowInput false))
        :onSubmitEditing
        (fn []
          (cond (data/not-empty? currentText)
                (do (setValues [(:.. values) currentText])
                    (setCurrentText "")
                    (setShowInput false))

                (data/not-empty? values)
                (setShowInput false)))
        :onChangeText
        (fn [text]
          (cond (j/endsWith text ",")
                (do (var out (string/trim (data/first (j/split text ","))))
                    (when (data/not-empty? out)
                      (setValues [(:.. values) out])
                      (setCurrentText "")))

                :else
                (setCurrentText text)))}]]
     
     [:% n/Row
      {:style {:flexWrap "wrap"
               :maxWidth 400
               :alignItems "center"}}
      (j/map values
             (fn:> [value i]
               [:% ui-chip/Chip
                #{design
                  {:key i
                   :text value
                   :onClose (fn:> (setValues (data/arr-omit values i)))}}]))
      (:? (data/not-empty? values)
          [:% ui-button/Button
           #{design
             {:variant {:fg {:key "primary"}
                        :bg {:key "background"}
                        :pressed {:bg {:key "background"}}}
              :outlined true
              :onPress (fn:> (:? (data/not-empty? values)
                                 (setShowInput (not showInput))))
              :style {:borderRadius 0
                      :margin 3
                      :padding 4
                      :paddingHorizontal 7
                      :width 30
                      :borderWidth 1
                      :textAlign "center"}
              :text [:% n/Icon
                     {:key "icon"
                      :name (:? visibleInput "minus" "plus")}]}}])]]]))

(def.js MODULE (!:module))
