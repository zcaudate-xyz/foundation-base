(ns xt.db.node.instance-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]
             [xt.db.node.schema-spec :as spec]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.node.instance-util :as util]
             [xt.event.base-view :as event-view]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt ensure-state
  "ensures a db.node state is attached to the current space"
  {:added "4.1"}
  [space node]
  (var state (xt/x:get-key space "state"))
  (when (not (util/state? state))
    (:= state (schema-state/base-state (util/node-opts node)))
    (xt/x:set-key space "state" state))
  (return state))

(defn.xt ensure-db
  "ensures the local db instance exists for a state"
  {:added "4.1"}
  [state]
  (var db (xt/x:get-key state "db"))
  (when (xt/x:nil? db)
    (var opts (or (xt/x:get-key state "opts") {}))
    (:= db (instance/db-create
            (or (xt/x:get-key opts "db")
                {"::" "db.cache"})
            (schema-state/get-schema state)
            (or (xt/x:get-key state "lookup") {})
            (or (xt/x:get-key opts "db_opts") {})))
     (xt/x:set-key state "db" db))
  (return db))

(defn.xt rebuild-model-deps
  "recomputes dependency indexes for all registered models"
  {:added "4.1"}
  [state]
  (xt/for:object [[model-id model] (or (xt/x:get-key state "models") {})]
    (var views (or (xt/x:get-key model "views") {}))
    (var deps (schema-state/get-model-deps model-id views))
    (xt/x:set-key model "deps" deps)
    (xt/x:set-key model
                  "unknown_deps"
                  (schema-state/get-unknown-deps state model-id views deps)))
  (return state))

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
  "gets all dependent models for a source model"
  {:added "4.1"}
  [state model-id]
  (var out {})
  (xt/for:object [[dmodel-id model] (or (xt/x:get-key state "models") {})]
    (var model-lu (xtd/get-in model ["deps" model-id]))
    (when (xt/x:not-nil? model-lu)
      (xt/x:set-key out dmodel-id true)))
  (return out))

(defn.xt put-model
  "stores a model and normalizes its views"
  {:added "4.1"}
  [state model-id model-spec]
  (var views (schema-state/model-views model-spec))
  (var model {:id model-id
              :meta (or (xt/x:get-key model-spec "meta") {})
              :views {}
              :deps {}
              :unknown_deps []})
  (xt/for:object [[view-id view] views]
    (xtd/set-in model ["views" view-id]
                (schema-state/normalize-view view-id view)))
  (xtd/set-in state ["models" model-id] model)
  (-/rebuild-model-deps state)
  (return model))

(defn.xt put-view
  "stores a single view on an existing model"
  {:added "4.1"}
  [state model-id view-id view-spec]
  (var model (schema-state/ensure-model state model-id))
  (var view (schema-state/normalize-view view-id view-spec))
  (xtd/set-in model ["views" view-id] view)
  (-/rebuild-model-deps state)
  (return view))

(defn.xt sync-view-state
  "mirrors base-view output state onto compatibility keys"
  {:added "4.1"}
  [view status error]
  (var output (event-view/get-output view nil))
  (xt/x:set-key view "value" (xt/x:get-key output "current"))
  (xt/x:set-key view "pending" (or (xt/x:get-key output "pending") false))
  (xt/x:set-key view "status" status)
  (xt/x:set-key view "error" error)
  (xt/x:set-key view "updated_at" (xt/x:get-key output "updated"))
  (return view))

(defn.xt clear-view-errored
  "clears any errored flag from the base-view output"
  {:added "4.1"}
  [view]
  (var output (xt/x:get-key view "output"))
  (when (and (xt/x:is-object? output)
             (xt/x:has-key? output "errored"))
    (xt/x:del-key output "errored"))
  (return view))

(defn.xt set-view-input
  "sets input on a view"
  {:added "4.1"}
  [state model-id view-id input]
  (var view (schema-state/ensure-view state model-id view-id))
  (event-view/set-input view {:data input})
  (return view))

(defn.xt set-view-pending
  "marks a view as pending"
  {:added "4.1"}
  [state model-id view-id]
  (var view (schema-state/ensure-view state model-id view-id))
  (event-view/set-pending view true nil)
  (return (-/sync-view-state view spec/STATUS_PENDING nil)))

(defn.xt set-view-success
  "stores the current view output"
  {:added "4.1"}
  [state model-id view-id query-key value tables]
  (var view (schema-state/ensure-view state model-id view-id))
  (var prev-tables (or (xt/x:get-key view "tables") {}))
  (event-view/set-pending view false nil)
  (event-view/set-output view value false "main" nil nil)
  (-/remove-view-watch state model-id view-id prev-tables)
  (xt/x:set-key view "query_key" query-key)
  (xt/x:set-key view "tables" (or tables {}))
  (-/watch-view state model-id view-id (xt/x:get-key view "tables"))
  (return (-/sync-view-state view spec/STATUS_READY nil)))

