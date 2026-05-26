(ns xt.substrate.page-spec
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(def$.xt STATE_TAG "substrate.page.state")

(def$.xt STATUS_IDLE "idle")
(def$.xt STATUS_PENDING "pending")
(def$.xt STATUS_READY "ready")
(def$.xt STATUS_STALE "stale")
(def$.xt STATUS_ERROR "error")

(def$.xt RESOLVER_TYPE_LOCAL "fn/local")
(def$.xt RESOLVER_TYPE_CUSTOM "fn/custom")
(def$.xt RESOLVER_TYPE_API "fn/api")
(def$.xt RESOLVER_TYPE_DB_QUERY "db/query")

(def$.xt TRIGGER_PRE "trigger.pre")
(def$.xt TRIGGER_POST "trigger.post")

(defn.xt model-state
  "gets model state from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "state")
              {})))

(defn.xt model-views
  "gets model views from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "views")
              model-spec
              {})))

(defn.xt model-actions
  "gets model actions from the page spec"
  {:added "4.1"}
  [model-spec]
  (return (or (xt/x:get-key model-spec "actions")
              {})))

(defn.xt view-default-input
  "gets the default input for a view"
  {:added "4.1"}
  [view-spec]
  (return (or (xt/x:get-key view-spec "input")
              (xt/x:get-key view-spec "default_input")
              [])))

(defn.xt view-source
  "gets the declared source for a view"
  {:added "4.1"}
  [view-spec]
  (return (or (xt/x:get-key view-spec "source")
              (xtd/get-in view-spec ["use" "source"])
              "caching")))

(defn.xt view-resolver
  "gets the resolver for a view"
  {:added "4.1"}
  [view-spec]
  (return (or (xt/x:get-key view-spec "resolver")
              {})))

(defn.xt resolver-type
  "gets the resolver type"
  {:added "4.1"}
  [resolver]
  (return (or (xt/x:get-key resolver "type")
              "fn/local")))

(defn.xt resolver-fn
  "gets the executable function for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "fn")))

(defn.xt resolver-args-fn
  "gets the argument builder for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "args_fn")))

(defn.xt resolver-action
  "gets the action for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "action")))

(defn.xt resolver-target
  "gets the target for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "target")))

(defn.xt resolver-meta
  "gets request meta for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (or (xt/x:get-key resolver "meta")
              {})))

(defn.xt resolver-api-template
  "gets the api template reference or inline template for an api resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "api_template")))

(defn.xt resolver-args
  "gets args for a resolver using snake_case keys"
  {:added "4.1"}
  [resolver ctx]
  (var args_fn (-/resolver-args-fn resolver))
  (if (xt/x:is-function? args_fn)
    (return (or (args_fn ctx) []))
    (return (or (xt/x:get-key resolver "args")
                (xt/x:get-key ctx "input")
                []))))

(defn.xt resolver-trigger-pre
  "gets the pre trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "trigger.pre")))

(defn.xt resolver-trigger-post
  "gets the post trigger for a resolver"
  {:added "4.1"}
  [resolver]
  (return (xt/x:get-key resolver "trigger.post")))

(defn.xt view-deps
  "gets normalized dependency groups from a view spec"
  {:added "4.1"}
  [view-spec]
  (var deps (or (xt/x:get-key view-spec "deps") []))
  (return (:? (xt/x:is-object? deps)
              {"views" (or (xt/x:get-key deps "views") [])
               "state" (or (xt/x:get-key deps "state") [])}
              {"views" deps
               "state" []})))
