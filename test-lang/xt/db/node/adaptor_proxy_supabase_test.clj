(ns xt.db.node.adaptor-proxy-supabase-test
  "Mirror of `xt.db.node.adaptor-supabase-test` where every call is made
   through the client-side proxy handlers in `xt.db.node.adaptor-proxy-supabase`.

   A SharedWorker hosts the server-side `xt.db.node.adaptor-supabase` handlers
   and connects to the local Supabase stack. The browser client (running under
   the `:chromedriver.instance` runtime) installs the proxy handlers, attaches
   the SharedWorker transport, and forwards the same substrate function ids to
   the worker."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-data :as xtd]
            [clojure.string :as string]
            [scaffold.supabase.local-min :as local-min]
            [js.net.http-fetch]
            [xt.net.addon-supabase]
            [xt.db.system.main]
            [xt.db.system.impl-supabase-session]
            [xt.db.node.adaptor-supabase]
            [js.worker.link :as worker-link]))

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
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.adaptor-proxy-supabase :as proxy]
             [js.worker.link :as worker-link]]})

(def +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(def +worker-script+
  "Full SharedWorker bootstrap that hosts the server-side supabase adaptor.
   It registers both an anon impl (service id `auth/supabase`) and a service
   impl (service id `auth/supabase-service`) so the proxy tests can exercise
   public and admin endpoints through the same worker.

   Global `error` and `unhandledrejection` listeners are installed so that
   runtime failures inside the worker are surfaced to the page instead of
   silently dropping responses."
  (l/emit-script
   '(do
      (var shared (. globalThis ["__adaptor_proxy_supabase_worker__"]))
      (if (xt.lang.spec-base/x:nil? shared)
        (do
          (:= shared {})
          (xt.lang.spec-base/x:set-key shared "ports" [])

          (var node (xt.substrate/node-create {"id" "adaptor-proxy-supabase-worker"}))
          (xt.db.node.adaptor-supabase/init-handlers node)

          (var anon-client
               (js.net.http-fetch/create
                {"host" (@! (-> local-min/+config+ :api :hostname))
                 "port" (@! (-> local-min/+config+ :api :port))
                 "secured" false
                 "apikey" (@! (-> local-min/+config+ :api :anon-key))}
                (xt.net.addon-supabase/middleware-supabase)))
          (var anon-impl
               (xt.db.system.main/create-impl
                "supabase"
                (xt.lang.spec-base/x:get-key anon-client "defaults")
                nil
                nil))
          (xt.db.system.impl-supabase-session/set-session anon-impl nil)
          (xt.substrate/set-service node "auth/supabase" anon-impl)

          (var service-client
               (js.net.http-fetch/create
                {"host" (@! (-> local-min/+config+ :api :hostname))
                 "port" (@! (-> local-min/+config+ :api :port))
                 "secured" false
                 "apikey" (@! (-> local-min/+config+ :api :service-key))}
                (xt.net.addon-supabase/middleware-supabase)))
          (var service-impl
               (xt.db.system.main/create-impl
                "supabase"
                (xt.lang.spec-base/x:get-key service-client "defaults")
                nil
                nil))
          (xt.db.system.impl-supabase-session/set-session service-impl nil)
          (xt.substrate/set-service node "auth/supabase-service" service-impl)

          (xt.lang.spec-base/x:set-key shared "node" node)
          (xt.lang.spec-base/x:set-key shared "counter" 0)
          (xt.lang.spec-base/x:set-key globalThis "__adaptor_proxy_supabase_worker__" shared)

          ;; surface runtime errors to every connected port
          (. globalThis (addEventListener
                         "error"
                         (fn [e]
                           (var ports (. shared ["ports"]))
                           (var payload {"__debug__" true
                                         "type" "error"
                                         "message" (. e ["message"])
                                         "stack" (:? (. e ["error"])
                                                   (. e ["error"] ["stack"])
                                                   nil)})
                           (xt.lang.spec-base/for:array [port ports]
                             (try
                               (. port (postMessage payload))
                               (catch err nil))))
                         false))
          (. globalThis (addEventListener
                         "unhandledrejection"
                         (fn [e]
                           (var ports (. shared ["ports"]))
                           (var reason (. e ["reason"]))
                           (var payload {"__debug__" true
                                         "type" "unhandledrejection"
                                         "message" (or (:? (xt.lang.spec-base/x:is-object? reason)
                                                           (. reason ["message"])
                                                           nil)
                                                       (xt.lang.spec-base/x:to-string reason))
                                         "stack" (:? (xt.lang.spec-base/x:is-object? reason)
                                                    (. reason ["stack"])
                                                    nil)})
                           (xt.lang.spec-base/for:array [port ports]
                             (try
                               (. port (postMessage payload))
                               (catch err nil))))
                         false))))

      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (xt.lang.spec-base/x:arr-push (. shared ["ports"]) port)
            (var idx (+ 1 (. shared ["counter"])))
            (xt.lang.spec-base/x:set-key shared "counter" idx)
            (return
             (xt.substrate.transport-browser/boot-self
              (. shared ["node"])
              {"transport_id" (xt.lang.spec-base/x:cat "host-" idx)
               "target" port
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "adaptor-proxy-supabase"}})))))
   {:lang :js
    :layout :full
    :emit {:native {:suppress true}}}))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop :js)]})

