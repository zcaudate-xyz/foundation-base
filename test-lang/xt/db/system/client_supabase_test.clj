(ns xt.db.system.client-supabase-test
  (:require [hara.lang :as l])
  (:require [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.client-supabase :as client]
             [xt.lib.supabase :as supabase]
             [xt.protocol.impl.client-fetch :as fetch]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.client-supabase/client? :added "4.1"}
(fact "detects tagged system supabase clients"

  (!.js
    [(client/client? (client/client {"base_url" "https://client.test"}))
     (client/client? {"::" "db.client.supabase"})
     (client/client? nil)])
  => [true true false])

^{:refer xt.db.system.client-supabase/unsupported-op :added "4.1"}
(fact "raises a read-only error"

  (!.js
    (client/unsupported-op "record_add"))
  => (throws))

^{:refer xt.db.system.client-supabase/raw-client :added "4.1"}
(fact "unwraps fetch clients and passes plain configs through"

  (!.js
    [(client/raw-client (fetch/client-create {"base_url" "https://client.test"} nil))
     (client/raw-client {"base_url" "https://plain.test"})])
  => [{"base_url" "https://client.test"}
      {"base_url" "https://plain.test"}])

^{:refer xt.db.system.client-supabase/resolve-transport :added "4.1"}
(fact "resolves fetch transports from nested transports or raw request impls"

  (!.js
    [(fetch/client? (client/resolve-transport
                     {"transport"
                      (fetch/client-create
                       {"request" (fn [request _opts]
                                    (return request))}
                       nil)}))
     (fetch/client? (client/resolve-transport
                     {"request" (fn [request _opts]
                                  (return request))}))])
  => [true true])

^{:refer xt.db.system.client-supabase/resolve-base-url :added "4.1"}
(fact "resolves base urls from client config or opts"

  (!.js
    [(client/resolve-base-url
      (client/client {"base_url" "https://client.test"})
      {})
     (client/resolve-base-url
      (client/client {})
      {"base_url" "https://opts.test"})])
  => ["https://client.test" "https://opts.test"])

^{:refer xt.db.system.client-supabase/resolve-api-key :added "4.1"}
(fact "resolves api keys from client config or opts"

  (!.js
    [(client/resolve-api-key
      (client/client {"api_key" "key-client"})
      {})
     (client/resolve-api-key
      (client/client {})
      {"api_key" "key-opts"})])
  => ["key-client" "key-opts"])

^{:refer xt.db.system.client-supabase/resolve-auth-token :added "4.1"}
(fact "resolves auth tokens from client config or opts"

  (!.js
    [(client/resolve-auth-token
      (client/client {"auth_token" "token-client"})
      {})
     (client/resolve-auth-token
      (client/client {})
      {"auth_token" "token-opts"})])
  => ["token-client" "token-opts"])

^{:refer xt.db.system.client-supabase/resolve-schema-name :added "4.1"}
(fact "resolves schema names from client config or opts"

  (!.js
    [(client/resolve-schema-name
      (client/client {"schema_name" "client_api"})
      {})
     (client/resolve-schema-name
      (client/client {})
      {"schema_name" "opts_api"})])
  => ["client_api" "opts_api"])

^{:refer xt.db.system.client-supabase/create-scaffold :added "4.1"}
(fact "creates a scaffold from client config and opts"

  (!.js
    (var scaffold
         (client/create-scaffold
          (client/client {"base_url" "https://client.test"
                          "schema_name" "client_api"
                          "api_key" "key-client"
                          "headers" {"x-client" "nested"
                                     "x-shared" "client"}})
          {"auth_token" "token-1"
           "headers" {"x-opt" "opt-1"
                      "x-shared" "opt"}}))
    [(. scaffold ["base_url"])
     (. scaffold ["schema_name"])
     (. scaffold ["api_key"])
     (. scaffold ["auth_token"])
     (xtd/get-in scaffold ["headers" "x-client"])
     (xtd/get-in scaffold ["headers" "x-opt"])
     (xtd/get-in scaffold ["headers" "x-shared"])])
  => ["https://client.test"
      "client_api"
      "key-client"
      "token-1"
      "nested"
      "opt-1"
      "opt"])

^{:refer xt.db.system.client-supabase/resolve-request-headers :added "4.1"}
(fact "merges request, client and opts headers with supabase auth metadata"

  (!.js
    (client/resolve-request-headers
     (client/client {"headers" {"x-client" "client"
                                "x-shared" "client"}
                     "schema_name" "client_api"
                     "api_key" "key-client"})
     {"method" "GET"
      "headers" {"accept" "application/json"
                 "x-shared" "request"}}
     {"auth_token" "token-1"
      "headers" {"x-opt" "opt-1"
                 "x-shared" "opt"}}))
  => {"accept" "application/json"
      "x-client" "client"
      "x-opt" "opt-1"
      "x-shared" "opt"
      "Content-Profile" "client_api"
      "Accept-Profile" "client_api"
      "apikey" "key-client"
      "Authorization" "Bearer token-1"})

^{:refer xt.db.system.client-supabase/prepare-request :added "4.1"}
(fact "prepares request urls, headers and encoded bodies"

  (!.js
    (var out
         (client/prepare-request
          (client/client {"base_url" "https://client.test"
                          "schema_name" "client_api"
                          "headers" {"x-client" "client"}})
          {"method" "POST"
           "url" "/rest/v1/Order"
           "headers" {"x-request" "req"}
           "body" {"id" "ord-1"}}
          {"api_key" "key-1"
           "auth_token" "token-1"
           "headers" {"x-opt" "opt"}}))
    [(. out ["method"])
     (. out ["url"])
     (. out ["body"])
     (xtd/get-in out ["headers" "x-client"])
     (xtd/get-in out ["headers" "x-request"])
     (xtd/get-in out ["headers" "x-opt"])
     (xtd/get-in out ["headers" "Content-Profile"])
     (xtd/get-in out ["headers" "Authorization"])])
  => ["POST"
      "https://client.test/rest/v1/Order"
      "{\"id\":\"ord-1\"}"
      "client"
      "req"
      "opt"
      "client_api"
      "Bearer token-1"])

^{:refer xt.db.system.client-supabase/unwrap-response :added "4.1"}
(fact "unwraps response bodies and data payloads"

  (!.js
    [(client/unwrap-response nil)
     (client/unwrap-response {"body" {"data" [{"id" "ord-1"}]}})
     (client/unwrap-response {"body" {"message" "ok"}})
     (client/unwrap-response {"data" [{"id" "ord-2"}]})
     (client/unwrap-response {"status" 204})])
  => [nil
      [{"id" "ord-1"}]
      {"message" "ok"}
      [{"id" "ord-2"}]
      {"status" 204}])

^{:refer xt.db.system.client-supabase/client :added "4.1"}
(fact "creates a tagged supabase client"

  (!.js
    (var db (client/client {"base_url" "https://example.supabase.co"
                            "client" {"transport"
                                      (fetch/client-create
                                       {"request" (fn [input _opts]
                                                    (return input))}
                                       nil)}}))
    (. db ["::"]))
  => "db.client.supabase")

^{:refer xt.db.system.client-supabase/resolve-client :added "4.1"}
(fact "resolves a supabase client from nested client or transport sources"

  (!.js
    [(supabase/client? (client/resolve-client
                        (client/client {"client"
                                        {"transport"
                                         (fetch/client-create
                                          {"request" (fn [request _opts]
                                                       (return request))}
                                          nil)}})
                        {}))
     (supabase/client? (client/resolve-client
                        (client/client {})
                        {"transport" {"request" (fn [request _opts]
                                                  (return request))}}))])
  => [true true])

^{:refer xt.db.system.client-supabase/pull-sync :added "4.1"}
(fact "rejects sync pulls for the read-only async client"

  (!.js
    (client/pull-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     ["Currency" ["id"]]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/pull :added "4.1"}
(fact "compiles tree ir and shorthand query forms into PostgREST requests"

  (notify/wait-on :js
    (var seen {"url" nil})
    (var transport
         (fetch/client-create
          {"request" (fn [input _opts]
                       (xt/x:set-key seen "url" (. input ["url"]))
                       (return {"body" {"data" [{"id" "USD"}]}}))}
          nil))
    (promise/x:promise-catch
     (promise/x:promise-then
      (client/pull
       (client/client {"base_url" "https://example.supabase.co"
                       "schema_name" "public"
                       "client" {"transport" transport}})
       sample/Schema
       ["Currency"
        {"where" [{"id" "USD"}]
         "data" ["id"]
         "links" []
         "custom" []}]
       {})
      (fn [rows]
        (repl/notify [(. seen ["url"]) rows])))
     (fn [err]
       (repl/notify err))))
  => ["https://example.supabase.co/rest/v1/Currency?select=id&id=eq.USD"
     [{"id" "USD"}]]

  (notify/wait-on :js
    (var seen {"url" nil})
    (var transport
        (fetch/client-create
         {"request" (fn [input _opts]
                      (xt/x:set-key seen "url" (. input ["url"]))
                      (return {"body" {"data" [{"id" "USD"}]}}))}
         nil))
    (promise/x:promise-catch
     (promise/x:promise-then
     (client/pull
      (client/client {"base_url" "https://example.supabase.co"
                      "schema_name" "public"
                      "client" {"transport" transport}})
      sample/Schema
      ["Currency"
       {"id" "USD"}
       ["id"]]
      {})
     (fn [rows]
       (repl/notify [(. seen ["url"]) rows])))
     (fn [err]
      (repl/notify err))))
  => ["https://example.supabase.co/rest/v1/Currency?select=id&id=eq.USD"
     [{"id" "USD"}]])

^{:refer xt.db.system.client-supabase/process-event-sync :added "4.1"}
(fact "rejects write and delete operations"

  (!.js
    (client/process-event-sync
     (client/client {"base_url" "https://example.supabase.co"})
     "add"
     {}
     sample/Schema
     sample/SchemaLookup
     {}))
  => (throws)

  (!.js
    (client/process-event-remove
     (client/client {"base_url" "https://example.supabase.co"})
     "remove"
     {}
     sample/Schema
     sample/SchemaLookup
     {}))
  => (throws)

  (!.js
   (client/record-add-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     [{"id" "USD"}]
     {}))
  => (throws)

  (!.js
   (client/record-add
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     [{"id" "USD"}]
     {}))
  => (throws)

  (!.js
   (client/record-delete-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws)

  (!.js
   (client/record-delete
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/process-event-remove :added "4.1"}
(fact "rejects nested remove operations"

  (!.js
    (client/process-event-remove
     (client/client {"base_url" "https://example.supabase.co"})
     "remove"
     {}
     sample/Schema
     sample/SchemaLookup
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/record-add-sync :added "4.1"}
(fact "rejects direct sync record writes"

  (!.js
    (client/record-add-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     [{"id" "USD"}]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/record-delete-sync :added "4.1"}
(fact "rejects direct sync deletes"

  (!.js
    (client/record-delete-sync
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/record-add :added "4.1"}
(fact "rejects async record writes"

  (!.js
    (client/record-add
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     [{"id" "USD"}]
     {}))
  => (throws))

^{:refer xt.db.system.client-supabase/record-delete :added "4.1"}
(fact "rejects async deletes"

  (!.js
    (client/record-delete
     (client/client {"base_url" "https://example.supabase.co"})
     sample/Schema
     "Currency"
     ["USD"]
     {}))
  => (throws))
