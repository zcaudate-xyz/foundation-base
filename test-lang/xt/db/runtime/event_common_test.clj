(ns xt.db.runtime.event-common-test
  (:require [hara.lang :as l]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.runtime.event-common :as event-common]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.event-common/unwrap-request :added "4.1.4"}
(fact "unwraps native xt.db requests from top-level or nested payload envelopes"
  (!.js
   [(event-common/unwrap-request {"db/sync" {"Entry" []}})
    (event-common/unwrap-request {"payload" {"db/remove" {"Entry" ["id-1"]}}})
    (event-common/request? {"payload" {"db/sync" {"Entry" []}}})
    (event-common/request? nil)])
  => [{"db/sync" {"Entry" []}}
     {"db/remove" {"Entry" ["id-1"]}}
     true
     false])

^{:refer xt.db.runtime.event-common/apply-request :added "4.1.4"}
(fact "applies native xt.db requests to a local cache db"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   (event-common/apply-request
    cache
    {"db/sync"
     {"Entry"
      [{"id" "00000000-0000-0000-0000-0000000000ee"
        "name" "common"
        "tags" ["event"]
        "__deleted__" false}]}}
    {})
   (event-common/apply-request
    cache
    {"db/remove"
     {"Entry" ["00000000-0000-0000-0000-0000000000ee"]}}
    {})
   (xdb/db-pull-sync
    cache
    (@! fixtures/+schema+)
    ["Entry"
     {"id" "00000000-0000-0000-0000-0000000000ee"}
     ["name"]]))
  => [])

^{:refer xt.db.runtime.event-common/client? :added "4.1.4"}
(fact "provides shared transport helpers for tagging, decoding, and subscription teardown"
  (notify/wait-on [:js 2000]
   (var closes [])
   (var driver
       (ws/driver-create
        {"connect_sync"
         (fn [_url]
           (return {"close" (fn [code reason]
                              (xt/x:arr-push closes [code reason])
                              (return true))}))}))
   (var wrapped (event-common/wrap-client driver "transport.test"))
   (promise/x:promise-then
    (ws/connect driver "ws://stream.test")
    (fn [socket]
     (var subscription {"::" "transport.subscription"
                        "active" true
                        "socket" socket})
     (promise/x:promise-then
      (event-common/unsubscribe subscription
                                "transport.subscription"
                                "common/unsubscribe")
      (fn [_]
        (repl/notify
         [(event-common/request-op {"db/remove" {"Entry" ["id-1"]}})
          (event-common/client? wrapped "transport.test")
          (event-common/trim-trailing-slash "https://stream.test/")
          (event-common/encode-query-params {"a" 1 "b" "two"})
          (event-common/decode-message
           {"data" "{\"db/sync\":{\"Entry\":[]}}"}
           {})
          (event-common/decode-message
           {"data" "plain-text"}
           {"decode_if_json" true})
          (event-common/subscription? subscription "transport.subscription")
          (event-common/subscription-active? subscription "transport.subscription")
          (xtd/get-in closes [0 1])]))))))
  => ["db/remove"
     true
     "https://stream.test"
     "a=1&b=two"
     {"db/sync" {"Entry" []}}
     "plain-text"
     true
     false
     "common/unsubscribe"])
