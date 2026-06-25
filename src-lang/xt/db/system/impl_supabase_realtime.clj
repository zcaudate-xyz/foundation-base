(ns xt.db.system.impl-supabase-realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main-ws :as main-ws]
             [xt.net.http-util :as http-util]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

;;
;; Supabase Realtime broadcast-only client.
;;
;; A single WebSocket connection multiplexes multiple Phoenix channel
;; subscriptions. The connection stays open across subscribe/unsubscribe
;; calls; only `phx_join` / `phx_leave` frames are sent per topic.
;;
;; The parent `impl` stores a map of realtime clients:
;;   impl.state.realtime = {"<id>" RealtimeClient}
;;
;; Each RealtimeClient wraps a ws-native client whose state holds:
;;   client.state.heartbeats = {}
;;   client.state.pubsub.topics = {"<topic>" {...}}
;;

(defn.xt get-realtime
  "returns the realtime client for the given id from the impl state"
  {:added "4.1"}
  [impl id]
  (return (xtd/get-in impl ["state" "realtime" id])))

(defn.xt prepare-connect-url
  "prepares the websocket url used to connect to realtime"
  {:added "4.1"}
  [config]
  (var input {})
  (var url (or (xt/x:get-key config "websocket_url")
               (xt/x:get-key config "url")
               (xt/x:get-key config "ws_url")))
  (when (xt/x:not-nil? url)
    (xt/x:set-key input "url" url))
  (var host (xt/x:get-key config "host"))
  (when (xt/x:nil? host)
    (xt/x:err "Supabase realtime missing host"))
  (var defaults {"basepath" "/realtime/v1/websocket"
                 "host"     host
                 "port"     (xt/x:get-key config "port")
                 "secured"  (xt/x:get-key config "secured")})
  (var base-url (websocket/prepare-url {"defaults" defaults} input))
  (var params (xt/x:obj-assign {"vsn" "2.0.0"}
                               (or (xt/x:get-key config "params") {})))
  (when (xt/x:not-nil? (xt/x:get-key config "api_key"))
    (xt/x:set-key params "apikey" (xt/x:get-key config "api_key")))
  (var query (http-util/encode-query-params params))
  (if (== query "")
    (return base-url)
    (return (xt/x:cat base-url "?" query))))

(defn.xt resolve-api-key
  "resolves the api key for websocket auth"
  {:added "4.1"}
  [realtime opts]
  (var impl (xt/x:get-key realtime "impl"))
  (return (or (xt/x:get-key opts "apikey")
              (xtd/get-in impl ["client" "defaults" "apikey"])
              nil)))

(defn.xt resolve-auth-token
  "resolves the realtime auth token from the impl session or client defaults"
  {:added "4.1"}
  [realtime opts]
  (var impl (xt/x:get-key realtime "impl"))
  (return (or (xt/x:get-key opts "token")
              (xtd/get-in impl ["state" "session" "access_token"])
              (xtd/get-in impl ["client" "defaults" "token"]))))

(defn.xt broadcast-join-payload
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [realtime opts]
  (var payload {"config" {"broadcast" {"ack" false
                                       "self" false}
                          "presence" {"key" ""}}})
  (var auth-token (-/resolve-auth-token realtime opts))
  (when (xt/x:not-nil? auth-token)
    (xt/x:set-key payload "access_token" auth-token))
  (return (xt/x:obj-assign payload
                           (or (xt/x:get-key opts "join_payload") {}))))

(defn.xt client-topics
  "returns the topic map from the websocket client state"
  {:added "4.1"}
  [client]
  (return (or (xtd/get-in client ["state" "pubsub" "topics"]) {})))

(defn.xt topic-entry
  "gets the subscription entry for a Phoenix topic"
  {:added "4.1"}
  [client topic]
  (return (xtd/get-in client ["state" "pubsub" "topics" topic])))

(defn.xt on-open
  "sends pending join frames and starts heartbeat when the socket opens"
  {:added "4.1"}
  [client realtime]
  (var topics (-/client-topics client))
  (xt/for:object [[topic entry] topics]
    (var join-frame (xt/x:get-key entry "join_frame"))
    (when (xt/x:not-nil? join-frame)
      (phoenix/send-frame client join-frame)))
  (websocket/start-heartbeat client
                             "pubsub"
                             (fn [client name]
                               (phoenix/send-heartbeat client {}))
                             30000)
  (return true))

(defn.xt route-frame
  "routes a decoded Phoenix frame to status handlers or topic callbacks"
  {:added "4.1"}
  [realtime frame]
  (var client (xt/x:get-key realtime "client"))
  (var event-name (xt/x:get-key frame "event"))
  (cond (== event-name "phx_reply")
        (do (var topic (xt/x:get-key frame "topic"))
            (var entry (-/topic-entry client topic))
            (when (xt/x:not-nil? entry)
              (var opts (xt/x:get-key entry "opts"))
              (var on-status (xt/x:get-key opts "on_status"))
              (var status (xtd/get-in frame ["payload" "status"]))
              (when (and (== status "ok") (xt/x:is-function? on-status))
                (on-status "SUBSCRIBED" frame))))

        (== event-name "broadcast")
        (do (var topic (xt/x:get-key frame "topic"))
            (var envelope (xt/x:get-key frame "payload"))
            (var broadcast-event (xt/x:get-key envelope "event"))
            (when (== broadcast-event "xt.db/event")
              (var payload (xt/x:get-key envelope "payload"))
              (var entry (-/topic-entry client topic))
              (when (xt/x:not-nil? entry)
                (var callback (xt/x:get-key entry "callback"))
                (when (xt/x:is-function? callback)
                  (callback payload))))))
  (return frame))

