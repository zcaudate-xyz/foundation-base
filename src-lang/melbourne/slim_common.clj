(ns melbourne.slim-common
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :playground/web-basic
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n :include [:fn]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-form :as event-form]
             [melbourne.base-font :as base-font]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-chip-input :as ui-chip-input]
             [melbourne.ui-color-input :as ui-color-input]
             [melbourne.ui-input-xl :as ui-input-xl]
             [melbourne.ui-input :as ui-input]
             [melbourne.ui-checkbox :as ui-checkbox]
             [melbourne.ui-group :as ui-group]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-toggle-button :as ui-toggle-button]
             [melbourne.ui-toggle-switch :as ui-toggle-switch]
             [melbourne.addon-validation :as addon-validation]]
   :export [MODULE]})

(def.js StyleButtonXL
    {:fontSize 15
     :paddingVertical 12
     :paddingHorizontal 20})

(defn.js FormEnclosed
  "creates the form"
  {:added "0.1"}
  [#{[design
      variant
      mini
      label
      labelHide labelNone
      styleLabel
      children
      (:= minWidth 120)]}]
  (var labelElem
       (:? (not labelHide)
           [:% n/View
            {:key "label",
             :style (:? mini
                        {:paddingLeft 5, :paddingTop 10}
                        {:paddingLeft 5 :paddingTop 8})}
            [:% ui-static/Text
             #{[design
                :variant (or variant
                             {:fg {:key "primary"
                                   :mix "neutral"
                                   :ratio 5}})
                :style [(:? mini {} {:paddingBottom 0, :width 110})
                        (:.. (data/arrayify styleLabel))]]} label]]))
  (var formElem
       [:% n/View
        {:key "form"
         :style {:minWidth minWidth
                 :flex 1}}
        children])
  (return
   (:? labelNone
       formElem

       mini
       [:% n/View
        {:style {:flex 1
                 :margin 2}}
        labelElem formElem]

       :else
       [:% n/Row {:style {:margin 2
                          :flex 1
                          :flexWrap "wrap"}}
        labelElem formElem])))

(defn.js FormReadOnly
  "creates an Input"
  {:added "0.1"}
  [props]
  (var #{[design
          mini
          variant
          meta
          label
          labelHide labelNone
          styleLabel
          entry
          format
          template
          fieldProps
          minWidth]} props)
  (var value (data/template-entry entry template props))
  (when (and value format)
    (:= value (format value)))
  (var #{[style
          (:.. rprops)]} (or fieldProps {}))
  (return
   [:% -/FormEnclosed
    #{design mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% n/View
     {:style {:margin 5}}
     (r/% ui-static/Text
          (Object.assign #{design variant}
                    {:style [{:fontSize 13}
                             (:.. (data/arrayify style))]}
                    rprops)
          (j/toString (or value " - ")))]]))

(defn.js FormInput
  "creates an Input"
  {:added "0.1"}
  [#{[design
      mini
      variant
      form
      meta
      label
      labelHide labelNone
      styleLabel
      field
      fieldProps
      minWidth
      hideValidation]}]
  (var #{value result} (ext-form/listenField form field
                                             (Object.assign {:slim/type "input"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% -/FormEnclosed
    #{design mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% ui-input/Input
     #{[design variant
        :indicatorParams {:focusing {:default {:duration 100}}}
        :highlighted (== (. result ["status"])
                         "errored")
        :value (j/toString (:? (lib/nil? value) "" value))
        :onFocus (fn []
                   (event-form/validate-field form field))
        :onChangeText (fn [v]
                        (event-form/set-field form field v)
                        (event-form/validate-field form field))
        :addons [(:? (not hideValidation)
                     (addon-validation/addonValidation
                      #{[result design
                         :style {:position "absolute"
                                 :right 0
                                 :top -5}]}))]
        :outlined false
        (:.. fieldProps)]}]]))

(defn.js FormInputXL
  "creates a Form XL"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      field
      fieldProps
      minWidth
      hideValidation]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "input_xl"
                                                     :fn/type   "field"}
                                                    meta)))
  (var #{placeholder} fieldProps)
  (var validation (:? (not hideValidation)
                      (addon-validation/addonValidation
                       #{[result
                          design
                          :style
                          {:position "absolute", :right 2, :top -2}]})))
  (return
   [:% ui-input-xl/InputXL
    #{[design
       variant
       placeholder
       :highlighted (== (. result ["status"]) "errored")
       :value (j/toString (or value ""))
       :onFocus (fn [] (event-form/validate-field form field))
       :onChangeText (fn
                       [v]
                       (event-form/set-field form field v)
                       (event-form/validate-field form field))
       :addons
       [validation]
       :outlined false
       (:.. fieldProps)]}]
   #_(:? mini
       [:% -/FormInput
        #{[design
           variant
           mini
           form
           meta
           field
           minWidth
           hideValidation
           :label
           placeholder]}]
       )))

(defn.js FormTextArea
  "creates an TextArea"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      styleContainer
      hideValidation]}]
  (return
   [:% -/FormInput
    #{[design
       variant
       mini
       form
       :meta (Object.assign {:slim/type "textarea"}
                       meta)
       label
       styleLabel
       labelHide labelNone
       field
       minWidth
       hideValidation
       :fieldProps     (Object.assign {:multiline true
                                  :style  {:height 60
                                           :flex nil}}
                                 fieldProps)
       :styleContainer [{:flexDirection "column"}
                        (:.. (data/arrayify styleContainer))]]}]))

