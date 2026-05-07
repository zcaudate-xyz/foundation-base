(ns postgres.sample.scratch-v3.realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]
   :export [MODULE]})

(def.xt CHANNEL
  {"topic" "realtime:public:Currency"
   "schema" "public"
   "table" "Currency"
   "event" "*"})

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
