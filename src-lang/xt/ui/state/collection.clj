(ns xt.ui.state.collection
  "Serializable collection query, pagination and selection state."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt create [opts]
  (return {"items" (or (xt/x:get-key opts "items") [])
           "query" (or (xt/x:get-key opts "query") {})
           "page" (or (xt/x:get-key opts "page") 0)
           "page_size" (or (xt/x:get-key opts "page_size") 25)
           "total" (or (xt/x:get-key opts "total") 0)
           "selected" {}
           "pending" false
           "error" nil}))

(defn.xt set-items! [state items total]
  (xt/x:set-key state "items" (or items []))
  (xt/x:set-key state "total" (or total (xt/x:len (or items []))))
  (xt/x:set-key state "pending" false)
  (xt/x:set-key state "error" nil)
  (return state))

(defn.xt set-query! [state path value]
  (var query (xtd/clone-nested (or (xt/x:get-key state "query") {})))
  (xtd/set-in query path value)
  (xt/x:set-key state "query" query)
  (xt/x:set-key state "page" 0)
  (return state))

(defn.xt set-page! [state page]
  (xt/x:set-key state "page" (or page 0))
  (return state))

(defn.xt select! [state id selected]
  (if (== true selected)
    (xt/x:set-key (xt/x:get-key state "selected") id true)
    (xt/x:del-key (xt/x:get-key state "selected") id))
  (return state))

(defn.xt selected-ids [state]
  (return (xt/x:obj-keys (xt/x:get-key state "selected"))))
