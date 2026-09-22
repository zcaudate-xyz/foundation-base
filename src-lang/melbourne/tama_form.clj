(ns melbourne.tama-form
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/tama-form
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.core.impl :as j]
             [js.react :as r :include [:fn]]
             [js.react.ext-form :as ext-form]
             [js.tamagui :as tm]
             [xt.event.base-form :as event-form]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [melbourne.tama-theme :as tama-theme]]
   :export [MODULE]})

(defn.js FormEnclosed
  "creates a direct Tamagui field layout"
  [#{[design
      variant
      mini
      label
      labelHide
      labelNone
      styleLabel
      styleContainer
      children
      (:= minWidth 160)
      (:.. rprops)]}]
  (var labelElem
       (:? (and (not labelHide)
                (not labelNone))
           [:% tm/Label
            {:color "$color11"
             :fontSize 13
             :fontWeight "700"
             :paddingHorizontal "$1"
             :paddingTop "$1"
             :minWidth (:? mini nil 128)
             :style styleLabel}
            label]
           nil))
  (var rootProps
       (Object.assign
        {:gap "$1"
         :flex 1
         :minWidth minWidth}
        (:? mini {:flexDirection "row"
                  :alignItems "center"
                  :gap "$2"} {})
        (:? styleContainer {:style styleContainer} {})
        rprops))
  (return
   [:% tm/Theme
    {:name (tama-theme/themeName design)}
    [:% tm/YStack
     #{(:.. rootProps)}
     labelElem
     children]]))

(defn.js FormReadOnly
  "creates a read-only field with the shared Slim contract"
  [props]
  (var #{[design
          variant
          label
          labelHide
          labelNone
          styleLabel
          entry
          format
          template
          fieldProps
          styleContainer
          minWidth]} props)
  (var value (data/template-entry entry template props))
  (when (and value format)
    (:= value (format value)))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Text
     #{(:.. (Object.assign
             {:padding "$2"
              :borderWidth 1
              :borderColor "$borderColor"
              :borderRadius "$2"
              :color "$color11"}
             (or fieldProps {})))}
     (j/toString (or value " - "))]]))

(defn.js FormInput
  "creates a controlled input from a Slim form"
  [#{[design variant mini form meta label labelHide labelNone styleLabel
      field fieldProps styleContainer minWidth]}]
  (var #{[value result]} (ext-form/listenField
                           form
                           field
                           (Object.assign {:slim/type "input"
                                           :fn/type "field"}
                                          (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant mini label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Input
     #{(:.. (Object.assign
             {:size "$4"
              :value (j/toString (:? (lib/nil? value) "" value))
              :borderRadius "$2"
              :borderColor (:? (== (. result ["status"]) "errored")
                               "$red8"
                               "$borderColor")
              :onFocus (fn []
                         (event-form/validate-field form field))
              :onChangeText (fn [v]
                              (event-form/set-field form field v)
                              (event-form/validate-field form field))}
             (or fieldProps {})))}
     ]]))

(defn.js FormInputXL
  "creates a larger controlled input from a Slim form"
  [#{[design variant form meta field fieldProps label labelHide labelNone
      styleLabel styleContainer minWidth]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "input_xl"
                                   :fn/type "field"}
                                  (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Input
     #{(:.. (Object.assign
             {:size "$5"
              :value (j/toString (or value ""))
              :borderRadius "$3"
              :fontSize 16
              :onFocus (fn []
                         (event-form/validate-field form field))
              :onChangeText (fn [v]
                              (event-form/set-field form field v)
                              (event-form/validate-field form field))}
             (or fieldProps {})))}
     ]]))

(defn.js FormTextArea
  "creates a controlled multiline input from a Slim form"
  [#{[design variant mini form meta field fieldProps label labelHide labelNone
      styleLabel styleContainer minWidth]}]
  (return
   [:% -/FormInput
    #{[design variant mini form meta field label labelHide labelNone styleLabel
       styleContainer minWidth
       :fieldProps (Object.assign {:multiline true
                                   :minHeight 96
                                   :textAlignVertical "top"}
                                  (or fieldProps {}))]}]))

(defn.js FormCheckBox
  "creates a direct Tamagui checkbox from a Slim form"
  [#{[design variant form meta label styleLabel field fieldProps]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "checkbox"
                                   :fn/type "field"}
                                  (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant label styleLabel]}
    [:% tm/XStack
     {:alignItems "center"
      :gap "$2"}
     [:% tm/Checkbox
      #{(:.. (Object.assign
              {:size "$4"
               :checked (:? value true false)
               :onCheckedChange (fn [checked]
                                  (event-form/set-field form field checked)
                                  (event-form/validate-field form field))}
              (or fieldProps {})))}
      [:% tm/CheckboxIndicator
       [:% tm/Text
        {:fontWeight "900"
         :color "$blue10"}
        "✓"]]]
     [:% tm/Text
      {:color "$color"}
      label]]]))

(defn.js FormToggleButton
  "creates a direct Tamagui toggle button from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel field
      fieldProps text styleContainer minWidth]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "toggle_button"
                                   :fn/type "field"}
                                  (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Button
     #{(:.. (Object.assign
             {:size "$4"
              :theme (:? value "active" nil)
              :color "$color12"
              :onPress (fn []
                         (event-form/toggle-field form field)
                         (event-form/validate-field form field))}
             (or fieldProps {})))}
     (or text (:? value "ON" "OFF"))]]))

