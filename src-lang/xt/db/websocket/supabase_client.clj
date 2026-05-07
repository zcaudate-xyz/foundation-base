(ns xt.db.websocket.supabase-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.xt client?
  "checks if a value is a supabase realtime client"
  {:added "4.1.3"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "supabase.realtime.client"
                   (xt/x:get-key obj "::")))))

(defn.xt channel?
  "checks if a value is a supabase realtime channel"
  {:added "4.1.3"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "supabase.realtime.channel"
                   (xt/x:get-key obj "::")))))

(defn.xt normalize-topic
  "normalizes a channel topic to the realtime namespace"
  {:added "4.1.3"}
  [topic]
  (if (str/starts-with? topic "realtime:")
    (return topic)
    (return (xt/x:cat "realtime:" topic))))

(defn.xt endpoint-base-url
  "normalizes a supabase realtime base endpoint"
  {:added "4.1.3"}
  [endpoint]
  (:= endpoint (or endpoint ""))
  (when (str/starts-with? endpoint "https://")
    (:= endpoint (xt/x:cat "wss://"
                           (xt/x:str-substring endpoint 8))))
  (when (str/starts-with? endpoint "http://")
    (:= endpoint (xt/x:cat "ws://"
                           (xt/x:str-substring endpoint 7))))
  (when (not (str/ends-with? endpoint "/websocket"))
    (:= endpoint
        (:? (str/ends-with? endpoint "/")
            (xt/x:cat endpoint "websocket")
            (xt/x:cat endpoint "/websocket"))))
  (return endpoint))

(defn.xt endpoint-url
  "constructs the websocket url for a realtime client"
  {:added "4.1.3"}
  [client]
  (var base (-/endpoint-base-url (xt/x:get-key client "endpoint")))
  (var params {})
  (when (xt/x:is-object? (xt/x:get-key client "params"))
    (xt/x:obj-assign params (xt/x:get-key client "params")))
  (when (xt/x:not-nil? (xt/x:get-key client "apikey"))
    (xt/x:set-key params "apikey" (xt/x:get-key client "apikey")))
  (xt/x:set-key params "vsn"
                (or (xt/x:get-key client "vsn")
                    "1.0.0"))
  (var parts [])
  (xt/for:object [[k v] params]
    (when (xt/x:not-nil? v)
      (xt/x:arr-push parts
                     (xt/x:cat k "=" (xt/x:to-string v)))))
  (if (== 0 (xt/x:len parts))
    (return base)
    (return (xt/x:cat base
                      (:? (< -1 (xt/x:str-index-of base "?"))
                          "&"
                          "?")
                      (xt/x:str-join "&" parts)))))

(defn.xt next-ref
  "allocates the next message ref"
  {:added "4.1.3"}
  [client]
  (var current (+ 1 (or (xt/x:get-key client "ref") 0)))
  (xt/x:set-key client "ref" current)
  (return (xt/x:to-string current)))

(defn.xt make-message
  "creates a phoenix channel envelope"
  {:added "4.1.3"}
  [topic event payload ref join-ref]
  (var out {"topic" topic
            "event" event
            "payload" (or payload {})
            "ref" ref})
  (when (xt/x:not-nil? join-ref)
    (xt/x:set-key out "join_ref" join-ref))
  (return out))

(defn.xt find-channel
  "finds a channel by normalized topic"
  {:added "4.1.3"}
  [client topic]
  (var channels (or (xt/x:get-key client "channels") []))
  (var found nil)
  (xt/for:array [channel channels]
    (when (== (xt/x:get-key channel "topic") topic)
      (:= found channel)))
  (return found))

(defn.xt send-now
  "encodes and sends one websocket frame immediately"
  {:added "4.1.3"}
  [client msg]
  (var conn (xt/x:get-key client "conn"))
  (var encode-fn (xt/x:get-key client "encode"))
  (var payload (:? (xt/x:is-function? encode-fn)
                   (encode-fn msg)
                   msg))
  (if (ws/client? conn)
    (ws/send conn payload)
    ((xt/x:get-key conn "send") payload))
  (return msg))

(defn.xt flush-send-buffer
  "flushes buffered websocket messages"
  {:added "4.1.3"}
  [client]
  (var buffer (or (xt/x:get-key client "send-buffer") []))
  (xt/x:set-key client "send-buffer" [])
  (xt/for:array [msg buffer]
    (-/send-now client msg))
  (return true))

