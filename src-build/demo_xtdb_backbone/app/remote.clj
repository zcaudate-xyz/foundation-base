(ns demo-xtdb-backbone.app.remote
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[demo-xtdb-backbone.app.backbone :as backbone]
             [js.worker.sharedworker :as sharedworker]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.page-model :as page-model]]})

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
   (event-node/node-create
    {"id" (or (xt/x:get-key config "browser_node_id")
              "demo-xtdb-backbone-webapp")})))

(defn.js create-page-node
  []
  (return (event-node/node-create {"id" "demo-xtdb-backbone-page-node"})))

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
  (return
   (sharedworker/connect
    browser-node
    (xtd/obj-assign-nested config
                           {"sharedworker" shared-worker}))))

(defn.js disconnect-session
  [session]
  (return
   (sharedworker/disconnect session)))

(defn.js bootstrap
  [shared-worker opts]
  (var config (-/merged-session-config opts))
  (return
   (promise/x:promise-then
    (-/connect-session shared-worker config)
    (fn [session]
      (return
       (promise/x:promise-then
        (sharedworker/node-summary session)
        (fn [summary]
          (return
           (promise/x:promise-then
            (-/disconnect-session session)
            (fn [_]
              (return {"shared_space" (xt/x:get-key session "shared_space")
                       "transport_id" (xt/x:get-key session "transport_id")
                       "summary" summary})))))))))))

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