(defn.xt ensure-realtime
  "returns the realtime client for id, creating the wrapper and websocket if necessary"
  {:added "4.1"}
  [impl id opts]
  (var realtime (-/get-realtime impl id))
  (when (xt/x:not-nil? realtime)
    (return realtime))
  (var impl-client (xt/x:get-key impl "client"))
  (var client-defaults (or (xt/x:get-key impl-client "defaults") {}))
  (var config {})
  (xt/x:set-key config "host"
                (or (xt/x:get-key opts "host")
                    (xt/x:get-key client-defaults "host")))
  (xt/x:set-key config "port"
                (or (xt/x:get-key opts "port")
                    (xt/x:get-key client-defaults "port")))
  (xt/x:set-key config "secured"
                (or (xt/x:get-key opts "secured")
                    (xt/x:get-key client-defaults "secured")))
  (xt/x:set-key config "websocket_url"
                (or (xt/x:get-key opts "websocket_url")
                    (xt/x:get-key opts "ws_url")))
  (xt/x:set-key config "api_key"
                (or (xt/x:get-key opts "apikey")
                    (xt/x:get-key client-defaults "apikey")))
  (xt/x:set-key config "params"
                (xt/x:obj-clone (xt/x:get-key opts "params")))
  (var websocket (or (xt/x:get-key opts "websocket")
                     (xt/x:get-key opts "transport")
                     (:? (xt/x:is-function? WebSocket)
                         WebSocket
                         nil)))
  (when (xt/x:not-nil? websocket)
    (xt/x:set-key config "websocket" websocket))
  (var ws-url (-/prepare-connect-url config))
  (:= realtime {"::" "xt.db.system.impl_supabase_realtime/RealtimeClient"
                "id" id
                "impl" impl
                "client" nil
                "id_counter" 0})
  (xtd/set-in impl ["state" "realtime" id] realtime)
  (var client (main-ws/create-ws-client "ws" (xt/x:obj-assign config {"url" ws-url})))
  (websocket/connect client {})
  (websocket/add-listeners client
                           {"message"
                            (fn [event]
                              (-/route-frame realtime (phoenix/decode-frame event))
                              (return event))
                            "open"
                            (fn [_event]
                              (-/on-open client realtime)
                              (return true))})
  (xt/x:set-key realtime "client" client)
  (return realtime))

(defn.xt subscribe
  "subscribes to a broadcast topic on the realtime websocket"
  {:added "4.1"}
  [realtime topic opts callback]
  (:= opts (or opts {}))
  (var client (xt/x:get-key realtime "client"))
  (var join-payload (-/broadcast-join-payload realtime opts))
  (var join-frame (phoenix/make-frame-join client
                                           join-payload
                                           {"topic" topic}))
  (var leave-frame (phoenix/make-frame-leave client
                                             {"topic" topic}))
  (var topics (-/client-topics client))
  (var id-counter (xt/x:get-key realtime "id_counter"))
  (var id (xt/x:cat "sub-" id-counter))
  (xt/x:set-key realtime "id_counter" (+ id-counter 1))
  (var handle {"topic" topic
               "id" id
               "callback" callback
               "active" true})
  (xt/x:set-key topics topic
                {"callback" callback
                 "opts" opts
                 "join_frame" join-frame
                 "leave_frame" leave-frame
                 "handle" handle
                 "active" true})
  (xtd/set-in client ["state" "pubsub" "topics"] topics)
  (phoenix/send-frame client join-frame)
  (return handle))

(defn.xt unsubscribe
  "leaves a topic on the realtime websocket"
  {:added "4.1"}
  [realtime handle]
  (var topic (xt/x:get-key handle "topic"))
  (var client (xt/x:get-key realtime "client"))
  (var topics (-/client-topics client))
  (var entry (xt/x:get-key topics topic))
  (when (xt/x:not-nil? entry)
    (var leave-frame (xt/x:get-key entry "leave_frame"))
    (when (and (xt/x:not-nil? client) (xt/x:not-nil? leave-frame))
      (phoenix/send-frame client leave-frame))
    (xt/x:del-key topics topic)
    (xtd/set-in client ["state" "pubsub" "topics"] topics))
  (xt/x:set-key handle "active" false)
  (return true))

(defn.xt publish
  "publishing is not supported by the supabase realtime abstraction"
  {:added "4.1"}
  [realtime topic message opts]
  (return (promise/x:promise-run nil)))
