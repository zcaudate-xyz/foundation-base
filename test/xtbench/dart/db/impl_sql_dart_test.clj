(ns
 xtbench.dart.db.impl-sql-dart-test
 (:use code.test)
 (:require [rt.basic.type-common :as common] [std.lang :as l]))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[dart.lib.driver-sqlite :as dart-sqlite]
   [xt.db.impl-sql :as impl-sql]
   [xt.db.sample-test :as sample]
   [xt.db.sql-manage :as manage]
   [xt.db.sql-raw :as raw]
   [xt.db.sql-util :as ut]
   [xt.lang.common-string :as str]
   [xt.sys.conn-dbsql :as dbsql]]})

(def CANARY-DART (common/program-exists? "dart"))

(fact
 "runs a minimal xt.db sqlite flow on Dart"
 (if
  CANARY-DART
  [(!.dt
    (var
     conn
     (dbsql/connect
      {:constructor dart-sqlite/connect-constructor, :memory true}
      nil))
    (dbsql/query-sync
     conn
     (str/join
      "\n\n"
      (manage/table-create-all
       sample/Schema
       sample/SchemaLookup
       (ut/sqlite-opts nil))))
    (dbsql/query-sync
     conn
     (raw/raw-insert
      "Currency"
      ["id"
       "type"
       "symbol"
       "native"
       "decimal"
       "name"
       "plural"
       "description"]
      (@! sample/+currency+)
      (ut/sqlite-opts nil)))
    (dbsql/query-sync conn "SELECT count(*) FROM \"Currency\";"))
   (!.dt
    (var
     conn
     (dbsql/connect
      {:constructor dart-sqlite/connect-constructor, :memory true}
      nil))
    (dbsql/query-sync
     conn
     (str/join
      "\n\n"
      (manage/table-create-all
       sample/Schema
       sample/SchemaLookup
       (ut/sqlite-opts nil))))
    (impl-sql/sql-process-event-sync
     conn
     "add"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil))
    (impl-sql/sql-pull-sync
     conn
     sample/Schema
     ["UserAccount" ["nickname" ["profile" ["first_name"]]]]
     (ut/sqlite-opts nil)))]
  :dart-unavailable)
 =>
 (any
  [4 [{"nickname" "root", "profile" [{"first_name" "Root"}]}]]
  :dart-unavailable))