(defn.js FormCheckBox
  "creates a check box"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      style
      styleLabel
      stylePadding
      label
      field
      fieldProps
      (:.. rprops)]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "checkbox"
                                                     :fn/type   "field"}
                                                    meta)))
  (return
   [:% n/Row
    [:% n/Padding
     {:style [{:width 5 #_(:? mini 5 115)}
              (:.. (data/arrayify stylePadding))]}]
    [:% n/Row
     {:style {:paddingVertical 3}}
     [:% ui-checkbox/CheckBox
      #{[design variant
         :indicatorParams {:active {:default {:duration 100}}}
         :selected value
         :style style
         :onPress  (fn []
                     (event-form/toggle-field form field)
                     (event-form/validate-field form field))
         (:.. fieldProps)]}]
     [:% n/Padding {:style {:width 15}}]
     [:% ui-static/Text
      #{[design
         :variant (data/get-in design ["variant" "label"])
         :style [base-font/fontH6
                 {:padding 3}
                 (:.. (data/arrayify styleLabel))]]}
      label]]]))

(defn.js FormToggleButton
  "creates a Form Toggle Button"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      text
      minWidth]}]
  (var #{value result} (ext-form/listenField form field
                                             (Object.assign {:slim/type "toggle_button"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% n/Row
     [:% ui-toggle-button/ToggleButton
      #{[design variant
         :selected value
         :onPress  (fn []
                     (event-form/toggle-field form field)
                     (event-form/validate-field form field))
         :style {:marginTop 5
                 :marginHorizontal 5
                 :padding 5}
         text
         (:.. fieldProps)]}]]]))

(defn.js FormToggleSwitch
  "creates a Form Toggle Switch"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      text
      minWidth]}]
  (var #{value result} (ext-form/listenField form field
                                             (Object.assign {:slim/type "toggle_switch"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% n/Row
     {:style {:marginVertical 7}}
     [:% ui-toggle-switch/ToggleSwitch
      #{[design variant
         :selected value
         :onPress  (fn []
                     (event-form/toggle-field form field)
                     (event-form/validate-field form field))
         :style {:marginTop 5
                 :marginHorizontal 5
                 :padding 10}
         (:.. fieldProps)]}]]]))

(defn.js FormEnumSingle
  "creates an Enum Single"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      (:= options [])]}]
  (var #{value result}
       (ext-form/listenField form field
                             (Object.assign {:slim/type "enum_single"
                                        :fn/type   "field"}
                                       meta)))
  (var #{mainNeutral} (base-palette/designPalette design))
  (var Component (or (data/get-in fieldProps
                               ["component"])
                     ui-text/TabsMinor))
  (return 
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide
      labelNone
      minWidth}
    [:% Component
     #{[design variant
        :styleContainer {:left -10}
        :style {:marginHorizontal 5
                :padding 3}
        :data  options
        :value value
        :setValue (fn [value]
                    (event-form/set-field form field value)
                    (event-form/validate-field form field))
        
        (:.. fieldProps)]}]]))

(defn.js FormEnumMulti
  "creates an Enum Multi"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      (:= options [])]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "enum_multi"
                                                     :fn/type   "field"}
                                                    meta)))
  (var #{mainNeutral} (base-palette/designPalette design))
  (var Component (or (data/get-in fieldProps
                               ["component"])
                     ui-text/EnumMinor))
  (return 
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% Component
     #{[design variant
        :style {}
        :data  options
        :values value
        :setValues (fn [values]
                     (event-form/set-field form field values)
                     (event-form/validate-field form field))
        (:.. fieldProps)]}]]))

(defn.js FormColorInput
  "creates an Enum Single"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      (:= options [])]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "color_input"
                                                     :fn/type   "field"}
                                                    meta)))
  (var #{mainNeutral} (base-palette/designPalette design))
  (return 
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% ui-color-input/ColorInput
     #{[design variant
        :value value
        :setValue (fn [value]
                    (event-form/set-field form field value)
                    (event-form/validate-field form field))
        
        (:.. fieldProps)]}]]))

(defn.js FormChipInput
  "creates an Enum Multi"
  {:added "0.1"}
  [#{[design
      variant
      mini
      form
      meta
      label
      styleLabel
      labelHide labelNone
      field
      fieldProps
      minWidth
      (:= options [])]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "chip_input"
                                                     :fn/type   "field"}
                                                    meta)))
  (var #{mainNeutral} (base-palette/designPalette design))
  (return 
   [:% -/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% ui-chip-input/ChipInput
     #{[design variant
        :values value
        :setValues (fn [values]
                     (event-form/set-field form field values)
                     (event-form/validate-field form field))
        (:.. fieldProps)]}]]))


(defn.js FormLayout
  "creates the basic form layout"
  {:added "0.1"}
  [#{[design
      form
      mini
      meta
      (:= fieldProps {})
      (:= fieldStyle {})
      rowStyle
      rows
      children
      (:.. rprops)]}]
  (var formElement
       (fn [#{[component
               field
               (:.. rprops)]}
            i]
         (var props (Object.assign rprops
                              #{design mini form field 
                                {:meta (Object.assign {} meta rprops.meta)}}))
         (var style (data/arr-concat
                     [(:.. (data/arrayify rowStyle))]
                     (data/arrayify (. fieldStyle field))))
         (return
          [:% n/Row
           #{[:key (or field i)
              :style [{:marginVertical 4}
                      (:.. (data/arrayify style))]
              (:.. (or (. fieldProps [field])
                       {}))]}
           (r/createElement component props)])))
  (return
   [:% n/View
    #{(:.. rprops)}
    (j/map (j/filter (data/arrayify rows) lib/identity)
           formElement)
    (:? (data/not-empty? children)
        [:% n/Padding {:style {:height 16}}])
    children]))

(def.js MODULE (!:module))
