(ns xt.substrate.page-state-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate.page-state :as page-state]
             [xt.event.base-view :as event-view]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-state/base-state :added "4.1"}
(fact "creates the base page state"
  (!.js
    (var state (page-state/base-state {"meta" {"label" "page-node"}}))
    [(. state ["::"])
     (. state ["meta"] ["label"])
     (xt/x:obj-keys (. state ["models"]))])
  => ["substrate.page.state" "page-node" []])

^{:refer xt.substrate.page-state/get-view-deps :added "4.1"}
(fact "normalizes view and state dependency declarations"
  (!.js
    (page-state/get-view-deps
     "orders"
     {"deps" {"views" ["main" ["stats" "daily"]]
              "state" ["selected_id" ["filters" "status"]]}}))
  => {"views" [["orders" "main"]
               ["stats" "daily"]]
      "state" [["selected_id"]
                ["filters" "status"]]})

^{:refer xt.substrate.page-state/normalize-view :added "4.1"}
(fact "normalizes a page view using only the standard input key"
  (!.js
    (var view
         (page-state/normalize-view
          "detail"
          {"input" ["task-1"]
           "default_input" ["ignored"]
           "resolver" {"type" "fn/local"}
           "value" {"name" "alpha"}}))
    [(. view ["id"])
     (. view ["::"])
     (. view ["resolver"] ["type"])
     (xt/x:get-path (event-view/get-input view) ["current" "data"])
     (. view ["status"])])
  => ["detail" "event.view" "fn/local" ["task-1"] "idle"])

^{:refer xt.substrate.page-state/put-model :added "4.1"}
(fact "stores state and indexes both view and state dependents"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model
     state
     "orders"
     {"state" {"selected_id" nil}
      "views" {"list" {"resolver" {"type" "fn/local"}}
               "detail" {"deps" {"views" ["list"]
                                  "state" ["selected_id"]}
                         "resolver" {"type" "fn/local"}}}})
    [(page-state/get-view-dependents state "orders" "list")
     (page-state/get-state-dependents state "orders" ["selected_id"])])
  => [{"orders" ["detail"]}
      ["detail"]])


^{:refer xt.substrate.page-state/identity-wrapper :added "4.1"}
(fact "passes through the handler unchanged"
  (!.js
    (var handler (fn [ctx] (return ctx)))
    [(xt/x:is-function? (page-state/identity-wrapper handler))
     (. ((page-state/identity-wrapper handler) {"id" "task-1"}) ["id"])])
  => [true "task-1"])

^{:refer xt.substrate.page-state/output-process :added "4.1"}
(fact "extracts nested value when present"
  (!.js
    [(page-state/output-process {"value" {"id" "task-1"}})
     (page-state/output-process {"id" "task-2"})])
  => [{"id" "task-1"}
      {"id" "task-2"}])

^{:refer xt.substrate.page-state/normalize-view-dep :added "4.1"}
(fact "normalizes view dependency paths"
  (!.js
    [(page-state/normalize-view-dep "orders" ["detail"])
     (page-state/normalize-view-dep "orders" ["stats" "daily"])
     (page-state/normalize-view-dep "orders" "list")
     (page-state/normalize-view-dep "orders" {"model" "stats" "view" "daily"})
     (page-state/normalize-view-dep "orders" nil)])
  => [["orders" "detail"]
      ["stats" "daily"]
      ["orders" "list"]
      ["stats" "daily"]
      nil])

^{:refer xt.substrate.page-state/normalize-state-dep :added "4.1"}
(fact "normalizes state dependency paths"
  (!.js
    [(page-state/normalize-state-dep ["filters" "status"])
     (page-state/normalize-state-dep "selected_id")
     (page-state/normalize-state-dep {"path" ["filters" "owner"]})
     (page-state/normalize-state-dep {"key" "selected_id"})
     (page-state/normalize-state-dep nil)])
  => [["filters" "status"]
      ["selected_id"]
      ["filters" "owner"]
      ["selected_id"]
      nil])

^{:refer xt.substrate.page-state/state-path-key :added "4.1"}
(fact "builds a stable key for a state path"
  (!.js
    (page-state/state-path-key ["filters" "status"]))
  => "[\"filters\",\"status\"]")

