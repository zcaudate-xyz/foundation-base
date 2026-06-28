(ns xt.db.poc.s03-shared-rpc-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]))

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
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [js.worker.link :as worker-link]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.adaptor-base :as adaptor-base]]})

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
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (var node (xt.substrate/node-create {"id" "db-model-server"}))
            (var schema xt.db.poc.s03-shared-rpc-test/Schema)
            (var lookup xt.db.poc.s03-shared-rpc-test/SchemaLookup)

            ;;
            ;;
            (xt.substrate/set-service
             node "db/primary"
             (xt.db.system.impl-supabase/impl-supabase
              (js.net.http-fetch/create
               (@! local-min/+config-supabase-anon+)
               (xt.net.addon-supabase/middleware-supabase))
              schema
              lookup))
            (xt.substrate/set-service
             node "db/caching"
             (xt.db.system.impl-memory/impl-memory schema lookup))
            (xt.substrate.page-proxy/install node)
            (xt.substrate.page-core/add-group-attach
             node
             "room/a"
             "demo"
             {"rpc-view" (xt.db.node.adaptor-base/create-rpc-model
                          "db/primary"
                          {"rpc_spec" {"input" []
                                       "return" "text"
                                       "schema" "scratch_v0"
                                       "id" "ping"
                                       "flags" {}}
                           "pipeline" {}
                           "options" {}
                           "defaults" {"fn_args" []}})})
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" port
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "db-model-server"}})))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

^{:refer xt.db.poc.s03-shared-rpc-test/attach-rpc-model
  :added "4.1"}
(fact "client can open a remote rpc model and read the rpc result"

  (notify/wait-on [:js 20000]
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (promise/x:promise-catch
     (promise/x:promise-then
      (browser-transport/connect-sharedworker
       client
       {"transport_id" "worker"
        "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
      (fn [conn]
        (var transport-id (. conn ["transport_id"]))
        (return
         (promise/x:promise-then
          (page-proxy/open-proxy-group
           client
           "room/a"
           "demo"
           {"transport_id" transport-id})
          (fn [group]
            (var model (xtd/get-in group ["models" "rpc-view"]))
            (return
             (promise/x:promise-then
              (base-page/remote-call client "room/a" "demo" "rpc-view" [[]] true)
              (fn [res]
                (return
                 (promise/x:with-delay
                  1000
                  (fn []
                    (repl/notify {"result" res
                                  "output" (event-model/get-current model nil)}))))))))))))
     (fn [err]
       (repl/notify {"error" err
                     "message" (xt/x:ex-message err)}))))
  => (contains-in
      {"output" "pong"}))
