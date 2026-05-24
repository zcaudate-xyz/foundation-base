(ns xt.db.node.view-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt source-base
  "creates the base structural definition for a source role"
  {:added "4.1"}
  [source-id]
  (return {"id" source-id
           "data" []
           "updated_at" nil
           "synced_at" nil
           "sync_from" (:? (== source-id "caching")
                          "primary"
                          nil)}))

(defn.xt normalize-source
  "normalizes a named model source"
  {:added "4.1"}
  [source-id current source]
  (:= current (or current {}))
  (:= source (or source {}))
  (var out
       (xt/x:obj-assign
        (-/source-base source-id)
        (xt/x:obj-assign
         {"id" source-id
          "sync_from" (or (xt/x:get-key source "sync_from")
                         (xt/x:get-key source "sync-from")
                         (xt/x:get-key current "sync_from")
                         (xt/x:get-key current "sync-from")
                         (:? (== source-id "caching")
                             "primary"
                             nil))}
         (xt/x:obj-assign current source))))
  (var config
       (xt/x:obj-assign
        (or (xt/x:get-key current "config") {})
        (or (xt/x:get-key source "config") {})))
  (when (> (xt/x:len (xt/x:obj-keys config)) 0)
    (xt/x:set-key out "config" config))
  (var setup
       (xt/x:obj-assign
        (or (xt/x:get-key current "setup") {})
        (or (xt/x:get-key source "setup") {})))
  (when (> (xt/x:len (xt/x:obj-keys setup)) 0)
    (xt/x:set-key out "setup" setup))
  (return out))

(defn.xt normalize-sources
  "normalizes model sources with primary and caching defaults"
  {:added "4.1"}
  [defaults sources]
  (var out {"primary" (-/source-base "primary")
            "caching" (-/source-base "caching")})
  (xt/for:object [[source-id source] (or defaults {})]
    (xt/x:set-key out
                 source-id
                 (-/normalize-source
                  source-id
                  (xt/x:get-key out source-id)
                  source)))
  (xt/for:object [[source-id source] (or sources {})]
    (xt/x:set-key out
                 source-id
                 (-/normalize-source
                  source-id
                  (xt/x:get-key out source-id)
                  source)))
  (return out))

(defn.xt normalize-view-source
  "normalizes the source role declared by a view"
  {:added "4.1"}
  [view]
  (return
   (or (xt/x:get-key view "source")
       (xtd/get-in view ["use" "source"])
       (xtd/get-in view ["use" "read-from"])
       "caching")))

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
           :sources (-/normalize-sources nil (xt/x:get-key opts "sources"))
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
             "source" (-/normalize-view-source view)
             "input" input
             "value" value
             "pending" false
             "status" spec/STATUS_IDLE
             "error" nil
             "meta" meta}))
  (xt/x:del-key out "default_input")
  (xt/x:del-key out "defaultArgs")
  (xt/x:del-key out "default_args")
  (xt/x:del-key out "use")
  (return out))

(defn.xt normalize-model
  "normalizes a single model record"
  {:added "4.1"}
  [default-sources model-id model-spec]
  (var views {})
  (xt/for:object [[view-id view] (-/model-views model-spec)]
    (xt/x:set-key views view-id (-/normalize-view view-id view)))
  (return {"id" model-id
           "meta" (or (xt/x:get-key model-spec "meta") {})
           "sources" (-/normalize-sources default-sources
                                          (xt/x:get-key model-spec "sources"))
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
  (var model (-/normalize-model
              (xt/x:get-key state "sources")
              model-id
              model-spec))
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

(defn.xt set-view-value
  "stores a view value sourced from a structural role"
  {:added "4.1"}
  [state model-id view-id source-id value]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "value" value)
  (xt/x:set-key view "source" source-id)
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_READY)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt get-source
  "gets a source binding from a registered model"
  {:added "4.1"}
  [state model-id source-id]
  (return (xtd/get-in state ["models" model-id "sources" source-id])))

(defn.xt ensure-source
  "gets a source binding from a registered model or throws"
  {:added "4.1"}
  [state model-id source-id]
  (var source (-/get-source state model-id source-id))
  (when (xt/x:nil? source)
    (xt/x:err (xt/x:cat "source not found - "
                        (xt/x:json-encode [model-id source-id]))))
  (return source))

(defn.xt set-source-data
  "stores source data on a model source binding"
  {:added "4.1"}
  [state model-id source-id data]
  (var source (-/ensure-source state model-id source-id))
  (xt/x:set-key source "data" (xtd/clone-nested data))
  (xt/x:set-key source "updated_at" (xt/x:now-ms))
  (return source))

(defn.xt sync-source
  "synchronizes a target source from its configured upstream source"
  {:added "4.1"}
  [state model-id source-id]
  (var source (-/ensure-source state model-id source-id))
  (var upstream-id (or (xt/x:get-key source "sync_from")
                       (xt/x:get-key source "sync-from")))
  (when (or (xt/x:nil? upstream-id)
            (== upstream-id source-id))
    (return source))
  (var upstream (-/ensure-source state model-id upstream-id))
  (-/set-source-data state
                     model-id
                     source-id
                     (xt/x:get-key upstream "data"))
  (xt/x:set-key source "synced_at" (xt/x:now-ms))
  (return source))

(defn.xt sync-model-sources
  "synchronizes all model sources that declare an upstream binding"
  {:added "4.1"}
  [state model-id]
  (var model (-/ensure-model state model-id))
  (var out {})
  (xt/for:object [[source-id source] (or (xt/x:get-key model "sources") {})]
    (when (xt/x:not-nil? (or (xt/x:get-key source "sync_from")
                             (xt/x:get-key source "sync-from")))
      (xt/x:set-key out
                    source-id
                    (-/sync-source state model-id source-id))))
  (return out))

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
