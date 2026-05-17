(ns js.worker.webworker-test
  (:require [clojure.string :as str]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.worker.webworker :as webworker]
             [js.worker.link :as worker-link]
             [xt.db.node :as db-node]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.worker.webworker/script :added "4.1"}
(fact "emits a worker bootstrap script from the public facade"
  (str/includes? (webworker/script {"db-node" {"schema" {"Order" {}}}})
                 "attach_transport")
  => true)

^{:refer js.worker.webworker/space :added "4.1"}
(fact "creates explicit space handles"
  (!.js
   (var handle (webworker/space {"node" {"id" "ui"}} "gd"))
   [(. handle ["space"])
    (. (. handle ["node"]) ["id"])])
  => ["gd" "ui"])

^{:refer js.worker.webworker/start :added "4.1"}
(fact "starts a worker-backed host runtime"
  (notify/wait-on :js
    (promise/x:promise-catch
     (promise/x:promise-then
     (webworker/start
       {"node" {"id" "ui"}
        "worker" {"link" (worker-link/make-mock-link {})
                  "script" "self.__worker_boot = true;"}
        "db-node" {"schema" fixtures/Schema
                   "lookup" fixtures/Lookup
                   "views" fixtures/Views}})
      (fn [runtime]
        (var transport (event-node/get-transport (. runtime ["node"])
                                                 (. runtime ["transport_id"])))
        (var out {"node?" (event-node/node? (. runtime ["node"]))
                  "transport_id" (. runtime ["transport_id"])
                  "transport?" (xt/x:is-object? transport)
                  "script?" (xt/x:is-string? (. runtime ["script"]))})
        (repl/notify out)))
     (fn [err]
       (repl/notify err))))
  => (contains-in {"node?" true
                   "transport_id" "worker"
                   "transport?" true
                   "script?" true}))

^{:refer js.worker.webworker/model-put :added "4.1"}
(fact "forwards space-scoped xt.db.node model state without globals"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (webworker/model-put client "orders" fixtures/ModelSpec)
    (promise/x:promise-then
     (webworker/snapshot client)
     (fn [snapshot]
       (repl/notify {"snapshot" snapshot}))))
  => (contains-in {"snapshot" {"models" {"orders" {"views" {"main" {}
                                                           "open" {}}}}}}))


^{:refer js.worker.webworker/script-bootstrap :added "4.1"}
(fact "merges worker bootstrap config on the host side"
  (webworker/script-bootstrap
   {"db_node" {"schema" {"Order" {}}}
    "handlers" {"custom/ping" "handler"}
    "triggers" {"signal/demo" "trigger"}
    "worker" {"node" {"id" "ui"}
              "bootstrap" {"runtime" "webworker"}}})
  => {"runtime" "webworker"
      "node" {"id" "ui"}
      "db-node" {"schema" {"Order" {}}}
      "handlers" {"custom/ping" "handler"}
      "triggers" {"signal/demo" "trigger"}})

^{:refer js.worker.webworker/db-node-opts :added "4.1"}
(fact "reads db-node options from either public key"
  (!.js
   [(webworker/db-node-opts {"db-node" {"schema" {"Order" {}}}})
    (webworker/db-node-opts {"db_node" {"lookup" {"Order" {"position" 0}}}})
    (webworker/db-node-opts {})])
  => [{"schema" {"Order" {}}}
      {"lookup" {"Order" {"position" 0}}}
      {}])

^{:refer js.worker.webworker/worker-bootstrap :added "4.1"}
(fact "builds the emitted worker bootstrap payload"
  (!.js
   (webworker/worker-bootstrap
    {"db-node" {"schema" {"Order" {}}}
     "handlers" {"custom/ping" true}
     "triggers" {"signal/demo" true}
     "worker" {"node" {"id" "ui"}
               "bootstrap" {"runtime" "webworker"}}}))
  => {"runtime" "webworker"
      "node" {"id" "ui"}
      "db-node" {"schema" {"Order" {}}}
      "handlers" {"custom/ping" true}
      "triggers" {"signal/demo" true}})

^{:refer js.worker.webworker/worker-script :added "4.1"}
(fact "resolves explicit worker script values"
  (!.js
   (webworker/worker-script {"worker" {"script" "console.log('one');"}}))
  => "console.log('one');")

^{:refer js.worker.webworker/worker-source :added "4.1"}
(fact "prefers an explicit worker link when provided"
  (!.js
   (var link (worker-link/make-mock-link {}))
   (var source (webworker/worker-source {"worker" {"link" link}}))
   {"same-link" (== source link)
    "has-create-fn" (xt/x:is-function? (. source ["create_fn"]))})
  => {"same-link" true
      "has-create-fn" true})