(defn.xt push
  "pushes a websocket message or buffers it while disconnected"
  {:added "4.1.3"}
  [client msg]
  (if (== "open" (xt/x:get-key client "state"))
    (return (-/send-now client msg))
    (do (xt/x:arr-push (xt/x:get-key client "send-buffer") msg)
        (return msg))))

(defn.xt stop-heartbeat
  "stops the active heartbeat timer"
  {:added "4.1.3"}
  [client]
  (var timer (xt/x:get-key client "heartbeat-timer"))
  (var clear-fn (xt/x:get-key client "clear-interval"))
  (when (and (xt/x:not-nil? timer)
             (xt/x:is-function? clear-fn))
    (clear-fn timer))
  (xt/x:set-key client "heartbeat-timer" nil)
  (return nil))

(defn.xt heartbeat
  "sends a heartbeat frame when the socket is open"
  {:added "4.1.3"}
  [client]
  (when (not (== "open" (xt/x:get-key client "state")))
    (return nil))
  (var pending (xt/x:get-key client "pending-heartbeat-ref"))
  (when (xt/x:not-nil? pending)
    (xt/x:set-key client "heartbeat-status" "timeout")
    (return nil))
  (var ref (-/next-ref client))
  (xt/x:set-key client "pending-heartbeat-ref" ref)
  (xt/x:set-key client "heartbeat-status" "sent")
  (return (-/push client
                  (-/make-message "phoenix"
                                  "heartbeat"
                                  {}
                                  ref
                                  nil))))

(defn.xt start-heartbeat
  "starts the active heartbeat timer"
  {:added "4.1.3"}
  [client]
  (-/stop-heartbeat client)
  (var schedule-fn (xt/x:get-key client "schedule-interval"))
  (when (xt/x:is-function? schedule-fn)
    (xt/x:set-key client "heartbeat-timer"
                  (schedule-fn
                   (fn []
                     (return (-/heartbeat client)))
                   (or (xt/x:get-key client "heartbeat-interval-ms")
                       25000))))
  (return true))

(defn.xt enrich-postgres-payload
  "normalizes raw postgres_changes payloads"
  {:added "4.1.3"}
  [payload]
  (when (xt/x:nil? payload)
    (return payload))
  (when (xt/x:not-nil? (xt/x:get-key payload "eventType"))
    (return payload))
  (var data (xt/x:get-key payload "data"))
  (when (xt/x:nil? data)
    (return payload))
  (return {"ids" (or (xt/x:get-key payload "ids") [])
           "schema" (xt/x:get-key data "schema")
           "table" (xt/x:get-key data "table")
           "commit_timestamp" (xt/x:get-key data "commit_timestamp")
           "errors" (or (xt/x:get-key data "errors") [])
           "eventType" (xt/x:get-key data "type")
           "new" (or (xt/x:get-key data "record") {})
           "old" (or (xt/x:get-key data "old_record") {})}))

(defn.xt binding-matches?
  "checks whether a binding should receive an event"
  {:added "4.1.3"}
  [binding event payload]
  (var btype (xt/x:str-to-lower (or (xt/x:get-key binding "type") "")))
  (var event-name (xt/x:str-to-lower (or event "")))
  (when (not (== btype event-name))
    (return false))
  (var filter (or (xt/x:get-key binding "filter") {}))
  (cond (== event-name "postgres_changes")
        (do (var current (-/enrich-postgres-payload payload))
            (var bind-id (xt/x:get-key binding "id"))
            (when (and (xt/x:not-nil? bind-id)
                       (not (xt/x:arr-some (or (xt/x:get-key current "ids") [])
                                           (fn [id]
                                             (return (== id bind-id))))))
              (return false))
            (var filter-event (or (xt/x:get-key filter "event") "*"))
            (var payload-event (or (xt/x:get-key current "eventType") ""))
            (when (and (not (== filter-event "*"))
                       (not (== (xt/x:str-to-upper filter-event)
                                (xt/x:str-to-upper payload-event))))
              (return false))
            (when (and (xt/x:not-nil? (xt/x:get-key filter "schema"))
                       (not (== (xt/x:get-key filter "schema")
                                (xt/x:get-key current "schema"))))
              (return false))
            (when (and (xt/x:not-nil? (xt/x:get-key filter "table"))
                       (not (== (xt/x:get-key filter "table")
                                (xt/x:get-key current "table"))))
              (return false))
            (return true))

        :else
        (do (var filter-event (or (xt/x:get-key filter "event") "*"))
            (var payload-event (or (xt/x:get-key payload "event")
                                   event))
            (return (or (== filter-event "*")
                        (== (xt/x:str-to-lower filter-event)
                            (xt/x:str-to-lower payload-event)))))))

