(ns xt.db.node.state
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.event-type :as event-type]
             [xt.db.runtime.dataview :as dataview]
             [xt.db.runtime :as db-runtime]
             [xt.substrate :as event-node]
             [xt.substrate.base-sync :as sync]
             [xt.substrate.base-space :as node-space]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt node-opts
  "gets xt.db.node options from node metadata"
  {:added "4.1"}
  [node]
  (return (or (xtd/get-in node ["meta" event-type/META_KEY])
             {})))

(defn.xt set-node-opts
  "stores xt.db.node options on node metadata"
  {:added "4.1"}
  [node opts]
  (xtd/set-in node ["meta" event-type/META_KEY] (or opts {}))
  (return (-/node-opts node)))

(defn.xt state?
  "checks for a db.node state map"
  {:added "4.1"}
  [state]
  (return (and (xt/x:is-object? state)
              (== event-type/STATE_TAG
                  (xt/x:get-key state "::")))))

(defn.xt query-spec
  "gets the xt.db query definition from query or legacy resolver keys"
  {:added "4.1"}
  [entry]
  (return (or (xt/x:get-key (or entry {}) "query")
              (xt/x:get-key (or entry {}) "resolver"))))

(defn.xt payload-view-context
  "normalizes payload view context"
  {:added "4.1"}
  [payload]
  (var view-context (xtd/clone-nested (or (xt/x:get-key payload "view") {})))
  (when (and (xt/x:nil? (xt/x:get-key view-context "model_id"))
             (xt/x:not-nil? (xt/x:get-key payload "model_id")))
    (xt/x:set-key view-context "model_id" (xt/x:get-key payload "model_id")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "view_id"))
             (xt/x:not-nil? (xt/x:get-key payload "view_id")))
    (xt/x:set-key view-context "view_id" (xt/x:get-key payload "view_id")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "args"))
             (xt/x:not-nil? (xt/x:get-key payload "args")))
    (xt/x:set-key view-context "args" (xt/x:get-key payload "args")))
  (when (and (xt/x:nil? (xt/x:get-key view-context "args"))
             (xt/x:not-nil? (xt/x:get-key payload "input")))
    (xt/x:set-key view-context "args" (xt/x:get-key payload "input")))
  (return view-context))

(defn.xt payload-query
  "gets the query payload for a request"
  {:added "4.1"}
  [payload]
  (return (or (xt/x:get-key payload "query")
              (xt/x:get-key payload "resolver")
              payload)))

(defn.xt prepare-query
  "prepares a local query plan through dataview"
  {:added "4.1"}
  [state query view-context]
  (return (dataview/prepare-query
           state
           query
           view-context)))

(defn.xt query-triggers
  "gets dependent tables for a db/query payload"
  {:added "4.1"}
  [state query]
  (:= query (or query {}))
  (return
   (dataview/query-triggers
    state
    (xt/x:get-key query "table")
    query)))

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

(defn.xt output-process
  "extracts the public value from a wrapped result payload"
  {:added "4.1"}
  [value]
  (if (and (xt/x:is-object? value)
           (xt/x:has-key? value "value"))
    (return (xt/x:get-key value "value"))
    (return value)))

