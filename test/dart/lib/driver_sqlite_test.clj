(ns dart.lib.driver-sqlite-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[dart.lib.driver-sqlite :as dart-sqlite]
             [xt.sys.conn-dbsql :as dbsql]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact "connects to sqlite through the Dart driver"
  (if CANARY-DART
    [(!.dt
       (var conn (dart-sqlite/connect-constructor {:memory true} nil))
       (dart-sqlite/raw-query (. conn ["raw"]) "SELECT 1;"))
     (!.dt
       (var conn (dbsql/connect {:constructor dart-sqlite/connect-constructor
                                 :memory true}
                                nil))
       (dbsql/query-sync conn "SELECT 1;"))
     (!.dt
       (var conn (dbsql/connect {:constructor dart-sqlite/connect-constructor
                                 :memory true}
                                nil))
       (dbsql/query conn "CREATE TABLE test (id INTEGER, name TEXT);" nil)
       (dbsql/query conn "INSERT INTO test (id, name) VALUES (1, 'alpha');" nil)
       (dbsql/query conn "SELECT name FROM test;" nil))
     (!.dt
       (var conn (dbsql/connect {:constructor dart-sqlite/connect-constructor
                                 :memory true}
                                nil))
       (dbsql/disconnect conn nil))]
    :dart-unavailable)
  => (any [1 1 "alpha" true]
          :dart-unavailable))
