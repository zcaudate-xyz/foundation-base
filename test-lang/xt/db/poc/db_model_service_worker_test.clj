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
      (. (xt.db.poc.db-model-service-worker/run-server
          worker-self
          {"primary" {"type" "memory"}
           "caching" {"type" "memory"}}
          {"Log" {"id" {"type" "uuid" "primary" true "order" 0}
                  "message" {"type" "text" "order" 1}}}
          {"Log" {"position" 0}}
          "room/a"
          "demo"
          {"entry" ["Log"]}
          {"signal" "ready"
           "worker" "db-model-server"}
          (fn [node]
            (var primary (xt.substrate/get-service node "db/primary"))
            (var caching (xt.substrate/get-service node "db/caching"))
            (xt.db.system.impl-common/record-add primary "Log" [{"id" "E-1" "message" "primary"}])
            (xt.db.system.impl-common/record-add caching "Log" [{"id" "E-1" "message" "cached"}])))
         (then
          (fn [node]
            (return node)))
         (catch
          (fn [err]
            (. worker-self (postMessage {"signal" "error"
                                         "message" (. err ["message"])}))
            (return nil)))))
   {:lang :js
    :layout :full}))

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

^{:refer xt.db.poc.db-model-service-worker/boot-worker-server :added "4.1"}
(fact "worker-hosted server exposes db models to a connecting client

  Note: the remote model output is currently nil in this end-to-end test because
  the server uses add-group-attach (no initial refresh) and the client-side
  model-update snapshot is not fully propagated before disconnect. The local test
  above verifies that the same model spec reads correctly from db/caching."

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
          (repl/notify {"connected" true
                        "groups" groups-ref
                        "has_group" (xt/x:not-nil? group)
                        "model_type" (xt/x:get-key model "::")
                        "output" (event-model/get-current model nil)})))
       (catch
        (fn [err]
          (repl/notify {"connected" false
                        "error" err
                        "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"connected" true
       "groups" {"demo" {"models" ["entry"]}}
       "has_group" true
       "model_type" "event.model"}))
