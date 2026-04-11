(ns xt.cell
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.base-runtime :as rt :with [defvar.xt]]
             [xt.lang.common-spec :as xt]
             [xt.cell.kernel :as kernel]
             [xt.cell.kernel.base-link-local :as base-link-local]
             [xt.cell.kernel.worker-local :as worker-local]
             [xt.cell.kernel.worker-state :as worker-state]]})

(defvar.xt ^{:ns "@worker"}
  SERVICE
  "gets the current service registry"
  {:added "4.0"}
  []
  (return nil))

(defvar.xt ^{:ns "@worker"}
  BINDINGS
  "gets the current bindings registry"
  {:added "4.0"}
  []
  (return {}))

(defn.xt ^{:cell/action "@cell/setup-service"
           :cell/static true}
  fn-setup-service
  "stores the current worker service registry"
  {:added "4.0"}
  [service]
  (xt/x:global-set __CELL_SERVICE service)
  (return (!:G __CELL_SERVICE)))

(defn.xt ^{:cell/action "@cell/get-service"
           :cell/static true}
  fn-get-service
  "gets the current worker service registry"
  {:added "4.0"}
  []
  (return (:? (xt/x:global-has? __CELL_SERVICE)
              (!:G __CELL_SERVICE)
              nil)))

(defn.xt ^{:cell/action "@cell/setup-bindings"
           :cell/static true}
  fn-setup-bindings
  "stores the current worker bindings registry"
  {:added "4.0"}
  [bindings]
  (xt/x:global-set __CELL_BINDINGS bindings)
  (return (!:G __CELL_BINDINGS)))

(defn.xt ^{:cell/action "@cell/get-bindings"
           :cell/static true}
  fn-get-bindings
  "gets the current worker bindings registry"
  {:added "4.0"}
  []
  (return (:? (xt/x:global-has? __CELL_BINDINGS)
              (!:G __CELL_BINDINGS)
              {})))

(defn.xt actions-cell
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

(defn.xt actions-baseline
  "returns worker baseline actions with js.cell setup helpers"
  {:added "4.0"}
  []
  (return (xt/x:obj-assign (worker-local/actions-baseline)
                        (-/actions-cell))))

(defn.xt actions-init
  "initialises worker baseline actions with js.cell setup helpers"
  {:added "4.0"}
  [actions worker]
  (return (worker-local/actions-init (xt/x:obj-assign (-/actions-cell)
                                                   (or actions {}))
                                     worker)))

(defn.xt setup-service
  "sets the worker service registry over the cell or link"
  {:added "4.0"}
  [client service]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/setup-service"
                        :body [service]})))

(defn.xt get-service
  "gets the worker service registry over the cell or link"
  {:added "4.0"}
  [client]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/get-service"
                        :body []})))

(defn.xt setup-bindings
  "sets the worker bindings registry over the cell or link"
  {:added "4.0"}
  [client bindings]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/setup-bindings"
                        :body [bindings]})))

(defn.xt get-bindings
  "gets the worker bindings registry over the cell or link"
  {:added "4.0"}
  [client]
  (return (kernel/call client
                       {:op "call"
                        :action "@cell/get-bindings"
                        :body []})))

(def.xt make-cell kernel/make-cell)

(def.xt GD kernel/GD)

(def.xt GD-reset kernel/GD-reset)

(def.xt GX kernel/GX)

(def.xt GX-reset kernel/GX-reset)

(def.xt GX-val kernel/GX-val)

(def.xt GX-set kernel/GX-set)

(def.xt get-cell kernel/get-cell)

(def.xt call kernel/call)

(def.xt fn-call-cell kernel/fn-call-cell)

(def.xt fn-call-model kernel/fn-call-model)

(def.xt fn-call-view kernel/fn-call-view)

(def.xt fn-access-cell kernel/fn-access-cell)

(def.xt fn-access-model kernel/fn-access-model)

(def.xt fn-access-view kernel/fn-access-view)

(def.xt list-models kernel/list-models)

(def.xt list-views kernel/list-views)

(def.xt get-model kernel/get-model)

(def.xt get-view kernel/get-view)

(def.xt cell-vals kernel/cell-vals)

(def.xt cell-outputs kernel/cell-outputs)

(def.xt cell-inputs kernel/cell-inputs)

(def.xt cell-trigger kernel/cell-trigger)

(def.xt model-outputs kernel/model-outputs)

(def.xt model-vals kernel/model-vals)

(def.xt model-is-errored kernel/model-is-errored)

(def.xt model-is-pending kernel/model-is-pending)

(def.xt add-model-attach kernel/add-model-attach)

(def.xt add-model kernel/add-model)

(def.xt remove-model kernel/remove-model)

(def.xt model-update kernel/model-update)

(def.xt model-trigger kernel/model-trigger)

(def.xt view-success kernel/view-success)

(def.xt view-val kernel/view-val)

(def.xt view-get-input kernel/view-get-input)

(def.xt view-get-output kernel/view-get-output)

(def.xt view-set-val kernel/view-set-val)

(def.xt view-get-time-updated kernel/view-get-time-updated)

(def.xt view-is-errored kernel/view-is-errored)

(def.xt view-is-pending kernel/view-is-pending)

(def.xt view-get-time-elapsed kernel/view-get-time-elapsed)

(def.xt view-set-input kernel/view-set-input)

(def.xt view-refresh kernel/view-refresh)

(def.xt view-update kernel/view-update)

(def.xt view-ensure kernel/view-ensure)

(def.xt view-call-remote kernel/view-call-remote)

(def.xt view-refresh-remote kernel/view-refresh-remote)

(def.xt view-trigger kernel/view-trigger)

(def.xt view-for kernel/view-for)

(def.xt view-for-input kernel/view-for-input)

(def.xt get-val kernel/get-val)

(def.xt get-for kernel/get-for)

(def.xt nil-view kernel/nil-view)

(def.xt nil-model kernel/nil-model)

(def.xt clear-listeners kernel/clear-listeners)

(def.xt add-listener kernel/add-listener)

(def.xt remove-listener kernel/remove-listener)

(def.xt list-listeners kernel/list-listeners)

(def.xt list-all-listeners kernel/list-all-listeners)

(def.xt add-raw-callback kernel/add-raw-callback)

(def.xt remove-raw-callback kernel/remove-raw-callback)

(def.xt list-raw-callbacks kernel/list-raw-callbacks)
