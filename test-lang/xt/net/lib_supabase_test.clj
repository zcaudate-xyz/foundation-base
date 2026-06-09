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
   :require [[js.net.http-fetch :as js-fetch]
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
  [apikey]
  (return
   (lib-supabase/create-client
    (js-fetch/create-methods)
    "127.0.0.1"
    (@! (-> docker-min/+config+ :api :port))
    false
    ""
    apikey)))

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
  => {"::" "net.superbase"
      "defaults"
      {"basepath" ""
       "host" "127.0.0.1"
       "secured" false
       "port" "55121"
       "headers"
       {"apikey" "key-client"
        "Content-Type" "application/json"
        "Accept" "application/json"}}})

^{:refer xt.net.lib-supabase/request :added "4.1"}
(fact "routes arbitrary requests through the live Supabase instance"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/request {"path" "/auth/v1/health"
                               "method" "GET"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["name"])])))))
  => [200 "GoTrue"])

^{:refer xt.net.lib-supabase/request-get :added "4.1"}
(fact "reads the live settings endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/request-get "/auth/v1/settings" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["disable_signup"])])))))
  => [200 false])

^{:refer xt.net.lib-supabase/request-json :added "4.1"}
(fact "posts JSON payloads to the live signup endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/request-json "/auth/v1/signup" "POST" {"email" email "password" "123456789"} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["user"] ["email"])])))))
  => (contains-in [200 string?]))

^{:refer xt.net.lib-supabase/rpc-call :added "4.1"}
(fact "calls an rpc entry"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/rpc-call "ping" {} {"headers" {"Content-Profile" "scratch_v0"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" "pong", "status" 200, "headers" {}}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/rpc-call "log_append_public"
                               {"i_message" "hello"}
                               {"headers" {"Content-Profile" "scratch_v0"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" {"message" "permission denied for function log_append_public", "hint" nil, "details" nil, "code" "42501"}, "status" 401, "headers" {}}

  (notify/wait-on :js
    (-> (-/default-client nil)
        (lib-supabase/rpc-call "log_append_public"
                               {"i_message" "hello"}
                               {"headers" {"Content-Profile" "scratch_v0"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"body" {"message" "permission denied for function log_append_public", "hint" nil, "details" nil, "code" "42501"}, "status" 401, "headers" {}})

^{:refer xt.net.lib-supabase/query-table :added "4.1"}
(fact "TODO")

^{:refer xt.net.lib-supabase/health :added "4.1"}
(fact "calls the auth health endpoint against local supabase"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/health {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["name"])])))))
  => [200 "GoTrue"])

^{:refer xt.net.lib-supabase/signup :added "4.1"}
(fact "signs up a user through the local auth endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/signup {"email" email
                              "password" "123456789"}
                             {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["user"] ["email"])])))))
  => (contains-in [200 string?]))

^{:refer xt.net.lib-supabase/admin-create-user :added "4.1"}
(fact "creates and cleans up a live auth user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (var client (-/default-client (@! (-> docker-min/+config+ :api :service-key))))
    (-> (lib-supabase/admin-create-user client
                                        {"email" email
                                         "password" "pass123456"
                                         "email_confirm" true}
                                        {"token" (@! (-> docker-min/+config+ :api :service-key))})
        (promise/x:promise-then
         (fn [created]
           (var body (. created ["body"]))
           (var user-id (. body ["id"]))
           (-> (lib-supabase/admin-delete-user client user-id {"token" (@! (-> docker-min/+config+ :api :service-key))})
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify [(. created ["status"])
                                (. body ["email"])
                                (. deleted ["status"])
                                (. deleted ["body"])]))))))))
  => (contains-in [200 string? 200 {}]))

^{:refer xt.net.lib-supabase/admin-delete-user :added "4.1"}
(fact "deletes a live auth user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (var client (-/default-client (@! (-> docker-min/+config+ :api :service-key))))
    (-> (lib-supabase/admin-create-user client
                                        {"email" email
                                         "password" "pass123456"
                                         "email_confirm" true}
                                        {"token" (@! (-> docker-min/+config+ :api :service-key))})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (lib-supabase/admin-delete-user client user-id {"token" (@! (-> docker-min/+config+ :api :service-key))})
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify [(. deleted ["status"])
                                (. deleted ["body"])]))))))))
  => [200 {}])

^{:refer xt.net.lib-supabase/admin-generate-link :added "4.1"}
(fact "requires a bearer token for the admin generate-link endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :service-key)))
        (lib-supabase/admin-generate-link {"type" "magiclink"
                                           "email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")}
                                          {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.lib-supabase/admin-get-user :added "4.1"}
(fact "fetches a live auth user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (var client (-/default-client (@! (-> docker-min/+config+ :api :service-key))))
    (-> (lib-supabase/admin-create-user client
                                        {"email" email
                                         "password" "pass123456"
                                         "email_confirm" true}
                                        {"token" (@! (-> docker-min/+config+ :api :service-key))})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (lib-supabase/admin-get-user client user-id {"token" (@! (-> docker-min/+config+ :api :service-key))})
               (promise/x:promise-then
                (fn [got]
                  (-> (lib-supabase/admin-delete-user client user-id {"token" (@! (-> docker-min/+config+ :api :service-key))})
                      (promise/x:promise-then
                       (fn [deleted]
                         (repl/notify [(. got ["status"])
                                       (. (. got ["body"]) ["email"])
                                       (. deleted ["status"])])))))))))))
  => (contains-in [200 string? 200]))

