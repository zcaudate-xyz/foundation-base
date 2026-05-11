(ns xt.db.runtime.supabase-pull-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
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

  (!.js
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

  (!.js
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

  (!.js
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

  (!.js
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
