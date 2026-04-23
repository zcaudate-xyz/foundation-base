(ns xt.cell.binding-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.binding :as binding]
              [xt.lang.common-lib :as k]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-data :as xtd]]})

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

^{:refer xt.cell.binding/resolve-section :added "4.1"}
(fact "resolves named dbs inside descriptor sections"

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
        "tag" "xt.cell.binding/db-not-found"
        "data" {"section" "query"
                "db" "missing"}}]])

^{:refer xt.cell.binding/prepare-view :added "4.1"}
(fact "prepares a view descriptor with resolved service references"

  (!.js
   (binding/prepare-view
    (@! +service+)
    "orders"
    "list"
    (xtd/get-in (@! +bindings+) ["orders" "list"])))
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

^{:refer xt.cell.binding/compile-model :added "4.1"}
(fact "compiles all views in a model with a provided compiler"

  (!.js
   (binding/compile-model
    (@! +service+)
    "orders"
    (xt/x:get-key (@! +bindings+) "orders")
     (fn [prepared]
       (return {"path" [(xt/x:get-key prepared "model_id")
                        (xt/x:get-key prepared "view_id")]
                "db_kind" (xtd/get-in prepared ["query" "db" "kind"])}))))
  => [true
      {"list" {"path" ["orders" "list"]
               "db_kind" "cache"}}])

^{:refer xt.cell.binding/compile-bindings :added "4.1"}
(fact "compiles all models with a provided compiler"

  (!.js
   (binding/compile-bindings
    (@! +service+)
    (@! +bindings+)
    (fn [prepared]
      (return {"path" [(xt/x:get-key prepared "model_id")
                       (xt/x:get-key prepared "view_id")]}))))
  => [true
      {"orders"
       {"list" {"path" ["orders" "list"]}}}])
