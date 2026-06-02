(ns xt.db.text.pgrest-graph-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.text.pgrest-graph :as g]
             [xt.db.text.sql-util :as ut]
             [xt.db.helpers.data-main-test :as sample]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.text.pgrest-graph/select-return.count :added "4.1"}
(fact "supports count and control custom nodes from tree ir"

  (!.js
    (g/select-return
     sample/Schema
     ["Currency"
      {"custom" [{"::" "sql/count"}
                 (ut/ORDER-BY ["name"])
                 (ut/ORDER-SORT "desc")
                 (ut/LIMIT 20)]
       "where" [{"type" "fiat"}]
       "links" []
       "data" []}]
     0
     {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "count",
      "filters" [{"type" "fiat"}],
      "params" ["select=count"
                "type=eq.fiat"
                "order=name.desc"
                "limit=20"],
      "query" "select=count&type=eq.fiat&order=name.desc&limit=20",
      "url" "/rest/v1/Currency?select=count&type=eq.fiat&order=name.desc&limit=20",
      "headers" {}})

^{:refer xt.db.text.pgrest-graph/tree-count? :added "4.1"}
(fact "detects count custom nodes"

  (!.js
    [(g/tree-count? [{"::" "sql/count"}])
     (g/tree-count? [(ut/LIMIT 20)])
     (g/tree-count? [])])
  => [true false false])

^{:refer xt.db.text.pgrest-graph/value->query-text :added "4.1"}
(fact "formats scalars for query strings"

  (!.js
    [(g/value->query-text nil)
     (g/value->query-text "open")
     (g/value->query-text 10)
     (g/value->query-text true)
     (g/value->query-text false)])
  => ["null" "open" "10" "true" "false"])

^{:refer xt.db.text.pgrest-graph/normalise-in-values :added "4.1"}
(fact "normalises nested arrays and scalars for in filters"

  (!.js
    [(g/normalise-in-values [["ord-1" "ord-2"]])
     (g/normalise-in-values ["ord-1" "ord-2"])
     (g/normalise-in-values "ord-1")])
  => [["ord-1" "ord-2"]
      ["ord-1" "ord-2"]
      ["ord-1"]])

^{:refer xt.db.text.pgrest-graph/filter-operator? :added "4.1"}
(fact "recognizes the shared filter operators"

  (!.js
    [(g/filter-operator? "eq")
     (g/filter-operator? "ilike")
     (g/filter-operator? "in")
     (g/filter-operator? "match")])
  => [true true true false])

^{:refer xt.db.text.pgrest-graph/compile-filter-value :added "4.1"}
(fact "compiles scalar and in filter values"

  (!.js
    [(g/compile-filter-value "eq" "open")
     (g/compile-filter-value "is" nil)
     (g/compile-filter-value "in" [["ord-1" "ord-2"]])])
  => ["eq.open"
      "is.null"
      "in.(ord-1,ord-2)"])

^{:refer xt.db.text.pgrest-graph/compile-filter-fragment :added "4.1"}
(fact "compiles a filter descriptor to a fragment"

  (!.js
    (g/compile-filter-fragment
     {"path" "account.id"
      "op" "eq"
      "value" "acct-1"}))
  => "account.id.eq.acct-1")

^{:refer xt.db.text.pgrest-graph/compile-clause-into :added "4.1"}
(fact "compiles nested where clauses into filter descriptors"

  (!.js
    (g/compile-clause-into
     ""
     {"account" {"id" "acct-1"}
      "id" ["in" [["ord-1" "ord-2"]]]
      "status" ["ilike" "%open%"]}
     []))
  => [{"path" "account.id"
       "op" "eq"
       "value" "acct-1"}
      {"path" "id"
       "op" "in"
       "value" [["ord-1" "ord-2"]]}
      {"path" "status"
       "op" "ilike"
       "value" "%open%"}])

^{:refer xt.db.text.pgrest-graph/compile-or-clause :added "4.1"}
(fact "joins multi-fragment branches with and(...)"

  (!.js
    [(g/compile-or-clause {"id" "ord-1"})
     (g/compile-or-clause {"id" "ord-1"
                           "status" ["eq" "open"]})])
  => ["id.eq.ord-1"
      "and(id.eq.ord-1,status.eq.open)"])

^{:refer xt.db.text.pgrest-graph/compile-where-params :added "4.1"}
(fact "compiles single and multiple where branches into params"

  (!.js
    [(g/compile-where-params {"id" "ord-1"
                              "status" ["eq" "open"]})
     (g/compile-where-params [{"id" "ord-1"
                               "status" ["eq" "open"]}
                              {"id" "ord-2"}])])
  => [["id=eq.ord-1" "status=eq.open"]
      ["or=(and(id.eq.ord-1,status.eq.open),id.eq.ord-2)"]])

^{:refer xt.db.text.pgrest-graph/compile-tree-select-item :added "4.1"}
(fact "compiles string and link select items"

  (!.js
    [(g/compile-tree-select-item "status")
     (g/compile-tree-select-item
      ["account"
       "forward"
       ["Account"
        {"custom" []
         "where" []
         "links" []
         "data" ["nickname"]}]])])
  => ["status"
      "account(nickname)"])

^{:refer xt.db.text.pgrest-graph/compile-tree-select-params :added "4.1"}
(fact "compiles tree params into select syntax"

  (!.js
    [(g/compile-tree-select-params
      {"custom" []
       "data" ["status"]
       "links" [["account"
                 "forward"
                 ["Account"
                  {"custom" []
                   "where" []
                   "links" []
                   "data" ["nickname"]}]]]})
     (g/compile-tree-select-params
      {"custom" [{"::" "sql/count"}]
       "data" []
       "links" []})
     (g/compile-tree-select-params
      {"custom" []
       "data" []
       "links" []})])
  => ["status,account(nickname)"
      "count"
      "*"])

^{:refer xt.db.text.pgrest-graph/compile-control-params :added "4.1"}
(fact "compiles order limit and offset controls"

  (!.js
    (g/compile-control-params
     [(ut/ORDER-BY ["name" "id"])
      (ut/ORDER-SORT "desc")
      (ut/LIMIT 20)
      (ut/OFFSET 10)]))
  => ["order=name.desc,id.desc"
      "limit=20"
      "offset=10"])

^{:refer xt.db.text.pgrest-graph/compile-query-string :added "4.1"}
(fact "joins params into a query string"

  (!.js
    [(g/compile-query-string ["select=*"
                              "limit=20"])
     (g/compile-query-string [])])
  => ["select=*&limit=20"
      ""])

^{:refer xt.db.text.pgrest-graph/compile-url :added "4.1"}
(fact "joins path and params into a url"

  (!.js
    [(g/compile-url "/rest/v1/Order" ["select=*"
                                      "limit=20"])
     (g/compile-url "/rest/v1/Order" [])])
  => ["/rest/v1/Order?select=*&limit=20"
      "/rest/v1/Order"])

^{:refer xt.db.text.pgrest-graph/select-return :added "4.1"}
(fact "compiles tree ir into a PostgREST request"

  (!.js
    (g/select-return
     sample/Schema
     ["Order"
      {"custom" [],
       "where" [{"account" {"id" "acct-1"}}],
       "links" [["account"
                 "forward"
                 ["Account"
                  {"custom" [],
                   "where" [],
                   "links" [],
                   "data" ["nickname"]}]]],
       "data" ["status"]}]
     0
     {}))
  => {"type" "query",
      "table" "Order",
      "method" "GET",
      "path" "/rest/v1/Order",
      "select" "status,account(nickname)",
      "filters" [{"account" {"id" "acct-1"}}],
      "params" ["select=status,account(nickname)"
                "account.id=eq.acct-1"],
      "query" "select=status,account(nickname)&account.id=eq.acct-1",
      "url" "/rest/v1/Order?select=status,account(nickname)&account.id=eq.acct-1",
      "headers" {}})

^{:refer xt.db.text.pgrest-graph/select-tree :added "4.1"}
(fact "returns the tree unchanged"

  (!.js
    (g/select-tree
     sample/Schema
     ["Currency"
      {"custom" []
       "where" [{"type" "fiat"}]
       "links" []
       "data" ["id"]}]
     {}))
  => ["Currency"
      {"custom" []
       "where" [{"type" "fiat"}]
       "links" []
       "data" ["id"]}])

^{:refer xt.db.text.pgrest-graph/select :added "4.1"}
(fact "wraps select-return at the top level"

  (!.js
    (g/select
     sample/Schema
     ["Currency"
      {"custom" [(ut/ORDER-BY ["name"])
                 (ut/LIMIT 5)]
       "where" [{"type" "fiat"}]
       "links" []
       "data" ["id" "name"]}]
     {}))
  => {"type" "query",
      "table" "Currency",
      "method" "GET",
      "path" "/rest/v1/Currency",
      "select" "id,name",
      "filters" [{"type" "fiat"}],
      "params" ["select=id,name"
                "type=eq.fiat"
                "order=name"
                "limit=5"],
      "query" "select=id,name&type=eq.fiat&order=name&limit=5",
      "url" "/rest/v1/Currency?select=id,name&type=eq.fiat&order=name&limit=5",
      "headers" {}})