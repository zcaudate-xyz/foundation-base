(ns js.react.ext-form
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-form :as event-form]
             [js.react :as r :include [:fn]]
             [js.core :as j]]})

;;
;; No Validation
;;

(defn.js makeFree
  "makes a free form (no validation)"
  {:added "4.0"}
  [initial keys]
  (var initialRef (r/useFollowRef initial))
  (return (r/const (event-form/make-form
                    (fn:> (k/obj-pick ((r/curr initialRef))
                                      keys))
                    (k/arr-juxt keys
                                k/identity
                                (fn:> []))))))

(defn.js makeFreeEdit
  "edit helper for forms"
  {:added "4.0"}
  [dataFn dataKeys dataArgs]
  (var getData (fn []
                 (var data (dataFn (:.. dataArgs)))
                 (var out {})
                 (k/for:array [k dataKeys]
                   (x:set-key out k (x:get-key data k)))
                 (return out)))
  (var form (-/makeFree getData dataKeys))
  (var isChanged (fn:> (k/not-empty?
                        (k/obj-diff (event-form/get-data form)
                                    (getData)))))
  (return #{form isChanged}))

;;
;; Validated
;;

(defn.js checkPrint
  "checks that form should be printed"
  {:added "4.0"}
  [meta]
  (var print (. meta ["debug/print"]))
  (cond (== true print)
        (return true)

        (k/obj? print)
        (do (k/for:object [[key v] print]
              (var term (. meta [key]))
              (when (k/is-empty? (k/arr-intersection (j/arrayify v)
                                                     (j/arrayify term)))
                (return false)))
            (return true)))
  (return false))

(defn.js makeForm
  "makes a form"
  {:added "4.0"}
  [initial
   validators]
  (var initialRef (r/useFollowRef initial))
  (return (r/const (event-form/make-form (fn:> ((r/curr initialRef)))
                                        validators))))

(defn.js useListener
  "generic listener"
  {:added "4.0"}
  [form fields #{getData
                 getResult
                 getStatus
                 getPassed
                 dataField}
   meta]
  (:= dataField (or dataField "data"))
  (var [data setData] (r/local getData))
  (var [result setResult] (r/local getResult))
  (var passedRef (r/ref))
  (var statusRef (r/ref))
  (r/init []
    (var listener-id (j/randomId 4))
    (event-form/add-listener
     form
     listener-id
     fields
     (fn [m]
       (var #{type meta} m)
       (when (-/checkPrint meta)
         (console.log "PRINTFORM" (getData)
                      meta))
       
       (cond (and (== type "form.data")
                  getData)
             (setData getData)
             
             (== type "form.validation")
             (when getResult
               (do (var nresult (getResult))
                   (cond (. meta ["validation/all"])
                         (setResult nresult)
                         
                         (. meta ["validation/passed"])
                         (do (var npassed (getPassed nresult))
                             (when (not= npassed (r/curr passedRef))
                               (setResult nresult)
                               (r/curr:set passedRef npassed)))
                         
                         :else
                         (do (var nstatus (getStatus nresult))
                             (when (not (k/eq-nested nstatus (r/curr statusRef)))
                               (setResult nresult)
                               (r/curr:set statusRef nstatus))))))))
     meta)
    (return
     (fn []
       (event-form/remove-listener form listener-id))))
  (return {dataField data
           :result result}))

(defn.js getFieldPassed
  "gets the passed status for field"
  {:added "4.0"}
  [field]
  (return (== "ok" (. field ["status"]))))

(defn.js getFieldStatus
  "gets the id and status for a field"
  {:added "4.0"}
  [field]
  (var #{status id} field)
  (return #{status id}))

(defn.js listenFields
  "listens to multiple fields"
  {:added "4.0"}
  [form fields meta]
  (var getData   (fn:> (k/arr-juxt fields
                                   k/identity
                                   (fn:> [field] (event-form/get-field form field)))))
  (var getResult (fn:> (k/arr-juxt fields
                                   k/identity
                                   (fn:> [field] (event-form/get-field-result form field)))))
  (var getStatus (fn [result]
                   (return (k/obj-map result -/getFieldStatus))))
  (var getPassed (fn [result]
                   (return (k/arr-every (k/obj-vals result) -/getFieldPassed))))
  (return (-/useListener form fields
                         #{getData
                           getResult
                           getStatus
                           getPassed
                           {:dataField "data"}}
                         (j/assign {:fn/fields fields}
                                   meta))))

(defn.js listenFieldsData
  "uses data from multiple fields in form"
  {:added "4.0"}
  [form fields meta]
  (var getData   (fn:> (k/arr-juxt fields
                                   k/identity
                                   (fn:> [field] (event-form/get-field form field)))))
  (return (-/useListener form fields
                         #{getData
                           {:dataField "data"}}
                         (j/assign {:fn/fields fields}
                                   meta))))

(defn.js listenField
  "gets value and result of a form field"
  {:added "4.0"}
  [form field meta]
  (var getValue  (fn:> (event-form/get-field form field)))
  (var getResult (fn []
                   (return (j/assign {} (event-form/get-field-result form field)))))
  (return (-/useListener form [field]
                         #{getResult
                           {:getData getValue
                            :getStatus -/getFieldStatus
                            :getPassed -/getFieldPassed
                            :dataField "value"}}
                         (j/assign {:fn/fields [field]}
                                   meta))))

(defn.js listenFieldValue
  "gets only a field value"
  {:added "4.0"}
  [form field meta]
  (var getValue  (fn:> (event-form/get-field form field)))
  (var #{value} (-/useListener form [field]
                               {:getData getValue
                                :dataField "value"}
                               (j/assign {:fn/fields [field]}
                                         meta)))
  (return value))

(defn.js listenFieldResult
  "gets result of a form field"
  {:added "4.0"}
  [form field meta]
  (var getResult (fn:> (j/assign {} (event-form/get-field-result form field))))
  (var #{result} (-/useListener form [field]
                          #{getResult
                            {:getStatus -/getFieldStatus
                             :getPassed -/getFieldPassed
                             :dataField "value"}}
                          (j/assign {:fn/fields [field]}
                                    meta)))
  (return result))

