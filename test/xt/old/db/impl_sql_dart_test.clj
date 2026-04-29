(ns xt.old.db.impl-sql-dart-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[dart.lib.driver-sqlite :as dart-sqlite]
             [xt.old.db.impl-sql :as impl-sql]
             [xt.old.db.sample-test :as sample]
             [xt.old.db.sql-manage :as manage]
              [xt.old.db.sql-raw :as raw]
              [xt.old.db.sql-util :as ut]
              [xt.lang.common-string :as str]
              [xt.lang.common-repl :as repl]
              [xt.lang.spec-promise :as spec-promise]
              [xt.old.sys.conn-dbsql :as dbsql]]})

(def CANARY-DART
  (common/program-exists? "dart"))

^{:lang-exceptions {:js {:skip true}
                    :python {:skip true}
                    :lua {:skip true}}}
(fact "runs a minimal xt.old.db sqlite flow on Dart"

  (if CANARY-DART
    [(notify/wait-on
      :dart
      (spec-promise/x:promise-then
       (dbsql/connect (dart-sqlite/driver)
                      {:memory true})
       (fn [conn]
         (dbsql/query-sync conn
                           (str/join "\n\n"
                                     (manage/table-create-all
                                      sample/Schema
                                      sample/SchemaLookup
                                      (ut/sqlite-opts nil))))
         (dbsql/query-sync conn
                           (raw/raw-insert "Currency"
                                           ["id" "type" "symbol" "native" "decimal"
                                            "name" "plural" "description"]
                                           (@! sample/+currency+)
                                           (ut/sqlite-opts nil)))
         (repl/notify
          (dbsql/query-sync conn "SELECT count(*) FROM \"Currency\";")))))
     (notify/wait-on
      :dart
      (spec-promise/x:promise-then
       (dbsql/connect (dart-sqlite/driver)
                      {:memory true})
       (fn [conn]
         (dbsql/query-sync conn
                           (str/join "\n\n"
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
         (repl/notify
          (impl-sql/sql-pull-sync
           conn
           sample/Schema
           ["UserAccount"
            ["nickname"
             ["profile"
              ["first_name"]]]]
           (ut/sqlite-opts nil))))))]
    :dart-unavailable)
  => (any [4 [{"nickname" "root"
               "profile" [{"first_name" "Root"}]}]]
          :dart-unavailable))
