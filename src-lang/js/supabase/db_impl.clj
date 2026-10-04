(ns js.supabase.db-impl
  (:require [lang.core :as l]
            [js.lib.supabase]
            [xt.db.system.impl-common]
            [xt.db.text.pgrest-graph]
            [xt.lang.common-protocol :as proto :refer [defimpl.xt]]))

(l/script :js
  {:require [[js.lib.supabase :as supabase]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.text.pgrest-graph :as pgrest-graph]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.js pull-async
  "Runs a tree query through the Supabase JavaScript client."
  {:added "4.1"}
  [impl tree]
  (var #{client schema lookup} impl)
  (var request (pgrest-graph/select schema tree {}))
  (var table-name (. request ["table"]))
  (var schema-name (or (xt/x:get-path lookup [table-name "schema"])
                       (. lookup ["schema"])))
  (var source (:? (xt/x:not-nil? schema-name)
                  (. client (schema schema-name))
                  client))
  (var builder (. source (from table-name)))
  (var count? (== (. request ["select"]) "count"))
  (:= builder
      (:? count?
          (. builder (select "*" {"count" "exact" "head" true}))
          (. builder (select (. request ["select"])))))
  (var filters (or (. request ["filters"]) []))
  (when (== (xt/x:len filters) 1)
    (var descriptors (pgrest-graph/compile-clause-into
                      ""
                      (xt/x:first filters)
                      []))
    (xt/for:array [filter descriptors]
      (var column (. filter ["path"]))
      (var op (. filter ["op"]))
      (var value (pgrest-graph/pgrest-resolve-value
                  (. filter ["value"])))
      (cond (== op "eq")
            (:= builder (. builder (eq column value)))
            (== op "neq")
            (:= builder (. builder (neq column value)))
            (== op "gt")
            (:= builder (. builder (gt column value)))
            (== op "gte")
            (:= builder (. builder (gte column value)))
            (== op "lt")
            (:= builder (. builder (lt column value)))
            (== op "lte")
            (:= builder (. builder (lte column value)))
            (== op "like")
            (:= builder (. builder (like column value)))
            (== op "ilike")
            (:= builder (. builder (ilike column value)))
            (== op "is")
            (:= builder (. builder (is column value)))
            (== op "in")
            (:= builder (. builder
                           (in column
                               (pgrest-graph/normalise-in-values value))))
            :else
            (xt/x:err (xt/x:cat "Unsupported Supabase filter operator: " op)))))
  (when (> (xt/x:len filters) 1)
    (var clauses (xt/x:arr-filter
                  (xt/x:arr-map filters pgrest-graph/compile-or-clause)
                  xtd/not-empty?))
    (:= builder (. builder (or (xt/x:str-join "," clauses)))))
  (var order nil)
  (var limit nil)
  (var offset nil)
  (xt/for:array [param (. request ["params"])]
    (cond (. param (startsWith "order="))
          (:= order (. (xt/x:second (. param (split "="))) (split ",")))
          (. param (startsWith "limit="))
          (:= limit (xt/x:second (. param (split "="))))
          (. param (startsWith "offset="))
          (:= offset (xt/x:second (. param (split "="))))))
  (when (xt/x:is-array? order)
    (xt/for:array [entry order]
      (var parts (. entry (split ".")))
      (var suffix (xt/x:last parts))
      (var directed? (or (== suffix "asc")
                         (== suffix "desc")))
      (var column (:? directed?
                    (xt/x:str-join
                     "."
                     (. parts (slice 0 (- (xt/x:len parts) 1))))
                    entry))
      (:= builder
          (. builder (order column {"ascending" (not (== suffix "desc"))})))))
  (when (xt/x:not-nil? offset)
    (when (xt/x:nil? limit)
      (xt/x:err "Supabase-js requires LIMIT when a tree query has OFFSET"))
    (var start (xt/x:to-number offset))
    (var size (xt/x:to-number limit))
    (:= builder (. builder (range start (+ start size -1)))))
  (when (and (xt/x:not-nil? limit)
             (xt/x:nil? offset))
    (:= builder (. builder (limit (xt/x:to-number limit)))))
  (return
   (promise/x:promise-then
    builder
    (fn [response]
      (var error (. response ["error"]))
      (when (xt/x:not-nil? error)
        (xt/x:err (or (. error ["message"])
                      "Supabase query failed")))
      (return (:? count?
                  (. response ["count"])
                  (. response ["data"])))))))

(defn.js rpc-call-async
  "Calls a Supabase RPC and returns its data, propagating SDK errors."
  {:added "4.1"}
  [impl rpc-spec args]
  (var #{client lookup} impl)
  (var schema-name (or (. rpc-spec ["schema"])
                       (xt/x:get-key lookup "schema")))
  (var source (:? (xt/x:not-nil? schema-name)
                  (. client (schema schema-name))
                  client))
  (var body {})
  (var inputs (or (. rpc-spec ["input"]) []))
  (xt/for:array [[index input] inputs]
    (var key (or (. input ["symbol"])
                 (. input ["name"])))
    (when (xt/x:not-nil? key)
      (xt/x:set-key body key (xt/x:get-idx args index))))
  (return
   (promise/x:promise-then
    (. source (rpc (. rpc-spec ["id"]) body))
    (fn [response]
      (var error (. response ["error"]))
      (when (xt/x:not-nil? error)
        (xt/x:err (or (. error ["message"])
                      "Supabase RPC failed")))
      (return (. response ["data"]))))))

(defn.js dispatch-event
  "Routes Supabase broadcast payloads to registered listeners and an optional cache."
  {:added "4.1"}
  [impl event]
  (var payload (or (. event ["payload"]) event))
  (var tables (impl-common/sync-get-tables payload))
  (when (> (xt/x:len tables) 0)
    (impl-common/sync-notify-listeners impl tables payload))
  (var caching-fn (or (xtd/get-in impl ["metadata" "caching_fn"])
                      (xtd/get-in impl ["state" "caching_fn"])))
  (when (xt/x:is-function? caching-fn)
    (var caching-impl (caching-fn))
    (when (xt/x:not-nil? caching-impl)
      (impl-common/sync-process-payload caching-impl payload)))
  (return true))

(defn.js subscribe-db
  "Subscribes one connection id to Supabase broadcast topics."
  {:added "4.1"}
  [impl conn-id topics]
  (var #{client channels connections} impl)
  (var connection (or (xt/x:get-key connections conn-id) {}))
  (xt/x:set-key connections conn-id connection)
  (var ready [])
  (xt/for:array [topic (or topics [])]
    (var entry (xt/x:get-key channels topic))
    (var already? (xt/x:get-key connection topic))
    (when (and already? (xt/x:nil? entry))
      (xt/x:del-key connection topic)
      (:= already? false))
    (when (xt/x:nil? entry)
      (var deferred {"resolve" nil})
      (var init (promise/x:promise-new
                 (fn [resolve reject]
                   (xt/x:set-key deferred "resolve" resolve))))
      (var channel
           (. client
              (channel topic
                       {"config" {"broadcast" {"ack" false "self" false}
                                  "private" true}})))
      (:= entry {"channel" channel
                 "refs" 0
                 "init" init
                 "resolve" (. deferred ["resolve"])
                 "ready" false})
      (xt/x:set-key channels topic entry)
      (var handler (fn [event]
                     (-/dispatch-event impl event)))
      (. channel (on "broadcast" {"event" "xt.db/event"} handler))
      (. channel (on "broadcast" {"event" "db/sync"} handler))
      (. channel (on "broadcast" {"event" "db/remove"} handler))
      (. channel
         (subscribe
          (fn [status error]
            (var resolve (. entry ["resolve"]))
            (if (== status "SUBSCRIBED")
              (do (xt/x:set-key entry "ready" true)
                  (resolve true))
              (do (xt/x:del-key channels topic)
                  (. client (removeChannel channel))
                  (resolve false)))))))
    (when (not already?)
      (xt/x:set-key entry "refs" (+ (. entry ["refs"]) 1))
      (xt/x:set-key connection topic true))
    (xt/x:arr-push ready (. entry ["init"])))
  (return
   (promise/x:promise-then
    (promise/x:promise-all ready)
    (fn [results]
      (return results)))))

(defn.js unsubscribe-db
  "Removes one connection's topic references and tears down unused channels."
  {:added "4.1"}
  [impl conn-id topics]
  (var #{client channels connections} impl)
  (var connection (xt/x:get-key connections conn-id))
  (var removals [])
  (when (xt/x:is-object? connection)
    (xt/for:array [topic (or topics [])]
      (when (xt/x:get-key connection topic)
        (xt/x:del-key connection topic)
        (var entry (xt/x:get-key channels topic))
        (when (xt/x:is-object? entry)
          (var refs (- (. entry ["refs"]) 1))
          (xt/x:set-key entry "refs" refs)
          (when (<= refs 0)
            (xt/x:del-key channels topic)
            (xt/x:arr-push removals
                           (. client (removeChannel (. entry ["channel"]))))))))
    (when (== (xt/x:len (xt/x:obj-keys connection)) 0)
      (xt/x:del-key connections conn-id)))
  (return
   (promise/x:promise-then
    (promise/x:promise-all removals)
    (fn [_]
      (return true)))))

(defn.js stop-db
  "Idempotently removes all owned Supabase channels and clears local state."
  {:added "4.1"}
  [impl]
  (var #{client channels listeners} impl)
  (var removals [])
  (xt/for:object [[_ topic] channels]
    (xt/x:arr-push removals
                   (. client (removeChannel (. topic ["channel"])))))
  (xt/x:set-key impl "channels" {})
  (xt/x:set-key impl "connections" {})
  (xt/x:set-key impl "listeners" {})
  (return
   (promise/x:promise-then
    (promise/x:promise-all removals)
    (fn [_]
      (return true)))))

(defimpl.xt ^{:lang :js} ImplSupabaseJS
  [client schema lookup channels connections listeners metadata]
  impl-common/ISourceRemote
  {impl-common/pull-async     -/pull-async
   impl-common/rpc-call-async -/rpc-call-async}

  impl-common/ISourceListener
  {impl-common/add-db-listener     impl-common/add-db-listener-default
   impl-common/remove-db-listener  impl-common/remove-db-listener-default
   impl-common/get-db-listener     impl-common/get-db-listener-default}

  impl-common/ISourceRealtime
  {impl-common/subscribe-db   -/subscribe-db
   impl-common/unsubscribe-db -/unsubscribe-db}

  impl-common/ISourceLifecycle
  {impl-common/stop-db -/stop-db})

(defn.js ^{:public true} impl-supabase-js
  "Constructs xt.db protocols around a caller-configured supabase-js client."
  [client schema lookup]
  (return (-/ImplSupabaseJS client schema lookup {} {} {} {})))

(defn.js ^{:public true} create
  "Creates a supabase-js client and wraps it with xt.db source protocols."
  [url key schema lookup options]
  (var client (supabase/createSupabaseClient url key options))
  (return (-/impl-supabase-js client schema lookup)))
