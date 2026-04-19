(ns xt.cell.service.db-query-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.service.db-query :as db-query]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.service.db-query/query-capable? :added "4.1"}
(fact "checks whether a db descriptor can prepare queries"
  ^:hidden

  (!.js
   [(db-query/query-capable? {"schema" {} "views" {}})
    (db-query/query-capable? {"schema" {}})])
  => [true false])

^{:refer xt.cell.service.db-query/view-local-transform :added "4.1"}
(fact "removes __deleted__ markers from local view entries"
  ^:hidden

  (!.js
   (db-query/view-local-transform
    {"view" {"query" ["id" "__deleted__" {"profile" {"__deleted__" true
                                                     "bio" true}}]}}))
  => {"view" {"query" ["id" {"profile" {"bio" true}}]}})

^{:refer xt.cell.service.db-query/query-check :added "4.1"}
(fact "validates args against entry input definitions"
  ^:hidden

  (!.js
   [(db-query/query-check {"input" []} [] false)
    (db-query/query-check {"input" ["ignored"]} [] true)])
  => [[true]
      [true]])

^{:refer xt.cell.service.db-query/normalize-query :added "4.1"}
(fact "fills query defaults from the view context"
  ^:hidden

  (!.js
   (db-query/normalize-query
    {}
    {"table" "Order"
     "select_method" "all"
     "return_method" "detail"
     "return_omit" ["secret"]}
    {"args" ["active"]}))
  => {"table" "Order"
      "select_method" "all"
      "select_args" ["active"]
      "return_method" "detail"
      "return_args" []
      "return_omit" ["secret"]
      })

^{:refer xt.cell.service.db-query/prepare-query :added "4.1"}
(fact "fails cleanly when a referenced select method is missing"
  ^:hidden

  (!.js
   (db-query/prepare-query
    {"schema" {}
     "views" {"Order" {"select" {} "return" {}}}}
    {"table" "Order"
     "select_method" "missing"}
    {}))
  => [false
      {"status" "error"
       "tag" "net/select-method-not-found"
       "data" {"input" "missing"}}])

^{:refer xt.cell.service.db-query/execute-query :added "4.1"}
(fact "requires a local db when executing a prepared query"
  ^:hidden

  (!.js
   (db-query/execute-query
    {"schema" {}}
    ["Order"]
    {}))
  => [false
      {"status" "error"
       "tag" "db/local-db-not-provided"}])

^{:refer xt.cell.service.db-query/run-query :added "4.1"}
(fact "propagates prepare failures without executing the query"
  ^:hidden

  (!.js
   (db-query/run-query
    {"schema" {}
     "views" {"Order" {"select" {} "return" {}}}}
    {"table" "Order"
     "select_method" "missing"}
    {}))
  => [false
      {"status" "error"
       "tag" "net/select-method-not-found"
       "data" {"input" "missing"}}])
