(ns xt.db.runtime.supabase-realtime-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.db.helpers.supabase-pull-live-test :as live])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-websocket :as js-ws]
            [xt.db.runtime :as xdb]
             [xt.db.runtime.supabase-realtime :as realtime]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)
          (do (live/init-live-postgres-runtime!)
              (l/rt:setup (live/pg-rt) live/+postgres-module+)
              (live/ensure-public-entry-table!)
              (live/enable-public-entry-realtime!)
              (live/reload-postgrest! live/+public-schema+)
              (live/refresh-live-supabase-config!)
              (live/cleanup-public-entry! live/+live-realtime-entry-name+)
              true)]
  :teardown [(do (live/cleanup-public-entry! live/+live-realtime-entry-name+)
                 (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                 (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                 (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                 true)
             (l/rt:stop)]})

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

^{:refer xt.db.runtime.supabase-realtime/subscribe :added "4.1.4"
  :setup [(live/cleanup-public-entry! live/+live-realtime-entry-name+)]}
(fact "subscribes through a local supabase websocket and applies incoming changes"

  (do
    (future
      (Thread/sleep 4000)
      (live/setup-public-entry!
       live/+live-realtime-entry-name+
       live/+live-realtime-entry-tags+))
    (notify/wait-on [:js 15000]
      (var cache
          (xtd/obj-assign
           (xdb/db-create {"::" "db.cache"}
                          (@! fixtures/+schema+)
                          (@! fixtures/+lookup+)
                          nil)
           {"schema" (@! fixtures/+schema+)}))
      (var instance (xt/x:obj-clone (@! live/+live-supabase-config+)))
      (var client-config (xt/x:obj-clone (. instance ["client"])))
      (var schema-name (@! live/+public-schema+))
      (var table-name (@! live/+scratch-entry-table+))
      (var connected [])
      (var closed [])
      (var statuses [])
      (var requests [])
      (var request-out nil)
      (var transport-source {})
      (xt/x:set-key transport-source
                   "connect"
                   (fn [url]
                     (xt/x:arr-push connected url)
                     (var socket (new WebSocket url))
                     (return
                      (new Promise
                           (fn [resolve reject]
                             (. socket (addEventListener
                                        "open"
                                        (fn [_]
                                          (var wrapper {"url" url})
                                          (xt/x:set-key wrapper "send"
                                                        (fn [payload]
                                                          (return (. socket (send payload)))))
                                          (xt/x:set-key wrapper "close"
                                                        (fn [code reason]
                                                          (return (. socket (close code reason)))))
                                          (xt/x:set-key wrapper "addEventListener"
                                                        (fn [event handler]
                                                          (return (. socket (addEventListener event handler)))))
                                          (resolve wrapper)))
                             (. socket (addEventListener
                                        "error"
                                        (fn [err]
                                          (reject err))))))))))
      (xt/x:set-key client-config "transport"
                   (js-ws/driver transport-source))
      (xt/x:set-key client-config "schema_name" schema-name)
      (xt/x:set-key client-config "table_name" table-name)
      (promise/x:promise-then
       (realtime/subscribe
       {"client" client-config}
       cache
       {"on_status" (fn [status _frame]
                      (xt/x:arr-push statuses status))
        "on_request" (fn [request _payload _frame]
                       (xt/x:arr-push requests request)
                       (when (and (xt/x:not-nil? request)
                                  (== (@! live/+live-realtime-entry-name+)
                                      (xtd/get-in request ["db/sync" table-name 0 "name"])))
                         (:= request-out request)))})
       (fn [sub]
        (var poll-id
             (setInterval
              (fn []
                (when (xt/x:not-nil? request-out)
                  (clearInterval poll-id)
                  (promise/x:promise-then
                   (realtime/unsubscribe sub)
                   (fn [_]
                     (xt/x:arr-push closed [1000 "supabase-realtime/unsubscribe"])
                     (var cached-row
                          (xt/x:get-idx
                           (xdb/db-pull-sync
                            cache
                            (@! fixtures/+schema+)
                            (@! live/+live-realtime-entry-query+))
                           0))
                     (repl/notify
                      {"connected" (xt/x:first connected)
                       "topic" (realtime/resolve-topic {"client" client-config}
                                                       {"schema_name" schema-name
                                                        "table_name" table-name})
                       "status" (xt/x:first statuses)
                       "request-name" (xtd/get-in request-out ["db/sync" table-name 0 "name"])
                       "request-tags" (xtd/get-in request-out ["db/sync" table-name 0 "tags"])
                       "cached-row" cached-row
                       "closed" (xt/x:get-idx closed 0)}))))
              100))
        (return sub)))))
    )
  => {"connected" string?
      "topic" "realtime:public:Entry"
      "status" "SUBSCRIBED"
      "request-name" "copilot_supabase_realtime_live"
      "request-tags" ["copilot" "supabase" "realtime"]
      "cached-row" {"name" "copilot_supabase_realtime_live"
                   "tags" ["copilot" "supabase" "realtime"]}
      "closed" [1000 "supabase-realtime/unsubscribe"]})

^{:refer xt.db.runtime.supabase-realtime/subscribe :added "4.1.4"}
(fact "supports custom topics and custom inbound event names with request transforms"

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
    (var statuses [])
    (var requests [])
    (var request-transform
         (fn [payload _source _opts]
           (return
            {"db/sync"
             {"Entry"
              [{"id" (xt/x:get-key payload "id")
                "name" (xt/x:get-key payload "name")
                "tags" (xt/x:get-key payload "tags")
                "__deleted__" false}]}})))
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
     (realtime/subscribe
      {"client" {"transport" driver
                 "base_url" "https://db.test"
                 "api_key" "key-1"
                 "topic" "room:entries"
                 "message_event" "broadcast"
                 "request_transform" request-transform}}
      cache
      {"on_status" (fn [status _frame]
                     (xt/x:arr-push statuses status))
       "on_request" (fn [request _payload _frame]
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
           "payload" {"data" {"id" "00000000-0000-0000-0000-0000000000ea"
                              "name" "custom"
                              "tags" ["topic" "event"]}}})})
       (return
        (promise/x:promise-then
         (realtime/unsubscribe sub)
         (fn [_]
           (repl/notify
            {"join-topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
             "status" (xt/x:first statuses)
             "request-name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])
             "cached-name" (xtd/get-in
                            (xdb/db-pull-sync
                             cache
                             (@! fixtures/+schema+)
                             ["Entry"
                              {"id" "00000000-0000-0000-0000-0000000000ea"}
                              ["name"]])
                            [0 "name"])})))))))
  => {"join-topic" "realtime:room:entries"
      "status" "SUBSCRIBED"
      "request-name" "custom"
      "cached-name" "custom"})

