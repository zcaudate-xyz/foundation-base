(ns xtbench.python.db.node.instance-sync-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.db.instance :as xdb]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.node.instance-sync :as instance-sync]
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

(def +views+
  {"Order"
   {"select"
    {"by_status"
     {"input" [{"symbol" "i_status", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "select"
              "tag" "by_status"
              "access" {"roles" {}}
              "guards" []
              "query" {"status" "{{i_status}}"}}}}
    "return"
    {"default"
     {"input" [{"symbol" "i_order_id", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "return"
              "tag" "default"
              "access" {"roles" {}}
              "guards" []
              "query" ["status"]}}}}})

(def +model-spec+
  {"views"
   {"main"
    {"query" {:table "Order"
              :return-method "default"
              :return-id "ord-1"}
      "input" []}}})

(def +dependent-model-spec+
  {"views"
   {"main"
    {"query" {:table "Order"
              :return-method "default"
              :return-id "ord-1"}
     "input" []}
    "open"
    {"query" {:table "Order"
              :select-method "by_status"}
     "default_input" ["open"]
     "deps" ["main"]}}})

(def +seed+
  {"Order"
   [{"id" "ord-1" "status" "open"}]})

^{:refer xt.db.node.instance-sync/normalize-sync :added "4.1"}
(fact "normalizes db/sync and db/remove keys"

  (!.py
     (instance-sync/normalize-sync
      {"db/sync" {"Order" []}}
      {"db/remove" {"Order" ["ord-1"]}}))
  => {"db/sync" {"Order" []}
      "db/remove" {"Order" ["ord-1"]}})

^{:refer xt.db.node.instance-sync/prepare-sync :added "4.1"}
(fact "validates sync request shapes"

  (!.py
    [(instance-sync/prepare-sync {"db/sync" {"Order" []}} {})
     (instance-sync/prepare-sync {"db/sync" "bad"} {})])
  => [[true {"db/sync" {"Order" []}}]
      [false {"status" "error"
              "tag" "db/sync-invalid"
              "data" {"input" "bad"}}]])

^{:refer xt.db.node.instance-sync/payload-tables :added "4.1"}
(fact "collects affected tables from payload shapes"

  (!.py
    [(instance-sync/payload-tables ["Order" "Audit" "Order"])
     (instance-sync/payload-tables {"db/sync" {"Order" []}
                                    "db/remove" {"Audit" []}})])
  => [{"Order" true
       "Audit" true}
      {"Order" true
       "Audit" true}])

^{:refer xt.db.node.instance-sync/apply-sync-request :added "4.1"}
(fact "applies sync requests to the local cache db"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "lookup" (@! +lookup+)}))
    (var result
         (instance-sync/apply-sync-request
          state
          {"db/sync" {"Order" [{"id" "ord-1" "status" "open"}]}}))
    (var rows
         (xdb/db-pull-sync
          (instance-state/ensure-db state)
          (@! +schema+)
          ["Order" {"id" "ord-1"} ["status"]]))
    [(. (. result ["tables"]) ["Order"])
     (xtd/get-in rows [0 "status"])])
  => [true
      "open"])

^{:refer xt.db.node.instance-sync/run-sync-local :added "4.1"}
(fact "prepares and applies local sync requests"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "lookup" (@! +lookup+)}))
    (var [ok result]
           (instance-sync/run-sync-local
            state
            {"db/sync" {"Order" [{"id" "ord-1" "status" "open"}]}}
            {}))
    [ok
     (. (. result ["tables"]) ["Order"])])
  => [true
      true])

^{:refer xt.db.node.instance-sync/clear-state-cache :added "4.1"}
(fact "clears cache rows and marks bound views stale"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1"})
    (xt/x:set-key (. state ["watch"]) "Order" {"q1" true})
    (instance-state/set-view-success
     state "orders" "main" "q1" [{"status" "open"}] {"Order" true})
    (instance-sync/clear-state-cache state)
    [(xt/x:obj-keys (. state ["queries"]))
     (xt/x:obj-keys (. state ["watch"]))
     (xt/x:obj-keys (. state ["view_watch"]))
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => [[] [] [] "stale"])

^{:refer xt.db.node.instance-sync/process-cache-payload :added "4.1"}
(fact "refreshes dependent views when auto-refreshing affected queries"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +dependent-model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Order"
           :return-method "default"
           :return-id "ord-1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-sync/process-cache-payload
     state
     {"db/sync" {"Order" [{"id" "ord-1" "status" "closed"}]}}
     false)
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-sync/process-cache-payload :added "4.1"}
(fact "refreshes dependent views when auto-refreshing affected queries"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +dependent-model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Order"
           :return-method "default"
           :return-id "ord-1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-sync/process-cache-payload
     state
     {"db/sync" {"Order" [{"id" "ord-1" "status" "closed"}]}}
     false)
    [(xt/x:is-string? (xtd/get-in state ["models" "orders" "views" "open" "query_key"]))
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => [true
      "ready"])

^{:refer xt.db.node.instance-sync/handle-cache-changed :added "4.1"}
(fact "ignores local-origin payload application and marks affected queries stale"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Order"
           :return-method "default"
           :return-id "ord-1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (var summary
         (instance-sync/handle-cache-changed
          {"state" state}
          {"data" {"tables" {"Order" true}}
           "meta" {"origin_node" "node-1"}}
          {"id" "node-1"}))
    [(. (. summary ["tables"]) ["Order"])
     (. summary ["queries"])
     (xtd/get-in state ["queries" (. result ["query_key"]) "status"])])
  => [true
       ["q1"]
       "stale"])

^{:refer xt.db.node.instance-sync/handle-cache-invalidated :added "4.1"}
(fact "marks dependent views stale when auto-refresh is disabled"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" (@! +dependent-model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Order"
           :return-method "default"
           :return-id "ord-1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-sync/handle-cache-invalidated
     {"state" state}
     {"data" {"db/sync" {"Order" [{"id" "ord-1"}]}}}
     {"id" "node-1"})
    [(xtd/get-in state ["queries" (. result ["query_key"]) "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])

^{:refer xt.db.node.instance-sync/handle-cache-invalidated :added "4.1"}
(fact "marks dependent views stale when auto-refresh is disabled"

  (!.py
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)
                                   "auto_refresh" false}))
    (instance-state/put-model state "orders" (@! +dependent-model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [_ result]
         (instance-query/run-local-query
          state
          {:key "q1"
           :table "Order"
           :return-method "default"
           :return-id "ord-1"}
          {:model-id "orders"
           :view-id "main"
           :args []}
          "orders"
          "main"))
    (instance-sync/handle-cache-invalidated
     {"state" state}
     {"data" {"db/sync" {"Order" [{"id" "ord-1"}]}}}
     {"id" "node-1"})
    [(xtd/get-in state ["queries" (. result ["query_key"]) "status"])
     (xtd/get-in state ["models" "orders" "views" "open" "status"])])
  => ["stale"
      "stale"])
