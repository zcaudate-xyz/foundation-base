(ns xt.db.node.instance-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]
             [xt.db.node.schema-spec :as spec]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.node.instance-util :as util]
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

(defn.xt put-model
  "stores a model and normalizes its views"
  {:added "4.1"}
  [state model-id model-spec]
  (var views (schema-state/model-views model-spec))
  (var model {:id model-id
              :meta (or (xt/x:get-key model-spec "meta") {})
              :views {}})
  (xt/for:object [[view-id view] views]
    (xtd/set-in model ["views" view-id]
                (schema-state/normalize-view view-id view)))
  (xtd/set-in state ["models" model-id] model)
  (return model))

(defn.xt put-view
  "stores a single view on an existing model"
  {:added "4.1"}
  [state model-id view-id view-spec]
  (var model (schema-state/ensure-model state model-id))
  (var view (schema-state/normalize-view view-id view-spec))
  (xtd/set-in model ["views" view-id] view)
  (return view))

(defn.xt set-view-input
  "sets input on a view"
  {:added "4.1"}
  [state model-id view-id input]
  (var view (schema-state/ensure-view state model-id view-id))
  (xt/x:set-key view "input" input)
  (return view))

(defn.xt set-view-pending
  "marks a view as pending"
  {:added "4.1"}
  [state model-id view-id]
  (var view (schema-state/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" true)
  (xt/x:set-key view "status" spec/STATUS_PENDING)
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt set-view-success
  "stores the current view output"
  {:added "4.1"}
  [state model-id view-id query-key value tables]
  (var view (schema-state/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_READY)
  (xt/x:set-key view "error" nil)
  (xt/x:set-key view "query_key" query-key)
  (xt/x:set-key view "value" value)
  (xt/x:set-key view "tables" (or tables {}))
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (return view))

(defn.xt set-view-error
  "stores a view error"
  {:added "4.1"}
  [state model-id view-id error]
  (var view (schema-state/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_ERROR)
  (xt/x:set-key view "error" error)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (return view))

(defn.xt set-view-stale
  "marks a view as stale"
  {:added "4.1"}
  [state model-id view-id reason]
  (var view (schema-state/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_STALE)
  (xt/x:set-key view "error" reason)
  (return view))

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
