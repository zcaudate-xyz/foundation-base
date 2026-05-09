(ns xt.db.node.schema-state-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.schema-state :as schema-state]
              [xt.db.node.schema-spec :as spec]
              [xt.event.base-view :as event-view]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

(def +schema+
  {"Order"
   {"id" {"ident" "id", "type" "text", "order" 0}
    "status" {"ident" "status", "type" "text", "order" 1}}})

(def +views+
  {"Order"
   {"return"
    {"default"
     {"input" [{"symbol" "i_order_id", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "return"
              "tag" "default"
              "access" {"roles" {}}
              "guards" []
              "query" ["status"]}}}}})

^{:refer xt.db.node.schema-state/base-state :added "4.1"}
(fact "creates the base node state"

  (!.js
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "remote" {"space" "remote"}
                                   "meta" {"label" "db-node"}}))
    [(. state ["::"])
     (. state ["meta"] ["label"])
     (. state ["remote"] ["space"])
     (xt/x:obj-keys (. state ["queries"]))])
  => ["xt.db.state" "db-node" "remote" []])

^{:refer xt.db.node.schema-state/get-schema :added "4.1"}
(fact "gets the configured schema"

  (!.js
    (xt/x:obj-keys
     (schema-state/get-schema
      (schema-state/base-state {"schema" (@! +schema+)}))))
  => ["Order"])

^{:refer xt.db.node.schema-state/get-views :added "4.1"}
(fact "gets the configured view map"

  (!.js
    (xt/x:obj-keys
     (schema-state/get-views
      (schema-state/base-state {"views" (@! +views+)}))))
  => ["Order"])

^{:refer xt.db.node.schema-state/model-views :added "4.1"}
(fact "normalizes models with or without an explicit views key"

  (!.js
    [(xt/x:obj-keys
      (schema-state/model-views {"views" {"main" {"query" {"table" "Order"}}}}))
     (xt/x:obj-keys
      (schema-state/model-views {"main" {"query" {"table" "Order"}}}))])
  => [["main"] ["main"]])

^{:refer xt.db.node.schema-state/normalize-view :added "4.1"}
(fact "normalizes a single view record"

  (!.js
    (var view
         (schema-state/normalize-view
          "main"
          {"default_input" ["ord-1"]
           "value" [{"status" "open"}]}))
    [(. view ["id"])
     (. view ["::"])
     (xt/x:get-path (event-view/get-input view)
                    ["current" "data"])
     (. view ["status"])
     (. view ["pending"])
     (. view ["value"])])
  => ["main"
      "event.view"
      ["ord-1"]
       "idle"
       false
       [{"status" "open"}]])

^{:refer xt.db.node.schema-state/get-view-deps :added "4.1"}
(fact "normalizes dependency shorthands on a single view"

  (!.js
    (schema-state/get-view-deps
     "orders"
     {"deps" ["main"
               ["audit" "latest"]
               {"model" "stats" "view" "summary"}]}))
  => [["orders" "main"]
      ["audit" "latest"]
      ["stats" "summary"]])

^{:refer xt.db.node.schema-state/get-model-deps :added "4.1"}
(fact "indexes dependent views by source model and view"

  (!.js
    (schema-state/get-model-deps
     "orders"
     {"summary" {"deps" ["main"
                          ["stats" "daily"]]}}))
  => {"orders" {"main" {"summary" true}}
      "stats" {"daily" {"summary" true}}})

^{:refer xt.db.node.schema-state/get-unknown-deps :added "4.1"}
(fact "reports dependency paths that are not currently registered"

  (!.js
    (var views {"summary" {"deps" ["main"
                                    ["stats" "daily"]
                                    ["stats" "missing"]]}})
    (schema-state/get-unknown-deps
     {"models" {"stats" {"views" {"daily" {}}}}}
     "orders"
     views
     (schema-state/get-model-deps "orders" views)))
  => [["orders" "main"]
      ["stats" "missing"]])

^{:refer xt.db.node.schema-state/get-model :added "4.1"}
(fact "gets a registered model"

  (!.js
    (schema-state/get-model
     {"models" {"orders" {"id" "orders"}}}
     "orders"))
  => {"id" "orders"})

^{:refer xt.db.node.schema-state/ensure-model :added "4.1"}
(fact "returns an existing model"

  (!.js
    (. (schema-state/ensure-model
        {"models" {"orders" {"id" "orders"}}}
        "orders")
       ["id"]))
  => "orders")

^{:refer xt.db.node.schema-state/get-view :added "4.1"}
(fact "gets a registered view"

  (!.js
    (schema-state/get-view
     {"models" {"orders" {"views" {"main" {"id" "main"}}}}}
     "orders"
     "main"))
  => {"id" "main"})

^{:refer xt.db.node.schema-state/ensure-view :added "4.1"}
(fact "returns an existing view"

  (!.js
    (. (schema-state/ensure-view
        {"models" {"orders" {"views" {"main" {"id" "main"}}}}}
        "orders"
        "main")
       ["id"]))
  => "main")


^{:refer xt.db.node.schema-state/identity-wrapper :added "4.1"}
(fact "passes handlers through unchanged"

  (!.js
   ((schema-state/identity-wrapper
     (fn [x]
       (return (+ x 1))))
    1))
  => 2)

^{:refer xt.db.node.schema-state/output-process :added "4.1"}
(fact "unwraps db node result payloads that carry a value key"

  (!.js
   [(schema-state/output-process {"value" {"id" "ord-1"}})
    (schema-state/output-process {"id" "ord-2"})])
  => [{"id" "ord-1"}
      {"id" "ord-2"}])

^{:refer xt.db.node.schema-state/normalize-dep :added "4.1"}
(fact "normalizes string vector and map dependency declarations"

  (!.js
   [(schema-state/normalize-dep "orders" "main")
    (schema-state/normalize-dep "orders" ["main"])
    (schema-state/normalize-dep "orders" ["stats" "summary"])
    (schema-state/normalize-dep "orders" {"model" "stats" "view" "summary"})
    (schema-state/normalize-dep "orders" {"id" "main"})
    (schema-state/normalize-dep "orders" 1)])
  => [["orders" "main"]
      ["orders" "main"]
      ["stats" "summary"]
      ["stats" "summary"]
      ["orders" "main"]
      nil])
