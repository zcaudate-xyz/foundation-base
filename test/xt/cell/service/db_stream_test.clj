(ns xt.cell.service.db-stream-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:require [[xt.cell.service.db-stream :as db-stream] [xt.lang.spec-base :as xt] [xt.lang.common-lib :as k]] :runtime :basic})

(l/script- :lua
  {:require [[xt.cell.service.db-stream :as db-stream] [xt.lang.spec-base :as xt] [xt.lang.common-lib :as k]] :runtime :basic})

(l/script- :python
  {:require [[xt.cell.service.db-stream :as db-stream] [xt.lang.spec-base :as xt] [xt.lang.common-lib :as k]] :runtime :basic})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-stream/stream-capable? :added "4.1"}
(fact "checks whether a descriptor can attach streams"

  (!.js
   (var db {"subscribe" (fn [stream on-event view-context]
                          (return {"id" "sub-1"}))})
   [(db-stream/stream-capable? db)
    (db-stream/stream-capable? {})])
  => [true false]

  (!.lua
   (var db {"subscribe" (fn [stream on-event view-context]
                          (return {"id" "sub-1"}))})
   [(db-stream/stream-capable? db)
    (db-stream/stream-capable? {})])
  => [true false]

  (!.py
   (var db {"subscribe" (fn [stream on-event view-context]
                          (return {"id" "sub-1"}))})
   [(db-stream/stream-capable? db)
    (db-stream/stream-capable? {})])
  => [true false])

^{:refer xt.cell.service.db-stream/normalize-stream :added "4.1"}
(fact "normalizes topic and stream helpers from the descriptor"

  (!.js
   (var db {"target" "supabase-main"
            "subscribe" (fn [stream on-event view-context]
                           (return {"id" "sub-1"}))})
   (var stream (db-stream/normalize-stream
                db
                {"topic" ["orders" "acct-1"]
                 "on-event" "patch"}
                {}))
   [(xt/x:get-key stream "target")
    (xt/x:get-key stream "topic")
    (xt/x:get-key stream "on_event")
    (k/is-function? (xt/x:get-key stream "subscribe"))])
  => ["supabase-main"
      ["orders" "acct-1"]
      "patch"
      true]

  (!.lua
   (var db {"target" "supabase-main"
            "subscribe" (fn [stream on-event view-context]
                           (return {"id" "sub-1"}))})
   (var stream (db-stream/normalize-stream
                db
                {"topic" ["orders" "acct-1"]
                 "on-event" "patch"}
                {}))
   [(xt/x:get-key stream "target")
    (xt/x:get-key stream "topic")
    (xt/x:get-key stream "on_event")
    (k/is-function? (xt/x:get-key stream "subscribe"))])
  => ["supabase-main"
      ["orders" "acct-1"]
      "patch"
      true]

  (!.py
   (var db {"target" "supabase-main"
            "subscribe" (fn [stream on-event view-context]
                           (return {"id" "sub-1"}))})
   (var stream (db-stream/normalize-stream
                db
                {"topic" ["orders" "acct-1"]
                 "on-event" "patch"}
                {}))
   [(xt/x:get-key stream "target")
    (xt/x:get-key stream "topic")
    (xt/x:get-key stream "on_event")
    (k/is-function? (xt/x:get-key stream "subscribe"))])
  => ["supabase-main"
      ["orders" "acct-1"]
      "patch"
      true])

^{:refer xt.cell.service.db-stream/subscription-key :added "4.1"}
(fact "builds a stable subscription key"

  (!.js
   (db-stream/subscription-key
    {"target" "supabase-main"}
    {"topic" ["orders" "acct-1"]}
    {"view-id" "orders/live"
     "model-id" "orders"}))
  => "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders"

  (!.lua
   (db-stream/subscription-key
    {"target" "supabase-main"}
    {"topic" ["orders" "acct-1"]}
    {"view-id" "orders/live"
     "model-id" "orders"}))
  => "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders"

  (!.py
   (db-stream/subscription-key
    {"target" "supabase-main"}
    {"topic" ["orders" "acct-1"]}
    {"view-id" "orders/live"
     "model-id" "orders"}))
  => "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders")

^{:refer xt.cell.service.db-stream/subscribe-stream :added "4.1"}
(fact "subscribes through the configured stream source"

  (!.js
   (var [ok handle] (db-stream/subscribe-stream
                     {"subscribe" (fn [stream on-event view-context]
                                    (return {"id" "sub-1"
                                             "detach_fn" (fn [] (return "stopped"))}))}
                     {"topic" ["orders" "acct-1"]}
                     (fn [payload] (return payload))
                     {}))
   [ok
    (xt/x:get-key handle "id")
    (k/is-function? (xt/x:get-key handle "detach_fn"))])
  => [true "sub-1" true]

  (!.lua
   (var [ok handle] (db-stream/subscribe-stream
                     {"subscribe" (fn [stream on-event view-context]
                                    (return {"id" "sub-1"
                                             "detach_fn" (fn [] (return "stopped"))}))}
                     {"topic" ["orders" "acct-1"]}
                     (fn [payload] (return payload))
                     {}))
   [ok
    (xt/x:get-key handle "id")
    (k/is-function? (xt/x:get-key handle "detach_fn"))])
  => [true "sub-1" true]

  (!.py
   (var [ok handle] (db-stream/subscribe-stream
                     {"subscribe" (fn [stream on-event view-context]
                                    (return {"id" "sub-1"
                                             "detach_fn" (fn [] (return "stopped"))}))}
                     {"topic" ["orders" "acct-1"]}
                     (fn [payload] (return payload))
                     {}))
   [ok
    (xt/x:get-key handle "id")
    (k/is-function? (xt/x:get-key handle "detach_fn"))])
  => [true "sub-1" true])

