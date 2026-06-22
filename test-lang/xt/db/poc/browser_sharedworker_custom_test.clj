(ns xt.db.poc.browser-sharedworker-custom-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [xt.substrate]
            [xt.substrate.transport-browser]
            [xt.db.node.adaptor-base]))

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
             [js.worker.link :as worker-link]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {:setup [(l/rt:restart :js)
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
            (. port (postMessage {"type" "worker-connected"}))
            (. (xt.db.node.adaptor-base/init-db
                (xt.substrate/node-create {"id" "db-model-server"})
                {"primary" {"type" "supabase"
                            "defaults" (@! local-min/+config-supabase-anon+)}
                 "caching" {"type" "sqlite"
                            "defaults" {}}}
                xt.db.poc.browser-sharedworker-custom-test/Schema
                xt.db.poc.browser-sharedworker-custom-test/SchemaLookup)
               (then
                (fn [node]
                  (. port (postMessage {"type" "primary-connected"}))
                  (. port (postMessage {"type" "sqlite-connected"}))
                  (xt.substrate/register-handler
                   node
                   "custom-pull-view"
                   xt.db.node.adaptor-base/custom-pull-view
                   {})
                  (return
                   (xt.substrate.transport-browser/boot-self
                    node
                    {"transport_id" "host"
                     "target" port}))))
               (catch
                   (fn [err]
                     (. port (postMessage {"type" "error"
                                           "stage" "init"
                                           "message" (. err ["message"])
                                           "stack" (. err ["stack"])}))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

^{:refer xt.db.poc.browser-sharedworker-custom-test/custom-pull-view
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "client can configure a custom pull view via a server-side handler"

  (notify/wait-on [:js 15000]
    (var messages [])
    (var blob (new Blob [(@! +sharedworker-script+)] {"type" "text/javascript"}))
    (var url (. (!:G URL) (createObjectURL blob)))
    (var shared (new SharedWorker url {"type" "module"}))
    (var port (. shared ["port"]))
    (. port (start))
    (. port (addEventListener
              "message"
              (fn [event]
                (var data (. event ["data"]))
                (. messages (push data))
                (var type (xt/x:get-key data "type"))
                (when (== type "error")
                  (repl/notify {"messages" messages
                                "error" data}))
                (when (== type "sqlite-connected")
                  (var client (substrate/node-create {"id" "db-model-client"}))
                  (. (browser-transport/connect-sharedworker
                      client
                      {"transport_id" "worker"
                       "source" shared
                       "wait_ready" false})
                     (then
                      (fn [conn]
                        (return
                         (substrate/request
                          client
                          "room/a"
                          "custom-pull-view"
                          [{"space_id" "room/a"
                            "group_id" "demo"
                            "model_id" "custom-view"
                            "service" {"local_id" "db/caching"
                                       "remote_id" "db/primary"}}
                           {"pipeline" {}
                            "options" {}
                            "defaults" {"args" [["Log"]]}}]
                          {"transport_id" "worker"}))))
                     (then
                      (fn [result]
                        (repl/notify result)))
                     (catch
                         (fn [err]
                           (repl/notify {"error" err
                                         "message" (. err ["message"])}))))))
              false))
    (. shared (addEventListener
               "error"
               (fn [event]
                 (. messages (push {"type" "sharedworker-error"
                                   "message" (. event ["message"])}))
                 (repl/notify {"messages" messages
                               "error" event}))
               false))
    (. (!:G URL) (revokeObjectURL url))
    (return shared))
  => (contains-in
      {"status" "attached"
       "space" "room/a"
       "group" "demo"
       "model" "custom-view"}))
