(ns xt.db.system.impl-supabase-realtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.impl-common-ws :as common-ws]
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


;;
;; Util Functions
;;

(defn.xt prepare-connect-url
  "prepares the websocket url used to connect to realtime"
  {:added "4.1"}
  [impl params]
  (var #{client} impl)
  (var #{defaults} client)
  (var path (xt/x:cat "/realtime/v1/websocket"
                      "?"
                      (http-util/encode-query-params
                       (xt/x:obj-assign
                        {"vsn"   "1.0.0"
                         "apikey" (xt/x:get-key defaults "apikey")}
                        params))))
  (return
   (websocket/prepare-url client {"path" path})))


(defn.xt get-auth-token
  "resolves the realtime auth token from the impl session or client defaults"
  {:added "4.1"}
  [impl]
  (return (or (xtd/get-in impl ["state" "session" "access_token"])
              (xtd/get-in impl ["client" "defaults" "apikey"]))))

(defn.xt topic-join-payload
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [impl topic]
  (var auth-token (-/get-auth-token impl))
  (return
   (phoenix/make-frame-join
    {"config" {"broadcast" {"ack" false "self" false}}
     "access_token" auth-token}
    {"topic" topic
     "ref" (xt/x:cat "#/join/" topic)})))

(defn.xt topic-leave-payload
  "builds the Phoenix join payload for a broadcast-only channel"
  {:added "4.1"}
  [impl topic]
  (return
   (phoenix/make-frame-leave
    {"topic" topic
     "ref" (xt/x:cat "#/leave/" topic)})))

;;
;; Realtime Client Management
;;

(defn.xt create-realtime-on-message
  [realtime-client]
  (return
   (phoenix/wrap-phoenix
    {"broadcast"
     (fn [frame]
       (var envelope (xt/x:get-key frame "payload"))
       (when (== "xt.db/event" (xt/x:get-key envelope "event"))
         (var payload (xt/x:get-key envelope "payload"))
         (var topic (xt/x:get-key frame "topic"))
         (var callbacks (xtd/get-in realtime-client ["state" "callbacks"]))
         (xt/for:object [[_id callback] callbacks]
           (callback (xt/x:obj-assign {"topic" topic}
                                      payload)))))
     "phx_reply"
     (fn [frame]
       (var join-ref (xt/x:get-key frame "join_ref"))
       (var topics   (xtd/get-in realtime-client ["state" "topics"]))
       (when (xt/x:not-nil? join-ref)
         (xt/for:object [[topic entry] topics]
           (when (== (xt/x:cat "#/join/" topic) join-ref)
             (var status (xtd/get-in frame ["payload" "status"]))
             (var ok  (== status "ok"))
             (var deferred  (xt/x:get-key entry "deferred"))
             (var resolve   (xt/x:get-key deferred "resolve"))
             (when (xt/x:is-function? resolve)
               (resolve ok))
             (xtd/set-in realtime-client ["state" "topics" topic "ready"] ok)))))})))

(defn.xt create-realtime
  "returns the realtime websocket client for id, creating it if necessary"
  {:added "4.1"}
  [impl conn-id]
  (var realtime-client (common-ws/create-ws-client {"id" conn-id}))
  (var ws-url (-/prepare-connect-url impl {}))
  (var init   (websocket/connect realtime-client
                                 {"url" ws-url}))
  (xtd/set-in realtime-client ["state" "init"] init)
  (websocket/add-listeners realtime-client
                           {"open"
                            (fn [raw]
                              (phoenix/start-heartbeat realtime-client))
                            "message"
                            (-/create-realtime-on-message realtime-client)})
  (return realtime-client))

(defn.xt get-realtime
  "returns the realtime websocket client for the given id from the impl state"
  {:added "4.1"}
  [impl conn-id]
  (return (xtd/get-in impl ["state" "realtimes" conn-id])))

(defn.xt set-realtime
  "stores the realtime websocket client for the given id in the impl state"
  {:added "4.1"}
  [impl conn-id client]
  (xtd/set-in impl ["state" "realtimes" conn-id] client)
  (return client))

(defn.xt ensure-realtime
  "returns the realtime websocket client for id, creating it if necessary"
  {:added "4.1"}
  [impl conn-id]
  (var client (-/get-realtime impl conn-id))
  (when (xt/x:not-nil? client)
    (return client))
  (:= client (-/create-realtime impl conn-id))
  (-/set-realtime impl conn-id client)
  (return client))

(defn.xt remove-realtime
  "disconnects and removes the realtime websocket client for id"
  {:added "4.1"}
  [impl conn-id]
  (var client (-/get-realtime impl conn-id))
  (when (xt/x:not-nil? client)
    (var topics (xtd/get-in client ["state" "topics"]))
    (xt/for:object [[topic entry] topics]
      (var deferred (xt/x:get-key entry "deferred"))
      (var resolve (xt/x:get-key deferred "resolve"))
      (when (xt/x:is-function? resolve)
        (resolve false)))
    (websocket/disconnect client))
  (xt/x:del-key (xtd/get-in impl ["state" "realtimes"]) conn-id)
  (return client))

(defn.xt get-realtime-callback
  "gets a broadcast callback from the realtime client"
  {:added "4.1"}
  [impl conn-id callback-id]
  (return
   (xtd/get-in (-/get-realtime impl conn-id) ["state" "callbacks" callback-id])))

(defn.xt add-realtime-callback
  "adds a callback to be invoked on xt.db/event broadcasts for the realtime client"
  {:added "4.1"}
  [impl conn-id callback-id handler]
  (var client (-/ensure-realtime impl conn-id))
  (var callbacks (xtd/get-in client ["state" "callbacks"]))
  (xt/x:set-key callbacks callback-id handler)
  (return handler))

(defn.xt remove-realtime-callback
  "removes a broadcast callback from the realtime client"
  {:added "4.1"}
  [impl conn-id callback-id]
  (var client (-/get-realtime impl conn-id))
  (when (xt/x:not-nil? client)
    (var callbacks (xtd/get-in client ["state" "callbacks"]))
    (xt/x:del-key callbacks callback-id))
  (return true))

(defn.xt get-topics
  "returns the map of subscribed topics for the realtime client"
  {:added "4.1"}
  [impl conn-id]
  (var client (-/get-realtime impl conn-id))
  (if (xt/x:not-nil? client)
    (return (xtd/get-in client ["state" "topics"]))
    (return {})))

(defn.xt subscribe
  "subscribes to one or more broadcast topics on the realtime websocket"
  {:added "4.1"}
  [impl conn-id topics]
  (var client (-/ensure-realtime impl conn-id))
  (xt/x:arr-map topics
                (fn [topic]
                  (var join-ref (xt/x:cat "#/join/" topic))
                  (var deferred {"resolve" nil "reject" nil})
                  (var init (promise/x:promise-new
                             (fn [resolve reject]
                               (xt/x:set-key deferred "resolve" resolve)
                               (xt/x:set-key deferred "reject" reject))))
                  (xtd/set-in client
                              ["state" "topics" topic]
                              {"init" init
                               "join_ref" join-ref
                               "deferred" deferred
                               "ready" false})))
  (return
   (promise/x:promise-then
    (xtd/get-in client ["state" "init"])
    (fn [_]
      (xt/x:arr-map topics
                    (fn [topic]
                      (phoenix/send-frame client (-/topic-join-payload impl topic))))
      (return (promise/x:promise-all
               (xt/x:arr-map topics
                             (fn [topic]
                               (xtd/get-in client ["state" "topics" topic "init"])))))))))

(defn.xt unsubscribe
  "unsubscribes from one or more broadcast topics on the realtime websocket"
  {:added "4.1"}
  [impl conn-id topics]
  (return
   (promise/x:promise
    (fn []
      (var client (-/get-realtime impl conn-id))
      (when (xt/x:not-nil? client)
        (xt/x:arr-map topics
                      (fn [topic]
                        (var entry (xtd/get-in client ["state" "topics" topic]))
                        (when (xt/x:not-nil? entry)
                          (phoenix/send-frame client (-/topic-leave-payload impl topic))
                          (xt/x:del-key (xtd/get-in client ["state" "topics"]) topic)))))
      (return true)))))
