(ns xt.db.runtime.model-query
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-query :as schema-query]
             [xt.lang.spec-base :as xt]]})

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
