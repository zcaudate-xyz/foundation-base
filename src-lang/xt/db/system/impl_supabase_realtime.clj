(ns xt.db.system.impl-supabase-realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.common-ws :as common-ws]
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
;;   impl.state.realtime = {"<conn-id>" ws-native-client}
;;
;; Each ws-native client holds pubsub state at:
;;   client.state.topics = {"<topic>" {...}}
;;


(defn.xt prepare-connect-url
  "prepares the websocket url used to connect to realtime"
  {:added "4.1"}
  [impl params]
  (var defaults (xtd/get-in impl ["client" "defaults"]))
  (var path (xt/x:cat "/realtime/v1/websocket"
                      "?"
                      (http-util/encode-query-params
                       (xt/x:obj-assign
                        {"vsn"   "1.0.0"
                         "apikey" (xtd/get-in defaults "apikey")}
                        params))))
  (return
   (websocket/prepare-url impl {"path" path})))

(defn.xt create-realtime
  "returns the realtime websocket client for id, creating it if necessary"
  {:added "4.1"}
  [impl conn-id topics]
  (var ws-url (-/prepare-connect-url impl {}))
  (var realtime-client (common-ws/create-ws-client {"id"     conn-id
                                                    "url"    ws-url
                                                    "topics" topics}))
  (websocket/connect realtime-client {})
  (websocket/add-listeners realtime-client
                           {"message"
                            (fn [event]
                              (-/route-frame impl conn-id (phoenix/decode-frame event))
                              (return event))
                            "open"
                            (fn [_event]
                              (phoenix/start-heartbeat realtime-client))})
  (return realtime-client))

(defn.xt get-realtime
  "returns the realtime websocket client for the given id from the impl state"
  {:added "4.1"}
  [impl conn-id]
  (return (xtd/get-in impl ["state" "realtimes" conn-id])))

(defn.xt set-websocket
  "stores the realtime websocket client for the given id in the impl state"
  {:added "4.1"}
  [impl conn-id client]
  (xtd/set-in impl ["state" "realtimes" conn-id] client)
  (return client))


(defn.xt get-auth-token
  "resolves the realtime auth token from the impl session or client defaults"
  {:added "4.1"}
  [impl]
  (return (or (xtd/get-in impl ["state" "session" "access_token"])
              (xtd/get-in impl ["client" "defaults" "token"]))))

(defn.xt join-topic-payload
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [impl topic]
  (var auth-token (-/get-auth-token impl))
  (return
   (phoenix/make-frame-join
    {"config" {"broadcast" {"ack" false "self" false}}}
    {"topic" topic
     "ref" (xt/x:cat "::JOIN/" topic)
     "access_token" auth-token})))



(defn.xt join-topic
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [impl conn-id topic]
  
  (return
   (phoenix/send-frame client join-frame)))

(defn.xt join-
  "sends pending join frames and starts heartbeat when the socket opens"
  {:added "4.1"}
  [impl conn-id]
  (var client (-/get-websocket impl conn-id))
  (var topics (-/get-websocket-topics impl conn-id))
  (xt/for:object [[topic entry] topics]
    (var join-frame (xt/x:get-key entry "join_frame"))
    (when (xt/x:not-nil? join-frame)
      (phoenix/send-frame client join-frame)))
  
  (return true))


(comment
  {"message"
   (fn [event]
     (-/route-frame impl conn-id (phoenix/decode-frame event))
     (return event))
   "open"
   (fn [_event]
     (-/on-open impl conn-id)
     (return true))}

  (websocket/start-heartbeat client
                             "pubsub"
                             (fn [client name]
                               (phoenix/send-heartbeat client {}))
                             30000))

(comment

  (defn.xt topic-entry
    "gets the subscription entry for a Phoenix topic"
    {:added "4.1"}
    [impl conn-id topic]
    (return (-/get-topic impl conn-id topic)))



  (defn.xt route-frame
    "routes a decoded Phoenix frame to status handlers or topic callbacks"
    {:added "4.1"}
    [impl conn-id frame]
    (var event-name (xt/x:get-key frame "event"))
    (cond (== event-name "phx_reply")
          (do (var topic (xt/x:get-key frame "topic"))
              (var entry (-/get-topic impl conn-id topic))
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
                (var entry (-/get-topic impl conn-id topic))
                (when (xt/x:not-nil? entry)
                  (var callback (xt/x:get-key entry "callback"))
                  (when (xt/x:is-function? callback)
                    (callback payload))))))
    (return frame))



  (defn.xt subscribe
    "subscribes to a broadcast topic on the realtime websocket"
    {:added "4.1"}
    [impl conn-id topic opts callback]
    (:= opts (or opts {}))
    (var client (-/get-websocket impl conn-id))
    (var join-payload (-/broadcast-join-payload impl conn-id opts))
    (var join-frame (phoenix/make-frame-join client
                                             join-payload
                                             {"topic" topic}))
    (var id (xts/str-rand 8))
    (var handle {"topic" topic
                 "id" id
                 "callback" callback
                 "active" true})
    (-/set-topic impl conn-id topic
                 {"callback" callback
                  "opts" opts})
    (phoenix/send-frame client join-frame)
    (return handle))

  (defn.xt unsubscribe
    "leaves a topic on the realtime websocket"
    {:added "4.1"}
    [impl conn-id handle]
    (var topic (xt/x:get-key handle "topic"))
    (var client (-/get-websocket impl conn-id))
    (var entry (-/get-topic impl conn-id topic))
    (when (xt/x:not-nil? entry)
      (var leave-frame (phoenix/make-frame-leave client
                                                 {"topic" topic}))
      (when (xt/x:not-nil? client)
        (phoenix/send-frame client leave-frame))
      (var topics (-/get-websocket-topics impl conn-id))
      (xt/x:del-key topics topic)
      (-/set-websocket-topics impl conn-id topics))
    (xt/x:set-key handle "active" false)
    (return true))

  (defn.xt publish
    "publishing is not supported by the supabase realtime abstraction"
    {:added "4.1"}
    [impl conn-id topic message opts]
    (return (promise/x:promise-run nil)))
  )
