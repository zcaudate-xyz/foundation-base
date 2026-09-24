(ns melbourne.slim-time
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
             [js.react :as r]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n]
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
             [melbourne.slim-common :as slim-common]
             [melbourne.ui-input :as ui-input]
             [melbourne.ui-datetime :as ui-datetime]
             [melbourne.addon-validation :as addon-validation]]
   :export [MODULE]})

(defn.js FormTime
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
                                             (Object.assign {:slim/type "time"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% slim-common/FormEnclosed
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

(defn.js FormDate
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
                                             (Object.assign {:slim/type "time"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% slim-common/FormEnclosed
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

(defn.js FormDatetime
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
                                             (Object.assign {:slim/type "time"
                                                        :fn/type   "field"}
                                                       meta)))
  (return
   [:% slim-common/FormEnclosed
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

(def.js MODULE (!:module))
