(ns
 xtbench.dart.sys.conn-dbsql-test
 (:require
  [rt.basic.type-common :as common]
  [std.lang :as l]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.sys.conn-dbsql :as dbsql]
   [dart.lib.driver-sqlite :as dart-sqlite]]})

(def CANARY-DART (common/program-exists? "dart"))

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.sys.conn-dbsql/connect, :added "4.0"}
(fact
 "connects to a database"
 ^{:hidden true}
 (if
  CANARY-DART
  (!.dt
   (var
    conn
    (dbsql/connect
     {:constructor dart-sqlite/connect-constructor, :memory true}
     nil))
   (dbsql/query-sync conn "SELECT 1;"))
  :dart-unavailable)
 =>
 (any 1 :dart-unavailable))

^{:refer xt.sys.conn-dbsql/disconnect, :added "4.0"}
(fact
 "disconnects form database"
 (if
  CANARY-DART
  (!.dt
   (var
    conn
    (dbsql/connect
     {:constructor dart-sqlite/connect-constructor, :memory true}
     nil))
   (dbsql/disconnect conn nil))
  :dart-unavailable)
 =>
 (any true :dart-unavailable))

^{:refer xt.sys.conn-dbsql/query-sync, :added "4.0"}
(fact
 "sends a synchronous query"
 (if
  CANARY-DART
  (!.dt
   (var
    conn
    (dbsql/connect
     {:constructor dart-sqlite/connect-constructor, :memory true}
     nil))
   (dbsql/query conn "CREATE TABLE test (id INTEGER, name TEXT);" nil)
   (dbsql/query
    conn
    "INSERT INTO test (id, name) VALUES (1, 'alpha');"
    nil)
   (dbsql/query-sync conn "SELECT name FROM test;"))
  :dart-unavailable)
 =>
 (any "alpha" :dart-unavailable))
