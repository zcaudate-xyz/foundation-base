(ns xt.db.system.event-supabase-test
  (:require [hara.lang :as l]
            [xt.db.helpers.test-fixtures :as fixtures]
            [xt.lang.common-notify :as notify]
            [xt.db.system.event-supabase :as event-supabase])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as xdb]
             [xt.db.system.event-supabase :as event-supabase]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.event-supabase/broadcast-client :added "4.1.4"}
(fact "marks a client config as a broadcast subscription"
  (!.js
   (event-supabase/broadcast-client
    {"base_url" "https://db.test"
     "topic" "room:old"}
    {"topic" "room:new"}))
  => {"base_url" "https://db.test"
      "topic" "room:new"
      "message_event" "broadcast"})

^{:refer xt.db.system.event-supabase/subscribe-broadcast :added "4.1.4"}
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


^{:refer xt.db.system.event-supabase/client? :added "4.1"}
(fact "detects wrapped supabase realtime clients"
  (!.js
   [(event-supabase/client? (event-supabase/client {"base_url" "https://db.test"}))
    (event-supabase/client? {"base_url" "https://db.test"})
    (event-supabase/client? nil)])
  => [true false false])

^{:refer xt.db.system.event-supabase/raw-client :added "4.1"}
(fact "unwraps tagged supabase realtime clients"
  (!.js
   [(event-supabase/raw-client (event-supabase/client {"base_url" "https://db.test"}))
    (event-supabase/raw-client {"base_url" "https://plain.test"})
    (event-supabase/raw-client nil)])
  => [{"base_url" "https://db.test"}
      {"base_url" "https://plain.test"}
      {}])

^{:refer xt.db.system.event-supabase/resolve-transport :added "4.1"}
(fact "resolves websocket drivers from realtime transport config"
  (!.js
   (do:>
    (var driver (ws/driver-create {"connect_sync" (fn [_url]
                                                    (return {"close" (fn [_code _reason]
                                                                       (return true))}))}))
    (var thrown false)
    (try
      (event-supabase/resolve-transport (event-supabase/client {"base_url" "https://missing.test"}))
      (catch err
        (:= thrown true)))
    (return
     [(ws/driver? (event-supabase/resolve-transport
                   (event-supabase/client {"transport" driver})))
      (ws/driver? (event-supabase/resolve-transport
                   (event-supabase/client {"transport" (fn [_url]
                                                         (return {"close" (fn [_code _reason]
                                                                            (return true))}))})))
      thrown])))
  => [true true true])

^{:refer xt.db.system.event-supabase/resolve-base-url :added "4.1"}
(fact "resolves base urls from client config then opts"
  (!.js
   [(event-supabase/resolve-base-url nil (event-supabase/client {"base_url" "https://db.test"}) {})
    (event-supabase/resolve-base-url nil (event-supabase/client {}) {"base_url" "https://opts.test"})
    (event-supabase/resolve-base-url nil (event-supabase/client {}) {})])
  => ["https://db.test" "https://opts.test" nil])

^{:refer xt.db.system.event-supabase/resolve-websocket-url :added "4.1"}
(fact "resolves websocket url overrides from client config then opts"
  (!.js
   [(event-supabase/resolve-websocket-url nil (event-supabase/client {"websocket_url" "wss://client.test"}) {})
    (event-supabase/resolve-websocket-url nil (event-supabase/client {}) {"websocket_url" "wss://opts.test"})
    (event-supabase/resolve-websocket-url nil (event-supabase/client {}) {})])
  => ["wss://client.test" "wss://opts.test" nil])

^{:refer xt.db.system.event-supabase/resolve-schema-name :added "4.1"}
(fact "resolves schema names from client config then opts"
  (!.js
   [(event-supabase/resolve-schema-name (event-supabase/client {"schema_name" "client_schema"}) {})
    (event-supabase/resolve-schema-name (event-supabase/client {}) {"schema_name" "opts_schema"})
    (event-supabase/resolve-schema-name (event-supabase/client {}) {})])
  => ["client_schema" "opts_schema" "public"])

