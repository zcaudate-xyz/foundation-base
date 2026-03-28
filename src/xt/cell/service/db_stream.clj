(ns xt.cell.service.db-stream
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.xt stream-capable?
  "checks that the db descriptor can attach streams"
  {:added "4.0"}
  [db]
  (return (k/is-function? (k/get-key db "subscribe"))))

(defn.xt normalize-stream
  "normalizes a stream spec"
  {:added "4.0"}
  [db stream-spec view-context]
  (return {"target"         (or (k/get-key stream-spec "target")
                                (k/get-key stream-spec "db")
                                (k/get-key db "target")
                                (k/get-key view-context "target"))
           "topic"          (or (k/get-key stream-spec "topic")
                                (k/get-key view-context "topic"))
           "on_event"       (or (k/get-key stream-spec "on_event")
                                (k/get-key stream-spec "on-event")
                                "refresh")
           "subscribe"      (or (k/get-key stream-spec "subscribe")
                                (k/get-key db "subscribe")
                                (k/get-key view-context "subscribe"))
           "unsubscribe"    (or (k/get-key stream-spec "unsubscribe")
                                (k/get-key db "unsubscribe")
                                (k/get-key view-context "unsubscribe"))
           "event_to_update" (or (k/get-key stream-spec "event_to_update")
                                 (k/get-key db "event_to_update")
                                 (k/get-key view-context "event_to_update"))}))

(defn.xt subscription-key
  "builds a stable subscription key"
  {:added "4.0"}
  [db stream-spec view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (return (k/cat (or (k/get-key stream "target") "")
                 "::"
                 (k/json-encode (k/get-key stream "topic"))
                 "::"
                 (or (k/get-key view-context "view-id") "")
                 "::"
                 (or (k/get-key view-context "model-id") ""))))

(defn.xt subscribe-stream
  "subscribes to a stream source"
  {:added "4.0"}
  [db stream-spec on-event view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (var subscribe-fn (k/get-key stream "subscribe"))
  (when (not (k/is-function? subscribe-fn))
    (return [false {:status "error"
                    :tag "db/stream-subscribe-not-provided"}]))
  (return [true (subscribe-fn stream on-event view-context)]))

(defn.xt unsubscribe-stream
  "unsubscribes from a stream source"
  {:added "4.0"}
  [db stream-handle view-context]
  (var detach-fn (k/get-key stream-handle "detach_fn"))
  (when (k/is-function? detach-fn)
    (return [true (detach-fn)]))
  (var unsubscribe-fn (or (k/get-key stream-handle "unsubscribe")
                          (k/get-key db "unsubscribe")
                          (k/get-key view-context "unsubscribe")))
  (when (not (k/is-function? unsubscribe-fn))
    (return [false {:status "error"
                    :tag "db/stream-unsubscribe-not-provided"}]))
  (return [true (unsubscribe-fn stream-handle view-context)]))

(defn.xt event->update
  "maps a stream payload to an update descriptor"
  {:added "4.0"}
  [db stream-spec payload view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (var map-fn (k/get-key stream "event_to_update"))
  (when (k/is-function? map-fn)
    (return (map-fn payload view-context)))
  (var on-event (k/get-key stream "on_event"))
  (cond (== on-event "patch")
        (return {"type" "patch"
                 "body" payload})

        (== on-event "sync")
        (return {"type" "sync"
                 "body" payload})

        (== on-event "invalidate")
        (return {"type" "invalidate"
                 "body" payload})

        :else
        (return {"type" "refresh"
                 "view_id" (k/get-key view-context "view-id")
                 "body" payload})))

(defn.xt attach-stream
  "attaches a stream and converts payloads to updates"
  {:added "4.0"}
  [db stream-spec view-context on-update]
  (var sub-key (-/subscription-key db stream-spec view-context))
  (var wrapped
       (fn [payload]
         (var update (-/event->update db stream-spec payload view-context))
         (when (k/is-function? on-update)
           (on-update update))
         (return update)))
  (var [ok raw-handle] (-/subscribe-stream db stream-spec wrapped view-context))
  (when (not ok)
    (return [ok raw-handle]))
  (return [true {"key" sub-key
                 "stream" raw-handle
                 "detach_fn" (k/get-key raw-handle "detach_fn")
                 "unsubscribe" (k/get-key raw-handle "unsubscribe")}]))

(defn.xt detach-stream
  "detaches a previously attached stream"
  {:added "4.0"}
  [db stream-handle view-context]
  (return (-/unsubscribe-stream db stream-handle view-context)))

(def.xt MODULE (!:module))
