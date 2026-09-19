(ns xt.db.node.view-base
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.base-util :as base-util]
             [xt.db.node.client-base :as client-base]]})

(defn.xt collect-views
  "Groups bound view descriptors by table, type, and tag."
  {:added "4.1"}
  [entries]
  (return (base-util/collect-views entries)))

(defn.xt view-query-entries
  "Resolves select and return methods for a table."
  {:added "4.1"}
  [views table query]
  (var select-method (xtd/get-in query ["select_method"]))
  (var return-method (xtd/get-in query ["return_method"]))
  (var select-entry (:? select-method
                         (xtd/get-in views [table "select" select-method])
                         nil))
  (var return-entry (:? return-method
                         (xtd/get-in views [table "return" return-method])
                         nil))
  (when (and select-method (xt/x:nil? select-entry))
    (throw (xt/x:ex
            "Select view method was not found"
            {"table" table
             "method" select-method})))
  (when (and return-method (xt/x:nil? return-entry))
    (throw (xt/x:ex
            "Return view method was not found"
            {"table" table
             "method" return-method})))
  (return {"select_entry" select-entry
           "return_entry" return-entry}))

(defn.xt view-query-spec
  "Builds the explicit dataview pair consumed by xt.db.node."
  {:added "4.1"}
  [views table query]
  (var entries (-/view-query-entries views table query))
  (return
   {"table" table
    "select_entry" (xtd/get-in entries ["select_entry"])
    "return_entry" (xtd/get-in entries ["return_entry"])
    "select_args" (or (xtd/get-in query ["select_args"]) [])
    "return_args" (or (xtd/get-in query ["return_args"]) [])
    "return_count" (xtd/get-in query ["return_count"])
    "return_id" (xtd/get-in query ["return_id"])
    "return_bulk" (xtd/get-in query ["return_bulk"])
    "return_omit" (or (xtd/get-in query ["return_omit"]) [])}))

(defn.xt view-query
  "Resolves a named view pair and executes it through a node."
  {:added "4.1"}
  [node primary-id views table query opts]
  (return
   (client-base/dataview-call node
                              primary-id
                              (-/view-query-spec views table query)
                              opts)))

(defn.xt view-query-cached
  "Resolves a named view pair and reads it from the node cache."
  {:added "4.1"}
  [node primary-id views table query opts]
  (return
   (client-base/dataview-cached node
                                primary-id
                                (-/view-query-spec views table query)
                                opts)))

(defn.xt view-attach-model
  "Attaches a page model backed by a named view pair."
  {:added "4.1"}
  [node primary-id views page-args model table query opts]
  (return
   (client-base/dataview-attach-model
    node
    primary-id
    page-args
    (-/view-query-spec views table query)
    model
    opts)))