(defn.xt set-view-error
  "stores a view error"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (schema-state/ensure-view state model-id view-id))
  (var current (event-view/get-current view nil))
  (event-view/set-pending view false nil)
  (event-view/set-output view current true "main" nil nil)
  (return (-/sync-view-state view spec/STATUS_ERROR error)))

(defn.xt set-view-stale
  "marks a view as stale"
  {:added "4.1"}
  [state model-id view-id reason]
  (var view (schema-state/ensure-view state model-id view-id))
  (event-view/set-pending view false nil)
  (-/clear-view-errored view)
  (return (-/sync-view-state view spec/STATUS_STALE reason)))

(defn.xt remove-query-watch
  "removes a query from the table watch index"
  {:added "4.1"}
  [state query-key tables]
  (var watch (xt/x:get-key state "watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var queries (xt/x:get-key watch table))
      (when (xt/x:not-nil? queries)
        (xt/x:del-key queries query-key)
        (when (== 0 (xt/x:len (xt/x:obj-keys queries)))
           (xt/x:del-key watch table)))))
  (return true))

(defn.xt remove-view-watch
  "removes a view binding from the table watch index"
  {:added "4.1"}
  [state model-id view-id tables]
  (var view-watch (xt/x:get-key state "view_watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var models (xt/x:get-key view-watch table))
      (when (xt/x:not-nil? models)
        (var views (xt/x:get-key models model-id))
        (when (xt/x:not-nil? views)
          (xt/x:del-key views view-id)
          (when (== 0 (xt/x:len (xt/x:obj-keys views)))
            (xt/x:del-key models model-id))
          (when (== 0 (xt/x:len (xt/x:obj-keys models)))
            (xt/x:del-key view-watch table))))))
  (return true))

(defn.xt watch-query
  "indexes a query by each table it touches"
  {:added "4.1"}
  [state query-key tables]
  (var watch (xt/x:get-key state "watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var queries (xt/x:get-key watch table))
      (when (xt/x:nil? queries)
        (:= queries {})
        (xt/x:set-key watch table queries))
       (xt/x:set-key queries query-key true)))
  (return tables))

(defn.xt watch-view
  "indexes a view by each table it touches"
  {:added "4.1"}
  [state model-id view-id tables]
  (var view-watch (xt/x:get-key state "view_watch"))
  (when (xt/x:is-object? tables)
    (xt/for:object [[table _] tables]
      (var models (xt/x:get-key view-watch table))
      (when (xt/x:nil? models)
        (:= models {})
        (xt/x:set-key view-watch table models))
      (var views (xt/x:get-key models model-id))
      (when (xt/x:nil? views)
        (:= views {})
        (xt/x:set-key models model-id views))
      (xt/x:set-key views view-id true)))
  (return tables))

(defn.xt affected-query-ids
  "gets cached query ids affected by a set of tables"
  {:added "4.1"}
  [state tables]
  (var out {})
  (var watch (xt/x:get-key state "watch"))
  (cond (xt/x:is-array? tables)
        (xt/for:array [table tables]
          (xt/for:object [[query-key _] (or (xt/x:get-key watch table) {})]
            (xt/x:set-key out query-key true)))

        (xt/x:is-object? tables)
        (xt/for:object [[table _] tables]
          (xt/for:object [[query-key _] (or (xt/x:get-key watch table) {})]
            (xt/x:set-key out query-key true))))
  (return (xt/x:obj-keys out)))

(defn.xt affected-view-bindings
  "gets bound view ids affected by a set of tables"
  {:added "4.1"}
  [state tables]
  (var out {})
  (var view-watch (xt/x:get-key state "view_watch"))
  (cond (xt/x:is-array? tables)
        (xt/for:array [table tables]
          (xt/for:object [[model-id views] (or (xt/x:get-key view-watch table) {})]
            (xt/for:object [[view-id _] views]
              (xtd/set-in out [model-id view-id] true))))

        (xt/x:is-object? tables)
        (xt/for:object [[table _] tables]
          (xt/for:object [[model-id views] (or (xt/x:get-key view-watch table) {})]
            (xt/for:object [[view-id _] views]
              (xtd/set-in out [model-id view-id] true)))))
  (return out))

(defn.xt remove-query
  "removes a cached query and its watch entries"
  {:added "4.1"}
  [state query-key]
  (var queries (xt/x:get-key state "queries"))
  (var prev (xt/x:get-key queries query-key))
  (when (xt/x:not-nil? prev)
    (-/remove-query-watch state query-key (xt/x:get-key prev "tables"))
    (xt/x:del-key queries query-key))
  (return prev))
