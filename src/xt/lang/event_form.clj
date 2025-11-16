(ns xt.lang.event-form
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]
             [xt.lang.util-validate :as validate]]})

(defn.xt make-form
  "creates a form"
  {:added "4.0"}
  [initial validators]
  (var result (validate/create-result validators))
  (return
   (event-common/make-container
    initial "event.form"
    {:result result
     :validators validators})))

(defn.xt check-event
  "checks that event needs to be processed"
  {:added "4.0"}
  [event fields]
  (k/for:array [field fields]
    (k/for:array [evfield (. event ["fields"])]
      (when (== evfield field)
        (return true))))
  (return false))

(defn.xt add-listener
  "adds listener to a form"
  {:added "4.0"}
  [form listener-id fields callback meta]
  (:= fields (k/arrayify fields))
  (return
   (event-common/add-listener
    form listener-id "form" callback
    (k/obj-assign
     {:form/fields fields}
     meta)
    (fn [event]
      (return (-/check-event event fields))))))

(def.xt ^{:arglists '([form listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists '([form])}
  list-listeners
  event-common/list-listeners)

(defn.xt trigger-all
  "triggers all fields"
  {:added "4.0"}
  [form event-type]
  (var #{validators} form)
  (var fields (k/obj-keys validators))
  (return
   (event-common/trigger-listeners
    form
    {:type   event-type
     :fields fields})))

(defn.xt trigger-field
  "triggers the callback"
  {:added "4.0"}
  [form fields event-type]
  (return
   (event-common/trigger-listeners
    form
    {:type   event-type
     :fields (k/arrayify fields)})))

(defn.xt set-field
  "sets the field"
  {:added "4.0"}
  [form field value]
  (var #{data} form)
  (k/set-key data field value)
  (return (-/trigger-field form field "form.data")))

(defn.xt get-field
  "gets the field"
  {:added "4.0"}
  [form field]
  (var #{data} form)
  (return (k/get-key data field)))

(defn.xt toggle-field
  "toggles the field"
  {:added "4.0"}
  [form field]
  (return
   (-/set-field
    form field
    (not (-/get-field form field)))))

(defn.xt field-fn
  "constructs a field function"
  {:added "4.0"}
  [form field]
  (return
   (fn [value]
     (return (-/set-field form field value)))))

(defn.xt get-result
  "gets the validation result"
  {:added "4.0"}
  [form]
  (return (k/get-key form "result")))

(defn.xt get-field-result
  "gets the validation status"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (var #{fields} result)
  (return (k/get-key fields field)))

(defn.xt get-data
  "gets the data"
  {:added "4.0"}
  [form]
  (return (k/get-key form "data")))

(defn.xt set-data
  "sets the data directly"
  {:added "4.0"}
  [form m]
  (var #{data} form)
  (k/obj-assign data m)
  (var fields (k/obj-keys m))
  (return (-/trigger-field form fields "form.data")))

(defn.xt reset-all-data
  "resets all data"
  {:added "4.0"}
  [form]
  (var #{initial} form)
  (var data (initial))
  (k/set-key form "data" data)
  (return (-/trigger-all form "form.data")))

(defn.xt reset-field-data
  "reset field data"
  {:added "4.0"}
  [form field]
  (var #{initial data} form)
  (var value (k/get-key (initial) field))
  (k/set-key data field value)
  (return (-/trigger-field form field "form.data")))

(defn.xt validate-all
  "validates all form"
  {:added "4.0"}
  [form hook-fn complete-fn]
  (var #{validators
         data
         result} form)
  (return
   (validate/validate-all data
                          validators
                          result
                          (fn [field status]
                            (when hook-fn
                              (hook-fn field status)))
                          (fn [res]
                            (-/trigger-all form "form.validation")
                            (when complete-fn
                              (complete-fn res))))))

(defn.xt validate-field
  "validates form field"
  {:added "4.0"}
  [form field hook-fn complete-fn]
  (var #{validators
         data
         result} form)
  (return
   (validate/validate-field data
                            field
                            validators
                            result
                            hook-fn
                            (fn [passed status]
                              (-/trigger-field form field "form.validation")
                              (when complete-fn
                                (complete-fn passed status))))))

(defn.xt reset-field-validator
  "reset field validators"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (k/set-key result field {:status "pending"})
  (-/trigger-field form field "form.validation")
  (return result))

(defn.xt reset-all-validators
  "reset all field validators"
  {:added "4.0"}
  [form]
  (var #{validators result} form)
  (k/set-key form "result" (validate/create-result validators))
  (-/trigger-all form "form.validation")
  (return result))

(defn.xt reset-all
  "resets data and validator result"
  {:added "4.0"}
  [form]
  (-/reset-all-data form)
  (-/reset-all-validators form))

(defn.xt check-field-passed
  "checks that field has passed"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (var #{fields} result)
  (return (== "ok" (k/get-in fields [field "status"]))))

(defn.xt check-field-errored
  "checks that field has passed"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (var #{fields} result)
  (return (== "errored" (k/get-in fields [field "status"]))))

(defn.xt check-all-passed
  "checks that all fields have passed"
  {:added "4.0"}
  [form]
  (var #{result} form)
  (var #{fields} result)
  (k/for:object [[_ v] fields]
    (when (not= "ok" (k/get-key v "status"))
      (return false)))
  (return true))

(defn.xt check-any-errored
  "checks that any fields have errored"
  {:added "4.0"}
  [form]
  (var #{result} form)
  (var #{fields} result)
  (k/for:object [[_ v] fields]
    (when (== "errored" (k/get-key v "status"))
      (return true)))
  (return false))

