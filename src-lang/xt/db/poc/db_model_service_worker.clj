(ns xt.db.poc.db-model-service-worker
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-remote :as page-remote]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]]})

(defn.js create-impl
  "Creates a db impl directly, avoiding the full impl-main dispatch so that
   worker bundles do not pull in unused native drivers (e.g. sqlite)."
  {:added "4.1"}
  [type schema lookup]
  (cond (== type "memory")
        (return (impl-memory/impl-memory schema lookup))

        :else
        (xt/x:err (+ "unsupported worker db type: " type))))

(defn.js init-services
  "Initialises db/primary and db/caching services on a node. For the worker POC
   only memory-backed impls are provided out of the box; a supabase impl can be
   injected by passing an already-created service object as :primary or :caching."
  {:added "4.1"}
  [node config schema lookup]
  (var #{primary caching} config)
  (substrate/set-service node "db/common" {:schema schema :lookup lookup})
  (var primary-impl (:? (xt/x:has-key? primary "impl")
                         (xt/x:get-key primary "impl")
                         (-/create-impl (xt/x:get-key primary "type") schema lookup)))
  (var caching-impl (:? (xt/x:has-key? caching "impl")
                         (xt/x:get-key caching "impl")
                         (-/create-impl (xt/x:get-key caching "type") schema lookup)))
  (substrate/set-service node "db/primary" primary-impl)
  (substrate/set-service node "db/caching" caching-impl)
  (return (promise/x:promise-run node)))

(defn.js create-page-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [model-id tree opts]
  (:= opts (or opts {}))
  (return {"handler" (fn [context]
                       (var node (. context ["node"]))
                       (var args (. context ["args"]))
                       (var pull-tree (xt/x:first args))
                       (var caching (substrate/get-service node "db/caching"))
                       (return (impl-common/pull caching pull-tree)))
           "pipeline" {"remote" {"handler" (fn [context]
                                             (var node (. context ["node"]))
                                             (var args (. context ["args"]))
                                             (var pull-tree (xt/x:first args))
                                             (var primary (substrate/get-service node "db/primary"))
                                             (return (impl-common/pull-async primary pull-tree)))}}
           "defaults" {"args" [tree]}
           "options" opts}))

(defn.js install-page-models
  "Attaches a group of db-model-service page models to a node space."
  {:added "4.1"}
  [node space-id group-id models]
  (var model-map {})
  (xt/for:object [[model-id model-spec] models]
    (xt/x:set-key model-map model-id model-spec))
  (return (page-core/add-group-attach node space-id group-id model-map)))

(defn.js create-server-node
  "Creates the worker server node and installs db/primary and db/caching services."
  {:added "4.1"}
  [config schema lookup]
  (return (-/init-services (substrate/node-create {"id" "db-model-server"})
                           config
                           schema
                           lookup)))

(defn.js install-server-models
  "Installs db-model-service page models on the server node."
  {:added "4.1"}
  [node space-id group-id model-specs]
  (return (-/install-page-models node space-id group-id model-specs)))

(defn.js boot-worker-server
  "Boots the server node on a worker self object (SharedWorker port, webworker self, etc.).

   The ready payload is posted back to the client once the transport is attached."
  {:added "4.1"}
  [node worker-self ready-payload]
  (page-remote/install node)
  (return
   (browser-transport/boot-self
    node
    {"transport_id" "host"
     "target" worker-self
     "ready" ready-payload})))

(defn.js run-server
  "High-level helper that creates the server node, installs services and models,
   then boots the worker transport. Designed to be called from a worker entry script."
  {:added "4.1"}
  [worker-self config schema lookup space-id group-id models ready-payload]
  (return
   (promise/x:promise-then
    (-/create-server-node config schema lookup)
    (fn [node]
      (page-remote/install node)
      (xt/for:object [[model-id tree] models]
        (-/install-page-models
         node
         space-id
         group-id
         {model-id (-/create-page-model model-id tree {})}))
      (return
       (promise/x:promise-then
        (-/boot-worker-server node worker-self ready-payload)
        (fn [_] (return node))))))))

(defn.js create-client-node
  "Creates a client node with page-remote installed."
  {:added "4.1"}
  []
  (var node (substrate/node-create {"id" "db-model-client"}))
  (page-remote/install node)
  (return node))

(defn.js connect-client
  "Connects a client node to a worker source."
  {:added "4.1"}
  [client source opts]
  (:= opts (or opts {}))
  (return
   (browser-transport/connect-sharedworker
    client
    (xt/x:obj-assign
     {"transport_id" "worker"
      "source" source}
     opts))))
