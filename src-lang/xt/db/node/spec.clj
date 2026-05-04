(ns xt.db.node.spec
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(def$.xt META_KEY "xt.db")
(def$.xt STATE_TAG "xt.db.state")

(def$.xt ACTION_QUERY         "xt.db/query")
(def$.xt ACTION_QUERY_REFRESH "xt.db/query-refresh")
(def$.xt ACTION_SYNC          "xt.db/sync")
(def$.xt ACTION_REMOVE        "xt.db/remove")
(def$.xt ACTION_CLEAR         "xt.db/clear")
(def$.xt ACTION_SNAPSHOT      "xt.db/snapshot")

(def$.xt SIGNAL_CACHE_CHANGED     "xt.db/cache.changed")
(def$.xt SIGNAL_CACHE_INVALIDATED "xt.db/cache.invalidated")
(def$.xt SIGNAL_QUERY_CHANGED     "xt.db/query.changed")
(def$.xt SIGNAL_MODEL_CHANGED     "xt.db/model.changed")

(def$.xt STATUS_IDLE    "idle")
(def$.xt STATUS_PENDING "pending")
(def$.xt STATUS_READY   "ready")
(def$.xt STATUS_STALE   "stale")
(def$.xt STATUS_ERROR   "error")

(defn.xt base-state
  "creates the base xt.db state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" -/STATE_TAG
           :db nil
           :schema   (or (xt/x:get-key opts "schema") {})
           :lookup   (or (xt/x:get-key opts "lookup") {})
           :views    (or (xt/x:get-key opts "views") {})
           :queries  {}
           :models   {}
           :watch    {}
           :pending  {}
           :remote   (or (xt/x:get-key opts "remote") {})
           :opts     opts
           :meta     (or (xt/x:get-key opts "meta") {})}))