^{:refer xt.net.lib-supabase/admin-list-users :added "4.1"}
(fact "lists users through the local admin auth endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :service-key)))
        (lib-supabase/admin-list-users {"token" (@! (-> docker-min/+config+ :api :service-key))})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["aud"])])))))
  => [200 "authenticated"])

^{:refer xt.net.lib-supabase/admin-update-user :added "4.1"}
(fact "updates a live auth user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (var client (-/default-client (@! (-> docker-min/+config+ :api :service-key))))
    (-> (lib-supabase/admin-create-user client
                                        {"email" email
                                         "password" "pass123456"
                                         "email_confirm" true}
                                        {"token" (@! (-> docker-min/+config+ :api :service-key))})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (lib-supabase/admin-update-user client
                                               user-id
                                               {"body" (xt/x:json-encode {"user_metadata" {"note" "updated-by-test"}})
                                                "token" (@! (-> docker-min/+config+ :api :service-key))})
               (promise/x:promise-then
                (fn [updated]
                  (-> (lib-supabase/admin-delete-user client user-id {"token" (@! (-> docker-min/+config+ :api :service-key))})
                      (promise/x:promise-then
                       (fn [deleted]
                         (repl/notify [(. updated ["status"])
                                       (. (. updated ["body"]) ["user_metadata"] ["note"])
                                       (. deleted ["status"])])))))))))))
  => [200 "updated-by-test" 200])

^{:refer xt.net.lib-supabase/authorize :added "4.1"}
(fact "returns the live OAuth provider validation failure"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/authorize {"redirect_to" "http://localhost/callback"} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.net.lib-supabase/callback :added "4.1"}
(fact "returns the live OAuth callback state error"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/callback {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "bad_oauth_callback"])

^{:refer xt.net.lib-supabase/invite :added "4.1"}
(fact "requires authorization on the live invite endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :service-key)))
        (lib-supabase/invite {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.lib-supabase/logout :added "4.1"}
(fact "requires authorization on the live logout endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :service-key)))
        (lib-supabase/logout {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.lib-supabase/otp :added "4.1"}
(fact "returns an empty success response for passwordless email OTP requests"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/otp {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. out ["body"])])))))
  => [200 {}])

^{:refer xt.net.lib-supabase/recovery :added "4.1"}
(fact "returns an empty success response for recovery emails"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/recovery {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. out ["body"])])))))
  => [200 {}])

^{:refer xt.net.lib-supabase/settings :added "4.1"}
(fact "reads the live auth settings endpoint"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/settings {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["external"] ["email"])])))))
  => [200 true])

^{:refer xt.net.lib-supabase/token-password :added "4.1"}
(fact "returns the live validation failure for password sign-in"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/token-password {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")
                                      "password" "123456789"}
                                     {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 nil])

^{:refer xt.net.lib-supabase/token-refresh :added "4.1"}
(fact "refreshes a live auth session"

  (notify/wait-on :js
    (var email (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com"))
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/signup {"email" email
                              "password" "123456789"}
                             {})
        (promise/x:promise-then
         (fn [signed-up]
           (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
               (lib-supabase/token-refresh {"refresh_token" (. (. signed-up ["body"]) ["refresh_token"])}
                                           {})
               (promise/x:promise-then
                (fn [refreshed]
                  (repl/notify [(. refreshed ["status"])
                                (. refreshed ["body"])]))))))))
  => (contains-in [200 {"access_token" string? "refresh_token" string?}]))

^{:refer xt.net.lib-supabase/user-get :added "4.1"}
(fact "returns a bearer-token error when the anon client has no session"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/user-get {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.lib-supabase/user-put :added "4.1"}
(fact "returns a bearer-token error when the anon client has no session"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :service-key)))
        (lib-supabase/user-put {"data" {"note" "updated-by-test"}} {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.lib-supabase/verify-get :added "4.1"}
(fact "returns the live verify validation error when the token is missing"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/verify-get {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")
                                 "type" "email"}
                                {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.net.lib-supabase/verify-post :added "4.1"}
(fact "returns the live verify validation error when the token is missing"

  (notify/wait-on :js
    (-> (-/default-client (@! (-> docker-min/+config+ :api :anon-key)))
        (lib-supabase/verify-post {"email" (xt/x:cat "lib-supabase-" (xt/x:to-string (xt/x:now-ms)) "@example.com")
                                  "type" "email"}
                                 {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])
