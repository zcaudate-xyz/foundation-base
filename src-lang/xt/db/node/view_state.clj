(ns xt.db.node.view-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt normalize-source
  "normalizes a named model source"
  {:added "4.1"}
  [source-id source]
  (return
   (xt/x:obj-assign
    {"id" source-id}
    (or source {}))))

(defn.xt normalize-sources
  "normalizes model sources with primary and caching defaults"
  {:added "4.1"}
  [sources]
  (var out {"primary" {"id" "primary"}
            "caching" {"id" "caching"}})
  (xt/for:object [[source-id source] (or sources {})]
    (xt/x:set-key out source-id (-/normalize-source source-id source)))
  (return out))

(defn.xt normalize-use
  "normalizes per-view source usage"
  {:added "4.1"}
  [use-spec]
  (return
   {"read-from" (or (xtd/get-in use-spec ["read-from"])
                    (xtd/get-in use-spec ["read_from"])
                    "caching")
    "refresh-from" (or (xtd/get-in use-spec ["refresh-from"])
                       (xtd/get-in use-spec ["refresh_from"])
                       "primary")
    "sync-to" (or (xtd/get-in use-spec ["sync-to"])
                  (xtd/get-in use-spec ["sync_to"])
                  "caching")}))

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
  "creates the base xt.db view state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" spec/STATE_TAG
           :schema (or (xt/x:get-key opts "schema") {})
           :lookup (or (xt/x:get-key opts "lookup") {})
           :models {}
           :queries {}
           :sources (-/normalize-sources (xt/x:get-key opts "sources"))
           :meta (or (xt/x:get-key opts "meta") {})
           :opts opts}))

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
  (var input (or (xt/x:get-key view "input")
                 (xt/x:get-key view "default_input")
                 (xt/x:get-key view "defaultArgs")
                 (xt/x:get-key view "default_args")
                 []))
  (var value (xt/x:get-key view "value"))
  (var meta (or (xt/x:get-key view "meta") {}))
  (var out (xt/x:obj-assign
            view
            {"id" view-id
             "input" input
             "value" value
             "pending" false
             "status" spec/STATUS_IDLE
             "error" nil
             "meta" meta
             "use" (-/normalize-use (xt/x:get-key view "use"))}))
  (xt/x:del-key out "default_input")
  (xt/x:del-key out "defaultArgs")
  (xt/x:del-key out "default_args")
  (return out))

(defn.xt normalize-model
  "normalizes a single model record"
  {:added "4.1"}
  [model-id model-spec]
  (var views {})
  (xt/for:object [[view-id view] (-/model-views model-spec)]
    (xt/x:set-key views view-id (-/normalize-view view-id view)))
  (return {"id" model-id
           "meta" (or (xt/x:get-key model-spec "meta") {})
           "sources" (-/normalize-sources (xt/x:get-key model-spec "sources"))
           "views" views
           "deps" {}
           "unknown_deps" []}))

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
  (xt/x:set-key view "status" spec/STATUS_READY)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt set-view-error
  "marks a view as errored"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_ERROR)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" error)
  (return view))

(defn.xt clear-state
  "clears query caches and marks views idle"
  {:added "4.1"}
  [state]
  (xt/x:set-key state "queries" {})
  (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
      (var view (-/ensure-view state model-id view-id))
      (xt/x:set-key view "pending" false)
      (xt/x:set-key view "status" spec/STATUS_IDLE)
      (xt/x:set-key view "error" nil)))
  (return true))

(defn.xt get-view-dependents
  "gets all dependent views for a source view"
  {:added "4.1"}
  [state model-id view-id]
  (var out {})
  (xt/for:object [[dmodel-id model] (or (xt/x:get-key state "models") {})]
    (var view-lu (xtd/get-in model ["deps" model-id view-id]))
    (when (xt/x:not-nil? view-lu)
      (xt/x:set-key out dmodel-id (xt/x:obj-keys view-lu))))
  (return out))

(defn.xt get-model-dependents
  "gets dependent models for the given source model"
  {:added "4.1"}
  [state model-id]
  (var out {})
  (xt/for:object [[dmodel-id model] (or (xt/x:get-key state "models") {})]
    (var model-lu (xtd/get-in model ["deps" model-id]))
    (when (xt/x:not-nil? model-lu)
      (xt/x:set-key out dmodel-id true)))
  (return out))

(defn.xt snapshot-state
  "returns a snapshot of the current view state"
  {:added "4.1"}
  [state]
  (return {"schema" (xt/x:get-key state "schema")
           "lookup" (xt/x:get-key state "lookup")
           "sources" (xt/x:get-key state "sources")
           "queries" (xt/x:get-key state "queries")
           "models" (xt/x:get-key state "models")}))