^{:refer xt.cell.service.db-stream/unsubscribe-stream :added "4.1"}
(fact "unsubscribes using the stream handle"

  (!.js
   (db-stream/unsubscribe-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"]

  (!.lua
   (db-stream/unsubscribe-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"]

  (!.py
   (db-stream/unsubscribe-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"])

^{:refer xt.cell.service.db-stream/event->update :added "4.1"}
(fact "maps payloads into update descriptors"

  (!.js
   [(db-stream/event->update
     {}
     {"on-event" "patch"}
     {"id" "ord-1"}
     {})
    (db-stream/event->update
     {}
     {"on-event" "refresh"}
     {"id" "ord-1"}
     {"view-id" "orders/live"})
    (db-stream/event->update
     {}
     {"event_to_update" (fn [payload _]
                          (return {"type" "sync"
                                   "body" payload}))}
     {"id" "ord-1"}
     {})])
  => [{"type" "patch"
       "body" {"id" "ord-1"}}
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}
      {"type" "sync"
       "body" {"id" "ord-1"}}]

  (!.lua
   [(db-stream/event->update
     {}
     {"on-event" "patch"}
     {"id" "ord-1"}
     {})
    (db-stream/event->update
     {}
     {"on-event" "refresh"}
     {"id" "ord-1"}
     {"view-id" "orders/live"})
    (db-stream/event->update
     {}
     {"event_to_update" (fn [payload _]
                          (return {"type" "sync"
                                   "body" payload}))}
     {"id" "ord-1"}
     {})])
  => [{"type" "patch"
       "body" {"id" "ord-1"}}
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}
      {"type" "sync"
       "body" {"id" "ord-1"}}]

  (!.py
   [(db-stream/event->update
     {}
     {"on-event" "patch"}
     {"id" "ord-1"}
     {})
    (db-stream/event->update
     {}
     {"on-event" "refresh"}
     {"id" "ord-1"}
     {"view-id" "orders/live"})
    (db-stream/event->update
     {}
     {"event_to_update" (fn [payload _]
                          (return {"type" "sync"
                                   "body" payload}))}
     {"id" "ord-1"}
     {})])
  => [{"type" "patch"
       "body" {"id" "ord-1"}}
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}
      {"type" "sync"
       "body" {"id" "ord-1"}}])

^{:refer xt.cell.service.db-stream/attach-stream :added "4.1"}
(fact "attaches a stream and forwards mapped updates"

  (!.js
   (var captured nil)
   (var db {"subscribe" (fn [stream on-event view-context]
                          (on-event {"id" "ord-1"})
                          (return {"id" "sub-1"
                                   "detach_fn" (fn [] (return "stopped"))}))})
   (var [ok handle] (db-stream/attach-stream
                     db
                     {"target" "supabase-main"
                      "topic" ["orders" "acct-1"]
                      "on-event" "refresh"}
                     {"view-id" "orders/live"
                      "model-id" "orders"}
                     (fn [update]
                       (:= captured update)
                       (return update))))
   [ok
    (xt/x:get-key handle "key")
    captured])
  => [true
      "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders"
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}]

  (!.lua
   (var captured nil)
   (var db {"subscribe" (fn [stream on-event view-context]
                          (on-event {"id" "ord-1"})
                          (return {"id" "sub-1"
                                   "detach_fn" (fn [] (return "stopped"))}))})
   (var [ok handle] (db-stream/attach-stream
                     db
                     {"target" "supabase-main"
                      "topic" ["orders" "acct-1"]
                      "on-event" "refresh"}
                     {"view-id" "orders/live"
                      "model-id" "orders"}
                     (fn [update]
                       (:= captured update)
                       (return update))))
   [ok
    (xt/x:get-key handle "key")
    captured])
  => [true
      "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders"
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}]

  (!.py
   (var captured nil)
   (var db {"subscribe" (fn [stream on-event view-context]
                          (on-event {"id" "ord-1"})
                          (return {"id" "sub-1"
                                   "detach_fn" (fn [] (return "stopped"))}))})
   (var [ok handle] (db-stream/attach-stream
                     db
                     {"target" "supabase-main"
                      "topic" ["orders" "acct-1"]
                      "on-event" "refresh"}
                     {"view-id" "orders/live"
                      "model-id" "orders"}
                     (fn [update]
                       (:= captured update)
                       (return update))))
   [ok
    (xt/x:get-key handle "key")
    captured])
  => [true
      "supabase-main::[\"orders\",\"acct-1\"]::orders/live::orders"
      {"type" "refresh"
       "view_id" "orders/live"
       "body" {"id" "ord-1"}}])

^{:refer xt.cell.service.db-stream/detach-stream :added "4.1"}
(fact "detaches a previously attached stream"

  (!.js
   (db-stream/detach-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"]

  (!.lua
   (db-stream/detach-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"]

  (!.py
   (db-stream/detach-stream
    {}
    {"detach_fn" (fn [] (return "stopped"))}
    {}))
  => [true "stopped"])