(defn.js listenForm
  "gets all form changes"
  {:added "4.0"}
  [form meta]
  (var #{validators} form)
  (var fields (j/keys validators))
  (var getData   (fn:> (j/assign {} (event-form/get-data form))))
  (var getResult (fn:> (j/assign {} (event-form/get-result form))))
  (var getStatus (fn [result]
                   (var #{fields} result)
                   (return (k/obj-map fields -/getFieldStatus))))
  (var getPassed (fn:> [result] (event-form/check-all-passed {:result result})))
  (return (-/useListener form fields
                         #{getData
                           getResult
                           getStatus
                           getPassed
                           {:dataField "data"}}
                         (j/assign {:fn/fields fields}
                                   meta))))

(defn.js listenFormData
  "gets all form data"
  {:added "4.0"}
  [form meta]
  (var #{validators} form)
  (var fields (j/keys validators))
  (var getData   (fn:> (j/assign {} (event-form/get-data form))))
  (return (. (-/useListener form fields
                            #{getData
                              {:dataField "data"}}
                            (j/assign {:fn/fields fields}
                                      meta))
             ["data"])))

(defn.js listenFormResult
  "gets form validation result"
  {:added "4.0"}
  [form meta]
  (var #{validators} form)
  (var fields (j/keys validators))
  (var getResult (fn:> (j/assign {} (event-form/get-result form))))
  (var getStatus (fn [result]
                   (var #{fields} result)
                   (return (k/obj-map fields -/getFieldStatus))))
  (var getPassed (fn:> [result] (event-form/check-all-passed {:result result})))
  (return (. (-/useListener form fields
                            #{getResult
                              getStatus
                              getPassed
                              {:dataField "data"}}
                            (j/assign {:fn/fields fields}
                                      meta))
             ["result"])))

(defn.js useSubmitField
  "gets submit field"
  {:added "4.0"}
  [#{[form
      field
      setResult
      explicit
      keep
      meta
      (:= onCheck (fn:> true))
      (:= isMounted (fn:> true))]}]
  (var fields (j/arrayify field))
  (var [clearing setClearing] (r/local (fn:> false)))
  (var #{data} (-/listenFields form fields meta))
  (var validateFields
       (fn []
         (k/for:array [field fields]
           (var fdata (event-form/get-field form field))
           (when (or explicit
                     (k/is-boolean? fdata)
                     (k/is-number? fdata)
                     (k/not-empty? fdata))
             (event-form/validate-field form field)))))
  (var onActionReset
       (fn []
         (setResult nil)
         (when (not keep)
           (k/for:array [field fields]
             (event-form/reset-field-data form field)))
         (j/future-delayed [100]
           (k/for:array [field fields]
             (event-form/reset-field-validator form field))
           (when (isMounted)
             (setClearing true)))))
  (var onActionCheck
       (fn:>
        (and (onCheck)
             (j/every fields (fn:> [field] (event-form/check-field-passed form field))))))
  (r/init []
    (validateFields))
  (r/watch [clearing]
    (when clearing
      (j/delayed [100]
        (when (isMounted)
          (validateFields)))
      (setClearing false)))
  (return #{onActionReset
            onActionCheck}))

(defn.js useSubmitForm
  "gets submit form"
  {:added "4.0"}
  [#{[form
      setResult
      explicit
      keep
      meta
      (:= onCheck (fn:> true))
      (:= isMounted (fn:> true))]}]
  (var [clearing setClearing] (r/local (fn:> false)))
  (var #{data} (-/listenForm form meta))
  (var onActionReset
       (fn []
         (setResult nil)
         (when (not keep)
           (event-form/reset-all-data form))
         (j/future-delayed [100]
           (event-form/reset-all-validators form)
           (when (isMounted)
             (setClearing true)))))
  (var onActionCheck (fn []
                       (return (and (onCheck)
                                    (event-form/check-all-passed form)))))
  
  (var validateFilled
       (fn []
         (k/for:array [vkey (k/obj-keys (. form validators))]
           (var v (k/get-key data vkey))
           (when (and (not (k/is-boolean? v))
                      (not (k/is-number? v))
                      (k/not-empty? v))
             (event-form/validate-all form)
             (return)))))
  (r/init []
    (if explicit
      (event-form/validate-all form)
      (validateFilled)))
  (r/watch [clearing]
    (when clearing
      (j/delayed [100]
        (if explicit
          (event-form/validate-all form)
          (validateFilled)))
      (setClearing false)))
  (return #{onActionReset
            onActionCheck}))

