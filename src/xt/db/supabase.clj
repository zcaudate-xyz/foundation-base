(ns xt.db.supabase
  (:require [std.lang :as l]))

;;
;; Canonical Supabase Realtime `postgres_changes` -> xt.db sync adapter.
;;
;; - Input payload shape follows Supabase Realtime callback payload.
;; - We translate to `xt.db` events and apply them to a local SQLite-backed xt.db instance.
;;

(l/script :js
  {:require [[xt.lang.common-spec :as xt]
             [xt.db :as xdb]]})

(defn.js snake->kebab
  "Converts a snake_case key to kebab-case.

  Supabase payload rows use column names as sent by Postgres (snake_case).
  `pg/bind-schema` + `xt.db` commonly operate on kebab-case keys.
  This normalizer accepts both and is a no-op for keys without underscores."
  {:added "4.1.3"}
  [s]
  (cond (xt/x:is-string? s)
        (return (xt/x:replace s "_" "-"))

        :else
        (return s)))

(defn.js normalize-row
  "Normalizes row keys to kebab-case (underscore -> dash).

  Keeps values intact; does not attempt any type coercion."
  {:added "4.1.3"}
  [row]
  (when (xt/x:nil? row)
    (return nil))
  (var out {})
  (xt/for:object [[k v] row]
    (xt/x:set-key out (-/snake->kebab k) v))
  (return out))

(defn.js payload->xdb-events
  "Translates a Supabase `postgres_changes` payload into one (or two) xt.db events.

  Options:
  - `:id-key` (string, default \"id\")

  Returns:
  - JS array of xt.db events: `[\"add\" {\"Table\" [row]}]` or `[\"remove\" {\"Table\" [{\"id\" ...}]}]`."
  {:added "4.1.3"}
  [payload opts]
  (:= opts (or opts {}))
  (var id-key (or (xt/x:get-key opts "id-key") "id"))
  (var eventType (xt/x:get-key payload "eventType"))
  (var table     (xt/x:get-key payload "table"))
  (var newv       (-/normalize-row (xt/x:get-key payload "new")))
  (var oldv       (-/normalize-row (xt/x:get-key payload "old")))
  (when (or (xt/x:nil? eventType)
            (xt/x:nil? table))
    (xt/x:err "Invalid supabase payload: missing `eventType` or `table`."))
  (cond (or (== eventType "INSERT")
            (== eventType "UPDATE"))
        (do (when (xt/x:nil? newv)
              (xt/x:err "Invalid supabase payload: INSERT/UPDATE missing `new` row."))
            (var newv-id (xt/x:get-key newv id-key))
            (var oldv-id (and oldv (xt/x:get-key oldv id-key)))
            (var add-body {})
            (xt/x:set-key add-body table [newv])
            ;; PK change on update: remove oldv id then add newv row.
            (cond (and (== eventType "UPDATE")
                       (xt/x:not-nil? oldv-id)
                       (xt/x:not-nil? newv-id)
                       (not= oldv-id newv-id))
                  (do (var rem-body {})
                      (xt/x:set-key rem-body table [{"id" oldv-id}])
                      (return [["remove" rem-body]
                               ["add"    add-body]]))

                  :else
                  (return [["add" add-body]])))

        (== eventType "DELETE")
        (do (when (xt/x:nil? oldv)
              (xt/x:err "Invalid supabase payload: DELETE missing `old` row."))
            (var oldv-id (xt/x:get-key oldv id-key))
            (when (xt/x:nil? oldv-id)
              (xt/x:err "Invalid supabase payload: DELETE missing `old.id`."))
            (var rem-body {})
            (xt/x:set-key rem-body table [{"id" oldv-id}])
            (return [["remove" rem-body]]))

        :else
        (xt/x:err (xt/x:cat "Unsupported supabase eventType: " (xt/x:to-string eventType)))))

(defn.js apply-payload
  "Applies a Supabase payload to a local xt.db instance using `xdb/sync-event`.

  Returns:
  - `{:table <string> :ids [..] :events [..]}` where ids are derived from `new/old`."
  {:added "4.1.3"}
  [xdb payload schema lookup opts apply-opts]
  (:= apply-opts (or apply-opts {}))
  (var id-key (or (xt/x:get-key apply-opts "id-key") "id"))
  (var table  (xt/x:get-key payload "table"))
  (var newv    (-/normalize-row (xt/x:get-key payload "new")))
  (var oldv    (-/normalize-row (xt/x:get-key payload "old")))
  (var ids    [])
  (when newv
    (var nid (xt/x:get-key newv id-key))
    (when (xt/x:not-nil? nid)
      (x:arr-push ids nid)))
  (when (and oldv (or (== (xt/x:get-key payload "eventType") "DELETE")
                     ;; include oldv-id on PK change updates
                     (and newv
                          (not= (xt/x:get-key oldv id-key)
                                (xt/x:get-key newv id-key)))))
    (var oid (xt/x:get-key oldv id-key))
    (when (and (xt/x:not-nil? oid)
               (not (xt/x:arr-some ids (fn:> [x] (== x oid)))))
      (x:arr-push ids oid)))
  (var events (-/payload->xdb-events payload {"id-key" id-key}))
  (xt/for:array [e events]
    (xdb/sync-event xdb e))
  (return {"table" table
           "ids" ids
           "events" events}))

(defn.js attach-events
  "Attaches Supabase Realtime `postgres_changes` listeners and applies each payload to `xdb`.

  Input:
  - `{:supabase <SupabaseClient>
      :xdb <xt.db instance>
      :schema <schema>
      :lookup <lookup>
      :opts <xt.db opts>
      :channel-name <string>
      :bindings [{:event ... :schema ... :table ... :filter ...} ...]
      :id-key \"id\"
      :on-payload (fn [payload] ...)
      :on-applied (fn [result] ...)}`

  Returns:
  - `{:channel <RealtimeChannel> :detach-fn (fn [])}`"
  {:added "4.1.3"}
  [m]
  (var supabase     (xt/x:get-key m "supabase"))
  (var xdb          (xt/x:get-key m "xdb"))
  (var schema       (xt/x:get-key m "schema"))
  (var lookup       (xt/x:get-key m "lookup"))
  (var opts         (xt/x:get-key m "opts"))
  (var channel-name (or (xt/x:get-key m "channel-name") "xt.db.supabase"))
  (var bindings     (or (xt/x:get-key m "bindings") []))
  (var id-key       (or (xt/x:get-key m "id-key") "id"))
  (var on-payload   (xt/x:get-key m "on-payload"))
  (var on-applied   (xt/x:get-key m "on-applied"))
  (when (or (xt/x:nil? supabase) (xt/x:nil? xdb))
    (xt/x:err "attach! requires `:supabase` and `:xdb`."))
  (var channel (. supabase (channel channel-name)))
  (var handler
       (fn [payload]
         (when (xt/x:is-function? on-payload)
           (on-payload payload))
         (var res (-/apply-payload xdb payload schema lookup opts {"id-key" id-key}))
         (when (xt/x:is-function? on-applied)
           (on-applied res))
         (return res)))
  (xt/for:array [binding bindings]
    ;; Supabase expects `{event, schema, table, filter?}`.
    (. channel (on "postgres_changes" binding handler)))
  (. channel (subscribe))
  (var detach-fn
       (fn []
         (cond (xt/x:is-function? (. channel ["unsubscribe"]))
               (return (. channel (unsubscribe)))

               (xt/x:is-function? (. supabase ["removeChannel"]))
               (return (. supabase (removeChannel channel)))
               
               :else
               (return nil))))
  (return {"channel" channel
           "detach-fn" detach-fn}))