^{:refer xt.db.system.event-supabase/resolve-table-name :added "4.1"}
(fact "resolves table names from opts, client config, or payload"
  (!.js
   [(event-supabase/resolve-table-name {"table" "payload_table"} (event-supabase/client {}) {})
    (event-supabase/resolve-table-name {} (event-supabase/client {"table_name" "client_table"}) {})
    (event-supabase/resolve-table-name {} (event-supabase/client {}) {"table_name" "opts_table"})])
  => ["payload_table" "client_table" "opts_table"])

^{:refer xt.db.system.event-supabase/resolve-id-key :added "4.1"}
(fact "resolves id keys from opts then client config"
  (!.js
   [(event-supabase/resolve-id-key (event-supabase/client {"id_key" "client_id"}) {})
    (event-supabase/resolve-id-key (event-supabase/client {"id_key" "client_id"}) {"id_key" "opts_id"})
    (event-supabase/resolve-id-key (event-supabase/client {}) {})])
  => ["client_id" "opts_id" "id"])

^{:refer xt.db.system.event-supabase/resolve-event :added "4.1"}
(fact "resolves event selectors from opts then client config"
  (!.js
   [(event-supabase/resolve-event (event-supabase/client {"event" "INSERT"}) {})
    (event-supabase/resolve-event (event-supabase/client {"event" "INSERT"}) {"event" "DELETE"})
    (event-supabase/resolve-event (event-supabase/client {}) {})])
  => ["INSERT" "DELETE" "*"])

^{:refer xt.db.system.event-supabase/resolve-api-key :added "4.1"}
(fact "resolves api keys from client config then opts"
  (!.js
   [(event-supabase/resolve-api-key nil (event-supabase/client {"api_key" "key-client"}) {})
    (event-supabase/resolve-api-key nil (event-supabase/client {}) {"api_key" "key-opts"})
    (event-supabase/resolve-api-key nil (event-supabase/client {}) {})])
  => ["key-client" "key-opts" nil])

^{:refer xt.db.system.event-supabase/resolve-auth-token :added "4.1"}
(fact "resolves auth tokens from client config then opts"
  (!.js
   [(event-supabase/resolve-auth-token nil (event-supabase/client {"auth_token" "token-client"}) {})
    (event-supabase/resolve-auth-token nil (event-supabase/client {}) {"auth_token" "token-opts"})
    (event-supabase/resolve-auth-token nil (event-supabase/client {}) {})])
  => ["token-client" "token-opts" nil])

^{:refer xt.db.system.event-supabase/resolve-params :added "4.1"}
(fact "merges websocket params on top of the default vsn"
  (!.js
   (event-supabase/resolve-params
    nil
    (event-supabase/client {"params" {"a" 1 "vsn" "client"}})
    {"params" {"b" 2}}))
  => {"vsn" "client" "a" 1 "b" 2})

^{:refer xt.db.system.event-supabase/create-scaffold :added "4.1"}
(fact "creates scaffold values used to connect to realtime"
  (!.js
   [(event-supabase/create-scaffold
     nil
     (event-supabase/client {"base_url" "https://db.test"
                             "schema_name" "public"
                             "api_key" "key-1"
                             "auth_token" "token-1"})
     {"params" {"tenant" "main"}})
    (event-supabase/trim-trailing-slash "https://db.test/")])
  => [{"client" {"::" "supabase.realtime.client"
                 "_raw" {"base_url" "https://db.test"
                         "schema_name" "public"
                         "api_key" "key-1"
                         "auth_token" "token-1"}}
       "base_url" "https://db.test"
       "websocket_url" nil
       "schema_name" "public"
       "api_key" "key-1"
       "auth_token" "token-1"
       "params" {"vsn" "1.0.0"
                 "tenant" "main"}}
      "https://db.test"])

