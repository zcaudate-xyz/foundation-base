(ns js.blessed.form
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-form :as base-form]
             [js.core :as j]
             [js.react :as r]
             [js.react.ext-form :as ext-form]
             [js.blessed.ui-style :as ui-style]
             [js.blessed.ui-core :as ui-core]
             [js.blessed.ui-group :as ui-group]
             [js.blessed.ui-date :as ui-date]]})

(defn.js FormWrapper
  "addes `width`, `offset` and `label`"
  {:added "4.0"}
  [props]
  (let [#{width offset label children x y color} props
        tprops (ui-style/getTopProps props)]
    (return [:box #{...tprops}
             [:box {:style {:bold true
                            :bg "black"
                            :fg "white"}
                    :top 0
                    :left (+ offset (- (k/len (or label ""))))
                    :content (or label "")}]
             [:box {:width 1
                    :height 1
                    :left (+ 1 offset)
                    :style {:bg (or color "yellow")}}]
             [:box {:bg "black"
                    :shrink true
                    :top (or y 0)
                    :left (+ offset 2 (or x 1))}
              children]])))

(defn.js wrapForm
  "wraps a component as a Form"
  {:added "4.0"}
  ([Component props opts]
   (var #{getter setter x y} (or opts {:getter "value"
                                       :setter "setValue"}))
   (var #{form field height meta} props)
   (var value     (ext-form/listenFieldValue form field meta))
   (var fprops    (j/assign #{x y} props))
   (var tprops    (ui-style/omitLayoutProps
                   (j/assign {getter (base-form/get-field form field)
                              setter (base-form/field-fn  form field)}
                             props
                             {:height (:? (and y height) (- height y) height)})))
   (return [:% -/FormWrapper #{...fprops}
            [:% Component #{...tprops}]])))

(defn.js Spinner
  "Constructs a Spinner"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/Spinner props))))

(defn.js NumberGridBox
  "Constructs a NumberGridBox"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/NumberGridBox props))))

(defn.js Dropdown
  "Constructs a Dropdown"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/Dropdown props))))

(defn.js EnumBox
  "Constructs a EnumBox"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/EnumBox props))))

(defn.js EnumMulti
  "Constructs a EnumMulti"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-group/EnumMulti
                       props
                       {:getter "values"
                        :setter "setValues"}))))

(defn.js Tabs
  "Constructs a Tabs"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-group/Tabs props))))

(defn.js TextBox
  "Constructs a TextBox"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/TextBox
                       props
                       {:getter "content"
                        :setter "setContent"}))))

(defn.js ToggleSwitch
  "Constructs a ToggleSwitch"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/ToggleSwitch
                       props
                       {:getter "selected"
                        :setter "setSelected"}))))

(defn.js ToggleButton
  "Constructs a ToggleButton"
  {:added "4.0"}
  ([props]
   (return (-/wrapForm ui-core/ToggleButton
                       props
                       {:getter "selected"
                        :setter "setSelected"}))))

(defn.js TimePicker
  "Constructs a TimePicker"
  {:added "4.0"}
  ([props]
   (var #{form hourField minuteField} props)
   (var data (ext-form/listenFieldsData form [hourField minuteField]))
   (var [hour setHour]   [(base-form/get-field form hourField)
                          (base-form/field-fn  form hourField)])
   (var [minute setMinute]  [(base-form/get-field form minuteField)
                             (base-form/field-fn  form minuteField)])
   (var tprops     (ui-style/omitLayoutProps
                    (j/assign #{hour setHour minute setMinute}
                              props)))
   (return [:% -/FormWrapper #{...props}
            [:% ui-date/TimePicker #{...tprops}]])))

(defn.js DatePicker
  "Constructs a DatePicker"
  {:added "4.0"}
  ([props]
   (var #{form dayField monthField yearField} props)
   (var data (ext-form/listenFieldsData form [dayField monthField yearField]))
   (var [day setDay]   [(base-form/get-field form dayField)
                        (base-form/field-fn  form dayField)])
   (var [month setMonth]   [(base-form/get-field form monthField)
                            (base-form/field-fn  form monthField)])
   (var [year setYear]   [(base-form/get-field form yearField)
                          (base-form/field-fn  form yearField)])
   (var tprops     (ui-style/omitLayoutProps
                    (j/assign #{day setDay month setMonth year setYear}
                              props)))
   (return [:% -/FormWrapper #{...props}
            [:% ui-date/DatePicker #{...tprops}]])))

(defn.js DurationPicker
  "Constructs a DurationPicker"
  {:added "4.0"}
  ([props]
   (var #{form dayField hourField minuteField} props)
   (var data (ext-form/listenFieldsData form [dayField hourField minuteField]))
   (var [day setDay]   [(base-form/get-field form dayField)
                        (base-form/field-fn  form  dayField)])
   (var [hour setHour]   [(base-form/get-field form hourField)
                          (base-form/field-fn  form  hourField)])
   (var [minute setMinute]  [(base-form/get-field form minuteField)
                             (base-form/field-fn  form  minuteField)])
   (var tprops  (ui-style/omitLayoutProps
                 (j/assign #{day setDay hour setHour minute setMinute}
                           props)))
   (return [:% -/FormWrapper #{...props}
            [:%  ui-date/DurationPicker #{...tprops}]])))

(def.js MODULE
  (do (:# (!:uuid))
      (!:module)))
