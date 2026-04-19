(ns xt.lang.event-form
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.event-common :as event-common]
             [xt.lang.util-validate :as validate]]})

(defspec.xt ValidationFieldResult
  [:xt/record
   ["status" :xt/str]])

(defspec.xt ValidationResult
  [:xt/record
   ["::" :xt/str]
   ["status" :xt/str]
   ["fields" [:xt/dict :xt/str ValidationFieldResult]]])

(defspec.xt FormEvent
  [:xt/record
   ["type" :xt/str]
   ["fields" [:xt/array :xt/str]]
   ["meta" [:xt/maybe xt.lang.event-common/EventListenerMeta]]])

(defspec.xt EventForm
  [:xt/record
   ["::" :xt/str]
   ["listeners" xt.lang.event-common/EventListenerMap]
   ["data" [:xt/dict :xt/str :xt/any]]
   ["validators" [:xt/dict :xt/str :xt/any]]
   ["result" ValidationResult]])

(defspec.xt make-form
  [:fn [[:or [:xt/dict :xt/str :xt/any]
             [:fn [] [:xt/dict :xt/str :xt/any]]]
        [:xt/dict :xt/str :xt/any]]
   EventForm])

(defspec.xt check-event
  [:fn [:xt/any [:xt/array :xt/str]] :xt/bool])

