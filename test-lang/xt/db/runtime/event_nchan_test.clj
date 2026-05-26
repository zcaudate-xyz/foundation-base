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


^{:refer xt.db.runtime.event-nchan/client? :added "4.1"}
(fact "detects wrapped nchan clients"
  (!.js
   [(event-nchan/client? (event-nchan/client {"base_url" "https://stream.test"}))
    (event-nchan/client? {"base_url" "https://stream.test"})
    (event-nchan/client? nil)])
  => [true false false])

^{:refer xt.db.runtime.event-nchan/raw-client :added "4.1"}
(fact "unwraps tagged nchan client descriptors"
  (!.js
   [(event-nchan/raw-client (event-nchan/client {"base_url" "https://stream.test"}))
    (event-nchan/raw-client {"base_url" "https://plain.test"})
    (event-nchan/raw-client nil)])
  => [{"base_url" "https://stream.test"}
      {"base_url" "https://plain.test"}
      {}])

^{:refer xt.db.runtime.event-nchan/resolve-transport :added "4.1"}
(fact "resolves websocket drivers from nchan transport config"
  (!.js
   (do:>
    (var driver (ws/driver-create {"connect_sync" (fn [_url]
                                                    (return {"close" (fn [_code _reason]
                                                                       (return true))}))}))
    (var thrown false)
    (try
      (event-nchan/resolve-transport (event-nchan/client {"base_url" "https://missing.test"}))
      (catch err
        (:= thrown true)))
    (return
     [(ws/driver? (event-nchan/resolve-transport
                   (event-nchan/client {"transport" driver})))
      (ws/driver? (event-nchan/resolve-transport
                   (event-nchan/client {"transport" (fn [_url]
                                                      (return {"close" (fn [_code _reason]
                                                                         (return true))}))})))
      thrown])))
  => [true true true])

^{:refer xt.db.runtime.event-nchan/resolve-base-url :added "4.1"}
(fact "resolves base urls from client config then opts"
  (!.js
   [(event-nchan/resolve-base-url nil (event-nchan/client {"base_url" "https://client.test"}) {})
    (event-nchan/resolve-base-url nil (event-nchan/client {}) {"base_url" "https://opts.test"})
    (event-nchan/resolve-base-url nil (event-nchan/client {}) {})])
  => ["https://client.test" "https://opts.test" nil])

^{:refer xt.db.runtime.event-nchan/trim-trailing-slash :added "4.1"}
(fact "trims a trailing slash from nchan base urls"
  (!.js
   [(event-nchan/trim-trailing-slash "https://stream.test/")
    (event-nchan/trim-trailing-slash "https://stream.test")
    (event-nchan/trim-trailing-slash nil)])
  => ["https://stream.test" "https://stream.test" nil])

^{:refer xt.db.runtime.event-nchan/derive-websocket-base-url :added "4.1"}
(fact "converts http origins to websocket origins"
  (!.js
   [(event-nchan/derive-websocket-base-url "https://stream.test/")
    (event-nchan/derive-websocket-base-url "http://stream.test")
    (event-nchan/derive-websocket-base-url "ws://stream.test")
    (event-nchan/derive-websocket-base-url nil)])
  => ["wss://stream.test" "ws://stream.test" "ws://stream.test" nil])

^{:refer xt.db.runtime.event-nchan/channel-group :added "4.1"}
(fact "resolves channel groups from opts then client"
  (!.js
   [(event-nchan/channel-group (event-nchan/client {"channel_group" "client-group"}) {})
    (event-nchan/channel-group (event-nchan/client {"channel_group" "client-group"}) {"channel_group" "opts-group"})
    (event-nchan/channel-group (event-nchan/client {}) {})])
  => ["client-group" "opts-group" "user"])

^{:refer xt.db.runtime.event-nchan/channel-id :added "4.1"}
(fact "resolves channel ids from opts then client"
  (!.js
   [(event-nchan/channel-id (event-nchan/client {"channel_id" "client-id"}) {})
    (event-nchan/channel-id (event-nchan/client {"channel_id" "client-id"}) {"channel_id" "opts-id"})
    (event-nchan/channel-id (event-nchan/client {}) {})])
  => ["client-id" "opts-id" "default"])

