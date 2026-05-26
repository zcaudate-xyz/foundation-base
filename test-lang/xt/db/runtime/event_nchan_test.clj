(ns xt.db.runtime.event-nchan-test
  (:require [hara.lang :as l]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify]
            [xt.db.runtime.event-nchan :as event-nchan])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.runtime.event-nchan :as event-nchan]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.event-nchan/resolve-subscriber-url :added "4.1.4"}
(fact "derives stream-style websocket/http urls and payloads"
  (!.js
   [(event-nchan/resolve-subscriber-url
     nil
     {"base_url" "https://stream.test"
      "channel_group" "delta"
      "channel_id" "id-1"
      "first_message" "newest"}
     {})
    (event-nchan/resolve-publisher-url
     nil
     {"base_url" "https://stream.test"
      "channel_group" "delta"
      "channel_id" "id-1"}
     {})
    (event-nchan/resolve-info-url
     nil
     {"base_url" "https://stream.test"
      "channel_group" "delta"}
     {})
    (event-nchan/request-payload
     {"db/sync" {"Entry" []}}
     nil
     {})
    (event-nchan/payload->request
     "{\"db/remove\":{\"Entry\":[\"id-1\"]}}"
     nil
     {})])
  => ["wss://stream.test/stream/delta?id=id-1&first_message=newest"
      "https://stream.test/stream/delta/publish?id=id-1"
      "https://stream.test/stream/delta/info"
      "{\"db/sync\":{\"Entry\":[]}}"
      {"db/remove" {"Entry" ["id-1"]}}])

^{:refer xt.db.runtime.event-nchan/subscribe :added "4.1.4"}
(fact "subscribes to an nchan topic carrying native xt.db requests"
  (notify/wait-on [:js 2000]
    (var cache
         (xtd/obj-assign
          (xdb/db-create {"::" "db.cache"}
                         (@! fixtures/+schema+)
                         (@! fixtures/+lookup+)
                         nil)
          {"schema" (@! fixtures/+schema+)}))
    (var handlers {})
    (var closes [])
    (var statuses [])
    (var requests [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [_url]
             (return {"close" (fn [code reason]
                                (xt/x:arr-push closes [code reason])
                                (return true))
                      "addEventListener" (fn [event handler]
                                           (xt/x:set-key handlers event handler)
                                           (return true))}))}))
    (promise/x:promise-then
     (event-nchan/subscribe
      {"client" {"transport" driver
                 "base_url" "https://stream.test"
                 "channel_group" "user"
                 "channel_id" "tab-a"
                 "first_message" "newest"}}
      cache
      {"on_status" (fn [status _payload]
                     (xt/x:arr-push statuses status))
       "on_request" (fn [request _payload _frame]
                      (xt/x:arr-push requests request))})
     (fn [sub]
       ((xt/x:get-key handlers "message")
        {"data"
         (xt/x:json-encode
          {"db/sync"
           {"Entry"
            [{"id" "00000000-0000-0000-0000-0000000000ed"
              "name" "event-nchan"
              "tags" ["stream"]
              "__deleted__" false}]}})})
       (promise/x:promise-then
        (event-nchan/unsubscribe sub)
        (fn [_]
          ((xt/x:get-key handlers "close")
           {"code" 1000})
          (repl/notify
           {"connect-url" (xt/x:get-key sub "connect_url")
            "request-name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])
            "entry-name" (xtd/get-in
                          (xdb/db-pull-sync
                           cache
                           (@! fixtures/+schema+)
                           ["Entry"
                            {"id" "00000000-0000-0000-0000-0000000000ed"}
                            ["name"]])
                          [0 "name"])
            "statuses" statuses
            "close-reason" (xtd/get-in closes [0 1])
            "active" (event-nchan/subscription-active? sub)}))))))
  => {"connect-url" "wss://stream.test/stream/user?id=tab-a&first_message=newest"
      "request-name" "event-nchan"
      "entry-name" "event-nchan"
      "statuses" ["SUBSCRIBED" "CLOSED"]
      "close-reason" "event-nchan/unsubscribe"
      "active" false})
