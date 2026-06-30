(ns xt.db.node.client-supabase-test
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
             [xt.net.http-util :as http-util]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.adaptor-supabase :as adaptor]
             [xt.db.node.client-supabase :as client]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.system.main :as main]
             [xt.db.system.impl-supabase-session :as session]
             [xt.substrate :as substrate]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.net.addon-supabase :as addon]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-client
  "creates a supabase http client"
  {:added "4.1"}
  [apikey token]
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey apikey
     :token token}
    (addon/middleware-supabase))))

(defn.js service-client
  "returns a service-role supabase client"
  {:added "4.1"}
  []
  (return
   (-/default-client (@! (-> local-min/+config+ :api :service-key))
                     (@! (-> local-min/+config+ :api :service-key)))))

(defn.js anon-client
  "returns an anonymous supabase client"
  {:added "4.1"}
  []
  (return
   (-/default-client (@! (-> local-min/+config+ :api :anon-key)) nil)))

(defn.js node-with-service
  "creates a node with a supabase service and the adaptor handlers installed"
  {:added "4.1"}
  [client session]
  (var node (substrate/node-create {}))
  (var impl (main/create-impl "supabase"
                              (xt/x:get-key client "defaults")
                              nil
                              nil))
  (session/set-session impl session)
  (substrate/set-service node "auth/supabase" impl)
  (adaptor/init-handlers node)
  (return node))

(defn.js make-bare-node
  "creates a bare node for use as a proxy client"
  {:added "4.1"}
  [id]
  (return (substrate/node-create {"id" id
                                  "spaces" {"room/a" {"state" {}}}})))

(defn.js link-nodes
  "links two nodes with an in-memory transport wire"
  {:added "4.1"}
  [server client]
  (var wire (transport-memory/memory-pair {"left_id" "client"
                                           "right_id" "server"}))
  (return
   (promise/x:promise-all
    [(substrate/attach-transport
      client
      "server"
      (transport-memory/text-endpoint (. wire ["left"])))
     (substrate/attach-transport
      server
      "client"
      (transport-memory/text-endpoint (. wire ["right"])))])))