^{:refer xt.db.runtime.event-nchan/resolve-first-message :added "4.1"}
(fact "resolves the first_message option from opts then client"
  (!.js
   [(event-nchan/resolve-first-message (event-nchan/client {"first_message" "oldest"}) {})
    (event-nchan/resolve-first-message (event-nchan/client {"first_message" "oldest"}) {"first_message" "newest"})
    (event-nchan/resolve-first-message (event-nchan/client {}) {})])
  => ["oldest" "newest" nil])

^{:refer xt.db.runtime.event-nchan/resolve-params :added "4.1"}
(fact "merges params with opts overriding client defaults"
  (!.js
   (event-nchan/resolve-params
    (event-nchan/client {"params" {"a" 1 "b" 2}})
    {"params" {"b" 3 "c" 4}}))
  => {"a" 1 "b" 3 "c" 4})

^{:refer xt.db.runtime.event-nchan/resolve-subscriber-path :added "4.1"}
(fact "resolves the subscriber path from opts or channel group"
  (!.js
   [(event-nchan/resolve-subscriber-path
     (event-nchan/client {"channel_group" "alpha"})
     {})
    (event-nchan/resolve-subscriber-path
     (event-nchan/client {"subscriber_path" "/custom/sub"})
     {"subscriber_path" "/opts/sub"})])
  => ["/stream/alpha" "/opts/sub"])

^{:refer xt.db.runtime.event-nchan/resolve-publisher-path :added "4.1"}
(fact "resolves the publisher path from opts or subscriber path"
  (!.js
   [(event-nchan/resolve-publisher-path
     (event-nchan/client {"channel_group" "alpha"})
     {})
    (event-nchan/resolve-publisher-path
     (event-nchan/client {"publisher_path" "/custom/pub"})
     {"publisher_path" "/opts/pub"})])
  => ["/stream/alpha/publish" "/opts/pub"])

^{:refer xt.db.runtime.event-nchan/resolve-info-path :added "4.1"}
(fact "resolves the info path from opts or subscriber path"
  (!.js
   [(event-nchan/resolve-info-path
     (event-nchan/client {"channel_group" "alpha"})
     {})
    (event-nchan/resolve-info-path
     (event-nchan/client {"info_path" "/custom/info"})
     {"info_path" "/opts/info"})])
  => ["/stream/alpha/info" "/opts/info"])

^{:refer xt.db.runtime.event-nchan/encode-query-params :added "4.1"}
(fact "encodes flat nchan query params"
  (!.js
   [(event-nchan/encode-query-params {"id" "tab-a" "first_message" "newest"})
    (event-nchan/encode-query-params nil)])
  => ["id=tab-a&first_message=newest" ""])

^{:refer xt.db.runtime.event-nchan/create-scaffold :added "4.1"}
(fact "creates derived nchan endpoint scaffold values"
  (!.js
   (event-nchan/create-scaffold
    nil
    (event-nchan/client {"base_url" "https://stream.test"
                         "channel_group" "delta"
                         "channel_id" "id-1"
                         "first_message" "newest"})
    {"params" {"view" "compact"}}))
  => {"client" {"::" "nchan.client"
                "_raw" {"base_url" "https://stream.test"
                        "channel_group" "delta"
                        "channel_id" "id-1"
                        "first_message" "newest"}}
      "base_url" "https://stream.test"
      "websocket_base_url" "wss://stream.test"
      "channel_group" "delta"
      "channel_id" "id-1"
      "subscriber_path" "/stream/delta"
      "publisher_path" "/stream/delta/publish"
      "info_path" "/stream/delta/info"
      "params" {"view" "compact"
                "id" "id-1"
                "first_message" "newest"}})

^{:refer xt.db.runtime.event-nchan/resolve-publisher-url :added "4.1"}
(fact "resolves publisher urls from direct config or scaffolded paths"
  (!.js
   [(event-nchan/resolve-publisher-url
     nil
     (event-nchan/client {"publisher_url" "https://publish.test/direct"})
     {})
    (event-nchan/resolve-publisher-url
     nil
     (event-nchan/client {"base_url" "https://stream.test"
                          "channel_group" "delta"
                          "channel_id" "id-1"})
     {})])
  => ["https://publish.test/direct"
      "https://stream.test/stream/delta/publish?id=id-1"])

^{:refer xt.db.runtime.event-nchan/resolve-info-url :added "4.1"}
(fact "resolves info urls from direct config or scaffolded paths"
  (!.js
   [(event-nchan/resolve-info-url
     nil
     (event-nchan/client {"info_url" "https://info.test/direct"})
     {})
    (event-nchan/resolve-info-url
     nil
     (event-nchan/client {"base_url" "https://stream.test"
                          "channel_group" "delta"})
     {})])
  => ["https://info.test/direct"
      "https://stream.test/stream/delta/info"])

