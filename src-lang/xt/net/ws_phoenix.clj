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

        (xt/x:not-nil? (xt/x:get-key message "data"))
        (return (xt/x:get-key message "data"))

        (xt/x:not-nil? (xt/x:get-key message "body"))
        (return (xt/x:get-key message "body"))

        :else
        (return message)))

(defn.xt decode-frame
  "decodes websocket message data into a phoenix frame.
   Phoenix v2 wire format is a JSON array: [join_ref, ref, topic, event, payload].
   This normalizes it back to the map representation used by callers."
  {:added "4.1.4"}
  [message]
  (var data (-/extract-message-data message))
  (when (not (xt/x:is-string? data))
    (return data))
  (var decoded (xt/x:json-decode data))
  (when (and (xt/x:is-array? decoded)
             (>= (xt/x:len decoded) 4))
    (return {"join_ref" (xt/x:get-idx decoded 0)
             "ref"      (xt/x:get-idx decoded 1)
             "topic"    (xt/x:get-idx decoded 2)
             "event"    (xt/x:get-idx decoded 3)
             "payload"  (or (xt/x:get-idx decoded 4) {})}))
  (return decoded))


;;
;;
;;

(defn.xt get-frame-ref
  "resolves a phoenix frame ref from opts"
  {:added "4.1.4"}
  [client opts]
  (return (xt/x:to-string
           (or (xt/x:get-key opts "ref")
               (xt/x:get-key opts "join_ref")
               (xt/x:now-ms)))))

(defn.xt make-frame
  "creates a generic phoenix frame as a map"
  {:added "4.1.4"}
  [client topic event payload opts]
  (var ref      (-/get-frame-ref client opts))
  (var join-ref (or (xt/x:get-key opts "join_ref")
                    ref))
  (return {"topic" topic
           "event" event
           "payload" (or payload {})
           "ref" ref
           "join_ref" join-ref}))

(defn.xt make-frame-join
  "creates a phoenix join frame"
  {:added "4.1.4"}
  [client payload opts]
  (var topic (xt/x:get-key opts "topic"))
  (when (xt/x:nil? topic)
    (xt/x:err "Phoenix channel missing topic"))
  (return (-/make-frame client
                        topic
                        "phx_join"
                        (or payload {})
                        (or opts {}))))

(defn.xt make-frame-leave
  "creates a phoenix leave frame"
  {:added "4.1.4"}
  [client opts]
  (var topic (xt/x:get-key opts "topic"))
  (when (xt/x:nil? topic)
    (xt/x:err "Phoenix channel missing topic"))
  (return (-/make-frame client topic "phx_leave" {} (or opts {}))))

(defn.xt make-frame-heartbeat
  "creates a phoenix heartbeat frame"
  {:added "4.1.4"}
  [client opts]
  (var ref (-/get-frame-ref client (or opts {})))
  (return {"topic" "phoenix"
           "event" "heartbeat"
           "payload" {}
           "ref" ref
           "join_ref" ref}))

(defn.xt encode-frame
  "encodes a phoenix frame map to the v2 wire array:
   [join_ref, ref, topic, event, payload]"
  {:added "4.1.4"}
  [frame]
  (return [(or (xt/x:get-key frame "join_ref")
               (xt/x:get-key frame "ref"))
           (xt/x:get-key frame "ref")
           (xt/x:get-key frame "topic")
           (xt/x:get-key frame "event")
           (or (xt/x:get-key frame "payload") {})]))

;;
;;
;;

(defn.xt send-frame
  "sends an encoded phoenix v2 frame over the websocket"
  {:added "4.1.4"}
  [client frame]
  (return (websocket/send client (xt/x:json-encode (-/encode-frame frame)))))

(defn.xt send-join
  "sends a phoenix join frame"
  {:added "4.1.4"}
  [client payload opts]
  (return (-/send-frame client
                        (-/make-frame-join client payload opts))))

(defn.xt send-leave
  "sends a phoenix leave frame"
  {:added "4.1.4"}
  [client opts]
  (return (-/send-frame client
                        (-/make-frame-leave client opts))))

(defn.xt send-heartbeat
  "sends a phoenix heartbeat frame"
  {:added "4.1.4"}
  [client opts]
  (return (-/send-frame client
                        (-/make-frame-heartbeat client (or opts {})))))

(defn.xt send
  "sends a phoenix push frame"
  {:added "4.1.4"}
  [client event payload opts]
  (return (-/send-frame client
                        (-/make-frame client
                                      (xt/x:get-key opts "topic")
                                      event
                                      payload opts))))

(defn.xt wrap-phoenix
  "returns a websocket 'message' listener that decodes phoenix frames
   and dispatches to the handler keyed by the frame's event name.
   Useful with websocket/add-listeners to route phx_reply, phx_join,
   phx_leave, presence_state, broadcast, and other channel events."
  {:added "4.1.4"}
  [handlers]
  (return (fn [event]
            (var frame (-/decode-frame event))
            (var handler (xt/x:get-key handlers (xt/x:get-key frame "event")))
            (when (xt/x:is-function? handler)
              (handler frame)))))
