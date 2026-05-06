(ns xtbench.python.db.text.pgrest-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
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

^{:refer xt.db.text.pgrest/compile-select-item :added "4.1"}
(fact "compiles nested return entries to PostgREST select syntax"

  (!.py
   [(pgrest/compile-select-item "status")
    (pgrest/compile-select-item ["account" ["nickname"]])])
  => ["status"
      "account(nickname)"])

^{:refer xt.db.text.pgrest/compile-select :added "4.1"}
(fact "compiles return vectors to PostgREST select syntax"

  (!.py
   (pgrest/compile-select
    ["status"
     ["account" ["nickname"]]]))
  => "status,account(nickname)")

^{:refer xt.db.text.pgrest/compile-filters-into :added "4.1"}
(fact "compiles nested where clauses into PostgREST filters"

  (!.py
   (pgrest/compile-filters-into
    ""
    {"account" {"id" "acct-1"}
     "status" "open"}
    []))
  => [{"path" "account.id"
       "op" "eq"
       "value" "acct-1"}
      {"path" "status"
       "op" "eq"
       "value" "open"}])

^{:refer xt.db.text.pgrest/compile-query :added "4.1"}
(fact "compiles the same PostgREST request across js lua and python"

  (!.py
   (var compiled
        (pgrest/compile-query
         (@! +query-tree-basic+)))
   [(. compiled ["table"])
    (. compiled ["select"])
    (. (. (. compiled ["filters"]) [0]) ["path"])
    (. (. (. compiled ["filters"]) [0]) ["value"])])
  => ["Order"
      "status,account(nickname)"
      "account.id"
      "acct-1"])

(comment
  (s/run ['xt.db.text.base-view])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r] :write true})
  (s/seedgen-benchadd '[xt.db.text.base-view] {:lang [:julia :dart] :write true})
  
  (s/seedgen-langadd 'xt.db.text.base-view {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.db.text.base-view {:lang [:lua :python] :write true}))