(defn.xt channel-trigger
  "dispatches one inbound event to a channel's bindings"
  {:added "4.1.3"}
  [channel event payload ref]
  (var bindings (or (xt/x:get-key channel "bindings") []))
  (xt/for:array [binding bindings]
    (when (-/binding-matches? binding event payload)
      (var callback (xt/x:get-key binding "callback"))
      (when (xt/x:is-function? callback)
        (callback (:? (== event "postgres_changes")
                      (-/enrich-postgres-payload payload)
                      payload)
                  ref))))
  (return true))

(defn.xt route-message
  "routes one decoded websocket message"
  {:added "4.1.3"}
  [client msg]
  (var event (xt/x:get-key msg "event"))
  (var ref (xt/x:get-key msg "ref"))
  (var payload (or (xt/x:get-key msg "payload") {}))
  (when (and (== event "phx_reply")
             (xt/x:not-nil? ref))
    (var pending (xt/x:get-key (xt/x:get-key client "pending") ref))
    (when (xt/x:is-function? pending)
      (pending payload msg)
      (xt/x:del-key (xt/x:get-key client "pending") ref)))
  (when (and (== (xt/x:get-key msg "topic") "phoenix")
             (== event "phx_reply")
             (== ref (xt/x:get-key client "pending-heartbeat-ref")))
    (xt/x:set-key client "pending-heartbeat-ref" nil)
    (xt/x:set-key client "heartbeat-status" "ok"))
  (var topic (xt/x:get-key msg "topic"))
  (when (xt/x:not-nil? topic)
    (xt/for:array [channel (or (xt/x:get-key client "channels") [])]
      (when (== (xt/x:get-key channel "topic") topic)
        (-/channel-trigger channel event payload ref))))
  (return msg))

(defn.xt receive-raw
  "decodes and routes a raw inbound websocket frame"
  {:added "4.1.3"}
  [client raw]
  (var decode-fn (xt/x:get-key client "decode"))
  (var msg (:? (xt/x:is-function? decode-fn)
               (decode-fn raw)
               raw))
  (return (-/route-message client msg)))

(defn.xt bind-channel-ids
  "binds server postgres_changes ids onto matching channel bindings"
  {:added "4.1.3"}
  [channel response]
  (var changes (or (xt/x:get-key response "postgres_changes") []))
  (var bindings (or (xt/x:get-key channel "bindings") []))
  (var index 0)
  (xt/for:array [binding bindings]
    (when (== (xt/x:get-key binding "type") "postgres_changes")
      (var current (xt/x:get-idx changes index nil))
      (when (xt/x:not-nil? current)
        (xt/x:set-key binding "id" (xt/x:get-key current "id")))
      (:= index (+ index 1))))
  (return channel))

(defn.xt channel-join-payload
  "creates the join payload for a channel"
  {:added "4.1.3"}
  [channel]
  (var client (xt/x:get-key channel "client"))
  (var params (or (xt/x:get-key channel "params") {}))
  (var config (xt/x:obj-clone (or (xt/x:get-key params "config") {})))
  (var changes [])
  (xt/for:array [binding (or (xt/x:get-key channel "bindings") [])]
    (when (== (xt/x:get-key binding "type") "postgres_changes")
      (xt/x:arr-push changes (xt/x:get-key binding "filter"))))
  (xt/x:set-key config "postgres_changes" changes)
  (var payload {"config" config})
  (var token (xt/x:get-key client "access-token"))
  (when (xt/x:not-nil? token)
    (xt/x:set-key payload "access_token" token))
  (when (xt/x:is-object? (xt/x:get-key channel "join-payload-extra"))
    (xt/x:obj-assign payload (xt/x:get-key channel "join-payload-extra")))
  (return payload))

(defn.xt subscribe-channel
  "subscribes a channel over the websocket transport"
  {:added "4.1.3"}
  [channel callback]
  (var client (xt/x:get-key channel "client"))
  (xt/x:set-key channel "subscribe-callback" callback)
  ((xt/x:get-key client "connect"))
  (var ref (-/next-ref client))
  (xt/x:set-key channel "state" "joining")
  (xt/x:set-key channel "join-ref" ref)
  (xt/x:set-key (xt/x:get-key client "pending")
                ref
                (fn [payload _msg]
                  (var status (xt/x:get-key payload "status"))
                  (var response (or (xt/x:get-key payload "response") {}))
                  (cond (== status "ok")
                        (do (-/bind-channel-ids channel response)
                            (xt/x:set-key channel "state" "joined")
                            (when (xt/x:is-function? callback)
                              (callback "SUBSCRIBED" nil)))

                        :else
                        (do (xt/x:set-key channel "state" "errored")
                            (when (xt/x:is-function? callback)
                              (callback "CHANNEL_ERROR" response))))))
  (-/push client
          (-/make-message (xt/x:get-key channel "topic")
                          "phx_join"
                          (-/channel-join-payload channel)
                          ref
                          nil))
  (return channel))

