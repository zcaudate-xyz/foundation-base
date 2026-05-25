(ns xt.substrate.page-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.event.base-view :as event-view]
             [xt.substrate.page-spec :as page-spec]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

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

(defn.xt get-view-deps
  "gets normalized dependencies for a single view"
  {:added "4.1"}
  [model-id view]
  (var out-views [])
  (var out-state [])
  (var deps (page-spec/view-deps view))
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

(defn.xt get-model-deps
  "gets view dependency indexes for a model"
  {:added "4.1"}
  [model-id views]
  (var all-deps {})
  (xt/for:object [[view-id view-entry] views]
    (xt/for:array [path (xt/x:get-key (-/get-view-deps model-id view-entry) "views")]
      (xtd/set-in all-deps
                  [(xt/x:first path)
                   (xt/x:second path)
                   view-id]
                  true)))
  (return all-deps))

(defn.xt get-model-state-deps
  "gets state dependency indexes for a model"
  {:added "4.1"}
  [views]
  (var out {})
  (xt/for:object [[view-id view-entry] views]
    (xt/for:array [path (xt/x:get-key (-/get-view-deps nil view-entry) "state")]
      (xtd/set-in out
                  [(-/state-path-key path) view-id]
                  true)))
  (return out))

(defn.xt get-unknown-deps
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

(defn.xt base-state
  "creates the base page model state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" page-spec/STATE_TAG
           "models" {}
           "meta" (or (xt/x:get-key opts "meta") {})
           "opts" opts}))

(defn.xt model-views
  "normalizes a model input into a view map"
  {:added "4.1"}
  [model-spec]
  (return (page-spec/model-views model-spec)))

(defn.xt normalize-view
  "normalizes a single view record"
  {:added "4.1"}
  [view-id view]
  (:= view (xtd/clone-nested (or view {})))
  (var default_input (page-spec/view-default-input view))
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
                default_input
                default-value
                -/output-process
                nil))
  (event-view/init-view runtime)
  (when (> (xt/x:len default_input) 0)
    (event-view/set-input runtime {"data" default_input}))
  (when (xt/x:not-nil? default-value)
    (xtd/set-in runtime ["output" "current"] default-value))
  (xt/x:obj-assign runtime carry)
  (xt/x:set-key runtime "id" view-id)
  (xt/x:set-key runtime "value" default-value)
  (xt/x:set-key runtime "status" page-spec/STATUS_IDLE)
  (xt/x:set-key runtime "pending" false)
  (xt/x:set-key runtime "error" nil)
  (xt/x:set-key runtime "updated_at" nil)
  (return runtime))

(defn.xt normalize-model
  "normalizes a single model record"
  {:added "4.1"}
  [model-id model-spec]
  (var views {})
  (xt/for:object [[view-id view] (-/model-views model-spec)]
    (xt/x:set-key views view-id (-/normalize-view view-id view)))
  (var model {"id" model-id
              "meta" (or (xt/x:get-key model-spec "meta") {})
              "state" (xtd/clone-nested (page-spec/model-state model-spec))
              "actions" (xtd/clone-nested (page-spec/model-actions model-spec))
              "views" views
              "deps" {}
              "state_deps" {}
              "unknown_deps" []})
  (xt/x:set-key model "deps" (-/get-model-deps model-id views))
  (xt/x:set-key model "state_deps" (-/get-model-state-deps views))
  (xt/x:set-key model "unknown_deps" (-/get-unknown-deps {"models" {model-id model}}
                                                         model-id
                                                         views
                                                         (xt/x:get-key model "deps")))
  (return model))

(defn.xt get-model
  "gets a registered model"
  {:added "4.1"}
  [state model-id]
  (return (xtd/get-in state ["models" model-id])))

(defn.xt ensure-model
  "gets a registered model or throws"
  {:added "4.1"}
  [state model-id]
  (var model (-/get-model state model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "model not found - " model-id)))
  (return model))

