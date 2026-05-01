(ns xt.protocol.sql-connection-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.sql-connection :as sqlp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.sql-connection :as sqlp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.sql-connection :as sqlp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.sql-connection/ISqlConnectionDriver :added "4.1"}
(fact "defines the SQL connection protocol surfaces"

  (!.js
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_sync"]
      ["connect"]
      ["disconnect" "query" "query_sync"]]

  (!.lua
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_sync"]
      ["connect"]
      ["disconnect" "query" "query_sync"]]

  (!.py
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_sync"]
      ["connect"]
      ["disconnect" "query" "query_sync"]])

(comment
  (s/snapto '[xt.protocol.sql-connection])
  
  (s/seedgen-langadd '[xt.protocol.sql-connection] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.sql-connection] {:lang [:lua :python] :write true}))
