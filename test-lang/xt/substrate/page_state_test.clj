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
