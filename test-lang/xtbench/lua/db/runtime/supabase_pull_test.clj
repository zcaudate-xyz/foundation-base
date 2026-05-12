(ns xtbench.lua.db.runtime.supabase-pull-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua
 {:runtime :basic
  :require [[xt.lang.spec-base :as xt]
             [xt.db.runtime.supabase-pull :as supabase-pull]
             [xt.db.text.pgrest :as pgrest]
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

^{:refer xt.db.runtime.supabase-pull/resolve-client :added "4.1.3"}
(fact "wraps raw request sources as fetch clients"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))}}
         {}))
   [(fetch/client? client)
    (fetch/request-sync client {"url" "/health"} nil)])
  => [true {"url" "/health"}])

^{:refer xt.db.runtime.supabase-pull/create-scaffold :added "4.1.3"}
(fact "creates a supabase scaffold from client db and opts"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "base_url" "https://client.test"
                    "schema_name" "client_api"
                    "api_key" "key-client"
                    "headers" {"x-client" "nested"
                               "x-shared" "client"}}}
         {}))
   (var scaffold
        (supabase-pull/create-scaffold
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

^{:refer xt.db.runtime.supabase-pull/prepare-request :added "4.1.3"}
(fact "prepares pgrest requests with joined urls and supabase headers"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "headers" {"x-client" "nested"}
                    "base_url" "https://client.test"
                    "schema_name" "client_api"
                    "api_key" "key-client"}}
         {}))
   (var compiled (pgrest/compile-query (@! +query-tree+)))
   (var request
        (supabase-pull/prepare-request
         {}
         client
         compiled
         {"auth_token" "token-1"
           "headers" {"x-opt" "opt-1"}}))
   [(. request ["url"])
    (. (. request ["headers"]) ["x-client"])
    (. (. request ["headers"]) ["x-opt"])
    (. (. request ["headers"]) ["Content-Profile"])
    (. (. request ["headers"]) ["apikey"])
    (. (. request ["headers"]) ["Authorization"])])
  => ["https://client.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "nested"
      "opt-1"
      "client_api"
      "key-client"
      "Bearer token-1"])

^{:refer xt.db.runtime.supabase-pull/supabase-pull-sync :added "4.1.3"}
(fact "compiles pgrest pull requests and executes them through client-fetch"

  (!.lua
   (var seen nil)
   (var out
        (supabase-pull/supabase-pull-sync
         {"client" {"request_sync" (fn [request _opts]
                                     (:= seen request)
                                     (return {"status" 200
                                              "body" {"data" [{"id" "ord-1"
                                                                "status" "open"}]}}))
                    "headers" {"x-client" "fetch-sync"}
                    "base_url" "https://db.test"
                    "schema_name" "api"}}
          nil
          (@! +query-tree+)
          {"auth_token" "token-2"}))
   [out
    (. seen ["url"])
    (. (. seen ["headers"]) ["x-client"])
    (. (. seen ["headers"]) ["Content-Profile"])
    (. (. seen ["headers"]) ["Authorization"])])
  => [[{"id" "ord-1"
         "status" "open"}]
      "https://db.test/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1&id=in.(ord-1,ord-2)"
      "fetch-sync"
      "api"
      "Bearer token-2"])

^{:refer xt.db.runtime.supabase-pull/raw-client :added "4.1"}
(fact "unwraps fetch clients and defaults missing raw data to empty objects"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "base_url" "https://client.test"}}
         {}))
   [(xt/x:get-key (supabase-pull/raw-client client) "base_url")
    (xt/x:get-key (supabase-pull/raw-client {"base_url" "https://direct.test"}) "base_url")
    (supabase-pull/raw-client nil)])
  => ["https://client.test"
      "https://direct.test"
      {}])

