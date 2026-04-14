(ns js.cell.binding-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell.binding :as binding]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +service+
  {"dbs"
   {"local-cache" {"kind" "cache"}
    "server-rpc" {"kind" "remote"}}})

(def +bindings+
  {"orders"
   {"list" {"query" {"db" "local-cache"
                     "table" "Order"}
            "remote" {"db" "server-rpc"
                      "target" "server-rpc"}
            "deps" [["accounts" "current"]]
            "resolve" {"policy" "replace"}}}})

^{:refer js.cell.binding/resolve-section :added "4.1"}
(fact "resolves named dbs inside descriptor sections"
  ^:hidden

  (!.js
   [(binding/resolve-section
     (@! +service+)
     {"query" {"db" "local-cache"
               "table" "Order"}}
     "query"
     {"model-id" "orders"
      "view-id" "list"})
    (binding/resolve-section
     (@! +service+)
     {"query" {"db" "missing"}}
     "query"
     {})])
  => [[true
       {"db" {"kind" "cache"}
        "table" "Order"}]
      [false
       {"status" "error"
        "tag" "js.cell.binding/db-not-found"
        "data" {"section" "query"
                "db" "missing"}}]])

^{:refer js.cell.binding/prepare-view :added "4.1"}
(fact "prepares a view descriptor with resolved service references"
  ^:hidden

  (!.js
   (binding/prepare-view
    (@! +service+)
    "orders"
    "list"
    (k/get-in (@! +bindings+) ["orders" "list"])))
  => [true
      {"model_id" "orders"
       "view_id" "list"
       "query" {"db" {"kind" "cache"}
                "table" "Order"}
       "remote" {"db" {"kind" "remote"}
                 "target" "server-rpc"}
       "sync" nil
       "stream" nil
       "resolve" {"policy" "replace"}
       "deps" [["accounts" "current"]]
       "options" {}}])

^{:refer js.cell.binding/compile-model :added "4.1"}
(fact "compiles all views in a model with a provided compiler"
  ^:hidden

  (!.js
   (binding/compile-model
    (@! +service+)
    "orders"
    (k/get-key (@! +bindings+) "orders")
    (fn [prepared]
      (return {"path" [(k/get-key prepared "model_id")
                       (k/get-key prepared "view_id")]
               "db_kind" (k/get-in prepared ["query" "db" "kind"])}))))
  => [true
      {"list" {"path" ["orders" "list"]
               "db_kind" "cache"}}])

^{:refer js.cell.binding/compile-bindings :added "4.1"}
(fact "compiles all models with a provided compiler"
  ^:hidden

  (!.js
   (binding/compile-bindings
    (@! +service+)
    (@! +bindings+)
    (fn [prepared]
      (return {"path" [(k/get-key prepared "model_id")
                       (k/get-key prepared "view_id")]}))))
  => [true
      {"orders"
       {"list" {"path" ["orders" "list"]}}}])
