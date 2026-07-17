(ns xt.ui.state.form
  "Serializable form drafts, validation and submission state."
  (:refer-clojure :exclude [reset!])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt clone [value]
  (return (xtd/clone-nested (or value {}))))

(defn.xt create [values validators]
  (var initial (-/clone values))
  (return {"initial" initial
           "draft" (-/clone initial)
           "validators" (or validators {})
           "errors" {}
           "touched" {}
           "dirty" false
           "valid" true
           "pending" false}))

(defn.xt validate-value [validators value draft]
  (var message nil)
  (xt/for:array [validator (or validators [])]
    (when (xt/x:nil? message)
      (:= message (validator value draft))))
  (return message))

(defn.xt validate! [form]
  (var errors {})
  (var draft (xt/x:get-key form "draft"))
  (xt/for:object [[field validators] (xt/x:get-key form "validators")]
    (var message (-/validate-value validators (xt/x:get-key draft field) draft))
    (when (xt/x:not-nil? message)
      (xt/x:set-key errors field message)))
  (xt/x:set-key form "errors" errors)
  (xt/x:set-key form "valid" (== 0 (xt/x:len (xt/x:obj-keys errors))))
  (return (xt/x:get-key form "valid")))

(defn.xt set-field! [form path value]
  (var draft (-/clone (xt/x:get-key form "draft")))
  (xtd/set-in draft path value)
  (xt/x:set-key form "draft" draft)
  (xtd/set-in (xt/x:get-key form "touched") path true)
  (xt/x:set-key form "dirty" true)
  (-/validate! form)
  (return draft))

(defn.xt reset! [form]
  (xt/x:set-key form "draft" (-/clone (xt/x:get-key form "initial")))
  (xt/x:set-key form "errors" {})
  (xt/x:set-key form "touched" {})
  (xt/x:set-key form "dirty" false)
  (xt/x:set-key form "valid" true)
  (xt/x:set-key form "pending" false)
  (return form))

(defn.xt pending! [form pending]
  (xt/x:set-key form "pending" (== true pending))
  (return form))