^{:refer xt.substrate.page-state/get-model-deps :added "4.1"}
(fact "indexes dependent views by source view"
  (!.js
    (page-state/get-model-deps
     "orders"
     {"detail" {"deps" {"views" ["list"]}}
      "summary" {"deps" {"views" [["stats" "daily"]]}}}))
  => {"orders" {"list" {"detail" true}}
      "stats" {"daily" {"summary" true}}})

^{:refer xt.substrate.page-state/get-model-state-deps :added "4.1"}
(fact "indexes dependent views by model state path"
  (!.js
    (page-state/get-model-state-deps
     {"detail" {"deps" {"state" ["selected_id"]}}
      "summary" {"deps" {"state" [["filters" "status"]]}}}))
  => {"[\"selected_id\"]" {"detail" true}
      "[\"filters\",\"status\"]" {"summary" true}})

^{:refer xt.substrate.page-state/get-unknown-deps :added "4.1"}
(fact "finds dependency paths missing from state"
  (!.js
    (page-state/get-unknown-deps
     {"models" {"stats" {"views" {"daily" {}}}}}
     "orders"
     {"detail" {}}
     {"orders" {"detail" true
                "missing" true}
      "stats" {"daily" true
               "weekly" true}
      "ghost" {"view" true}}))
  => [["orders" "missing"]
      ["stats" "weekly"]
      ["ghost" "view"]])

^{:refer xt.substrate.page-state/model-views :added "4.1"}
(fact "delegates to page-spec model view extraction"
  (!.js
    [(xt/x:not-nil? (. (page-state/model-views {"views" {"list" {}}}) ["list"]))
     (xt/x:not-nil? (. (page-state/model-views {"detail" {}}) ["detail"]))])
  => [true true])

^{:refer xt.substrate.page-state/normalize-model :added "4.1"}
(fact "normalizes state, actions, views, and dependency indexes"
  (!.js
    (var model
         (page-state/normalize-model
          "orders"
          {"state" {"selected_id" nil}
           "actions" {"save" {}}
           "views" {"list" {"resolver" {"type" "fn/local"}}
                    "detail" {"deps" {"views" ["list"]
                                       "state" ["selected_id"]}
                              "resolver" {"type" "fn/local"}}}}))
    {"id" (. model ["id"])
     "state_keys" (xt/x:obj-keys (. model ["state"]))
     "action_keys" (xt/x:obj-keys (. model ["actions"]))
     "view_keys" (xt/x:obj-keys (. model ["views"]))
     "unknown_deps" (. model ["unknown_deps"])})
  => {"id" "orders"
      "state_keys" ["selected_id"]
      "action_keys" ["save"]
      "view_keys" ["list" "detail"]
      "unknown_deps" []})

^{:refer xt.substrate.page-state/get-model :added "4.1"}
(fact "gets a registered model"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"list" {}}})
    [(. (page-state/get-model state "orders") ["id"])
     (page-state/get-model state "missing")])
  => ["orders" nil])

^{:refer xt.substrate.page-state/ensure-model :added "4.1"}
(fact "returns an existing model"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"list" {}}})
    (. (page-state/ensure-model state "orders") ["id"]))
  => "orders")

^{:refer xt.substrate.page-state/get-view :added "4.1"}
(fact "gets a registered view"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    [(. (page-state/get-view state "orders" "detail") ["id"])
     (page-state/get-view state "orders" "missing")])
  => ["detail" nil])

^{:refer xt.substrate.page-state/ensure-view :added "4.1"}
(fact "returns an existing view"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    (. (page-state/ensure-view state "orders" "detail") ["id"]))
  => "detail")

^{:refer xt.substrate.page-state/rebuild-model :added "4.1"}
(fact "recomputes dependency indexes for an updated model"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "stats" {"views" {"daily" {}}})
    (page-state/put-model state "orders" {"views" {"list" {}
                                                    "detail" {"deps" {"views" ["list"]}}}})
    (page-state/put-view state "orders" "summary" {"deps" {"views" [["stats" "daily"]]}})
    (var model (page-state/rebuild-model state "orders"))
    {"deps" (. model ["deps"])
     "unknown_deps" (. model ["unknown_deps"])})
  => {"deps" {"orders" {"list" {"detail" true}}
              "stats" {"daily" {"summary" true}}}
      "unknown_deps" []})

