(ns js.cell.service.db-stream
  (:require [std.lang :as l]))

(l/script :xtalk
  {:export [MODULE] :require [[xt.lang.common-spec :as xt] [xt.lang.common-string :as str]]})

(defn.xt stream-capable?
  "checks that the db descriptor can attach streams"
  {:added "4.0"}
  [db]
  (return (xt/x:is-function? (xt/x:get-key db "subscribe"))))

(defn.xt normalize-stream
  "normalizes a stream spec"
  {:added "4.0"}
  [db stream-spec view-context]
  (return {"target"         (or (xt/x:get-key stream-spec "target")
                                (xt/x:get-key stream-spec "db")
                                (xt/x:get-key db "target")
                                (xt/x:get-key view-context "target"))
           "topic"          (or (xt/x:get-key stream-spec "topic")
                                (xt/x:get-key view-context "topic"))
           "on_event"       (or (xt/x:get-key stream-spec "on_event")
                                (xt/x:get-key stream-spec "on-event")
                                "refresh")
           "subscribe"      (or (xt/x:get-key stream-spec "subscribe")
                                (xt/x:get-key db "subscribe")
                                (xt/x:get-key view-context "subscribe"))
           "unsubscribe"    (or (xt/x:get-key stream-spec "unsubscribe")
                                (xt/x:get-key db "unsubscribe")
                                (xt/x:get-key view-context "unsubscribe"))
           "event_to_update" (or (xt/x:get-key stream-spec "event_to_update")
                                 (xt/x:get-key db "event_to_update")
                                 (xt/x:get-key view-context "event_to_update"))}))

(defn.xt subscription-key
  "builds a stable subscription key"
  {:added "4.0"}
  [db stream-spec view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (return (str/join "::"
                    [(or (xt/x:get-key stream "target") "")
                     (xt/x:json-encode (xt/x:get-key stream "topic"))
                     (or (xt/x:get-key view-context "view-id") "")
                     (or (xt/x:get-key view-context "model-id") "")])))

(defn.xt subscribe-stream
  "subscribes to a stream source"
  {:added "4.0"}
  [db stream-spec on-event view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (var subscribe-fn (xt/x:get-key stream "subscribe"))
  (when (not (xt/x:is-function? subscribe-fn))
    (return [false {:status "error"
                    :tag "db/stream-subscribe-not-provided"}]))
  (return [true (subscribe-fn stream on-event view-context)]))

(defn.xt unsubscribe-stream
  "unsubscribes from a stream source"
  {:added "4.0"}
  [db stream-handle view-context]
  (var detach-fn (xt/x:get-key stream-handle "detach_fn"))
  (when (xt/x:is-function? detach-fn)
    (return [true (detach-fn)]))
  (var unsubscribe-fn (or (xt/x:get-key stream-handle "unsubscribe")
                          (xt/x:get-key db "unsubscribe")
                          (xt/x:get-key view-context "unsubscribe")))
  (when (not (xt/x:is-function? unsubscribe-fn))
    (return [false {:status "error"
                    :tag "db/stream-unsubscribe-not-provided"}]))
  (return [true (unsubscribe-fn stream-handle view-context)]))

(defn.xt event->update
  "maps a stream payload to an update descriptor"
  {:added "4.0"}
  [db stream-spec payload view-context]
  (var stream (-/normalize-stream db stream-spec view-context))
  (var map-fn (xt/x:get-key stream "event_to_update"))
  (when (xt/x:is-function? map-fn)
    (return (map-fn payload view-context)))
  (var on-event (xt/x:get-key stream "on_event"))
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
                 "view_id" (xt/x:get-key view-context "view-id")
                 "body" payload})))

(defn.xt attach-stream
  "attaches a stream and converts payloads to updates"
  {:added "4.0"}
  [db stream-spec view-context on-update]
  (var sub-key (-/subscription-key db stream-spec view-context))
  (var wrapped
       (fn [payload]
         (var update (-/event->update db stream-spec payload view-context))
         (when (xt/x:is-function? on-update)
           (on-update update))
         (return update)))
  (var [ok raw-handle] (-/subscribe-stream db stream-spec wrapped view-context))
  (when (not ok)
    (return [ok raw-handle]))
  (return [true {"key" sub-key
                 "stream" raw-handle
                 "detach_fn" (xt/x:get-key raw-handle "detach_fn")
                 "unsubscribe" (xt/x:get-key raw-handle "unsubscribe")}]))

(defn.xt detach-stream
  "detaches a previously attached stream"
  {:added "4.0"}
  [db stream-handle view-context]
  (return (-/unsubscribe-stream db stream-handle view-context)))

(def.xt MODULE (!:module))
