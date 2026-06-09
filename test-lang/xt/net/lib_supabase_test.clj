(ns xt.net.lib-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]
             [postgres.core :as pg]
             [postgres.core.supabase :as s]]
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :shutdown docker-min/stop-supabase}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as client-fetch]
             [js.net.http-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.lib-supabase :as lib-supabase]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-client
  []
  (return
   (lib-supabase/create-client
    (js-fetch/create-methods)
    (@! (-> docker-min/+config+ :api :hostname))
    (@! (-> docker-min/+config+ :api :port))
    false
    ""
    (@! (-> docker-min/+config+ :api :anon-key)))))

^{:refer xt.net.lib-supabase/create-client :added "4.1"}
(fact "creates a supabase client wrapper with the expected defaults"

  (!.js
    (lib-supabase/create-client
     (js-fetch/create-methods)
     "127.0.0.1"
     "55121"
     false
     "/auth/v1"
     "key-client"))
  => {"::" "net.superbase",
      "defaults"
      {"basepath" "",
       "host" "127.0.0.1",
       "secured" false,
       "port" "55121",
       "headers"
       {"apikey" "key-client",
        "Content-Type" "application/json",
        "Accept" "application/json"}}})

^{:refer xt.net.lib-supabase/request :added "4.1"}
(fact "merges auth headers and dispatches the wrapped request"

  (notify/wait-on :js
    (-> (-/default-client)
        (lib-supabase/request {:path "/auth/v1/health"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"body" {"name" "GoTrue"},
       "status" 200, "headers" {}}))

^{:refer xt.net.lib-supabase/query-path :added "4.1"}
(fact "encodes flat query params onto a path"

  (!.js
    (lib-supabase/query-path
     "/auth/v1/authorize"
     {"query" {"redirect_to" "https://example.com/callback"}}))
  => "/auth/v1/authorize?redirect_to=https://example.com/callback")

^{:refer xt.net.lib-supabase/request-json :added "4.1"}
(fact "json encodes request bodies before dispatching"

  (notify/wait-on :js
    (var seen nil)
    (var client
         (lib-supabase/create-client
          {"request_http"
           (fn [self input opts]
             (:= seen input)
             (return {"status" 200
                      "body" input}))}
          "https://client.test"
          "key-client"
          false
          ""
          "key-client"))
    (promise/x:promise-then
     (lib-supabase/request-json client
                                "/auth/v1/verify"
                                "POST"
                                {"email" "alice@example.com"}
                                {"headers" {"X-Test" "1"}})
     (fn [out]
       (repl/notify [(. seen ["method"])
                     (. seen ["body"])
                     (. (. seen ["headers"]) ["X-Test"])
                     (. (. out ["body"]) ["email"])]))))
  => ["POST"
      "{"email":"alice@example.com"}"
      "1"
      "alice@example.com"])

^{:refer xt.net.lib-supabase/health :added "4.1"}
(fact "calls the auth health endpoint"

  (notify/wait-on :js
    (-> (-/default-client)
        (lib-supabase/health {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify (. out status))))))
  => 200)

^{:refer xt.net.lib-supabase/signup :added "4.1"}
(fact "signs up a user through the auth endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (lib-supabase/create-client
         (js-fetch/create-methods)
         (-> docker-min/+config+ :api :hostname)
         (-> docker-min/+config+ :api :port)
         false
         ""
         (-> docker-min/+config+ :api :anon-key))
        (lib-supabase/signup {"email" email
                              "password" "123456789"}
                             {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out status)
                         (. (. out body) ["user"] ["email"])])))))
  => [200 string?])

^{:refer xt.net.lib-supabase/admin-list-users :added "4.1"}
(fact "lists users through the admin auth endpoint"

  (notify/wait-on :js
    (-> (lib-supabase/create-client
         (js-fetch/create-methods)
         (-> docker-min/+config+ :api :hostname)
         (-> docker-min/+config+ :api :port)
         false
         ""
         (-> docker-min/+config+ :api :service-key))
        (lib-supabase/admin-list-users {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [. out status
                         (. (. out body) ["aud"])])))))
  => [200 "authenticated"])

^{:refer xt.net.lib-supabase/authorize :added "4.1"}
(fact "routes authorize requests through the query-path helper"

  (notify/wait-on :js
    (var seen nil)
    (var client
         (lib-supabase/create-client
          {"request_http"
           (fn [self input opts]
             (:= seen input)
             (return {"status" 200
                      "body" input}))}
          "https://client.test"
          "key-client"
          false
          ""
          "key-client"))
    (promise/x:promise-then
     (lib-supabase/authorize client
                             {"query" {"redirect_to" "https://example.com/callback"}})
     (fn [out]
       (repl/notify [(. seen ["method"])
                     (. seen ["path"])
                     (. (. out ["body"]) ["path"])]))))
  => ["GET"
      "/auth/v1/authorize?redirect_to=https://example.com/callback"
      "/auth/v1/authorize?redirect_to=https://example.com/callback"])

^{:refer xt.net.lib-supabase/verify-post :added "4.1"}
(fact "posts verify payloads as json"

  (notify/wait-on :js
    (var seen nil)
    (var client
         (lib-supabase/create-client
          {"request_http"
           (fn [self input opts]
             (:= seen input)
             (return {"status" 200
                      "body" input}))}
          "https://client.test"
          "key-client"
          false
          ""
          "key-client"))
    (promise/x:promise-then
     (lib-supabase/verify-post client
                               {"type" "signup"
                                "token" "token-1"}
                               {})
     (fn [out]
       (repl/notify [(. seen ["method"])
                     (. seen ["body"])
                     (. (. out ["body"]) ["type"])
                     (. (. out ["body"] ) ["token"])]))))
  => ["POST"
      "{"type":"signup","token":"token-1"}"
      "signup"
      "token-1"])
