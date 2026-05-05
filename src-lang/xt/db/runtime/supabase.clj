(ns xt.db.runtime.supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.pgrest :as pgrest]]})

(defn.xt thenable?
  "checks whether a value can be chained via .then"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (xt/x:is-function? (xt/x:get-key value "then")))))

(defn.xt query-client?
  "checks whether a value looks like a Supabase query client"
  {:added "4.1"}
  [value]
  (return (and (xt/x:is-object? value)
               (xt/x:is-function? (xt/x:get-key value "from")))))

(defn.xt resolve-client
  "resolves a Supabase client from db or opts"
  {:added "4.1"}
  [db opts]
  (var db-supabase (xt/x:get-key db "supabase"))
  (var db-client   (xt/x:get-key db "client"))
  (var opt-supabase (xt/x:get-key opts "supabase"))
  (var opt-client   (xt/x:get-key opts "client"))
  (cond (-/query-client? db-supabase)
        (return db-supabase)

        (-/query-client? db-client)
        (return db-client)

        (-/query-client? opt-supabase)
        (return opt-supabase)

        (-/query-client? opt-client)
        (return opt-client)

        (-/query-client? db)
        (return db)

        (-/query-client? opts)
        (return opts)

        :else
        (return nil)))

(defn.xt resolve-schema-name
  "resolves an optional Supabase schema name from db or opts"
  {:added "4.1"}
  [db opts]
  (var db-schema (xt/x:get-key db "schema"))
  (var opt-schema (xt/x:get-key opts "schema"))
  (var schema-name (or (xt/x:get-key db "schema-name")
                       (xt/x:get-key db "schema_name")
                       (xt/x:get-key db "supabase-schema")
                       (xt/x:get-key db "supabase_schema")
                       (:? (xt/x:is-string? db-schema) db-schema nil)
                       (xt/x:get-key opts "schema-name")
                       (xt/x:get-key opts "schema_name")
                       (xt/x:get-key opts "supabase-schema")
                       (xt/x:get-key opts "supabase_schema")
                       (:? (xt/x:is-string? opt-schema) opt-schema nil)))
  (return (:? (xt/x:is-string? schema-name)
              schema-name
              nil)))

(defn.xt supabase-capable?
  "checks that the db descriptor can execute compiled supabase queries"
  {:added "4.1"}
  [db]
  (return (or (xt/x:is-function? (xt/x:get-key db "execute"))
              (-/query-client? (xt/x:get-key db "supabase"))
              (-/query-client? (xt/x:get-key db "client"))
              (-/query-client? db))))

(def.xt compile-select-item pgrest/compile-select-item)

(def.xt compile-select pgrest/compile-select)

(def.xt compile-filters-into pgrest/compile-filters-into)

(def.xt compile-query pgrest/compile-query)

(defn.xt invoke-method-1
  "invokes a method by key with one argument"
  {:added "4.1"}
  [obj method arg]
  (var f (xt/x:get-key obj method))
  (when (not (xt/x:is-function? f))
    (xt/x:err (xt/x:cat "supabase method not found - "
                        method)))
  (return (f arg)))

(defn.xt invoke-method-2
  "invokes a method by key with two arguments"
  {:added "4.1"}
  [obj method arg1 arg2]
  (var f (xt/x:get-key obj method))
  (when (not (xt/x:is-function? f))
    (xt/x:err (xt/x:cat "supabase method not found - "
                        method)))
  (return (f arg1 arg2)))

(defn.xt apply-filter
  "applies one compiled filter to a Supabase query builder"
  {:added "4.1"}
  [query filter]
  (var path  (xt/x:get-key filter "path"))
  (var op    (xt/x:get-key filter "op"))
  (var value (xt/x:get-key filter "value"))
  (cond (== op "eq")
        (return (-/invoke-method-2 query "eq" path value))

        (== op "neq")
        (return (-/invoke-method-2 query "neq" path value))

        (== op "gt")
        (return (-/invoke-method-2 query "gt" path value))

        (== op "gte")
        (return (-/invoke-method-2 query "gte" path value))

        (== op "lt")
        (return (-/invoke-method-2 query "lt" path value))

        (== op "lte")
        (return (-/invoke-method-2 query "lte" path value))

        (== op "like")
        (return (-/invoke-method-2 query "like" path value))

        (== op "ilike")
        (return (-/invoke-method-2 query "ilike" path value))

        (== op "is")
        (return (-/invoke-method-2 query "is" path value))

        (== op "in")
        (return (-/invoke-method-2 query "in" path value))

        (== op "contains")
        (return (-/invoke-method-2 query "contains" path value))

        (== op "containedBy")
        (return (-/invoke-method-2 query "containedBy" path value))

        (== op "match")
        (return (-/invoke-method-1 query "match" value))

        :else
        (xt/x:err (xt/x:cat "unsupported supabase filter op - "
                            (xt/x:to-string op)))))

