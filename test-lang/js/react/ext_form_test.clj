(ns js.react.ext-form-test
    (:require [lang.core :as l]
              [js.react.helper-jsdom :as helper-source])
    (:use code.test))

(l/script- :js
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [js.react :as r]
                      [js.react.helper-jsdom :as helper]
                      [js.react.ext-form :as ext-form]
                      [xt.event.base-form :as event-form]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(def.js Validators
        {"first" [["required" {"message" "Required"
                               "check" (fn:> [v rec]
                                             (and (xt/x:not-nil? v)
                                                  (< 0 (xt/x:len v))))}]]
         "last" [["required" {"message" "Required"
                              "check" (fn:> [v rec]
                                            (and (xt/x:not-nil? v)
                                                 (< 0 (xt/x:len v))))}]]})

(defn.js await-dom
         [f]
         (return
          (new Promise
               (fn [resolve]
                   (setTimeout (fn [] (resolve (f))) 0)))))

^{:refer js.react.ext-form/makeFree :added "4.0"}
(fact "creates a free form without validators"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeFree {"first" "Ada" "extra" 1} ["first"]))
            (xt/x:set-key controls "form" form)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (event-form/get-data (. controls ["form"]))))))))
  => {"first" "Ada"})

^{:refer js.react.ext-form/makeFreeEdit :added "4.0"}
(fact "tracks changes to an editable free form"
  (helper-source/wait-on
   {}
   (var controls {})
   (var record {"profile" {"name" "Ada"}})
   (var Component
        (fn []
            (var [form isChanged]
                 (ext-form/makeFreeEdit
                  (fn [value] (return (. value ["profile"])))
                  ["name"]
                  [record]))
            (xt/x:set-key controls "form" form)
            (xt/x:set-key controls "isChanged" isChanged)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var form (. controls ["form"]))
                 (event-form/set-field form "name" "Grace")
                 (return {"data" (event-form/get-data form)
                          "changed" ((. controls ["isChanged"]))}))))))
  => {"data" {"name" "Grace"} "changed" true})

^{:refer js.react.ext-form/checkPrint :added "4.0"}
(fact "checks debug print metadata"
  (!.js
   (return [(ext-form/checkPrint {"debug/print" true})
            (ext-form/checkPrint {"debug/print" {"phase" ["run"]}
                                  "phase" "run"})
            (ext-form/checkPrint {"debug/print" {"phase" ["run"]}
                                  "phase" "skip"})
            (ext-form/checkPrint {})]))
  => [true true false false])

^{:refer js.react.ext-form/makeForm :added "4.0"}
(fact "creates a validated form"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "form" form)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var form (. controls ["form"]))
                 (return {"type" (. form ["::"])
                          "data" (event-form/get-data form)
                          "status" (. (event-form/get-field-result form "first") ["status"])}))))))
  => {"type" "event.form" "data" {"first" "Ada"} "status" "pending"})

^{:refer js.react.ext-form/useListener :added "4.0"}
(fact "updates all listener variants when form data changes"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (var fields (ext-form/listenFields form ["first"] nil))
            (var fieldsData (ext-form/listenFieldsData form ["first"] nil))
            (var field (ext-form/listenField form "first" nil))
            (var fieldValue (ext-form/listenFieldValue form "first" nil))
            (var fieldResult (ext-form/listenFieldResult form "first" nil))
            (var formState (ext-form/listenForm form nil))
            (var formData (ext-form/listenFormData form nil))
            (var formResult (ext-form/listenFormResult form nil))
            (xt/x:set-key controls "form" form)
            (xt/x:set-key controls "fields" fields)
            (xt/x:set-key controls "fieldsData" fieldsData)
            (xt/x:set-key controls "field" field)
            (xt/x:set-key controls "fieldValue" fieldValue)
            (xt/x:set-key controls "fieldResult" fieldResult)
            (xt/x:set-key controls "formState" formState)
            (xt/x:set-key controls "formData" formData)
            (xt/x:set-key controls "formResult" formResult)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var form (. controls ["form"]))
                 (event-form/set-field form "first" "Grace")
                 (return (-/await-dom
                          (fn []
                              (return {"fields" (xt/x:get-key (. controls ["fields"] ["data"]) "first")
                                       "fieldsData" (xt/x:get-key (. controls ["fieldsData"] ["data"]) "first")
                                       "field" (. (. controls ["field"]) ["value"])
                                       "fieldValue" (. controls ["fieldValue"])
                                       "fieldResult" (. (. controls ["fieldResult"]) ["status"])
                                       "form" (xt/x:get-key (. controls ["formState"] ["data"]) "first")
                                       "formData" (xt/x:get-key (. controls ["formData"]) "first")
                                       "formResult" (. (. controls ["formResult"]) ["status"])})))))))))
  => {"fields" "Grace"
      "fieldsData" "Grace"
      "field" "Grace"
      "fieldValue" "Grace"
      "fieldResult" "pending"
      "form" "Grace"
      "formData" "Grace"
      "formResult" "pending"})