(defn.xt unsubscribe-channel
  "unsubscribes a channel over the websocket transport"
  {:added "4.1.3"}
  [channel]
  (var client (xt/x:get-key channel "client"))
  (var ref (-/next-ref client))
  (xt/x:set-key channel "state" "closed")
  (-/push client
          (-/make-message (xt/x:get-key channel "topic")
                          "phx_leave"
                          {}
                          ref
                          (xt/x:get-key channel "join-ref")))
  (return true))

(defn.xt set-auth
  "updates the access token and broadcasts it to joined channels"
  {:added "4.1.3"}
  [client token]
  (xt/x:set-key client "access-token" token)
  (xt/for:array [channel (or (xt/x:get-key client "channels") [])]
    (when (== "joined" (xt/x:get-key channel "state"))
      (-/push client
              (-/make-message (xt/x:get-key channel "topic")
                              "access_token"
                              {"access_token" token}
                              (-/next-ref client)
                              (xt/x:get-key channel "join-ref")))))
  (return token))

(defn.xt remove-channel-local
  "removes a channel entry from the client registry"
  {:added "4.1.3"}
  [client channel]
  (var channels (or (xt/x:get-key client "channels") []))
  (var idx -1)
  (var i 0)
  (xt/for:array [current channels]
    (when (and (< idx 0)
               (== current channel))
      (:= idx i))
    (:= i (+ i 1)))
  (when (<= 0 idx)
    (xt/x:arr-remove channels idx))
  (return channel))

(defn.xt remove-channel
  "unsubscribes and removes a single channel"
  {:added "4.1.3"}
  [client channel]
  (when (-/channel? channel)
    (-/unsubscribe-channel channel)
    (-/remove-channel-local client channel))
  (return "ok"))

(defn.xt remove-all-channels
  "unsubscribes and removes all channels"
  {:added "4.1.3"}
  [client]
  (var out [])
  (xt/for:array [channel (xt/x:arr-slice (or (xt/x:get-key client "channels") []) 0)]
    (xt/x:arr-push out (-/remove-channel client channel)))
  (return out))

(defn.xt connect
  "connects the client using the injected transport hook"
  {:added "4.1.3"}
  [client]
  (when (or (== "open" (xt/x:get-key client "state"))
            (== "connecting" (xt/x:get-key client "state")))
    (return client))
  (xt/x:set-key client "manual-disconnect" false)
  (var connect-fn (xt/x:get-key client "connect-fn"))
  (when (not (xt/x:is-function? connect-fn))
    (xt/x:err "Supabase realtime client connection failed: transport configuration is missing or invalid."))
  (return (connect-fn client)))

(defn.xt disconnect
  "disconnects the client using the injected transport hook"
  {:added "4.1.3"}
  [client code reason]
  (xt/x:set-key client "manual-disconnect" true)
  (-/stop-heartbeat client)
  (var disconnect-fn (xt/x:get-key client "disconnect-fn"))
  (when (xt/x:is-function? disconnect-fn)
    (disconnect-fn client code reason))
  (xt/x:set-key client "state" "closed")
  (xt/x:set-key client "conn" nil)
  (return client))

(defn.xt create-channel
  "creates and registers a realtime channel"
  {:added "4.1.3"}
  [client topic params]
  (var current (-/find-channel client (-/normalize-topic topic)))
  (when (xt/x:not-nil? current)
    (return current))
  (var channel {"::" "supabase.realtime.channel"
                "client" client
                "topic" (-/normalize-topic topic)
                "params" (or params {"config" {}})
                "bindings" []
                "join-payload-extra" {}
                "join-ref" nil
                "state" "closed"})
  (xt/x:set-key channel "on"
                (fn [type filter callback]
                  (xt/x:arr-push (xt/x:get-key channel "bindings")
                                 {"type" (xt/x:str-to-lower type)
                                  "filter" (or filter {})
                                  "callback" callback})
                  (return channel)))
  (xt/x:set-key channel "subscribe"
                (fn [callback]
                  (return (-/subscribe-channel channel callback))))
  (xt/x:set-key channel "unsubscribe"
                (fn []
                  (return (-/unsubscribe-channel channel))))
  (xt/x:set-key channel "updateJoinPayload"
                (fn [payload]
                  (xt/x:obj-assign (xt/x:get-key channel "join-payload-extra")
                                   (or payload {}))
                  (return channel)))
  (xt/x:arr-push (xt/x:get-key client "channels") channel)
  (return channel))

