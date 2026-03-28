(ns xt.cell.binding.model-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.binding :as binding]
             [xt.cell.binding.model :as binding-model]
             [xt.db :as xdb]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +schema+
  {"Order"
   {"id" {"ident" "id", "type" "text", "order" 0}
    "status" {"ident" "status", "type" "text", "order" 1}}})

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
    {"summary"
     {"input" [{"symbol" "i_order_id", "type" "text"}]
      "return" "jsonb"
      "view" {"table" "Order"
              "type" "return"
              "tag" "summary"
              "access" {"roles" {}}
              "guards" []
              "query" ["id" "status"]}}}}})

(def +lookup+
  {"Order" {"position" 0}})

^{:refer xt.cell.binding.model/unwrap-result :added "4.1"}
(fact "unwraps service-layer [ok result] tuples"
  ^:hidden

  (!.js
   [(binding-model/unwrap-result [true {"value" 1}])
    (binding-model/unwrap-result [true 2])])
  => [{"value" 1}
      2])

^{:refer xt.cell.binding.model/make-view-context :added "4.1"}
(fact "creates the runtime context for compiled handlers"
  ^:hidden

  (!.js
   (binding-model/make-view-context
    {"model_id" "orders"
     "view_id" "list"}
    {"kind" "cache"}
    {"id" "link-1"}
    ["open"]))
  => {"args" ["open"]
      "db" {"kind" "cache"}
      "link" {"id" "link-1"}
      "model-id" "orders"
      "view-id" "list"})

^{:refer xt.cell.binding.model/compile-main-handler :added "4.1"}
(fact "compiles a local query handler"
  ^:hidden

  (!.js
   (var schema (@! +schema+))
   (var views (@! +views+))
   (var local-db (k/obj-assign
                  (xdb/db-create {"::" "db.cache"} schema (@! +lookup+) nil)
                  {"schema" schema
                   "views" views}))
   (xdb/sync-event local-db
                   ["add"
                    {"Order" [{"id" "ord-1"
                               "status" "open"}
                              {"id" "ord-2"
                               "status" "closed"}]}])
   (var service {"dbs" {"local-cache" local-db}})
   (var [ok prepared] (binding/prepare-view
                       service
                       "orders"
                        "list"
                        {"query" {"db" "local-cache"
                                  "table" "Order"
                                  "select_method" "by_status"
                                  "return_method" "summary"}}))
   (var handler (binding-model/compile-main-handler prepared))
   [ok
    (handler {"id" "link-1"} "open")])
  => [true
      [{"id" "ord-1"
        "status" "open"}]])

^{:refer xt.cell.binding.model/compile-remote-handler :added "4.1"}
(fact "compiles a Supabase-backed remote handler"
  ^:hidden

  (!.js
   (var schema (@! +schema+))
   (var views (@! +views+))
   (var supabase-db {"schema" schema
                     "views" views
                     "execute" (fn [compiled _]
                                 (return [true compiled]))})
   (var service {"dbs" {"supabase-main" supabase-db}})
   (var [ok prepared] (binding/prepare-view
                       service
                       "orders"
                        "list"
                        {"query" {"db" "supabase-main"
                                  "table" "Order"
                                  "select_method" "by_status"
                                  "return_method" "summary"
                                  "target" "supabase"}}))
   (var handler (binding-model/compile-remote-handler prepared))
   [ok
    (handler {"id" "link-1"} "open")])
  => [true
      {"table" "Order"
       "select" "id,status"
       "filters" [{"path" "status"
                   "op" "eq"
                   "value" "open"}]}])

^{:refer xt.cell.binding.model/compile-sync-pipeline :added "4.1"}
(fact "compiles the sync pipeline handler"
  ^:hidden

  (!.js
   (var schema (@! +schema+))
   (var views (@! +views+))
   (var local-db (k/obj-assign
                  (xdb/db-create {"::" "db.cache"} schema (@! +lookup+) nil)
                  {"schema" schema
                   "views" views}))
   (var service {"dbs" {"local-cache" local-db}})
   (var [ok prepared] (binding/prepare-view
                       service
                       "orders"
                       "save"
                       {"sync" {"db" "local-cache"
                                "sync" {"Order" [{"id" "ord-1"
                                                  "status" "open"}]}}}))
   (var pipeline (binding-model/compile-sync-pipeline prepared))
   [ok
    ((k/get-in pipeline ["sync" "handler"]) {"id" "link-1"})])
  => [true
      {"result" {"db/sync" {"Order" [{"id" "ord-1"
                                      "status" "open"}]}}
       "update" {"type" "sync"
                 "body" {"db/sync" {"Order" ["ord-1"]}}}}])

^{:refer xt.cell.binding.model/compile-view-spec :added "4.1"}
(fact "compiles a prepared descriptor into a kernel view spec"
  ^:hidden

  (!.js
   (var schema (@! +schema+))
   (var views (@! +views+))
   (var local-db (k/obj-assign
                  (xdb/db-create {"::" "db.cache"} schema (@! +lookup+) nil)
                  {"schema" schema
                   "views" views}))
   (var service {"dbs" {"local-cache" local-db}})
   (var [ok prepared] (binding/prepare-view
                       service
                       "orders"
                       "list"
                       {"query" {"db" "local-cache"
                                 "table" "Order"
                                 "select_method" "by_status"
                                 "return_method" "summary"}
                        "deps" [["accounts" "current"]]
                        "resolve" {"policy" "replace"}}))
   (var spec (binding-model/compile-view-spec prepared))
   [ok
    (k/is-function? (k/get-key spec "handler"))
    (k/get-key spec "deps")
    (k/get-in spec ["options" "context" "modelId"])
    (k/get-in spec ["options" "context" "resolve"])])
  => [true
      true
      [["accounts" "current"]]
      "orders"
      {"policy" "replace"}])
