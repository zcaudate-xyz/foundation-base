(ns xt.db.runtime.supabase-realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.runtime :as db-runtime]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.xt client?
  "checks if a value is a wrapped supabase realtime client descriptor"
  {:added "4.1.4"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "supabase.realtime.client"
                   (xt/x:get-key obj "::")))))

(defn.xt raw-client
  "unwraps the tagged realtime client descriptor"
  {:added "4.1.4"}
  [client]
  (if (-/client? client)
    (return (or (xt/x:get-key client "_raw") {}))
    (return (or client {}))))

(defn.xt resolve-transport
  "resolves a websocket transport as a standard websocket driver or client"
  {:added "4.1.4"}
  [client]
  (var raw-client (-/raw-client client))
  (var transport-source
       (xt/x:get-key raw-client "transport"))
  (cond (ws/client? transport-source)
        (return transport-source)

        (ws/driver? transport-source)
        (return transport-source)

        (xt/x:is-function? transport-source)
        (return (ws/driver-create {"connect" transport-source}))

        (or (xt/x:is-function? (xt/x:get-key transport-source "connect"))
            (xt/x:is-function? (xt/x:get-key transport-source "connect_sync")))
        (return (ws/driver-create transport-source))

        :else
        (xt/x:err "Supabase realtime missing websocket transport")))

(defn.xt resolve-base-url
  "resolves the base Supabase http url"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key raw-client "base_url")
              (xt/x:get-key opts "base_url")
              nil)))

(defn.xt resolve-websocket-url
  "resolves the websocket url override"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key raw-client "websocket_url")
              (xt/x:get-key opts "websocket_url")
              nil)))

(defn.xt resolve-schema-name
  "resolves the realtime schema name"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (or (xt/x:get-key raw-client "schema_name")
              (xt/x:get-key opts "schema_name")
              "public")))

(defn.xt resolve-table-name
  "resolves the realtime table name"
  {:added "4.1.4"}
  [payload source opts]
  (var raw-client (-/raw-client source))
  (var table (or (xt/x:get-key opts "table_name")
                 (xt/x:get-key raw-client "table_name")
                 (xt/x:get-key payload "table")
                 nil))
  (when (xt/x:nil? table)
    (xt/x:err "Supabase realtime missing table_name"))
  (return table))

(defn.xt resolve-id-key
  "resolves the primary key name"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (or (xt/x:get-key opts "id_key")
              (xt/x:get-key raw-client "id_key")
              "id")))

(defn.xt resolve-event
  "resolves the realtime event selector"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (or (xt/x:get-key opts "event")
              (xt/x:get-key raw-client "event")
              "*")))

(defn.xt resolve-api-key
  "resolves the api key for websocket auth"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key raw-client "api_key")
              (xt/x:get-key opts "api_key")
              nil)))

(defn.xt resolve-auth-token
  "resolves the bearer token for join payload auth"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key raw-client "auth_token")
              (xt/x:get-key opts "auth_token")
              nil)))

(defn.xt resolve-params
  "resolves websocket query params"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign
     {"vsn" "1.0.0"}
     (or (xt/x:get-key raw-client "params") {}))
    (or (xt/x:get-key opts "params") {}))))

(defn.xt create-scaffold
  "creates the scaffold used to connect to Supabase realtime"
  {:added "4.1.4"}
  [db client opts]
  (return {"client" client
           "base_url" (-/resolve-base-url db client opts)
           "websocket_url" (-/resolve-websocket-url db client opts)
           "schema_name" (-/resolve-schema-name client opts)
           "api_key" (-/resolve-api-key db client opts)
           "auth_token" (-/resolve-auth-token db client opts)
           "params" (-/resolve-params db client opts)}))

(defn.xt trim-trailing-slash
  "trims a single trailing slash"
  {:added "4.1.4"}
  [s]
  (if (and (xt/x:is-string? s)
           (str/ends-with? s "/"))
    (return (xt/x:str-substring s 0 (- (xt/x:str-len s) 1)))
    (return s)))

(defn.xt derive-websocket-url
  "derives the Supabase realtime websocket endpoint from the base api url"
  {:added "4.1.4"}
  [base-url]
  (when (or (xt/x:nil? base-url)
            (not (xt/x:is-string? base-url)))
    (return nil))
  (:= base-url (-/trim-trailing-slash base-url))
  (when (str/ends-with? base-url "/rest/v1")
    (:= base-url (xt/x:str-substring base-url 0 (- (xt/x:str-len base-url) 8))))
  (cond (str/starts-with? base-url "https://")
        (:= base-url (xt/x:cat "wss://"
                               (xt/x:str-substring base-url 8)))

        (str/starts-with? base-url "http://")
        (:= base-url (xt/x:cat "ws://"
                               (xt/x:str-substring base-url 7))))
  (return (xt/x:cat base-url "/realtime/v1/websocket")))

