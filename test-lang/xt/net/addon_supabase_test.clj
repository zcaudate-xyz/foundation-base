(ns xt.net.addon-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[js.net.http-fetch :as js-fetch]
             [xt.net.http-fetch :as http-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-client
  [apikey token]
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey apikey
     :token token}
    (addon/middleware-supabase))))

^{:refer xt.net.addon-supabase/wrap-supabase-auth :added "4.1"}
(fact "adds the Supabase auth headers while preserving custom request headers"
  (!.js
    (var wrapped (addon/wrap-supabase-auth
                  (fn [client input]
                    (return input))))
    (wrapped
     {:defaults {:apikey "default-apikey"
                 :token "default-token"}}
     {:path "/auth/v1/health"
      :apikey "input-apikey"
      :token "input-token"
      :headers {"x-trace" "trace-1"}}))
  => {"apikey" "input-apikey"
      "headers" {"Accept" "application/json"
                  "Authorization" "Bearer input-token"
                  "Content-Type" "application/json"
                  "apikey" "input-apikey"
                  "x-trace" "trace-1"}
      "path" "/auth/v1/health"
      "token" "input-token"})

^{:refer xt.net.addon-supabase/middleware-supabase :added "4.1"}
(fact "composes the fetch middleware stack"
  (!.js
    (xt/x:len (addon/middleware-supabase)))
  => 3)

^{:refer xt.net.addon-supabase/cmd-health :added "4.1"}
(fact "builds the auth health request"
  (addon/cmd-health {})
  => {"method" "GET"
      "path" "/auth/v1/health"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-health {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in
      {"body" {"name" "GoTrue"}
       "status" 200
       "headers" {}}))

^{:refer xt.net.addon-supabase/cmd-signup :added "4.1"}
(fact "builds the auth signup request"
  (addon/cmd-signup {"email" "hello@example.com"
                     "password" "secret123"}
                    {})
  => {"body" "{\"email\":\"hello@example.com\",\"password\":\"secret123\"}"
      "method" "POST"
      "path" "/auth/v1/signup"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-signup {"email" email
                            "password" "secret123"}
                           {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["user"] ["email"])])))))
  => (contains-in [200 string?]))

^{:refer xt.net.addon-supabase/cmd-admin-create-user :added "4.1"}
(fact "builds the admin create-user request"
  (addon/cmd-admin-create-user {"email" "hello@example.com"
                                "password" "secret123"
                                "email_confirm" true}
                               {})
  => {"body" "{\"email\":\"hello@example.com\",\"password\":\"secret123\",\"email_confirm\":true}"
      "method" "POST"
      "path" "/auth/v1/admin/users"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                  (@! (-> local-min/+config+ :api :service-key))))
    (-> (http-fetch/request-http client
                                 (addon/cmd-admin-create-user {"email" email
                                                               "password" "pass123456"
                                                               "email_confirm" true}
                                                              {}))
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (http-fetch/request-http client
                                        (addon/cmd-admin-delete-user user-id {}))
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify [(. created ["status"])
                                (. (. created ["body"]) ["email"])
                                (. deleted ["status"])
                                (. deleted ["body"])]))))))))
  => (contains-in [200 string? 200 {}]))

^{:refer xt.net.addon-supabase/cmd-admin-delete-user :added "4.1"}
(fact "builds the admin delete-user request"
  (addon/cmd-admin-delete-user "user-1" {})
  => {"method" "DELETE"
      "path" "/auth/v1/admin/users/user-1"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                  (@! (-> local-min/+config+ :api :service-key))))
    (-> (http-fetch/request-http client
                                 (addon/cmd-admin-create-user {"email" email
                                                               "password" "pass123456"
                                                               "email_confirm" true}
                                                              {}))
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (http-fetch/request-http client
                                        (addon/cmd-admin-delete-user user-id {}))
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify [(. deleted ["status"])
                                (. deleted ["body"])]))))))))
  => [200 {}])

