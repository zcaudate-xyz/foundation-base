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
  "decodes websocket message data into a phoenix frame"
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
  [client opts]
  (return (xt/x:to-string
           (or (xt/x:get-key opts "ref")
               (xt/x:get-key opts "join_ref")
               (xt/x:now-ms)))))

(defn.xt make-frame
  "creates a generic phoenix frame"
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


;;
;;
;;

(defn.xt send-join
  "sends a phoenix join frame"
  {:added "4.1.4"}
  [client payload opts]
  (return (websocket/send client (xt/x:json-encode
                                  (-/make-frame-join client payload opts)))))

(defn.xt send-leave
  "sends a phoenix leave frame"
  {:added "4.1.4"}
  [client opts]
  (return (websocket/send client (xt/x:json-encode
                                  (-/make-frame-leave client opts)))))

(defn.xt send
  "sends a phoenix push frame"
  {:added "4.1.4"}
  [client event payload opts]
  (return (websocket/send client (xt/x:json-encode
                                  (-/make-frame client
                                                (xt/x:get-key opts "topic")
                                                event
                                                payload opts)))))