(defn.xt client-create-base
  "creates a stateful realtime client with injected transport hooks"
  {:added "4.1.3"}
  [endpoint opts]
  (:= opts (or opts {}))
  (var params (xt/x:obj-clone (or (xt/x:get-key opts "params") {})))
  (var apikey (or (xt/x:get-key opts "apikey")
                  (xt/x:get-key params "apikey")))
  (var token (or (xt/x:get-key opts "access-token")
                 (xt/x:get-key opts "access_token")
                 (xt/x:get-key opts "token")))
  (var client {"::" "supabase.realtime.client"
               "endpoint" endpoint
               "params" params
               "apikey" apikey
               "access-token" token
               "vsn" (or (xt/x:get-key opts "vsn")
                         "1.0.0")
               "timeout" (or (xt/x:get-key opts "timeout") 10000)
               "heartbeat-interval-ms" (or (xt/x:get-key opts "heartbeat-interval-ms")
                                           (xt/x:get-key opts "heartbeatIntervalMs")
                                           25000)
               "state" "closed"
               "ref" 0
               "conn" nil
               "channels" []
               "pending" {}
               "pending-heartbeat-ref" nil
               "send-buffer" []
                "connect-fn" (xt/x:get-key opts "connect-fn")
                "disconnect-fn" (xt/x:get-key opts "disconnect-fn")
                "transport-driver" (xt/x:get-key opts "transport-driver")
                "encode" (xt/x:get-key opts "encode")
                "decode" (xt/x:get-key opts "decode")
                "schedule-interval" (xt/x:get-key opts "schedule-interval")
                "clear-interval" (xt/x:get-key opts "clear-interval")})
  (xt/x:set-key client "connect"
                (fn []
                  (return (-/connect client))))
  (xt/x:set-key client "disconnect"
                (fn [code reason]
                  (return (-/disconnect client code reason))))
  (xt/x:set-key client "push"
                (fn [msg]
                  (return (-/push client msg))))
  (xt/x:set-key client "channel"
                (fn [topic params]
                  (return (-/create-channel client topic params))))
  (xt/x:set-key client "setAuth"
                (fn [token]
                  (return (-/set-auth client token))))
  (xt/x:set-key client "getChannels"
                (fn []
                  (return (xt/x:get-key client "channels"))))
  (xt/x:set-key client "removeChannel"
                (fn [channel]
                  (return (-/remove-channel client channel))))
  (xt/x:set-key client "removeAllChannels"
                (fn []
                  (return (-/remove-all-channels client))))
  (xt/x:set-key client "endpointURL"
                (fn []
                  (return (-/endpoint-url client))))
  (return client))

(defabstract.xt default-encode [msg])
(defabstract.xt default-decode [raw])
(defabstract.xt create-client [endpoint opts])

;;
;; JS
;;

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.js default-encode
  "encodes a realtime message to JSON"
  {:added "4.1.3"}
  [msg]
  (return (JSON.stringify msg)))

(defn.js default-decode
  "decodes a realtime message from JSON"
  {:added "4.1.3"}
  [raw]
  (cond (xt/x:is-string? raw)
        (return (JSON.parse raw))

        (and (xt/x:is-object? raw)
             (xt/x:not-nil? (xt/x:get-key raw "data")))
        (return (JSON.parse (xt/x:get-key raw "data")))

        :else
        (return raw)))

(defn.js transport->driver
  "normalizes a transport constructor or driver into the websocket protocol"
  {:added "4.1.3"}
  [transport]
  (when (ws/driver? transport)
    (return transport))
  (when (xt/x:nil? transport)
    (return nil))
  (return
   (ws/driver-create
    {"connect" (fn [url]
                 (return (new transport url)))})))