(defn.xt model-spec-views
  "gets normalized model views from a model spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key (or model-spec {}) "views")
              model-spec
              {})))

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
                         (xt/x:get-key current "sync_from")
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
  (var query (-/query-spec source))
  (when (xt/x:not-nil? query)
    (xt/x:set-key out "query" query))
  (xt/x:del-key out "resolver")
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
  (return (or (xt/x:get-key view "source")
              (xtd/get-in view ["use" "source"])
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
  (xt/for:array [dep (or (xt/x:get-key (or view {}) "deps") [])]
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
  (xt/for:object [[view-id view-entry] (or views {})]
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
  (xt/for:object [[linked-model-id linked-views] (or model-deps {})]
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
  (var state {"::" event-type/STATE_TAG
              "models" {}
              "meta" (or (xt/x:get-key opts "meta") {})
              "opts" opts})
  (xt/x:set-key state "::" event-type/STATE_TAG)
  (xt/x:set-key state "schema" (or (xt/x:get-key opts "schema") {}))
  (xt/x:set-key state "lookup" (or (xt/x:get-key opts "lookup") {}))
  (xt/x:set-key state "queries" {})
  (xt/x:set-key state "sources" (-/normalize-sources nil (xt/x:get-key opts "sources")))
  (xt/x:set-key state "remote" (or (xt/x:get-key opts "remote") {}))
  (return state))

(defn.xt model-views
  "normalizes a model input into a view map"
  {:added "4.1"}
  [model-spec]
  (return (-/model-spec-views model-spec)))

(defn.xt normalize-view
  "normalizes a single view record"
  {:added "4.1"}
  [view-id view]
  (:= view (xtd/clone-nested (or view {})))
  (var input (or (xt/x:get-key view "input")
                 (xt/x:get-key view "default_input")
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
             "status" event-type/STATUS_IDLE
             "error" nil
             "meta" meta
             "tables" {}
             "query_key" nil}))
  (var query (-/query-spec view))
  (when (xt/x:not-nil? query)
    (xt/x:set-key out "query" query))
  (xt/x:del-key out "default_input")
  (xt/x:del-key out "use")
  (xt/x:del-key out "resolver")
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
    (xt/x:err (xt/x:cat "view not found - "
                        (xt/x:json-encode [model-id view-id]))))
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
  (xt/x:set-key view "status" event-type/STATUS_READY)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt set-view-error
  "marks a view as errored"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" event-type/STATUS_ERROR)
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
  (xt/x:set-key view "status" event-type/STATUS_READY)
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
  (var upstream-id (xt/x:get-key source "sync_from"))
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
    (when (xt/x:not-nil? (xt/x:get-key source "sync_from"))
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
      (xt/x:set-key view "status" event-type/STATUS_IDLE)
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

(defn.xt not-implemented
  "returns a standard not-implemented payload"
  {:added "4.1"}
  [tag payload]
  (return {:status "error"
           :tag tag
           :data payload}))

(defn.xt view-refresh-result
  "returns the public refresh result from the current view state"
  {:added "4.1"}
  [view]
  (return {:query_key (xt/x:get-key view "query_key")
           :source (xt/x:get-key view "source")
           :value (xt/x:get-key view "value")
           :status (xt/x:get-key view "status")}))

(defn.xt view-remote-spec
  "gets the configured remote spec for a view, if present"
  {:added "4.1"}
  [view]
  (var remote (xt/x:get-key view "remote"))
  (if (and (xt/x:is-object? remote)
           (or (xt/x:has-key? remote "space")
               (xt/x:has-key? remote "target")
               (xt/x:has-key? remote "transport")
               (xt/x:has-key? remote "meta")
               (xt/x:has-key? remote "model_id")
               (xt/x:has-key? remote "view_id")))
    (return remote)
    (return nil)))

(defn.xt normalize-remote
  "normalizes remote settings from state, view, and call context"
  {:added "4.1"}
  [state remote-spec view-context]
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign
     (xt/x:obj-assign
      {}
      (or (xt/x:get-key state "remote")
          (xt/x:get-key (or (xt/x:get-key state "opts") {}) "remote")
          {}))
     (or remote-spec {}))
    (or (xt/x:get-key view-context "remote") {}))))

(defn.xt request-remote
  "issues a remote node request"
  {:added "4.1"}
  [node space-id remote action payload]
  (var meta (xtd/clone-nested (or (xt/x:get-key remote "meta") {})))
  (when (and (xt/x:not-nil? (xt/x:get-key remote "transport"))
             (xt/x:nil? (xt/x:get-key meta "transport_id")))
    (xt/x:set-key meta "transport_id" (xt/x:get-key remote "transport")))
  (return
   (event-node/request
    node
    (or (xt/x:get-key remote "space")
        (xt/x:get-key remote "target")
        space-id)
    action
    [payload]
    meta)))

(defn.xt apply-remote-events
  "applies db event tags returned from a remote response"
  {:added "4.1"}
  [state response]
  (when (and (xt/x:is-object? response)
             (xt/x:not-nil? (xt/x:get-key response "db/sync")))
    (-/sync-state-event state "add" (xt/x:get-key response "db/sync")))
  (when (and (xt/x:is-object? response)
             (xt/x:not-nil? (xt/x:get-key response "db/remove")))
    (-/sync-state-event state "remove" (xt/x:get-key response "db/remove")))
  (return response))

(defn.xt remote-view-payload
  "builds a remote query payload from a local registered view"
  {:added "4.1"}
  [view remote model-id view-id]
  (var payload
       {"view"
        {"model_id" (or (xt/x:get-key remote "model_id")
                        model-id)
         "view_id" (or (xt/x:get-key remote "view_id")
                       view-id)
         "args" (or (xt/x:get-key view "input") [])}})
  (var query (-/query-spec view))
  (when (xt/x:not-nil? query)
    (xt/x:set-key payload "query" query))
  (return payload))

(defn.xt find-view-by-query-key
  "locates a registered view by query key"
  {:added "4.1"}
  [state query-key]
  (var out nil)
  (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[view-id view] (or (xt/x:get-key model "views") {})]
      (when (and (xt/x:nil? out)
                 (== query-key (xt/x:get-key view "query_key")))
        (:= out {"model_id" model-id
                 "view_id" view-id
                 "view" view}))))
  (return out))