(defspec.xt add-listener
  [:fn [EventForm
        :xt/str
        [:or :xt/str [:xt/array :xt/str]]
        [:fn [FormEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt trigger-all
  [:fn [EventForm :xt/str] [:xt/array :xt/str]])

(defspec.xt trigger-field
  [:fn [EventForm [:or :xt/str [:xt/array :xt/str]] :xt/str] [:xt/array :xt/str]])

(defspec.xt set-field
  [:fn [EventForm :xt/str :xt/any] [:xt/array :xt/str]])

(defspec.xt get-field
  [:fn [EventForm :xt/str] [:xt/maybe :xt/any]])

(defspec.xt toggle-field
  [:fn [EventForm :xt/str] [:xt/array :xt/str]])

(defspec.xt field-fn
  [:fn [EventForm :xt/str] [:fn [:xt/any] [:xt/array :xt/str]]])

(defspec.xt get-result
  [:fn [EventForm] ValidationResult])

(defspec.xt get-field-result
  [:fn [EventForm :xt/str] [:xt/maybe ValidationFieldResult]])

(defspec.xt get-data
  [:fn [EventForm] [:xt/dict :xt/str :xt/any]])

(defspec.xt set-data
  [:fn [EventForm [:xt/dict :xt/str :xt/any]] [:xt/array :xt/str]])

(defspec.xt reset-all-data
  [:fn [EventForm] [:xt/array :xt/str]])

(defspec.xt reset-field-data
  [:fn [EventForm :xt/str] [:xt/array :xt/str]])

(defspec.xt validate-all
  [:fn [EventForm :xt/any :xt/any] :xt/any])

(defspec.xt validate-field
  [:fn [EventForm :xt/str :xt/any :xt/any] :xt/any])

(defspec.xt reset-field-validator
  [:fn [EventForm :xt/str] ValidationResult])

(defspec.xt reset-all-validators
  [:fn [EventForm] ValidationResult])

(defspec.xt reset-all
  [:fn [EventForm] ValidationResult])

(defspec.xt check-field-passed
  [:fn [EventForm :xt/str] :xt/bool])

(defspec.xt check-field-errored
  [:fn [EventForm :xt/str] :xt/bool])

(defspec.xt check-all-passed
  [:fn [EventForm] :xt/bool])

(defspec.xt check-any-errored
  [:fn [EventForm] :xt/bool])

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
  (xt/for:array [field fields]
    (xt/for:array [evfield (. event ["fields"])]
      (when (== evfield field)
        (return true))))
  (return false))

(defn.xt add-listener
  "adds listener to a form"
  {:added "4.0"}
  [form listener-id fields callback meta]
  (:= fields (event-common/arrayify-path fields))
  (return
   (event-common/add-listener
    form listener-id "form" callback
    (xt/x:obj-assign
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
  (var fields (xt/x:obj-keys validators))
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
     :fields (event-common/arrayify-path fields)})))

(defn.xt set-field
  "sets the field"
  {:added "4.0"}
  [form field value]
  (var #{data} form)
  (xt/x:set-key data field value)
  (return (-/trigger-field form field "form.data")))

(defn.xt get-field
  "gets the field"
  {:added "4.0"}
  [form field]
  (var #{data} form)
  (return (xt/x:get-key data field)))

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
  (return (xt/x:get-key form "result")))

(defn.xt get-field-result
  "gets the validation status"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (var #{fields} result)
  (return (xt/x:get-key fields field)))

(defn.xt get-data
  "gets the data"
  {:added "4.0"}
  [form]
  (return (xt/x:get-key form "data")))

(defn.xt set-data
  "sets the data directly"
  {:added "4.0"}
  [form m]
  (var #{data} form)
  (xt/x:obj-assign data m)
  (var fields (xt/x:obj-keys m))
  (return (-/trigger-field form fields "form.data")))

(defn.xt reset-all-data
  "resets all data"
  {:added "4.0"}
  [form]
  (var #{initial} form)
  (var data (initial))
  (xt/x:set-key form "data" data)
  (return (-/trigger-all form "form.data")))

(defn.xt reset-field-data
  "reset field data"
  {:added "4.0"}
  [form field]
  (var #{initial data} form)
  (var value (xt/x:get-key (initial) field))
  (xt/x:set-key data field value)
  (return (-/trigger-field form field "form.data")))

(defn.xt validate-all
  "validates all form"
  {:added "4.0"}
  [form hook-fn complete-fn]
  (var #{validators
         data
         result} form)
  (var hook-status-fn
       (fn [field status]
         (when hook-fn
           (hook-fn field status))))
  (var complete-validation-fn
       (fn [passed res]
         (-/trigger-all form "form.validation")
         (when complete-fn
           (complete-fn res))))
  (return
   (validate/validate-all data
                          validators
                          result
                          hook-status-fn
                          complete-validation-fn)))

(defn.xt validate-field
  "validates form field"
  {:added "4.0"}
  [form field hook-fn complete-fn]
  (var #{validators
         data
         result} form)
  (var complete-field-fn
       (fn [passed status]
         (-/trigger-field form field "form.validation")
         (when complete-fn
           (complete-fn passed status))))
  (return
   (validate/validate-field data
                            field
                            validators
                            result
                            hook-fn
                            complete-field-fn)))

(defn.xt reset-field-validator
  "reset field validators"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (xt/x:set-key result field {:status "pending"})
  (-/trigger-field form field "form.validation")
  (return result))

(defn.xt reset-all-validators
  "reset all field validators"
  {:added "4.0"}
  [form]
  (var #{validators result} form)
  (xt/x:set-key form "result" (validate/create-result validators))
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
  (return (== "ok" (xtd/get-in fields [field "status"]))))

(defn.xt check-field-errored
  "checks that field has passed"
  {:added "4.0"}
  [form field]
  (var #{result} form)
  (var #{fields} result)
  (return (== "errored" (xtd/get-in fields [field "status"]))))

(defn.xt check-all-passed
  "checks that all fields have passed"
  {:added "4.0"}
  [form]
  (var #{result} form)
  (var #{fields} result)
  (xt/for:object [[_ v] fields]
    (when (not= "ok" (xt/x:get-key v "status"))
      (return false)))
  (return true))

(defn.xt check-any-errored
  "checks that any fields have errored"
  {:added "4.0"}
  [form]
  (var #{result} form)
  (var #{fields} result)
  (xt/for:object [[_ v] fields]
    (when (== "errored" (xt/x:get-key v "status"))
      (return true)))
  (return false))
