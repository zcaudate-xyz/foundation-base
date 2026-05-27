(ns xt.lib.supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lib.supabase :as supabase]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lib.supabase/create-client :added "4.1.4"}
(fact "creates wrapped xtalk supabase clients"
  (!.js
   (var client (supabase/create-client
                "https://client.test"
                "key-client"
                {"schema_name" "client_api"}))
   [(supabase/client? client)
    (. (supabase/raw-client client) ["base_url"])
    (. (supabase/raw-client client) ["api_key"])
    (. (supabase/raw-client client) ["schema_name"])])
  => [true "https://client.test" "key-client" "client_api"])

^{:refer xt.lib.supabase/sign-in-with-password :added "4.1.4"}
(fact "signs in with password through the auth endpoint and stores session state"
  (notify/wait-on [:js 2000]
    (var seen nil)
    (var client
         (supabase/create-client
          "https://client.test"
          "key-client"
          {"schema_name" "client_api"
           "transport"
           (js-fetch/client
            {"request" (fn [request _opts]
                         (:= seen request)
                         (return {"status" 200
                                  "body" {"access_token" "token-1"
                                          "refresh_token" "refresh-1"
                                          "expires_in" 3600
                                          "token_type" "bearer"
                                          "user" {"id" "user-1"}
                                          "weak_password" {"message" "warn"
                                                           "reasons" ["short"]}}}))})}))
    (promise/x:promise-then
     (supabase/sign-in-with-password
      client
      {"email" "hello@world.com"
       "password" "secret"
       "options" {"captcha_token" "captcha-1"}})
     (fn [result]
       (promise/x:promise-then
        (supabase/get-session client)
        (fn [stored]
          (var payload (xt/x:json-decode (. seen ["body"])))
          (repl/notify
           [(xt/x:get-key (xt/x:get-key (xt/x:get-key result "data") "user") "id")
            (xt/x:get-key (xt/x:get-key (xt/x:get-key result "data") "session") "access_token")
            (xt/x:get-key (xt/x:get-key (xt/x:get-key result "data") "weak_password") "message")
            (xt/x:not-nil? (xt/x:get-key (xt/x:get-key (xt/x:get-key result "data") "session") "expires_at"))
            (xt/x:get-key (xt/x:get-key (xt/x:get-key stored "data") "session") "refresh_token")
            (. seen ["url"])
            (. (. seen ["headers"]) ["apikey"])
            (. (. seen ["headers"]) ["Content-Type"])
            (. (. seen ["headers"]) ["Content-Profile"])
            (xt/x:get-key (xt/x:get-key payload "gotrue_meta_security") "captcha_token")
            (. (supabase/raw-client client) ["auth_token"])]))))))
  => ["user-1"
      "token-1"
      "warn"
      true
      "refresh-1"
      "https://client.test/auth/v1/token?grant_type=password"
      "key-client"
      "application/json;charset=UTF-8"
      nil
      "captcha-1"
      "token-1"])

^{:refer xt.lib.supabase/sign-out :added "4.1.4"}
(fact "signs out through the auth endpoint and clears the current session"
  (notify/wait-on [:js 2000]
    (var seen nil)
    (var client
         (supabase/create-client
          "https://client.test"
          "key-client"
          {"transport"
           (js-fetch/client
            {"request" (fn [request _opts]
                         (:= seen request)
                         (return {"status" 204
                                  "body" nil}))})}))
    (xt/x:set-key (supabase/raw-client client)
                  "session"
                  {"access_token" "token-1"
                   "refresh_token" "refresh-1"
                   "user" {"id" "user-1"}})
    (xt/x:set-key (supabase/raw-client client)
                  "auth_token"
                  "token-1")
    (promise/x:promise-then
     (supabase/sign-out client {"scope" "global"})
     (fn [result]
       (repl/notify
        [(. result ["error"])
         (. seen ["url"])
         (. (. seen ["headers"]) ["Authorization"])
         (. (supabase/raw-client client) ["session"])
         (. (supabase/raw-client client) ["auth_token"])]))))
  => [nil
      "https://client.test/auth/v1/logout?scope=global"
      "Bearer token-1"
      nil
      nil])
