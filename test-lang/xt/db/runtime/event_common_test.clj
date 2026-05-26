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


^{:refer xt.db.runtime.event-common/raw-client :added "4.1"}
(fact "unwraps tagged clients and leaves plain sources intact"
  (!.js
   (var driver (ws/driver-create {"connect_sync" (fn [_url]
                                                   (return {"close" (fn [_code _reason]
                                                                      (return true))}))}))
   (var wrapped (event-common/wrap-client {"transport" driver
                                           "request_transform" "from-client"}
                                          "transport.test"))
   [(event-common/client? wrapped "transport.test")
    (xt/x:not-nil? (xt/x:get-key (event-common/raw-client wrapped "transport.test") "transport"))
    (event-common/raw-client {"foo" 1} "transport.test")
    (event-common/raw-client nil "transport.test")])
  => [true
      true
      {"foo" 1}
      {}])

^{:refer xt.db.runtime.event-common/resolve-client-source :added "4.1"}
(fact "resolves client or transport sources from db then opts"
  (!.js
   [(event-common/resolve-client-source {"client" {"id" "db-client"}} {"client" {"id" "opts-client"}})
    (event-common/resolve-client-source {"transport" {"id" "db-transport"}} {"transport" {"id" "opts-transport"}})
    (event-common/resolve-client-source {} {"client" {"id" "opts-client"}})
    (event-common/resolve-client-source {} {})])
  => [{"id" "db-client"}
      {"id" "db-transport"}
      {"id" "opts-client"}
      nil])

^{:refer xt.db.runtime.event-common/wrap-client :added "4.1"}
(fact "wraps websocket and plain configs with a tagged client descriptor"
  (!.js
   (var driver (ws/driver-create {"connect_sync" (fn [_url]
                                                   (return {"close" (fn [_code _reason]
                                                                      (return true))}))}))
   (var wrapped-driver (event-common/wrap-client driver "transport.test"))
   (var wrapped-map (event-common/wrap-client {"base_url" "wss://example.test"} "transport.test"))
   [(event-common/client? wrapped-driver "transport.test")
    (ws/driver? (xt/x:get-key (event-common/raw-client wrapped-driver "transport.test") "transport"))
    (event-common/raw-client wrapped-map "transport.test")
    (event-common/raw-client (event-common/wrap-client nil "transport.test") "transport.test")])
  => [true
      true
      {"base_url" "wss://example.test"}
      {}])

^{:refer xt.db.runtime.event-common/resolve-transport :added "4.1"}
(fact "resolves websocket drivers from wrapped configs or transport functions"
  (!.js
   (do:>
    (var driver (ws/driver-create {"connect_sync" (fn [_url]
                                                    (return {"close" (fn [_code _reason]
                                                                       (return true))}))}))
    (var thrown false)
    (try
      (event-common/resolve-transport
       (event-common/wrap-client {"base_url" "wss://missing.test"} "transport.test")
       "transport.test"
       "event-common")
      (catch err
        (:= thrown true)))
    (return
     [(ws/driver? (event-common/resolve-transport
                   (event-common/wrap-client {"transport" driver} "transport.test")
                   "transport.test"
                   "event-common"))
      (ws/driver? (event-common/resolve-transport
                   (event-common/wrap-client {"transport" (fn [_url]
                                                            (return {"close" (fn [_code _reason]
                                                                               (return true))}))}
                                             "transport.test")
                   "transport.test"
                   "event-common"))
      thrown])))
  => [true true true])

^{:refer xt.db.runtime.event-common/request-op :added "4.1"}
(fact "detects supported xt.db request ops at top level"
  (!.js
   [(event-common/request-op {"db/sync" {"Entry" []}})
    (event-common/request-op {"db/remove" {"Entry" ["id-1"]}})
    (event-common/request-op {"payload" {"db/sync" {"Entry" []}}})
    (event-common/request-op nil)])
  => ["db/sync" "db/remove" nil nil])

^{:refer xt.db.runtime.event-common/request? :added "4.1"}
(fact "detects xt.db requests directly or inside payload envelopes"
  (!.js
   [(event-common/request? {"db/sync" {"Entry" []}})
    (event-common/request? {"payload" {"db/remove" {"Entry" ["id-1"]}}})
    (event-common/request? {"payload" {"noop" true}})
    (event-common/request? "plain-text")])
  => [true true false false])

^{:refer xt.db.runtime.event-common/trim-trailing-slash :added "4.1"}
(fact "trims only one trailing slash from strings"
  (!.js
   [(event-common/trim-trailing-slash "https://stream.test/")
    (event-common/trim-trailing-slash "https://stream.test")
    (event-common/trim-trailing-slash nil)])
  => ["https://stream.test"
      "https://stream.test"
      nil])

^{:refer xt.db.runtime.event-common/encode-query-params :added "4.1"}
(fact "encodes flat query params while skipping nil values"
  (!.js
   [(event-common/encode-query-params {"a" 1 "b" "two" "c" nil})
    (event-common/encode-query-params {})
    (event-common/encode-query-params nil)])
  => ["a=1&b=two" "" ""])

