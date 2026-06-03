(ns xt.db.runtime.supabase-client-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.db.runtime.supabase-client :as supabase]
             [xt.db.text.pgrest-graph :as pgrest]
             [xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

(def +query-tree+
  ["Order"
   {"account" {"id" "acct-1"}
    "id" ["in" [["ord-1" "ord-2"]]]}
   ["status"
    ["account" ["nickname"]]]])

^{:refer xt.db.runtime.supabase-client/resolve-client :added "4.1.3"}
(fact "wraps supabase configs as shared fetch clients"

  (!.js
   (var client
        (supabase/resolve-client
         {"client" {"transport"
                    (js-fetch/client
                     {"request" (fn [request _opts]
                                  (return request))})
                    "base_url" "https://client.test"}}
         {}))
   [(supabase/client? client)
    (fetch/client? client)
    (xt/x:get-key (supabase/raw-client client) "base_url")])
  => [true true "https://client.test"])

^{:refer xt.db.runtime.supabase-client/prepare-request :added "4.1.3"}
(fact "prepares supabase requests on top of the shared fetch envelope"

  (!.js
   (var client
        (supabase/client
         {"transport"
          (js-fetch/client
           {"request" (fn [request _opts]
                        (return request))})
          "base_url" "https://client.test"
          "schema_name" "client_api"
          "api_key" "key-client"
          "headers" {"x-client" "nested"}}))
   (supabase/prepare-request
    {}
    client
    {"url" "/rest/v1/Order?select=id"
     "headers" {"x-opt" "opt-1"}}
    {"auth_token" "token-1"}))
  => {"method" "GET"
      "url" "https://client.test/rest/v1/Order?select=id"
      "headers" {"x-client" "nested"
                 "x-opt" "opt-1"
                 "Content-Profile" "client_api"
                 "Accept-Profile" "client_api"
                 "apikey" "key-client"
                 "Authorization" "Bearer token-1"}})

^{:refer xt.db.runtime.supabase-client/client :added "4.1.3"}
(fact "dispatches supabase requests through the wrapped transport client"

  (notify/wait-on [:js 2000]
    (promise/x:promise-then
     (fetch/request
      (supabase/client
       {"transport"
        (js-fetch/client
         {"request" (fn [request _opts]
                      (return {"status" 200
                               "body" request}))})
        "base_url" "https://client.test"
        "schema_name" "client_api"
        "api_key" "key-client"})
      {"url" "/rest/v1/Order"}
      {"auth_token" "token-1"})
     (fn [result]
       (repl/notify result))))
  => {"status" 200
      "headers" {}
      "body" {"method" "GET"
              "url" "https://client.test/rest/v1/Order"
              "headers" {"Content-Profile" "client_api"
                         "Accept-Profile" "client_api"
                         "apikey" "key-client"
                         "Authorization" "Bearer token-1"}}})

^{:refer xt.db.runtime.supabase-client/create-scaffold :added "4.1.3"}
(fact "creates a supabase scaffold from client db and opts"

  (!.js
   (var client
        (supabase/resolve-client
         {"client" {"transport"
                    (js-fetch/client
                     {"request" (fn [request _opts]
                                  (return request))})
                    "base_url" "https://client.test"
                    "schema_name" "client_api"
                    "api_key" "key-client"
                    "headers" {"x-client" "nested"
                               "x-shared" "client"}}}
         {}))
   (var scaffold
        (supabase/create-scaffold
         {}
         client
         {"auth_token" "token-1"
          "headers" {"x-opt" "opt-1"
                     "x-shared" "opt"}}))
   [(fetch/client? (. scaffold ["client"]))
    (. scaffold ["base_url"])
    (. scaffold ["schema_name"])
    (. scaffold ["api_key"])
    (. scaffold ["auth_token"])
    (. (. scaffold ["headers"]) ["x-client"])
    (. (. scaffold ["headers"]) ["x-opt"])
    (. (. scaffold ["headers"]) ["x-shared"])])
  => [true
      "https://client.test"
      "client_api"
      "key-client"
      "token-1"
      "nested"
      "opt-1"
      "opt"])

^{:refer xt.db.runtime.supabase-client/pull :added "4.1.3"}
(fact "compiles pgrest pull requests and executes them through client-fetch"

  (notify/wait-on [:js 2000]
    (var seen nil)
    (promise/x:promise-then
     (supabase/pull
      {"client" {"transport"
                 (js-fetch/client
                  {"request" (fn [request _opts]
                               (:= seen request)
                               (return {"status" 200
                                        "body" {"data" [{"id" "ord-1"
                                                          "status" "open"}]}}))})
                 "headers" {"x-client" "fetch-sync"}
                 "base_url" "https://db.test"
                 "schema_name" "api"}}
      nil
      (@! +query-tree+)
      {"auth_token" "token-2"})
     (fn [out]
       (repl/notify
        [out
         (. seen ["url"])
         (. (. seen ["headers"]) ["x-client"])
         (. (. seen ["headers"]) ["Content-Profile"])
         (. (. seen ["headers"]) ["Authorization"])]))))
  => [[{"id" "ord-1"
         "status" "open"}]
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "fetch-sync"
      "api"
      "Bearer token-2"])

^{:refer xt.db.runtime.supabase-client/join-url :added "4.1"}
(fact "joins base urls and paths without duplicating or dropping slashes"

  (!.js
   [(supabase/join-url nil "/rest/v1/Order")
    (supabase/join-url "https://demo.test/" "/rest/v1/Order")
    (supabase/join-url "https://demo.test" "rest/v1/Order")
    (supabase/join-url "https://demo.test" "/rest/v1/Order")])
  => ["/rest/v1/Order"
      "https://demo.test/rest/v1/Order"
      "https://demo.test/rest/v1/Order"
      "https://demo.test/rest/v1/Order"])

^{:refer xt.db.runtime.supabase-client/resolve-request-headers :added "4.1"}
(fact "merges compiled, client and opts headers with supabase auth metadata"

  (!.js
   (var client
        (supabase/resolve-client
         {"client" {"transport"
                    (js-fetch/client
                     {"request" (fn [request _opts]
                                  (return request))})
                    "headers" {"x-client" "client"
                               "x-shared" "client"}
                    "schema_name" "client_api"
                    "api_key" "key-client"}}
         {}))
   (supabase/resolve-request-headers
    {}
    client
    {"headers" {"accept" "application/json"
                "x-shared" "compiled"}}
    {"auth_token" "token-1"
     "headers" {"x-opt" "opt-1"
                "x-shared" "opt"}}))
  => {"accept" "application/json"
      "x-client" "client"
      "x-opt" "opt-1"
      "x-shared" "opt"
      "Content-Profile" "client_api"
      "apikey" "key-client"
      "Authorization" "Bearer token-1"})

^{:refer xt.db.runtime.supabase-client/unwrap-response :added "4.1"}
(fact "unwraps supabase response bodies and data payloads"

  (!.js
   [(supabase/unwrap-response nil)
    (supabase/unwrap-response {"body" {"data" [{"id" "ord-1"}]}})
    (supabase/unwrap-response {"body" {"message" "ok"}})
    (supabase/unwrap-response {"data" [{"id" "ord-2"}]})
    (supabase/unwrap-response {"status" 204})])
  => [nil
      [{"id" "ord-1"}]
      {"message" "ok"}
      [{"id" "ord-2"}]
      {"status" 204}])


^{:refer xt.db.runtime.supabase-client/client? :added "4.1"}
(fact "detects wrapped supabase fetch clients"
  (!.js
   [(supabase/client? (supabase/client {"base_url" "https://client.test"}))
    (supabase/client? (js-fetch/client {}))
    (supabase/client? nil)])
  => [true false false])

^{:refer xt.db.runtime.supabase-client/raw-client :added "4.1"}
(fact "unwraps wrapped clients and passes plain configs through"
  (!.js
   [(supabase/raw-client (supabase/client {"base_url" "https://client.test"}))
    (supabase/raw-client {"base_url" "https://plain.test"})])
  => [{"base_url" "https://client.test"
       "::supabase" "supabase.client"}
      {"base_url" "https://plain.test"}])

^{:refer xt.db.runtime.supabase-client/resolve-transport :added "4.1"}
(fact "resolves fetch transports from wrapped clients or raw request impls"
  (!.js
   [(fetch/client? (supabase/resolve-transport
                    (supabase/client
                     {"transport"
                      (js-fetch/client
                       {"request" (fn [request _opts]
                                    (return request))})})))
    (fetch/client? (supabase/resolve-transport
                    {"request" (fn [request _opts]
                                 (return request))}))])
  => [true true])

^{:refer xt.db.runtime.supabase-client/resolve-base-url :added "4.1"}
(fact "resolves base urls from client config or opts"
  (!.js
   [(supabase/resolve-base-url
     {}
     (supabase/client {"base_url" "https://client.test"})
     {})
    (supabase/resolve-base-url
     {}
     (supabase/client {})
     {"base_url" "https://opts.test"})])
  => ["https://client.test" "https://opts.test"])

^{:refer xt.db.runtime.supabase-client/resolve-schema-name :added "4.1"}
(fact "resolves schema names from client config or opts"
  (!.js
   [(supabase/resolve-schema-name
     {}
     (supabase/client {"schema_name" "client_api"})
     {})
    (supabase/resolve-schema-name
     {}
     (supabase/client {})
     {"schema_name" "opts_api"})])
  => ["client_api" "opts_api"])

^{:refer xt.db.runtime.supabase-client/resolve-api-key :added "4.1"}
(fact "resolves api keys from client config or opts"
  (!.js
   [(supabase/resolve-api-key
     {}
     (supabase/client {"api_key" "key-client"})
     {})
    (supabase/resolve-api-key
     {}
     (supabase/client {})
     {"api_key" "key-opts"})])
  => ["key-client" "key-opts"])

^{:refer xt.db.runtime.supabase-client/resolve-auth-token :added "4.1"}
(fact "resolves auth tokens from client config or opts"
  (!.js
   [(supabase/resolve-auth-token
     {}
     (supabase/client {"auth_token" "token-client"})
     {})
    (supabase/resolve-auth-token
     {}
     (supabase/client {})
     {"auth_token" "token-opts"})])
  => ["token-client" "token-opts"])

^{:refer xt.db.runtime.supabase-client/dispatch-request :added "4.1"}
(fact "dispatches prepared requests through the resolved transport"
  (notify/wait-on [:js 2000]
    (promise/x:promise-then
     (supabase/dispatch-request
      {"transport"
       (js-fetch/client
        {"request" (fn [request _opts]
                     (return {"status" 200
                              "body" request}))})
       "base_url" "https://client.test"
       "schema_name" "client_api"
       "api_key" "key-client"}
      {"url" "/rest/v1/Order"
       "headers" {"x-opt" "opt-1"}}
      {"auth_token" "token-1"})
     (fn [result]
       (repl/notify result))))
  => {"status" 200
      "headers" {}
      "body" {"method" "GET"
              "url" "https://client.test/rest/v1/Order"
              "headers" {"x-opt" "opt-1"
                         "Content-Profile" "client_api"
                         "Accept-Profile" "client_api"
                         "apikey" "key-client"
                         "Authorization" "Bearer token-1"}}})