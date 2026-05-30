(ns postgres.sample.scratch-v3.realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]
   :export [MODULE]})

(def.xt EV_DB_SYNC "currency/db-sync")
(def.xt EV_DB_REMOVE "currency/db-remove")

(def.xt CHANNEL
  {"topic" "realtime:public:Currency"
   "schema" "public"
   "table" "Currency"
   "event" "*"})

(defn.xt sync-tables
  "returns a predicate that matches updates for any table in the set"
  {:added "4.1"}
  [tables]
  (var arr (:? (xt/x:is-array? tables) tables [tables]))
  (var lu {})
  (xt/for:array [table arr]
   (xt/x:set-key lu table true))
  (return (fn [m]
           (var body (or (. m ["body"]) {}))
           (xt/for:object [[table _] body]
             (when (. lu [table])
               (return true)))
           (return false))))

(defn.xt db-sync
  "builds the event-sync map used by curated api descriptors"
  {:added "4.1"}
  [tables]
  (return {-/EV_DB_SYNC   (-/sync-tables tables)
          -/EV_DB_REMOVE (-/sync-tables tables)}))

(defn.xt payload-id
  "extracts the primary id from a realtime payload"
  {:added "4.1"}
  [payload]
  (return (or (. payload ["id"])
              (xtd/get-in payload ["old" "id"])
              (xtd/get-in payload ["new" "id"]))))

(defn.xt postgres-change->sync-request
  "converts a Supabase postgres_changes payload into db/sync or db/remove"
  {:added "4.1"}
  [payload]
  (var event-type (xt/x:str-to-upper (or (. payload ["eventType"])
                                         (. payload ["event_type"])
                                         "")))
  (var new-row (or (. payload ["new"]) {}))
  (var old-row (or (. payload ["old"]) {}))
  (var row (:? (== event-type "DELETE") old-row new-row))
  (var row-id (-/payload-id payload))
  (cond (== event-type "")
        (return nil)

        (== event-type "DELETE")
        (return {"db/remove" {"Currency" [row-id]}})

        :else
        (do (xt/x:obj-assign row {"__deleted__" false})
            (return {"db/sync" {"Currency" [row]}}))))

(defn.xt postgres-change->update
  "wraps a sync request in the js.cell patch update shape"
  {:added "4.1"}
  [payload]
  (var request (-/postgres-change->sync-request payload))
  (when request
    (return {"type" "patch"
             "body" request})))

(def.xt MODULE (!:module))
