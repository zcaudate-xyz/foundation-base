(ns xt.substrate.page-model
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.substrate :as event-node]
             [xt.event.base-view :as event-view]
             [xt.event.util-throttle :as th]
             [xt.substrate.base-space :as node-space]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]]})

(def$.xt STATE_TAG "substrate.page.state")

(def$.xt STATUS_IDLE "idle")
(def$.xt STATUS_PENDING "pending")
(def$.xt STATUS_READY "ready")
(def$.xt STATUS_ERROR "error")

(def$.xt RESOLVER_TYPE_LOCAL "fn/local")
(def$.xt RESOLVER_TYPE_CUSTOM "fn/custom")
(def$.xt RESOLVER_TYPE_API "fn/api")

(defn.xt model-spec-state
  "gets model state from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "state")
              {})))

(defn.xt model-spec-views
  "gets model views from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "views")
              model-spec
              {})))

(defn.xt model-spec-actions
  "gets model actions from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "actions")
              {})))

(defn.xt view-spec-default-input
  "gets the default input for a view"
  {:added "4.1"}
  [view-spec]
  (return (or (xt/x:get-key view-spec "input")
              (xt/x:get-key view-spec "default_input")
              [])))

(defn.xt view-spec-resolver
  "gets the resolver for a view"
  {:added "4.1"}
  [view-spec]
  (return (or (xt/x:get-key view-spec "resolver")
              {})))

(defn.xt view-spec-deps
  "gets normalized dependency groups from a view spec"
  {:added "4.1"}
  [view-spec]
  (var deps (or (xt/x:get-key view-spec "deps") []))
  (return (:? (xt/x:is-object? deps)
              {"views" (or (xt/x:get-key deps "views") [])
               "state" (or (xt/x:get-key deps "state") [])}
              {"views" deps
               "state" []})))

(defn.xt resolver-spec-type
  "gets the resolver type"
  {:added "4.1"}
  [resolver]
  (return (or (xt/x:get-key resolver "type")
              -/RESOLVER_TYPE_LOCAL)))