^{:refer xt.db.system.event-supabase/trim-trailing-slash :added "4.1"}
(fact "trims trailing slashes from base urls"
  (!.js
   [(event-supabase/trim-trailing-slash "https://db.test/")
    (event-supabase/trim-trailing-slash "https://db.test")
    (event-supabase/trim-trailing-slash nil)])
  => ["https://db.test" "https://db.test" nil])

^{:refer xt.db.system.event-supabase/derive-websocket-url :added "4.1"}
(fact "derives realtime websocket urls from Supabase base urls"
  (!.js
   [(event-supabase/derive-websocket-url "https://db.test")
    (event-supabase/derive-websocket-url "https://db.test/rest/v1")
    (event-supabase/derive-websocket-url "http://db.test")
    (event-supabase/derive-websocket-url nil)])
  => ["wss://db.test/realtime/v1/websocket"
      "wss://db.test/realtime/v1/websocket"
      "ws://db.test/realtime/v1/websocket"
      nil])

^{:refer xt.db.system.event-supabase/encode-query-params :added "4.1"}
(fact "encodes flat realtime query params"
  (!.js
   [(event-supabase/encode-query-params {"vsn" "1.0.0" "apikey" "key-1"})
    (event-supabase/encode-query-params nil)])
  => ["vsn=1.0.0&apikey=key-1" ""])

^{:refer xt.db.system.event-supabase/prepare-connect-url :added "4.1"}
(fact "builds the realtime websocket url with params and apikey"
  (!.js
   [(event-supabase/prepare-connect-url
     nil
     (event-supabase/client {"base_url" "https://db.test"
                             "api_key" "key-1"})
     {})
    (event-supabase/prepare-connect-url
     nil
     (event-supabase/client {"websocket_url" "wss://socket.test/realtime"
                             "params" {"tenant" "main"}})
     {})])
  => ["wss://db.test/realtime/v1/websocket?vsn=2.0.0&apikey=key-1"
      "wss://socket.test/realtime?vsn=2.0.0&tenant=main"])

^{:refer xt.db.system.event-supabase/resolve-topic :added "4.1"}
(fact "resolves direct or derived realtime topics"
  (!.js
   [(event-supabase/resolve-topic (event-supabase/client {"topic" "room:entries"}) {})
    (event-supabase/resolve-topic (event-supabase/client {"topic" "realtime:room:entries"}) {})
    (event-supabase/resolve-topic (event-supabase/client {"schema_name" "public"
                                                          "table_name" "Entry"}) {})])
  => ["realtime:room:entries"
      "realtime:room:entries"
      "realtime:public:Entry"])

^{:refer xt.db.system.event-supabase/normalize-filter :added "4.1"}
(fact "normalizes postgres_changes filters with defaults"
  (!.js
   (event-supabase/normalize-filter
    {"filter" "id=eq.1"}
    (event-supabase/client {"schema_name" "public"
                            "table_name" "Entry"
                            "event" "UPDATE"})
    {}))
  => {"event" "UPDATE"
      "schema" "public"
      "table" "Entry"
      "filter" "id=eq.1"})

^{:refer xt.db.system.event-supabase/resolve-filters :added "4.1"}
(fact "resolves filters from arrays, single filters, or table declarations"
  (!.js
   [(event-supabase/resolve-filters
     (event-supabase/client {"schema_name" "public"
                             "table_name" "Entry"
                             "filters" [{"event" "INSERT"}
                                        {"event" "DELETE"}]})
     {})
    (event-supabase/resolve-filters
     (event-supabase/client {"schema_name" "public"
                             "table_name" "Entry"
                             "filter" {"filter" "id=eq.1"}})
     {})
    (event-supabase/resolve-filters
     (event-supabase/client {"schema_name" "public"
                             "table_name" "Entry"})
     {})])
  => [[{"event" "INSERT" "schema" "public" "table" "Entry"}
       {"event" "DELETE" "schema" "public" "table" "Entry"}]
      [{"event" "*" "schema" "public" "table" "Entry" "filter" "id=eq.1"}]
      [{"event" "*" "schema" "public" "table" "Entry"}]])