(defn.xt apply-filters
  "applies compiled filters to a Supabase query builder"
  {:added "4.1"}
  [query filters]
  (var out query)
  (xt/for:array [filter (or filters [])]
    (:= out (-/apply-filter out filter)))
  (return out))

(defn.xt execute-query-default
  "executes a compiled query using a Supabase client descriptor"
  {:added "4.1"}
  [db compiled opts]
  (var client (-/resolve-client db opts))
  (when (xt/x:nil? client)
    (return nil))
  (var schema-name (-/resolve-schema-name db opts))
  (var query (:? (and (xt/x:not-nil? schema-name)
                      (xt/x:is-function? (xt/x:get-key client "schema")))
                  (-/invoke-method-1 client "schema" schema-name)
                  client))
  (:= query (-/invoke-method-1 query "from" (xt/x:get-key compiled "table")))
  (:= query (-/invoke-method-1 query
                               "select"
                               (:? (xtd/not-empty? (xt/x:get-key compiled "select"))
                                   (xt/x:get-key compiled "select")
                                   "*")))
  (return (-/apply-filters query
                           (xt/x:get-key compiled "filters"))))

(defn.xt unwrap-query-output
  "unwraps execution output into rows or a mapped local error"
  {:added "4.1"}
  [db output opts]
  (cond (and (xt/x:is-array? output)
             (== 2 (xt/x:len output))
             (xt/x:is-boolean? (xt/x:first output)))
        (do (var ok (xt/x:first output))
            (var result (xt/x:second output))
            (when (not ok)
              (return (-/map-supabase-error db result opts)))
            (when (== "error" (xt/x:get-key result "status"))
              (return (-/map-supabase-error db result opts)))
            (when (and (xt/x:is-object? result)
                       (xt/x:not-nil? (xt/x:get-key result "error")))
              (return (-/map-supabase-error db
                                            (xt/x:get-key result "error")
                                            opts)))
            (when (and (xt/x:is-object? result)
                       (xt/x:has-key? result "data"))
              (return (xt/x:get-key result "data")))
            (return result))

        (and (xt/x:is-object? output)
             (xt/x:not-nil? (xt/x:get-key output "error")))
        (return (-/map-supabase-error db
                                      (xt/x:get-key output "error")
                                      opts))

        (== "error" (xt/x:get-key output "status"))
        (return (-/map-supabase-error db output opts))

        (and (xt/x:is-object? output)
             (xt/x:has-key? output "data"))
        (return (xt/x:get-key output "data"))

        :else
        (return output)))

(defn.xt execute-query
  "executes a compiled Supabase query via an injected executor"
  {:added "4.1"}
  [db compiled opts]
  (var execute-fn (or (xt/x:get-key db "execute")
                      (xt/x:get-key opts "execute")))
  (when (xt/x:is-function? execute-fn)
    (return (execute-fn compiled opts)))
  (var default-output (-/execute-query-default db compiled opts))
  (when (xt/x:not-nil? default-output)
    (return default-output))
  (return [false {:status "error"
                  :tag "db/supabase-execute-not-provided"
                  :data compiled}]))

(defn.xt map-supabase-error
  "maps an execution error into the local error contract"
  {:added "4.1"}
  [db error opts]
  (var map-fn (or (xt/x:get-key db "map_error")
                  (xt/x:get-key opts "map_error")))
  (if (xt/x:is-function? map-fn)
    (return (map-fn error opts))
    (return {:status "error"
             :tag "db/supabase-query-failed"
             :data error})))

(defn.xt supabase-pull-sync
  "compiles a query tree and executes it against a Supabase backend"
  {:added "4.1"}
  [db schema tree opts]
  (var compiled (pgrest/compile-query tree))
  (var output (-/execute-query db compiled opts))
  (if (-/thenable? output)
    (do (var chained
             (-/invoke-method-1 output
                                "then"
                                (fn [result]
                                  (return (-/unwrap-query-output db result opts)))))
        (if (xt/x:is-function? (xt/x:get-key chained "catch"))
          (return
           (-/invoke-method-1 chained
                              "catch"
                              (fn [err]
                                (return (-/map-supabase-error db err opts)))))
          (return chained)))
    (return (-/unwrap-query-output db output opts))))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.db.text.pgrest :as pgrest]]})

