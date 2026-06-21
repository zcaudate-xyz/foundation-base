(ns xt.db.poc.node-webworker-test
  (:use code.test)
  (:require [clojure.string]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [js.worker.link]))

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
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-remote :as page-remote]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.main]
             [xt.db.poc.db-model-service-worker :as db-model-worker]
             [js.worker.link :as worker-link]]})

(defn- fix-worker-script
  "Node 24 eval workers parse scripts containing ESM imports as modules, so the
   worker_threads setup must use a static ESM import instead of CommonJS require."
  [script]
  (str "import { parentPort } from 'worker_threads';\n"
       (-> script
           (clojure.string/replace #"let worker_threads = require\(\"worker_threads\"\);\n" "")
           (clojure.string/replace #"worker_threads\[\"parentPort\"\]" "parentPort"))))

(def ^:private +server-script+
  (fix-worker-script
   (l/emit-script
    '(do
       (var worker-threads (require "worker_threads"))
       (var parent-port (. worker-threads ["parentPort"]))
       (var worker-self
            {"postMessage" (fn [data]
                             (. parent-port (postMessage data)))
             "addEventListener" (fn [event listener capture]
                                  (when (== event "message")
                                    (. parent-port (on "message"
                                                      (fn [data]
                                                        (listener {"data" data}))))))})
       (var schema {"Log" {"id" {"ident" "id"
                                  "type" "uuid"
                                  "primary" true
                                  "order" 0}
                           "message" {"ident" "message"
                                      "type" "text"
                                      "order" 1}}})
       (var lookup {"Log" {"position" 0}})
       (var primary-impl (xt.db.system.main/create-impl
                          "supabase"
                          (@! local-min/+config-supabase-anon+)
                          schema
                          lookup))
       (var caching-impl (xt.db.system.main/create-impl
                          "sqlite"
                          {"filename" ":memory:"}
                          schema
                          lookup))
       (. (. (. (xt.db.system.main/create-impl-init primary-impl)
                (then
                 (fn [_]
                   (return (xt.db.system.main/create-impl-init caching-impl)))))
             (then
              (fn [_]
                (return
                 (xt.db.poc.db-model-service-worker/run-server
                  worker-self
                  {"primary" {"impl" primary-impl}
                   "caching" {"impl" caching-impl}}
                  schema
                  lookup
                  "room/a"
                  "demo"
                  {"entry" ["Log"]}
                  {"signal" "ready"
                   "worker" "db-model-server"}
                  (fn [node]
                    (var primary (xt.substrate/get-service node "db/primary"))
                    (var caching (xt.substrate/get-service node "db/caching"))
                    (return
                     (. (xt.db.system.impl-common/pull-async primary ["Log"])
                        (then
                         (fn [records]
                           (xt.db.system.impl-common/record-add caching "Log" records)
                           (. worker-self (postMessage {"signal" "seeded"}))))))))))))
          (then
           (fn [node]
             (return node)))
          (catch
           (fn [err]
             (. worker-self (postMessage {"signal" "error"
                                          "message" (. err ["message"])}))
             (return nil)))))
    {:lang :js
     :layout :full
     :emit {:override {"@sqlite.org/sqlite-wasm"
                       "file:///home/hoebat/Development/greenways/gw-v2/ref/foundation/node_modules/@sqlite.org/sqlite-wasm/dist/node.mjs"
                       "pg"
                       "data:text/javascript,export default {Client: function() {}}"}}})))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.db-model-service-worker/create-server-node :added "4.1"
  :setup [(scratch-v0/log-append-public "primary")]}
(fact "server node installs db/primary and db/caching services and exposes page models"

  (notify/wait-on :js
    (var schema {"Log" {"id" {"ident" "id"
                               "type" "uuid"
                               "primary" true
                               "order" 0}
                        "message" {"ident" "message"
                                   "type" "text"
                                   "order" 1}}})
    (var lookup {"Log" {"position" 0}})
    (-> (db-model-worker/create-server-node
         {"primary" {"type" "memory"}
          "caching" {"type" "memory"}}
         schema
         lookup)
        (promise/x:promise-then
         (fn [node]
           (page-remote/install node)
           (db-model-worker/install-page-models
            node "room/a" "demo"
            {"entry" (db-model-worker/create-page-model "entry" ["Log"] {})})
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           (impl-common/record-add primary "Log" [{"id" "E-1" "message" "primary"}])
           (impl-common/record-add caching "Log" [{"id" "E-1" "message" "cached"}])
           (-> (page-remote/list-remote-groups node "room/a" {})
               (promise/x:promise-then
                (fn [groups]
                  (-> (page-remote/open-remote-group node "room/a" "demo" {})
                      (promise/x:promise-then
                       (fn [group]
                         (repl/notify
                          {"groups" groups
                           "has_group" (xt/x:not-nil? group)
                           "model_type" (xt/x:get-key (xtd/get-in group ["models" "entry"]) "::")})))))))))))
  => {"groups" {"demo" {"models" ["entry"]}}
      "has_group" true
      "model_type" "event.model"})

^{:refer xt.db.poc.db-model-service-worker/boot-worker-server :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "worker-hosted server exposes db models to a connecting client"

  (notify/wait-on [:js 10000]
    (var link (worker-link/make-node-link (@! +server-script+) {}))
    (var client (db-model-worker/create-client-node))
    (var conn-ref nil)
    (var groups-ref nil)
    (. (db-model-worker/connect-client client link {})
       (then
        (fn [conn]
          (:= conn-ref conn)
          (return (page-remote/list-remote-groups client "room/a" {"transport_id" (. conn ["transport_id"])}))))
       (then
        (fn [groups]
          (:= groups-ref groups)
          (return (page-remote/open-remote-group client "room/a" "demo" {"transport_id" (. conn-ref ["transport_id"])}))))
       (then
        (fn [group]
          (browser-transport/disconnect conn-ref)
          (var model (xtd/get-in group ["models" "entry"]))
          (repl/notify
           {"connected" true
            "groups" groups-ref
            "has_group" (xt/x:not-nil? group)
            "model_type" (xt/x:get-key model "::")
            "output" (event-model/get-current model nil)})))
       (catch
        (fn [err]
          (repl/notify
           {"connected" false
            "error" err
            "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"connected" true
       "groups" {"demo" {"models" ["entry"]}}
       "has_group" true
       "model_type" "event.model"}))
