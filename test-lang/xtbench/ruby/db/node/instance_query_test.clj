(ns xtbench.ruby.db.node.instance-query-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.db.runtime :as xdb]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.node.instance-state :as instance-state]
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

(def +seed+
  {"Order"
   [{"id" "ord-1" "status" "open"}
    {"id" "ord-2" "status" "closed"}]})

^{:refer xt.db.node.instance-query/attach-query-entry :added "4.1"}
(fact "stores cached query results and updates bound views"

  (!.rb
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var entry
         (instance-query/attach-query-entry
          state
          {"key" "q1"
           "query" {"table" "Order"}
           "context" {"model_id" "orders" "view_id" "main"}
           "plan" ["Order" {"id" "ord-1"} ["status"]]
           "tables" {"Order" true}}
          [{"status" "open"}]
          nil
          "orders"
          "main"))
    [(. entry ["key"])
     (. (. state ["watch"]) ["Order"] ["q1"])
     (xtd/get-in state ["models" "orders" "views" "main" "query_key"])
     (xtd/get-in state ["models" "orders" "views" "main" "value" 0 "status"])])
  => ["q1"
      true
      "q1"
      "open"])

^{:refer xt.db.node.instance-query/mark-query-stale :added "4.1"}
(fact "marks one cached query and bound view as stale"

  (!.rb
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (instance-query/attach-query-entry
     state
     {"key" "q1"
      "query" {"table" "Order"}
      "context" {}
      "plan" ["Order" {"id" "ord-1"} ["status"]]
      "tables" {"Order" true}}
     [{"status" "open"}]
     nil
     "orders"
     "main")
    (var entry (instance-query/mark-query-stale state "q1" "changed"))
    [(. entry ["status"])
     (. entry ["error"])
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => ["stale"
      "changed"
      "stale"])

^{:refer xt.db.node.instance-query/mark-query-stale-many :added "4.1"}
(fact "marks many cached queries as stale"

  (!.rb
    (var state (schema-state/base-state {}))
    (xt/x:set-key (. state ["queries"]) "q1" {"key" "q1" "bindings" {}})
    (xt/x:set-key (. state ["queries"]) "q2" {"key" "q2" "bindings" {}})
    (xtd/arr-map
     (instance-query/mark-query-stale-many state ["q1" "q2"] "changed")
     (fn [e]
       (return (. e ["status"])))))
  => ["stale" "stale"])

^{:refer xt.db.node.instance-query/refresh-query-entry :added "4.1"}
(fact "refreshes one cached query from the local cache db"

  (!.rb
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var db (instance-state/ensure-db state))
    (xdb/sync-event db ["add" (@! +seed+)])
    (var [ok result]
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
    (xdb/sync-event db ["add" {"Order" [{"id" "ord-1" "status" "closed"}]}])
    (var entry (instance-query/refresh-query-entry state (. result ["query_key"])))
    [ok
     (xtd/get-in entry ["value" 0 "status"])
     (xtd/get-in state ["models" "orders" "views" "main" "value" 0 "status"])])
  => [true
      "closed"
      "closed"])

^{:refer xt.db.node.instance-query/refresh-query-keys :added "4.1"}
(fact "refreshes many cached query keys"

  (!.rb
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (var db (instance-state/ensure-db state))
    (xdb/sync-event db ["add" (@! +seed+)])
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
    (xdb/sync-event db ["add" {"Order" [{"id" "ord-1" "status" "closed"}]}])
    (var refreshed
         (instance-query/refresh-query-keys state [(. result ["query_key"])]))
    [(xt/x:len refreshed)
     (xtd/get-in refreshed [0 "value" 0 "status"])])
  => [1
      "closed"])

^{:refer xt.db.node.instance-query/run-local-query :added "4.1"}
(fact "prepares, executes, and caches a local query"

  (!.rb
    (var state
         (schema-state/base-state {"schema" (@! +schema+)
                                   "views" (@! +views+)
                                   "lookup" (@! +lookup+)}))
    (instance-state/put-model state "orders" (@! +model-spec+))
    (xdb/sync-event (instance-state/ensure-db state) ["add" (@! +seed+)])
    (var [ok result]
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
    [ok
     (xtd/get-in result ["value" 0 "status"])
     (. (. result ["tables"]) ["Order"])
     (xtd/get-in state ["models" "orders" "views" "main" "status"])])
  => [true
      "open"
      true
      "ready"])