(defn.xt resolver-spec-fn
  "gets the executable function for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "fn")))

(defn.xt resolver-spec-args-fn
  "gets the argument builder for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "args_fn")))

(defn.xt resolver-spec-action
  "gets the action for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "action")))

(defn.xt resolver-spec-target
  "gets the target for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "target")))

(defn.xt resolver-spec-meta
  "gets request meta for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (or (xt/x:get-key resolver "meta")
              {})))

(defn.xt resolver-spec-service
  "gets the configured service reference for a custom resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "service")))

(defn.xt resolver-spec-api-template
  "gets the api template reference or inline template for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "api_template")))

(defn.xt resolver-spec-args
  "gets args for a resolver using snake_case keys"
  {:added "4.1"}
  [resolver ctx]
  (var args-fn (-/resolver-spec-args-fn resolver))
  (if (xt/x:is-function? args-fn)
    (return (or (args-fn ctx) []))
    (return (or (xt/x:get-key resolver "args")
                (xt/x:get-key ctx "input")
                []))))

(defn.xt resolver-spec-trigger-pre
  "gets the pre trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "trigger.pre")))

(defn.xt resolver-spec-trigger-post
  "gets the post trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "trigger.post")))

(defn.xt identity-wrapper
  "passes through a context-aware handler"
  {:added "4.1"}
  [handler]
  (return handler))

(defn.xt output-process
  "extracts the public value from a result payload"
  {:added "4.1"}
  [value]
  (if (and (xt/x:is-object? value)
           (xt/x:has-key? value "value"))
    (return (xt/x:get-key value "value"))
    (return value)))

(defn.xt normalize-view-dep
  "normalizes a dependency path into [model-id view-id]"
  {:added "4.1"}
  [model-id dep]
  (cond (xt/x:is-array? dep)
        (if (== 1 (xt/x:len dep))
          (return [model-id
                   (xt/x:first dep)])
          (return [(xt/x:first dep)
                   (xt/x:second dep)]))

        (xt/x:is-string? dep)
        (return [model-id dep])

        (xt/x:is-object? dep)
        (return [(or (xt/x:get-key dep "model")
                     model-id)
                 (or (xt/x:get-key dep "view")
                     (xt/x:get-key dep "id")
                     (xt/x:get-key dep "name"))])

        :else
        (return nil)))

(defn.xt normalize-state-dep
  "normalizes a state dependency into a path array"
  {:added "4.1"}
  [dep]
  (cond (xt/x:is-array? dep)
        (return dep)

        (xt/x:is-string? dep)
        (return [dep])

        (xt/x:is-object? dep)
        (return (or (xt/x:get-key dep "path")
                    (:? (xt/x:not-nil? (xt/x:get-key dep "key"))
                        [(xt/x:get-key dep "key")]
                        (:? (xt/x:not-nil? (xt/x:get-key dep "id"))
                            [(xt/x:get-key dep "id")]
                            nil))))

        :else
        (return nil)))

(defn.xt state-get-view-deps
  "gets normalized dependencies for a single view"
  {:added "4.1"}
  [model-id view]
  (var out-views [])
  (var out-state [])
  (var deps (-/view-spec-deps view))
  (var raw-views (xt/x:get-key deps "views"))
  (var raw-state (xt/x:get-key deps "state"))
  (xt/for:array [dep (or raw-views [])]
    (var path (-/normalize-view-dep model-id dep))
    (when (and (xt/x:not-nil? path)
               (xt/x:not-nil? (xt/x:first path))
               (xt/x:not-nil? (xt/x:second path)))
      (xt/x:arr-push out-views path)))
  (xt/for:array [dep (or raw-state [])]
    (var path (-/normalize-state-dep dep))
    (when (and (xt/x:not-nil? path)
               (> (xt/x:len path) 0))
      (xt/x:arr-push out-state path)))
  (return {"views" out-views
           "state" out-state}))

(defn.xt state-path-key
  "builds a stable key for a state dependency path"
  {:added "4.1"}
  [path]
  (return (xt/x:json-encode path)))

(defn.xt state-get-model-deps
  "gets view dependency indexes for a model"
  {:added "4.1"}
  [model-id views]
  (var all-deps {})
  (xt/for:object [[view-id view-entry] views]
    (xt/for:array [path (xt/x:get-key (-/state-get-view-deps model-id view-entry) "views")]
      (xtd/set-in all-deps
                  [(xt/x:first path)
                   (xt/x:second path)
                   view-id]
                  true)))
  (return all-deps))

(defn.xt state-get-model-state-deps
  "gets state dependency indexes for a model"
  {:added "4.1"}
  [views]
  (var out {})
  (xt/for:object [[view-id view-entry] views]
    (xt/for:array [path (xt/x:get-key (-/state-get-view-deps nil view-entry) "state")]
      (xtd/set-in out
                  [(-/state-path-key path) view-id]
                  true)))
  (return out))

(defn.xt state-get-unknown-deps
  "gets unresolved view dependency paths for a model"
  {:added "4.1"}
  [state model-id views model-deps]
  (var out [])
  (xt/for:object [[linked-model-id linked-views] model-deps]
    (cond (== model-id linked-model-id)
          (xt/for:object [[linked-view-id _] linked-views]
            (when (xt/x:nil? (xt/x:get-key views linked-view-id))
              (xt/x:arr-push out [linked-model-id linked-view-id])))

          :else
          (do (var linked-model (xtd/get-in state ["models" linked-model-id]))
              (xt/for:object [[linked-view-id _] linked-views]
                (when (or (xt/x:nil? linked-model)
                          (xt/x:nil? (xt/x:get-key (xt/x:get-key linked-model "views")
                                                   linked-view-id)))
                  (xt/x:arr-push out [linked-model-id linked-view-id]))))))
  (return out))

(defn.xt state-base
  "creates the base page model state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" -/STATE_TAG
           "models" {}
           "meta" (or (xt/x:get-key opts "meta") {})
           "opts" opts}))

(defn.xt state-normalize-view
  "normalizes a single view record"
  {:added "4.1"}
  [view-id view]
  (:= view (xtd/clone-nested (or view {})))
  (var default-input (-/view-spec-default-input view))
  (var default-value (xt/x:get-key view "value"))
  (var carry (xtd/obj-clone view))
  (xt/x:del-key carry "input")
  (xt/x:del-key carry "value")
  (xt/x:del-key carry "default_input")
  (var runtime (event-view/create-view
                nil
                {"main" {"wrapper" -/identity-wrapper}
                 "remote" {"wrapper" -/identity-wrapper}
                 "sync" {"wrapper" -/identity-wrapper}}
                default-input
                default-value
                -/output-process
                nil))
  (event-view/init-view runtime)
  (when (> (xt/x:len default-input) 0)
    (event-view/set-input runtime {"data" default-input}))
  (when (xt/x:not-nil? default-value)
    (xtd/set-in runtime ["output" "current"] default-value))
  (xt/x:obj-assign runtime carry)
  (xt/x:set-key runtime "id" view-id)
  (xt/x:set-key runtime "value" default-value)
  (xt/x:set-key runtime "status" -/STATUS_IDLE)
  (xt/x:set-key runtime "pending" false)
  (xt/x:set-key runtime "error" nil)
  (xt/x:set-key runtime "updated_at" nil)
  (return runtime))

(defn.xt state-normalize-model
  "normalizes a single model record"
  {:added "4.1"}
  [model-id model-spec]
  (var views {})
  (xt/for:object [[view-id view] (-/model-spec-views model-spec)]
    (xt/x:set-key views view-id (-/state-normalize-view view-id view)))
  (var model {"id" model-id
              "meta" (or (xt/x:get-key model-spec "meta") {})
              "state" (xtd/clone-nested (-/model-spec-state model-spec))
              "actions" (xtd/clone-nested (-/model-spec-actions model-spec))
              "views" views
              "deps" {}
              "state_deps" {}
              "unknown_deps" []})
  (xt/x:set-key model "deps" (-/state-get-model-deps model-id views))
  (xt/x:set-key model "state_deps" (-/state-get-model-state-deps views))
  (xt/x:set-key model "unknown_deps" (-/state-get-unknown-deps {"models" {model-id model}}
                                                               model-id
                                                               views
                                                               (xt/x:get-key model "deps")))
  (return model))

(defn.xt state-get-model
  "gets a registered model"
  {:added "4.1"}
  [state model-id]
  (return (xtd/get-in state ["models" model-id])))

(defn.xt state-ensure-model
  "gets a registered model or throws"
  {:added "4.1"}
  [state model-id]
  (var model (-/state-get-model state model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "model not found - " model-id)))
  (return model))

(defn.xt state-get-view
  "gets a registered view"
  {:added "4.1"}
  [state model-id view-id]
  (return (xtd/get-in state ["models" model-id "views" view-id])))

(defn.xt state-ensure-view
  "gets a registered view or throws"
  {:added "4.1"}
  [state model-id view-id]
  (var view (-/state-get-view state model-id view-id))
  (when (xt/x:nil? view)
    (xt/x:err (xt/x:cat "view not found - " (xt/x:json-encode [model-id view-id]))))
  (return view))

(defn.xt state-rebuild-model
  "recomputes dependency indexes for a model"
  {:added "4.1"}
  [state model-id]
  (var model (-/state-ensure-model state model-id))
  (var views (or (xt/x:get-key model "views") {}))
  (var deps (-/state-get-model-deps model-id views))
  (xt/x:set-key model "deps" deps)
  (xt/x:set-key model "state_deps" (-/state-get-model-state-deps views))
  (xt/x:set-key model
                "unknown_deps"
                (-/state-get-unknown-deps state model-id views deps))
  (return model))

(defn.xt state-put-model
  "stores a model and normalizes its views"
  {:added "4.1"}
  [state model-id model-spec]
  (var model (-/state-normalize-model model-id model-spec))
  (xtd/set-in state ["models" model-id] model)
  (-/state-rebuild-model state model-id)
  (return model))

(defn.xt state-put-view
  "stores a single view on an existing model"
  {:added "4.1"}
  [state model-id view-id view-spec]
  (var model (-/state-ensure-model state model-id))
  (xtd/set-in model ["views" view-id] (-/state-normalize-view view-id view-spec))
  (-/state-rebuild-model state model-id)
  (return (xtd/get-in model ["views" view-id])))

(defn.xt state-set-model-state
  "sets model local state for a path"
  {:added "4.1"}
  [state model-id path value]
  (var model (-/state-ensure-model state model-id))
  (if (and (xt/x:is-array? path)
           (> (xt/x:len path) 0))
    (xtd/set-in model
                ["state" path]
                value)
    (xt/x:set-key model "state" (or value {})))
  (return model))

(defn.xt state-set-view-input
  "sets input on a view"
  {:added "4.1"}
  [state model-id view-id input]
  (var view (-/state-ensure-view state model-id view-id))
  (event-view/set-input view {"data" (or input [])})
  (return view))

(defn.xt state-set-view-error
  "marks a view as errored"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (-/state-ensure-view state model-id view-id))
  (event-view/set-output view error true "resolver" nil nil)
  (event-view/set-pending view false nil)
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" -/STATUS_ERROR)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" error)
  (return view))

(defn.xt state-set-view-value
  "stores a view value"
  {:added "4.1"}
  [state model-id view-id source-id value]
  (var view (-/state-ensure-view state model-id view-id))
  (event-view/set-output view value false source-id nil nil)
  (event-view/set-pending view false nil)
  (xt/x:set-key view "value" value)
  (when (xt/x:not-nil? source-id)
    (xt/x:set-key view "source" source-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" -/STATUS_READY)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt state-get-view-dependents
  "gets dependent views for a source view"
  {:added "4.1"}
  [state model-id view-id]
  (var out {})
  (xt/for:object [[dmodel-id model] (or (xt/x:get-key state "models") {})]
    (var view-lu (xtd/get-in model ["deps" model-id view-id]))
    (when (xt/x:not-nil? view-lu)
      (xt/x:set-key out dmodel-id (xt/x:obj-keys view-lu))))
  (return out))

(defn.xt state-get-state-dependents
  "gets dependent views for a model state path"
  {:added "4.1"}
  [state model-id path]
  (var model (-/state-ensure-model state model-id))
  (return (xt/x:obj-keys (or (xtd/get-in model ["state_deps" (-/state-path-key path)])
                             {}))))

(defn.xt base-state
  "public wrapper for the generic page-model state shape"
  {:added "4.1"}
  [opts]
  (return (-/state-base opts)))

(defn.xt model-views
  "public wrapper for extracting model views"
  {:added "4.1"}
  [model-spec]
  (return (-/model-spec-views model-spec)))

(defn.xt normalize-view
  "public wrapper for normalizing a view runtime"
  {:added "4.1"}
  [view-id view]
  (return (-/state-normalize-view view-id view)))

(defn.xt get-view-deps
  "public wrapper for normalized view dependencies"
  {:added "4.1"}
  [model-id view]
  (return (xt/x:get-key (-/state-get-view-deps model-id view) "views")))

(defn.xt get-model-deps
  "public wrapper for model dependency indexes"
  {:added "4.1"}
  [model-id views]
  (return (-/state-get-model-deps model-id views)))

(defn.xt get-unknown-deps
  "public wrapper for unresolved dependency paths"
  {:added "4.1"}
  [state model-id views model-deps]
  (return (-/state-get-unknown-deps state model-id views model-deps)))

(defn.xt get-model
  "public wrapper for state-get-model"
  {:added "4.1"}
  [state model-id]
  (return (-/state-get-model state model-id)))

(defn.xt ensure-model
  "public wrapper for state-ensure-model"
  {:added "4.1"}
  [state model-id]
  (return (-/state-ensure-model state model-id)))

(defn.xt get-view
  "public wrapper for state-get-view"
  {:added "4.1"}
  [state model-id view-id]
  (return (-/state-get-view state model-id view-id)))

(defn.xt ensure-view
  "public wrapper for state-ensure-view"
  {:added "4.1"}
  [state model-id view-id]
  (return (-/state-ensure-view state model-id view-id)))

(defn.xt ensure-space-state
  "ensures the target node space has page model state"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (not (and (xt/x:not-nil? state)
                  (== (xt/x:get-key state "::") -/STATE_TAG)))
    (:= state (-/state-base nil))
    (xt/x:set-key space "state" state))
  (return state))

(defn.xt model-put
  "registers a model on a node space"
  {:added "4.1"}
  [node space-id model-id model-spec]
  (var state (-/ensure-space-state node space-id))
  (var model (-/state-put-model state model-id model-spec))
  (-/ensure-model-runtime node space-id model-id)
  (return model))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (var view (-/state-put-view state model-id view-id view-spec))
  (-/ensure-model-runtime node space-id model-id)
  (return view))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/state-get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/state-get-view state model-id view-id)))