^{:refer xt.db.runtime.event-nchan/resolve-request-transform :added "4.1"}
(fact "prefers request transforms from opts over client config"
  (!.js
   [(event-nchan/resolve-request-transform
     (event-nchan/client {"request_transform" "from-client"})
     {})
    (event-nchan/resolve-request-transform
     (event-nchan/client {"request_transform" "from-client"})
     {"request_transform" "from-opts"})])
  => ["from-client" "from-opts"])

^{:refer xt.db.runtime.event-nchan/resolve-client :added "4.1"}
(fact "resolves wrapped clients from db or opts and throws when missing"
  (!.js
   (do:>
    (var thrown false)
    (try
      (event-nchan/resolve-client {} {})
      (catch err
        (:= thrown true)))
    (return
     [(event-nchan/client? (event-nchan/resolve-client {"client" {"base_url" "https://db.test"}} {}))
      (event-nchan/client? (event-nchan/resolve-client {} {"client" {"base_url" "https://opts.test"}}))
      thrown])))
  => [true true true])

^{:refer xt.db.runtime.event-nchan/client :added "4.1"}
(fact "wraps raw config as a tagged nchan client"
  (!.js
   (var client (event-nchan/client {"base_url" "https://stream.test"}))
   [(event-nchan/client? client)
    (event-nchan/raw-client client)])
  => [true {"base_url" "https://stream.test"}])

^{:refer xt.db.runtime.event-nchan/connect :added "4.1"}
(fact "connects through the websocket driver to the derived subscriber url"
  (notify/wait-on [:js 2000]
    (var urls [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [url]
             (xt/x:arr-push urls url)
             (return {"close" (fn [_code _reason]
                                (return true))
                      "addEventListener" (fn [_event _handler]
                                           (return true))}))}))
    (promise/x:promise-then
     (event-nchan/connect
      nil
      (event-nchan/client {"transport" driver
                           "base_url" "https://stream.test"
                           "channel_group" "user"
                           "channel_id" "tab-a"})
      {})
     (fn [socket]
       (repl/notify
        {"client" (ws/client? socket)
         "url" (xtd/get-in urls [0])}))))
  => {"client" true
      "url" "wss://stream.test/stream/user?id=tab-a"})

^{:refer xt.db.runtime.event-nchan/extract-message-data :added "4.1"}
(fact "extracts message payload text from nchan frames"
  (!.js
   [(event-nchan/extract-message-data "plain")
    (event-nchan/extract-message-data {"data" "{\"ok\":true}"})
    (event-nchan/extract-message-data {"body" "fallback"})])
  => ["plain" "{\"ok\":true}" "fallback"])

^{:refer xt.db.runtime.event-nchan/decode-message :added "4.1"}
(fact "decodes json text when the payload looks like json"
  (!.js
   [(event-nchan/decode-message "{\"db/sync\":{\"Entry\":[]}}")
    (event-nchan/decode-message {"data" "{\"ok\":true}"})
    (event-nchan/decode-message {"data" "plain-text"})])
  => [{"db/sync" {"Entry" []}}
      {"ok" true}
      "plain-text"])

^{:refer xt.db.runtime.event-nchan/request-payload :added "4.1"}
(fact "encodes requests directly or inside an event envelope"
  (!.js
   [(event-nchan/request-payload
     {"db/sync" {"Entry" []}}
     nil
     {})
    (event-nchan/request-payload
     {"payload" {"db/remove" {"Entry" ["id-1"]}}}
     (event-nchan/client {"payload_envelope" true})
     {})])
  => ["{\"db/sync\":{\"Entry\":[]}}"
      "{\"event\":\"db/remove\",\"payload\":{\"db/remove\":{\"Entry\":[\"id-1\"]}}}"])