(defn.js snake->kebab
  "Converts a snake_case key to kebab-case."
  {:added "4.1.3"}
  [s]
  (cond (xt/x:is-string? s)
        (return (xt/x:str-replace s "_" "-"))

        :else
        (return s)))

(defn.js normalize-row
  "Normalizes row keys to kebab-case."
  {:added "4.1.3"}
  [row]
  (when (xt/x:nil? row)
    (return nil))
  (var out {})
  (xt/for:object [[k v] row]
    (xt/x:set-key out (-/snake->kebab k) v))
  (return out))

(defn.js payload->xdb-events
  "Translates a Supabase `postgres_changes` payload into xt.db events."
  {:added "4.1.3"}
  [payload opts]
  (:= opts (or opts {}))
  (var id-key (or (xt/x:get-key opts "id-key") "id"))
  (var eventType (xt/x:get-key payload "eventType"))
  (var table (xt/x:get-key payload "table"))
  (var newv (-/normalize-row (xt/x:get-key payload "new")))
  (var oldv (-/normalize-row (xt/x:get-key payload "old")))
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
            (cond (and (== eventType "UPDATE")
                       (xt/x:not-nil? oldv-id)
                       (xt/x:not-nil? newv-id)
                       (not= oldv-id newv-id))
                  (do (var rem-body {})
                      (xt/x:set-key rem-body table [{"id" oldv-id}])
                      (return [["remove" rem-body]
                               ["add" add-body]]))

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

(defn.js process-triggers-local
  "Runs local db triggers for tables touched by a Supabase payload sync."
  {:added "4.1.3"}
  [db tables]
  (var triggers (or (xt/x:get-key db "triggers") {}))
  (var out [])
  (xt/for:object [[id trigger] triggers]
    (var #{listen callback} trigger)
    (var update? (xt/x:arr-some listen (fn [key] (return (xt/x:has-key? tables key)))))
    (when update?
      (xt/x:arr-push out id)
      (if (xt/x:get-key trigger "async")
        (callback db trigger)
        (callback db trigger))))
  (return out))

(defn.js sync-event-local
  "Applies one xt.db event to a local db instance object without requiring xt.db.runtime."
  {:added "4.1.3"}
  [db event]
  (var sync-handler (xt/x:get-key db "sync_handler"))
  (var output (sync-handler event))
  (cond (or (xt/x:is-string? output)
            (xt/x:nil? output))
        (return output)

        :else
        (do (var tables (xtd/arr-lookup output))
            (return [(-/process-triggers-local db tables)
                     tables]))))

(defn.js apply-payload
  "Applies a Supabase payload to a local db instance using local xt.db sync handlers."
  {:added "4.1.3"}
  [xdb payload schema lookup opts apply-opts]
  (:= apply-opts (or apply-opts {}))
  (var id-key (or (xt/x:get-key apply-opts "id-key") "id"))
  (var table (xt/x:get-key payload "table"))
  (var newv (-/normalize-row (xt/x:get-key payload "new")))
  (var oldv (-/normalize-row (xt/x:get-key payload "old")))
  (var ids [])
  (when newv
    (var nid (xt/x:get-key newv id-key))
    (when (xt/x:not-nil? nid)
      (xt/x:arr-push ids nid)))
  (when (and oldv (or (== (xt/x:get-key payload "eventType") "DELETE")
                      (and newv
                           (not= (xt/x:get-key oldv id-key)
                                 (xt/x:get-key newv id-key)))))
    (var oid (xt/x:get-key oldv id-key))
    (when (and (xt/x:not-nil? oid)
               (not (xt/x:arr-some ids (fn:> [x] (== x oid)))))
      (xt/x:arr-push ids oid)))
  (var events (-/payload->xdb-events payload {"id-key" id-key}))
  (xt/for:array [e events]
    (-/sync-event-local xdb e))
  (return {"table" table
           "ids" ids
           "events" events}))

(defn.js attach-events
  "Attaches Supabase Realtime `postgres_changes` listeners and applies each payload to `xdb`."
  {:added "4.1.3"}
  [m]
  (var supabase (xt/x:get-key m "supabase"))
  (var xdb (xt/x:get-key m "xdb"))
  (var schema (xt/x:get-key m "schema"))
  (var lookup (xt/x:get-key m "lookup"))
  (var opts (xt/x:get-key m "opts"))
  (var channel-name (or (xt/x:get-key m "channel-name") "xt.db.supabase"))
  (var bindings (or (xt/x:get-key m "bindings") []))
  (var id-key (or (xt/x:get-key m "id-key") "id"))
  (var on-payload (xt/x:get-key m "on-payload"))
  (var on-applied (xt/x:get-key m "on-applied"))
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