(defn.xt model-set-state
  "sets model local state"
  {:added "4.1"}
  [node space-id model-id path value]
  (var state (-/ensure-space-state node space-id))
  (var model (-/state-set-model-state state model-id path value))
  (-/refresh-state-dependents node space-id model-id path)
  (return model))

(defn.xt view-set-input
  "sets view input"
  {:added "4.1"}
  [node space-id model-id view-id input]
  (var state (-/ensure-space-state node space-id))
  (-/state-set-view-input state model-id view-id input)
  (-/view-update node space-id model-id view-id {"type" "input"})
  (return (-/state-get-view state model-id view-id)))

(defn.xt view-refresh-result
  "returns the public refresh result from the current view state"
  {:added "4.1"}
  [view]
  (return {"value" (xt/x:get-key view "value")
           "status" (xt/x:get-key view "status")
           "error" (xt/x:get-key view "error")}))

(defn.xt resolver-pre-trigger
  "gets the pre trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (-/resolver-spec-trigger-pre resolver)))

(defn.xt resolver-post-trigger
  "gets the post trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (-/resolver-spec-trigger-post resolver)))

(defn.xt resolver-fn
  "gets the executable function for a resolver"
  {:added "4.1"}
  [resolver]
  (return (-/resolver-spec-fn resolver)))

(defn.xt resolver-service
  "gets the configured service reference for a resolver"
  {:added "4.1"}
  [resolver]
  (return (-/resolver-spec-service resolver)))

(defn.xt node-services
  "gets named services registered on the node"
  {:added "4.1"}
  [node]
  (return (event-node/get-services node)))

(defn.xt node-service
  "gets a named service registered on the node"
  {:added "4.1"}
  [node service-id]
  (return (event-node/get-service node service-id)))

(defn.xt service-fn
  "gets an executable function from a service entry"
  {:added "4.1"}
  [service]
  (cond (xt/x:is-function? service)
        (return service)

        (and (xt/x:is-object? service)
             (xt/x:is-function? (xt/x:get-key service "fn")))
        (return (xt/x:get-key service "fn"))

        :else
        (return nil)))

(defn.xt view-input
  "gets resolver input from the underlying event view"
  {:added "4.1"}
  [view]
  (return (or (xtd/get-in (event-view/get-input view) ["current" "data"])
              [])))

(defn.xt resolve-custom-handler
  "resolves a custom resolver handler from the resolver or node services"
  {:added "4.1"}
  [resolver ctx]
  (var exec (-/resolver-fn resolver))
  (when (xt/x:is-function? exec)
    (return exec))
  (var service-ref (-/resolver-service resolver))
  (cond (xt/x:is-function? service-ref)
        (return service-ref)

        (xt/x:is-object? service-ref)
        (return (-/service-fn service-ref))

        (xt/x:is-string? service-ref)
        (return (-/service-fn
                 (-/node-service (xt/x:get-key ctx "node")
                                 service-ref)))

        :else
        (return nil)))

(defn.xt create-view-context
  "creates the page resolver execution context"
  {:added "4.1"}
  [node space-id state model-id view-id event]
  (var model (-/state-ensure-model state model-id))
  (var view (-/state-ensure-view state model-id view-id))
  (var resolver (-/view-spec-resolver view))
  (return {"node" node
           "space_id" space-id
           "state" state
           "model_id" model-id
           "view_id" view-id
           "model" model
           "view" view
           "resolver" resolver
           "services" (-/node-services node)
           "event" (or event {})
           "input" (-/view-input view)}))

(defn.xt prep-view
  "prepares the current page view context"
  {:added "4.1"}
  [node space-id model-id view-id event]
  (var state (-/ensure-space-state node space-id))
  (var model (-/ensure-model-runtime node space-id model-id))
  (var view (-/state-ensure-view state model-id view-id))
  (var resolver (-/view-spec-resolver view))
  (return {"state" state
           "model" model
           "view" view
           "resolver" resolver
           "ctx" (-/create-view-context node space-id state model-id view-id event)}))

(defn.xt set-view-pending
  "marks a page view as pending"
  {:added "4.1"}
  [state model-id view-id]
  (var view (-/state-ensure-view state model-id view-id))
  (event-view/set-pending view true nil)
  (xt/x:set-key view "pending" true)
  (xt/x:set-key view "status" -/STATUS_PENDING)
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt get-view-dependents
  "gets dependent views for a page view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (return (-/state-get-view-dependents (-/ensure-space-state node space-id)
                                       model-id
                                       view-id)))