^{:refer xt.db.system.event-supabase/resolve-ref :added "4.1"}
(fact "resolves stable refs from opts or client config"
  (!.js
   [(event-supabase/resolve-ref (event-supabase/client {"ref" "client-ref"}) {})
    (event-supabase/resolve-ref (event-supabase/client {"ref" "client-ref"}) {"ref" "opts-ref"})])
  => ["client-ref" "opts-ref"])

^{:refer xt.db.system.event-supabase/resolve-message-event :added "4.1"}
(fact "resolves inbound message events from opts then client config"
  (!.js
   [(event-supabase/resolve-message-event (event-supabase/client {"message_event" "broadcast"}) {})
    (event-supabase/resolve-message-event (event-supabase/client {"message_event" "broadcast"}) {"message_event" "custom"})
    (event-supabase/resolve-message-event (event-supabase/client {}) {})])
  => ["broadcast" "custom" "postgres_changes"])

^{:refer xt.db.system.event-supabase/resolve-request-transform :added "4.1"}
(fact "prefers request transforms from opts over client config"
  (!.js
   [(event-supabase/resolve-request-transform
     (event-supabase/client {"request_transform" "from-client"})
     {})
    (event-supabase/resolve-request-transform
     (event-supabase/client {"request_transform" "from-client"})
     {"request_transform" "from-opts"})])
  => ["from-client" "from-opts"])

^{:refer xt.db.system.event-supabase/join-payload :added "4.1"}
(fact "builds join payloads with filters and auth tokens"
  (!.js
   (event-supabase/join-payload
    nil
    (event-supabase/client {"schema_name" "public"
                            "table_name" "Entry"
                            "auth_token" "token-1"})
    {}))
  => {"config" {"broadcast" {"ack" false
                             "self" false}
                "presence" {"key" ""}
                "postgres_changes" [{"event" "*"
                                     "schema" "public"
                                     "table" "Entry"}]}
      "access_token" "token-1"})

^{:refer xt.db.system.event-supabase/join-frame :added "4.1"}
(fact "creates phoenix join frames for the resolved topic"
  (!.js
   (event-supabase/join-frame
    nil
    (event-supabase/client {"topic" "room:entries"})
    {"ref" "join-1"}))
  => {"topic" "realtime:room:entries"
      "event" "phx_join"
      "payload" {"config" {"broadcast" {"ack" false
                                        "self" false}
                           "presence" {"key" ""}}}
      "ref" "join-1"
      "join_ref" "join-1"})

^{:refer xt.db.system.event-supabase/leave-frame :added "4.1"}
(fact "creates phoenix leave frames for the resolved topic"
  (!.js
   (event-supabase/leave-frame
    (event-supabase/client {"topic" "room:entries"})
    {"ref" "leave-1"}))
  => {"topic" "realtime:room:entries"
      "event" "phx_leave"
      "payload" {}
      "ref" "leave-1"
      "join_ref" "leave-1"})

^{:refer xt.db.system.event-supabase/resolve-client :added "4.1"}
(fact "resolves wrapped realtime clients from db or opts"
  (!.js
   (do:>
    (var thrown false)
    (try
      (event-supabase/resolve-client {} {})
      (catch err
        (:= thrown true)))
    (return
     [(event-supabase/client? (event-supabase/resolve-client {"client" {"base_url" "https://db.test"}} {}))
      (event-supabase/client? (event-supabase/resolve-client {} {"client" {"base_url" "https://opts.test"}}))
      thrown])))
  => [true true true])

^{:refer xt.db.system.event-supabase/client :added "4.1"}
(fact "wraps raw realtime config as a tagged client"
  (!.js
   (var client (event-supabase/client {"base_url" "https://db.test"}))
   [(event-supabase/client? client)
    (event-supabase/raw-client client)])
  => [true {"base_url" "https://db.test"}])