(defn.js connect-worker
  "Creates a fresh SharedWorker from the bootstrap script, connects a browser
   node to it, installs the proxy handlers, and sets the default transport so
   that subsequent proxy calls are forwarded to the worker.

   Any worker debug/error payloads are stored in `globalThis.__worker_debug__`
   so tests can inspect them if a handler hangs."
  []
  (var url (worker-link/make-blob-url (@! +worker-script+)))
  (var client (substrate/node-create {"id" (xt/x:cat "adaptor-proxy-supabase-client-" (xt/x:now-ms))}))
  (proxy/init-proxy-handlers client)
  (return
   (promise/x:promise-then
    (browser-transport/connect-sharedworker
     client
     {"transport_id" "worker"
      "source" {"create_fn"
                (fn [listener]
                  (var shared (new SharedWorker url))
                  (. shared (addEventListener
                             "error"
                             (fn [err]
                               (:= (. globalThis ["__worker_debug__"])
                                   {"type" "sharedworker-error"
                                    "message" (. err ["message"])}))
                             false))
                  (var port (. shared ["port"]))
                  (. port (start))
                  (. port (addEventListener
                           "message"
                           (fn [e]
                             (var data (. e ["data"]))
                             (when (. data ["__debug__"])
                               (:= (. globalThis ["__worker_debug__"]) data))
                             (listener data))
                           false))
                  (return port))}
       "wait_ready" true})
    (fn [conn]
      (proxy/set-default-transport client (. conn ["transport_id"]))
      (return client)))))

^{:refer xt.db.node.adaptor-proxy-supabase-test/sharedworker-smoke :added "4.1"}
(fact "sharedworker smoke"
  (notify/wait-on [:js 30000]
    (var url (worker-link/make-blob-url (@! +worker-script+)))
    (-> (browser-transport/connect-sharedworker
         (substrate/node-create {"id" "smoke-client"})
         {"transport_id" "smoke"
          "source" {"create_fn"
                    (fn [listener]
                      (var shared (new SharedWorker url))
                      (var port (. shared ["port"]))
                      (. port (start))
                      (. port (addEventListener
                               "message"
                               (fn [e]
                                 (listener (. e ["data"])))
                               false))
                      (return port))}
          "wait_ready" true})
        (promise/x:promise-then
         (fn [conn]
           (repl/notify conn)))))
  => (contains-in {"transport_id" "smoke"}))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/set-default-transport :added "4.1"}
(fact "sets and returns the default server transport id"
  (!.js
   (var client (substrate/node-create {"id" "test-set-default"}))
   (proxy/init-proxy-handlers client)
   (proxy/set-default-transport client "worker"))
  => "worker")

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/get-default-transport :added "4.1"}
(fact "gets the default server transport id"
  (!.js
   (var client (substrate/node-create {"id" "test-get-default"}))
   (proxy/init-proxy-handlers client)
   (proxy/set-default-transport client "worker")
   (proxy/get-default-transport client))
  => "worker")

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/get-transport-id :added "4.1"}
(fact "resolves the transport id from opts or the node default"
  (!.js
   (var client (substrate/node-create {"id" "test-transport-id"}))
   (proxy/init-proxy-handlers client)
   (proxy/set-default-transport client "worker")
   [(proxy/get-transport-id client {})
    (proxy/get-transport-id client {"transport_id" "explicit"})])
  => ["worker" "explicit"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/request-meta :added "4.1"}
(fact "builds request meta with an explicit transport_id"
  (!.js
   (var client (substrate/node-create {"id" "test-request-meta"}))
   (proxy/init-proxy-handlers client)
   (proxy/set-default-transport client "worker")
   (proxy/request-meta client {}))
  => {"transport_id" "worker"})
^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-sign-up-handler :added "4.1"}
(fact "proxies sign-up to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (return
            (proxy/supabase-sign-up-handler nil
                                            ["auth/supabase" {"email" email "password" "secret123"} {}]
                                            nil
                                            client))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"access_token" string? "refresh_token" string? "user" {"email" string?}}))