^{:refer xt.db.runtime.supabase-pull/resolve-base-url :added "4.1"}
(fact "prefers raw client base urls before opts"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "base_url" "https://client.test"}}
         {}))
   [(supabase-pull/resolve-base-url {} client {"base_url" "https://opts.test"})
    (supabase-pull/resolve-base-url {} {"base_url" "https://direct.test"} {})
    (supabase-pull/resolve-base-url {} {} {"base_url" "https://opts-only.test"})])
  => ["https://client.test"
      "https://direct.test"
      "https://opts-only.test"])

^{:refer xt.db.runtime.supabase-pull/resolve-schema-name :added "4.1"}
(fact "prefers raw client schema names before opts"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "schema_name" "client_api"}}
         {}))
   [(supabase-pull/resolve-schema-name {} client {"schema_name" "opts_api"})
    (supabase-pull/resolve-schema-name {} {"schema_name" "direct_api"} {})
    (supabase-pull/resolve-schema-name {} {} {"schema_name" "opts_only_api"})])
  => ["client_api"
      "direct_api"
      "opts_only_api"])

^{:refer xt.db.runtime.supabase-pull/resolve-api-key :added "4.1"}
(fact "prefers raw client api keys before opts"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "api_key" "key-client"}}
         {}))
   [(supabase-pull/resolve-api-key {} client {"api_key" "key-opts"})
    (supabase-pull/resolve-api-key {} {"api_key" "key-direct"} {})
    (supabase-pull/resolve-api-key {} {} {"api_key" "key-opts-only"})])
  => ["key-client"
      "key-direct"
      "key-opts-only"])

^{:refer xt.db.runtime.supabase-pull/resolve-auth-token :added "4.1"}
(fact "prefers raw client auth tokens before opts"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "auth_token" "token-client"}}
         {}))
   [(supabase-pull/resolve-auth-token {} client {"auth_token" "token-opts"})
    (supabase-pull/resolve-auth-token {} {"auth_token" "token-direct"} {})
    (supabase-pull/resolve-auth-token {} {} {"auth_token" "token-opts-only"})])
  => ["token-client"
      "token-direct"
      "token-opts-only"])

^{:refer xt.db.runtime.supabase-pull/join-url :added "4.1"}
(fact "joins base urls and paths without duplicating or dropping slashes"

  (!.lua
   [(supabase-pull/join-url nil "/rest/v1/Order")
    (supabase-pull/join-url "https://demo.test/" "/rest/v1/Order")
    (supabase-pull/join-url "https://demo.test" "rest/v1/Order")
    (supabase-pull/join-url "https://demo.test" "/rest/v1/Order")])
  => ["/rest/v1/Order" "https://demo.test//rest/v1/Order" "https://demo.test/rest/v1/Order" "https://demo.test/rest/v1/Order"])

^{:refer xt.db.runtime.supabase-pull/resolve-request-headers :added "4.1"}
(fact "merges compiled, client and opts headers with supabase auth metadata"

  (!.lua
   (var client
        (supabase-pull/resolve-client
         {"client" {"request_sync" (fn [request _opts]
                                     (return request))
                    "headers" {"x-client" "client"
                               "x-shared" "client"}
                    "schema_name" "client_api"
                    "api_key" "key-client"}}
         {}))
   (supabase-pull/resolve-request-headers
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

^{:refer xt.db.runtime.supabase-pull/unwrap-response :added "4.1"}
(fact "unwraps supabase response bodies and data payloads"

  (!.lua
   [(supabase-pull/unwrap-response nil)
    (supabase-pull/unwrap-response {"body" {"data" [{"id" "ord-1"}]}})
    (supabase-pull/unwrap-response {"body" {"message" "ok"}})
    (supabase-pull/unwrap-response {"data" [{"id" "ord-2"}]})
    (supabase-pull/unwrap-response {"status" 204})])
  => [nil
      [{"id" "ord-1"}]
      {"message" "ok"}
      [{"id" "ord-2"}]
      {"status" 204}])