(defn.xt mark-view-stale
  "marks a single view as stale"
  {:added "4.1"}
  [view]
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" event-type/STATUS_STALE)
  (xt/x:set-key view "error" nil)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (return view))

(defn.xt mark-stale-by-tables
  "marks views stale when their recorded table dependencies intersect"
  {:added "4.1"}
  [state tables]
  (xt/for:object [[_ model] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[_ view] (or (xt/x:get-key model "views") {})]
      (var listen (or (xt/x:get-key view "tables") {}))
      (var stale? false)
      (if (> (xt/x:len (xt/x:obj-keys listen)) 0)
        (xt/for:object [[table _] listen]
          (when (xt/x:has-key? tables table)
            (:= stale? true)))
        (when (xt/x:not-nil? (xt/x:get-key view "query_key"))
          (:= stale? true)))
      (when stale?
        (-/mark-view-stale view))))
  (return tables))

(defn.xt sync-state-event
  "applies a db event across model sources"
  {:added "4.1"}
  [state event-tag data]
  (var tables {})
  (xt/for:object [[table rows] (or data {})]
    (xt/x:set-key tables table true)
    (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
      (xt/for:object [[source-id source] (or (xt/x:get-key model "sources") {})]
        (var query (-/query-spec source))
        (var source-table (or (xt/x:get-key source "table")
                              (:? (xt/x:not-nil? query)
                                  (xt/x:get-key query "table")
                                  nil)))
        (when (or (xt/x:nil? source-table)
                  (== source-table table))
          (var existing (xtd/clone-nested (or (xt/x:get-key source "data") [])))
          (var out [])
          (cond (== event-tag "add")
                (do (var lookup {})
                    (xt/for:array [row existing]
                      (when (xt/x:not-nil? (xt/x:get-key row "id"))
                        (xt/x:set-key lookup (xt/x:get-key row "id") true))
                      (xt/x:arr-push out row))
                    (xt/for:array [row (or rows [])]
                      (var row-id (xt/x:get-key row "id"))
                      (if (and (xt/x:not-nil? row-id)
                               (== true (xt/x:get-key lookup row-id)))
                        (do (var replaced [])
                            (xt/for:array [entry out]
                              (if (== (xt/x:get-key entry "id") row-id)
                                (xt/x:arr-push replaced row)
                                (xt/x:arr-push replaced entry)))
                            (:= out replaced))
                        (xt/x:arr-push out row))))

                (== event-tag "remove")
                (do (var remove-lu {})
                    (xt/for:array [row-id (or rows [])]
                      (xt/x:set-key remove-lu row-id true))
                    (xt/for:array [row existing]
                      (when (not (== true (xt/x:get-key remove-lu (xt/x:get-key row "id"))))
                        (xt/x:arr-push out row))))

                :else
                (:= out existing))
          (-/set-source-data state model-id source-id out)))))
  (-/mark-stale-by-tables state tables)
  (return tables))

