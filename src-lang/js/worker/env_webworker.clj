(ns js.worker.env-webworker
  "WebWorker runtime script entrypoint for js.worker."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.event.node-transport-browser :as node-transport]
             [xt.db.node :as db-node]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt db-node-opts
  "gets db-node opts from runtime config"
  {:added "4.1"}
  [opts]
  (return (or (xt/x:get-key opts "db-node")
              (xt/x:get-key opts "db_node")
              {})))

(defn.xt runtime-config
  "gets runtime config from args or the emitted bootstrap global"
  {:added "4.1"}
  [opts]
  (return (or opts
              (. globalThis ["__JS_WORKER_WEBWORKER_OPTS"])
              {})))

(defn.xt register-handlers
  "registers extra handlers onto the worker node"
  {:added "4.1"}
  [node handlers]
  (xt/for:object [[action handler] (or handlers {})]
    (event-node/register-handler node action handler nil))
  (return node))

(defn.xt register-triggers
  "registers extra triggers onto the worker node"
  {:added "4.1"}
  [node triggers]
  (xt/for:object [[signal trigger] (or triggers {})]
    (event-node/register-trigger node signal trigger nil))
  (return node))

(defn.xt create-node
  "creates a worker-hosted node and installs xt.db.node"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var node (event-node/node-create (or (xt/x:get-key config "node") {})))
  (db-node/install node (-/db-node-opts config))
  (-/register-handlers node (xt/x:get-key config "handlers"))
  (-/register-triggers node (xt/x:get-key config "triggers"))
  (return node))

(defn.xt attach-self
  "attaches worker self as a node transport"
  {:added "4.1"}
  [node worker opts]
  (var config (or opts {}))
  (var transport-id (or (xt/x:get-key config "transport_id")
                        "host"))
  (return
   (promise/x:promise-then
    (event-node/attach-transport
     node
     transport-id
     (node-transport/self-endpoint worker))
    (fn [_]
      (return node)))))

(defn.xt init-worker
  "boots a WebWorker-hosted xt.db.node runtime"
  {:added "4.1"}
  [worker opts]
  (var node (-/create-node opts))
  (return (-/attach-self node worker opts)))

(defn.xt runtime-bootstrap
  "boots js.worker inside a WebWorker using emitted global config"
  {:added "4.1"}
  []
  (var config (or (. globalThis ["__JS_WORKER_WEBWORKER_OPTS"]) {}))
  (var node-opts (or (. config ["node"]) {}))
  (var db-opts (or (. config ["db-node"])
                   (. config ["db_node"])
                   {}))
  (var transportId (or (. config ["transport_id"])
                       "host"))
  (var node (event-node/node-create node-opts))
  (db-node/install node db-opts)
  (event-node/attach-transport
   node
   transportId
   (node-transport/self-endpoint self))
  (return node))

(defn.xt runtime-init
  "boots js.worker inside a WebWorker"
  {:added "4.1"}
  [opts]
  (return (-/init-worker self (-/runtime-config opts))))
