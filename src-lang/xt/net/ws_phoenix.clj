(ns xt.net.ws-phoenix
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]]})

(def.xt IPhxFrame
  ["encode_frame"
   "decode_frame"
   "frame"
   "join_frame"
   "leave_frame"
   "push_frame"
   "send_frame"
   "join"
   "leave"
   "push"])


(defn.xt extract-message-data
  "extracts websocket text from a raw event or payload wrapper"
  {:added "4.1.4"}
  [message]
  (cond (xt/x:is-string? message)
        (return message)

        (xt/x:not-nil? (. message ["data"]))
        (return (. message ["data"]))

        (xt/x:not-nil? (. message ["body"]))
        (return (. message ["body"]))

        :else
        (return message)))

(defn.xt decode-frame
  "decodes websocket message data into a phoenix frame.
   Phoenix v1.0.0 wire format is a JSON object:
   {join_ref, ref, topic, event, payload}."
  {:added "4.1.4"}
  [message]
  (var data (-/extract-message-data message))
  (when (not (xt/x:is-string? data))
    (return data))
  (return (xt/x:json-decode data)))


;;
;;
;;

(defn.xt get-frame-ref
  "resolves a phoenix frame ref from opts"
  {:added "4.1.4"}
  [opts]
  (return (xt/x:to-string
           (or (. opts ["ref"])
               (. opts ["join_ref"])
               (xt/x:now-ms)))))

(defn.xt make-frame
  "creates a generic phoenix frame as a map"
  {:added "4.1.4"}
  [topic event payload opts]
  (var ref      (-/get-frame-ref opts))
  (var join-ref (or (. opts ["join_ref"])
                    ref))
  (return {"topic" topic
           "event" event
           "payload" (or payload {})
           "ref" ref
           "join_ref" join-ref}))

(defn.xt make-frame-join
  "creates a phoenix join frame"
  {:added "4.1.4"}
  [payload opts]
  (var topic (. opts ["topic"]))
  (when (xt/x:nil? topic)
    (xt/x:err "Phoenix channel missing topic"))
  (return (-/make-frame topic
                        "phx_join"
                        (or payload {})
                        (or opts {}))))

(defn.xt make-frame-leave
  "creates a phoenix leave frame"
  {:added "4.1.4"}
  [opts]
  (var topic (. opts ["topic"]))
  (when (xt/x:nil? topic)
    (xt/x:err "Phoenix channel missing topic"))
  (return (-/make-frame topic "phx_leave" {} (or opts {}))))

(defn.xt make-frame-heartbeat
  "creates a phoenix heartbeat frame"
  {:added "4.1.4"}
  [opts]
  (var ref (-/get-frame-ref (or opts {})))
  (return {"topic" "phoenix"
           "event" "heartbeat"
           "payload" {}
           "ref" ref
           "join_ref" ref}))

(defn.xt encode-frame
  "encodes a phoenix frame map to the v1.0.0 wire object:
   {join_ref, ref, topic, event, payload}"
  {:added "4.1.4"}
  [frame]
  (return {"join_ref" (or (. frame ["join_ref"])
                          (. frame ["ref"]))
           "ref"      (. frame ["ref"])
           "topic"    (. frame ["topic"])
           "event"    (. frame ["event"])
           "payload"  (or (. frame ["payload"]) {})}))

;;
;;
;;

(defn.xt send-frame
  "sends an encoded phoenix v1.0.0 frame over the websocket"
  {:added "4.1.4"}
  [client frame]
  (return (websocket/send client (xt/x:json-encode (-/encode-frame frame)))))

(defn.xt wrap-phoenix
  "returns a websocket 'message' listener that decodes phoenix frames
   and dispatches to the handler keyed by the frame's event name.
   Useful with websocket/add-listeners to route phx_reply, phx_join,
   phx_leave, presence_state, broadcast, and other channel events."
  {:added "4.1.4"}
  [handlers]
  (return (fn [event]
            (var frame (-/decode-frame event))
            (var handler (xt/x:get-key handlers (. frame ["event"])))
            (when (xt/x:is-function? handler)
              (handler frame)))))


(defn.xt start-heartbeat
  "sends pending join frames and starts heartbeat when the socket opens"
  {:added "4.1"}
  [client]
  (return
   (websocket/start-heartbeat client
                              "phoenix.default"
                              (fn [client name]
                                (-/send-frame client
                                              (-/make-frame-heartbeat {})))
                              30000)))
