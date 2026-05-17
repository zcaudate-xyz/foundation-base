(ns js.worker.webworker
  "Public WebWorker facade for worker-hosted xt.db.node runtimes."
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]
            [js.worker.emit :as worker-emit]))

(l/script :xtalk
  {:require [[js.worker.link :as worker-link]
             [xt.event.node-transport-browser :as node-transport]
             [xt.db.node :as db-node]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn script-bootstrap
  "extracts the worker bootstrap config on the host side"
  {:added "4.1"}
  [opts]
  (let [worker-opts (or (get opts "worker") {})
        bootstrap  (or (get worker-opts "bootstrap") {})]
    (merge bootstrap
           {"node" (or (get worker-opts "node") {})
            "db-node" (or (get opts "db-node")
                          (get opts "db_node")
                          {})
            "handlers" (or (get opts "handlers") {})
            "triggers" (or (get opts "triggers") {})})))

(defn script
  "emits the worker bootstrap script on the host side"
  {:added "4.1"}
  ([opts]
   (worker-emit/webworker-script (script-bootstrap opts)))
  ([opts layout]
   (worker-emit/webworker-script (script-bootstrap opts) layout)))

(defn.xt db-node-opts
  "gets db-node opts from the public config"
  {:added "4.1"}
  [opts]
  (return (or (xt/x:get-key opts "db-node")
              (xt/x:get-key opts "db_node")
              {})))

(defn.xt worker-bootstrap
  "extracts the worker bootstrap config"
  {:added "4.1"}
  [opts]
  (var worker-opts (or (xt/x:get-key opts "worker") {}))
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign
     {}
     (or (xt/x:get-key worker-opts "bootstrap") {}))
    {"node" (or (xt/x:get-key worker-opts "node") {})
     "db-node" (-/db-node-opts opts)
     "handlers" (or (xt/x:get-key opts "handlers") {})
     "triggers" (or (xt/x:get-key opts "triggers") {})})))

(defn.xt worker-script
  "resolves an explicit worker script or emits one from config"
  {:added "4.1"}
  [opts]
  (var worker-opts (or (xt/x:get-key opts "worker") {}))
  (var explicit (xt/x:get-key worker-opts "script"))
  (if (xt/x:not-nil? explicit)
    (return (worker-link/resolve-script explicit))
    (throw "js.worker.webworker/start requires `worker.script` or `worker.link`; use js.worker.webworker/script on the host to generate the bootstrap source.")))

(defn.xt worker-source
  "resolves the low-level worker source"
  {:added "4.1"}
  [opts]
  (var worker-opts (or (xt/x:get-key opts "worker") {}))
  (var link (xt/x:get-key worker-opts "link"))
  (if (xt/x:not-nil? link)
    (return link)
    (return (worker-link/make-webworker-link (-/worker-script opts)))))

(defn.xt start
  "creates a host node, worker, and transport attachment"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var host-node (event-node/node-create (or (xt/x:get-key config "node") {})))
  (var worker-opts (or (xt/x:get-key config "worker") {}))
  (var transport-id (or (xt/x:get-key worker-opts "transport_id")
                        "worker"))
  (var source (-/worker-source config))
  (var explicit-script (xt/x:get-key worker-opts "script"))
  (var link (xt/x:get-key worker-opts "link"))
  (var script (:? (xt/x:not-nil? explicit-script)
                  (worker-link/resolve-script explicit-script)
                  (:? (xt/x:not-nil? link)
                      nil
                      (-/worker-script config))))
  (return
   (promise/x:promise-then
    (event-node/attach-transport
     host-node
     transport-id
     (node-transport/worker-endpoint source))
    (fn [_]
      (var transport (event-node/get-transport host-node transport-id))
      (return {"node" host-node
               "worker" (xt/x:get-key transport "listener")
               "transport_id" transport-id
               "script" script})))))

(defn.xt stop
  "detaches the transport and stops the worker"
  {:added "4.1"}
  [runtime]
  (var transport-id (or (xt/x:get-key runtime "transport_id")
                        "worker"))
  (return
   (promise/x:promise-then
    (event-node/detach-transport
     (xt/x:get-key runtime "node")
     transport-id)
    (fn [_]
      (return true)))))

(defn.xt space
  "creates an explicit space handle"
  {:added "4.1"}
  [runtime space-id]
  (return {"runtime" runtime
           "node" (xt/x:get-key runtime "node")
           "space" space-id}))

(defn.xt query
  "runs a node query in a space"
  {:added "4.1"}
  [client payload]
  (return (db-node/query
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           payload)))

(defn.xt sync
  "runs a node sync in a space"
  {:added "4.1"}
  [client payload]
  (return (db-node/sync
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           payload)))

(defn.xt remove
  "runs a node remove in a space"
  {:added "4.1"}
  [client payload]
  (return (db-node/remove
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           payload)))

(defn.xt clear
  "clears a node space"
  {:added "4.1"}
  [client]
  (return (db-node/clear
           (xt/x:get-key client "node")
           (xt/x:get-key client "space"))))

(defn.xt snapshot
  "gets a node snapshot for a space"
  {:added "4.1"}
  [client]
  (return (db-node/snapshot
           (xt/x:get-key client "node")
           (xt/x:get-key client "space"))))

(defn.xt model-put
  "registers a model on a space"
  {:added "4.1"}
  [client model-id model-spec]
  (return (db-node/model-put
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           model-id
           model-spec)))

(defn.xt view-put
  "registers a view on a space"
  {:added "4.1"}
  [client model-id view-id view-spec]
  (return (db-node/view-put
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           model-id
           view-id
           view-spec)))

(defn.xt view-set-input
  "sets view input inside a space"
  {:added "4.1"}
  [client model-id view-id input]
  (return (db-node/view-set-input
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           model-id
           view-id
           input)))

(defn.xt view-refresh
  "refreshes a view inside a space"
  {:added "4.1"}
  [client model-id view-id]
  (return (db-node/view-refresh
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           model-id
           view-id)))

(defn.xt view-val
  "gets a view value inside a space"
  {:added "4.1"}
  [client model-id view-id]
  (return (db-node/view-val
           (xt/x:get-key client "node")
           (xt/x:get-key client "space")
           model-id
           view-id)))