(defn.xt run-tail-call
  "runs dependent refreshes after a successful view refresh"
  {:added "4.1"}
  [node space-id model-id view-id view refresh-deps-fn]
  (when (and view
             (not (xt/x:not-nil? (xt/x:get-key view "error")))
             refresh-deps-fn)
    (refresh-deps-fn node
                     space-id
                     model-id
                     view-id))
  (return view))

(defn.xt create-throttle
  "creates a throttled refresher for a page model"
  {:added "4.1"}
  [node space-id model-id]
  (return
   (th/throttle-create
    (fn [view-id event]
      (return (-/refresh-view-raw node
                                  space-id
                                  model-id
                                  view-id
                                  event
                                  -/refresh-view-dependents)))
    xt/x:now-ms)))

(defn.xt ensure-model-runtime
  "ensures the page model has a throttle for refresh scheduling"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (-/state-ensure-model state model-id))
  (when (xt/x:nil? (xt/x:get-key model "throttle"))
    (xt/x:set-key model "throttle" (-/create-throttle node space-id model-id)))
  (return model))

(defn.xt node-api-templates
  "gets named api templates registered on the node"
  {:added "4.1"}
  [node]
  (return (or (xtd/get-in node ["meta" "api_templates"])
              {})))

(defn.xt default-api-template
  "gets the default api template registered on the node"
  {:added "4.1"}
  [node]
  (var raw (xtd/get-in node ["meta" "api_template"]))
  (cond (xt/x:nil? raw)
        (return (or (xtd/get-in node ["meta" "api_templates" "default"])
                    {}))

        (xt/x:is-object? raw)
        (return raw)

        (xt/x:is-string? raw)
        (return (or (xt/x:get-key (-/node-api-templates node) raw)
                    {}))

        :else
        (return {})))