(defn.js default-connect-fn
  "connects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client]
  (when (xt/x:not-nil? (xt/x:get-key client "conn"))
    (return (xt/x:get-key client "conn")))
  (var driver (xt/x:get-key client "transport-driver"))
  (when (xt/x:nil? driver)
    (throw (new Error "WebSocket transport not available. Provide a custom transport or transport-driver option.")))
  (xt/x:set-key client "state" "connecting")
  (var socket (ws/connect driver (-/endpoint-url client)))
  (xt/x:set-key client "conn" socket)
  (ws/add-listener socket "open"
                   (fn [_]
                     (xt/x:set-key client "state" "open")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (-/flush-send-buffer client)
                     (-/start-heartbeat client)))
  (ws/add-listener socket "close"
                   (fn [event]
                     (-/stop-heartbeat client)
                     (xt/x:set-key client "conn" nil)
                     (xt/x:set-key client "state" "closed")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (xt/for:array [channel (or (xt/x:get-key client "channels") [])]
                       (xt/x:set-key channel "state" "closed")
                       (var callback (xt/x:get-key channel "subscribe-callback"))
                       (when (xt/x:is-function? callback)
                         (callback "CLOSED" event)))))
  (ws/add-listener socket "error"
                   (fn [event]
                     (xt/x:set-key client "last-error" event)))
  (ws/add-listener socket "message"
                   (fn [event]
                     (return (-/receive-raw client event))))
  (return socket))

(defn.js default-disconnect-fn
  "disconnects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client code reason]
  (var conn (xt/x:get-key client "conn"))
  (when (xt/x:not-nil? conn)
    (if (ws/client? conn)
      (ws/disconnect conn code reason)
      (if (and (xt/x:not-nil? conn)
               (xt/x:is-function? (xt/x:get-key conn "close")))
        (if (xt/x:not-nil? code)
          (conn.close code (or reason ""))
          (conn.close)))))
  (return nil))

(defn.js create-client
  "creates a JS realtime client compatible with Supabase channel APIs"
  {:added "4.1.3"}
  [endpoint opts]
  (:= opts (xt/x:obj-clone (or opts {})))
  (when (xt/x:nil? (xt/x:get-key opts "encode"))
    (xt/x:set-key opts "encode" -/default-encode))
  (when (xt/x:nil? (xt/x:get-key opts "decode"))
    (xt/x:set-key opts "decode" -/default-decode))
  (when (xt/x:nil? (xt/x:get-key opts "schedule-interval"))
    (xt/x:set-key opts "schedule-interval" setInterval))
  (when (xt/x:nil? (xt/x:get-key opts "clear-interval"))
    (xt/x:set-key opts "clear-interval" clearInterval))
  (when (xt/x:nil? (xt/x:get-key opts "connect-fn"))
    (xt/x:set-key opts "connect-fn" -/default-connect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "disconnect-fn"))
    (xt/x:set-key opts "disconnect-fn" -/default-disconnect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "transport-driver"))
    (xt/x:set-key opts "transport-driver"
                  (-/transport->driver
                   (or (xt/x:get-key opts "transport")
                       (!:G WebSocket)))))
  (when (xt/x:nil? (xt/x:get-key opts "transport"))
    (xt/x:set-key opts "transport" (!:G WebSocket)))
  (var client (-/client-create-base endpoint opts))
  (xt/x:set-key client "transport" (xt/x:get-key opts "transport"))
  (xt/x:set-key client "transport-driver" (xt/x:get-key opts "transport-driver"))
  (return client))

;;
;; LUA NGINX
;;

(l/script :lua.nginx
  {:import [["cjson" :as cjson]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.protocol.impl.client-websocket :as ws]
             [xt.db.websocket.nginx-client :as ngx-client]]})

(defn.lua default-encode
  "encodes a realtime message to JSON"
  {:added "4.1.3"}
  [msg]
  (return (cjson.encode msg)))

(defn.lua default-decode
  "decodes a realtime message from JSON"
  {:added "4.1.3"}
  [raw]
  (cond (xt/x:is-string? raw)
        (return (cjson.decode raw))

        (and (xt/x:is-object? raw)
             (xt/x:not-nil? (xt/x:get-key raw "data")))
        (return (cjson.decode (xt/x:get-key raw "data")))

        :else
        (return raw)))

(defn.lua transport->driver
  "normalizes a callable transport or driver into the websocket protocol"
  {:added "4.1.3"}
  [transport]
  (when (ws/driver? transport)
    (return transport))
  (when (xt/x:nil? transport)
    (return nil))
  (return
   (ws/driver-create
    {"connect" (fn [url]
                 (return (transport url)))})))

