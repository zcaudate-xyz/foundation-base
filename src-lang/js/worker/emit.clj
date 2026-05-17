(ns js.worker.emit
  "Host-side worker script emitters for js.worker."
  (:require [js.worker.env-node]
             [js.worker.env-sharedworker]
             [hara.lang :as l]
             [xt.db.node]
             [xt.substrate]
             [xt.substrate.transport-browser]))

(defn emit-worker-script
  "emits a worker bootstrap script"
  ([forms]
    (emit-worker-script forms :full))
  ([forms layout]
    (l/emit-script (cons 'do forms)
                   {:lang :js
                    :layout layout})))

(defn webworker-forms
  "returns the WebWorker bootstrap forms"
  ([]
   (webworker-forms {}))
  ([opts]
   [(list ':= (list '. 'globalThis ["__JS_WORKER_WEBWORKER_OPTS"]) opts)
    '(var config (or (. globalThis ["__JS_WORKER_WEBWORKER_OPTS"]) {}))
    '(var nodeOpts (or (. config ["node"]) {}))
    '(var dbOpts (or (. config ["db-node"])
                     (. config ["db_node"])
                     {}))
    '(var transportId (or (. config ["transport_id"])
                          "host"))
    '(var node (xt.substrate/node-create nodeOpts))
    '(xt.db.node/install node dbOpts)
    '(xt.substrate/attach-transport
      node
      transportId
      (xt.substrate.transport-browser/self-endpoint self))
    'node]))

(defn webworker-script
  "emits the WebWorker bootstrap script"
  ([]
   (webworker-script {} :full))
  ([opts]
   (webworker-script opts :full))
  ([opts layout]
   (emit-worker-script (webworker-forms opts) layout)))

(defn sharedworker-forms
  "returns the SharedWorker bootstrap forms"
  []
  '[(js.worker.env-sharedworker/runtime-init)])

(defn sharedworker-script
  "emits the SharedWorker bootstrap script"
  ([]
   (sharedworker-script :full))
  ([layout]
   (emit-worker-script (sharedworker-forms) layout)))

(defn node-forms
  "returns the Node worker bootstrap forms"
  []
  '[(js.worker.env-node/runtime-init)])

(defn node-script
  "emits the Node worker bootstrap script"
  ([]
   (node-script :full))
  ([layout]
   (emit-worker-script (node-forms) layout)))
