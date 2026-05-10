(ns xt.db.node.instance-query
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]
             [xt.db.node.schema-spec :as spec]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.node.schema-query :as schema-query]
             [xt.db.node.instance-state :as instance-state]
             [xt.event.base-view :as event-view]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt seen-view?
  "checks whether a view has already been visited in a local graph walk"
  {:added "4.1"}
  [visited model-id view-id]
  (return (== true (xtd/get-in visited [model-id view-id]))))

(defn.xt mark-view-seen
  "marks a view as visited in a local graph walk"
  {:added "4.1"}
  [visited model-id view-id]
  (xtd/set-in visited [model-id view-id] true)
  (return visited))

(defn.xt view-context
  "gets the current local view context from state"
  {:added "4.1"}
  [state model-id view-id]
  (var view (schema-state/ensure-view state model-id view-id))
  (var current (xt/x:get-key (event-view/get-input view) "current"))
  (var args [])
  (cond (xt/x:is-object? current)
        (:= args (or (xt/x:get-key current "data")
                     []))

        (xt/x:is-array? current)
        (:= args current))
  (return {:model-id model-id
           :view-id view-id
           :args args}))

(defn.xt view-remote-spec
  "gets the configured remote spec for a view, if present"
  {:added "4.1"}
  [view]
  (var remote (xt/x:get-key view "remote"))
  (if (and (xt/x:is-object? remote)
           (or (xt/x:has-key? remote "space")
               (xt/x:has-key? remote "target")
               (xt/x:has-key? remote "transport")
               (xt/x:has-key? remote "meta")))
    (return remote)
    (return nil)))

(defn.xt mark-view-dependents-stale
  "marks dependent views stale through the local dependency graph"
  {:added "4.1"}
  [state model-id view-id reason visited]
  (when (xt/x:nil? visited)
    (:= visited {}))
  (-/mark-view-seen visited model-id view-id)
  (xt/for:object [[dmodel-id dview-ids] (instance-state/get-view-dependents state model-id view-id)]
    (xt/for:array [dview-id dview-ids]
      (when (not (-/seen-view? visited dmodel-id dview-id))
        (instance-state/set-view-stale state dmodel-id dview-id reason)
        (-/mark-view-dependents-stale state dmodel-id dview-id reason visited))))
  (return visited))

(defn.xt refresh-view-local
  "refreshes a single local view and then its dependents"
  {:added "4.1"}
  [state model-id view-id visited]
  (when (xt/x:nil? visited)
    (:= visited {}))
  (when (-/seen-view? visited model-id view-id)
    (return visited))
  (-/mark-view-seen visited model-id view-id)
  (var dview (schema-state/ensure-view state model-id view-id))
  (if (xt/x:not-nil? (-/view-remote-spec dview))
    (-/mark-view-dependents-stale
     state
     model-id
     view-id
     {:status "stale"
      :tag "db.node/dependent-remote-refresh-required"}
     visited)
    (do (var view-context (-/view-context state model-id view-id))
        (var [ok prepared] (schema-query/prepare-query
                            state
                            (xt/x:get-key dview "query")
                            view-context))
        (if (not ok)
          (instance-state/set-view-error state model-id view-id prepared)
          (do (var plan (xt/x:get-key prepared "plan"))
              (var value (:? (xt/x:not-nil? plan)
                             (instance/db-pull-sync
                              (instance-state/ensure-db state)
                              (schema-state/get-schema state)
                              plan)
                             nil))
              (-/attach-query-entry
               state
               prepared
               value
               (xt/x:get-key prepared "tables")
               model-id
               view-id)
              (-/refresh-view-dependents-local state model-id view-id visited)))))
  (return visited))

(defn.xt refresh-view-dependents-local
  "refreshes dependent views through the local dependency graph"
  {:added "4.1"}
  [state model-id view-id visited]
  (when (xt/x:nil? visited)
    (:= visited {}))
  (-/mark-view-seen visited model-id view-id)
  (xt/for:object [[dmodel-id dview-ids] (instance-state/get-view-dependents state model-id view-id)]
    (xt/for:array [dview-id dview-ids]
      (when (not (-/seen-view? visited dmodel-id dview-id))
        (-/refresh-view-local state dmodel-id dview-id visited))))
  (return visited))

(defn.xt mark-view-bindings-stale
  "marks bound views and their dependents stale"
  {:added "4.1"}
  [state bindings reason]
  (var visited {})
  (xt/for:object [[model-id views] (or bindings {})]
    (xt/for:object [[view-id _] views]
      (instance-state/set-view-stale state model-id view-id reason)
      (-/mark-view-dependents-stale state model-id view-id reason visited)))
  (return visited))

(defn.xt refresh-view-bindings-local
  "refreshes bound views and their dependents locally"
  {:added "4.1"}
  [state bindings]
  (var visited {})
  (xt/for:object [[model-id views] (or bindings {})]
    (xt/for:object [[view-id _] views]
      (-/refresh-view-local state model-id view-id visited)))
  (return visited))

