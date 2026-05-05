(ns js.worker.env-webworker-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.event.node :as event-node]
             [xt.db.node :as db-node]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]
             [js.worker.env-webworker :as env-webworker]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.worker.env-webworker/create-node :added "4.1"}
(fact "creates a worker-hosted node with xt.db.node installed"
  (!.js
   (var node
        (env-webworker/create-node
         {"node" {"id" "worker-a"}
          "db-node" {"schema" {}
                     "lookup" {}}
           "handlers" {"custom/echo" (fn [space args request node]
                                         (return {"ok" true}))}
           "triggers" {"signal/demo" (fn [space stream node]
                                         (return true))}}))
   {"node?" (event-node/node? node)
    "id" (. node ["id"])
    "has-query-handler" (xt/x:has-key? (. node ["handlers"]) db-node/ACTION_QUERY)
    "has-custom-handler" (xt/x:has-key? (. node ["handlers"]) "custom/echo")
    "has-cache-trigger" (xt/x:has-key? (. node ["triggers"]) db-node/SIGNAL_CACHE_CHANGED)
    "has-custom-trigger" (xt/x:has-key? (. node ["triggers"]) "signal/demo")})
  => {"node?" true
      "id" "worker-a"
      "has-query-handler" true
      "has-custom-handler" true
      "has-cache-trigger" true
      "has-custom-trigger" true})

^{:refer js.worker.env-webworker/runtime-init :added "4.1"}
(fact "boots xt.db.node inside a WebWorker and attaches self transport"
  (notify/wait-on :js
    (var previous-self (!:G self))
    (var worker {:listeners []
                 :postMessage (fn [msg] msg)
                 :addEventListener (fn [event listener capture]
                                     (worker.listeners.push listener))})
    (:= (!:G self) worker)
    (promise/x:promise-catch
     (promise/x:promise-then
      (env-webworker/runtime-init
       {"node" {"id" "worker-b"}
        "db-node" {"schema" {}
                   "lookup" {}}})
      (fn [node]
        (var out {"node?" (event-node/node? node)
                  "has-query-handler" (xt/x:has-key? (. node ["handlers"]) db-node/ACTION_QUERY)
                  "transports" (event-node/list-transports node)
                  "listeners" (xt/x:len worker.listeners)})
        (:= (!:G self) previous-self)
        (repl/notify out)))
     (fn [err]
       (:= (!:G self) previous-self)
       (repl/notify err))))
  => (contains-in {"node?" true
                   "has-query-handler" true
                   "transports" ["host"]}))


^{:refer js.worker.env-webworker/db-node-opts :added "4.1"}
(fact "prefers db-node/db_node settings and defaults to an empty map"
  (!.js
   [(env-webworker/db-node-opts {"db-node" {"schema" {"Order" {}}}})
    (env-webworker/db-node-opts {"db_node" {"lookup" {"Order" {"position" 0}}}})
    (env-webworker/db-node-opts {})])
  => [{"schema" {"Order" {}}}
      {"lookup" {"Order" {"position" 0}}}
      {}])

^{:refer js.worker.env-webworker/runtime-config :added "4.1"}
(fact "uses explicit opts first and otherwise falls back to the bootstrap global"
  (!.js
   (var key "__JS_WORKER_WEBWORKER_OPTS")
   (var prev (xt/x:get-key globalThis key))
   (xt/x:set-key globalThis key {"global" true})
   (var out [(env-webworker/runtime-config {"local" true})
             (env-webworker/runtime-config nil)])
   (if (xt/x:nil? prev)
     (xt/x:del-key globalThis key)
     (xt/x:set-key globalThis key prev))
   (return out))
  => [{"local" true}
      {"global" true}])

^{:refer js.worker.env-webworker/register-handlers :added "4.1"}
(fact "registers extra handlers onto the worker node"
  (!.js
   (var node (event-node/node-create {"id" "worker-c"}))
   (env-webworker/register-handlers
    node
    {"custom/ping" (fn [space args request node]
                     (return {"ok" true}))})
   {"id" (. node ["id"])
    "handler?" (xt/x:has-key? (. node ["handlers"]) "custom/ping")})
  => {"id" "worker-c"
      "handler?" true})

^{:refer js.worker.env-webworker/register-triggers :added "4.1"}
(fact "registers extra triggers onto the worker node"
  (!.js
   (var node (event-node/node-create {"id" "worker-d"}))
   (env-webworker/register-triggers
    node
    {"signal/demo" (fn [space stream node]
                     (return true))})
   {"id" (. node ["id"])
    "trigger?" (xt/x:has-key? (. node ["triggers"]) "signal/demo")})
  => {"id" "worker-d"
      "trigger?" true})

^{:refer js.worker.env-webworker/attach-self :added "4.1"}
(fact "attaches worker self as a named transport"
  (notify/wait-on :js
    (var listeners [])
    (var worker {"postMessage" (fn [msg] msg)
                 "addEventListener" (fn [event listener capture]
                                      (listeners.push listener))})
    (var node (event-node/node-create {"id" "worker-e"}))
    (promise/x:promise-catch
     (promise/x:promise-then
      (env-webworker/attach-self node worker {"transport-id" "loopback"})
      (fn [attached]
        (repl/notify {"id" (. attached ["id"])
                      "transports" (event-node/list-transports attached)
                      "listener-count" (xt/x:len listeners)})))
     (fn [err]
       (repl/notify err))))
  => {"id" "worker-e"
      "transports" ["loopback"]
      "listener-count" 0})

^{:refer js.worker.env-webworker/init-worker :added "4.1"}
(fact "creates the node and immediately attaches worker self"
  (notify/wait-on :js
    (var listeners [])
    (var worker {"postMessage" (fn [msg] msg)
                 "addEventListener" (fn [event listener capture]
                                      (listeners.push listener))})
    (promise/x:promise-catch
     (promise/x:promise-then
      (env-webworker/init-worker
       worker
       {"node" {"id" "worker-f"}
        "db-node" {"schema" {}
                   "lookup" {}}
        "transport-id" "bridge"})
      (fn [node]
        (repl/notify {"id" (. node ["id"])
                      "has-query-handler" (xt/x:has-key? (. node ["handlers"]) db-node/ACTION_QUERY)
                      "transports" (event-node/list-transports node)
                      "listener-count" (xt/x:len listeners)})))
     (fn [err]
       (repl/notify err))))
  => {"id" "worker-f"
      "has-query-handler" true
      "transports" ["bridge"]
      "listener-count" 0})

^{:refer js.worker.env-webworker/runtime-bootstrap :added "4.1"}
(fact "boots from the emitted WebWorker global config"
  (!.js
   (var key "__JS_WORKER_WEBWORKER_OPTS")
   (var prev-config (xt/x:get-key globalThis key))
   (var prev-self (!:G self))
   (:= (!:G self) {"postMessage" (fn [msg] msg)
                   "addEventListener" (fn [event listener capture]
                                        listener)})
   (xt/x:set-key globalThis key
                 {"node" {"id" "worker-g"}
                  "db_node" {"schema" {}
                             "lookup" {}}
                  "transport_id" "host-bridge"})
   (var node (env-webworker/runtime-bootstrap))
   (:= (!:G self) prev-self)
   (if (xt/x:nil? prev-config)
     (xt/x:del-key globalThis key)
     (xt/x:set-key globalThis key prev-config))
   {"id" (. node ["id"])
    "has-query-handler" (xt/x:has-key? (. node ["handlers"]) db-node/ACTION_QUERY)
    "transports" (event-node/list-transports node)})
  => {"id" "worker-g"
      "has-query-handler" true
      "transports" ["host-bridge"]})
