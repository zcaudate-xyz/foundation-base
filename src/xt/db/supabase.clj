(ns xt.db.supabase
  (:require [std.lang :as l]))

;;
;; Canonical Supabase Realtime `postgres_changes` -> xt.db sync adapter.
;;
;; - Input payload shape follows Supabase Realtime callback payload.
;; - We translate to `xt.db` events and apply them to a local SQLite-backed xt.db instance.
;;

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.db :as xdb]]})

(defn.js snake->kebab
  "Converts a snake_case key to kebab-case.

  Supabase payload rows use column names as sent by Postgres (snake_case).
  `pg/bind-schema` + `xt.db` commonly operate on kebab-case keys.
  This normalizer accepts both and is a no-op for keys without underscores."
  {:added "4.1.3"}
  [s]
  (cond (k/is-string? s)
        (return (k/replace s "_" "-"))

        :else
        (return s)))

(defn.js normalize-row
  "Normalizes row keys to kebab-case (underscore -> dash).

  Keeps values intact; does not attempt any type coercion."
  {:added "4.1.3"}
  [row]
  (when (k/nil? row)
    (return nil))
  (var out {})
  (k/for:object [[k v] row]
    (k/set-key out (-/snake->kebab k) v))
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
  (var id-key (or (k/get-key opts "id-key") "id"))
  (var eventType (k/get-key payload "eventType"))
  (var table     (k/get-key payload "table"))
  (var newv       (-/normalize-row (k/get-key payload "new")))
  (var oldv       (-/normalize-row (k/get-key payload "old")))
  (when (or (k/nil? eventType)
            (k/nil? table))
    (k/err "Invalid supabase payload: missing `eventType` or `table`."))
  (cond (or (== eventType "INSERT")
            (== eventType "UPDATE"))
        (do (when (k/nil? newv)
              (k/err "Invalid supabase payload: INSERT/UPDATE missing `new` row."))
            (var newv-id (k/get-key newv id-key))
            (var oldv-id (and oldv (k/get-key oldv id-key)))
            (var add-body {})
            (k/set-key add-body table [newv])
            ;; PK change on update: remove oldv id then add newv row.
            (cond (and (== eventType "UPDATE")
                       (k/not-nil? oldv-id)
                       (k/not-nil? newv-id)
                       (not= oldv-id newv-id))
                  (do (var rem-body {})
                      (k/set-key rem-body table [{"id" oldv-id}])
                      (return [["remove" rem-body]
                               ["add"    add-body]]))

                  :else
                  (return [["add" add-body]])))

        (== eventType "DELETE")
        (do (when (k/nil? oldv)
              (k/err "Invalid supabase payload: DELETE missing `old` row."))
            (var oldv-id (k/get-key oldv id-key))
            (when (k/nil? oldv-id)
              (k/err "Invalid supabase payload: DELETE missing `old.id`."))
            (var rem-body {})
            (k/set-key rem-body table [{"id" oldv-id}])
            (return [["remove" rem-body]]))

        :else
        (k/err (k/cat "Unsupported supabase eventType: " (k/to-string eventType)))))

(defn.js apply-payload
  "Applies a Supabase payload to a local xt.db instance using `xdb/sync-event`.

  Returns:
  - `{:table <string> :ids [..] :events [..]}` where ids are derived from `new/old`."
  {:added "4.1.3"}
  [xdb payload schema lookup opts apply-opts]
  (:= apply-opts (or apply-opts {}))
  (var id-key (or (k/get-key apply-opts "id-key") "id"))
  (var table  (k/get-key payload "table"))
  (var newv    (-/normalize-row (k/get-key payload "new")))
  (var oldv    (-/normalize-row (k/get-key payload "old")))
  (var ids    [])
  (when newv
    (var nid (k/get-key newv id-key))
    (when (k/not-nil? nid)
      (x:arr-push ids nid)))
  (when (and oldv (or (== (k/get-key payload "eventType") "DELETE")
                     ;; include oldv-id on PK change updates
                     (and newv
                          (not= (k/get-key oldv id-key)
                                (k/get-key newv id-key)))))
    (var oid (k/get-key oldv id-key))
    (when (and (k/not-nil? oid)
               (not (k/arr-some ids (fn:> [x] (== x oid)))))
      (x:arr-push ids oid)))
  (var events (-/payload->xdb-events payload {"id-key" id-key}))
  (k/for:array [e events]
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
  (var supabase     (k/get-key m "supabase"))
  (var xdb          (k/get-key m "xdb"))
  (var schema       (k/get-key m "schema"))
  (var lookup       (k/get-key m "lookup"))
  (var opts         (k/get-key m "opts"))
  (var channel-name (or (k/get-key m "channel-name") "xt.db.supabase"))
  (var bindings     (or (k/get-key m "bindings") []))
  (var id-key       (or (k/get-key m "id-key") "id"))
  (var on-payload   (k/get-key m "on-payload"))
  (var on-applied   (k/get-key m "on-applied"))
  (when (or (k/nil? supabase) (k/nil? xdb))
    (k/err "attach! requires `:supabase` and `:xdb`."))
  (var channel (. supabase (channel channel-name)))
  (var handler
       (fn [payload]
         (when (k/fn? on-payload)
           (on-payload payload))
         (var res (-/apply-payload xdb payload schema lookup opts {"id-key" id-key}))
         (when (k/fn? on-applied)
           (on-applied res))
         (return res)))
  (k/for:array [binding bindings]
    ;; Supabase expects `{event, schema, table, filter?}`.
    (. channel (on "postgres_changes" binding handler)))
  (. channel (subscribe))
  (var detach-fn
       (fn []
         (cond (k/fn? (. channel ["unsubscribe"]))
               (return (. channel (unsubscribe)))

               (k/fn? (. supabase ["removeChannel"]))
               (return (. supabase (removeChannel channel)))
               
               :else
               (return nil))))
  (return {"channel" channel
           "detach-fn" detach-fn}))

