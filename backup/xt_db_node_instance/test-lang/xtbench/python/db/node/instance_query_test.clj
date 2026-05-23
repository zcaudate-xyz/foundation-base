(ns xtbench.python.db.node.instance-query-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.db.node.schema-state :as schema-state]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +schema+ fixtures/Schema)

(def +lookup+ fixtures/Lookup)

(def +views+ fixtures/Views)

(def +model-spec+ fixtures/ModelSpec)

(def +dependent-model-spec+ fixtures/DependentModelSpec)

(def +seed+ fixtures/Seed)

^{:refer xt.db.node.instance-query/attach-query-entry :added "4.1"}
(fact "stores cached query results and updates bound views"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/ModelSpec)
    (var entry
         (instance-query/attach-query-entry
          state
          {"key" "q1"
           "query" {"table" "Task"}
           "context" {"model_id" "orders" "view_id" "main"}
           "plan" ["Task" {"id" "00000000-0000-0000-0000-0000000000a1"} ["status"]]
           "tables" {"Task" true}}
          [{"status" "open"}]
          nil
          "orders"
          "main"))
    [(. entry ["key"])
     (. (. state ["watch"]) ["Task"] ["q1"])
     (xtd/get-in state ["models" "orders" "views" "main" "query_key"])
     (xtd/get-in state ["models" "orders" "views" "main" "value" 0 "status"])])
  => ["q1"
      true
      "q1"
      "open"])

