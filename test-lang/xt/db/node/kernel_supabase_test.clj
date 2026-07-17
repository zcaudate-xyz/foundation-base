^{:seedgen/skip true}
(ns xt.db.node.kernel-supabase-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [clojure.java.shell :as sh]
            [clojure.string :as string]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]))

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
             [xt.net.http-util :as http-util]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.kernel-supabase :as adaptor]
             [xt.db.system.main :as main]
             [xt.substrate.page-core :as page-core]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
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

(defn.js node-with-service
  [client session]
  (var node (substrate/node-create {}))
  (var impl (main/create-impl "supabase"
                              (xt/x:get-key client "defaults")
                              nil
                              nil))
  (session/set-session impl session)
  (substrate/set-service node "auth/supabase" impl)
  (return node))

(defn.js service-client
  []
  (return
   (-/default-client (@! (-> local-min/+config+ :api :service-key))
                     (@! (-> local-min/+config+ :api :service-key)))))

(defn.js anon-client
  []
  (return
   (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)))

^{:refer xt.net.http-util/get-body-data :added "4.1"}
(fact "extracts the body payload from a response"
  (!.js
   (http-util/get-body-data {"body" {"access_token" "abc"}}))
  => {"access_token" "abc"})

^{:refer xt.db.node.kernel-supabase/supabase-request :added "4.1"}
(fact "executes a command through the service client"

  (notify/wait-on :js
    (-> (adaptor/supabase-request (-/node-with-service (-/anon-client) nil)
                                  "auth/supabase"
                                  (addon/cmd-health {}))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.kernel-supabase/supabase-sign-up-handler :added "4.1"}
(fact "signs up, stores the session and starts auto-refresh"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (adaptor/supabase-sign-up-handler nil
                                          ["auth/supabase" {"email" email "password" "secret123"} {}]
                                          nil
                                          node)
        (promise/x:promise-then
         (fn [out]
           (var impl (substrate/get-service node "auth/supabase"))
           (var session (session/get-session impl))
           (var timer (xt/x:not-nil? (xt/x:get-key (xt/x:get-key (xt/x:get-key impl "state") "auto_refresh") "current")))
           (session/auto-refresh-stop impl)
           (repl/notify [out session timer])))))
  => (contains-in [{"access_token" string? "refresh_token" string? "user" {"email" string?}}
                {"access_token" string? "refresh_token" string? "user" {"email" string?}}
                true]))

^{:refer xt.db.node.kernel-supabase/supabase-sign-in-handler :added "4.1"}
(fact "signs in, stores the session and starts auto-refresh"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/anon-client))
    (var node (-/node-with-service client nil))
    (-> (http-fetch/request-http client (addon/cmd-signup {"email" email "password" "secret123"} {}))
        (promise/x:promise-then
         (fn [_]
           (-> (adaptor/supabase-sign-in-handler nil
                                                 ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                 nil
                                                 node)
               (promise/x:promise-then
                (fn [out]
                  (var impl (substrate/get-service node "auth/supabase"))
                  (var session (session/get-session impl))
                  (var timer (xt/x:not-nil? (xt/x:get-key (xt/x:get-key (xt/x:get-key impl "state") "auto_refresh") "current")))
                  (session/auto-refresh-stop impl)
                  (repl/notify [out session timer]))))))))
  => (contains-in [{"access_token" string? "refresh_token" string? "user" {"email" string?}}
                {"access_token" string? "refresh_token" string? "user" {"email" string?}}
                true]))

^{:refer xt.db.node.kernel-supabase/supabase-sign-out-handler :added "4.1"}
(fact "signs out and clears the stored session"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var anon (-/anon-client))
    (var node (-/node-with-service anon nil))
    (-> (adaptor/supabase-sign-up-handler nil
                                          ["auth/supabase" {"email" email "password" "secret123"} {}]
                                          nil
                                          node)
        (promise/x:promise-then
         (fn [session]
           (session/auto-refresh-stop (substrate/get-service node "auth/supabase"))
           (var token-client (-/default-client (@! (-> local-min/+config+ :api :anon-key))
                                               (. session ["access_token"])))
           (var token-node (-/node-with-service token-client session))
           (session/auto-refresh-start (substrate/get-service token-node "auth/supabase") {"interval" 1000})
           (-> (adaptor/supabase-sign-out-handler nil ["auth/supabase" {}] nil token-node)
               (promise/x:promise-then
                (fn [out]
                  (var impl (substrate/get-service token-node "auth/supabase"))
                  (var new-session (session/get-session impl))
                  (var timer (xt/x:get-key (xt/x:get-key (xt/x:get-key impl "state") "auto_refresh") "current"))
                  (repl/notify [out new-session timer]))))))))
  => [{"status" "ok"} nil nil])

