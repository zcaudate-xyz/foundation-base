(ns xt.db.node.model-query
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-query :as schema-query]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt payload-view-context
  "normalizes payload view context"
  {:added "4.1"}
  [payload]
  (var view-context (xtd/clone-nested (or (xt/x:get-key payload "view") {})))
  (when (and (xt/x:nil? (xt/x:get-key view-context "model_id"))
             (xt/x:not-nil? (xt/x:get-key payload "model_id")))
    (xt/x:set-key view-context "model_id" (xt/x:get-key payload "model_id")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "view_id"))
             (xt/x:not-nil? (xt/x:get-key payload "view_id")))
    (xt/x:set-key view-context "view_id" (xt/x:get-key payload "view_id")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "args"))
             (xt/x:not-nil? (xt/x:get-key payload "args")))
    (xt/x:set-key view-context "args" (xt/x:get-key payload "args")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "args"))
             (xt/x:not-nil? (xt/x:get-key payload "input")))
    (xt/x:set-key view-context "args" (xt/x:get-key payload "input")))
  (return view-context))

(defn.xt query-resolver
  "gets the resolver/query payload for a request"
  {:added "4.1"}
  [payload]
  (return (or (xt/x:get-key payload "resolver")
              payload)))

(defn.xt prepare-resolver
  "prepares a local query plan from a db/query resolver"
  {:added "4.1"}
  [state resolver view-context]
  (return (schema-query/prepare-query
           state
           resolver
           view-context)))

(defn.xt resolver-triggers
  "gets dependent tables for a db/query resolver"
  {:added "4.1"}
  [state resolver]
  (:= resolver (or resolver {}))
  (return
   (schema-query/view-triggers
    state
    (xt/x:get-key resolver "table")
    resolver)))