^{:refer xt.db.system.event-supabase/connect :added "4.1"}
(fact "connects through the websocket driver to the prepared url"
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
                                           (return true))
                      "send" (fn [_payload]
                               (return true))}))}))
    (promise/x:promise-then
     (event-supabase/connect
      nil
      (event-supabase/client {"transport" driver
                              "base_url" "https://db.test"
                              "api_key" "key-1"})
      {})
     (fn [socket]
       (repl/notify
        {"client" (ws/client? socket)
         "url" (xtd/get-in urls [0])}))))
  => {"client" true
      "url" "wss://db.test/realtime/v1/websocket?vsn=2.0.0&apikey=key-1"})

^{:refer xt.db.system.event-supabase/extract-message-data :added "4.1"}
(fact "extracts payload text from websocket events"
  (!.js
   [(event-supabase/extract-message-data "plain")
    (event-supabase/extract-message-data {"data" "{\"ok\":true}"})
    (event-supabase/extract-message-data {"body" "fallback"})])
  => ["plain" "{\"ok\":true}" "fallback"])

^{:refer xt.db.system.event-supabase/decode-message :added "4.1"}
(fact "decodes websocket frames from json"
  (!.js
   [(event-supabase/decode-message "{\"event\":\"phx_reply\"}")
    (event-supabase/decode-message {"data" "{\"payload\":{\"status\":\"ok\"}}"})])
  => [{"event" "phx_reply"}
      {"payload" {"status" "ok"}}])

^{:refer xt.db.system.event-supabase/payload-event-type :added "4.1"}
(fact "normalizes postgres change event type keys"
  (!.js
   [(event-supabase/payload-event-type {"eventType" "insert"})
    (event-supabase/payload-event-type {"event_type" "delete"})
    (event-supabase/payload-event-type {"type" "update"})])
  => ["INSERT" "DELETE" "UPDATE"])

^{:refer xt.db.system.event-supabase/payload-row :added "4.1"}
(fact "extracts the current or deleted row from change payloads"
  (!.js
   [(event-supabase/payload-row {"eventType" "INSERT"
                                 "record" {"id" "id-1"}})
    (event-supabase/payload-row {"eventType" "DELETE"
                                 "old_record" {"id" "id-2"}})])
  => [{"id" "id-1"}
      {"id" "id-2"}])

^{:refer xt.db.system.event-supabase/payload-id :added "4.1"}
(fact "extracts ids from payload rows using the resolved id key"
  (!.js
   [(event-supabase/payload-id {"record" {"id" "id-1"}} (event-supabase/client {}) {})
    (event-supabase/payload-id {"record" {"entry_id" "id-2"}} (event-supabase/client {"id_key" "entry_id"}) {})])
  => ["id-1" "id-2"])

^{:refer xt.db.system.event-supabase/postgres-change->sync-request :added "4.1"}
(fact "converts postgres change payloads into native xt.db requests"
  (!.js
   [(event-supabase/postgres-change->sync-request
     {"eventType" "INSERT"
      "table" "Entry"
      "record" {"id" "id-1"
                "name" "sync"}}
     (event-supabase/client {})
     {})
    (event-supabase/postgres-change->sync-request
     {"eventType" "DELETE"
      "table" "Entry"
      "old_record" {"id" "id-2"}}
     (event-supabase/client {})
     {})])
  => [{"db/sync" {"Entry" [{"id" "id-1"
                            "name" "sync"
                            "__deleted__" false}]}}
      {"db/remove" {"Entry" ["id-2"]}}])