^{:refer xt.db.node.kernel-supabase/supabase-refresh-handler :added "4.1"}
(fact "refreshes the stored session"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (adaptor/supabase-sign-up-handler nil
                                          ["auth/supabase" {"email" email "password" "secret123"} {}]
                                          nil
                                          node)
        (promise/x:promise-then
         (fn [_]
           (-> (adaptor/supabase-refresh-handler nil ["auth/supabase"] nil node)
               (promise/x:promise-then
                (fn [out]
                  (var impl (substrate/get-service node "auth/supabase"))
                  (var session (session/get-session impl))
                  (session/auto-refresh-stop impl)
                  (repl/notify [out session]))))))))
  => (contains-in [nil
                {"access_token" string? "refresh_token" string? "user" {"email" string?}}]))

^{:refer xt.db.node.kernel-supabase/supabase-current-session-handler :added "4.1"}
(fact "returns the session stored on the service"
  (!.js
   (var node (-/node-with-service (-/anon-client) {"access_token" "abc"}))
   (adaptor/supabase-current-session-handler nil ["auth/supabase"] nil node))
  => {"access_token" "abc"})

^{:refer xt.db.node.kernel-supabase/supabase-signed-in-handler :added "4.1"}
(fact "returns whether the service has a stored session"
  (!.js
   [(adaptor/supabase-signed-in-handler nil ["auth/supabase"] nil (-/node-with-service (-/anon-client) nil))
    (adaptor/supabase-signed-in-handler nil ["auth/supabase"] nil (-/node-with-service (-/anon-client) {"access_token" "abc"}))])
  => [false true])

^{:refer xt.db.node.kernel-supabase/supabase-query-table-handler :added "4.1"}
(fact "queries a table through the service client"
  (notify/wait-on :js
    (-> (adaptor/supabase-query-table-handler nil
                                              ["auth/supabase" "Log" {}
                                               {"headers" {"Accept-Profile" "scratch_v0"}}]
                                              nil
                                              (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"has-data" (xt/x:is-array? out)})))))
  => {"has-data" true})

^{:refer xt.db.node.kernel-supabase/supabase-rpc-call-handler :added "4.1"}
(fact "calls an rpc entry on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-rpc-call-handler nil
                                            ["auth/supabase"
                                             "ping"
                                             {}
                                             {"headers" {"Accept-Profile" "scratch_v0"
                                                         "Content-Profile" "scratch_v0"}}]
                                            nil
                                            (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.node.kernel-supabase/supabase-health-handler :added "4.1"}
(fact "calls the auth health endpoint on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-health-handler nil ["auth/supabase" {}] nil (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.kernel-supabase/supabase-admin-create-user-handler :added "4.1"}
(fact "creates a user through the admin endpoint"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (adaptor/supabase-admin-create-user-handler nil
                                                    ["auth/supabase" {"email" email "password" "pass123456" "email_confirm" true} {}]
                                                    nil
                                                    (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.kernel-supabase/supabase-admin-delete-user-handler :added "4.1"}
(fact "deletes a user through the admin endpoint"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (adaptor/supabase-admin-create-user-handler nil
                                                    ["auth/supabase" {"email" email "password" "pass123456" "email_confirm" true} {}]
                                                    nil
                                                    node)
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (adaptor/supabase-admin-delete-user-handler nil ["auth/supabase" user-id {}] nil node)
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify deleted))))))))
  => {"status" 200 "http_status" 200})

^{:refer xt.db.node.kernel-supabase/supabase-admin-generate-link-handler :added "4.1"}
(fact "generates an admin link on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-admin-generate-link-handler nil
                                                      ["auth/supabase" {"type" "magiclink" "email" "test@example.com"} {}]
                                                      nil
                                                      (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["action_link"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.kernel-supabase/supabase-admin-get-user-handler :added "4.1"}
(fact "fetches a user through the admin endpoint"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (adaptor/supabase-admin-create-user-handler nil
                                                    ["auth/supabase" {"email" email "password" "pass123456" "email_confirm" true} {}]
                                                    nil
                                                    node)
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (adaptor/supabase-admin-get-user-handler nil ["auth/supabase" user-id {}] nil node)
               (promise/x:promise-then
                (fn [got]
                  (-> (adaptor/supabase-admin-delete-user-handler nil ["auth/supabase" user-id {}] nil node)
                      (promise/x:promise-then
                       (fn [_]
                         (repl/notify [(. got ["email"])])))))))))))
  => (contains-in [string?]))

