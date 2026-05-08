(ns xtbench.python.db.node.schema-state-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
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

  (!.py
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

  (!.py
    (xt/x:obj-keys
     (schema-state/get-schema
      (schema-state/base-state {"schema" (@! +schema+)}))))
  => ["Order"])

^{:refer xt.db.node.schema-state/get-views :added "4.1"}
(fact "gets the configured view map"

  (!.py
    (xt/x:obj-keys
     (schema-state/get-views
      (schema-state/base-state {"views" (@! +views+)}))))
  => ["Order"])

^{:refer xt.db.node.schema-state/model-views :added "4.1"}
(fact "normalizes models with or without an explicit views key"

  (!.py
    [(xt/x:obj-keys
      (schema-state/model-views {"views" {"main" {"query" {"table" "Order"}}}}))
     (xt/x:obj-keys
      (schema-state/model-views {"main" {"query" {"table" "Order"}}}))])
  => [["main"] ["main"]])

^{:refer xt.db.node.schema-state/normalize-view :added "4.1"}
(fact "normalizes a single view record"

  (!.py
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

^{:refer xt.db.node.schema-state/get-model :added "4.1"}
(fact "gets a registered model"

  (!.py
    (schema-state/get-model
     {"models" {"orders" {"id" "orders"}}}
     "orders"))
  => {"id" "orders"})

^{:refer xt.db.node.schema-state/ensure-model :added "4.1"}
(fact "returns an existing model"

  (!.py
    (. (schema-state/ensure-model
        {"models" {"orders" {"id" "orders"}}}
        "orders")
       ["id"]))
  => "orders")

^{:refer xt.db.node.schema-state/get-view :added "4.1"}
(fact "gets a registered view"

  (!.py
    (schema-state/get-view
     {"models" {"orders" {"views" {"main" {"id" "main"}}}}}
     "orders"
     "main"))
  => {"id" "main"})

^{:refer xt.db.node.schema-state/ensure-view :added "4.1"}
(fact "returns an existing view"

  (!.py
    (. (schema-state/ensure-view
        {"models" {"orders" {"views" {"main" {"id" "main"}}}}}
        "orders"
        "main")
       ["id"]))
  => "main")