(defn.xt merge-api-template
  "merges a resolver with its configured or default api template"
  {:added "4.1"}
  [resolver ctx]
  (var raw (-/resolver-spec-api-template resolver))
  (var node (xt/x:get-key ctx "node"))
  (var template nil)
  (cond (xt/x:nil? raw)
        (:= template (-/default-api-template node))

        (xt/x:is-object? raw)
        (:= template raw)

        (xt/x:is-string? raw)
        (do
          (:= template (xt/x:get-key (-/node-api-templates node) raw))
          (when (xt/x:nil? template)
            (return {"status" "error"
                     "tag" "substrate.page/api-template-not-found"
                     "data" {"api_template" raw}})))

        :else
        (return {"status" "error"
                 "tag" "substrate.page/api-template-invalid"
                 "data" {"api_template" raw}}))
  (var merged (xtd/clone-nested (or template {})))
  (xtd/obj-assign-nested merged resolver)
  (xt/x:del-key merged "api_template")
  (return merged))

(defn.xt apply-pre-trigger
  "applies a resolver pre trigger to a context"
  {:added "4.1"}
  [resolver ctx]
  (var trigger (-/resolver-pre-trigger resolver))
  (if (xt/x:is-function? trigger)
    (do (var out (trigger ctx))
        (return (:? (xt/x:is-object? out)
                    out
                    ctx)))
    (return ctx)))

