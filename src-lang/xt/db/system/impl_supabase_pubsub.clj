(ns xt.db.system.impl-supabase-pubsub
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(defn.xt client-base-url
  "derives the http base url from the supabase http client defaults"
  {:added "4.1"}
  [client]
  (var defaults (or (xt/x:get-key client "defaults") {}))
  (var secured (xt/x:get-key defaults "secured"))
  (var host (xt/x:get-key defaults "host"))
  (var port (xt/x:get-key defaults "port"))
  (when (xt/x:nil? host)
    (return nil))
  (return (xt/x:cat (:? secured "https" "http")
                    "://"
                    host
                    (:? (xt/x:not-nil? port)
                        (xt/x:cat ":" port)
                        ""))))

(defn.xt resolve-api-key
  "resolves the api key for websocket auth"
  {:added "4.1"}
  [impl opts]
  (return (or (xt/x:get-key opts "api_key")
              (xt/x:get-key opts "apikey")
              (xt/x:get-key (or (xt/x:get-key (xt/x:get-key impl "client") "defaults") {}) "apikey")
              nil)))

(defn.xt resolve-auth-token
  "resolves the realtime auth token from the impl session or client defaults"
  {:added "4.1"}
  [impl opts]
  (return (or (xt/x:get-key opts "auth_token")
              (xt/x:get-key opts "token")
              (xt/x:get-key opts "access_token")
              (xt/x:get-key (xt/x:get-key (xt/x:get-key impl "state") "session") "access_token")
              (xt/x:get-key (or (xt/x:get-key (xt/x:get-key impl "client") "defaults") {}) "token")
              nil)))

(defn.xt trim-trailing-slash
  [s]
  (if (and (xt/x:is-string? s)
           (str/ends-with? s "/"))
    (return (xt/x:str-substring s 0 (- (xt/x:str-len s) 1)))
    (return s)))

(defn.xt derive-websocket-url
  "derives the Supabase realtime websocket endpoint from the base api url"
  {:added "4.1"}
  [base-url]
  (when (or (xt/x:nil? base-url)
            (not (xt/x:is-string? base-url)))
    (return nil))
  (:= base-url (-/trim-trailing-slash base-url))
  (when (str/ends-with? base-url "/rest/v1")
    (:= base-url (xt/x:str-substring base-url 0 (- (xt/x:str-len base-url) 8))))
  (when (str/starts-with? base-url "https://")
    (:= base-url (xt/x:cat "wss://"
                           (xt/x:str-substring base-url 8))))
  (when (str/starts-with? base-url "http://")
    (:= base-url (xt/x:cat "ws://"
                           (xt/x:str-substring base-url 7))))
  (return (xt/x:cat base-url "/realtime/v1/websocket")))

(defn.xt encode-query-params
  "encodes a flat query param map"
  {:added "4.1"}
  [params]
  (var parts [])
  (xt/for:object [[k v] params]
    (xt/x:arr-push parts
                   (xt/x:cat (encodeURIComponent k)
                             "="
                             (encodeURIComponent v))))
  (return (str/join parts "&")))

(defn.xt prepare-connect-url
  "prepares the websocket url used to connect to realtime"
  {:added "4.1"}
  [config]
  (var base-url (or (xt/x:get-key config "websocket_url")
                    (-/derive-websocket-url (xt/x:get-key config "base_url"))))
  (when (xt/x:nil? base-url)
    (xt/x:err "Supabase realtime missing websocket_url/base_url"))
  (var params (xt/x:obj-assign {"vsn" "2.0.0"}
                               (or (xt/x:get-key config "params") {})))
  (when (xt/x:not-nil? (xt/x:get-key config "api_key"))
    (xt/x:set-key params "apikey" (xt/x:get-key config "api_key")))
  (var query (-/encode-query-params params))
  (if (== query "")
    (return base-url)
    (return (xt/x:cat base-url "?" query))))

(defn.xt realtime-config
  "builds a raw supabase realtime client config from the impl and options"
  {:added "4.1"}
  [impl topic opts]
  (var client (xt/x:get-key impl "client"))
  (var defaults (or (xt/x:get-key client "defaults") {}))
  (var config {})
  (xt/x:set-key config "base_url"
                (or (xt/x:get-key opts "base_url")
                    (-/client-base-url client)))
  (xt/x:set-key config "websocket_url"
                (or (xt/x:get-key opts "websocket_url")
                    (xt/x:get-key opts "ws_url")))
  (xt/x:set-key config "api_key" (-/resolve-api-key impl opts))
  (xt/x:set-key config "auth_token" (-/resolve-auth-token impl opts))
  (xt/x:set-key config "schema_name"
                (or (xt/x:get-key opts "schema_name")
                    "public"))
  (xt/x:set-key config "event"
                (or (xt/x:get-key opts "event")
                    "*"))
  (xt/x:set-key config "id_key"
                (or (xt/x:get-key opts "id_key")
                    "id"))
  (xt/x:set-key config "table_name"
                (or (xt/x:get-key opts "table_name")
                    (:? (xt/x:is-string? topic)
                        topic
                        nil)))
  (xt/x:set-key config "topic"
                (or (xt/x:get-key opts "topic")
                    (:? (and (xt/x:is-string? topic)
                             (str/starts-with? topic "realtime:"))
                        topic
                        nil)))
  (xt/x:set-key config "filters"
                (xt/x:obj-clone (xt/x:get-key opts "filters")))
  (xt/x:set-key config "filter"
                (xt/x:obj-clone (xt/x:get-key opts "filter")))
  (xt/x:set-key config "params"
                (xt/x:obj-clone (xt/x:get-key opts "params")))
  (xt/x:set-key config "join_payload"
                (xt/x:obj-clone (xt/x:get-key opts "join_payload")))
  (var websocket (or (xt/x:get-key opts "websocket")
                     (xt/x:get-key opts "transport")
                     (:? (xt/x:is-function? WebSocket)
                         WebSocket
                         nil)))
  (when (xt/x:not-nil? websocket)
    (xt/x:set-key config "websocket" websocket))
  (return config))

(defn.xt resolve-topic
  "resolves the realtime channel topic"
  {:added "4.1"}
  [config]
  (var explicit-topic (xt/x:get-key config "topic"))
  (when (xt/x:not-nil? explicit-topic)
    (return explicit-topic))
  (var schema (xt/x:get-key config "schema_name"))
  (var table (xt/x:get-key config "table_name"))
  (when (xt/x:nil? table)
    (xt/x:err "Supabase realtime missing table_name/topic"))
  (return (xt/x:cat "realtime:" schema ":" table)))

(defn.xt normalize-filter
  "normalizes a postgres_changes filter"
  {:added "4.1"}
  [filter config]
  (return
   (xt/x:obj-assign
    {"event" (xt/x:get-key config "event")
     "schema" (xt/x:get-key config "schema_name")
     "table" (xt/x:get-key config "table_name")}
    (or filter {}))))

(defn.xt resolve-filters
  "resolves one or more postgres_changes filters"
  {:added "4.1"}
  [config]
  (var filters (xt/x:get-key config "filters"))
  (var filter (xt/x:get-key config "filter"))
  (when (xt/x:is-array? filters)
    (return (xt/x:arr-map filters
                          (fn [f]
                            (return (-/normalize-filter f config))))))
  (when (xt/x:not-nil? filter)
    (return [(-/normalize-filter filter config)]))
  (when (xt/x:not-nil? (xt/x:get-key config "table_name"))
    (return [(-/normalize-filter {} config)]))
  (return nil))

(defn.xt join-payload
  "creates the phoenix join payload for a realtime subscription"
  {:added "4.1"}
  [config]
  (var filters (-/resolve-filters config))
  (var payload
       {"config" {"broadcast" {"ack" false
                               "self" false}
                  "presence" {"key" ""}}})
  (when (xt/x:not-nil? filters)
    (xtd/set-in payload ["config" "postgres_changes"] filters))
  (when (xt/x:not-nil? (xt/x:get-key config "auth_token"))
    (xt/x:set-key payload
                  "access_token"
                  (xt/x:get-key config "auth_token")))
  (return (xt/x:obj-assign
           payload
           (or (xt/x:get-key config "join_payload") {}))))

(defn.xt payload-event-type
  "normalizes the postgres_changes event type"
  {:added "4.1"}
  [payload]
  (return
   (xt/x:str-to-upper
    (or (xt/x:get-key payload "eventType")
        (xt/x:get-key payload "event_type")
        (xt/x:get-key payload "type")
        ""))))

(defn.xt payload-row
  "gets the row payload for an event"
  {:added "4.1"}
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
  {:added "4.1"}
  [payload config]
  (var id-key (xt/x:get-key config "id_key"))
  (var row (-/payload-row payload))
  (return (or (xt/x:get-key payload id-key)
              (xtd/get-in payload ["old_record" id-key])
              (xtd/get-in payload ["record" id-key])
              (xtd/get-in payload ["old" id-key])
              (xtd/get-in payload ["new" id-key])
              (xt/x:get-key row id-key)
              nil)))

(defn.xt postgres-change->event
  "converts a Supabase postgres_changes payload into an xt.db event vector"
  {:added "4.1"}
  [payload config]
  (var event-type (-/payload-event-type payload))
  (when (== event-type "")
    (return nil))
  (var table (xt/x:get-key config "table_name"))
  (var row-id (-/payload-id payload config))
  (cond (== event-type "DELETE")
        (do (when (xt/x:nil? row-id)
              (return nil))
            (return ["remove" {table [row-id]}]))

        :else
        (do (var row (xtd/clone-nested (-/payload-row payload)))
            (when (xt/x:nil? row)
              (return nil))
            (xt/x:set-key row "__deleted__" false)
            (return ["add" {table [row]}]))))

(defn.xt subscribe
  "subscribes the supabase impl to a realtime topic and emits xt.db events"
  {:added "4.1"}
  [impl topic opts callback]
  (:= opts (or opts {}))
  (var config (-/realtime-config impl topic opts))
  (var ws-url (-/prepare-connect-url config))
  (var client {"::" "js.net.ws_native/http_websocket_client"
               "defaults" (xt/x:obj-assign config {"url" ws-url})
               "heartbeats" {}})
  (websocket/connect client {})
  (var resolved-topic (-/resolve-topic config))
  (var join-frame (phoenix/make-frame-join client
                                           (-/join-payload config)
                                           {"topic" resolved-topic}))
  (var leave-frame (phoenix/make-frame-leave client
                                             {"topic" resolved-topic}))
  (var state (xt/x:get-key impl "state"))
  (var id-counter (xt/x:get-key state "id_counter"))
  (var id (xt/x:cat "sub-" id-counter))
  (xt/x:set-key state "id_counter" (+ id-counter 1))
  (var handle {"client" client
               "topic" resolved-topic
               "id" id
               "callback" callback
               "config" config
               "leave_frame" leave-frame
               "active" true})
  (var on-status (xt/x:get-key opts "on_status"))
  (var message-handler
       (phoenix/wrap-phoenix
        {"phx_reply"
         (fn [frame]
           (var status (xtd/get-in frame ["payload" "status"]))
           (when (and (== status "ok") (xt/x:is-function? on-status))
             (on-status "SUBSCRIBED" frame))
           (return frame))
         "postgres_changes"
         (fn [frame]
           (var payload (or (xtd/get-in frame ["payload" "data"])
                            (xt/x:get-key frame "payload")))
           (var event (-/postgres-change->event payload config))
           (when (and (xt/x:not-nil? event) (xt/x:is-function? callback))
             (callback event))
           (return event))}))
  (websocket/add-listeners client {"message" message-handler})
  (websocket/add-listeners client
                           {"open"
                            (fn [_event]
                              (phoenix/send-frame client join-frame)
                              (var interval (or (xt/x:get-key opts "heartbeat_interval")
                                                30000))
                              (http-fetch/start-heartbeat client id
                                                          (fn [client name]
                                                            (phoenix/send-heartbeat client {}))
                                                          interval)
                              (return true))})
  (var pubsub (or (xt/x:get-key state "pubsub") {}))
  (xt/x:set-key pubsub id handle)
  (xt/x:set-key state "pubsub" pubsub)
  (return handle))

(defn.xt unsubscribe
  "tears down a supabase pubsub subscription"
  {:added "4.1"}
  [impl handle]
  (var client (xt/x:get-key handle "client"))
  (var leave-frame (xt/x:get-key handle "leave_frame"))
  (var id (xt/x:get-key handle "id"))
  (http-fetch/stop-heartbeat client id)
  (when (xt/x:not-nil? leave-frame)
    (phoenix/send-frame client leave-frame))
  (websocket/disconnect client)
  (xt/x:set-key handle "active" false)
  (var state (xt/x:get-key impl "state"))
  (var pubsub (or (xt/x:get-key state "pubsub") {}))
  (xt/x:del-key pubsub id)
  (xt/x:set-key state "pubsub" pubsub)
  (return true))

(defn.xt publish
  "publishing is not supported by the supabase realtime abstraction"
  {:added "4.1"}
  [impl topic message opts]
  (return (promise/x:promise-run nil)))
