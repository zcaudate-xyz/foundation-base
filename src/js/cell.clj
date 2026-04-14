(ns js.cell
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.common-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]
             [js.cell.kernel :as kernel]
             [js.cell.kernel.base-link-local :as base-link-local]
             [js.cell.kernel.worker-local :as worker-local]
             [js.cell.kernel.worker-state :as worker-state]]})

(defvar.js ^{:ns "@worker"}
  SERVICE
  "gets the current service registry"
  {:added "4.0"}
  []
  (return nil))

(defvar.js ^{:ns "@worker"}
  BINDINGS
  "gets the current bindings registry"
  {:added "4.0"}
  []
  (return {}))

(defn.js ^{:cell/action "@cell/setup-service"
           :cell/static true}
  fn-setup-service
  "stores the current worker service registry"
  {:added "4.0"}
  [service]
  (x:global-set __CELL_SERVICE service)
  (return (!:G __CELL_SERVICE)))

(defn.js ^{:cell/action "@cell/get-service"
           :cell/static true}
  fn-get-service
  "gets the current worker service registry"
  {:added "4.0"}
  []
  (return (:? (x:global-has? __CELL_SERVICE)
              (!:G __CELL_SERVICE)
              nil)))

(defn.js ^{:cell/action "@cell/setup-bindings"
           :cell/static true}
  fn-setup-bindings
  "stores the current worker bindings registry"
  {:added "4.0"}
  [bindings]
  (x:global-set __CELL_BINDINGS bindings)
  (return (!:G __CELL_BINDINGS)))

(defn.js ^{:cell/action "@cell/get-bindings"
           :cell/static true}
  fn-get-bindings
  "gets the current worker bindings registry"
  {:added "4.0"}
  []
  (return (:? (x:global-has? __CELL_BINDINGS)
              (!:G __CELL_BINDINGS)
              {})))

(defn.js actions-cell
  "returns the js.cell worker setup actions"
  {:added "4.0"}
  []
  (return
    (tab
     ["@cell/setup-service"
     {:handler -/fn-setup-service,
      :is-async false,
      :args ["service"]}]
     ["@cell/get-service"
     {:handler -/fn-get-service,
      :is-async false,
      :args []}]
     ["@cell/setup-bindings"
     {:handler -/fn-setup-bindings,
      :is-async false,
      :args ["bindings"]}]
     ["@cell/get-bindings"
     {:handler -/fn-get-bindings,
      :is-async false,
      :args []}])))

(defn.js actions-baseline
  "returns worker baseline actions with js.cell setup helpers"
  {:added "4.0"}
  []
  (return (k/obj-assign (worker-local/actions-baseline)
                        (-/actions-cell))))

(defn.js actions-init
  "initialises worker baseline actions with js.cell setup helpers"
  {:added "4.0"}
  [actions worker]
  (return (worker-local/actions-init (k/obj-assign (-/actions-cell)
                                                   (or actions {}))
                                     worker)))

(defn.js setup-service
  "sets the worker service registry over the cell or link"
  {:added "4.0"}
  [client service]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/setup-service"
                        :body [service]})))

(defn.js get-service
  "gets the worker service registry over the cell or link"
  {:added "4.0"}
  [client]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/get-service"
                        :body []})))

(defn.js setup-bindings
  "sets the worker bindings registry over the cell or link"
  {:added "4.0"}
  [client bindings]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/setup-bindings"
                        :body [bindings]})))

(defn.js get-bindings
  "gets the worker bindings registry over the cell or link"
  {:added "4.0"}
  [client]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/get-bindings"
                        :body []})))

(def.js make-cell kernel/make-cell)

(def.js GD kernel/GD)

(def.js GD-reset kernel/GD-reset)

(def.js GX kernel/GX)

(def.js GX-reset kernel/GX-reset)

(def.js GX-val kernel/GX-val)

(def.js GX-set kernel/GX-set)

(def.js get-cell kernel/get-cell)

(def.js call kernel/call)

(def.js fn-call-cell kernel/fn-call-cell)

(def.js fn-call-model kernel/fn-call-model)

(def.js fn-call-view kernel/fn-call-view)

(def.js fn-access-cell kernel/fn-access-cell)

(def.js fn-access-model kernel/fn-access-model)

(def.js fn-access-view kernel/fn-access-view)

(def.js list-models kernel/list-models)

(def.js list-views kernel/list-views)

(def.js get-model kernel/get-model)

(def.js get-view kernel/get-view)

(def.js cell-vals kernel/cell-vals)

(def.js cell-outputs kernel/cell-outputs)

(def.js cell-inputs kernel/cell-inputs)

(def.js cell-trigger kernel/cell-trigger)

(def.js model-outputs kernel/model-outputs)

(def.js model-vals kernel/model-vals)

(def.js model-is-errored kernel/model-is-errored)

(def.js model-is-pending kernel/model-is-pending)

(def.js add-model-attach kernel/add-model-attach)

(def.js add-model kernel/add-model)

(def.js remove-model kernel/remove-model)

(def.js model-update kernel/model-update)

(def.js model-trigger kernel/model-trigger)

(def.js view-success kernel/view-success)

(def.js view-val kernel/view-val)

(def.js view-get-input kernel/view-get-input)

(def.js view-get-output kernel/view-get-output)

(def.js view-set-val kernel/view-set-val)

(def.js view-get-time-updated kernel/view-get-time-updated)

(def.js view-is-errored kernel/view-is-errored)

(def.js view-is-pending kernel/view-is-pending)

(def.js view-get-time-elapsed kernel/view-get-time-elapsed)

(def.js view-set-input kernel/view-set-input)

(def.js view-refresh kernel/view-refresh)

(def.js view-update kernel/view-update)

(def.js view-ensure kernel/view-ensure)

(def.js view-call-remote kernel/view-call-remote)

(def.js view-refresh-remote kernel/view-refresh-remote)

(def.js view-trigger kernel/view-trigger)

(def.js view-for kernel/view-for)

(def.js view-for-input kernel/view-for-input)

(def.js get-val kernel/get-val)

(def.js get-for kernel/get-for)

(def.js nil-view kernel/nil-view)

(def.js nil-model kernel/nil-model)

(def.js clear-listeners kernel/clear-listeners)

(def.js add-listener kernel/add-listener)

(def.js remove-listener kernel/remove-listener)

(def.js list-listeners kernel/list-listeners)

(def.js list-all-listeners kernel/list-all-listeners)

(def.js add-raw-callback kernel/add-raw-callback)

(def.js remove-raw-callback kernel/remove-raw-callback)

(def.js list-raw-callbacks kernel/list-raw-callbacks)