(defn.xt apply-post-trigger
  "applies a resolver post trigger to a result"
  {:added "4.1"}
  [resolver ctx result]
  (var trigger (-/resolver-post-trigger resolver))
  (if (xt/x:is-function? trigger)
    (do (var out (trigger ctx result))
        (return (:? (xt/x:not-nil? out)
                    out
                    result)))
    (return result)))

(defn.xt finalize-result
  "applies post processing and updates the view state"
  {:added "4.1"}
  [state model-id view-id resolver ctx result]
  (:= result (-/apply-post-trigger resolver ctx result))
  (if (and (xt/x:is-object? result)
           (== (xt/x:get-key result "status") "error"))
    (do (-/state-set-view-error state model-id view-id result)
        (return (-/state-get-view state model-id view-id)))
    (do (-/state-set-view-value state model-id view-id "resolver" result)
        (return (-/state-get-view state model-id view-id)))))

(defn.xt run-api-resolver
  "runs an api resolver through substrate request"
  {:added "4.1"}
  [resolver ctx]
  (:= resolver (-/merge-api-template resolver ctx))
  (when (and (xt/x:is-object? resolver)
             (== (xt/x:get-key resolver "status") "error"))
    (return resolver))
  (var action (-/resolver-spec-action resolver))
  (when (xt/x:nil? action)
    (return {"status" "error"
             "tag" "substrate.page/resolver-action-not-found"
             "data" {"type" "fn/api"}}))
  (return
   (event-node/request
    (xt/x:get-key ctx "node")
    (-/resolver-spec-target resolver)
    action
    (-/resolver-spec-args resolver ctx)
    (-/resolver-spec-meta resolver))))