^{:refer xt.db.node.kernel-supabase/supabase-admin-list-users-handler :added "4.1"}
(fact "lists users through the admin endpoint"
  (notify/wait-on :js
    (-> (adaptor/supabase-admin-list-users-handler nil ["auth/supabase" {}] nil (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["aud"])])))))
  => ["authenticated"])

^{:refer xt.db.node.kernel-supabase/supabase-admin-update-user-handler :added "4.1"}
(fact "updates a user through the admin endpoint"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (adaptor/supabase-admin-create-user-handler nil
                                                    ["auth/supabase" {"email" email "password" "pass123456" "email_confirm" true} {}]
                                                    nil
                                                    node)
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (adaptor/supabase-admin-update-user-handler nil
                                                           ["auth/supabase" user-id {"body" (xt/x:json-encode {"user_metadata" {"note" "updated-by-test"}})}]
                                                           nil
                                                           node)
               (promise/x:promise-then
                (fn [updated]
                  (-> (adaptor/supabase-admin-delete-user-handler nil ["auth/supabase" user-id {}] nil node)
                      (promise/x:promise-then
                       (fn [_]
                         (repl/notify [(xt/x:get-key (xt/x:get-key updated "user_metadata") "note")])))))))))))
  => ["updated-by-test"])

^{:refer xt.db.node.kernel-supabase/supabase-authorize-handler :added "4.1"}
(fact "starts an OAuth authorization request"
  (notify/wait-on :js
    (-> (adaptor/supabase-authorize-handler nil
                                            ["auth/supabase" {"redirect_to" "http://localhost/callback"} {}]
                                            nil
                                            (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-catch
         (fn [err]
           (var data (xt/x:ex-data err))
           (repl/notify ["rejected" (xt/x:get-key data "status")
                         (xt/x:get-key data "error_code")])))))
  => ["rejected" 400 "validation_failed"])

^{:refer xt.db.node.kernel-supabase/supabase-callback-handler :added "4.1"}
(fact "handles an OAuth callback request"
  (notify/wait-on :js
    (-> (adaptor/supabase-callback-handler nil ["auth/supabase" {}] nil (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.kernel-supabase/supabase-invite-handler :added "4.1"}
(fact "sends an invite on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-invite-handler nil
                                          ["auth/supabase" {"email" "test@example.com"} {}]
                                          nil
                                          (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.kernel-supabase/supabase-otp-handler :added "4.1"}
(fact "requests a passwordless OTP on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-otp-handler nil
                                       ["auth/supabase" {"email" (xt/x:cat "kernel-otp-"
                                                                           (xt/x:to-string (xt/x:now-ms))
                                                                           "@example.com")} {}]
                                       nil
                                       (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"status" 200 "http_status" 200})

^{:refer xt.db.node.kernel-supabase/supabase-recovery-handler :added "4.1"}
(fact "requests a recovery email on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-recovery-handler nil
                                            ["auth/supabase" {"email" (xt/x:cat "kernel-recovery-"
                                                                                 (xt/x:to-string (xt/x:now-ms))
                                                                                 "@example.com")} {}]
                                            nil
                                            (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"status" 200 "http_status" 200})

^{:refer xt.db.node.kernel-supabase/supabase-settings-handler :added "4.1"}
(fact "reads auth settings on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-settings-handler nil ["auth/supabase" {}] nil (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["external"] ["email"])])))))
  => [true])

^{:refer xt.db.node.kernel-supabase/supabase-token-refresh-handler :added "4.1"}
(fact "refreshes a session with a refresh token on the service"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/anon-client))
    (var node (-/node-with-service client nil))
    (-> (http-fetch/request-http client (addon/cmd-signup {"email" email "password" "secret123"} {}))
        (promise/x:promise-then
         (fn [signed-up]
           (var refresh-token (. (. signed-up ["body"]) ["refresh_token"]))
           (-> (adaptor/supabase-token-refresh-handler nil
                                                       ["auth/supabase" {"refresh_token" refresh-token} {}]
                                                       nil
                                                       node)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(xt/x:not-nil? (xt/x:get-key out "access_token"))
                                (xt/x:not-nil? (xt/x:get-key out "refresh_token"))]))))))))
  => [true true])