(defn.lua normalize-endpoint
  "normalizes a realtime websocket endpoint for Lua clients"
  {:added "4.1.3"}
  [endpoint]
  (:= endpoint (or endpoint ""))
  (when (== 1 (xt/x:str-index-of endpoint "https://"))
    (:= endpoint (xt/x:cat "wss://"
                           (xt/x:str-substring endpoint 9))))
  (when (== 1 (xt/x:str-index-of endpoint "http://"))
    (:= endpoint (xt/x:cat "ws://"
                           (xt/x:str-substring endpoint 8))))
  (var websocket-suffix "/websocket")
  (var endpoint-len (xt/x:len endpoint))
  (var suffix-len (xt/x:len websocket-suffix))
  (when (or (< endpoint-len suffix-len)
            (not (== websocket-suffix
                     (xt/x:str-substring endpoint
                                         (+ (- endpoint-len suffix-len) 1)
                                         endpoint-len))))
    (:= endpoint
        (xt/x:cat endpoint
                  (:? (and (< 0 endpoint-len)
                           (== "/" (xt/x:str-substring endpoint
                                                       endpoint-len
                                                       endpoint-len)))
                      "websocket"
                      "/websocket"))))
  (return endpoint))

(defn.lua default-connect-fn
  "connects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client]
  (when (xt/x:not-nil? (xt/x:get-key client "conn"))
    (return (xt/x:get-key client "conn")))
  (var driver (xt/x:get-key client "transport-driver"))
  (when (xt/x:nil? driver)
    (xt/x:err "WebSocket transport not available. Provide a custom transport or transport-driver option."))
  (xt/x:set-key client "state" "connecting")
  (var socket (ws/connect driver (-/endpoint-url client)))
  (xt/x:set-key client "conn" socket)
  (ws/add-listener socket "open"
                   (fn [_]
                     (xt/x:set-key client "state" "open")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (-/flush-send-buffer client)
                     (-/start-heartbeat client)))
  (ws/add-listener socket "close"
                   (fn [event]
                     (-/stop-heartbeat client)
                     (xt/x:set-key client "conn" nil)
                     (xt/x:set-key client "state" "closed")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (xt/for:array [channel (or (xt/x:get-key client "channels") [])]
                       (xt/x:set-key channel "state" "closed")
                       (var callback (xt/x:get-key channel "subscribe-callback"))
                       (when (xt/x:is-function? callback)
                         (callback "CLOSED" event)))))
  (ws/add-listener socket "error"
                   (fn [event]
                     (xt/x:set-key client "last-error" event)))
  (ws/add-listener socket "message"
                   (fn [event]
                     (return (-/receive-raw client event))))
  (return socket))

(defn.lua default-disconnect-fn
  "disconnects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client code reason]
  (var conn (xt/x:get-key client "conn"))
  (when (xt/x:not-nil? conn)
    (if (ws/client? conn)
      (ws/disconnect conn code reason)
      (if (xt/x:is-function? (xt/x:get-key conn "close"))
        (if (xt/x:not-nil? code)
          ((xt/x:get-key conn "close") code (or reason ""))
          ((xt/x:get-key conn "close"))))))
  (return nil))

(defn.lua create-client
  "creates a Lua realtime client compatible with Supabase channel APIs"
  {:added "4.1.3"}
  [endpoint opts]
  (:= opts (xt/x:obj-clone (or opts {})))
  (var params (xt/x:obj-clone (or (xt/x:get-key opts "params") {})))
  (when (and (xt/x:not-nil? (xt/x:get-key opts "apikey"))
             (xt/x:nil? (xt/x:get-key params "apikey")))
    (xt/x:set-key params "apikey" (xt/x:get-key opts "apikey")))
  (xt/x:set-key opts "params" params)
  (when (xt/x:nil? (xt/x:get-key opts "encode"))
    (xt/x:set-key opts "encode" -/default-encode))
  (when (xt/x:nil? (xt/x:get-key opts "decode"))
    (xt/x:set-key opts "decode" -/default-decode))
  (when (xt/x:nil? (xt/x:get-key opts "connect-fn"))
    (xt/x:set-key opts "connect-fn" -/default-connect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "disconnect-fn"))
    (xt/x:set-key opts "disconnect-fn" -/default-disconnect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "transport-driver"))
    (xt/x:set-key opts "transport-driver"
                  (or (-/transport->driver
                       (xt/x:get-key opts "transport"))
                      (ngx-client/create-driver opts))))
  (var client (-/client-create-base (-/normalize-endpoint endpoint) opts))
  (xt/x:set-key client "params" params)
  (xt/x:set-key client "apikey" (or (xt/x:get-key opts "apikey")
                                    (xt/x:get-key params "apikey")))
  (xt/x:set-key client "transport" (xt/x:get-key opts "transport"))
  (xt/x:set-key client "transport-driver" (xt/x:get-key opts "transport-driver"))
  (return client))

