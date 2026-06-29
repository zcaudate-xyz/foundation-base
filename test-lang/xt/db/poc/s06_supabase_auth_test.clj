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
             [js.worker.link :as worker-link]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.adaptor-base :as adaptor-base]
             [xt.db.node.adaptor-supabase :as adaptor-supabase]
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

(def +sharedworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "db-auth-server"}))
      (xt.substrate/register-handler
       node "@xt.db/ping"
       (fn [space args request node]
         (return {"status" "pong"}))
       nil)
      (xt.db.node.adaptor-base/init-handlers node)
      (xt.substrate/register-handler
       node "@xt.db/init-adaptor"
       (fn [space args request node]
         (return
          (. (xt.db.node.adaptor-base/init-adaptor-main
              node
              (. args [0])
              (. args [1])
              (. args [2]))
             (then
              (fn [_]
                (return {"status" "ok"})))
             (catch
              (fn [err]
                (return {"status" "error"
                         "message" (. err ["message"])
                         "stack" (. err ["stack"])}))))))
       nil)
      (xt.db.node.adaptor-supabase/init-handlers node)
      (xt.substrate.page-proxy/install node)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" port
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "db-auth-server"}})))))
    {:lang :js
     :layout :full
     :emit {:override {"@sqlite.org/sqlite-wasm"
                       "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                       "pg"
                       "data:text/javascript,export default {Client: function() {}}"}}}))

(defn.js with-auth-worker
  [callback]
  (var client (substrate/node-create {"id" "db-auth-client"}))
  (page-proxy/install client)
  (return
   (promise/x:promise-then
    (browser-transport/connect-sharedworker
     client
     {"transport_id" "worker"
      "source" (worker-link/make-sharedworker-link-opts (@! +sharedworker-script+) {"type" "module"})})
    (fn [conn]
      (var transport-id (. conn ["transport_id"]))
      (return
       (promise/x:promise-then
        (substrate/request client
                           "room/a"
                           "@xt.db/init-adaptor"
                           [{"primary" {"id" "db/primary"
                                        "type" "supabase"
                                        "defaults" (@! local-min/+config-supabase-anon+)}}
                            xt.db.poc.s06-supabase-auth-test/Schema
                            xt.db.poc.s06-supabase-auth-test/SchemaLookup]
                           {"transport_id" transport-id})
        (fn [_]
          (return (callback client transport-id)))))))))

(defn.js request-worker
  [client transport-id op args]
  (return
   (substrate/request client "room/a" op args {"transport_id" transport-id})))

^{:refer xt.db.poc.s06-supabase-auth-test/auth-worker-ping
  :added "4.1"}
(fact "client can reach a simple ping handler on the auth shared worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client transport-id]
       (return
        (promise/x:promise-then
         (-/request-worker client transport-id "@xt.db/ping" [])
         (fn [v]
           (repl/notify v)))))))
  => {"status" "pong"})

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-signed-in-anon
  :added "4.1"}
(fact "anon service reports not signed in through the auth worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client transport-id]
       (return
        (promise/x:promise-then
         (-/request-worker client transport-id "@xt.supabase/signed-in?" ["db/primary"])
         (fn [v]
           (repl/notify {"signed-in" v})))))))
  => {"signed-in" false})

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-current-session-anon
  :added "4.1"}
(fact "anon service returns no session through the auth worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client transport-id]
       (return
        (promise/x:promise-then
         (-/request-worker client transport-id "@xt.supabase/current-session" ["db/primary"])
         (fn [v]
           (repl/notify v)))))))
  => nil)

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-sign-up-user-info
  :added "4.1"}
(fact "client can sign up through the worker and then query user info"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client transport-id]
       (var email (xt/x:cat "auth-test-"
                            (xt/x:to-string (xt/x:now-ms))
                            "@example.com"))
       (return
        (promise/x:promise-then
         (promise/x:promise-then
          (-/request-worker client transport-id "@xt.supabase/sign-up" ["db/primary"
                                                                          {"email" email
                                                                           "password" "pass123456"}])
          (fn [_]
            (return
             (-/request-worker client transport-id "@xt.supabase/user-info" ["db/primary"]))))
         (fn [v]
           (repl/notify v)))))))
  => (contains-in
      {"user" {"email" string?}}))

^{:refer xt.db.poc.s06-supabase-auth-test/supabase-health
  :added "4.1"}
(fact "client can call the auth health endpoint through the worker"

  (notify/wait-on [:js 20000]
    (-/with-auth-worker
     (fn [client transport-id]
       (return
        (promise/x:promise-then
         (-/request-worker client transport-id "@xt.supabase/health" ["db/primary"])
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