^{:refer xt.db.node.instance-query/mark-query-stale :added "4.1"}
(fact "propagates stale status to dependent views"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (instance-query/attach-query-entry
     state
     {"key" "q1"
      "query" {"table" "Task"}
      "context" {}
      "plan" ["Task" {"id" "00000000-0000-0000-0000-0000000000a1"} ["status"]]
      "tables" {"Task" true}}
     [{"status" "open"}]
     nil
     "orders"
     "main")
    (instance-query/mark-query-stale state "q1" "changed")
    [(xtd/get-in state ["models" "orders" "views" "main" "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])

^{:refer xt.db.node.instance-query/mark-query-stale :added "4.1"}
(fact "propagates stale status to dependent views"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (instance-query/attach-query-entry
     state
     {"key" "q1"
      "query" {"table" "Task"}
      "context" {}
      "plan" ["Task" {"id" "00000000-0000-0000-0000-0000000000a1"} ["status"]]
      "tables" {"Task" true}}
     [{"status" "open"}]
     nil
     "orders"
     "main")
    (instance-query/mark-query-stale state "q1" "changed")
    [(xtd/get-in state ["models" "orders" "views" "main" "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])

^{:refer xt.db.node.instance-query/mark-query-stale-many :added "4.1"}
(fact "marks many cached queries as stale"

  (!.py
    (var state (schema-state/base-state {}))
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1" "bindings" {}})
    (xt/x:set-key (. state ["queries"]) "q2" {"key" "q2" "bindings" {}})
    (xtd/arr-map
     (instance-query/mark-query-stale-many state ["q1" "q2"] "changed")
     (fn [e]
       (return (. e ["status"])))))
  => ["stale" "stale"])

^{:refer xt.db.node.instance-query/refresh-query-entry :added "4.1"}
(fact "refreshes dependent local views after a bound query refreshes"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (var db (instance-state/ensure-db state))
    (xdb/sync-event db ["add" fixtures/Seed])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Task"
           :return-method "default"
           :return-id "00000000-0000-0000-0000-0000000000a1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-query/refresh-query-entry state (. result ["query_key"]))
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-query/refresh-query-entry :added "4.1"}
(fact "refreshes dependent local views after a bound query refreshes"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (var db (instance-state/ensure-db state))
    (xdb/sync-event db ["add" fixtures/Seed])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Task"
           :return-method "default"
           :return-id "00000000-0000-0000-0000-0000000000a1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-query/refresh-query-entry state (. result ["query_key"]))
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-query/refresh-query-keys :added "4.1"}
(fact "refreshes many cached query keys"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/ModelSpec)
    (var db (instance-state/ensure-db state))
    (xdb/sync-event db ["add" fixtures/Seed])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Task"
           :return-method "default"
           :return-id "00000000-0000-0000-0000-0000000000a1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (xdb/sync-event db ["add" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1" "status" "closed"}]}])
    (var refreshed
         (instance-query/refresh-query-keys state [(. result ["query_key"])]))
    [(xt/x:len refreshed)
     (xtd/get-in refreshed [0 "value" 0 "status"])])
  => [1
      "closed"])

^{:refer xt.db.node.instance-query/run-local-query :added "4.1"}
(fact "prepares, executes, and caches a local query"

  (!.py
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/ModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" fixtures/Seed])
    (var [ok result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Task"
           :return-method "default"
           :return-id "00000000-0000-0000-0000-0000000000a1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    [ok
     (xtd/get-in result ["value" 0 "status"])
     (. (. result ["tables"]) ["Task"])
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => [true
      "open"
      true
      "ready"])

^{:refer xt.db.node.instance-query/seen-view? :added "4.1"}
(fact "checks whether a view has already been visited"

  (!.py
   [(instance-query/seen-view? {} "orders" "main")
    (instance-query/seen-view? {"orders" {"main" true}} "orders" "main")])
  => [false true])

^{:refer xt.db.node.instance-query/mark-view-seen :added "4.1"}
(fact "marks a nested model and view path as visited"

  (!.py
   (instance-query/mark-view-seen {} "orders" "main"))
  => {"orders" {"main" true}})

^{:refer xt.db.node.instance-query/view-context :added "4.1"}
(fact "returns the current model view and input args"

  (!.py
   (var state (schema-state/base-state {}))
   (instance-state/put-model state "orders" fixtures/ModelSpec)
   (instance-state/set-view-input state "orders" "main" ["00000000-0000-0000-0000-0000000000a2"])
   (instance-query/view-context state "orders" "main"))
  => {"model_id" "orders"
      "view_id" "main"
      "args" ["00000000-0000-0000-0000-0000000000a2"]})

^{:refer xt.db.node.instance-query/view-remote-spec :added "4.1"}
(fact "returns only remote configs that can drive transport refreshes"

  (!.py
   [(instance-query/view-remote-spec {"remote" {"space" "room/b"}})
    (instance-query/view-remote-spec {"remote" {"meta" {"trace" true}}})
    (instance-query/view-remote-spec {"remote" {"channel" "ignored"}})])
  => [{"space" "room/b"}
      {"meta" {"trace" true}}
      nil])

^{:refer xt.db.node.instance-query/mark-view-dependents-stale :added "4.1"}
(fact "marks dependent views stale and records the traversal"

  (!.py
   (var state
        (schema-state/base-state {"schema" fixtures/Schema
                                  "views" fixtures/Views
                                  "lookup" fixtures/Lookup}))
   (instance-state/put-model state "orders" fixtures/DependentModelSpec)
   (var visited
        (instance-query/mark-view-dependents-stale
         state
         "orders"
         "main"
         {"tag" "changed"}
         nil))
   [visited
    (xtd/get-in state ["models" "orders" "views" "open" "status"])
    (xtd/get-in state ["models" "orders" "views" "open" "error" "tag"])])
  => [{"orders" {"main" true
                 "open" true}}
      "stale"
      "changed"])

^{:refer xt.db.node.instance-query/refresh-view-dependents-local :added "4.1"}
(fact "refreshes dependent local views through the dependency graph"

  (!.py
   (var state
        (schema-state/base-state {"schema" fixtures/Schema
                                  "views" fixtures/Views
                                  "lookup" fixtures/Lookup}))
   (instance-state/put-model state "orders" fixtures/DependentModelSpec)
   (xdb/sync-event (instance-state/ensure-db state) ["add" fixtures/Seed])
   (var visited
        (instance-query/refresh-view-dependents-local
         state
         "orders"
         "main"
         nil))
   [visited
    (xtd/get-in state ["models" "orders" "views" "open" "status"])
    (xt/x:is-string?
     (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))])
  => [{"orders" {"main" true
                 "open" true}}
      "ready"
      true])

^{:refer xt.db.node.instance-query/refresh-view-local :added "4.1"}
(fact "refreshes one local view and then its dependents"

  (!.py
   (var state
        (schema-state/base-state {"schema" fixtures/Schema
                                  "views" fixtures/Views
                                  "lookup" fixtures/Lookup}))
   (instance-state/put-model state "orders" fixtures/DependentModelSpec)
   (xdb/sync-event (instance-state/ensure-db state) ["add" fixtures/Seed])
   (var visited
        (instance-query/refresh-view-local
         state
         "orders"
         "main"
         nil))
   [visited
    (xtd/get-in state ["models" "orders" "views" "main" "value" 0 "status"])
    (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [{"orders" {"main" true
                 "open" true}}
      "open"
      "ready"])

^{:refer xt.db.node.instance-query/mark-view-bindings-stale :added "4.1"}
(fact "marks bound views and dependent views stale"

  (!.py
   (var state
        (schema-state/base-state {"schema" fixtures/Schema
                                  "views" fixtures/Views
                                  "lookup" fixtures/Lookup}))
   (instance-state/put-model state "orders" fixtures/DependentModelSpec)
   (var visited
        (instance-query/mark-view-bindings-stale
         state
         {"orders" {"main" true}}
         {"tag" "cache/changed"}))
   [visited
    (xtd/get-in state ["models" "orders" "views" "main" "status"])
    (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [{"orders" {"main" true
                 "open" true}}
      "stale"
      "stale"])

^{:refer xt.db.node.instance-query/refresh-view-bindings-local :added "4.1"}
(fact "refreshes bound views and all dependent local views"

  (!.py
   (var state
        (schema-state/base-state {"schema" fixtures/Schema
                                  "views" fixtures/Views
                                  "lookup" fixtures/Lookup}))
   (instance-state/put-model state "orders" fixtures/DependentModelSpec)
   (xdb/sync-event (instance-state/ensure-db state) ["add" fixtures/Seed])
   (var visited
        (instance-query/refresh-view-bindings-local
         state
         {"orders" {"main" true}}))
   [visited
    (xtd/get-in state ["models" "orders" "views" "main" "status"])
    (xt/x:is-string?
     (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))])
  => [{"orders" {"main" true
                 "open" true}}
      "ready"
      true])