^{:refer xt.db.system.event-supabase/apply-sync-request :added "4.1"}
(fact "applies sync requests to the local cache db"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   [(event-supabase/apply-sync-request
     cache
     {"db/sync"
      {"Entry"
       [{"id" "00000000-0000-0000-0000-0000000000ea"
         "name" "apply-sync"
         "tags" ["supabase"]
         "__deleted__" false}]}}
     nil
     {})
    (xtd/get-in
     (xdb/db-pull-sync
      cache
      (@! fixtures/+schema+)
      ["Entry"
       {"id" "00000000-0000-0000-0000-0000000000ea"}
       ["name"]])
     [0 "name"])])
  => [{"db/sync"
       {"Entry"
        [{"id" "00000000-0000-0000-0000-0000000000ea"
          "name" "apply-sync"
          "tags" ["supabase"]
          "__deleted__" false}]}}
      "apply-sync"])

^{:refer xt.db.system.event-supabase/apply-postgres-change :added "4.1"}
(fact "converts postgres changes and applies them to the cache"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   [(event-supabase/apply-postgres-change
     cache
     {"eventType" "INSERT"
      "table" "Entry"
      "record" {"id" "00000000-0000-0000-0000-0000000000e9"
                "name" "apply-postgres"}}
     (event-supabase/client {})
     {})
    (xtd/get-in
     (xdb/db-pull-sync
      cache
      (@! fixtures/+schema+)
      ["Entry"
       {"id" "00000000-0000-0000-0000-0000000000e9"}
       ["name"]])
     [0 "name"])])
  => [[true
       {"db/sync"
        {"Entry"
         [{"id" "00000000-0000-0000-0000-0000000000e9"
           "name" "apply-postgres"
           "__deleted__" false}]}}]
      "apply-postgres"])

^{:refer xt.db.system.event-supabase/payload->request :added "4.1"}
(fact "normalizes incoming realtime payloads into xt.db requests"
  (!.js
   [(event-supabase/payload->request
     {"db/sync" {"Entry" []}}
     (event-supabase/client {})
     {})
    (event-supabase/payload->request
     {"eventType" "DELETE"
      "table" "Entry"
      "old_record" {"id" "id-2"}}
     (event-supabase/client {})
     {})
    (event-supabase/payload->request
     {"payload" {"db/remove" {"Entry" ["id-3"]}}}
     (event-supabase/client {"message_event" "broadcast"
                             "request_transform" (fn [payload _source _opts]
                                                   (return (xt/x:get-key payload "payload")))} )
     {})])
  => [{"db/sync" {"Entry" []}}
      {"db/remove" {"Entry" ["id-2"]}}
      {"db/remove" {"Entry" ["id-3"]}}])

^{:refer xt.db.system.event-supabase/apply-request :added "4.1"}
(fact "applies normalized realtime requests to the cache"
  (!.js
   (var cache
        (xtd/obj-assign
         (xdb/db-create {"::" "db.cache"}
                        (@! fixtures/+schema+)
                        (@! fixtures/+lookup+)
                        nil)
         {"schema" (@! fixtures/+schema+)}))
   [(event-supabase/apply-request
     cache
     {"eventType" "INSERT"
      "table" "Entry"
      "record" {"id" "00000000-0000-0000-0000-0000000000e8"
                "name" "apply-request"}}
     (event-supabase/client {})
     {})
    (xtd/get-in
     (xdb/db-pull-sync
      cache
      (@! fixtures/+schema+)
      ["Entry"
       {"id" "00000000-0000-0000-0000-0000000000e8"}
       ["name"]])
     [0 "name"])])
  => [[true
       {"db/sync"
        {"Entry"
         [{"id" "00000000-0000-0000-0000-0000000000e8"
           "name" "apply-request"
           "__deleted__" false}]}}]
      "apply-request"])