(defn.xt ensure-space-state
  "ensures the target node space has xt.db view state"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (not (-/state? state))
    (:= state (-/base-state (-/node-opts node)))
    (xt/x:set-key space "state" state))
  (return state))

(defn.xt query
  "issues a node query request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_QUERY
                              [payload]
                              nil)))

(defn.xt query-refresh
  "issues a cached query refresh request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_QUERY_REFRESH
                              [payload]
                              nil)))

(defn.xt sync
  "issues a node sync request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_SYNC
                              [payload]
                              nil)))

(defn.xt remove
  "issues a node remove request"
  {:added "4.1"}
  [node space-id payload]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_REMOVE
                              [payload]
                              nil)))

(defn.xt clear
  "issues a node clear request"
  {:added "4.1"}
  [node space-id]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_CLEAR
                              [{}]
                              nil)))

(defn.xt snapshot
  "requests a node state snapshot"
  {:added "4.1"}
  [node space-id]
  (return (event-node/request node
                              space-id
                              event-type/ACTION_SNAPSHOT
                              [{}]
                              nil)))

(defn.xt model-put
  "registers a model and its views on a node space"
  {:added "4.1"}
  [node space-id model-id model-spec]
  (var state (-/ensure-space-state node space-id))
  (return (-/put-model state model-id model-spec)))

(defn.xt view-put
  "registers a single view on a node space"
  {:added "4.1"}
  [node space-id model-id view-id view-spec]
  (var state (-/ensure-space-state node space-id))
  (return (-/put-view state model-id view-id view-spec)))

(defn.xt model-get
  "gets a model from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/get-model state model-id)))

(defn.xt view-get
  "gets a view from a node space"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/get-view state model-id view-id)))

(defn.xt view-val
  "gets the current value for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "value")
              nil)))

(defn.xt view-input
  "gets the current input for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (or (xt/x:get-key view "input") [])
              [])))

(defn.xt view-pending
  "gets the pending flag for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (or (xt/x:get-key view "pending") false)
              false)))

(defn.xt view-error
  "gets the current error for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var view (-/view-get node space-id model-id view-id))
  (return (:? view
              (xt/x:get-key view "error")
              nil)))

