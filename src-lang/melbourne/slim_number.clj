(ns melbourne.slim-number
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n :include [:fn]]
             [xt.event.base-form :as event-form]
             [melbourne.slim-common :as slim-common]
             [melbourne.ui-spinner :as ui-spinner]
             [melbourne.ui-spinner-basic :as ui-spinner-basic]
             [melbourne.ui-slider :as ui-slider]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js FormSpinner
  "creates a Spinner"
  {:added "0.1"}
  [#{[design
      mini
      variant
      form
      meta
      label
      labelHide labelNone
      styleLabel
      labelWidth
      field
      fieldProps
      minWidth
      max
      min
      step
      decimal]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "spinner"
                                                     :fn/type   "field"}
                                                    meta)))
  (return 
   [:% slim-common/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% n/Row
     {:style {:marginTop 5}}
     [:% ui-spinner/SpinnerControls
      #{[design
         variant
         :value value
         :setValue (event-form/field-fn form field)
         max
         min
         step
         :style {:paddingHorizontal 5}]}
      [:% ui-spinner/Spinner
      #{[design
         variant
         :style {:marginHorizontal 5
                 #_#_:padding 3}
         :value value
         :setValue (event-form/field-fn form field)
         max
         min
         step
         decimal
         (:.. fieldProps)]}]]]]))

(defn.js FormSpinnerBasic
  "creates a SpinnerBasic"
  {:added "0.1"}
  [#{[design
      mini
      variant
      form
      meta
      label
      labelHide labelNone
      styleLabel
      labelWidth
      field
      fieldProps
      minWidth
      max
      min
      step
      decimal]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "spinner"
                                                     :fn/type   "field"}
                                                    meta)))
  (return 
   [:% slim-common/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% n/Row
     {:style {:marginTop 5}}
     [:% ui-spinner-basic/SpinnerBasicControls
      #{[design
         variant
         :value value
         :setValue (event-form/field-fn form field)
         max
         min
         step
         :style {:paddingHorizontal 5}]}
      [:% ui-spinner-basic/SpinnerBasic
      #{[design
         variant
         :style {:marginHorizontal 5
                 #_#_:padding 3}
         :value value
         :setValue (event-form/field-fn form field)
         max
         min
         step
         decimal
         (:.. fieldProps)]}]]]]))

(defn.js FormSlider
  "creates a Slider"
  {:added "0.1"}
  [#{[design
      mini
      variant
      form
      meta
      styleLabel
      
      label
      labelHide labelNone
      
      field
      fieldProps
      minWidth
      max
      min
      step
      decimal]}]
  (var #{value result} (ext-form/listenField form field
                                          (Object.assign {:slim/type "slider"
                                                     :fn/type   "field"}
                                                    meta)))
  (return 
   [:% slim-common/FormEnclosed
    #{design
      mini
      {:variant (data/get-in design ["variant" "label"])}
      styleLabel
      label
      labelHide labelNone
      minWidth}
    [:% ui-slider/Slider
     #{[design variant
        :style {:marginTop 10
                :marginBottom 5}
        :value value
        :setValue (event-form/field-fn form field)
        max
        min
        step
        decimal
        (:.. fieldProps)]}]]))

(def.js MODULE (!:module))