(defn.xt get-view
  "gets a registered view"
  {:added "4.1"}
  [state model-id view-id]
  (return (xtd/get-in state ["models" model-id "views" view-id])))

(defn.xt ensure-view
  "gets a registered view or throws"
  {:added "4.1"}
  [state model-id view-id]
  (var view (-/get-view state model-id view-id))
  (when (xt/x:nil? view)
    (xt/x:err (xt/x:cat "view not found - " (xt/x:json-encode [model-id view-id]))))
  (return view))

(defn.xt rebuild-model
  "recomputes dependency indexes for a model"
  {:added "4.1"}
  [state model-id]
  (var model (-/ensure-model state model-id))
  (var views (or (xt/x:get-key model "views") {}))
  (var deps (-/get-model-deps model-id views))
  (xt/x:set-key model "deps" deps)
  (xt/x:set-key model "state_deps" (-/get-model-state-deps views))
  (xt/x:set-key model
                "unknown_deps"
                (-/get-unknown-deps state model-id views deps))
  (return model))

(defn.xt put-model
  "stores a model and normalizes its views"
  {:added "4.1"}
  [state model-id model-spec]
  (var model (-/normalize-model model-id model-spec))
  (xtd/set-in state ["models" model-id] model)
  (-/rebuild-model state model-id)
  (return model))

(defn.xt put-view
  "stores a single view on an existing model"
  {:added "4.1"}
  [state model-id view-id view-spec]
  (var model (-/ensure-model state model-id))
  (xtd/set-in model ["views" view-id] (-/normalize-view view-id view-spec))
  (-/rebuild-model state model-id)
  (return (xtd/get-in model ["views" view-id])))

(defn.xt get-model-state
  "gets model local state or a nested path"
  {:added "4.1"}
  [state model-id path]
  (var model (-/ensure-model state model-id))
  (if (xt/x:nil? path)
    (return (xt/x:get-key model "state"))
    (return (xtd/get-in model
                        ["state" (or path [])]))))

(defn.xt set-model-state
  "sets model local state for a path"
  {:added "4.1"}
  [state model-id path value]
  (var model (-/ensure-model state model-id))
  (if (and (xt/x:is-array? path)
           (> (xt/x:len path) 0))
    (xtd/set-in model
                ["state" path]
                value)
    (xt/x:set-key model "state" (or value {})))
  (return model))

(defn.xt set-view-input
  "sets input on a view"
  {:added "4.1"}
  [state model-id view-id input]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "input" (or input []))
  (return view))

(defn.xt set-view-ready
  "marks a view as ready without changing its value"
  {:added "4.1"}
  [state model-id view-id]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" "ready")
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt set-view-error
  "marks a view as errored"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" "error")
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" error)
  (return view))

(defn.xt set-view-value
  "stores a view value"
  {:added "4.1"}
  [state model-id view-id source-id value]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "value" value)
  (when (xt/x:not-nil? source-id)
    (xt/x:set-key view "source" source-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" "ready")
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt get-view-dependents
  "gets dependent views for a source view"
  {:added "4.1"}
  [state model-id view-id]
  (var out {})
  (xt/for:object [[dmodel-id model] (or (xt/x:get-key state "models") {})]
    (var view-lu (xtd/get-in model ["deps" model-id view-id]))
    (when (xt/x:not-nil? view-lu)
      (xt/x:set-key out dmodel-id (xt/x:obj-keys view-lu))))
  (return out))

(defn.xt get-state-dependents
  "gets dependent views for a model state path"
  {:added "4.1"}
  [state model-id path]
  (var model (-/ensure-model state model-id))
  (return (xt/x:obj-keys (or (xtd/get-in model ["state_deps" (-/state-path-key path)])
                             {}))))

(defn.xt snapshot-state
  "returns a snapshot of the current page state"
  {:added "4.1"}
  [state]
  (return {"models" (xt/x:get-key state "models")
           "meta" (xt/x:get-key state "meta")}))
