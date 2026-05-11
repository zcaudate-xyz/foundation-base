(ns xt.db.node.schema-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.event.base-view :as event-view]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt identity-wrapper
  "passes through a context-aware handler"
  {:added "4.1"}
  [handler]
  (return handler))

(defn.xt output-process
  "extracts the public view value from a db node result payload"
  {:added "4.1"}
  [value]
  (if (and (xt/x:is-object? value)
           (xt/x:has-key? value "value"))
    (return (xt/x:get-key value "value"))
    (return value)))

(defn.xt normalize-dep
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

(defn.xt get-view-deps
  "gets normalized dependencies for a single view"
  {:added "4.1"}
  [model-id view]
  (var out [])
  (xt/for:array [dep (or (xt/x:get-key view "deps") [])]
    (var path (-/normalize-dep model-id dep))
    (when (and (xt/x:not-nil? path)
               (xt/x:not-nil? (xt/x:first path))
               (xt/x:not-nil? (xt/x:second path)))
      (xt/x:arr-push out path)))
  (return out))

(defn.xt get-model-deps
  "gets view dependency indexes for a model"
  {:added "4.1"}
  [model-id views]
  (var all-deps {})
  (xt/for:object [[view-id view-entry] views]
    (xt/for:array [path (-/get-view-deps model-id view-entry)]
      (xtd/set-in all-deps
                  [(xt/x:first path)
                   (xt/x:second path)
                   view-id]
                  true)))
  (return all-deps))

(defn.xt get-unknown-deps
  "gets unresolved dependency paths for a model"
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
  "creates the base xt.db state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" spec/STATE_TAG
           :db nil
           :schema   (or (xt/x:get-key opts "schema") {})
           :lookup   (or (xt/x:get-key opts "lookup") {})
           :views    (or (xt/x:get-key opts "views") {})
           :queries  {}
           :models   {}
           :watch    {}
           :view_watch {}
           :pending  {}
           :remote   (or (xt/x:get-key opts "remote") {})
           :opts     opts
           :meta     (or (xt/x:get-key opts "meta") {})}))

(defn.xt get-schema
  "gets the configured schema"
  {:added "4.1"}
  [state]
  (return (or (xt/x:get-key state "schema")
              {})))

(defn.xt get-views
  "gets the configured view map"
  {:added "4.1"}
  [state]
  (return (or (xt/x:get-key state "views")
              {})))

(defn.xt model-views
  "normalizes a model input into a view map"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "views")
              model-spec
              {})))

(defn.xt normalize-view
  "normalizes a single view record"
  {:added "4.1"}
  [view-id view]
  (:= view (xtd/clone-nested (or view {})))
  (var default-input (or (xt/x:get-key view "input")
                         (xt/x:get-key view "default_input")
                         (xt/x:get-key view "defaultArgs")
                         (xt/x:get-key view "default_args")
                         []))
  (var default-value (xt/x:get-key view "value"))
  (var carry (xtd/obj-clone view))
  (xt/x:del-key carry "input")
  (xt/x:del-key carry "value")
  (var runtime (event-view/create-view
                nil
                {:main {:wrapper -/identity-wrapper}
                 :remote {:wrapper -/identity-wrapper}
                 :sync {:wrapper -/identity-wrapper}}
                default-input
                default-value
                -/output-process
                nil))
  (event-view/init-view runtime)
  (when (xt/x:not-nil? default-value)
    (xtd/set-in runtime ["output" "current"] default-value))
  (xt/x:obj-assign runtime carry)
  (xt/x:set-key runtime "id" view-id)
  (xt/x:set-key runtime "value" default-value)
  (xt/x:set-key runtime "status" spec/STATUS_IDLE)
  (xt/x:set-key runtime "pending" false)
  (xt/x:set-key runtime "error" nil)
  (xt/x:set-key runtime "tables" {})
  (xt/x:set-key runtime "query_key" nil)
  (xt/x:set-key runtime "updated_at" nil)
  (return runtime))

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
