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

^{:refer js.react.ext-form/makeFree :added "4.0"}
(fact "creates a free form without validators"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeFree (fn [] (return {"first" "Ada" "extra" 1})) ["first"]))
     (xt/x:set-key (. props ["state"]) "form" form)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var form (. (. props ["state"]) ["form"]))
     (var result (event-form/get-data form))
     (return result)))
  => {"first" "Ada"})

^{:refer js.react.ext-form/makeFreeEdit :added "4.0"}
(fact "returns an editable free form state"
  (helper-source/test
   (fn [props]
     (var record {"name" "Ada"})
     (var #{form isChanged}
          (ext-form/makeFreeEdit
           (fn [value] (return {"name" "Ada" "extra" 1}))
           ["name"]
           [record]))
     (xt/x:set-key (. props ["state"]) "form" form)
     (xt/x:set-key (. props ["state"]) "isChanged" isChanged)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var state (. props ["state"]))
     (var form (. state ["form"]))
     (event-form/set-field form "name" "Grace")
     (var result {"data" (event-form/get-data form)
                  "changed" ((. state ["isChanged"]))})
     (return result)))
  => {"data" {"name" "Grace"} "changed" false})

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
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "form" form)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var form (. (. props ["state"]) ["form"]))
     (var result {"type" (. form ["::"])
                  "data" (event-form/get-data form)
                  "status" (. (event-form/get-field-result form "first") ["status"])})
     (return result)))
  => {"type" "event.form" "data" {"first" "Ada"} "status" "pending"})

^{:refer js.react.ext-form/useListener :added "4.0"}
(fact "updates all listener variants when form data changes"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (var fields (ext-form/listenFields form ["first"] nil))
     (var fieldsData (ext-form/listenFieldsData form ["first"] nil))
     (var field (ext-form/listenField form "first" nil))
     (var fieldValue (ext-form/listenFieldValue form "first" nil))
     (var fieldResult (ext-form/listenFieldResult form "first" nil))
     (var formState (ext-form/listenForm form nil))
     (var formData (ext-form/listenFormData form nil))
     (var formResult (ext-form/listenFormResult form nil))
     (xt/x:set-key (. props ["state"]) "form" form)
     (return
      (r/createElement "span" nil
                       (JSON.stringify
                        {"fields" (xt/x:get-key (. fields ["data"]) "first")
                         "fieldsData" (xt/x:get-key (. fieldsData ["data"]) "first")
                         "field" (. field ["value"])
                         "fieldValue" fieldValue
                         "fieldResult" (. fieldResult ["status"])
                         "form" (xt/x:get-key (. formState ["data"]) "first")
                         "formData" (xt/x:get-key formData "first")
                         "formResult" (. formResult ["status"])}))))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var form (. (. props ["state"]) ["form"]))
     (return
      (. (Promise.resolve
          (r/act (fn [] (event-form/set-field form "first" "Grace"))))
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var result (JSON.parse document.body.textContent))
                     (return result))))))))))
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
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (var result (ext-form/listenFields form ["first"] nil))
     (xt/x:set-key (. props ["state"]) "result" result)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (. (. props ["state"]) ["result"]))
     (var output {"data" (xt/x:get-key (. result ["data"]) "first")
                  "status" (. (. result ["result"] ["first"]) ["status"])})
     (return output)))
  => {"data" "Ada" "status" "pending"})

^{:refer js.react.ext-form/listenFieldsData :added "4.0"}
(fact "listens to multiple field data"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (var result (ext-form/listenFieldsData form ["first"] nil))
     (xt/x:set-key (. props ["state"]) "result" result)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (xt/x:get-key (. (. props ["state"]) ["result"] ["data"]) "first"))
     (return result)))
  => "Ada")

^{:refer js.react.ext-form/listenField :added "4.0"}
(fact "listens to one field value and result"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (var result (ext-form/listenField form "first" nil))
     (xt/x:set-key (. props ["state"]) "result" result)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (. (. props ["state"]) ["result"]))
     (var output {"value" (. result ["value"])
                  "status" (. (. result ["result"]) ["status"])})
     (return output)))
  => {"value" "Ada" "status" "pending"})

^{:refer js.react.ext-form/listenFieldValue :added "4.0"}
(fact "listens to only one field value"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "value" (ext-form/listenFieldValue form "first" nil))
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (. (. props ["state"]) ["value"]))
     (return result)))
  => "Ada")

^{:refer js.react.ext-form/listenFieldResult :added "4.0"}
(fact "listens to one field result"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "result" (ext-form/listenFieldResult form "first" nil))
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (. (. props ["state"]) ["result"] ["status"]))
     (return result)))
  => "pending")

^{:refer js.react.ext-form/listenForm :added "4.0"}
(fact "listens to the complete form"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "result" (ext-form/listenForm form nil))
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (xt/x:get-key (. (. props ["state"]) ["result"] ["data"]) "first"))
     (return result)))
  => "Ada")

^{:refer js.react.ext-form/listenFormData :added "4.0"}
(fact "listens to complete form data"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "data" (ext-form/listenFormData form nil))
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (xt/x:get-key (. (. props ["state"]) ["data"]) "first"))
     (return result)))
  => "Ada")

^{:refer js.react.ext-form/listenFormResult :added "4.0"}
(fact "returns the form validation result"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (xt/x:set-key (. props ["state"]) "form" form)
     (xt/x:set-key (. props ["state"]) "result" (ext-form/listenFormResult form nil))
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var result (. (. props ["state"]) ["result"]))
     (return result)))
  => {"::" "validation.result"
      "status" "pending"
      "fields" {"first" {"status" "pending"}}})

^{:refer js.react.ext-form/useSubmitField :added "4.0"}
(fact "returns submit actions for one field"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" ""}))
                                  {"first" [["required" {"message" "Required"
                                                          "check" (fn:> [v rec]
                                                                     (and (xt/x:not-nil? v)
                                                                          (< 0 (xt/x:len v))))}]]}))
     (var actions (ext-form/useSubmitField {"form" form
                                             "field" "first"
                                             "explicit" true}))
     (xt/x:set-key (. props ["state"]) "form" form)
     (xt/x:set-key (. props ["state"]) "actions" actions)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var state (. props ["state"]))
     (return
      (. (event-form/validate-field (. state ["form"]) "first" nil nil)
         (then (fn [_]
                 (var result {"passed" ((. state ["actions"] ["onActionCheck"]))
                              "hasReset" (xt/x:is-function? (. state ["actions"] ["onActionReset"]))})
                 (return result)))))))
  => {"passed" false "hasReset" true})

^{:refer js.react.ext-form/useSubmitForm :added "4.0"}
(fact "returns submit actions for the form"
  (helper-source/test
   (fn [props]
     (var form (ext-form/makeForm (fn [] (return {"first" "Ada"}))
                                  {"first" [["required" {"check" (fn:> [v rec] (return true))}]]}))
     (var actions (ext-form/useSubmitForm {"form" form
                                            "explicit" true}))
     (xt/x:set-key (. props ["state"]) "form" form)
     (xt/x:set-key (. props ["state"]) "actions" actions)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var state (. props ["state"]))
     (return
      (. (event-form/validate-all (. state ["form"]) nil nil)
         (then (fn [_]
                 (var result {"passed" ((. state ["actions"] ["onActionCheck"]))
                              "hasReset" (xt/x:is-function? (. state ["actions"] ["onActionReset"]))})
                 (return result)))))))
  => {"passed" true "hasReset" true})