^{:refer xt.db.system.event-supabase/handle-frame :added "4.1"}
(fact "handles join replies and postgres change frames"
  (notify/wait-on [:js 2000]
    (var cache
         (xtd/obj-assign
          (xdb/db-create {"::" "db.cache"}
                         (@! fixtures/+schema+)
                         (@! fixtures/+lookup+)
                         nil)
          {"schema" (@! fixtures/+schema+)}))
    (var statuses [])
    (var requests [])
    (var sub {"topic" "realtime:public:Entry"
              "client" (event-supabase/client {"table_name" "Entry"})
              "local_db" cache
              "opts" {}
              "on_status" (fn [status _frame]
                            (xt/x:arr-push statuses status))
              "on_request" (fn [request _payload _frame]
                             (xt/x:arr-push requests request))})
    (event-supabase/handle-frame
     sub
     {"data" "{\"topic\":\"realtime:public:Entry\",\"event\":\"phx_reply\",\"payload\":{\"status\":\"ok\"}}"})
    (repl/notify
     {"result"
      (event-supabase/handle-frame
       sub
       {"data"
        "{\"topic\":\"realtime:public:Entry\",\"event\":\"postgres_changes\",\"payload\":{\"data\":{\"eventType\":\"INSERT\",\"table\":\"Entry\",\"record\":{\"id\":\"00000000-0000-0000-0000-0000000000e7\",\"name\":\"handle-frame\"}}}}"})
      "status" (xtd/get-in statuses [0])
      "request_name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])}))
  => {"result"
      {"db/sync"
       {"Entry"
        [{"id" "00000000-0000-0000-0000-0000000000e7"
          "name" "handle-frame"
          "__deleted__" false}]}}
      "status" "SUBSCRIBED"
      "request_name" "handle-frame"})

^{:refer xt.db.system.event-supabase/subscribe :added "4.1"}
(fact "subscribes to postgres_changes topics and applies incoming requests"
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
     (event-supabase/subscribe
      {"client" {"transport" driver
                 "base_url" "https://db.test"
                 "table_name" "Entry"
                 "api_key" "key-1"}}
      cache
      {"on_status" (fn [status _payload]
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
                              "record" {"id" "00000000-0000-0000-0000-0000000000e6"
                                        "name" "subscribe"}}}})})
       (promise/x:promise-then
        (event-supabase/unsubscribe sub)
        (fn [_]
          (repl/notify
           {"join_topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
            "request_name" (xtd/get-in requests [0 "db/sync" "Entry" 0 "name"])
            "status" (xtd/get-in statuses [0])}))))))
  => {"join_topic" "realtime:public:Entry"
      "request_name" "subscribe"
      "status" "SUBSCRIBED"})

^{:refer xt.db.system.event-supabase/subscription? :added "4.1"}
(fact "detects tagged supabase realtime subscriptions"
  (!.js
   [(event-supabase/subscription? {"::" "db.supabase.realtime.subscription"})
    (event-supabase/subscription? {"::" "other.subscription"})])
  => [true false])

^{:refer xt.db.system.event-supabase/unsubscribe :added "4.1"}
(fact "sends the leave frame then closes the realtime socket"
  (notify/wait-on [:js 2000]
    (var sent [])
    (var closes [])
    (var driver
         (ws/driver-create
          {"connect_sync"
           (fn [_url]
             (return {"send" (fn [payload]
                               (xt/x:arr-push sent payload)
                               (return true))
                      "close" (fn [code reason]
                                (xt/x:arr-push closes [code reason])
                                (return true))}))}))
    (promise/x:promise-then
     (ws/connect driver "ws://unsubscribe.test")
     (fn [socket]
       (var sub {"::" "db.supabase.realtime.subscription"
                 "active" true
                 "socket" socket
                 "leave_frame" {"topic" "realtime:room:entries"
                                "event" "phx_leave"
                                "payload" {}
                                "ref" "leave-1"
                                "join_ref" "leave-1"}})
       (promise/x:promise-then
        (event-supabase/unsubscribe sub)
        (fn [_]
          (repl/notify
           {"active" (. sub ["active"])
            "leave_topic" (xtd/get-in (xt/x:json-decode (xt/x:get-idx sent 0)) ["topic"])
            "close_reason" (xtd/get-in closes [0 1])}))))))
  => {"active" false
      "leave_topic" "realtime:room:entries"
      "close_reason" "event-supabase/unsubscribe"})