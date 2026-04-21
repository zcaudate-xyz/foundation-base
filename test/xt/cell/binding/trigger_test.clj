(ns xt.cell.binding.trigger-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.binding.trigger :as trigger]
             [xt.lang.common-data :as xtd]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.binding.trigger/normalize-deps :added "4.1"}
(fact "normalizes dependency paths against the current model"

  (!.js
   (trigger/normalize-deps
    {"model_id" "orders"
     "deps" ["list" ["accounts" "current"]]}))
  => [["orders" "list"]
      ["accounts" "current"]])

^{:refer xt.cell.binding.trigger/compile-trigger :added "4.1"}
(fact "extracts trigger metadata from a prepared descriptor"

  (!.js
   (trigger/compile-trigger
    {"trigger" {"topic" "refresh"}}))
  => {"topic" "refresh"})

^{:refer xt.cell.binding.trigger/compile-stream-options :added "4.1"}
(fact "builds stream option context with a stable subscription key"

  (!.js
   (var out
        (trigger/compile-stream-options
         {"model_id" "orders"
          "view_id" "list"
          "stream" {"db" {"target" "orders-db"}
                    "topic" {"table" "Order"}}}))
   [(xtd/get-in out ["context" "stream" "spec" "target"])
    (xtd/get-in out ["context" "stream" "spec" "topic"])
    (xtd/get-in out ["context" "stream" "key"])])
  => ["orders-db"
      {"table" "Order"}
      "orders-db::{\"table\":\"Order\"}::list::orders"])

^{:refer xt.cell.binding.trigger/compile-view-hooks :added "4.1"}
(fact "compiles deps, trigger, and stream options together"

  (!.js
   (trigger/compile-view-hooks
    {"model_id" "orders"
     "view_id" "list"
     "deps" ["summary"]
     "trigger" "refresh"
     "stream" {"db" {"target" "orders-db"}
               "topic" "orders"}}))
  => (contains-in
      {"deps" [["orders" "summary"]]
       "trigger" "refresh"
       "options"
       {"context"
        {"stream"
         {"spec" {"target" "orders-db"
                  "topic" "orders"}}}}}))