(defn.xt encode-query-params
  "encodes a flat query param map"
  {:added "4.1.4"}
  [params]
  (var out [])
  (xt/for:object [[k v] (or params {})]
    (when (xt/x:not-nil? v)
      (xt/x:arr-push out (xt/x:cat k "=" (xt/x:to-string v)))))
  (return (xt/x:str-join "&" out)))

(defn.xt prepare-connect-url
  "prepares the websocket url used to connect to realtime"
  {:added "4.1.4"}
  [db client opts]
  (var scaffold (-/create-scaffold db client opts))
  (var base-url (or (xt/x:get-key scaffold "websocket_url")
                    (-/derive-websocket-url (xt/x:get-key scaffold "base_url"))))
  (when (xt/x:nil? base-url)
    (xt/x:err "Supabase realtime missing websocket_url/base_url"))
  (var params (xt/x:obj-assign {}
                               (xt/x:get-key scaffold "params")))
  (when (xt/x:not-nil? (xt/x:get-key scaffold "api_key"))
    (xt/x:set-key params "apikey" (xt/x:get-key scaffold "api_key")))
  (var query (-/encode-query-params params))
  (if (== query "")
    (return base-url)
    (return (xt/x:cat base-url "?" query))))

(defn.xt resolve-topic
  "resolves the realtime channel topic"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (var topic (or (xt/x:get-key opts "topic")
                 (xt/x:get-key raw-client "topic")
                 nil))
  (when (xt/x:not-nil? topic)
    (return topic))
  (return (xt/x:cat "realtime:"
                    (-/resolve-schema-name source opts)
                    ":"
                    (-/resolve-table-name {} source opts))))

(defn.xt normalize-filter
  "normalizes a postgres_changes filter"
  {:added "4.1.4"}
  [filter source opts]
  (return
   (xt/x:obj-assign
    {"event" (-/resolve-event source opts)
    "schema" (-/resolve-schema-name source opts)
    "table" (-/resolve-table-name {} source opts)}
    (or filter {}))))

(defn.xt resolve-filters
  "resolves one or more postgres_changes filters"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (var filters (or (xt/x:get-key opts "filters")
                   (xt/x:get-key raw-client "filters")
                   nil))
  (when (xt/x:is-array? filters)
    (return (xt/x:arr-map filters
                          (fn [filter]
                            (return (-/normalize-filter filter source opts))))))
  (return [(-/normalize-filter {} source opts)]))

(defn.xt resolve-ref
  "resolves a string reference id for channel frames"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (xt/x:to-string
           (or (xt/x:get-key opts "ref")
               (xt/x:get-key raw-client "ref")
               (xt/x:now-ms)))))

(defn.xt join-payload
  "creates the phoenix join payload for a realtime subscription"
  {:added "4.1.4"}
  [db client opts]
  (var scaffold (-/create-scaffold db client opts))
  (var filters (-/resolve-filters client opts))
  (var payload
       {"config" {"broadcast" {"ack" false
                               "self" false}
                  "presence" {"key" ""}
                  "postgres_changes" filters}})
  (when (xt/x:not-nil? (xt/x:get-key scaffold "auth_token"))
    (xt/x:set-key payload
                  "access_token"
                  (xt/x:get-key scaffold "auth_token")))
  (return (xt/x:obj-assign
           payload
           (or (xt/x:get-key opts "join_payload")
               {}))))

(defn.xt join-frame
  "creates the phoenix join frame"
  {:added "4.1.4"}
  [db client opts]
  (var ref (-/resolve-ref client opts))
  (return {"topic" (-/resolve-topic client opts)
           "event" "phx_join"
           "payload" (-/join-payload db client opts)
           "ref" ref
           "join_ref" ref}))

(defn.xt leave-frame
  "creates the phoenix leave frame"
  {:added "4.1.4"}
  [client opts]
  (var ref (-/resolve-ref client opts))
  (return {"topic" (-/resolve-topic client opts)
           "event" "phx_leave"
           "payload" {}
           "ref" ref
           "join_ref" ref}))