^{:refer xt.db.runtime.event-nchan/payload->request :added "4.1"}
(fact "normalizes raw nchan payloads into native xt.db requests"
  (!.js
   [(event-nchan/payload->request
     {"db/sync" {"Entry" []}}
     nil
     {})
    (event-nchan/payload->request
     "{\"db/remove\":{\"Entry\":[\"id-1\"]}}"
     nil
     {})
    (event-nchan/payload->request
     {"event" "db/sync"
      "payload" {"db/sync" {"Entry" [{"id" "id-1"}]}}}
     (event-nchan/client {"request_transform" (fn [payload _source _opts]
                                                (return (xt/x:get-key payload "payload")))} )
     {})])
  => [{"db/sync" {"Entry" []}}
      {"db/remove" {"Entry" ["id-1"]}}
      {"db/sync" {"Entry" [{"id" "id-1"}]}}])

^{:refer xt.db.runtime.event-nchan/apply-request :added "4.1"}
(fact "applies normalized requests to the local cache db"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   [(event-nchan/apply-request
     cache
     {"db/sync"
      {"Entry"
       [{"id" "00000000-0000-0000-0000-0000000000ec"
         "name" "apply-request"
         "tags" ["nchan"]
         "__deleted__" false}]}}
     nil
     {})
    (xdb/db-pull-sync
     cache
     (@! fixtures/+schema+)
     ["Entry"
      {"id" "00000000-0000-0000-0000-0000000000ec"}
      ["name"]])])
  => [[true
       {"db/sync"
        {"Entry"
         [{"id" "00000000-0000-0000-0000-0000000000ec"
           "name" "apply-request"
           "tags" ["nchan"]
           "__deleted__" false}]}}]
      [{"name" "apply-request"}]])

^{:refer xt.db.runtime.event-nchan/handle-message :added "4.1"}
(fact "decodes inbound frames, applies them, and notifies on_request"
  (notify/wait-on [:js 2000]
    (var cache
         (xtd/obj-assign
          (xdb/db-create {"::" "db.cache"}
                         (@! fixtures/+schema+)
                         (@! fixtures/+lookup+)
                         nil)
          {"schema" (@! fixtures/+schema+)}))
    (var seen [])
    (var subscription {"client" (event-nchan/client {})
                       "local_db" cache
                       "opts" {}
                       "on_request" (fn [request payload _frame]
                                      (xt/x:arr-push seen {"request" request
                                                           "payload" payload}))})
    (repl/notify
     {"result"
      (event-nchan/handle-message
       subscription
       {"data"
        "{\"db/sync\":{\"Entry\":[{\"id\":\"00000000-0000-0000-0000-0000000000eb\",\"name\":\"handle-message\",\"tags\":[\"nchan\"],\"__deleted__\":false}]}}"})
      "seen_name" (xtd/get-in seen [0 "request" "db/sync" "Entry" 0 "name"])
      "entry_name" (xtd/get-in
                    (xdb/db-pull-sync
                     cache
                     (@! fixtures/+schema+)
                     ["Entry"
                      {"id" "00000000-0000-0000-0000-0000000000eb"}
                      ["name"]])
                    [0 "name"])}))
  => {"result"
      {"db/sync"
       {"Entry"
        [{"id" "00000000-0000-0000-0000-0000000000eb"
          "name" "handle-message"
          "tags" ["nchan"]
          "__deleted__" false}]}}
      "seen_name" "handle-message"
      "entry_name" "handle-message"})

^{:refer xt.db.runtime.event-nchan/subscription? :added "4.1"}
(fact "detects tagged nchan subscriptions"
  (!.js
   [(event-nchan/subscription? {"::" "db.nchan.subscription"})
    (event-nchan/subscription? {"::" "other.subscription"})])
  => [true false])

^{:refer xt.db.runtime.event-nchan/subscription-active? :added "4.1"}
(fact "treats subscriptions as active until explicitly disabled"
  (!.js
   [(event-nchan/subscription-active? {"::" "db.nchan.subscription"})
    (event-nchan/subscription-active? {"::" "db.nchan.subscription"
                                       "active" false})
    (event-nchan/subscription-active? {"::" "other.subscription"
                                       "active" true})])
  => [true false false])

^{:refer xt.db.runtime.event-nchan/unsubscribe :added "4.1"}
(fact "marks nchan subscriptions inactive and closes the socket"
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
       (var sub {"::" "db.nchan.subscription"
                 "active" true
                 "socket" socket})
       (promise/x:promise-then
        (event-nchan/unsubscribe sub)
        (fn [_]
          (repl/notify
           {"active" (. sub ["active"])
            "close_reason" (xtd/get-in closes [0 1])
            "close_count" (. closes ["length"])}))))))
  => {"active" false
      "close_reason" "event-nchan/unsubscribe"
      "close_count" 1})