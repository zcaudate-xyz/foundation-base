(ns xtbench.dart.db.text.pgrest-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.db.text.pgrest :as pgrest]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(def +query-tree-basic+
  ["Order"
   {"account" {"id" "acct-1"}}
   ["status"
    ["account" ["nickname"]]]])

(def +query-tree-advanced+
  ["Order"
   {"account" {"id" "acct-1"}
    "id" ["in" [["ord-1" "ord-2"]]]
    "$or" [{"status" ["eq" "open"]}
           {"status" ["eq" "pending"]}]
    "$order" [["status" "desc"]
              ["id" "asc"]]
    "$limit" 10
    "$offset" 20}
   ["status"
    ["name" "first_name"]
    ["account" ["nickname"]]]])

^{:refer xt.db.text.pgrest/compile-select-item :added "4.1"}
(fact "compiles nested return entries to PostgREST select syntax"

  (!.dt
    [(pgrest/compile-select-item "status")
     (pgrest/compile-select-item ["name" "first_name"])
     (pgrest/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "name:first_name"
      "account(nickname)"])

^{:refer xt.db.text.pgrest/compile-select :added "4.1"}
(fact "compiles return vectors to PostgREST select syntax"

  (!.dt
    (pgrest/compile-select
     ["status"
      ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer xt.db.text.pgrest/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into PostgREST filters"

  (!.dt
    (pgrest/compile-filters-into
     ""
     {"account" {"id" "acct-1"}
      "status" ["ilike" "%open%"]
      "id" ["in" [["ord-1" "ord-2"]]]
      "$limit" 10}
     []))
  => [{"path" "account.id"
       "op" "eq"
       "value" "acct-1"}
      {"path" "id"
       "op" "in"
       "value" ["ord-1" "ord-2"]}
      {"path" "status"
       "op" "ilike"
       "value" "%open%"}])

^{:refer xt.db.text.pgrest/apply-filter :added "4.1"}
(fact "compiles filters into PostgREST query params"

  (!.dt
    [(pgrest/apply-filter
      {"path" "account.id"
       "op" "eq"
       "value" "acct-1"})
     (pgrest/apply-filter
      {"path" "id"
       "op" "in"
       "value" ["ord-1" "ord-2"]})
     (pgrest/apply-filter
      {"path" "ignored"
       "op" "match"
       "value" {"status" "open"}})])
  => [["account.id=eq.acct-1"]
      ["id=in.(ord-1,ord-2)"]
      ["status=eq.open"]])

^{:refer xt.db.text.pgrest/compile-query :added "4.1"}
(fact "compiles the same PostgREST request across js lua and python"

  (!.dt
    (var compiled
         (pgrest/compile-query
          (@! +query-tree-advanced+)))
    [(. compiled ["type"])
     (. compiled ["table"])
     (. compiled ["select"])
     (. compiled ["path"])
     (. compiled ["query"])
     (. compiled ["url"])
     (. (. (. compiled ["filters"]) [0]) ["path"])
     (. (. (. compiled ["filters"]) [1]) ["op"])
     (. (. compiled ["params"]) [2])])
  => ["query"
      "Order"
      "status,name:first_name,account(nickname)"
      "/rest/v1/Order"
      "select=status,name:first_name,account(nickname)&or=(status.eq.open,status.eq.pending)&account.id=eq.acct-1&id=in.(ord-1,ord-2)&order=status.desc,id.asc&limit=10&offset=20"
      "/rest/v1/Order?select=status,name:first_name,account(nickname)&or=(status.eq.open,status.eq.pending)&account.id=eq.acct-1&id=in.(ord-1,ord-2)&order=status.desc,id.asc&limit=10&offset=20"
      "or"
      "eq"
      "account.id=eq.acct-1"])

(comment
  (s/run ['xt.db.text.pgrest])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.text.pgrest] {:lang [:julia :dart] :write true})
  
  (s/seedgen-benchadd '[xt.db] {:lang [:lua :python :ruby :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.text.pgrest {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.pgrest {:lang [:lua :python] :write true}))

^{:refer xt.db.text.pgrest/filter-operator? :added "4.1"}
(fact "recognizes supported PostgREST operators"

  (!.dt
   [(pgrest/filter-operator? "eq")
    (pgrest/filter-operator? "match")
    (pgrest/filter-operator? "wat")])
  => [true true false])

^{:refer xt.db.text.pgrest/top-level-control? :added "4.1"}
(fact "only treats top-level order limit and offset keys as controls"

  (!.dt
   [(pgrest/top-level-control? "" "$order")
    (pgrest/top-level-control? "" "limit")
    (pgrest/top-level-control? "account" "$order")
    (pgrest/top-level-control? "" "status")])
  => [true true false false])

^{:refer xt.db.text.pgrest/value->query-text :added "4.1"}
(fact "formats nil scalars and booleans for query strings"

  (!.dt
   [(pgrest/value->query-text nil)
    (pgrest/value->query-text "open")
    (pgrest/value->query-text 10)
    (pgrest/value->query-text true)])
  => ["null" "open" "10" "true"])

^{:refer xt.db.text.pgrest/normalise-in-values :added "4.1"}
(fact "flattens nested in operands and wraps scalars"

  (!.dt
   [(pgrest/normalise-in-values [["ord-1" "ord-2"]])
    (pgrest/normalise-in-values ["ord-1" "ord-2"])
    (pgrest/normalise-in-values "ord-1")])
  => [["ord-1" "ord-2"]
      ["ord-1" "ord-2"]
      ["ord-1"]])

^{:refer xt.db.text.pgrest/compile-filter-value :added "4.1"}
(fact "compiles scalar and in filter operands"

  (!.dt
   [(pgrest/compile-filter-value "eq" "open")
    (pgrest/compile-filter-value "is" nil)
    (pgrest/compile-filter-value "in" [["ord-1" "ord-2"]])])
  => ["eq.open"
      "is.null"
      "in.(ord-1,ord-2)"])

^{:refer xt.db.text.pgrest/compile-filter-fragment :added "4.1"}
(fact "compiles one filter descriptor into a fragment"

  (!.dt
   (pgrest/compile-filter-fragment
    {"path" "account.id"
     "op" "eq"
     "value" "acct-1"}))
  => "account.id.eq.acct-1")

^{:refer xt.db.text.pgrest/compile-order-value :added "4.1"}
(fact "formats string tuple and nested order declarations"

  (!.dt
   [(pgrest/compile-order-value "status.desc")
    (pgrest/compile-order-value ["id" "asc"])
    (pgrest/compile-order-value [["status" "desc"]
                                 ["id" "asc"]])])
  => ["status.desc"
      "id.asc"
      "status.desc,id.asc"])

^{:refer xt.db.text.pgrest/compile-or-clause :added "4.1"}
(fact "accepts precompiled strings descriptors and nested where maps"

  (!.dt
   [(pgrest/compile-or-clause "status.eq.open")
    (pgrest/compile-or-clause
     {"path" "id"
      "op" "eq"
      "value" "ord-1"})
    (pgrest/compile-or-clause
     {"id" "ord-1"
      "status" ["eq" "open"]})])
  => ["status.eq.open"
      "id.eq.ord-1"
      "id.eq.ord-1,status.eq.open"])

^{:refer xt.db.text.pgrest/compile-filter-params :added "4.1"}
(fact "expands filter descriptors into query params"

  (!.dt
   (pgrest/compile-filter-params
    [{"path" "status"
      "op" "eq"
      "value" "open"}
     {"path" "or"
      "op" "or"
      "value" [{"id" "ord-1"}
               {"id" "ord-2"}]}]))
  => ["status=eq.open"
      "or=(id.eq.ord-1,id.eq.ord-2)"])

^{:refer xt.db.text.pgrest/compile-control-params :added "4.1"}
(fact "compiles top-level order limit and offset settings"

  (!.dt
   (pgrest/compile-control-params
    {"$order" [["status" "desc"]
               ["id" "asc"]]
     "$limit" 10
     "$offset" 20}))
  => ["order=status.desc,id.asc"
      "limit=10"
      "offset=20"])

^{:refer xt.db.text.pgrest/compile-query-string :added "4.1"}
(fact "joins params into a query string"

  (!.dt
   (pgrest/compile-query-string
    ["select=status"
     "status=eq.open"]))
  => "select=status&status=eq.open")

^{:refer xt.db.text.pgrest/compile-url :added "4.1"}
(fact "adds a query string only when params are present"

  (!.dt
   [(pgrest/compile-url "/rest/v1/Order"
                        ["select=status"])
    (pgrest/compile-url "/rest/v1/Order" [])])
  => ["/rest/v1/Order?select=status"
      "/rest/v1/Order"])

^{:refer xt.db.text.pgrest/compile-rpc :added "4.1"}
(fact "compiles rpc calls into POST request descriptions"

  (!.dt
   (pgrest/compile-rpc "list-orders"
                       {"status" "open"}))
  => {"type" "rpc"
      "fn" "list_orders"
      "method" "POST"
      "path" "/rest/v1/rpc/list_orders"
      "url" "/rest/v1/rpc/list_orders"
      "body" {"status" "open"}
      "headers" {"Content-Type" "application/json"}})

^{:refer xt.db.text.pgrest/compile-request :added "4.1"}
(fact "compiles full request maps from query plans"

  (!.dt
   (var compiled
        (pgrest/compile-request (@! +query-tree-advanced+)))
   [(. compiled ["table"])
    (. compiled ["select"])
    (. compiled ["query"])
    (. compiled ["url"])
    (xt/x:len (. compiled ["filters"]))])
  => ["Order"
      "status,name:first_name,account(nickname)"
      "select=status,name:first_name,account(nickname)&or=(status.eq.open,status.eq.pending)&account.id=eq.acct-1&id=in.(ord-1,ord-2)&order=status.desc,id.asc&limit=10&offset=20"
      "/rest/v1/Order?select=status,name:first_name,account(nickname)&or=(status.eq.open,status.eq.pending)&account.id=eq.acct-1&id=in.(ord-1,ord-2)&order=status.desc,id.asc&limit=10&offset=20"
      3])