(defn.js FormToggleSwitch
  "creates a direct Tamagui switch from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel field
      fieldProps styleContainer minWidth]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "toggle_switch"
                                   :fn/type "field"}
                                  (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/XStack
     {:alignItems "center"
      :gap "$2"}
     [:% tm/Switch
      #{(:.. (Object.assign
              {:size "$4"
               :checked (:? value true false)
               :onCheckedChange (fn [checked]
                                  (event-form/set-field form field checked)
                                  (event-form/validate-field form field))}
              (or fieldProps {})))}
      [:% tm/SwitchThumb]]
     [:% tm/Text
      {:color "$color11"}
      (:? value "ON" "OFF")]]]))

(defn.js FormEnumSingle
  "creates a direct Tamagui single select from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel styleContainer
      field fieldProps minWidth (:= options [])]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "enum_single"
                                   :fn/type "field"}
                                  (or meta {}))))
  (var setValue
       (fn [v]
         (event-form/set-field form field v)
         (event-form/validate-field form field)))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Select
     #{(:.. (Object.assign {:value (or value "")
                            :onValueChange setValue}
                           (or fieldProps {})))}
     [:% tm/SelectTrigger
      {:width "100%"
       :maxWidth 360
       :color "$color12"}
      [:% tm/SelectValue {:placeholder "Choose one"}]]
     [:% tm/SelectContent
      {:zIndex 200000}
      [:% tm/SelectViewport
       {:minWidth 240}
       (j/map options
              (fn [option i]
                (return
                 [:% tm/SelectItem
                  {:key i
                   :index i
                   :value option}
                  [:% tm/SelectItemText option]])))]]]]))

(defn.js FormEnumMulti
  "creates a direct Tamagui multi-choice control from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel styleContainer
      field fieldProps minWidth (:= options [])]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "enum_multi"
                                   :fn/type "field"}
                                  (or meta {}))))
  (var values (or value []))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/XStack
     {:gap "$1"
      :flexWrap "wrap"}
     (j/map options
            (fn [option i]
              (var selected (. values (includes option)))
              (return
               [:% tm/Button
                {:key i
                 :size "$2"
                 :chromeless (not selected)
                 :color "$color12"
                 :onPress (fn []
                            (event-form/set-field
                             form
                             field
                             (:? selected
                                 (. values
                                    (filter
                                     (fn [v]
                                       (return (not (== v option))))))
                                 (. values (concat [option]))))
                            (event-form/validate-field form field))}
                option])))]]))

(defn.js FormColorInput
  "creates a direct color input from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel styleContainer
      field fieldProps minWidth]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "color_input"
                                   :fn/type "field"}
                                  (or meta {}))))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/Input
     #{(:.. (Object.assign {:type "color"
                            :size "$4"
                            :value (or value "#2563eb")
                            :onChangeText (fn [v]
                                            (event-form/set-field form field v)
                                            (event-form/validate-field form field))}
                           (or fieldProps {})))}
     ]]))

(defn.js FormChipInput
  "creates a direct chip input from a Slim form"
  [#{[design variant form meta label labelHide labelNone styleLabel styleContainer
      field fieldProps minWidth]}]
  (var #{[value]} (ext-form/listenField
                   form
                   field
                   (Object.assign {:slim/type "chip_input"
                                   :fn/type "field"}
                                  (or meta {}))))
  (var [draft setDraft] (r/local ""))
  (var values (or value []))
  (var addValue
       (fn []
         (when (and draft
                    (> (. draft length) 0))
           (event-form/set-field form field (. values (concat [draft])))
           (event-form/validate-field form field)
           (setDraft ""))))
  (return
   [:% -/FormEnclosed
    #{[design variant label labelHide labelNone styleLabel styleContainer minWidth]}
    [:% tm/YStack
     {:gap "$2"}
     [:% tm/XStack
      {:gap "$1"
       :flexWrap "wrap"}
      (j/map values
             (fn [item i]
               (return
                [:% tm/Button
                 {:key i
                  :size "$2"
                  :chromeless true
                  :color "$color12"
                  :onPress (fn []
                             (event-form/set-field
                              form
                              field
                              (. values
                                 (filter
                                  (fn [v]
                                    (return (not (== v item))))))
                              )
                             (event-form/validate-field form field))}
                 item])))]
     [:% tm/XStack
      {:gap "$2"
       :alignItems "center"}
      [:% tm/Input
       #{(:.. (Object.assign
               {:flex 1
                :value draft
                :placeholder "Add a value"
                :onChangeText setDraft}
               (or fieldProps {})))}]
      [:% tm/Button
       {:size "$3"
        :color "$color12"
        :onPress addValue}
       "ADD"]]]]))


(defn.js FormLayout
  "creates a direct Tamagui form layout from Slim rows"
  [#{[design form mini meta rows children rowStyle fieldProps (:.. rprops)]}]
  (return
   [:% tm/YStack
    #{(:.. (Object.assign {:gap "$2"} rprops))}
    (j/map (or rows [])
           (fn [row i]
             (var #{[component field (:.. rowProps)]} row)
             (return
              [:% tm/YStack
               {:key (or field i)
                :style rowStyle}
               (r/createElement
                component
                (Object.assign
                 {:design design
                  :form form
                  :mini mini
                  :meta (Object.assign {} meta rowProps.meta)
                  :fieldProps (or (. (or fieldProps {}) [field]) {})}
                 rowProps))])))
    children]))

(def.js MODULE (!:module))
