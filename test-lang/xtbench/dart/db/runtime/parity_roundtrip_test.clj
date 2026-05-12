(ns xtbench.dart.db.runtime.parity-roundtrip-test
  (:require [hara.lang :as l]
            [std.string.prose :as prose]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:require [[js.cell.service.db-query :as db-query]
          [xt.db.helpers.data-main-test :as sample]
          [xt.db.instance :as xdb]
          [xt.db.runtime.sql :as impl-sql]
          [xt.lang.spec-base :as xt]
          [xt.lang.common-data :as xtd]
          [xt.lang.common-lib :as k]
          [xt.lang.common-string :as str]
          [xt.lang.common-repl :as repl]
          [xt.lang.spec-promise :as spec-promise]
          [xt.protocol.impl.connection-sql :as dbsql]
          [xt.db.text.sql-util :as ut]
          [xt.db.text.sql-raw :as raw]
          [xt.db.text.sql-manage :as manage]
          [dart.lib.driver-sqlite :as dart-sqlite]]
          :runtime :twostep})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.runtime.sql/sql-gen-delete :added "4.0"}
(fact "generates delete statements"

  (!.dt
    (impl-sql/sql-gen-delete "HELLO"
                             ["A" "B"]
                             (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.runtime.sql/sql-process-event-sync :added "4.0"
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
                   (def +sql-touched-output+
                     ["UserAccount" "UserProfile"])
                   (def +nested-user-output+
                     [{"nickname" "root"
                       "profile" [{"first_name" "Root"}]}])]}
(fact "syncs and pulls sql data"

  (notify/wait-on [:dart 30000]
    (-> (dbsql/connect (dart-sqlite/driver) {})
        (spec-promise/x:promise-then
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
            [(xtd/arr-sort
              (impl-sql/sql-process-event-sync conn
                                               "add"
                                               {"UserAccount" [sample/RootUser]}
                                               sample/Schema
                                               sample/SchemaLookup
                                               (ut/sqlite-opts nil))
              k/identity
              k/lt)
             (impl-sql/sql-pull-sync conn
                                     sample/Schema
                                     (@! +user-profile-tree+)
                                     (ut/sqlite-opts nil))])))))
  => [+sql-touched-output+
      +nested-user-output+])

^{:refer xt.db.runtime.sql/sql-process-event-remove :added "4.0"
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
                   (def +sql-touched-output+
                     ["UserAccount" "UserProfile"])
                   (def +sql-remove-output+
                     (prose/|
                      "DELETE FROM \"UserAccount\" WHERE \"id\" = '00000000-0000-0000-0000-000000000000';"
                      ""
                      "DELETE FROM \"UserProfile\" WHERE \"id\" = 'c4643895-b0ce-44cc-b07b-2386bf18d43b';"))]}
(fact "emits remove sql and deletes synced rows"

  (notify/wait-on [:dart 30000]
    (-> (dbsql/connect (dart-sqlite/driver) {})
        (spec-promise/x:promise-then
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
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (repl/notify
            [(impl-sql/sql-process-event-remove conn
                                                "input"
                                                {"UserAccount" [sample/RootUser]}
                                                sample/Schema
                                                sample/SchemaLookup
                                                (ut/sqlite-opts nil))
             (xtd/arr-sort
              (impl-sql/sql-process-event-remove conn
                                                 "remove"
                                                 {"UserAccount" [sample/RootUser]}
                                                 sample/Schema
                                                 sample/SchemaLookup
                                                 (ut/sqlite-opts nil))
              k/identity
              k/lt)
             (xt/x:len
              (impl-sql/sql-pull-sync conn
                                      sample/Schema
                                      (@! +user-profile-tree+)
                                      (ut/sqlite-opts nil)))])))))
  => [+sql-remove-output+
      +sql-touched-output+
      0])
