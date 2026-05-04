(ns xt.db.node.state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.instance :as instance]
             [xt.db.node.spec :as spec]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt node-opts
  "gets xt.db.node options from node metadata"
  {:added "4.1"}
  [node]
  (return (or (xtd/get-in node ["meta" spec/META_KEY])
              {})))

(defn.xt set-node-opts
  "stores xt.db.node options on node metadata"
  {:added "4.1"}
  [node opts]
  (xtd/set-in node ["meta" spec/META_KEY] (or opts {}))
  (return opts))

(defn.xt state?
  "checks for a db.node state map"
  {:added "4.1"}
  [state]
  (return (and (xt/x:is-object? state)
               (== spec/STATE_TAG
                   (xt/x:get-key state "::")))))

(defn.xt ensure-state
  "ensures a db.node state is attached to the current space"
  {:added "4.1"}
  [space node]
  (var state (xt/x:get-key space "state"))
  (when (not (-/state? state))
    (:= state (spec/base-state (-/node-opts node)))
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
            (or (xt/x:get-key state "schema") {})
            (or (xt/x:get-key state "lookup") {})
            (or (xt/x:get-key opts "db_opts") {})))
    (xt/x:set-key state "db" db))
  (return db))

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
  (return
   (xt/x:obj-assign
    {:id view-id
     :input default-input
     :value (xt/x:get-key view "value")
     :status spec/STATUS_IDLE
     :pending false
     :error nil
     :tables {}
     :query_key nil
     :updated_at nil}
    view)))

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

(defn.xt put-model
  "stores a model and normalizes its views"
  {:added "4.1"}
  [state model-id model-spec]
  (var views (-/model-views model-spec))
  (var model {:id model-id
              :meta (or (xt/x:get-key model-spec "meta") {})
              :views {}})
  (xt/for:object [[view-id view] views]
    (xtd/set-in model ["views" view-id]
                (-/normalize-view view-id view)))
  (xtd/set-in state ["models" model-id] model)
  (return model))

(defn.xt put-view
  "stores a single view on an existing model"
  {:added "4.1"}
  [state model-id view-id view-spec]
  (var model (-/ensure-model state model-id))
  (var view (-/normalize-view view-id view-spec))
  (xtd/set-in model ["views" view-id] view)
  (return view))

(defn.xt set-view-input
  "sets input on a view"
  {:added "4.1"}
  [state model-id view-id input]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "input" input)
  (return view))

(defn.xt set-view-pending
  "marks a view as pending"
  {:added "4.1"}
  [state model-id view-id]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" true)
  (xt/x:set-key view "status" spec/STATUS_PENDING)
  (xt/x:set-key view "error" nil)
  (return view))

(defn.xt set-view-success
  "stores the current view output"
  {:added "4.1"}
  [state model-id view-id query-key value tables]
  (var view (-/ensure-view state model-id view-id))
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
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_ERROR)
  (xt/x:set-key view "error" error)
  (xt/x:set-key view "updated_at" (xt/x:now-ms))
  (return view))

(defn.xt set-view-stale
  "marks a view as stale"
  {:added "4.1"}
  [state model-id view-id reason]
  (var view (-/ensure-view state model-id view-id))
  (xt/x:set-key view "pending" false)
  (xt/x:set-key view "status" spec/STATUS_STALE)
  (xt/x:set-key view "error" reason)
  (return view))