^{:refer js.react.ext-form/getFieldPassed :added "4.0"}
(fact "checks whether a field passed"
  (!.js
   (return [(ext-form/getFieldPassed {"status" "ok"})
            (ext-form/getFieldPassed {"status" "pending"})]))
  => [true false])

^{:refer js.react.ext-form/getFieldStatus :added "4.0"}
(fact "selects field id and status"
  (!.js
   (return (ext-form/getFieldStatus {"id" "first"
                                     "status" "ok"
                                     "extra" true})))
  => {"id" "first" "status" "ok"})

^{:refer js.react.ext-form/listenFields :added "4.0"}
(fact "listens to multiple field values and results"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (var result (ext-form/listenFields form ["first"] nil))
            (xt/x:set-key controls "result" result)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return {"data" (xt/x:get-key (. controls ["result"] ["data"]) "first")
                          "status" (. (. controls ["result"] ["result"] ["first"]) ["status"])}))))))
  => {"data" "Ada" "status" "pending"})

^{:refer js.react.ext-form/listenFieldsData :added "4.0"}
(fact "listens to multiple field data"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (var result (ext-form/listenFieldsData form ["first"] nil))
            (xt/x:set-key controls "result" result)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (xt/x:get-key (. controls ["result"] ["data"]) "first")))))))
  => "Ada")

^{:refer js.react.ext-form/listenField :added "4.0"}
(fact "listens to one field value and result"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (var result (ext-form/listenField form "first" nil))
            (xt/x:set-key controls "result" result)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return {"value" (. controls ["result"] ["value"])
                          "status" (. (. controls ["result"] ["result"]) ["status"])}))))))
  => {"value" "Ada" "status" "pending"})

^{:refer js.react.ext-form/listenFieldValue :added "4.0"}
(fact "listens to only one field value"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "value" (ext-form/listenFieldValue form "first" nil))
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_] (return (. controls ["value"])))))))
  => "Ada")

^{:refer js.react.ext-form/listenFieldResult :added "4.0"}
(fact "listens to one field result"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "result" (ext-form/listenFieldResult form "first" nil))
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (. (. controls ["result"]) ["status"])))))))
  => "pending")

^{:refer js.react.ext-form/listenForm :added "4.0"}
(fact "listens to the complete form"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "result" (ext-form/listenForm form nil))
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (xt/x:get-key (. controls ["result"] ["data"]) "first")))))))
  => "Ada")

^{:refer js.react.ext-form/listenFormData :added "4.0"}
(fact "listens to complete form data"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "data" (ext-form/listenFormData form nil))
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (xt/x:get-key (. controls ["data"]) "first")))))))
  => "Ada")

^{:refer js.react.ext-form/listenFormResult :added "4.0"}
(fact "listens to complete form validation result"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (xt/x:set-key controls "result" (ext-form/listenFormResult form nil))
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (return (. (. controls ["result"] ["first"]) ["status"])))))))
  => "pending")

^{:refer js.react.ext-form/useSubmitField :added "4.0"}
(fact "returns submit actions for one field"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" ""} Validators))
            (var actions (ext-form/useSubmitField {"form" form
                                                   "field" "first"
                                                   "explicit" true}))
            (xt/x:set-key controls "form" form)
            (xt/x:set-key controls "actions" actions)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var form (. controls ["form"]))
                 (return (. (event-form/validate-field form "first" nil nil)
                            (then (fn [_]
                                      (return {"passed" ((. controls ["actions"] ["onActionCheck"]))
                                               "hasReset" (xt/x:is-function? (. controls ["actions"] ["onActionReset"]))}))))))))))
  => {"passed" false "hasReset" true})

^{:refer js.react.ext-form/useSubmitForm :added "4.0"}
(fact "returns submit actions for the form"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var form (ext-form/makeForm {"first" "Ada"} Validators))
            (var actions (ext-form/useSubmitForm {"form" form
                                                  "explicit" true}))
            (xt/x:set-key controls "form" form)
            (xt/x:set-key controls "actions" actions)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var form (. controls ["form"]))
                 (return (. (event-form/validate-all form nil nil)
                            (then (fn [_]
                                      (return {"passed" ((. controls ["actions"] ["onActionCheck"]))
                                               "hasReset" (xt/x:is-function? (. controls ["actions"] ["onActionReset"]))}))))))))))
  => {"passed" true "hasReset" true})
