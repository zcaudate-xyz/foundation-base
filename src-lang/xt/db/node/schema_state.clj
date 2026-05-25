(ns xt.db.node.schema-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.substrate.page-state :as page-state]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(defn.xt identity-wrapper
  "passes through a context-aware handler"
  {:added "4.1"}
  [handler]
  (return (page-state/identity-wrapper handler)))

(defn.xt output-process
  "extracts the public view value from a db node result payload"
  {:added "4.1"}
  [value]
  (return (page-state/output-process value)))

(defn.xt normalize-dep
  "normalizes a dependency path into [model-id view-id]"
  {:added "4.1"}
  [model-id dep]
  (return (page-state/normalize-view-dep model-id dep)))

(defn.xt get-view-deps
  "gets normalized dependencies for a single view"
  {:added "4.1"}
  [model-id view]
  (return (xt/x:get-key (page-state/get-view-deps model-id view) "views")))

(defn.xt get-model-deps
  "gets view dependency indexes for a model"
  {:added "4.1"}
  [model-id views]
  (return (page-state/get-model-deps model-id views)))

(defn.xt get-unknown-deps
  "gets unresolved dependency paths for a model"
  {:added "4.1"}
  [state model-id views model-deps]
  (return (page-state/get-unknown-deps state model-id views model-deps)))

(defn.xt base-state
  "creates the base xt.db state"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (var state (page-state/base-state opts))
  (xt/x:set-key state "::" spec/STATE_TAG)
  (xt/x:set-key state "db" nil)
  (xt/x:set-key state "schema" (or (xt/x:get-key opts "schema") {}))
  (xt/x:set-key state "lookup" (or (xt/x:get-key opts "lookup") {}))
  (xt/x:set-key state "views" (or (xt/x:get-key opts "views") {}))
  (xt/x:set-key state "queries" {})
  (xt/x:set-key state "watch" {})
  (xt/x:set-key state "view_watch" {})
  (xt/x:set-key state "pending" {})
  (xt/x:set-key state "remote" (or (xt/x:get-key opts "remote") {}))
  (return state))

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
  (return (page-state/model-views model-spec)))

(defn.xt normalize-view
  "normalizes a single view record"
  {:added "4.1"}
  [view-id view]
  (var runtime (page-state/normalize-view view-id view))
  (xt/x:set-key runtime "tables" {})
  (xt/x:set-key runtime "query_key" nil)
  (return runtime))

(defn.xt get-model
  "gets a registered model"
  {:added "4.1"}
  [state model-id]
  (return (page-state/get-model state model-id)))

(defn.xt ensure-model
  "gets a registered model or throws"
  {:added "4.1"}
  [state model-id]
  (return (page-state/ensure-model state model-id)))

(defn.xt get-view
  "gets a registered view"
  {:added "4.1"}
  [state model-id view-id]
  (return (page-state/get-view state model-id view-id)))

(defn.xt ensure-view
  "gets a registered view or throws"
  {:added "4.1"}
  [state model-id view-id]
  (return (page-state/ensure-view state model-id view-id)))