(defn.xt view-dependents
  "gets dependent views for the given source view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/get-view-dependents state model-id view-id)))

(defn.xt model-dependents
  "gets dependent models for the given source model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/get-model-dependents state model-id)))

(defn.xt source-get
  "gets a model source binding"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (return (-/get-source state model-id source-id)))

(defn.xt source-put
  "stores source data on a model source binding"
  {:added "4.1"}
  [node space-id model-id source-id data]
  (var state (-/ensure-space-state node space-id))
  (return (-/set-source-data state model-id source-id data)))

(defn.xt source-refresh
  "refreshes a live source from its declared query"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (var source (-/ensure-source state model-id source-id))
  (var query (-/query-spec source))
  (var db (xt/x:get-key source "db"))
  (when (or (not (== true (xt/x:get-key source "live")))
            (xt/x:nil? db)
            (xt/x:nil? query))
    (return (promise/x:promise-run source)))
  (var [ok prepared]
       (-/prepare-query
        state
        query
        {"model_id" model-id
         "source_id" source-id
         "args" (or (xt/x:get-key source "input") [])}))
  (when (not ok)
    (return (promise/x:promise-run prepared)))
  (xt/x:set-key source "query_key" (xt/x:get-key prepared "key"))
  (return
   (promise/x:promise-then
    (db-runtime/db-pull
     db
     (xt/x:get-key state "schema")
     (xt/x:get-key prepared "plan"))
    (fn [value]
      (-/set-source-data state model-id source-id value)
      (return (-/ensure-source state model-id source-id))))))

(defn.xt source-sync
  "synchronizes a model source from its configured upstream source"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var state (-/ensure-space-state node space-id))
  (var source (-/ensure-source state model-id source-id))
  (var upstream-id (xt/x:get-key source "sync_from"))
  (when (or (xt/x:nil? upstream-id)
            (== upstream-id source-id))
    (return (promise/x:promise-run source)))
  (var upstream (-/ensure-source state model-id upstream-id))
  (var upstream-refresh
       (:? (and (== true (xt/x:get-key upstream "live"))
                (xt/x:not-nil? (xt/x:get-key upstream "db"))
                (xt/x:not-nil? (-/query-spec upstream)))
           (-/source-refresh node space-id model-id upstream-id)
           (promise/x:promise-run upstream)))
  (return
   (promise/x:promise-then
    upstream-refresh
    (fn [_]
      (-/sync-source state model-id source-id)
      (var synced (-/ensure-source state model-id source-id))
      (var db (xt/x:get-key synced "db"))
      (when (and (== true (xt/x:get-key synced "live"))
                 (xt/x:not-nil? db))
        (db-runtime/db-clear db)
        (var data (or (xt/x:get-key synced "data") []))
        (var query (-/query-spec synced))
        (var table (or (xt/x:get-key synced "table")
                       (:? (xt/x:not-nil? query)
                           (xt/x:get-key query "table")
                           nil)))
        (when (> (xt/x:len data) 0)
          (db-runtime/sync-event
           db
           ["add" (:? (and (xt/x:is-array? data)
                           (xt/x:not-nil? table))
                     {table data}
                     data)])))
      (return synced)))))

(defn.xt model-sync
  "synchronizes all structural sources for a model"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (-/ensure-model state model-id))
  (var source-ids [])
  (var running [])
  (xt/for:object [[source-id source] (or (xt/x:get-key model "sources") {})]
    (when (xt/x:not-nil? (xt/x:get-key source "sync_from"))
      (xt/x:arr-push source-ids source-id)
      (xt/x:arr-push running (-/source-sync node space-id model-id source-id))))
  (if (> (xt/x:len running) 0)
    (return
     (promise/x:promise-then
      (promise/x:promise-all running)
      (fn [results]
        (var out {})
        (xt/for:index [idx [0 (xt/x:len source-ids)]]
          (var source-id (xt/x:get-idx source-ids idx))
          (xt/x:set-key out source-id (xt/x:get-idx results idx)))
        (return out))))
    (return (promise/x:promise-run {}))))

