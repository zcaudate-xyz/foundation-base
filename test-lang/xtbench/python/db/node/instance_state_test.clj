(ns xtbench.python.db.node.instance-state-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.node.instance-state :as instance-state]
             [xt.db.node.schema-state :as schema-state]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +schema+
  {"Order"
   {"id" {"ident" "id", "type" "text", "order" 0}
    "status" {"ident" "status", "type" "text", "order" 1}}})

(def +lookup+
  {"Order" {"position" 0}})

(def +model-spec+
  {"views"
   {"main"
    {"default_input" ["ord-1"]
     "query" {:table "Order"
              :return-method "default"
              :return-id "ord-1"}}}})

^{:refer xt.db.node.instance-state/ensure-state :added "4.1"}
(fact "attaches node state to a space when missing"

  (!.py
    (var space {})
    (var node {"meta" {"xt.db" {"schema" (@! +schema+)
                                "lookup" (@! +lookup+)}}})
    (var state (instance-state/ensure-state space node))
    [(. state ["::"])
     (. (. space ["state"]) ["::"])])
  => ["xt.db.state" "xt.db.state"])

^{:refer xt.db.node.instance-state/ensure-db :added "4.1"}
(fact "creates the local cache db on demand"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "lookup" (@! +lookup+)}))
    (. (instance-state/ensure-db state) ["::"]))
  => "db.cache")

^{:refer xt.db.node.instance-state/put-model :added "4.1"}
(fact "stores a model and normalizes its views"

  (!.py
    (var state (schema-state/base-state {}))
    (var model (instance-state/put-model state "orders" (@! +model-spec+)))
    [(. model ["id"])
     (xtd/get-in state ["models" "orders" "views" "main" "input"])
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => ["orders"
      ["ord-1"]
      "idle"])

^{:refer xt.db.node.instance-state/put-view :added "4.1"}
(fact "stores a single view on an existing model"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" {"views" {}})
    (var view
         (instance-state/put-view
          state
          "orders"
          "secondary"
          {"default_input" ["ord-2"]}))
    [(. view ["id"])
     (. view ["input"])])
  => ["secondary"
      ["ord-2"]])

^{:refer xt.db.node.instance-state/set-view-input :added "4.1"}
(fact "updates a view input"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (. (instance-state/set-view-input state "orders" "main" ["ord-2"]) ["input"]))
  => ["ord-2"])

^{:refer xt.db.node.instance-state/set-view-pending :added "4.1"}
(fact "marks a view as pending"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view (instance-state/set-view-pending state "orders" "main"))
    [(. view ["pending"])
     (. view ["status"])
     (. view ["error"])])
  => [true
      "pending"
      nil])

^{:refer xt.db.node.instance-state/set-view-success :added "4.1"}
(fact "stores a successful view value"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-success
          state
          "orders"
          "main"
          "q1"
          [{"status" "open"}]
          {"Order" true}))
    [(. view ["status"])
     (. view ["query_key"])
     (. view ["value"])
     (. view ["tables"])
     (xt/x:is-number? (. view ["updated_at"]))])
  => ["ready"
      "q1"
      [{"status" "open"}]
      {"Order" true}
      true])

^{:refer xt.db.node.instance-state/set-view-error :added "4.1"}
(fact "stores a view error"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-error
          state
          "orders"
          "main"
          {"status" "error"
           "tag" "boom"}))
    [(. view ["status"])
     (. view ["error"] ["tag"])
     (. view ["pending"])])
  => ["error"
      "boom"
      false])

^{:refer xt.db.node.instance-state/set-view-stale :added "4.1"}
(fact "marks a view as stale"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var view
         (instance-state/set-view-stale
          state
          "orders"
          "main"
          "changed"))
    [(. view ["status"])
     (. view ["error"])
     (. view ["pending"])])
  => ["stale"
      "changed"
      false])

^{:refer xt.db.node.instance-state/remove-query-watch :added "4.1"}
(fact "removes watched query ids from selected tables"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true "Audit" true})
    (instance-state/remove-query-watch state "q1" {"Order" true})
    [(. (. state ["watch"]) ["Order"])
     (xt/x:obj-keys (. (. state ["watch"]) ["Audit"]))])
  => [nil
      ["q1"]])

^{:refer xt.db.node.instance-state/watch-query :added "4.1"}
(fact "indexes watched queries by table"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true "Audit" true})
    [(xt/x:obj-keys (. state ["watch"]))
     (. (. (. state ["watch"]) ["Order"]) ["q1"])])
  => [["Order" "Audit"]
      true])

^{:refer xt.db.node.instance-state/affected-query-ids :added "4.1"}
(fact "collects affected query ids from watched tables"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true})
    (instance-state/watch-query state "q2" {"Audit" true})
    (instance-state/watch-query state "q3" {"Order" true "Audit" true})
    [(xtd/arr-lookup (instance-state/affected-query-ids state ["Order"]))
     (xtd/arr-lookup (instance-state/affected-query-ids state {"Audit" true}))])
  => [{"q1" true "q3" true}
      {"q2" true "q3" true}])

^{:refer xt.db.node.instance-state/remove-query :added "4.1"}
(fact "removes cached queries and their watch entries"

  (!.py
    (var state (schema-state/base-state {}))
    (instance-state/watch-query state "q1" {"Order" true})
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1"
                                               "tables" {"Order" true}})
    (var prev (instance-state/remove-query state "q1"))
    [(. prev ["key"])
     (xt/x:obj-keys (. state ["queries"]))
     (. (. state ["watch"]) ["Order"])])
  => ["q1"
      []
      nil])
