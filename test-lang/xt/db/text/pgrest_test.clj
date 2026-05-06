(ns xt.db.text.pgrest-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
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

  (!.js
   [(pgrest/compile-select-item "status")
    (pgrest/compile-select-item ["name" "first_name"])
    (pgrest/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "name:first_name"
      "account(nickname)"])

^{:refer xt.db.text.pgrest/compile-select :added "4.1"}
(fact "compiles return vectors to PostgREST select syntax"

  (!.js
   (pgrest/compile-select
    ["status"
     ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer xt.db.text.pgrest/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into PostgREST filters"

  (!.js
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

  (!.js
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

  (!.js
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
  (s/run ['xt.db.text.base-view])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.text.base-view] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.text.base-view {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.base-view {:lang [:lua :python] :write true}))
