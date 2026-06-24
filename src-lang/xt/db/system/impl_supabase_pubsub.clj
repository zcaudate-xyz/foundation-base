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

;;
;; Supabase Realtime broadcast-only pubsub.
;;
;; This namespace supports a single WebSocket connection that multiplexes
;; multiple Phoenix channel subscriptions. It only handles broadcast events
;; with the event name "xt.db/event".
;;

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
  (return (or (xt/x:get-key opts "apikey")
              (xtd/get-in impl ["client"  "defaults" "apikey"])
              nil)))

(defn.xt resolve-auth-token
  "resolves the realtime auth token from the impl session or client defaults"
  {:added "4.1"}
  [impl opts]
  (return (or (xt/x:get-key opts "token")
              (xtd/get-in impl ["state"  "session" "access_token"])
              (xtd/get-in impl ["client"  "defaults" "token"]))))


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

(defn.xt websocket-config
  "builds the websocket connection config from the impl and options"
  {:added "4.1"}
  [impl opts]
  (var client (xt/x:get-key impl "client"))
  (var config {})
  (xt/x:set-key config "base_url"
                (or (xt/x:get-key opts "base_url")
                    (-/client-base-url client)))
  (xt/x:set-key config "websocket_url"
                (or (xt/x:get-key opts "websocket_url")
                    (xt/x:get-key opts "ws_url")))
  (xt/x:set-key config "api_key" (-/resolve-api-key impl opts))
  (xt/x:set-key config "params"
                (xt/x:obj-clone (xt/x:get-key opts "params")))
  (var websocket (or (xt/x:get-key opts "websocket")
                     (xt/x:get-key opts "transport")
                     (:? (xt/x:is-function? WebSocket)
                         WebSocket
                         nil)))
  (when (xt/x:not-nil? websocket)
    (xt/x:set-key config "websocket" websocket))
  (return config))

(defn.xt broadcast-join-payload
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [impl opts]
  (var payload {"config" {"broadcast" {"ack" false
                                       "self" false}
                          "presence" {"key" ""}}})
  (var auth-token (-/resolve-auth-token impl opts))
  (when (xt/x:not-nil? auth-token)
    (xt/x:set-key payload "access_token" auth-token))
  (return (xt/x:obj-assign payload
                           (or (xt/x:get-key opts "join_payload") {}))))