(defn.xt view-refresh
  "refreshes a view from its declared structural source role"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var state (-/ensure-space-state node space-id))
  (var view (-/ensure-view state model-id view-id))
  (var remote-spec (-/view-remote-spec view))
  (when (xt/x:not-nil? remote-spec)
    (var remote (-/normalize-remote
                 state
                 remote-spec
                 {"model_id" model-id
                  "view_id" view-id
                  "args" (or (xt/x:get-key view "input") [])}))
    (return
     (promise/x:promise-then
      (-/request-remote
       node
       space-id
       remote
       event-type/ACTION_QUERY
       (-/remote-view-payload view remote model-id view-id))
      (fn [response]
        (-/apply-remote-events state response)
        (when (xt/x:not-nil? (xt/x:get-key response "query_key"))
          (xt/x:set-key view "query_key" (xt/x:get-key response "query_key")))
        (when (xt/x:not-nil? (xt/x:get-key response "tables"))
          (xt/x:set-key view "tables" (xt/x:get-key response "tables")))
        (if (and (xt/x:is-object? response)
                 (== (xt/x:get-key response "status") "error"))
          (do (-/set-view-error state model-id view-id response)
              (return (-/view-refresh-result view)))
          (do (-/set-view-value
               state
               model-id
               view-id
               (or (xt/x:get-key response "source")
                   (xt/x:get-key view "source"))
              (-/output-process response))
              (return (-/view-refresh-result view))))))))
  (var source-id (or (xt/x:get-key view "source")
                     "caching"))
  (var source (-/ensure-source state model-id source-id))
  (var live? (xt/x:get-key source "live"))
  (var db (xt/x:get-key source "db"))
  (cond (and (xt/x:is-boolean? live?)
             live?)
        (do (var [ok prepared]
                 (-/prepare-query
                  state
                  (-/query-spec view)
                  {"model_id" model-id
                   "view_id" view-id
                   "args" (or (xt/x:get-key view "input") [])}))
            (when (not ok)
              (-/set-view-error state model-id view-id prepared)
              (return (promise/x:promise-run prepared)))
            (xt/x:set-key view "query_key" (xt/x:get-key prepared "key"))
            (xt/x:set-key view "tables" (or (xt/x:get-key prepared "tables") {}))
            (return
             (promise/x:promise-then
              (db-runtime/db-pull
               db
               (xt/x:get-key state "schema")
               (xt/x:get-key prepared "plan"))
              (fn [value]
                (-/set-source-data state model-id source-id value)
                (-/set-view-value state model-id view-id source-id value)
                (return (-/view-refresh-result view))))))

        :else
        (do (var value (xtd/clone-nested (xt/x:get-key source "data")))
            (-/set-view-value state model-id view-id source-id value)
            (return (promise/x:promise-run (-/view-refresh-result view))))))

(defn.xt model-refresh
  "marks all views in a registered model as ready"
  {:added "4.1"}
  [node space-id model-id]
  (var state (-/ensure-space-state node space-id))
  (var model (-/ensure-model state model-id))
  (var running [])
  (xt/for:object [[view-id _] (or (xt/x:get-key model "views") {})]
    (xt/x:arr-push running (-/view-refresh node space-id model-id view-id)))
  (return (promise/x:promise-all running)))

(defn.xt view-set-input
  "sets view input and marks the view ready"
  {:added "4.1"}
  [node space-id model-id view-id input]
  (var state (-/ensure-space-state node space-id))
  (-/set-view-input state model-id view-id input)
  (return (-/view-refresh node space-id model-id view-id)))

(defn.xt handle-query
  "handles a query request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (sync/request-payload args))
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (var view-context (-/payload-view-context payload))
  (var model-id (xt/x:get-key view-context "model_id"))
  (var view-id (xt/x:get-key view-context "view_id"))
  (var query (-/payload-query payload))
  (when (and (xt/x:not-nil? model-id)
             (xt/x:not-nil? view-id))
    (var view (-/ensure-view state model-id view-id))
    (when (xt/x:not-nil? (xt/x:get-key view-context "args"))
      (-/set-view-input
       state
       model-id
       view-id
       (xt/x:get-key view-context "args")))
    (when (and (xt/x:not-nil? query)
               (or (xt/x:has-key? payload "query")
                   (xt/x:has-key? payload "resolver")))
      (xt/x:set-key view "query" query)
      (xt/x:del-key view "resolver"))
    (return (-/view-refresh node
                            (xt/x:get-key current-space "id")
                            model-id
                            view-id)))
  (return (-/not-implemented
           "xt.db.node.view/query-not-implemented"
           payload)))