(defn.xt resolve-client
  "resolves the supabase realtime client descriptor from db or opts"
  {:added "4.1.4"}
  [db opts]
  (var source (or (xt/x:get-key db "client")
                  (xt/x:get-key opts "client")
                  (xt/x:get-key db "transport")
                  (xt/x:get-key opts "transport")
                  nil))
  (when (xt/x:nil? source)
    (xt/x:err "Supabase realtime missing client"))
  (if (-/client? source)
    (return source)
    (return (-/client source))))

(defn.xt client
  "wraps raw realtime config into the standard supabase realtime client descriptor.

   standard config keys:
   - transport
   - base_url
   - websocket_url
   - schema_name
   - table_name
   - id_key
   - event
   - params
   - filters
   - topic
   - ref"
  {:added "4.1.4"}
  [raw]
  (when (-/client? raw)
    (return raw))
  (var source nil)
  (cond (or (ws/client? raw)
            (ws/driver? raw))
        (:= source {"transport" raw})

        (xt/x:nil? raw)
        (:= source {})

        :else
        (:= source (xt/x:obj-clone raw)))
  (return {"::" "supabase.realtime.client"
           "_raw" source}))

(defn.xt connect
  "connects the realtime client through the websocket protocol"
  {:added "4.1.4"}
  [db client opts]
  (var transport (-/resolve-transport client))
  (if (ws/client? transport)
    (return (promise/x:promise-run transport))
    (return (ws/connect transport
                        (-/prepare-connect-url db client opts)))))

(defn.xt extract-message-data
  "extracts websocket message data from raw events or payload strings"
  {:added "4.1.4"}
  [message]
  (cond (xt/x:is-string? message)
        (return message)

        (xt/x:not-nil? (xt/x:get-key message "data"))
        (return (xt/x:get-key message "data"))

        (xt/x:not-nil? (xt/x:get-key message "body"))
        (return (xt/x:get-key message "body"))

        :else
        (return message)))

(defn.xt decode-message
  "decodes a websocket message into a realtime frame"
  {:added "4.1.4"}
  [message]
  (var data (-/extract-message-data message))
  (if (xt/x:is-string? data)
    (return (xt/x:json-decode data))
    (return data)))

(defn.xt payload-event-type
  "normalizes the postgres_changes event type"
  {:added "4.1.4"}
  [payload]
  (return
   (xt/x:str-to-upper
    (or (xt/x:get-key payload "eventType")
        (xt/x:get-key payload "event_type")
        (xt/x:get-key payload "type")
        ""))))

(defn.xt payload-row
  "gets the row payload for an event"
  {:added "4.1.4"}
  [payload]
  (var event-type (-/payload-event-type payload))
  (return
   (:? (== event-type "DELETE")
       (or (xt/x:get-key payload "old_record")
           (xt/x:get-key payload "old")
           {})
       (or (xt/x:get-key payload "record")
           (xt/x:get-key payload "new")
           {}))))

(defn.xt payload-id
  "extracts the primary id from a realtime payload"
  {:added "4.1.4"}
  [payload source opts]
  (var id-key (-/resolve-id-key source opts))
  (var row (-/payload-row payload))
  (return (or (xt/x:get-key payload id-key)
              (xtd/get-in payload ["old_record" id-key])
              (xtd/get-in payload ["record" id-key])
              (xtd/get-in payload ["old" id-key])
              (xtd/get-in payload ["new" id-key])
              (xt/x:get-key row id-key)
              nil)))

(defn.xt postgres-change->sync-request
  "converts a Supabase postgres_changes payload into xt.db sync requests"
  {:added "4.1.4"}
  [payload source opts]
  (var event-type (-/payload-event-type payload))
  (when (== event-type "")
    (return nil))
  (var table (-/resolve-table-name payload source opts))
  (var row-id (-/payload-id payload source opts))
  (cond (== event-type "DELETE")
        (do (when (xt/x:nil? row-id)
              (return nil))
            (return {"db/remove" {table [row-id]}}))

        :else
        (do (var row (xtd/clone-nested (-/payload-row payload)))
            (when (xt/x:nil? row)
              (return nil))
            (xt/x:set-key row "__deleted__" false)
            (return {"db/sync" {table [row]}}))))

