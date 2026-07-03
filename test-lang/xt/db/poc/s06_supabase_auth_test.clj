(ns xt.db.poc.s06-supabase-auth-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
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
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.db.node.runtime :as runtime]
             [js.net.http-fetch]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {
  :setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(defn.js connect-auth-worker
  "connects to the shared worker and initialises the db adaptor on the client"
  {:added "4.1"}
  [client]
  (return
   (runtime/sharedworker-connect client
                                 {"primary" {"id" "db/primary"
                                             "type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"id" "db/caching"
                                             "type" "memory"
                                             "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)))

(defn.js with-auth-worker
  "connects a client to the shared worker and invokes callback"
  {:added "4.1"}
  [callback]
  (var client (substrate/node-create {"id" "db-auth-client"}))
  (return
   (promise/x:promise-then
    (-/connect-auth-worker client)
    (fn [_]
      (return (callback client))))))

(defn.js request-worker
  [client op args]
  (return
   (substrate/request client "room/a" op args {})))

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-signed-in-anon
  :added "4.1"}
(fact "anon service reports not signed in through the auth worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client]
       (return
        (promise/x:promise-then
         (-/request-worker client "@xt.supabase/signed-in?" ["db/primary"])
         (fn [v]
           (repl/notify {"signed-in" v})))))))
  => {"signed-in" false})

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-current-session-anon
  :added "4.1"}
(fact "anon service returns no session through the auth worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client]
       (return
        (promise/x:promise-then
         (-/request-worker client "@xt.supabase/current-session" ["db/primary"])
         (fn [v]
           (repl/notify v)))))))
  => nil)

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-sign-up-user-info
  :added "4.1"}
(fact "client can sign up through the worker and then query user info"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client]
       (var email (xt/x:cat "auth-test-"
                            (xt/x:to-string (xt/x:now-ms))
                            "@example.com"))
       (return
        (promise/x:promise-then
         (promise/x:promise-then
          (-/request-worker client "@xt.supabase/sign-up" ["db/primary"
                                                           {"email" email
                                                            "password" "pass123456"}])
          (fn [_]
            (return
             (-/request-worker client "@xt.supabase/user-info" ["db/primary"]))))
         (fn [v]
           (repl/notify v)))))))
  => (contains-in
      {"user" {"email" string?}}))

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-health
  :added "4.1"}
(fact "client can call the auth health endpoint through the worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client]
       (return
        (promise/x:promise-then
         (-/request-worker client "@xt.supabase/health" ["db/primary"])
         (fn [v]
           (repl/notify v)))))))
  => (contains-in
      {"name" "GoTrue"}))

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-reachable
  :added "4.1"
  :setup [(scratch-v0/log-append-public "hello")]}
(fact "browser page can reach supabase rest endpoint"

  (notify/wait-on [:js 20000]
    (var client (js.net.http-fetch/create
                  (@! local-min/+config-supabase-anon+)
                  (xt.net.addon-supabase/middleware-supabase)))
    (-> (js.net.http-fetch/request-http
         client
         {"path" "/rest/v1/Log"
          "method" "GET"
          "headers" {"Accept-Profile" "scratch_v0"
                     "apikey" (@! (-> local-min/+config+ :api :anon-key))}})
        (promise/x:promise-then
         (fn [res]
           (repl/notify (. res ["body"]))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify err)))))
  => (contains-in
      [{"id" string?
        "message" "hello"}]))
