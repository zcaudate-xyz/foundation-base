(ns xt.db.runtime.event-supabase-test
  (:require [hara.lang :as l]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify]
            [xt.db.runtime.event-supabase :as event-supabase])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.runtime.event-supabase :as event-supabase]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.event-supabase/broadcast-client :added "4.1.4"}
(fact "marks a client config as a broadcast subscription"
  (!.js
   (event-supabase/broadcast-client
    {"base_url" "https://db.test"
     "topic" "room:old"}
    {"topic" "room:new"}))
  => {"base_url" "https://db.test"
      "topic" "room:new"
      "message_event" "broadcast"})

^{:refer xt.db.runtime.event-supabase/subscribe-broadcast :added "4.1.4"}
(fact "subscribes to broadcast topics carrying native xt.db requests"
  (notify/wait-on [:js 2000]
    (var cache
         (xtd/obj-assign
          (xdb/db-create {"::" "db.cache"}
                         (@! fixtures/+schema+)
                         (@! fixtures/+lookup+)
                         nil)
          {"schema" (@! fixtures/+schema+)}))
    (var handlers {})
    (var sent [])
    (var requests [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [_url]
             (return {"send" (fn [payload]
                               (xt/x:arr-push sent payload)
                               (return true))
                      "close" (fn [_code _reason]
                                (return true))
                      "addEventListener" (fn [event handler]
                                           (xt/x:set-key handlers event handler)
                                           (return true))}))}))
    (promise/x:promise-then
     (event-supabase/subscribe-broadcast
      {"client" {"transport" driver
                 "base_url" "https://db.test"
                 "api_key" "key-1"
                 "topic" "room:entries"}}
      cache
      {"on_request" (fn [request _payload _frame]
                      (xt/x:arr-push requests request))})
     (fn [sub]
       ((xt/x:get-key handlers "message")
        {"data"
         (xt/x:json-encode
          {"topic" "realtime:room:entries"
           "event" "phx_reply"
           "payload" {"status" "ok"}})})
       ((xt/x:get-key handlers "message")
        {"data"
         (xt/x:json-encode
          {"topic" "realtime:room:entries"
           "event" "broadcast"
           "payload" {"data" {"db/sync"
                              {"Entry"
                               [{"id" "00000000-0000-0000-0000-0000000000ec"
                                 "name" "event-supabase"
                                 "tags" ["broadcast"]
                                 "__deleted__" false}]}}}})})
       (promise/x:promise-then
        (event-supabase/unsubscribe sub)
        (fn [_]
          (repl/notify
           {"join-topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
            "request-name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])}))))))
  => {"join-topic" "realtime:room:entries"
      "request-name" "event-supabase"})
