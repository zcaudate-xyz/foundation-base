^{:seedgen/skip true}
(ns xt.db.poc.n01-webworker-test
  (:use code.test)
  (:require [clojure.string]
            [hara.lang :as l]
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
             [xt.db.node.runtime :as runtime]]})


(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))


(defn.js node-worker-module-source
  "creates a Node worker_threads source that evaluates the script as an ES module"
  [script opts]
  (var #{Worker} (require "worker_threads"))
  (return
   {"create_fn"
    (fn [listener]
      (var worker (new Worker script
                            {:eval true
                             :type "module"}))
      (. worker (on "message"
                    (fn [data]
                      (return (listener data)))))
      (return worker))}))


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
                (xt.substrate.page-core/group-add-attach
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
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})


^{:refer xt.db.poc.node-webworker-test/worker-server-fact :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "worker-hosted server exposes db models to a connecting client"

  (notify/wait-on [:js 10000]
    (var link (-/node-worker-module-source (@! +server-script+) {}))
    (var client (substrate/node-create {"id" "db-model-client"}))
    (page-proxy/install client)
    (var conn-ref nil)
    (var groups-ref nil)
    (. (browser-transport/connect-worker
        client
        {"transport_id" "worker"
         "source" link})
       (then
        (fn [conn]
          (:= conn-ref conn)
          (return (page-proxy/group-list-proxy client "room/a" {"transport_id" (. conn ["transport_id"])}))))
       (then
        (fn [groups]
          (:= groups-ref groups)
          (return (page-proxy/group-open-proxy client "room/a" "demo" {"transport_id" (. conn-ref ["transport_id"])}))))
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