^{:refer xt.net.addon-supabase/cmd-authorize :added "4.1"}
(fact "builds the authorize request"
  (addon/cmd-authorize {"redirect_to" "http://localhost/callback"} {})
  => {"method" "GET"
      "path" "/auth/v1/authorize?redirect_to=http://localhost/callback"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-authorize {"redirect_to" "http://localhost/callback"} {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.net.addon-supabase/cmd-verify-get :added "4.1"}
(fact "builds the verify get request"
  (addon/cmd-verify-get {"type" "email"} {})
  => {"method" "GET"
      "path" "/auth/v1/verify?type=email"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-verify-get {"type" "email"
                                "email" (xt/x:cat "addon-supabase-"
                                                   (xt/x:to-string (xt/x:now-ms))
                                                   "@example.com")} {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.net.addon-supabase/cmd-verify-post :added "4.1"}
(fact "builds the verify post request"
  (addon/cmd-verify-post {"type" "email"
                          "token" "abc123"}
                         {})
  => {"body" "{\"type\":\"email\",\"token\":\"abc123\"}"
      "method" "POST"
      "path" "/auth/v1/verify"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-verify-post {"type" "email"
                                 "token" "abc123"}
                                {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.net.addon-supabase/cmd-token-password :added "4.1"}
(fact "builds the password sign-in request"
  (addon/cmd-token-password {"email" "hello@example.com"
                             "password" "secret123"}
                            {})
  => {"body" "{\"email\":\"hello@example.com\",\"password\":\"secret123\"}"
      "method" "POST"
      "path" "/auth/v1/token?grant_type=password"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-token-password {"email" (xt/x:cat "addon-supabase-"
                                                       (xt/x:to-string (xt/x:now-ms))
                                                       "@example.com")
                                  "password" "secret123"}
                                 {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [400 "invalid_credentials"])

^{:refer xt.net.addon-supabase/cmd-token-refresh :added "4.1"}
(fact "builds the refresh-token request"
  (addon/cmd-token-refresh {"refresh_token" "refresh-1"} {})
  => {"body" "{\"refresh_token\":\"refresh-1\"}"
      "method" "POST"
      "path" "/auth/v1/token?grant_type=refresh_token"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-signup {"email" email
                            "password" "secret123"}
                           {}))
        (promise/x:promise-then
         (fn [signed-up]
           (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
               (http-fetch/request-http
                (addon/cmd-token-refresh {"refresh_token" (. (. signed-up ["body"]) ["refresh_token"])}
                                         {}))
               (promise/x:promise-then
                (fn [refreshed]
                  (repl/notify [(. refreshed ["status"])
                                (. refreshed ["body"])]))))))))
  => (contains-in [200 {"access_token" string? "refresh_token" string?}]))

^{:refer xt.net.addon-supabase/cmd-user-get :added "4.1"}
(fact "builds the current-user request"
  (addon/cmd-user-get {})
  => {"method" "GET"
      "path" "/auth/v1/user"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key)) nil)
        (http-fetch/request-http
         (addon/cmd-user-get {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.addon-supabase/cmd-user-put :added "4.1"}
(fact "builds the current-user update request"
  (addon/cmd-user-put {"data" {"note" "updated-by-test"}} {})
  => {"body" "{\"data\":{\"note\":\"updated-by-test\"}}"
      "method" "PUT"
      "path" "/auth/v1/user"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key)) nil)
        (http-fetch/request-http
         (addon/cmd-user-put {"data" {"note" "updated-by-test"}} {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.addon-supabase/cmd-settings :added "4.1"}
(fact "builds the settings request"
  (addon/cmd-settings {})
  => {"method" "GET"
      "path" "/auth/v1/settings"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-settings {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["external"] ["email"])])))))
  => [200 true])

^{:refer xt.net.addon-supabase/cmd-logout :added "4.1"}
(fact "builds the logout request"
  (addon/cmd-logout {})
  => {"method" "POST"
      "path" "/auth/v1/logout"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key)) nil)
        (http-fetch/request-http
         (addon/cmd-logout {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.addon-supabase/cmd-admin-list-users :added "4.1"}
(fact "builds the admin list-users request"
  (addon/cmd-admin-list-users {})
  => {"method" "GET"
      "path" "/auth/v1/admin/users"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key))
                          (@! (-> local-min/+config+ :api :service-key)))
        (http-fetch/request-http
         (addon/cmd-admin-list-users {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["aud"])])))))
  => [200 "authenticated"])

^{:refer xt.net.addon-supabase/cmd-callback :added "4.1"}
(fact "builds the callback request"
  (addon/cmd-callback {})
  => {"method" "GET"
      "path" "/auth/v1/callback"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-callback {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [200 nil])

^{:refer xt.net.addon-supabase/cmd-otp :added "4.1"}
(fact "builds the otp request"
  (addon/cmd-otp {"email" "hello@example.com"} {})
  => {"body" "{\"email\":\"hello@example.com\"}"
      "method" "POST"
      "path" "/auth/v1/otp"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-otp {"email" (xt/x:cat "addon-supabase-"
                                           (xt/x:to-string (xt/x:now-ms))
                                           "@example.com")}
                        {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. out ["body"])])))))
  => [200 {}])

^{:refer xt.net.addon-supabase/cmd-recovery :added "4.1"}
(fact "builds the recovery request"
  (addon/cmd-recovery {"email" "hello@example.com"} {})
  => {"body" "{\"email\":\"hello@example.com\"}"
      "method" "POST"
      "path" "/auth/v1/recover"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)
        (http-fetch/request-http
         (addon/cmd-recovery {"email" (xt/x:cat "addon-supabase-"
                                                 (xt/x:to-string (xt/x:now-ms))
                                                 "@example.com")}
                              {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. out ["body"])])))))
  => [200 {}])