;;
;; PYTHON
;;

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [python.core :as py]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.py default-encode
  "encodes a realtime message to JSON"
  {:added "4.1.3"}
  [msg]
  (return (py/js:dumps msg)))

(defn.py default-decode
  "decodes a realtime message from JSON"
  {:added "4.1.3"}
  [raw]
  (cond (xt/x:is-string? raw)
        (return (py/js:loads raw))

        (and (xt/x:is-object? raw)
             (xt/x:not-nil? (xt/x:get-key raw "data")))
        (return (py/js:loads (xt/x:get-key raw "data")))

        :else
        (return raw)))

(defn.py transport->driver
  "normalizes a callable transport or driver into the websocket protocol"
  {:added "4.1.3"}
  [transport]
  (when (ws/driver? transport)
    (return transport))
  (when (xt/x:nil? transport)
    (return nil))
  (return
   (ws/driver-create
    {"connect" (fn [url]
                 (return (transport url)))})))

(defn.py default-connect-fn
  "connects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client]
  (when (xt/x:not-nil? (xt/x:get-key client "conn"))
    (return (xt/x:get-key client "conn")))
  (var driver (xt/x:get-key client "transport-driver"))
  (when (xt/x:nil? driver)
    (xt/x:err "WebSocket transport not available. Provide a custom transport or transport-driver option."))
  (xt/x:set-key client "state" "connecting")
  (var socket (ws/connect driver (-/endpoint-url client)))
  (xt/x:set-key client "conn" socket)
  (ws/add-listener socket "open"
                   (fn [_]
                     (xt/x:set-key client "state" "open")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (-/flush-send-buffer client)
                     (-/start-heartbeat client)))
  (ws/add-listener socket "close"
                   (fn [event]
                     (-/stop-heartbeat client)
                     (xt/x:set-key client "conn" nil)
                     (xt/x:set-key client "state" "closed")
                     (xt/x:set-key client "pending-heartbeat-ref" nil)
                     (xt/for:array [channel (or (xt/x:get-key client "channels") [])]
                       (xt/x:set-key channel "state" "closed")
                       (var callback (xt/x:get-key channel "subscribe-callback"))
                       (when (xt/x:is-function? callback)
                         (callback "CLOSED" event)))))
  (ws/add-listener socket "error"
                   (fn [event]
                     (xt/x:set-key client "last-error" event)))
  (ws/add-listener socket "message"
                   (fn [event]
                     (return (-/receive-raw client event))))
  (return socket))

(defn.py default-disconnect-fn
  "disconnects a client using the configured websocket transport"
  {:added "4.1.3"}
  [client code reason]
  (var conn (xt/x:get-key client "conn"))
  (when (xt/x:not-nil? conn)
    (if (ws/client? conn)
      (ws/disconnect conn code reason)
      (if (xt/x:is-function? (xt/x:get-key conn "close"))
        (if (xt/x:not-nil? code)
          ((xt/x:get-key conn "close") code (or reason ""))
          ((xt/x:get-key conn "close"))))))
  (return nil))

(defn.py create-client
  "creates a Python realtime client compatible with Supabase channel APIs"
  {:added "4.1.3"}
  [endpoint opts]
  (:= opts (xt/x:obj-clone (or opts {})))
  (when (xt/x:nil? (xt/x:get-key opts "encode"))
    (xt/x:set-key opts "encode" -/default-encode))
  (when (xt/x:nil? (xt/x:get-key opts "decode"))
    (xt/x:set-key opts "decode" -/default-decode))
  (when (xt/x:nil? (xt/x:get-key opts "connect-fn"))
    (xt/x:set-key opts "connect-fn" -/default-connect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "disconnect-fn"))
    (xt/x:set-key opts "disconnect-fn" -/default-disconnect-fn))
  (when (xt/x:nil? (xt/x:get-key opts "transport-driver"))
    (xt/x:set-key opts "transport-driver"
                  (-/transport->driver
                   (xt/x:get-key opts "transport"))))
  (var client (-/client-create-base endpoint opts))
  (xt/x:set-key client "transport" (xt/x:get-key opts "transport"))
  (xt/x:set-key client "transport-driver" (xt/x:get-key opts "transport-driver"))
  (return client))
