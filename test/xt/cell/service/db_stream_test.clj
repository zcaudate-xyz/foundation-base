(ns xt.cell.service.db-stream-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-stream :as db-stream]
             [xt.lang.common-data :as xtd]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-stream/stream-capable? :added "4.1"}
(fact "checks whether a db descriptor supports streaming"

  (!.js
   [(db-stream/stream-capable? {"subscribe" (fn:> [stream on-event ctx] {})})
    (db-stream/stream-capable? {})])
  => [true false])

^{:refer xt.cell.service.db-stream/normalize-stream :added "4.1"}
(fact "normalizes stream settings from the available contexts"

  (!.js
   (db-stream/normalize-stream
    {"target" "db-target"
     "subscribe" "db-sub"}
    {"topic" {"table" "Order"}
     "on_event" "patch"}
    {"unsubscribe" "ctx-unsub"}))
  => {"target" "db-target"
      "topic" {"table" "Order"}
      "on_event" "patch"
      "subscribe" "db-sub"
      "unsubscribe" "ctx-unsub"})

^{:refer xt.cell.service.db-stream/subscription-key :added "4.1"}
(fact "builds a stable stream subscription key"

  (!.js
   (db-stream/subscription-key
    {}
    {"target" "orders-db"
     "topic" {"table" "Order"}}
    {"view-id" "list"
     "model-id" "orders"}))
  => "orders-db::{\"table\":\"Order\"}::list::orders")

^{:refer xt.cell.service.db-stream/subscribe-stream :added "4.1"}
(fact "subscribes through the resolved subscribe function"

  (!.js
   [(db-stream/subscribe-stream
     {}
     {"subscribe" (fn [stream on-event ctx]
                    (return {}))}
     (fn:> [payload] payload)
     {"view-id" "list"})
    (db-stream/subscribe-stream {} {} (fn:>) {})])
  => [[true {}]
      [false
       {"status" "error"
        "tag" "db/stream-subscribe-not-provided"}]])

^{:refer xt.cell.service.db-stream/unsubscribe-stream :added "4.1"}
(fact "prefers detach_fn and falls back to unsubscribe"

  (!.js
   [(db-stream/unsubscribe-stream
     {}
     {"detach_fn" (fn [] (return "detached"))}
     {})
    (db-stream/unsubscribe-stream
     {"unsubscribe" (fn [handle ctx]
                      (return {"key" handle.key}))}
     {"key" "sub-1"}
     {"model-id" "orders"})])
  => [[true "detached"]
      [true {"key" "sub-1"}]])

^{:refer xt.cell.service.db-stream/event->update :added "4.1"}
(fact "maps stream payloads into update descriptors"

  (!.js
   [(db-stream/event->update
     {}
     {"on_event" "patch"}
     {"id" 1}
     {})
    (db-stream/event->update
     {}
     {}
     {"id" 2}
     {"view-id" "list"})
    (db-stream/event->update
     {}
     {"event_to_update" (fn [payload ctx]
                          (return {"type" "custom"
                                   "body" payload}))}
     {"id" 3}
     {"view-id" "list"})])
  => [{"type" "patch"
       "body" {"id" 1}}
      {"type" "refresh"
       "view_id" "list"
       "body" {"id" 2}}
      {"type" "custom"
       "body" {"id" 3}}])

^{:refer xt.cell.service.db-stream/attach-stream :added "4.1"}
(fact "attaches a stream and forwards mapped updates to the callback"

  (!.js
   (var seen nil)
   (var out
        (db-stream/attach-stream
         {"subscribe" (fn [stream on-event ctx]
                        (on-event {"id" 1})
                        (return {"detach_fn" (fn [] (return "detached"))}))}
         {"target" "orders-db"
          "topic" {"table" "Order"}}
         {"view-id" "list"
          "model-id" "orders"}
         (fn [update]
           (:= seen update))))
  [out seen])
  => [[true
       {"key" "orders-db::{\"table\":\"Order\"}::list::orders"
        "stream" {}}]
      {"type" "refresh"
       "view_id" "list"
       "body" {"id" 1}}])

^{:refer xt.cell.service.db-stream/detach-stream :added "4.1"}
(fact "detaches an attached stream through unsubscribe-stream"

  (!.js
   (db-stream/detach-stream
    {}
    {"detach_fn" (fn [] (return "detached"))}
    {}))
  => [true "detached"])