^{:refer xt.substrate.page-state/put-view :added "4.1"}
(fact "adds a single view onto an existing model"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"list" {}}})
    (page-state/put-view state "orders" "detail" {"deps" {"views" ["list"]}})
    [(xt/x:not-nil? (. (page-state/get-view state "orders" "detail") ["id"]))
     (. (page-state/get-model state "orders") ["deps"])])
  => [true
      {"orders" {"list" {"detail" true}}}])

^{:refer xt.substrate.page-state/get-model-state :added "4.1"}
(fact "gets model local state at either the root or a nested path"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"state" {"selected_id" "task-1"
                                                    "filters" {"status" "open"}}
                                         "views" {"list" {}}})
    [(page-state/get-model-state state "orders" nil)
     (page-state/get-model-state state "orders" ["filters" "status"])])
  => [{"selected_id" "task-1"
       "filters" {"status" "open"}}
      nil])

^{:refer xt.substrate.page-state/set-model-state :added "4.1"}
(fact "sets model state at either the root or a nested path"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"state" {"selected_id" nil}
                                         "views" {"list" {}}})
    (page-state/set-model-state state "orders" ["selected_id"] "task-9")
    (page-state/set-model-state state "orders" nil {"selected_id" "task-10"})
    (page-state/get-model-state state "orders" nil))
  => {"selected_id" "task-10"})

^{:refer xt.substrate.page-state/set-view-input :added "4.1"}
(fact "updates the view input vector"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    (. (page-state/set-view-input state "orders" "detail" ["task-3"]) ["input"]))
  => ["task-3"])

^{:refer xt.substrate.page-state/set-view-ready :added "4.1"}
(fact "marks a view ready and clears errors"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    (page-state/set-view-error state "orders" "detail" {"message" "boom"})
    (var view (page-state/set-view-ready state "orders" "detail"))
    [(. view ["status"])
     (. view ["pending"])
     (xt/x:is-number? (. view ["updated_at"]))
     (. view ["error"])])
  => ["ready" false true nil])

^{:refer xt.substrate.page-state/set-view-error :added "4.1"}
(fact "marks a view errored"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    (var view (page-state/set-view-error state "orders" "detail" {"message" "boom"}))
    [(. view ["status"])
     (. view ["pending"])
     (. view ["error"] ["message"])])
  => ["error" false "boom"])

^{:refer xt.substrate.page-state/set-view-value :added "4.1"}
(fact "stores a view value and marks the view ready"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"detail" {"resolver" {"type" "fn/local"}}}})
    (var view (page-state/set-view-value state "orders" "detail" "resolver" {"id" "task-4"}))
    [(. view ["status"])
     (. view ["source"])
     (. view ["value"] ["id"])])
  => ["ready" "resolver" "task-4"])

^{:refer xt.substrate.page-state/get-view-dependents :added "4.1"}
(fact "gets dependent views for a source view"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"views" {"list" {}
                                                    "detail" {"deps" {"views" ["list"]}}}})
    (page-state/put-model state "stats" {"views" {"daily" {"deps" {"views" [["orders" "list"]]}}}})
    (page-state/get-view-dependents state "orders" "list"))
  => {"orders" ["detail"]
      "stats" ["daily"]})

^{:refer xt.substrate.page-state/get-state-dependents :added "4.1"}
(fact "gets dependent views for a model state path"
  (!.js
    (var state (page-state/base-state nil))
    (page-state/put-model state "orders" {"state" {"selected_id" nil}
                                         "views" {"detail" {"deps" {"state" ["selected_id"]}}}})
    (page-state/get-state-dependents state "orders" ["selected_id"]))
  => ["detail"])

^{:refer xt.substrate.page-state/snapshot-state :added "4.1"}
(fact "returns the public page state snapshot"
  (!.js
    (var state (page-state/base-state {"meta" {"label" "ui"}}))
    (page-state/put-model state "orders" {"views" {"list" {}}})
    {"keys" (xt/x:obj-keys (page-state/snapshot-state state))
     "models" (xt/x:obj-keys (. (page-state/snapshot-state state) ["models"]))
     "meta" (. (page-state/snapshot-state state) ["meta"] ["label"])})
  => {"keys" ["models" "meta"]
      "models" ["orders"]
      "meta" "ui"})