^{:refer xt.db.node.kernel-supabase/supabase-user-get-handler :added "4.1"}
(fact "fetches the current authenticated user on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-user-get-handler nil ["auth/supabase" {}] nil (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-catch
         (fn [err]
           (var data (xt/x:ex-data err))
           (repl/notify ["rejected" (xt/x:get-key data "status")
                         (xt/x:get-key data "error_code")])))))
  => ["rejected" 401 "no_authorization"])

^{:refer xt.db.node.kernel-supabase/supabase-user-put-handler :added "4.1"}
(fact "updates the current authenticated user on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-user-put-handler nil
                                            ["auth/supabase" {"data" {"note" "updated"}} {}]
                                            nil
                                            (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-catch
         (fn [err]
           (var data (xt/x:ex-data err))
           (repl/notify ["rejected" (xt/x:get-key data "status")
                         (xt/x:get-key data "error_code")])))))
  => ["rejected" 401 "no_authorization"])

^{:refer xt.db.node.kernel-supabase/supabase-verify-get-handler :added "4.1"}
(fact "verifies a token via GET on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-verify-get-handler nil
                                              ["auth/supabase" {"type" "email"} {}]
                                              nil
                                              (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-catch
         (fn [err]
           (var data (xt/x:ex-data err))
           (repl/notify ["rejected" (xt/x:get-key data "status")
                         (xt/x:get-key data "error_code")])))))
  => ["rejected" 400 "validation_failed"])

^{:refer xt.db.node.kernel-supabase/supabase-verify-post-handler :added "4.1"}
(fact "verifies a token via POST on the service"
  (notify/wait-on :js
    (-> (adaptor/supabase-verify-post-handler nil
                                               ["auth/supabase" {"type" "email" "token" "abc123"} {}]
                                               nil
                                               (-/node-with-service (-/anon-client) nil))
        (promise/x:promise-catch
         (fn [err]
           (var data (xt/x:ex-data err))
           (repl/notify ["rejected" (xt/x:get-key data "status")
                         (xt/x:get-key data "error_code")])))))
  => ["rejected" 400 "validation_failed"])

^{:refer xt.db.node.kernel-supabase/init-handlers :added "4.1"}
(fact "registers all supabase adaptor handlers on the node"
  (!.js
   (var node (substrate/node-create {}))
   (adaptor/init-handlers node)
   (and (xt/x:not-nil? (xt/x:get-key (xt/x:get-key node "handlers") "@xt.supabase/sign-up"))
        (xt/x:not-nil? (xt/x:get-key (xt/x:get-key node "handlers") "@xt.supabase/health"))
        (== 36 (xt/x:len (xtd/obj-keys (xt/x:get-key node "handlers"))))))
  => true)


^{:refer xt.db.node.kernel-supabase/supabase-signed-in-handler.live :added "4.1"}
(fact "returns true after signing up a fresh email user"
  (notify/wait-on :js
    (var email (xt/x:cat "kernel-signed-in-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (adaptor/supabase-sign-up-handler nil
                                          ["auth/supabase" {"email" email "password" "secret123"} {}]
                                          nil
                                          node)
        (promise/x:promise-then
         (fn [_]
           (session/auto-refresh-stop (substrate/get-service node "auth/supabase"))
           (repl/notify (adaptor/supabase-signed-in-handler nil ["auth/supabase"] nil node))))))
  => true)

^{:refer xt.db.node.kernel-supabase/supabase-rpc-call-handler.live :added "4.1"}
(fact "calls log_append_public and returns the created Log row"
  (notify/wait-on :js
    (-> (adaptor/supabase-rpc-call-handler nil
                                            ["auth/supabase"
                                             "log_append_public"
                                             {"i_message" "kernel-rpc-log"}
                                             {"headers" {"Content-Profile" "scratch_v0"}}]
                                            nil
                                            (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"message" (. out ["message"])
                         "has-id" (xt/x:not-nil? (. out ["id"]))})))))
  => {"message" "kernel-rpc-log"
      "has-id" true})

^{:refer xt.db.node.kernel-supabase/supabase-query-table-handler.live :added "4.1"}
(fact "queries the Log table via the kernel handler"
  (notify/wait-on :js
    (-> (adaptor/supabase-query-table-handler nil
                                              ["auth/supabase"
                                               "Log"
                                               {}
                                               {"headers" {"Accept-Profile" "scratch_v0"}}]
                                              nil
                                              (-/node-with-service (-/service-client) nil))
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"has-data" (xt/x:is-array? out)})))))
  => {"has-data" true})