^{:refer xt.db.runtime.supabase-realtime/subscribe :added "4.1.4"}
(fact "unwraps native xt.db requests from live broadcast envelopes"

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
    (var statuses [])
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
     (realtime/subscribe
      {"client" {"transport" driver
                "base_url" "https://db.test"
                "api_key" "key-1"
                "topic" "room:entries"
                "message_event" "broadcast"}}
      cache
      {"on_status" (fn [status _frame]
                    (xt/x:arr-push statuses status))
      "on_request" (fn [request _payload _frame]
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
          "payload" {"event" "entry_sync"
                     "meta" {"id" "msg-1"}
                     "payload" {"db/sync"
                                {"Entry"
                                 [{"id" "00000000-0000-0000-0000-0000000000eb"
                                   "name" "native"
                                   "tags" ["broadcast"]
                                   "__deleted__" false}]}}
                     "type" "broadcast"}})})
      (return
       (promise/x:promise-then
        (realtime/unsubscribe sub)
        (fn [_]
          (repl/notify
           {"join-topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
            "status" (xt/x:first statuses)
            "request-name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])
            "cached-name" (xtd/get-in
                           (xdb/db-pull-sync
                            cache
                            (@! fixtures/+schema+)
                            ["Entry"
                             {"id" "00000000-0000-0000-0000-0000000000eb"}
                             ["name"]])
                           [0 "name"])})))))))
  => {"join-topic" "realtime:room:entries"
      "status" "SUBSCRIBED"
      "request-name" "native"
      "cached-name" "native"})