^{:refer xt.db.node.client-supabase/sign-up :added "4.1"}
(fact "signs up, stores the session and starts auto-refresh"

  (notify/wait-on :js
    (var email (xt/x:cat "client-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (client/sign-up node "auth/supabase" {"email" email "password" "secret123"} {})
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

^{:refer xt.db.node.client-supabase/sign-in :added "4.1"}
(fact "signs in, stores the session and starts auto-refresh"

  (notify/wait-on :js
    (var email (xt/x:cat "client-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/anon-client))
    (var node (-/node-with-service client nil))
    (-> (http-fetch/request-http client (addon/cmd-signup {"email" email "password" "secret123"} {}))
        (promise/x:promise-then
         (fn [_]
           (-> (client/sign-in node "auth/supabase" {"email" email "password" "secret123"} {})
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

^{:refer xt.db.node.client-supabase/sign-out :added "4.1"}
(fact "signs out and clears the stored session"

  (notify/wait-on :js
    (var email (xt/x:cat "client-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var anon (-/anon-client))
    (var node (-/node-with-service anon nil))
    (-> (client/sign-up node "auth/supabase" {"email" email "password" "secret123"} {})
        (promise/x:promise-then
         (fn [session]
           (session/auto-refresh-stop (substrate/get-service node "auth/supabase"))
           (var token-client (-/default-client (@! (-> local-min/+config+ :api :anon-key))
                                               (. session ["access_token"])))
           (var token-node (-/node-with-service token-client session))
           (session/auto-refresh-start (substrate/get-service token-node "auth/supabase") {"interval" 1000})
           (-> (client/sign-out token-node "auth/supabase" {})
               (promise/x:promise-then
                (fn [out]
                  (var impl (substrate/get-service token-node "auth/supabase"))
                  (var new-session (session/get-session impl))
                  (var timer (xt/x:get-key (xt/x:get-key (xt/x:get-key impl "state") "auto_refresh") "current"))
                  (repl/notify [out new-session timer]))))))))
  => [{"status" "ok"} nil nil])

^{:refer xt.db.node.client-supabase/refresh :added "4.1"}
(fact "refreshes the stored session"

  (notify/wait-on :js
    (var email (xt/x:cat "client-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (client/sign-up node "auth/supabase" {"email" email "password" "secret123"} {})
        (promise/x:promise-then
         (fn [_]
           (-> (client/refresh node "auth/supabase" {})
               (promise/x:promise-then
                (fn [out]
                  (var impl (substrate/get-service node "auth/supabase"))
                  (var session (session/get-session impl))
                  (session/auto-refresh-stop impl)
                  (repl/notify [out session]))))))))
  => (contains-in [nil
                {"access_token" string? "refresh_token" string? "user" {"email" string?}}]))

^{:refer xt.db.node.client-supabase/signed-in? :added "4.1"}
(fact "returns true when the service has a session"

  (notify/wait-on :js
    (var node (-/node-with-service (-/anon-client) {"access_token" "abc"}))
    (-> (client/signed-in? node "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => true)

^{:refer xt.db.node.client-supabase/signed-in? :added "4.1"}
(fact "returns false when the service has no session"

  (notify/wait-on :js
    (var node (-/node-with-service (-/anon-client) nil))
    (-> (client/signed-in? node "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => false)

^{:refer xt.db.node.client-supabase/current-session :added "4.1"}
(fact "returns the session stored on the service"

  (notify/wait-on :js
    (var node (-/node-with-service (-/anon-client) {"access_token" "abc"}))
    (-> (client/current-session node "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"access_token" "abc"})

^{:refer xt.db.node.client-supabase/rpc-call :added "4.1"}
(fact "calls an rpc entry on the service"

  (notify/wait-on :js
    (-> (client/rpc-call (-/node-with-service (-/anon-client) nil)
                         "auth/supabase"
                         "ping"
                         {}
                         {"headers" {"Accept-Profile" "scratch_v0"
                                     "Content-Profile" "scratch_v0"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.node.client-supabase/query-table :added "4.1"}
(fact "queries a table on the service"
  {:setup [(scratch-v0/log-append-public "hello")]}

  (notify/wait-on :js
    (-> (client/query-table (-/node-with-service (-/service-client) nil)
                            "auth/supabase"
                            "Log"
                            "select=*"
                            {"headers" {"Accept-Profile" "scratch_v0"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in [{"message" "hello", "author_id" nil, "id" string?}]))

^{:refer xt.db.node.client-supabase/health :added "4.1"}
(fact "calls the auth health endpoint on the service"

  (notify/wait-on :js
    (-> (client/health (-/node-with-service (-/anon-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "GoTrue"}))

^{:refer xt.db.node.client-supabase/admin-create-user :added "4.1"}
(fact "creates a user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "client-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (client/admin-create-user (-/node-with-service (-/service-client) nil)
                                  "auth/supabase"
                                  {"email" email "password" "pass123456" "email_confirm" true}
                                  {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.client-supabase/admin-delete-user :added "4.1"}
(fact "deletes a user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "client-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (client/admin-create-user node
                                  "auth/supabase"
                                  {"email" email "password" "pass123456" "email_confirm" true}
                                  {})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (client/admin-delete-user node "auth/supabase" user-id {})
               (promise/x:promise-then
                (fn [deleted]
                  (repl/notify [(. created ["email"])
                                (== 0 (xt/x:len (xtd/obj-keys deleted)))]))))))))
  => (contains-in [string? true]))

^{:refer xt.db.node.client-supabase/admin-generate-link :added "4.1"}
(fact "generates an admin link on the service"

  (notify/wait-on :js
    (-> (client/admin-generate-link (-/node-with-service (-/service-client) nil)
                                    "auth/supabase"
                                    {"type" "magiclink" "email" "test@example.com"}
                                    {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["action_link"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.client-supabase/admin-get-user :added "4.1"}
(fact "fetches a user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "client-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (client/admin-create-user node
                                  "auth/supabase"
                                  {"email" email "password" "pass123456" "email_confirm" true}
                                  {})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (client/admin-get-user node "auth/supabase" user-id {})
               (promise/x:promise-then
                (fn [got]
                  (-> (client/admin-delete-user node "auth/supabase" user-id {})
                      (promise/x:promise-then
                       (fn [_]
                         (repl/notify [(. got ["email"])])))))))))))
  => (contains-in [string?]))

^{:refer xt.db.node.client-supabase/admin-list-users :added "4.1"}
(fact "lists users through the admin endpoint"

  (notify/wait-on :js
    (-> (client/admin-list-users (-/node-with-service (-/service-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["aud"])])))))
  => ["authenticated"])

^{:refer xt.db.node.client-supabase/admin-update-user :added "4.1"}
(fact "updates a user through the admin endpoint"

  (notify/wait-on :js
    (var email (xt/x:cat "client-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var node (-/node-with-service (-/service-client) nil))
    (-> (client/admin-create-user node
                                  "auth/supabase"
                                  {"email" email "password" "pass123456" "email_confirm" true}
                                  {})
        (promise/x:promise-then
         (fn [created]
           (var user-id (. created ["id"]))
           (-> (client/admin-update-user node
                                         "auth/supabase"
                                         user-id
                                         {"body" (xt/x:json-encode {"user_metadata" {"note" "updated-by-test"}})})
               (promise/x:promise-then
                (fn [updated]
                  (repl/notify [(xt/x:get-key (xt/x:get-key updated "user_metadata") "note")]))))))))
  => ["updated-by-test"])


^{:refer xt.db.node.client-supabase/authorize :added "4.1"}
(fact "starts an OAuth authorization request"

  (notify/wait-on :js
    (-> (client/authorize (-/node-with-service (-/anon-client) nil)
                          "auth/supabase"
                          {"redirect_to" "http://localhost/callback"}
                          {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.db.node.client-supabase/callback :added "4.1"}
(fact "handles an OAuth callback request"

  (notify/wait-on :js
    (-> (client/callback (-/node-with-service (-/anon-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify {"has_response" (xt/x:not-nil? out)})))))
  => {"has_response" true})




^{:refer xt.db.node.client-supabase/invite :added "4.1"}
(fact "sends an invite on the service"

  (notify/wait-on :js
    (-> (client/invite (-/node-with-service (-/service-client) nil)
                       "auth/supabase"
                       {"email" "test@example.com"}
                       {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:refer xt.db.node.client-supabase/otp :added "4.1"}
(fact "requests a passwordless OTP on the service"

  (notify/wait-on :js
    (-> (client/otp (-/node-with-service (-/anon-client) nil)
                    "auth/supabase"
                    {"email" (xt/x:cat "client-otp-"
                                       (xt/x:to-string (xt/x:now-ms))
                                       "@example.com")}
                    {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {})

^{:refer xt.db.node.client-supabase/recovery :added "4.1"}
(fact "requests a recovery email on the service"

  (notify/wait-on :js
    (-> (client/recovery (-/node-with-service (-/anon-client) nil)
                         "auth/supabase"
                         {"email" (xt/x:cat "client-recovery-"
                                            (xt/x:to-string (xt/x:now-ms))
                                            "@example.com")}
                         {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {})

^{:refer xt.db.node.client-supabase/settings :added "4.1"}
(fact "reads auth settings on the service"

  (notify/wait-on :js
    (-> (client/settings (-/node-with-service (-/anon-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["external"] ["email"])])))))
  => [true])

^{:refer xt.db.node.client-supabase/token-refresh :added "4.1"}
(fact "refreshes a session with a refresh token on the service"

  (notify/wait-on :js
    (var email (xt/x:cat "client-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var client (-/anon-client))
    (var node (-/node-with-service client nil))
    (-> (http-fetch/request-http client (addon/cmd-signup {"email" email "password" "secret123"} {}))
        (promise/x:promise-then
         (fn [signed-up]
           (var refresh-token (. (. signed-up ["body"]) ["refresh_token"]))
           (-> (client/token-refresh node
                                     "auth/supabase"
                                     {"refresh_token" refresh-token}
                                     {})
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(xt/x:not-nil? (xt/x:get-key out "access_token"))
                                (xt/x:not-nil? (xt/x:get-key out "refresh_token"))]))))))))
  => [true true])

^{:refer xt.db.node.client-supabase/user-get :added "4.1"}
(fact "fetches the current authenticated user on the service"

  (notify/wait-on :js
    (-> (client/user-get (-/node-with-service (-/anon-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.db.node.client-supabase/user-info :added "4.1"}
(fact "user-info is an alias for user-get"

  (notify/wait-on :js
    (-> (client/user-info (-/node-with-service (-/anon-client) nil) "auth/supabase" {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.db.node.client-supabase/user-put :added "4.1"}
(fact "updates the current authenticated user on the service"

  (notify/wait-on :js
    (-> (client/user-put (-/node-with-service (-/anon-client) nil)
                         "auth/supabase"
                         {"data" {"note" "updated"}}
                         {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:refer xt.db.node.client-supabase/verify-get :added "4.1"}
(fact "verifies a token via GET on the service"

  (notify/wait-on :js
    (-> (client/verify-get (-/node-with-service (-/anon-client) nil)
                           "auth/supabase"
                           {"type" "email"}
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.db.node.client-supabase/verify-post :added "4.1"}
(fact "verifies a token via POST on the service"

  (notify/wait-on :js
    (-> (client/verify-post (-/node-with-service (-/anon-client) nil)
                            "auth/supabase"
                            {"type" "email" "token" "abc123"}
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:refer xt.db.node.client-supabase/sign-up :added "4.1"}
(fact "forwards sign-up through a proxy-supabase node"

  (notify/wait-on :js
    (var email (xt/x:cat "client-proxy-supabase-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var server (-/node-with-service (-/anon-client) nil))
    (var client (-/make-bare-node "client"))
    (proxy-supabase/init-proxy-handlers client)
    (-> (-/link-nodes server client)
        (promise/x:promise-then
         (fn [_]
           (proxy-util/set-default-transport client "server")
           (return (client/sign-up client
                                   "auth/supabase"
                                   {"email" email "password" "secret123"}
                                   {}))))
        (promise/x:promise-then
         (fn [out]
           (var impl (substrate/get-service server "auth/supabase"))
           (session/auto-refresh-stop impl)
           (repl/notify [(. out ["access_token"])
                         (. out ["refresh_token"])
                         (xt/x:not-nil? (session/get-session impl))])))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in [string? string? true]))
