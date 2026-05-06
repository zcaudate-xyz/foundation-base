(ns xtbench.ruby.protocol.connection-sql-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.protocol.connection-sql :as sqlp]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.connection-sql/ISqlConnectionDriver :added "4.1"}
(fact "defines the SQL connection protocol surfaces"

  (!.rb
    [sqlp/ISqlConnectionDriver
     sqlp/ISqlConnection
     sqlp/ISqlRuntimeDriver
     sqlp/ISqlRuntimeConnection])
  => [["connect"]
      ["disconnect" "query" "query_sync"]
      ["connect"]
      ["disconnect" "query" "query_sync"]])

(comment
  (s/snapto '[xt.protocol.connection-sql])
  
  (s/seedgen-langadd '[xt.protocol.connection-sql] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.connection-sql] {:lang [:lua :python] :write true}))