^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-sign-in-handler :added "4.1"}
(fact "proxies sign-in to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (var email email)
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [_]
                  (proxy/supabase-sign-in-handler nil
                                                  ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                  nil
                                                  client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify out))))))))
  => (contains-in {"access_token" string? "refresh_token" string? "user" {"email" string?}}))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-sign-out-handler :added "4.1"}
(fact "proxies sign-out to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [_]
                  (proxy/supabase-sign-out-handler nil ["auth/supabase" {}] nil client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify out))))))))
  => {"status" "ok"})

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-refresh-handler :added "4.1"}
(fact "proxies session refresh to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [_]
                  (proxy/supabase-refresh-handler nil ["auth/supabase"] nil client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify out))))))))
  => (contains-in {"access_token" string? "refresh_token" string? "user" {"email" string?}}))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-signed-in-handler :added "4.1"}
(fact "proxies signed-in? query to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-signed-in-handler nil ["auth/supabase"] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => false)

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-current-session-handler :added "4.1"}
(fact "proxies current-session query to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-current-session-handler nil ["auth/supabase"] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => nil)

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-rpc-call-handler :added "4.1"}
(fact "proxies an rpc call to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-rpc-call-handler nil
                                            ["auth/supabase"
                                             "ping"
                                             {}
                                             {"headers" {"Accept-Profile" "scratch_v0"
                                                         "Content-Profile" "scratch_v0"}}]
                                            nil
                                            client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-query-table-handler :added "4.1"}
(fact "proxies a table query to the shared worker"
  {:setup [(scratch-v0/log-append-public "hello-proxy")]}
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-query-table-handler nil
                                               ["auth/supabase-service"
                                                "Log"
                                                "select=*"
                                                {"headers" {"Accept-Profile" "scratch_v0"}}]
                                               nil
                                               client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in [{"message" "hello-proxy", "author_id" nil, "id" string?}]))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-health-handler :added "4.1"}
(fact "proxies auth health check to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => (contains-in {"name" "GoTrue"}))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-create-user-handler :added "4.1"}
(fact "proxies admin create-user to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-admin-create-user-handler nil
                                                     ["auth/supabase-service"
                                                      {"email" email "password" "pass123456" "email_confirm" true}
                                                      {}]
                                                     nil
                                                     client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-delete-user-handler :added "4.1"}
(fact "proxies admin delete-user to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-admin-create-user-handler nil
                                                         ["auth/supabase-service"
                                                          {"email" email "password" "pass123456" "email_confirm" true}
                                                          {}]
                                                         nil
                                                         client)
               (promise/x:promise-then
                (fn [created]
                  (proxy/supabase-admin-delete-user-handler nil
                                                            ["auth/supabase-service" (. created ["id"]) {}]
                                                            nil
                                                            client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(== 0 (xt/x:len (xtd/obj-keys out)))]))))))))
  => [true])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-generate-link-handler :added "4.1"}
(fact "proxies admin generate-link to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-admin-generate-link-handler nil
                                                       ["auth/supabase-service"
                                                        {"type" "magiclink" "email" "test@example.com"}
                                                        {}]
                                                       nil
                                                       client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["action_link"])])))))
  => (contains-in [string?]))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-get-user-handler :added "4.1"}
(fact "proxies admin get-user to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-admin-create-user-handler nil
                                                         ["auth/supabase-service"
                                                          {"email" email "password" "pass123456" "email_confirm" true}
                                                          {}]
                                                         nil
                                                         client)
               (promise/x:promise-then
                (fn [created]
                  (proxy/supabase-admin-get-user-handler nil
                                                         ["auth/supabase-service" (. created ["id"]) {}]
                                                         nil
                                                         client)))
               (promise/x:promise-then
                (fn [got]
                  (repl/notify [(. got ["email"])]))))))))
  => (contains-in [string?]))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-list-users-handler :added "4.1"}