(defn.xt handle-query-refresh
  "handles a cached query refresh request"
  {:added "4.1"}
  [current-space args request node]
  (var payload (sync/request-payload args))
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (var query-key (xt/x:get-key payload "query_key"))
  (when (xt/x:not-nil? query-key)
    (var found (-/find-view-by-query-key state query-key))
    (when (xt/x:not-nil? found)
      (-/mark-view-stale (xt/x:get-key found "view"))
      (return (-/view-refresh node
                              (xt/x:get-key current-space "id")
                              (xt/x:get-key found "model_id")
                              (xt/x:get-key found "view_id")))))
  (return (-/handle-query current-space args request node)))

(defn.xt handle-sync
  "handles a sync request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (sync/request-payload args))
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (var sync-data (or (xt/x:get-key payload "db/sync")
                     payload))
  (var tables (-/sync-state-event state "add" sync-data))
  (return {"db/sync" sync-data
           "tables" tables}))

(defn.xt handle-remove
  "handles a remove request for the clean view-based implementation"
  {:added "4.1"}
  [current-space args request node]
  (var payload (sync/request-payload args))
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (var remove-data (or (xt/x:get-key payload "db/remove")
                       payload))
  (var tables (-/sync-state-event state "remove" remove-data))
  (return {"db/remove" remove-data
           "tables" tables}))

(defn.xt handle-clear
  "clears the local view state caches"
  {:added "4.1"}
  [current-space args request node]
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (-/clear-state state)
  (return true))

(defn.xt handle-snapshot
  "returns a snapshot of the current view state"
  {:added "4.1"}
  [current-space args request node]
  (var state (-/ensure-space-state node (xt/x:get-key current-space "id")))
  (return (-/snapshot-state state)))

(defn.xt install
  "installs xt.db state handlers and services on a substrate node"
  {:added "4.1"}
  [node opts]
  (-/set-node-opts node opts)
  (event-node/set-service
   node
   event-type/SERVICE_NODE
   {"opts" (-/node-opts node)
    "actions" {"query" event-type/ACTION_QUERY
               "query_refresh" event-type/ACTION_QUERY_REFRESH
               "sync" event-type/ACTION_SYNC
               "remove" event-type/ACTION_REMOVE
               "clear" event-type/ACTION_CLEAR
               "snapshot" event-type/ACTION_SNAPSHOT}})
  (event-node/set-service
   node
   event-type/SERVICE_SOURCE_KINDS
   {"postgres" true
    "sqlite" true
    "cache" true
    "supabase" true})
  (event-node/register-handler node event-type/ACTION_QUERY -/handle-query nil)
  (event-node/register-handler node event-type/ACTION_QUERY_REFRESH -/handle-query-refresh nil)
  (event-node/register-handler node event-type/ACTION_SYNC -/handle-sync nil)
  (event-node/register-handler node event-type/ACTION_REMOVE -/handle-remove nil)
  (event-node/register-handler node event-type/ACTION_CLEAR -/handle-clear nil)
  (event-node/register-handler node event-type/ACTION_SNAPSHOT -/handle-snapshot nil)
  (return node))

(defn.xt uninstall
  "removes xt.db state handlers from a node"
  {:added "4.1"}
  [node]
  (event-node/unregister-handler node event-type/ACTION_QUERY)
  (event-node/unregister-handler node event-type/ACTION_QUERY_REFRESH)
  (event-node/unregister-handler node event-type/ACTION_SYNC)
  (event-node/unregister-handler node event-type/ACTION_REMOVE)
  (event-node/unregister-handler node event-type/ACTION_CLEAR)
  (event-node/unregister-handler node event-type/ACTION_SNAPSHOT)
  (event-node/set-service node event-type/SERVICE_NODE nil)
  (event-node/set-service node event-type/SERVICE_SOURCE_KINDS nil)
  (return node))
