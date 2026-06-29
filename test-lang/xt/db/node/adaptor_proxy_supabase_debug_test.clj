(ns xt.db.node.adaptor-proxy-supabase-debug-test
  "Isolated debug test for SharedWorker proxy handler timeouts."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [xt.lang.common-data :as xtd]
            [xt.lang.common-repl :as repl]
            [xt.lang.spec-base :as xt]
            [xt.lang.spec-promise :as promise]
            [xt.substrate :as substrate]
            [xt.substrate.transport-browser :as browser-transport]
            [xt.db.node.adaptor-proxy-supabase :as proxy]
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
  "Debug SharedWorker bootstrap with global error capture.
   Errors and unhandled rejections are posted back to every connected port
   as a plain debug payload."
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

(defn.js connect-worker-debug
  "Connects a client to the debug worker and stores any worker debug messages
   in `globalThis.__worker_debug__`."
  []
  (var url (worker-link/make-blob-url (@! +worker-script+)))
  (var client (substrate/node-create {"id" (xt/x:cat "debug-client-" (xt/x:now-ms))}))
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
                             (listener (. e ["data"])))
                           false))
                  (return port))}
       "wait_ready" true})
    (fn [conn]
      (proxy/set-default-transport client (. conn ["transport_id"]))
      (return client)))))

(defn.js poll-worker-debug
  "Polls `globalThis.__worker_debug__` and calls `cb` with the payload when
   one appears. Returns the interval id so the caller can clear it."
  [cb]
  (var interval nil)
  (:= interval
      (. globalThis (setInterval
                     (fn []
                       (var debug (. globalThis ["__worker_debug__"]))
                       (when debug
                         (. globalThis (clearInterval interval))
                         (:= (. globalThis ["__worker_debug__"]) nil)
                         (cb debug)))
                     100)))
  (return interval))

(def +worker-script-original+
  "Exact copy of the original SharedWorker bootstrap for comparison."
  (l/emit-script
   '(do
      (var shared (. globalThis ["__adaptor_proxy_supabase_worker__"]))
      (if (xt.lang.spec-base/x:nil? shared)
        (do
          (:= shared {})
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
          (xt.lang.spec-base/x:set-key globalThis "__adaptor_proxy_supabase_worker__" shared)))

      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
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

(defn.js connect-worker-original
  "Exact copy of the original connect-worker for comparison."
  []
  (var url (worker-link/make-blob-url (@! +worker-script-original+)))
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
                               (listener {"signal" "worker-error"
                                          "message" (. err ["message"])}))
                             false))
                  (var port (. shared ["port"]))
                  (. port (start))
                  (. port (addEventListener
                           "message"
                           (fn [e]
                             (listener (. e ["data"])))
                           false))
                  (return port))}
       "wait_ready" true})
    (fn [conn]
      (proxy/set-default-transport client (. conn ["transport_id"]))
      (return client)))))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/health-original
  :added "4.1"}
(fact "health handler with original worker script"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/sign-in-original
  :added "4.1"}