(defn.xt attach-query-entry
  "stores a query result in cache state"
  {:added "4.1"}
  [state prepared value tables model-id view-id]
  (var query-key (xt/x:get-key prepared "key"))
  (var queries (xt/x:get-key state "queries"))
  (var prev (xt/x:get-key queries query-key))
  (when (xt/x:not-nil? prev)
    (instance-state/remove-query-watch state query-key (xt/x:get-key prev "tables")))
  (var bindings (:? (xt/x:not-nil? prev)
                    (or (xt/x:get-key prev "bindings") {})
                    {}))
  (when (and (xt/x:not-nil? model-id)
             (xt/x:not-nil? view-id))
    (xtd/set-in bindings [model-id view-id] true))
  (var entry (xt/x:obj-assign
              (or prev {})
              {:key query-key
               :query (xt/x:get-key prepared "query")
               :context (xt/x:get-key prepared "context")
               :plan (xt/x:get-key prepared "plan")
               :tables (or tables (xt/x:get-key prepared "tables") {})
               :value value
               :status spec/STATUS_READY
               :error nil
               :t (xt/x:now-ms)
               :bindings bindings}))
  (xt/x:set-key queries query-key entry)
  (instance-state/watch-query state query-key (xt/x:get-key entry "tables"))
  (when (and (xt/x:not-nil? model-id)
             (xt/x:not-nil? view-id))
    (instance-state/set-view-success state
                                     model-id
                                     view-id
                                     query-key
                                     value
                                     (xt/x:get-key entry "tables")))
  (return entry))

(defn.xt mark-query-stale
  "marks a cached query and its bound views as stale"
  {:added "4.1"}
  [state query-key reason]
  (var entry (xtd/get-in state ["queries" query-key]))
  (when (xt/x:nil? entry)
    (return nil))
  (xt/x:set-key entry "status" spec/STATUS_STALE)
  (xt/x:set-key entry "error" reason)
  (-/mark-view-bindings-stale state (xt/x:get-key entry "bindings") reason)
  (return entry))

(defn.xt mark-query-stale-many
  "marks many cached queries as stale"
  {:added "4.1"}
  [state query-keys reason]
  (var out [])
  (xt/for:array [query-key query-keys]
    (xt/x:arr-push out (-/mark-query-stale state query-key reason)))
  (return out))

(defn.xt refresh-query-entry
  "refreshes a cached query from the local db instance"
  {:added "4.1"}
  [state query-key]
  (var entry (xtd/get-in state ["queries" query-key]))
  (when (xt/x:nil? entry)
    (return nil))
  (var plan (xt/x:get-key entry "plan"))
  (when (xt/x:nil? plan)
    (return (-/mark-query-stale state query-key {:status "error"
                                                 :tag "db.node/query-plan-not-found"
                                                 :data {:query-key query-key}})))
  (var value (instance/db-pull-sync
              (instance-state/ensure-db state)
              (schema-state/get-schema state)
              plan))
  (xt/x:set-key entry "value" value)
  (xt/x:set-key entry "status" spec/STATUS_READY)
  (xt/x:set-key entry "error" nil)
  (xt/x:set-key entry "t" (xt/x:now-ms))
  (xt/for:object [[model-id views] (or (xt/x:get-key entry "bindings") {})]
    (xt/for:object [[view-id _] views]
      (instance-state/set-view-success state
                                       model-id
                                       view-id
                                       query-key
                                       value
                                       (xt/x:get-key entry "tables"))))
  (-/refresh-view-bindings-local state (xt/x:get-key entry "bindings"))
  (return entry))

(defn.xt refresh-query-keys
  "refreshes many cached queries"
  {:added "4.1"}
  [state query-keys]
  (var out [])
  (xt/for:array [query-key query-keys]
    (xt/x:arr-push out (-/refresh-query-entry state query-key)))
  (return out))

(defn.xt run-local-query
  "prepares and executes a local query"
  {:added "4.1"}
  [state query-spec view-context model-id view-id]
  (var [ok prepared] (schema-query/prepare-query state query-spec view-context))
  (when (not ok)
    (when (and (xt/x:not-nil? model-id)
               (xt/x:not-nil? view-id))
      (instance-state/set-view-error state model-id view-id prepared))
    (return [ok prepared]))
  (var plan (xt/x:get-key prepared "plan"))
  (var value (:? (xt/x:not-nil? plan)
                 (instance/db-pull-sync
                  (instance-state/ensure-db state)
                  (schema-state/get-schema state)
                  plan)
                 nil))
  (var entry (-/attach-query-entry state
                                   prepared
                                   value
                                   (xt/x:get-key prepared "tables")
                                   model-id
                                   view-id))
  (return [true {:query_key (xt/x:get-key entry "key")
                 :value value
                 :tables (xt/x:get-key entry "tables")}]))
