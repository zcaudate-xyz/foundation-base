(ns xt.db.runtime.event-supabase
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.runtime.event-common :as event-common]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.xt client?
  "checks if a value is a wrapped supabase realtime client descriptor"
  {:added "4.1.4"}
  [obj]
  (return (event-common/client? obj "supabase.realtime.client")))

(defn.xt raw-client
  "unwraps the tagged realtime client descriptor"
  {:added "4.1.4"}
  [client]
  (return (event-common/raw-client client "supabase.realtime.client")))

(defn.xt resolve-transport
  "resolves a websocket transport as a standard websocket driver or client"
  {:added "4.1.4"}
  [client]
  (return (event-common/resolve-transport client
                                          "supabase.realtime.client"
                                          "Supabase realtime")))

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
  (return (event-common/trim-trailing-slash s)))

(defn.xt derive-websocket-url
  "derives the Supabase realtime websocket endpoint from the base api url"
  {:added "4.1.4"}
  [base-url]
  (when (or (xt/x:nil? base-url)
            (not (xt/x:is-string? base-url)))
    (return nil))
  (:= base-url (event-common/trim-trailing-slash base-url))
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
  (return (event-common/encode-query-params params)))

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
    (return (:? (str/starts-with? topic "realtime:")
                topic
                (xt/x:cat "realtime:" topic))))
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
  (when (or (xt/x:not-nil? (xt/x:get-key opts "filter"))
            (xt/x:not-nil? (xt/x:get-key raw-client "filter")))
    (return [(-/normalize-filter
              (or (xt/x:get-key opts "filter")
                  (xt/x:get-key raw-client "filter")
                  {})
              source
              opts)]))
  (when (or (xt/x:not-nil? (xt/x:get-key opts "table_name"))
            (xt/x:not-nil? (xt/x:get-key raw-client "table_name")))
    (return [(-/normalize-filter {} source opts)]))
  (return nil))

(defn.xt resolve-ref
  "resolves a string reference id for channel frames"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (xt/x:to-string
           (or (xt/x:get-key opts "ref")
               (xt/x:get-key raw-client "ref")
               (xt/x:now-ms)))))

(defn.xt resolve-message-event
  "resolves the inbound event name to handle on the subscribed topic"
  {:added "4.1.4"}
  [source opts]
  (var raw-client (-/raw-client source))
  (return (or (xt/x:get-key opts "message_event")
              (xt/x:get-key raw-client "message_event")
              "postgres_changes")))

(defn.xt resolve-request-transform
  "resolves an optional payload->request transform for custom topic events"
  {:added "4.1.4"}
  [source opts]
  (return (event-common/resolve-request-transform
           source
           opts
           "supabase.realtime.client")))

(defn.xt broadcast-client
  "Builds a broadcast-oriented realtime client descriptor."
  {:added "4.1.4"}
  [client opts]
  (var config (xt/x:obj-assign {} (or client {})))
  (var extra (or opts {}))
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign config extra)
    {"message_event" "broadcast"})))

(defn.xt join-payload
  "creates the phoenix join payload for a realtime subscription"
  {:added "4.1.4"}
  [db client opts]
  (var scaffold (-/create-scaffold db client opts))
  (var filters (-/resolve-filters client opts))
  (var payload
       {"config" {"broadcast" {"ack" false
                               "self" false}
                  "presence" {"key" ""}}})
  (when (xt/x:not-nil? filters)
    (xtd/set-in payload ["config" "postgres_changes"] filters))
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
  (var source (event-common/resolve-client-source db opts))
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
   - ref
   - message_event
   - request_transform"
  {:added "4.1.4"}
  [raw]
  (return (event-common/wrap-client raw "supabase.realtime.client")))

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
  (return (event-common/extract-message-data message)))

(defn.xt decode-message
  "decodes a websocket message into a realtime frame"
  {:added "4.1.4"}
  [message]
  (return (event-common/decode-message message {})))

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
  (return (event-common/apply-request local-db request opts)))

(defn.xt apply-postgres-change
  "converts and applies a realtime postgres_changes payload"
  {:added "4.1.4"}
  [local-db payload source opts]
  (var request (-/postgres-change->sync-request payload source opts))
  (when request
    (-/apply-sync-request local-db request source opts))
  (return [true request]))

(defn.xt payload->request
  "normalizes inbound realtime payloads into xt.db requests"
  {:added "4.1.4"}
  [payload source opts]
  (cond (event-common/request? payload)
        (return (event-common/unwrap-request payload))

        (xt/x:is-function? (-/resolve-request-transform source opts))
        (return ((-/resolve-request-transform source opts) payload source opts))

        (== "postgres_changes" (-/resolve-message-event source opts))
        (return (-/postgres-change->sync-request payload source opts))

        :else
        (return nil)))

(defn.xt apply-request
  "applies a normalized xt.db request to the local cache db"
  {:added "4.1.4"}
  [local-db payload source opts]
  (var request (-/payload->request payload source opts))
  (when request
    (event-common/apply-request local-db request opts))
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
  (var message-event (-/resolve-message-event source opts))
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
             (== message-event (xt/x:get-key frame "event")))
        (do (var payload (or (xtd/get-in frame ["payload" "data"])
                             (xt/x:get-key frame "payload")))
            (var local-db (xt/x:get-key subscription "local_db"))
            (var [ok request] (-/apply-request local-db payload source opts))
            (when (xt/x:is-function? on-request)
              (on-request request payload frame))
            (return request))

        :else
        (return frame)))

(defn.xt subscribe
  "subscribes a local xt.db cache to Supabase realtime events"
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

(defn.xt subscribe-broadcast
  "Subscribes a local xt.db cache to a Supabase broadcast topic carrying native xt.db requests."
  {:added "4.1.4"}
  [source local-db opts]
  (var input (or source {}))
  (var config (or opts {}))
  (var client (or (xt/x:get-key input "client")
                  input))
  (return
   (-/subscribe
    {"client" (-/broadcast-client client config)}
    local-db
    config)))

(defn.xt subscription?
  "checks if the object is a realtime subscription handle"
  {:added "4.1.4"}
  [obj]
  (return (event-common/subscription? obj
                                      "db.supabase.realtime.subscription")))

(defn.xt unsubscribe
  "tears down a realtime subscription handle"
  {:added "4.1.4"}
  [subscription]
  (when (not (-/subscription? subscription))
    (return (promise/x:promise-run nil)))
  (var socket (xt/x:get-key subscription "socket"))
  (xt/x:set-key subscription "active" false)
  (return
   (promise/x:promise-then
    (ws/send socket
             (xt/x:json-encode
              (xt/x:get-key subscription "leave_frame")))
    (fn [_]
      (return (event-common/unsubscribe subscription
                                        "db.supabase.realtime.subscription"
                                        "event-supabase/unsubscribe"))))))
