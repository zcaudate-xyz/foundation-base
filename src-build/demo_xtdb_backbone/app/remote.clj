(ns demo-xtdb-backbone.app.remote
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[demo-xtdb-backbone.app.backbone :as backbone]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-model :as page-model]
             [xt.substrate.transport-browser :as browser-transport]]})

(defn.js default-session-config
  []
  (return
   {"browser_node_id" "demo-xtdb-backbone-webapp"
    "shared_space" "demo/shared"
    "model_id" "demo-xtdb-backbone/summary"
    "model_spec" nil}))

(defn.js merged-session-config
  [opts]
  (return
   (xtd/obj-assign-nested
    (xtd/obj-assign-nested {} (-/default-session-config))
    (or opts {}))))

(defn.js create-browser-node
  [opts]
  (var config (-/merged-session-config opts))
  (return
   (substrate/node-create
    {"id" (or (xt/x:get-key config "browser_node_id")
              "demo-xtdb-backbone-webapp")})))

(defn.js create-page-node
  []
  (return (substrate/node-create {"id" "demo-xtdb-backbone-page-node"})))

(defn.js install-demo-models
  [node space-id]
  (var specs (backbone/page-model-specs))
  (page-model/model-put node space-id "ping" (xt/x:get-key specs "ping"))
  (page-model/model-put node space-id "log_append" (xt/x:get-key specs "log_append"))
  (return node))

(defn.js connect-session
  [shared-worker opts]
  (var config (-/merged-session-config opts))
  (var browser-node (-/create-browser-node config))
  (var shared-space (xt/x:get-key config "shared_space"))
  (return
   (-> (browser-transport/connect-sharedworker
        browser-node
        {"transport_id" "worker"
         "sharedworker" shared-worker})
       (promise/x:promise-then
        (fn [conn]
          (return {"node" browser-node
                   "conn" conn
                   "ready" (. conn ["ready"])
                   "transport_id" "worker"
                   "shared_space" shared-space}))))))

(defn.js disconnect-session
  [session]
  (return
   (browser-transport/disconnect
    (xt/x:get-key session "conn"))))

(defn.js node-summary
  [session]
  (return
   (promise/x:promise-catch
    (promise/x:promise-then
     (substrate/request (xt/x:get-key session "node")
                        (xt/x:get-key session "shared_space")
                        "@xt.db/node-summary"
                        []
                        {"transport_id" (xt/x:get-key session "transport_id")})
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

(defn.js bootstrap
  [shared-worker opts]
  (var config (-/merged-session-config opts))
  (return
   (-> (-/connect-session shared-worker config)
       (promise/x:promise-then
        (fn [session]
          (return
           (-> (-/node-summary session)
               (promise/x:promise-then
                (fn [summary]
                  (return
                   (-> (-/disconnect-session session)
                       (promise/x:promise-then
                        (fn [_]
                          (return {"shared_space" (xt/x:get-key session "shared_space")
                                   "transport_id" (xt/x:get-key session "transport_id")
                                   "summary" summary}))))))))))))))

(defn.js set-view-input
  [node space-id model-id view-id input]
  (return (page-model/view-set-input node space-id model-id view-id input)))

(defn.js refresh-page-view
  [node space-id model-id view-id]
  (return (page-model/view-refresh node space-id model-id view-id)))

(defn.js remote-api
  []
  (return
   {"defaultSessionConfig" -/default-session-config
    "createBrowserNode" -/create-browser-node
    "createPageNode" -/create-page-node
    "pageModelSpecs" backbone/page-model-specs
    "sharedworkerConfig" backbone/sharedworker-config
    "installDemoModels" -/install-demo-models
    "connectSession" -/connect-session
    "disconnectSession" -/disconnect-session
    "bootstrap" -/bootstrap
    "setViewInput" -/set-view-input
    "refreshPageView" -/refresh-page-view}))

(defn.js install-global
  []
  (var api (-/remote-api))
  (xt/x:set-key globalThis "__DEMO_XTDB_BACKBONE__" api)
  (return api))
