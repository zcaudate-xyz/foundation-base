(ns xtbench.dart.db.node.instance-sync-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
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

(def +model-spec+
  {"views"
   {"main"
    {"query" {:table "Order"
              :return-method "default"
              :return-id "ord-1"}
     "input" []}}})

(def +seed+
  {"Order"
   [{"id" "ord-1" "status" "open"}]})

^{:refer xt.db.node.instance-sync/normalize-sync :added "4.1"}
(fact "normalizes sync aliases into db/sync and db/remove keys"

  (!.dt
    (instance-sync/normalize-sync
     {"sync" {"Order" []}}
     {"remove" {"Order" ["ord-1"]}}))
  => {"db/sync" {"Order" []}
      "db/remove" {"Order" ["ord-1"]}})

^{:refer xt.db.node.instance-sync/prepare-sync :added "4.1"}
(fact "validates sync request shapes"

  (!.dt
    [(instance-sync/prepare-sync {"sync" {"Order" []}} {})
     (instance-sync/prepare-sync {"sync" "bad"} {})])
  => [[true {"db/sync" {"Order" []}}]
      [false {"status" "error"
              "tag" "db/sync-invalid"
              "data" {"input" "bad"}}]])

^{:refer xt.db.node.instance-sync/payload-tables :added "4.1"}
(fact "collects affected tables from payload shapes"

  (!.dt
    [(instance-sync/payload-tables ["Order" "Audit" "Order"])
     (instance-sync/payload-tables {"db/sync" {"Order" []}
                                    "db/remove" {"Audit" []}})])
  => [{"Order" true
       "Audit" true}
      {"Order" true
       "Audit" true}])

^{:refer xt.db.node.instance-sync/apply-sync-request :added "4.1"}
(fact "applies sync requests to the local cache db"

  (!.dt
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

  (!.dt
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "lookup" (@! +lookup+)}))
    (var [ok result]
         (instance-sync/run-sync-local
          state
          {"sync" {"Order" [{"id" "ord-1" "status" "open"}]}}
          {}))
    [ok
     (. (. result ["tables"]) ["Order"])])
  => [true
      true])

^{:refer xt.db.node.instance-sync/clear-state-cache :added "4.1"}
(fact "clears cache rows and marks bound views stale"

  (!.dt
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
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => [[] [] "stale"])

^{:refer xt.db.node.instance-sync/process-cache-payload :added "4.1"}
(fact "applies payloads and refreshes affected cached queries"

  (!.dt
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
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
         (instance-sync/process-cache-payload
          state
          {"db/sync" {"Order" [{"id" "ord-1" "status" "closed"}]}}
          false))
    [(. (. summary ["tables"]) ["Order"])
     (. summary ["queries"])
     (xtd/get-in state ["queries" (. result ["query_key"]) "value" 0 "status"])])
  => [true
      ["q1"]
      "closed"])

^{:refer xt.db.node.instance-sync/handle-cache-changed :added "4.1"}
(fact "ignores local-origin payload application and marks affected queries stale"

  (!.dt
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
      "ready"])

^{:refer xt.db.node.instance-sync/handle-cache-invalidated :added "4.1"}
(fact "handles invalidation payloads using tables only"

  (!.dt
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
         (instance-sync/handle-cache-invalidated
          {"state" state}
          {"data" {"db/sync" {"Order" [{"id" "ord-1"}]}}}
          {"id" "node-1"}))
    [(. (. summary ["tables"]) ["Order"])
     (. summary ["queries"])
     (xtd/get-in state ["queries" (. result ["query_key"]) "status"])])
  => [true
      ["q1"]
      "ready"])