^{:refer js.worker.webworker/stop :added "4.1"}
(fact "detaches the worker transport on stop"
  (notify/wait-on :js
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/start
       {"node" {"id" "ui"}
        "worker" {"link" (worker-link/make-mock-link {})
                  "script" "self.__worker_boot = true;"}
        "db-node" {"schema" fixtures/Schema
                   "lookup" fixtures/Lookup
                   "views" fixtures/Views}})
      (fn [runtime]
        (promise/x:promise-then
         (webworker/stop runtime)
         (fn [stopped]
           (repl/notify {"stopped" stopped
                         "transports" (event-node/list-transports (. runtime ["node"]))})))))
     (fn [err]
       (repl/notify err))))
  => {"stopped" true
      "transports" []})

^{:refer js.worker.webworker/query :added "4.1"}
(fact "runs a space-scoped query through xt.db.node"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (promise/x:promise-then
         (webworker/query client {:table "Order"
                                  :return-method "default"
                                  :return-id "ord-1"})
         (fn [result]
           (repl/notify {"query-key?" (xt/x:is-string? (. result ["query_key"]))
                         "value?" (xt/x:not-nil? (. result ["value"]))
                         "tables" (xt/x:obj-keys (. result ["tables"]))})))))
     (fn [err]
       (repl/notify err))))
  => {"query-key?" true
      "value?" true
      "tables" ["Order"]})

^{:refer js.worker.webworker/sync :added "4.1"}
(fact "applies sync payloads in the selected space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (promise/x:promise-then
         (webworker/snapshot client)
         (fn [snapshot]
           (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1" "ord-2"]})

^{:refer js.worker.webworker/remove :added "4.1"}
(fact "removes rows in the selected space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (promise/x:promise-then
         (webworker/remove client {"db/remove" {"Order" ["ord-2"]}})
         (fn [_]
           (promise/x:promise-then
            (webworker/snapshot client)
            (fn [snapshot]
              (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))))))
     (fn [err]
       (repl/notify err))))
  => {"rows" ["ord-1"]})

^{:refer js.worker.webworker/clear :added "4.1"}
(fact "clears cached rows in the selected space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (promise/x:promise-then
         (webworker/clear client)
         (fn [_]
           (promise/x:promise-then
            (webworker/snapshot client)
            (fn [snapshot]
              (repl/notify {"rows" (xt/x:obj-keys (. snapshot ["rows"]))})))))))
     (fn [err]
       (repl/notify err))))
  => {"rows" []})

^{:refer js.worker.webworker/snapshot :added "4.1"}
(fact "returns a snapshot for the active space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (webworker/model-put client "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/snapshot client)
      (fn [snapshot]
        (repl/notify {"models" (xt/x:obj-keys (. snapshot ["models"]))
                      "rows" (xt/x:obj-keys (. snapshot ["rows"]))})))
     (fn [err]
       (repl/notify err))))
  => {"models" ["orders"]
      "rows" []})

^{:refer js.worker.webworker/view-put :added "4.1"}
(fact "registers a single view inside the client space"
  (!.js
   (var node (event-node/node-create {"id" "ui"}))
   (db-node/install node fixtures/InstallOpts)
   (var client (webworker/space {"node" node} "gd"))
   (webworker/model-put client "orders" fixtures/ModelSpec)
   (webworker/view-put client "orders" "secondary" {"default_input" ["ord-2"]})
   {"view-id" (. (db-node/view-get node "gd" "orders" "secondary") ["id"])
    "input" (db-node/view-input node "gd" "orders" "secondary")})
  => {"view-id" "secondary"
      "input" ["ord-2"]})

^{:refer js.worker.webworker/view-set-input :added "4.1"}
(fact "updates view input through the public worker facade"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (webworker/model-put client "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (var result (webworker/view-set-input client "orders" "open" ["closed"]))
        (repl/notify {"input" (db-node/view-input node "gd" "orders" "open")
                      "result?" (xt/x:not-nil? result)})))
     (fn [err]
       (repl/notify err))))
  => {"input" ["closed"]
      "result?" true})

^{:refer js.worker.webworker/view-refresh :added "4.1"}
(fact "refreshes a view in the selected space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (webworker/model-put client "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (webworker/sync client {"db/sync" fixtures/Seed})
      (fn [_]
        (var result (webworker/view-refresh client "orders" "main"))
        (repl/notify {"result?" (xt/x:not-nil? result)
                      "pending" (db-node/view-pending node "gd" "orders" "main")})))
     (fn [err]
       (repl/notify err))))
  => {"result?" true
      "pending" false})

^{:refer js.worker.webworker/view-val :added "4.1"}
(fact "reads back the current view value from the client space"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "ui"}))
    (db-node/install node fixtures/InstallOpts)
    (var client (webworker/space {"node" node} "gd"))
    (webworker/model-put client "orders" fixtures/ModelSpec)
    (promise/x:promise-catch
     (promise/x:promise-then
      (promise/x:promise-then
       (webworker/sync client {"db/sync" fixtures/Seed})
       (fn [_]
         (webworker/view-refresh client "orders" "main")))
      (fn [_]
        (repl/notify {"status" (. (xt/x:first (webworker/view-val client "orders" "main")) ["status"])
                      "pending" (db-node/view-pending node "gd" "orders" "main")})))
     (fn [err]
       (repl/notify err))))
  => {"status" "open"
      "pending" false})
