(ns xt.db.node.view-sync
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt payload-tables
  "gets the affected tables for a sync or invalidation payload"
  {:added "4.1"}
  [payload]
  (cond (xt/x:is-array? payload)
        (return payload)

        (xt/x:is-object? payload)
        (return (xt/x:obj-keys
                 (or (xt/x:get-key payload "db/sync")
                     (xt/x:get-key payload "db/remove")
                     (xt/x:get-key payload "tables")
                     {})))

        :else
        (return [])))

(defn.xt not-implemented
  "returns a standard sync-not-implemented payload"
  {:added "4.1"}
  [payload]
  (return {:status "error"
           :tag "xt.db.node.view/sync-not-implemented"
           :data payload
           :tables (-/payload-tables payload)}))