(defn.xt broadcast->event-vectors
  "converts a Supabase broadcast payload into xt.db event vectors

   Expected broadcast envelope:
     {\"event\": \"xt.db/event\",
      \"payload\": {\"db/sync\": {...}, \"db/remove\": {...}}}"
  {:added "4.1"}
  [payload]
  (var event (xt/x:get-key payload "event"))
  (when (not= event "xt.db/event")
    (return nil))
  (var data (xt/x:get-key payload "payload"))
  (var out [])
  (var db-sync (xt/x:get-key data "db/sync"))
  (var db-remove (xt/x:get-key data "db/remove"))
  (when (xt/x:not-nil? db-sync)
    (xt/x:arr-push out ["add" db-sync]))
  (when (xt/x:not-nil? db-remove)
    (xt/x:arr-push out ["remove" db-remove]))
  (return out))

(defn.xt topic-entry
  "gets the subscription entry for a Phoenix topic"
  {:added "4.1"}
  [impl topic]
  (return (xtd/get-in impl ["state" "pubsub" "topics" topic])))

(defn.xt on-open
  "sends pending join frames and starts heartbeat when the shared socket opens"
  {:added "4.1"}
  [client impl]
  (var pubsub (or (xtd/get-in impl ["state" "pubsub"]) {}))
  (var topics (or (xt/x:get-key pubsub "topics") {}))
  (xt/for:object [[topic entry] topics]
    (var join-frame (xt/x:get-key entry "join_frame"))
    (when (xt/x:not-nil? join-frame)
      (phoenix/send-frame client join-frame)))
  (http-fetch/start-heartbeat client
                              "pubsub"
                              (fn [client name]
                                (phoenix/send-heartbeat client {}))
                              30000)
  (return true))

(defn.xt shared-message-handler
  "creates the websocket message listener that routes by Phoenix topic"
  {:added "4.1"}
  [impl]
  (return
   (phoenix/wrap-phoenix
    {"phx_reply"
     (fn [frame]
       (var topic (xt/x:get-key frame "topic"))
       (var entry (-/topic-entry impl topic))
       (when (xt/x:not-nil? entry)
         (var opts (xt/x:get-key entry "opts"))
         (var on-status (xt/x:get-key opts "on_status"))
         (var status (xtd/get-in frame ["payload" "status"]))
         (when (and (== status "ok") (xt/x:is-function? on-status))
           (on-status "SUBSCRIBED" frame)))
       (return frame))

     "broadcast"
     (fn [frame]
       (var topic (xt/x:get-key frame "topic"))
       (var payload (xt/x:get-key frame "payload"))
       (var events (-/broadcast->event-vectors payload))
       (var entry (-/topic-entry impl topic))
       (when (and (xt/x:not-nil? events) (xt/x:not-nil? entry))
         (var callback (xt/x:get-key entry "callback"))
         (when (xt/x:is-function? callback)
           (xt/for:array [event events]
             (callback event))))
       (return events))})))

(defn.xt ensure-websocket-client
  "returns the shared websocket client, creating it if necessary"
  {:added "4.1"}
  [impl opts]
  (var state (xt/x:get-key impl "state"))
  (var pubsub (or (xt/x:get-key state "pubsub") {}))
  (var client (xt/x:get-key pubsub "client"))
  (when (xt/x:not-nil? client)
    (return [client false]))
  (var config (-/websocket-config impl opts))
  (var ws-url (-/prepare-connect-url config))
  (:= client {"::" "js.net.ws_native/http_websocket_client"
              "defaults" (xt/x:obj-assign config {"url" ws-url})
              "heartbeats" {}})
  (websocket/connect client {})
  (websocket/add-listeners client {"message" (-/shared-message-handler impl)})
  (websocket/add-listeners client
                           {"open"
                            (fn [_event]
                              (-/on-open client impl)
                              (return true))})
  (xt/x:set-key pubsub "client" client)
  (xt/x:set-key state "pubsub" pubsub)
  (return [client true]))

(defn.xt subscribe
  "subscribes to a broadcast topic on the shared realtime websocket"
  {:added "4.1"}
  [impl topic opts callback]
  (:= opts (or opts {}))
  (var [client is-new] (-/ensure-websocket-client impl opts))
  (var join-payload (-/broadcast-join-payload impl opts))
  (var join-frame (phoenix/make-frame-join client
                                           join-payload
                                           {"topic" topic}))
  (var leave-frame (phoenix/make-frame-leave client
                                             {"topic" topic}))
  (var state (xt/x:get-key impl "state"))
  (var pubsub (or (xt/x:get-key state "pubsub") {}))
  (var topics (or (xt/x:get-key pubsub "topics") {}))
  (var id-counter (or (xt/x:get-key state "id_counter") 0))
  (var id (xt/x:cat "sub-" id-counter))
  (xt/x:set-key state "id_counter" (+ id-counter 1))
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
  (xt/x:set-key pubsub "topics" topics)
  (xt/x:set-key state "pubsub" pubsub)
  (when (not is-new)
    (phoenix/send-frame client join-frame))
  (return handle))

(defn.xt unsubscribe
  "leaves a topic and tears down the shared socket when no topics remain"
  {:added "4.1"}
  [impl handle]
  (var topic (xt/x:get-key handle "topic"))
  (var state (xt/x:get-key impl "state"))
  (var pubsub (or (xt/x:get-key state "pubsub") {}))
  (var topics (or (xt/x:get-key pubsub "topics") {}))
  (var client (xt/x:get-key pubsub "client"))
  (var entry (xt/x:get-key topics topic))
  (when (xt/x:not-nil? entry)
    (var leave-frame (xt/x:get-key entry "leave_frame"))
    (when (and (xt/x:not-nil? client) (xt/x:not-nil? leave-frame))
      (phoenix/send-frame client leave-frame))
    (xt/x:del-key topics topic)
    (xt/x:set-key pubsub "topics" topics))
  (when (== (xt/x:len (xt/x:obj-keys topics)) 0)
    (when (xt/x:not-nil? client)
      (http-fetch/stop-heartbeat client "pubsub")
      (websocket/disconnect client))
    (xt/x:set-key pubsub "client" nil))
  (xt/x:set-key state "pubsub" pubsub)
  (xt/x:set-key handle "active" false)
  (return true))

(defn.xt publish
  "publishing is not supported by the supabase realtime abstraction"
  {:added "4.1"}
  [impl topic message opts]
  (return (promise/x:promise-run nil)))
