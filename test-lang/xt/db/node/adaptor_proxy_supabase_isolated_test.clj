(ns xt.db.node.adaptor-proxy-supabase-isolated-test
  "Isolated reproduction of proxy handler nil issue."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
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
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.adaptor-proxy-supabase :as proxy]
             [js.worker.link :as worker-link]]})

(def +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(def +worker-script+
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

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop :js)]})

(defn.js connect-worker
  []
  (var url (worker-link/make-blob-url (@! +worker-script+)))
  (var client (substrate/node-create {"id" (xt/x:cat "iso-client-" (xt/x:now-ms))}))
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
      (return client)))))

^{:refer xt.db.node.adaptor-proxy-supabase-isolated-test/signed-in-only
  :added "4.1"}
(fact "signed-in? only"
  (notify/wait-on [:js 60000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-signed-in-handler nil ["auth/supabase"] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"error" err}))))))))
  => (contains-in {"out" false}))

^{:refer xt.db.node.adaptor-proxy-supabase-isolated-test/health-only
  :added "4.1"}
(fact "health only"
  (notify/wait-on [:js 60000]
    (-> (-/connect-worker)
        (promise/x:promise-then
         (fn [client]
           (-> (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"error" err}))))))))
  => (contains-in {"out" {"name" "GoTrue"}}))

^{:refer xt.db.node.adaptor-proxy-supabase-isolated-test/sign-up-then-health
  :added "4.1"}
(fact "sign up then health"
  (notify/wait-on [:js 60000]
    (var email (xt/x:cat "up-health-proxy-"
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
                  (proxy/supabase-health-handler nil ["auth/supabase" {}] nil client)))
               (promise/x:promise-then
                (fn [out]
                  (repl/notify {"out" out})))
               (promise/x:promise-catch
                (fn [err]
                  (repl/notify {"error" err}))))))))
  => (contains-in {"out" {"name" "GoTrue"}}))
