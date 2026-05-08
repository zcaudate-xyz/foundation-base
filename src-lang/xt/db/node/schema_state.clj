(ns xt.db.node.schema-state
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.schema-spec :as spec]
             [xt.event.base-view :as event-view]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

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
                {}
                default-input
                default-value
                nil
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