(fact "sign-in handler with original worker script"
  (notify/wait-on [:js 30000]
    (var email (xt/x:cat "orig-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
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
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/health-original-again
  :added "4.1"}
(fact "health handler with original worker script (second call)"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/settings-original
  :added "4.1"}
(fact "settings handler with original worker script"
  (notify/wait-on [:js 30000]
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-settings-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/worker-sharing-check
  :added "4.1"}
(fact "blob urls do not share SharedWorker instances"
  (notify/wait-on [:js 30000]
    (var url1 (worker-link/make-blob-url (@! +worker-script-original+)))
    (var url2 (worker-link/make-blob-url (@! +worker-script-original+)))
    (var make-client
         (fn [url]
           (var client (substrate/node-create {"id" (xt/x:cat "share-check-" (xt/x:now-ms))}))
           (proxy/init-proxy-handlers client)
           (return
            (promise/x:promise-then
             (browser-transport/connect-sharedworker
              client
              {"transport_id" "worker"
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
             (fn [conn]
               (proxy/set-default-transport client (. conn ["transport_id"]))
               (return client))))))
    (-> (make-client url1)
        (promise/x:promise-then
         (fn [client1]
           (-> (make-client url2)
               (promise/x:promise-then
                (fn [client2]
                  (repl/notify {"host-1" (proxy/get-default-transport client1)
                                "host-2" (proxy/get-default-transport client2)}))))))))
  => {"host-1" string? "host-2" string?})

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/health-debug
  :added "4.1"}
(fact "debug health handler through shared worker"
  (notify/wait-on [:js 20000]
    (var interval (-/poll-worker-debug (fn [debug]
                                          (repl/notify {"stage" "worker-debug" "debug" debug}))))
    (-> (-/connect-worker-debug)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (. globalThis (clearInterval interval))
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (. globalThis (clearInterval interval))
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (. globalThis (clearInterval interval))
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/sign-in-debug
  :added "4.1"}
(fact "debug sign-in handler through shared worker"
  (notify/wait-on [:js 30000]
    (var interval (-/poll-worker-debug (fn [debug]
                                          (repl/notify {"stage" "worker-debug" "debug" debug}))))
    (var email (xt/x:cat "debug-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (-> (-/connect-worker-debug)
        (promise/x:promise-then
         (fn [client]
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
                  (. globalThis (clearInterval interval))
                  (repl/notify {"stage" "ok" "out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (. globalThis (clearInterval interval))
                  (repl/notify {"stage" "error" "err" err}))))))
        (promise/x:promise-catch
         (fn [err]
           (. globalThis (clearInterval interval))
           (repl/notify {"stage" "connect-error" "err" err})))))
  => (contains-in {"stage" "ok"}))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/health-stress
  :added "4.1"}
(fact "health handler stress with fresh workers"
  (notify/wait-on [:js 120000]
    (var results [])
    (var run
         (fn [n]
           (if (<= n 0)
             (return (repl/notify results))
             (-> (-/connect-worker-original)
                 (promise/x:promise-then
                  (fn [client]
                    (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
                        (promise/x:promise-then
                         (fn [out]
                           (xt.lang.spec-base/x:arr-push results out)
                           (return (run (- n 1)))))
                        (promise/x:promise-catch
                         (fn [err]
                           (xt.lang.spec-base/x:arr-push results {"error" err})
                           (return (run (- n 1))))))))))))
    (run 10))
  => (contains-in [{"name" "GoTrue"} {"name" "GoTrue"} {"name" "GoTrue"}
                   {"name" "GoTrue"} {"name" "GoTrue"} {"name" "GoTrue"}
                   {"name" "GoTrue"} {"name" "GoTrue"} {"name" "GoTrue"}
                   {"name" "GoTrue"}]))

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/mixed-sequence-single
  :added "4.1"}
(fact "sequence of handlers on a single worker"
  (notify/wait-on [:js 120000]
    (var email (xt/x:cat "mixed-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var results {})
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [health]
                  (xt.lang.spec-base/x:set-key results "health" health)
                  (-> (proxy/supabase-sign-up-handler nil
                                                      ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                      nil
                                                      client)
                      (promise/x:promise-then
                       (fn [signed-up]
                         (xt.lang.spec-base/x:set-key results "sign-up" signed-up)
                         (-> (proxy/supabase-sign-in-handler nil
                                                             ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                             nil
                                                             client)
                             (promise/x:promise-then
                              (fn [signed-in]
                                (xt.lang.spec-base/x:set-key results "sign-in" signed-in)
                                (-> (proxy/supabase-sign-out-handler nil ["auth/supabase" {}] nil client)
                                    (promise/x:promise-then
                                     (fn [signed-out]
                                       (xt.lang.spec-base/x:set-key results "sign-out" signed-out)
                                       (return (repl/notify results))))))))))))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err})))))
  => map?)

^{:refer xt.db.node.adaptor-proxy-supabase-debug-test/sign-up-first-sequence
  :added "4.1"}
(fact "sequence starting with sign-up on a single worker"
  (notify/wait-on [:js 120000]
    (var email (xt/x:cat "upfirst-proxy-"
                         (xt/x:to-string (xt/x:now-ms))
                         "@example.com"))
    (var results {})
    (-> (-/connect-worker-original)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-sign-up-handler nil
                                               ["auth/supabase" {"email" email "password" "secret123"} {}]
                                               nil
                                               client)
               (promise/x:promise-then
                (fn [signed-up]
                  (xt.lang.spec-base/x:set-key results "sign-up" signed-up)
                  (-> (proxy/supabase-sign-in-handler nil
                                                      ["auth/supabase" {"email" email "password" "secret123"} {}]
                                                      nil
                                                      client)
                      (promise/x:promise-then
                       (fn [signed-in]
                         (xt.lang.spec-base/x:set-key results "sign-in" signed-in)
                         (-> (proxy/supabase-sign-out-handler nil ["auth/supabase" {}] nil client)
                             (promise/x:promise-then
                              (fn [signed-out]
                                (xt.lang.spec-base/x:set-key results "sign-out" signed-out)
                                (return (repl/notify results))))))))))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err})))))
  => map?)
