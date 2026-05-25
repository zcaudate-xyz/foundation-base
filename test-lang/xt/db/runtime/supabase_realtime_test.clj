(ns xt.db.runtime.supabase-realtime-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.runtime.supabase-realtime :as realtime]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.supabase-realtime/prepare-connect-url :added "4.1.4"}
(fact "wraps websocket transports in a tagged supabase realtime client descriptor"

  (!.js
   (var driver
        (ws/driver-create
         {"connect_sync" (fn [url]
                           (return {"url" url
                                    "send" (fn [_] (return true))
                                    "close" (fn [code reason] (return true))
                                    "addEventListener" (fn [event-name listener] (return true))}))}))
   (var client
        (realtime/resolve-client
         {"client" {"transport" driver
                    "base_url" "https://db.test"
                    "api_key" "key-1"
                    "schema_name" "public"
                    "table_name" "Entry"}}
         {}))
   [(realtime/client? client)
    (ws/driver? (realtime/resolve-transport client))
    (xtd/get-in (realtime/create-scaffold nil client {}) ["schema_name"])
    (realtime/resolve-topic client {})
    (realtime/prepare-connect-url nil client {})])
  => [true
      true
      "public"
      "realtime:public:Entry"
      "wss://db.test/realtime/v1/websocket?vsn=1.0.0&apikey=key-1"])

^{:refer xt.db.runtime.supabase-realtime/postgres-change->sync-request :added "4.1.4"}
(fact "converts generic postgres_changes payloads into xt.db sync and remove requests"

  (!.js
   [(realtime/postgres-change->sync-request
     {"eventType" "INSERT"
      "table" "Entry"
      "new" {"id" "00000000-0000-0000-0000-0000000000d1"
             "name" "gamma"
             "tags" ["guide"]}}
     {}
     {})
    (realtime/postgres-change->sync-request
     {"type" "INSERT"
      "table" "Entry"
     "record" {"id" "00000000-0000-0000-0000-0000000000d9"
                "name" "theta"
                "tags" ["live"]}}
     {}
     {})
    (realtime/postgres-change->sync-request
     {"eventType" "DELETE"
     "table" "Entry"
     "old" {"id" "00000000-0000-0000-0000-0000000000d1"}}
     {}
     {})])
  => [{"db/sync"
       {"Entry"
        [{"id" "00000000-0000-0000-0000-0000000000d1"
          "name" "gamma"
          "tags" ["guide"]
          "__deleted__" false}]}}
     {"db/sync"
      {"Entry"
       [{"id" "00000000-0000-0000-0000-0000000000d9"
         "name" "theta"
         "tags" ["live"]
         "__deleted__" false}]}}
     {"db/remove"
       {"Entry" ["00000000-0000-0000-0000-0000000000d1"]}}])

^{:refer xt.db.runtime.supabase-realtime/apply-postgres-change :added "4.1.4"}
(fact "applies postgres_changes payloads directly into a cache db"

  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   (realtime/apply-postgres-change
    cache
    {"eventType" "INSERT"
     "table" "Entry"
     "new" {"id" "00000000-0000-0000-0000-0000000000d2"
            "name" "delta"
            "tags" ["sync"]}}
    {}
    {})
   (xdb/db-pull-sync
    cache
    (@! fixtures/+schema+)
    ["Entry"
     {"id" "00000000-0000-0000-0000-0000000000d2"}
     ["id" "name" "tags"]]))
  => [{"id" "00000000-0000-0000-0000-0000000000d2"
       "name" "delta"
       "tags" ["sync"]}]

  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   (xdb/sync-event cache
                   ["add"
                    {"Entry"
                     [{"id" "00000000-0000-0000-0000-0000000000d3"
                       "name" "epsilon"
                       "tags" ["sync"]
                       "__deleted__" false}]}])
   (realtime/apply-postgres-change
    cache
    {"eventType" "DELETE"
     "table" "Entry"
     "old" {"id" "00000000-0000-0000-0000-0000000000d3"}}
    {}
    {})
   (xdb/db-pull-sync
    cache
    (@! fixtures/+schema+)
    ["Entry"
     {"id" "00000000-0000-0000-0000-0000000000d3"}
     ["id" "name"]]))
  => [])

^{:refer xt.db.runtime.supabase-realtime/subscribe :added "4.1.4"}
(fact "subscribes through the websocket client protocol and applies incoming changes"

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
    (var connected [])
    (var closed [])
    (var statuses [])
    (var requests [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [url]
             (xt/x:arr-push connected url)
             (return {"send" (fn [payload]
                               (xt/x:arr-push sent payload)
                               (return true))
                      "close" (fn [code reason]
                                (xt/x:arr-push closed [code reason])
                                (return true))
                      "addEventListener" (fn [event handler]
                                           (xt/x:set-key handlers event handler)
                                           (return true))}))}))
    (promise/x:promise-then
     (realtime/subscribe
      {"client" {"transport" driver
                 "base_url" "https://db.test"
                 "api_key" "key-1"
                 "auth_token" "token-1"
                 "schema_name" "public"
                 "table_name" "Entry"}}
      cache
      {"on_status" (fn [status _frame]
                     (xt/x:arr-push statuses status))
       "on_request" (fn [request _payload _frame]
                      (xt/x:arr-push requests request))})
     (fn [sub]
       ((xt/x:get-key handlers "message")
        {"data"
         (xt/x:json-encode
          {"topic" "realtime:public:Entry"
           "event" "phx_reply"
           "payload" {"status" "ok"}})})
       ((xt/x:get-key handlers "message")
        {"data"
         (xt/x:json-encode
          {"topic" "realtime:public:Entry"
           "event" "postgres_changes"
           "payload" {"data" {"eventType" "INSERT"
                              "table" "Entry"
                              "new" {"id" "00000000-0000-0000-0000-0000000000d4"
                                     "name" "zeta"
                                     "tags" ["realtime"]}}}})})
       (return
        (promise/x:promise-then
         (realtime/unsubscribe sub)
         (fn [_]
           (repl/notify
            {"connected" (xt/x:first connected)
             "join-event" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["event"])
             "join-topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
             "join-table" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0))
                                      ["payload" "config" "postgres_changes" 0 "table"])
             "join-token" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0))
                                      ["payload" "access_token"])
             "leave-event" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 1)) ["event"])
             "status" (xt/x:first statuses)
             "request-name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])
             "cached-name" (xtd/get-in
                            (xdb/db-pull-sync
                             cache
                             (@! fixtures/+schema+)
                             ["Entry"
                              {"id" "00000000-0000-0000-0000-0000000000d4"}
                              ["name"]])
                            [0 "name"])
             "closed" (xt/x:get-idx closed 0)})))))))
  => {"connected" "wss://db.test/realtime/v1/websocket?vsn=1.0.0&apikey=key-1"
      "join-event" "phx_join"
      "join-topic" "realtime:public:Entry"
      "join-table" "Entry"
      "join-token" "token-1"
      "leave-event" "phx_leave"
      "status" "SUBSCRIBED"
      "request-name" "zeta"
      "cached-name" "zeta"
      "closed" [1000 "supabase-realtime/unsubscribe"]})