(defn.xt apply-sync-request
  "applies an xt.db sync request to a local db"
  {:added "4.1.4"}
  [local-db request source opts]
  (when (xt/x:not-nil? (xt/x:get-key request "db/sync"))
    (db-runtime/sync-event local-db
                           ["add" (xt/x:get-key request "db/sync")]))
  (when (xt/x:not-nil? (xt/x:get-key request "db/remove"))
    (var schema (or (xt/x:get-key opts "schema")
                    (xt/x:get-key local-db "schema")
                    nil))
    (if (xt/x:not-nil? schema)
      (xt/for:object [[table ids] (xt/x:get-key request "db/remove")]
        (db-runtime/db-delete-sync local-db schema table ids))
      (db-runtime/sync-event local-db
                             ["remove" (xt/x:get-key request "db/remove")])))
  (return request))

(defn.xt apply-postgres-change
  "converts and applies a realtime postgres_changes payload"
  {:added "4.1.4"}
  [local-db payload source opts]
  (var request (-/postgres-change->sync-request payload source opts))
  (when request
    (-/apply-sync-request local-db request source opts))
  (return [true request]))

(defn.xt handle-frame
  "handles inbound realtime websocket frames"
  {:added "4.1.4"}
  [subscription message]
  (var frame (-/decode-message message))
  (var topic (xt/x:get-key subscription "topic"))
  (var source (xt/x:get-key subscription "client"))
  (var opts (or (xt/x:get-key subscription "opts") {}))
  (var on-status (xt/x:get-key subscription "on_status"))
  (var on-request (xt/x:get-key subscription "on_request"))
  (cond (and (== topic (xt/x:get-key frame "topic"))
             (== "phx_reply" (xt/x:get-key frame "event")))
        (do (when (xt/x:is-function? on-status)
              (var status (xtd/get-in frame ["payload" "status"]))
              (when (== status "ok")
                (on-status "SUBSCRIBED" frame))
              (when (and (xt/x:not-nil? status)
                         (not (== status "ok")))
                (on-status status frame)))
            (return frame))

        (and (== topic (xt/x:get-key frame "topic"))
             (== "postgres_changes" (xt/x:get-key frame "event")))
        (do (var payload (or (xtd/get-in frame ["payload" "data"])
                             (xt/x:get-key frame "payload")))
            (var local-db (xt/x:get-key subscription "local_db"))
            (var [ok request] (-/apply-postgres-change local-db payload source opts))
            (when (xt/x:is-function? on-request)
              (on-request request payload frame))
            (return request))

        :else
        (return frame)))

(defn.xt subscribe
  "subscribes a local xt.db cache to Supabase realtime postgres_changes"
  {:added "4.1.4"}
  [source local-db opts]
  (:= source (or source {}))
  (:= opts (or opts {}))
  (var client (-/resolve-client source opts))
  (var topic (-/resolve-topic client opts))
  (var on-status (or (xt/x:get-key opts "on_status")
                     nil))
  (var on-request (or (xt/x:get-key opts "on_request")
                      nil))
  (return
   (promise/x:promise-then
    (-/connect nil client opts)
    (fn [socket]
      (var subscription {"::" "db.supabase.realtime.subscription"
                         "client" client
                         "socket" socket
                         "topic" topic
                         "filters" (-/resolve-filters client opts)
                         "join_frame" (-/join-frame nil client opts)
                         "leave_frame" (-/leave-frame client opts)
                         "local_db" local-db
                         "opts" opts
                         "on_status" on-status
                         "on_request" on-request})
      (return
       (promise/x:promise-then
        (ws/add-listener
         socket
         "message"
         (fn [message]
           (return (-/handle-frame subscription message))))
        (fn [_]
          (return
           (promise/x:promise-then
            (ws/send socket
                     (xt/x:json-encode
                      (xt/x:get-key subscription "join_frame")))
            (fn [_]
              (return subscription)))))))))))

(defn.xt subscription?
  "checks if the object is a realtime subscription handle"
  {:added "4.1.4"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "db.supabase.realtime.subscription"
                   (xt/x:get-key obj "::")))))

(defn.xt unsubscribe
  "tears down a realtime subscription handle"
  {:added "4.1.4"}
  [subscription]
  (when (not (-/subscription? subscription))
    (return (promise/x:promise-run nil)))
  (var socket (xt/x:get-key subscription "socket"))
  (return
   (promise/x:promise-then
    (ws/send socket
             (xt/x:json-encode
              (xt/x:get-key subscription "leave_frame")))
    (fn [_]
      (return (ws/disconnect socket 1000 "supabase-realtime/unsubscribe"))))))
