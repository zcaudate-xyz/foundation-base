(ns xt.db.poc.db-model-service-worker-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [js.worker.link]))

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
             [xt.db.poc.db-model-service-worker :as db-model-worker]
             [js.worker.link :as worker-link]]})

(def ^:private +server-script+
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
      (var schema {"Log" {"id" {"type" "uuid" "primary" true "order" 0}
                          "message" {"type" "text" "order" 1}}})
      (var lookup {"Log" {"position" 0}})
      (var node (xt.substrate/node-create
                 {"id" "db-model-server"
                  "spaces" {"room/a" {"state" {}}}}))
      (xt.substrate.page-remote/install node)
      (xt.substrate.page-core/add-group
       node
       "room/a"
       "demo"
       {"main" {"defaults" {"args" ["hello"]
                            "output" {}
                            "process" (fn [x] (return x))
                            "init" (fn [] (return nil))}
                "handler" (fn [ctx]
                            (var data (xt.lang.common-data/get-in ctx ["input" "data"]))
                            (return {"value" (xt.lang.spec-base/x:first data)}))
                "trigger" true
                "options" {}}})
      (. (xt.substrate.transport-browser/boot-self
          node
          {"transport_id" "host"
           "target" worker-self
           "ready" {"signal" "ready"
                    "worker" "db-model-server"}})
         (then
          (fn [_]
            (return node)))
         (catch
          (fn [err]
            (. worker-self (postMessage {"signal" "error"
                                         "message" (or (. err ["message"])
                                                       "boot error")}))
            (return nil)))))
   {:lang :js
    :layout :flat}))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.poc.db-model-service-worker/create-server-node :added "4.1"}
(fact "server node installs db/primary and db/caching services and exposes page models"

  (notify/wait-on :js
    (-> (db-model-worker/create-server-node
         {"primary" {"type" "memory"}
          "caching" {"type" "memory"}}
         {"Log" {"id" {"type" "uuid" "primary" true "order" 0}
                 "message" {"type" "text" "order" 1}}}
         {"Log" {"position" 0}})
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
           (var groups (page-remote/list-remote-groups node "room/a" {}))
           (-> (page-remote/open-remote-group node "room/a" "demo" {})
               (promise/x:promise-then
                (fn [group]
                  (repl/notify
                   {"groups" groups
                    "has_group" (xt/x:not-nil? group)
                    "model_type" (xt/x:get-key (xtd/get-in group ["models" "entry"]) "::")}))))))))
  => {"groups" {"demo" {"models" ["entry"]}}
      "has_group" true
      "model_type" "event.model"})

^{:refer xt.db.poc.db-model-service-worker/create-server-node :added "4.1"}
(fact "server node installs db/primary and db/caching services"

  (notify/wait-on :js
    (-> (db-model-worker/create-server-node
         {"primary" {"type" "memory"}
          "caching" {"type" "memory"}}
         {"Log" {"id" {"type" "uuid" "primary" true "order" 0}
                 "message" {"type" "text" "order" 1}}}
         {"Log" {"position" 0}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"primary" (xt/x:get-key (substrate/get-service node "db/primary") "::")
             "caching" (xt/x:get-key (substrate/get-service node "db/caching") "::")})))))
  => {"primary" "xt.db.system.impl_memory/ImplMemory"
      "caching" "xt.db.system.impl_memory/ImplMemory"})

^{:refer xt.db.poc.db-model-service-worker/boot-worker-server :added "4.1"}
(fact "worker-hosted server exposes db model groups to a connecting client"

  (notify/wait-on [:js 10000]
    (var link (worker-link/make-node-link (@! +server-script+) {}))
    (var client (db-model-worker/create-client-node))
    (-> (db-model-worker/connect-client client link {})
        (promise/x:promise-then
         (fn [conn]
           (var transport-id (. conn ["transport_id"]))
           (return
            (promise/x:promise-then
             (page-remote/list-remote-groups client "room/a" {"transport_id" transport-id})
             (fn [groups]
               (browser-transport/disconnect conn)
               (repl/notify {"groups" groups}))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"groups" {"demo" {"models" ["main"]}}}))
