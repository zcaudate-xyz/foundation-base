(ns js.worker.sharedworker
  "SharedWorker bootstrap and remote xt.db.node helpers."
  (:require [clojure.walk :as walk]
            [hara.lang :as l]
            [js.lib.client-fetch]
            [xt.db.node]
            [xt.lang.common-data]
            [xt.lang.spec-base]
            [xt.lang.spec-promise]
            [xt.substrate]
            [xt.substrate.transport-browser]))

(defn script
  "Emits a SharedWorker bootstrap script that keeps a singleton node on globalThis."
  {:added "4.1"}
  ([opts]
   (script opts :full))
  ([opts layout]
   (let [config            (or opts {})
         node-init         (or (get config "node_init")
                               (throw (ex-info "js.worker.sharedworker/script requires `node_init`."
                                               {:opts config})))
         shared-key        (or (get config "shared_key")
                               "__js_worker_shared__")
         state             (or (get config "state")
                               {"counter" 0})
         ready             (or (get config "ready")
                               {"signal" "ready"})
         transport-prefix  (or (get config "transport_prefix")
                               "host-")
         template
         '(do
            (var shared (. globalThis [__SHARED_KEY__]))
            (if (xt.lang.spec-base/x:nil? shared)
              (do
                (:= shared __STATE__)
                (xt.lang.spec-base/x:set-key
                 shared
                 "ready"
                 (xt.lang.spec-promise/x:promise-then
                  __NODE_INIT__
                  (fn [node]
                    (xt.lang.spec-base/x:set-key shared "node" node)
                    (return node))))
                (xt.lang.spec-base/x:set-key globalThis __SHARED_KEY__ shared)))
            (:= (. globalThis ["onconnect"])
                (fn [e]
                  (var port (. e ["ports"] [0]))
                  (. port (start))
                  (xt.lang.spec-promise/x:promise-then
                   (. shared ["ready"])
                   (fn [node]
                     (var idx (+ 1 (or (. shared ["counter"]) 0)))
                     (xt.lang.spec-base/x:set-key shared "counter" idx)
                     (return
                      (xt.substrate.transport-browser/boot-self
                       node
                       {"transport_id" (xt.lang.spec-base/x:cat __TRANSPORT_PREFIX__ idx)
                        "target" port
                        "ready" __READY__})))))))
         form             (walk/postwalk-replace {'__NODE_INIT__ node-init
                                                 '__SHARED_KEY__ shared-key
                                                 '__STATE__ state
                                                 '__READY__ ready
                                                 '__TRANSPORT_PREFIX__ transport-prefix}
                                                template)]
     (l/emit-script form
                    {:lang :js
                     :layout layout}))))

(l/script :js
  {:require [[xt.db.node :as db-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-browser :as browser-transport]]})

(defn.js request
  "Sends a remote request through an attached SharedWorker transport."
  {:added "4.1"}
  [browser-node transport-id space action payload]
  (return
   (promise/x:promise-catch
    (promise/x:promise-then
     (event-node/request browser-node
                         space
                         action
                         [payload]
                         {"transport_id" transport-id})
     (fn [response]
       (var kind (xt/x:get-key response "kind"))
       (var status (xt/x:get-key response "status"))
       (cond (not (== kind "response"))
             (return response)

             (== "ok" status)
             (return (xt/x:get-key response "data"))

             :else
             (xt/x:throw (or (xt/x:get-key response "error")
                             response)))))
    (fn [err]
      (xt/x:throw (or (xt/x:get-key err "error")
                      err))))))

(defn.js connect
  "Connects a browser node to a SharedWorker and returns a session map."
  {:added "4.1"}
  [browser-node opts]
  (var config (or opts {}))
  (return
   (promise/x:promise-then
    (browser-transport/connect-sharedworker browser-node config)
    (fn [conn]
      (return
       {"node" browser-node
        "conn" conn
        "ready" (. conn ["ready"])
        "transport_id" (. conn ["transport_id"])
        "shared_space" (xt/x:get-key config "shared_space")})))))

(defn.js disconnect
  "Disconnects a SharedWorker session."
  {:added "4.1"}
  [session]
  (return
   (browser-transport/disconnect
    (xt/x:get-key session "conn"))))

(defn.js ensure-model
  "Ensures the shared space has a materialized, synced model."
  {:added "4.1"}
  [session model-id model-spec]
  (var browser-node (xt/x:get-key session "node"))
  (var transport-id (xt/x:get-key session "transport_id"))
  (var shared-space (xt/x:get-key session "shared_space"))
  (return
   (-> (-/request browser-node
                  transport-id
                  shared-space
                  db-node/ACTION_MODEL_PUT
                  {"model_id" model-id
                   "model_spec" model-spec})
       (promise/x:promise-then
        (fn [_]
          (return
           (-/request browser-node
                      transport-id
                      shared-space
                      db-node/ACTION_MODEL_MATERIALIZE
                      {"model_id" model-id}))))
       (promise/x:promise-then
        (fn [_]
          (return
           (-/request browser-node
                      transport-id
                      shared-space
                      db-node/ACTION_MODEL_SYNC
                      {"model_id" model-id})))))))

(defn.js query-view
  "Queries a model view in a specific space."
  {:added "4.1"}
  [session space model-id view-id args]
  (return
   (-/request (xt/x:get-key session "node")
              (xt/x:get-key session "transport_id")
              space
              db-node/ACTION_QUERY
              {"view" {"model_id" model-id
                       "view_id" view-id
                       "args" (or args [])}})))

(defn.js open-tab
  "Creates a tab-local model, shares sources from the shared space, and queries list/detail."
  {:added "4.1"}
  [session tab-space model-id model-spec detail-name]
  (var browser-node (xt/x:get-key session "node"))
  (var transport-id (xt/x:get-key session "transport_id"))
  (var shared-space (xt/x:get-key session "shared_space"))
  (return
   (-> (-/request browser-node
                  transport-id
                  tab-space
                  db-node/ACTION_MODEL_PUT
                  {"model_id" model-id
                   "model_spec" model-spec})
       (promise/x:promise-then
        (fn [_]
          (return
           (-/request browser-node
                      transport-id
                      tab-space
                      db-node/ACTION_SOURCE_SHARE
                      {"model_id" model-id
                       "from_space" shared-space
                       "source_ids" ["primary" "caching"]}))))
       (promise/x:promise-then
        (fn [_]
          (return
           (-/request browser-node
                      transport-id
                      tab-space
                      db-node/ACTION_MODEL_SYNC
                      {"model_id" model-id}))))
       (promise/x:promise-then
        (fn [_]
          (return
           (promise/x:promise-all
            [(-/query-view session
                           tab-space
                           model-id
                           "list"
                           [])
             (-/query-view session
                           tab-space
                           model-id
                           "detail"
                           [detail-name])]))))
       (promise/x:promise-then
        (fn [[list-view detail-view]]
          (return
           {"space_id" tab-space
            "list_count" (xt/x:len (xt/x:get-key list-view "value"))
            "list_source" (xt/x:get-key list-view "source")
            "detail_name" (xtd/get-in (xt/x:get-key detail-view "value") [0 "name"])
            "cached_first" (xtd/get-in (xt/x:get-key list-view "value") [0 "name"])}))))))

(defn.js node-summary
  "Fetches a remote node summary from the shared space."
  {:added "4.1"}
  [session]
  (return
   (-/request (xt/x:get-key session "node")
              (xt/x:get-key session "transport_id")
              (xt/x:get-key session "shared_space")
              db-node/ACTION_NODE_SUMMARY
              {})))