(defn.xt run-fn-resolver
  "runs a direct function resolver"
  {:added "4.1"}
  [resolver ctx type]
  (var exec (-/resolver-fn resolver))
  (if (xt/x:is-function? exec)
    (return (exec ctx))
    (return {"status" "error"
            "tag" "substrate.page/resolver-fn-not-found"
            "data" {"type" type}})))

(defn.xt run-custom-resolver
  "runs a custom resolver via node services or an inline function"
  {:added "4.1"}
  [resolver ctx]
  (var exec (-/resolve-custom-handler resolver ctx))
  (if (xt/x:is-function? exec)
    (return (exec ctx))
    (return {"status" "error"
            "tag" "substrate.page/resolver-service-not-found"
            "data" {"type" -/RESOLVER_TYPE_CUSTOM
                    "service" (-/resolver-service resolver)}})))

(defn.xt run-resolver
  "runs a resolver without coupling it to the view pipeline"
  {:added "4.1"}
  [resolver ctx]
  (var type (-/resolver-spec-type resolver))
  (cond (== type -/RESOLVER_TYPE_LOCAL)
       (return (-/run-fn-resolver resolver ctx type))

       (== type -/RESOLVER_TYPE_CUSTOM)
       (return (-/run-custom-resolver resolver ctx))

       (== type -/RESOLVER_TYPE_API)
       (return (-/run-api-resolver resolver ctx))

       :else
      (return (-/run-fn-resolver resolver ctx type))))

