(ns xtbench.lua.db.node.instance-sync-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.node.instance-sync :as instance-sync]
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

(def +seed+
  {"Task"
   [{"id" "00000000-0000-0000-0000-0000000000a1"
     "status" "open"
     "name" "alpha-task"}]})

^{:refer xt.db.node.instance-sync/normalize-sync :added "4.1"}
(fact "normalizes db/sync and db/remove keys"

  (!.lua
     (instance-sync/normalize-sync
      {"db/sync" {"Task" []}}
      {"db/remove" {"Task" ["00000000-0000-0000-0000-0000000000a1"]}}))
  => (l/as-lua {"db/remove" {"Task" ["00000000-0000-0000-0000-0000000000a1"]}, "db/sync" {"Task" []}}))

^{:refer xt.db.node.instance-sync/prepare-sync :added "4.1"}
(fact "validates sync request shapes"

  (!.lua
    [(instance-sync/prepare-sync {"db/sync" {"Task" []}} {})
     (instance-sync/prepare-sync {"db/sync" "bad"} {})])
  => [[true {"db/sync" {"Task" []}}]
      [false {"status" "error"
              "tag" "db/sync-invalid"
              "data" {"input" "bad"}}]])

^{:refer xt.db.node.instance-sync/payload-tables :added "4.1"}
(fact "collects affected tables from payload shapes"

  (!.lua
    [(instance-sync/payload-tables ["Task" "Audit" "Task"])
     (instance-sync/payload-tables {"db/sync" {"Task" []}
                                    "db/remove" {"Audit" []}})])
  => [{"Task" true
       "Audit" true}
      {"Task" true
       "Audit" true}])

^{:refer xt.db.node.instance-sync/apply-sync-request :added "4.1"}
(fact "applies sync requests to the local cache db"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "lookup" fixtures/Lookup}))
    (var result
         (instance-sync/apply-sync-request
          state
          {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1"
                               "status" "open"
                               "name" "alpha-task"}]}}))
    (var rows
         (xdb/db-pull-sync
          (instance-state/ensure-db state)
          fixtures/Schema
          ["Task" {"id" "00000000-0000-0000-0000-0000000000a1"} ["status"]]))
    [(. (. result ["tables"]) ["Task"])
     (xtd/get-in rows [0 "status"])])
  => [true
      "open"])

^{:refer xt.db.node.instance-sync/run-sync-local :added "4.1"}
(fact "prepares and applies local sync requests"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "lookup" fixtures/Lookup}))
    (var [ok result]
           (instance-sync/run-sync-local
            state
            {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1"
                                 "status" "open"
                                 "name" "alpha-task"}]}}
            {}))
    [ok
     (. (. result ["tables"]) ["Task"])])
  => [true
      true])

^{:refer xt.db.node.instance-sync/clear-state-cache :added "4.1"}
(fact "clears cache rows and marks bound views stale"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/ModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1"})
    (xt/x:set-key (. state ["watch"]) "Task" {"q1" true})
    (instance-state/set-view-success
     state "orders" "main" "q1" [{"status" "open"}] {"Task" true})
    (instance-sync/clear-state-cache state)
    [(xt/x:obj-keys (. state ["queries"]))
     (xt/x:obj-keys (. state ["watch"]))
     (xt/x:obj-keys (. state ["view_watch"]))
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => (l/as-lua [[] [] [] "stale"]))

^{:refer xt.db.node.instance-sync/process-cache-payload :added "4.1"}
(fact "refreshes dependent views when auto-refreshing affected queries"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
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
    (instance-sync/process-cache-payload
     state
     {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1" "status" "closed"}]}}
     false)
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-sync/process-cache-payload :added "4.1"}
(fact "refreshes dependent views when auto-refreshing affected queries"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
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
    (instance-sync/process-cache-payload
     state
     {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1" "status" "closed"}]}}
     false)
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-sync/handle-cache-changed :added "4.1"}
(fact "ignores local-origin payload application and marks affected queries stale"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" fixtures/ModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
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
    (var summary
         (instance-sync/handle-cache-changed
          {"state" state}
          {"data" {"tables" {"Task" true}}
           "meta" {"origin_node" "node-1"}}
          {"id" "node-1"}))
    [(. (. summary ["tables"]) ["Task"])
     (. summary ["queries"])
     (xtd/get-in state ["queries" (. result ["query_key"]) "status"])])
  => [true
       ["q1"]
       "stale"])

^{:refer xt.db.node.instance-sync/handle-cache-invalidated :added "4.1"}
(fact "marks dependent views stale when auto-refresh is disabled"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
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
    (instance-sync/handle-cache-invalidated
     {"state" state}
     {"data" {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1"}]}}}
     {"id" "node-1"})
    [(xtd/get-in state ["queries" (. result ["query_key"]) "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])

^{:refer xt.db.node.instance-sync/handle-cache-invalidated :added "4.1"}
(fact "marks dependent views stale when auto-refresh is disabled"

  (!.lua
    (var state
         (schema-state/base-state {"schema" fixtures/Schema
                                   "views" fixtures/Views
                                   "lookup" fixtures/Lookup
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" fixtures/DependentModelSpec)
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
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
    (instance-sync/handle-cache-invalidated
     {"state" state}
     {"data" {"db/sync" {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1"}]}}}
     {"id" "node-1"})
    [(xtd/get-in state ["queries" (. result ["query_key"]) "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])