^{:refer xt.net.addon-supabase/cmd-invite :added "4.1"}
(fact "builds the invite request"
  (addon/cmd-invite {"email" "hello@example.com"} {})
  => {"body" "{\"email\":\"hello@example.com\"}"
      "method" "POST"
      "path" "/auth/v1/invite"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key)) nil)
        (http-fetch/request-http
         (addon/cmd-invite {"email" (xt/x:cat "addon-supabase-"
                                               (xt/x:to-string (xt/x:now-ms))
                                               "@example.com")}
                            {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.net.addon-supabase/cmd-admin-generate-link :added "4.1"}
(fact "builds the admin generate-link request"
  (addon/cmd-admin-generate-link {"type" "magiclink"
                                  "email" "hello@example.com"}
                                 {})
  => {"body" "{\"type\":\"magiclink\",\"email\":\"hello@example.com\"}"
      "method" "POST"
      "path" "/auth/v1/admin/generate_link"}

  (notify/wait-on :js
    (-> (-/default-client (@! (-> local-min/+config+ :api :service-key))
                          (@! (-> local-min/+config+ :api :service-key)))
        (http-fetch/request-http
         (addon/cmd-admin-generate-link {"type" "magiclink"
                                         "email" (xt/x:cat "addon-supabase-"
                                                            (xt/x:to-string (xt/x:now-ms))
                                                            "@example.com")}
                                        {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["status"])
                         (. (. out ["body"]) ["error_code"])])))))
  => [200 nil])

^{:refer xt.net.addon-supabase/cmd-admin-get-user :added "4.1"}
(fact "builds the admin get-user request"
  (addon/cmd-admin-get-user "user-1" {})
  => {"method" "GET"
      "path" "/auth/v1/admin/users/user-1"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                  (@! (-> local-min/+config+ :api :service-key))))
    (-> (http-fetch/request-http client
                                 (addon/cmd-admin-create-user {"email" email
                                                               "password" "pass123456"
                                                               "email_confirm" true}
                                                              {}))
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (http-fetch/request-http client
                                        (addon/cmd-admin-get-user user-id {}))
               (promise/x:promise-then
                (fn [got]
                  (-> (http-fetch/request-http client
                                               (addon/cmd-admin-delete-user user-id {}))
                      (promise/x:promise-then
                       (fn [deleted]
                         (repl/notify [(. got ["status"])
                                       (. (. got ["body"]) ["email"])
                                       (. deleted ["status"])])))))))))))
  => (contains-in [200 string? 200]))

^{:refer xt.net.addon-supabase/cmd-admin-update-user :added "4.1"}
(fact "builds the admin update-user request"
  (addon/cmd-admin-update-user "user-1" {})
  => {"method" "PUT"
      "path" "/auth/v1/admin/users/user-1"}

  (notify/wait-on :js
    (var email (xt/x:cat "addon-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/default-client (@! (-> local-min/+config+ :api :service-key))
                                  (@! (-> local-min/+config+ :api :service-key))))
    (-> (http-fetch/request-http client
                                 (addon/cmd-admin-create-user {"email" email
                                                               "password" "pass123456"
                                                               "email_confirm" true}
                                                              {}))
        (promise/x:promise-then
         (fn [created]
           (var user-id (. (. created ["body"]) ["id"]))
           (-> (http-fetch/request-http client
                                        (addon/cmd-admin-update-user user-id
                                                                     {"body" (xt/x:json-encode {"user_metadata" {"note" "updated-by-test"}})
                                                                      "token" (@! (-> local-min/+config+ :api :service-key))} ))
               (promise/x:promise-then
                (fn [updated]
                  (-> (http-fetch/request-http client
                                               (addon/cmd-admin-delete-user user-id {}))
                      (promise/x:promise-then
                       (fn [deleted]
                         (repl/notify [(. updated ["status"])
                                       (. (. updated ["body"]) ["user_metadata"] ["note"])
                                       (. deleted ["status"])])))))))))))
  => [200 "updated-by-test" 200])


^{:refer xt.net.addon-supabase/cmd-rpc-call :added "4.1"}
(fact "builds the rpc call request"
  (addon/cmd-rpc-call "ping" {} {})
  => {"body" "{}"
      "method" "POST"
      "path" "/rest/v1/rpc/ping"}

  (addon/cmd-rpc-call "ping" {"message" "hello"} {"headers" {"x-trace" "1"}})
  => {"body" "{\"message\":\"hello\"}"
      "headers" {"x-trace" "1"}
      "method" "POST"
      "path" "/rest/v1/rpc/ping"})

^{:refer xt.net.addon-supabase/cmd-query-table :added "4.1"}
(fact "builds the query table request"
  (addon/cmd-query-table "scratch_v0.Log" "select=*" {})
  => {"method" "GET"
      "path" "/rest/v1/scratch_v0.Log?select=*"}

  (addon/cmd-query-table "scratch_v0.Log" "select=id" {"headers" {"Prefer" "count=exact"}})
  => {"headers" {"Prefer" "count=exact"}
      "method" "GET"
      "path" "/rest/v1/scratch_v0.Log?select=id"})