(defn.xt refresh-view-dependents
  "refreshes dependent views using each model throttle"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var dependents (-/get-view-dependents node space-id model-id view-id))
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (var model (-/ensure-model-runtime node space-id dmodel-id))
    (var throttle (xt/x:get-key model "throttle"))
    (xt/for:array [dview-id dview-ids]
    (th/throttle-run throttle dview-id [{"type" "dependency"
                                         "source" [model-id view-id]}])))
  (return dependents))

(defn.xt refresh-state-dependents
  "refreshes views that depend on a model state path"
  {:added "4.1"}
  [node space-id model-id path]
  (when (and (xt/x:is-array? path)
           (> (xt/x:len path) 0))
    (var dview-ids (-/state-get-state-dependents (-/ensure-space-state node space-id)
                                                 model-id
                                                 path))
    (var model (-/ensure-model-runtime node space-id model-id))
    (var throttle (xt/x:get-key model "throttle"))
    (xt/for:array [view-id dview-ids]
    (th/throttle-run throttle view-id [{"type" "state"
                                        "path" path}]))
    (return dview-ids))
  (return []))

(defn.xt refresh-view-raw
  "runs a view resolver and cascades refreshes"
  {:added "4.1"}
  [node space-id model-id view-id event refresh-deps-fn]
  (var prepared (-/prep-view node space-id model-id view-id event))
  (var state (xt/x:get-key prepared "state"))
  (var resolver (xt/x:get-key prepared "resolver"))
  (var ctx (xt/x:get-key prepared "ctx"))
  (-/set-view-pending state model-id view-id)
  (:= ctx (-/apply-pre-trigger resolver ctx))
  (var result (-/run-resolver resolver ctx))
  (var finalize
     (fn [output]
       (var view (-/finalize-result state model-id view-id resolver ctx output))
       (return (-/run-tail-call node
                                space-id
                                model-id
                                view-id
                                view
                                refresh-deps-fn))))
  (if (promise/x:promise-native? result)
    (return (promise/x:promise-then result finalize))
    (return (finalize result))))

(defn.xt model-update
  "refreshes all views in a model via the throttle"
  {:added "4.1"}
  [node space-id model-id event]
  (var model (-/ensure-model-runtime node space-id model-id))
  (var running [])
  (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
    (var entry (th/throttle-run (xt/x:get-key model "throttle")
                              view-id
                              [(or event {})]))
    (xt/x:arr-push running (xt/x:get-key entry "promise")))
  (return (promise/x:promise-all running)))

(defn.xt view-update
  "refreshes a single view through the model throttle"
  {:added "4.1"}
  [node space-id model-id view-id event]
  (var model (-/ensure-model-runtime node space-id model-id))
  (var entry (th/throttle-run (xt/x:get-key model "throttle")
                            view-id
                            [(or event {})]))
  (return (xt/x:get-key entry "promise")))

(defn.xt resolve-view
  "executes a generic page resolver"
  {:added "4.1"}
  [node space-id model-id view-id]
  (return (-/refresh-view-raw node
                            space-id
                            model-id
                            view-id
                            {}
                            -/refresh-view-dependents)))

(defn.xt view-refresh
  "refreshes a page view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var result (-/resolve-view node space-id model-id view-id))
  (if (promise/x:promise-native? result)
    (return
    (promise/x:promise-then
      result
      (fn [view]
        (return (-/view-refresh-result view)))))
    (return (-/view-refresh-result result))))