(fact "proxies admin list-users to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-admin-list-users-handler nil ["auth/supabase-service" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["aud"])])))))
  => ["authenticated"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-admin-update-user-handler :added "4.1"}
(fact "proxies admin update-user to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-admin-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-admin-create-user-handler nil
                                                         ["auth/supabase-service"
                                                          {"email" email "password" "pass123456" "email_confirm" true}
                                                          {}]
                                                         nil
                                                         client)
               (promise/x:promise-then
                (fn [created]
                  (proxy/supabase-admin-update-user-handler nil
                                                            ["auth/supabase-service"
                                                             (. created ["id"])
                                                             {"body" (xt/x:json-encode {"user_metadata" {"note" "updated-by-proxy-test"}})}]
                                                            nil
                                                            client)))
               (promise/x:promise-then
                (fn [updated]
                  (repl/notify [(xt/x:get-key (xt/x:get-key updated "user_metadata") "note")]))))))))
  => ["updated-by-proxy-test"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-authorize-handler :added "4.1"}
(fact "proxies OAuth authorize to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-authorize-handler nil
                                             ["auth/supabase" {"redirect_to" "http://localhost/callback"} {}]
                                             nil
                                             client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-callback-handler :added "4.1"}
(fact "proxies OAuth callback to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-callback-handler nil ["auth/supabase" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(xt/x:is-string? out)
                         (> (xt/x:len out) 0)])))))
  => [true true])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-invite-handler :added "4.1"}
(fact "proxies invite to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-invite-handler nil
                                          ["auth/supabase-service" {"email" "test@example.com"} {}]
                                          nil
                                          client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["email"])])))))
  => (contains-in [string?]))

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-otp-handler :added "4.1"}
(fact "proxies OTP request to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-otp-handler nil
                                       ["auth/supabase"
                                        {"email" (xt/x:cat "adaptor-proxy-otp-"
                                                           (xt/x:to-string (xt/x:now-ms))
                                                           "@example.com")}
                                        {}]
                                       nil
                                       client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {})

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-recovery-handler :added "4.1"}
(fact "proxies recovery request to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-recovery-handler nil
                                            ["auth/supabase"
                                             {"email" (xt/x:cat "adaptor-proxy-recovery-"
                                                                (xt/x:to-string (xt/x:now-ms))
                                                                "@example.com")}
                                             {}]
                                             nil
                                             client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {})

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-settings-handler :added "4.1"}
(fact "proxies auth settings read to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-settings-handler nil ["auth/supabase" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["external"] ["email"])])))))
  => [true])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-token-refresh-handler :added "4.1"}
(fact "proxies token refresh to the shared worker"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "adaptor-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [signed-up]
                  (proxy/supabase-token-refresh-handler nil
                                                        ["auth/supabase" {"refresh_token" (. signed-up ["refresh_token"])} {}]
                                                        nil
                                                        client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify [(xt/x:not-nil? (xt/x:get-key out "access_token"))
                                (xt/x:not-nil? (xt/x:get-key out "refresh_token"))]))))))))
  => [true true])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-user-get-handler :added "4.1"}
(fact "proxies current user get to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-user-get-handler nil ["auth/supabase" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-user-info-handler :added "4.1"}
(fact "proxies current user info to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-user-info-handler nil ["auth/supabase" {}] nil client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-user-put-handler :added "4.1"}
(fact "proxies current user update to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-user-put-handler nil
                                            ["auth/supabase" {"data" {"note" "updated"}} {}]
                                            nil
                                            client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [401 "no_authorization"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-verify-get-handler :added "4.1"}
(fact "proxies verify GET to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-verify-get-handler nil
                                              ["auth/supabase" {"type" "email"} {}]
                                              nil
                                              client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/supabase-verify-post-handler :added "4.1"}
(fact "proxies verify POST to the shared worker"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (proxy/supabase-verify-post-handler nil
                                               ["auth/supabase" {"type" "email" "token" "abc123"} {}]
                                               nil
                                               client)))
        (promise/x:promise-then
         (fn [out]
           (repl/notify [(. out ["code"])
                         (. out ["error_code"])])))))
  => [400 "validation_failed"])

^{:timeout 60000 :refer xt.db.node.adaptor-proxy-supabase/init-proxy-handlers :added "4.1"}
(fact "registers all supabase proxy handlers on the node"
  (!.js
   (var node (substrate/node-create {}))
   (proxy/init-proxy-handlers node)
   (and (xt/x:not-nil? (xt/x:get-key (xt/x:get-key node "handlers") "@xt.supabase/sign-up"))
        (xt/x:not-nil? (xt/x:get-key (xt/x:get-key node "handlers") "@xt.supabase/health"))
        (== 27 (xt/x:len (xtd/obj-keys (xt/x:get-key node "handlers"))))))
  => true)

