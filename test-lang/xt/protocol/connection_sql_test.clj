(ns xt.protocol.connection-sql-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.connection-sql :as sqlp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.connection-sql :as sqlp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.connection-sql :as sqlp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.connection-sql/ISqlConnectionDriver :added "4.1"}
(fact "defines the SQL connection protocol surfaces"

  (!.js
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_async"]
      ["connect"]
      ["disconnect" "query" "query_async"]]

  (!.lua
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_async"]
      ["connect"]
      ["disconnect" "query" "query_async"]]

  (!.py
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_async"]
      ["connect"]
      ["disconnect" "query" "query_async"]])

(comment
  (s/snapto '[xt.protocol.connection-sql])
  
  (s/seedgen-langadd '[xt.protocol.connection-sql] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.connection-sql] {:lang [:lua :python] :write true}))