^{:refer xt.db.runtime.event-common/extract-message-data :added "4.1"}
(fact "extracts websocket payloads from strings, data, or body fields"
  (!.js
   [(event-common/extract-message-data "plain-text")
    (event-common/extract-message-data {"data" "{\"ok\":true}"})
    (event-common/extract-message-data {"body" "fallback"})
    (event-common/extract-message-data {"other" true})])
  => ["plain-text"
      "{\"ok\":true}"
      "fallback"
      {"other" true}])

^{:refer xt.db.runtime.event-common/decode-message :added "4.1"}
(fact "decodes json by default and can decode only json-looking payloads"
  (!.js
   [(event-common/decode-message {"data" "{\"db/sync\":{\"Entry\":[]}}"} {})
    (event-common/decode-message {"data" "plain-text"} {"decode_json" false})
    (event-common/decode-message {"data" "{\"ok\":true}"} {"decode_if_json" true})
    (event-common/decode-message {"data" "plain-text"} {"decode_if_json" true})])
  => [{"db/sync" {"Entry" []}}
      "plain-text"
      {"ok" true}
      "plain-text"])

^{:refer xt.db.runtime.event-common/resolve-request-transform :added "4.1"}
(fact "prefers request transforms from opts over client config"
  (!.js
   [(event-common/resolve-request-transform
     (event-common/wrap-client {"request_transform" "from-client"} "transport.test")
     {}
     "transport.test")
    (event-common/resolve-request-transform
     (event-common/wrap-client {"request_transform" "from-client"} "transport.test")
     {"request_transform" "from-opts"}
     "transport.test")
    (event-common/resolve-request-transform
     nil
     {}
     "transport.test")])
  => ["from-client"
      "from-opts"
      nil])

^{:refer xt.db.runtime.event-common/apply-payload :added "4.1"}
(fact "unwraps payload envelopes and applies native requests to the cache"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   [(event-common/apply-payload
     cache
     {"payload"
      {"db/sync"
       {"Entry"
        [{"id" "00000000-0000-0000-0000-0000000000ef"
          "name" "payload"
          "tags" ["event"]
          "__deleted__" false}]}}}
     {})
    (xdb/db-pull-sync
     cache
     (@! fixtures/+schema+)
     ["Entry"
      {"id" "00000000-0000-0000-0000-0000000000ef"}
      ["name"]])
    (event-common/apply-payload cache {"noop" true} {})])
  => [[true
       {"db/sync"
        {"Entry"
         [{"id" "00000000-0000-0000-0000-0000000000ef"
           "name" "payload"
           "tags" ["event"]
           "__deleted__" false}]}}]
      [{"name" "payload"}]
      [true nil]])

^{:refer xt.db.runtime.event-common/subscription? :added "4.1"}
(fact "detects tagged subscription handles and active state"
  (!.js
   (var subscription {"::" "transport.subscription"
                      "active" true
                      "socket" {"close" (fn [_code _reason]
                                          (return true))}})
   [(event-common/subscription? subscription "transport.subscription")
    (event-common/subscription? {} "transport.subscription")
    (event-common/subscription-active? subscription "transport.subscription")
    (event-common/subscription-active? {"::" "transport.subscription"
                                        "active" false}
                                       "transport.subscription")])
  => [true false true false])

^{:refer xt.db.runtime.event-common/subscription-active? :added "4.1"}
(fact "treats missing active flags as active until explicitly disabled"
  (!.js
   [(event-common/subscription-active? {"::" "transport.subscription"} "transport.subscription")
    (event-common/subscription-active? {"::" "transport.subscription"
                                        "active" false}
                                       "transport.subscription")
    (event-common/subscription-active? {"::" "other.subscription"
                                        "active" true}
                                       "transport.subscription")])
  => [true false false])

^{:refer xt.db.runtime.event-common/unsubscribe :added "4.1"}
(fact "marks subscriptions inactive and disconnects tagged sockets only"
  (notify/wait-on [:js 2000]
    (var closes [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [_url]
             (return {"close" (fn [code reason]
                                (xt/x:arr-push closes [code reason])
                                (return true))}))}))
    (promise/x:promise-then
     (ws/connect driver "ws://unsubscribe.test")
     (fn [socket]
       (var subscription {"::" "transport.subscription"
                          "active" true
                          "socket" socket})
       (promise/x:promise-then
        (event-common/unsubscribe subscription
                                  "transport.subscription"
                                  "event-common/unsubscribe")
        (fn [_]
          (promise/x:promise-then
           (event-common/unsubscribe
            {"::" "other.subscription"}
            "transport.subscription"
            "ignored")
           (fn [_]
             (repl/notify
              {"active" (. subscription ["active"])
               "close_reason" (xtd/get-in closes [0 1])
               "close_count" (. closes ["length"])}))))))))
  => {"active" false
      "close_reason" "event-common/unsubscribe"
      "close_count" 1})