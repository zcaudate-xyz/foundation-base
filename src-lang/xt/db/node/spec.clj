(ns xt.db.node.spec
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(def$.xt META_KEY "xt.db.node")
(def$.xt STATE_TAG "xt.db.node.state")

(def$.xt ACTION_QUERY         "xt.db.node/query")
(def$.xt ACTION_QUERY_REFRESH "xt.db.node/query-refresh")
(def$.xt ACTION_SYNC          "xt.db.node/sync")
(def$.xt ACTION_REMOVE        "xt.db.node/remove")
(def$.xt ACTION_CLEAR         "xt.db.node/clear")
(def$.xt ACTION_SNAPSHOT      "xt.db.node/snapshot")

(def$.xt SIGNAL_CACHE_CHANGED     "xt.db.node/cache.changed")
(def$.xt SIGNAL_CACHE_INVALIDATED "xt.db.node/cache.invalidated")
(def$.xt SIGNAL_QUERY_CHANGED     "xt.db.node/query.changed")
(def$.xt SIGNAL_MODEL_CHANGED     "xt.db.node/model.changed")

(def$.xt STATUS_IDLE    "idle")
(def$.xt STATUS_PENDING "pending")
(def$.xt STATUS_READY   "ready")
(def$.xt STATUS_STALE   "stale")
(def$.xt STATUS_ERROR   "error")

(defn.xt base-state
  "creates the base xt.db.node state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" -/STATE_TAG
           :db nil
           :schema (or (xt/x:get-key opts "schema") {})
           :lookup (or (xt/x:get-key opts "lookup") {})
           :views (or (xt/x:get-key opts "views") {})
           :queries {}
           :models {}
           :watch {}
           :pending {}
           :remote (or (xt/x:get-key opts "remote") {})
           :opts opts
           :meta (or (xt/x:get-key opts "meta") {})}))
