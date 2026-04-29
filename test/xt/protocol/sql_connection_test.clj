(ns xt.protocol.sql-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.sql-connection :as sqlp]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.protocol.sql-connection :as sqlp]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.protocol.sql-connection :as sqlp]]})

(def CANARY-DART
  (common/program-exists? "dart"))

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

  (if CANARY-DART
    (!.dt
      [sqlp/ISqlConnectionDriver
       sqlp/ISqlConnection
       sqlp/ISqlRuntimeDriver
       sqlp/ISqlRuntimeConnection])
    :dart-unavailable)
  => (any [["connect"]
           ["disconnect" "query" "query_sync"]
           ["connect"]
           ["disconnect" "query" "query_sync"]]
          :dart-unavailable))
