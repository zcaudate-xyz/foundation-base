(ns xtbench.dart.net.conn-sql-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.net.conn-sql :as conn-sql]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.net.conn-sql/connection-create :added "4.1"}
(fact "creates a generic sql connection from a raw value and impl map"

  (!.dt
    (var conn (conn-sql/connection-create "raw" {"query" (fn [r q] ["js" r q])}))
    [(xt/x:has-key? conn "raw")
     (xt/x:has-key? conn "impl")
     (xt/x:get-key conn "raw")])
  => [true true "raw"])

^{:refer xt.net.conn-sql/connection-connect :added "4.1"}
(fact "connects through the impl connect function, or returns the client unchanged"

  (!.dt
    [(conn-sql/connection-connect {"impl" {} "raw" "R"} {})
     (conn-sql/connection-connect {"impl" {"connect" (fn [raw opts] (return ["connected" raw opts]))}
                                   "raw" "R"}
                                  {"opt" 1})])
  => [{"impl" {} "raw" "R"} ["connected" "R" {"opt" 1}]])

^{:refer xt.net.conn-sql/connection-disconnect :added "4.1"}
(fact "delegates disconnect to the impl disconnect function"

  (!.dt
    (conn-sql/connection-disconnect {"impl" {"disconnect" (fn [raw] (return ["disconnected" raw]))}
                                     "raw" "R"}))
  => ["disconnected" "R"])

^{:refer xt.net.conn-sql/connection-query :added "4.1"}
(fact "delegates query to the impl query or query_sync function"

  (!.dt
    [(conn-sql/connection-query {"impl" {"query" (fn [raw input] (return ["query" raw input]))}
                                 "raw" "R"}
                                "SELECT 1;")
     (conn-sql/connection-query {"impl" {"query_sync" (fn [raw input] (return ["sync" raw input]))}
                                 "raw" "R"}
                                "SELECT 2;")])
  => [["query" "R" "SELECT 1;"] ["sync" "R" "SELECT 2;"]])

^{:refer xt.net.conn-sql/connection-query-async :added "4.1"}
(fact "delegates async query to the impl query_async or query function"

  (!.dt
    [(conn-sql/connection-query-async {"impl" {"query_async" (fn [raw input] (return ["async" raw input]))}
                                       "raw" "R"}
                                      "SELECT 1;")
     (conn-sql/connection-query-async {"impl" {"query" (fn [raw input] (return ["fallback" raw input]))}
                                       "raw" "R"}
                                      "SELECT 2;")])
  => [["async" "R" "SELECT 1;"] ["fallback" "R" "SELECT 2;"]])
