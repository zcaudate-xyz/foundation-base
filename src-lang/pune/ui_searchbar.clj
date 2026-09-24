(ns pune.ui-searchbar
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/pune-searchbar
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n]
             [xt.lang.common-lib :as lib]
             [pune.common.style :as common-style]]
   :export [MODULE]})

(defn.js SearchBar
  "creates a compact Pune search control without a wrapper stack"
  [#{[design
      value
      setValue
      onChangeText
      onSubmitEditing
      onClear
      placeholder
      disabled
      autoFocus
      style
      styleInput
      (:.. rprops)]}]
  (var theme (common-style/tokens design))
  (var changeFn (or onChangeText setValue (fn [])))
  (var clearFn
       (or onClear
           (fn []
             (when setValue
               (setValue "")))))
  (return
   [:% n/View
    {:style [{:flexDirection "row"
              :alignItems "center"
              :gap 8
              :width "100%"
              :minHeight 42
              :paddingHorizontal 12
              :borderWidth 1
              :borderRadius 10}
             (common-style/controlStyle design)
             style]}
    [:% n/Text
     {:style {:fontSize 17
              :color (. theme muted)}}
     "⌕"]
    [:% n/TextInput
     #{(:.. (Object.assign
             {:style [{:flex 1
                       :minWidth 0
                       :padding 0
                       :fontSize 14
                       :color (. theme text)}
                      styleInput]
              :value (or value "")
              :placeholder (or placeholder "Search")
              :placeholderTextColor (. theme muted)
              :editable (not disabled)
              :autoFocus autoFocus
              :onChangeText changeFn
              :onSubmitEditing onSubmitEditing}
             rprops))}]
    (:? (and value
             (> (. value length) 0))
        [:% n/Pressable
         {:accessibilityRole "button"
          :onPress clearFn}
         [:% n/Text
          {:style {:fontSize 18
                   :color (. theme muted)}}
          "×"]]
        nil)]))

(def.js MODULE (!:module))
