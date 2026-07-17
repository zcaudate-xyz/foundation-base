(ns xt.ui.state.crud
  "Schema-driven operational controller specifications."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.state.collection :as collection]
             [xt.ui.state.form :as form]]})

(defn.xt spec [id models fields columns actions opts]
  (return {"id" id
           "strategy" "page_controller"
           "models" (or models {})
           "fields" (or fields [])
           "columns" (or columns [])
           "actions" (or actions [])
           "opts" (or opts {})}))

(defn.xt create-state [crud-spec values]
  (return {"status" "idle"
           "spec" crud-spec
           "collection" (collection/create {})
           "form" (form/create (or values {}) {})
           "record" nil
           "mode" "list"
           "errors" {}}))

(defn.xt set-mode! [state mode record]
  (xt/x:set-key state "mode" mode)
  (xt/x:set-key state "record" record)
  (return state))
