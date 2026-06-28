(ns xt.db.poc.n01-webworker-test
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
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.main]
             [js.worker.link :as worker-link]]})


(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

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
                          "memory"
                          {}
                          schema
                          lookup))
       (. (. (. (xt.db.system.main/create-impl-init primary-impl)
                (then
                 (fn [_]
                   (return (xt.db.system.main/create-impl-init caching-impl)))))
             (then
              (fn [_]
                (var node (xt.substrate/node-create {"id" "db-model-server"}))
                (xt.substrate/set-service node "db/common" {:schema schema :lookup lookup})
                (xt.substrate/set-service node "db/primary" primary-impl)
                (xt.substrate/set-service node "db/caching" caching-impl)
                (xt.substrate.page-proxy/install node)
                (var primary (xt.substrate/get-service node "db/primary"))
                (var caching (xt.substrate/get-service node "db/caching"))
                (. (xt.db.system.impl-common/pull-async primary ["Log"])
                   (then
                    (fn [records]
                      (xt.db.system.impl-common/record-add caching "Log" records)
                      (. worker-self (postMessage {"signal" "seeded"}))
                      (return nil))))
                (xt.substrate.page-core/add-group-attach
                 node "room/a" "demo"
                 {"entry" {"handler" (fn [context]
                                       (var n (. context ["node"]))
                                       (var args (. context ["args"]))
                                       (var pull-tree (. args [0]))
                                       (var c (xt.substrate/get-service n "db/caching"))
                                       (return (xt.db.system.impl-common/pull c pull-tree)))
                           "pipeline" {"remote" {"handler" (fn [context]
                                                             (var n (. context ["node"]))
                                                             (var args (. context ["args"]))
                                                             (var pull-tree (. args [0]))
                                                             (var p (xt.substrate/get-service n "db/primary"))
                                                             (return (xt.db.system.impl-common/pull-async p pull-tree)))}}
                           "defaults" {"args" [["Log"]]}
                           "options" {}}})
                (return
                 (xt.substrate.transport-browser/boot-self
                  node
                  {"transport_id" "host"
                   "target" worker-self
                   "ready" {"signal" "ready"
                            "worker" "db-model-server"}})))))
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
                       "data:text/javascript,export default {}"
                       "pg"
                       "data:text/javascript,export default {Client: function() {}}"}}})))

(fact:global
 {
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.node-webworker-test/server-node-fact :added "4.1"
  :setup [(scratch-v0/log-append-public "primary")]}
(fact "server node installs db/primary and db/caching services and exposes page models"

  (notify/wait-on :js
    (var schema -/Schema)
    (var lookup -/SchemaLookup)
    
    (var node (substrate/node-create {"id" "db-model-server"}))
    (substrate/set-service node "db/common" {:schema schema :lookup lookup})
    (substrate/set-service node "db/primary" (impl-memory/impl-memory schema lookup))
    (substrate/set-service node "db/caching" (impl-memory/impl-memory schema lookup))
    (-> (promise/x:promise-run node)
        (promise/x:promise-then
         (fn [node]
           (page-proxy/install node)
           (page-core/add-group-attach
            node "room/a" "demo"
            {"entry" {"handler" (fn [context]
                                  (var node (. context ["node"]))
                                  (var args (. context ["args"]))
                                  (var pull-tree (. args [0]))
                                  (var caching (substrate/get-service node "db/caching"))
                                  (return (impl-common/pull caching pull-tree)))
                      "pipeline" {"remote" {"handler" (fn [context]
                                                        (var node (. context ["node"]))
                                                        (var args (. context ["args"]))
                                                        (var pull-tree (. args [0]))
                                                        (var primary (substrate/get-service node "db/primary"))
                                                        (return (impl-common/pull-async primary pull-tree)))}}
                      "defaults" {"args" [["Log"]]}
                      "options" {}}})
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           (impl-common/record-add primary "Log" [{"id" "E-1" "message" "primary"}])
           (impl-common/record-add caching "Log" [{"id" "E-1" "message" "cached"}])
           (-> (page-proxy/list-proxy-groups node "room/a" {})
               (promise/x:promise-then
                (fn [groups]
                  (-> (page-proxy/open-proxy-group node "room/a" "demo" {})
                      (promise/x:promise-then
                       (fn [group]
                         (repl/notify
                          {"groups" groups
                           "has_group" (xt/x:not-nil? group)
                           "model_type" (xt/x:get-key (xtd/get-in group ["models" "entry"]) "::")})))))))))))
  => {"groups" {"demo" {"models" ["entry"]}}
      "has_group" true
      "model_type" "event.model"})

^{:refer xt.db.poc.node-webworker-test/worker-server-fact :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "worker-hosted server exposes db models to a connecting client"

  (notify/wait-on [:js 10000]
    (var link (worker-link/make-node-link (@! +server-script+) {}))
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (var conn-ref nil)
    (var groups-ref nil)
    (. (browser-transport/connect-sharedworker
        client
        {"transport_id" "worker"
         "source" link})
       (then
        (fn [conn]
          (:= conn-ref conn)
          (return (page-proxy/list-proxy-groups client "room/a" {"transport_id" (. conn ["transport_id"])}))))
       (then
        (fn [groups]
          (:= groups-ref groups)
          (return (page-proxy/open-proxy-group client "room/a" "demo" {"transport_id" (. conn-ref ["transport_id"])}))))
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
