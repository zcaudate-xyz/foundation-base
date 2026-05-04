(ns xt.db.instance.sql-js-test
  (:require [hara.lang :as l]
              [std.string.prose :as prose]
              [xt.lang.common-notify :as notify]
              [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  (l/script- :xtalk
    {:require [[xt.lang.common-string :as str]
               [xt.lang.common-repl :as repl]
               [xt.lang.spec-promise :as spec-promise]
               [xt.protocol.impl.connection-sql :as dbsql]
               [xt.db.text.sql-util :as ut]
               [xt.db.text.sql-raw :as raw]
               [xt.db.text.sql-manage :as manage]
               [xt.db.helpers.data-main-test :as sample]]})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.instance.sql :as impl-sql]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

^{:seedgen/derived true}
(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db.instance.sql :as impl-sql]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

^{:seedgen/derived true}
(l/script- :python
  {:runtime :basic
   :require [[xt.db.instance.sql :as impl-sql]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
              [xt.db.helpers.data-main-test :as sample]
              [python.lib.driver-sqlite :as py-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (. (dbsql/connect (js-sqlite/driver) {})
       (then (fn [conn]
               (try
                 (:= (!:G INSTANCE) conn)
                 (dbsql/query-sync INSTANCE
                                   (str/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                              sample/SchemaLookup
                                              (ut/sqlite-opts nil))))
                 (dbsql/query-sync INSTANCE
                                   (raw/raw-insert "Currency"
                                                   ["id" "type" "symbol" "native" "decimal"
                                                    "name" "plural" "description"]
                                                   (@! sample/+currency+)
                                                   (ut/sqlite-opts nil)))
                 (repl/notify true)
                 (catch e
                   (repl/notify e))))))))

(defn bootstrap-lua
  []
  (notify/wait-on [:lua.nginx 2000]
    (. (dbsql/connect (lua-sqlite/driver) {:memory true})
       (then (fn [conn]
               (try
                 (:= (!:G INSTANCE) conn)
                 (dbsql/query-sync INSTANCE
                                   (str/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                              sample/SchemaLookup
                                              (ut/sqlite-opts nil))))
                 (dbsql/query-sync INSTANCE
                                   (raw/raw-insert "Currency"
                                                   ["id" "type" "symbol" "native" "decimal"
                                                    "name" "plural" "description"]
                                                   (@! sample/+currency+)
                                                   (ut/sqlite-opts nil)))
                 (repl/notify true)
                 (catch e
                   (repl/notify e))))))))

(defn bootstrap-python
  []
  (notify/wait-on [:python 2000]
    (. (dbsql/connect (py-sqlite/driver) {})
       (then (fn [conn]
               (try
                 (:= (!:G INSTANCE) conn)
                 (dbsql/query-sync INSTANCE
                                   (str/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                              sample/SchemaLookup
                                              (ut/sqlite-opts nil))))
                 (dbsql/query-sync INSTANCE
                                   (raw/raw-insert "Currency"
                                                   ["id" "type" "symbol" "native" "decimal"
                                                    "name" "plural" "description"]
                                                   (@! sample/+currency+)
                                                   (ut/sqlite-opts nil)))
                 (repl/notify true)
                 (catch e
                   (repl/notify e))))))))

(fact:global
 {:setup    [(l/rt:restart)
             ^{:seedgen/base {:lua {:input (do (l/rt:scaffold :lua.nginx)
                                               true)}
                              :python {:input (do (l/rt:scaffold :python)
                                                  true)}}}
             (do (l/rt:scaffold :js)
                 true)
             ^{:seedgen/base {:lua {:input (bootstrap-lua)}
                               :python {:input (bootstrap-python)}}}
             (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.instance.sql/sql-gen-delete :added "4.0"}
(fact "generates delete statements"

  (!.js
   (impl-sql/sql-gen-delete "HELLO"
                            ["A" "B"]
                            (ut/sqlite-opts nil)))
  => ["DELETE FROM \"HELLO\" WHERE \"id\" = 'A';"
      "DELETE FROM \"HELLO\" WHERE \"id\" = 'B';"])

^{:refer xt.db.instance.sql/sql-process-event-remove :added "4.0"}
(fact "syncs, removes, and pulls sql data"

  (!.js
   (xtd/arr-sort (impl-sql/sql-process-event-sync
                  INSTANCE
                  "add"
                  {"UserAccount" [sample/RootUser]}
                  sample/Schema
                  sample/SchemaLookup
                  (ut/sqlite-opts nil))
                 k/identity
                 k/lt))
  => ["UserAccount" "UserProfile"]

  (!.js
   (impl-sql/sql-pull-sync
     INSTANCE
     sample/Schema
     ["UserAccount"
     ["nickname"
      ["profile"
       ["first_name"]]]]
    (ut/sqlite-opts nil)))
  => [{"nickname" "root", "profile" [{"first_name" "Root"}]}]

  (!.js
   (impl-sql/sql-process-event-remove
    INSTANCE
    "input"
    {"UserAccount" [sample/RootUser]}
    sample/Schema
    sample/SchemaLookup
    (ut/sqlite-opts nil)))
  => (prose/|
      "DELETE FROM \"UserAccount\" WHERE \"id\" = '00000000-0000-0000-0000-000000000000';"
      ""
      "DELETE FROM \"UserProfile\" WHERE \"id\" = 'c4643895-b0ce-44cc-b07b-2386bf18d43b';")

  (!.js
   (xtd/arr-sort
    (impl-sql/sql-process-event-remove
     INSTANCE
     "remove"
     {"UserAccount" [sample/RootUser]}
     sample/Schema
     sample/SchemaLookup
     (ut/sqlite-opts nil))
    k/identity
    k/lt))
  => ["UserAccount" "UserProfile"]

  (!.js
   (impl-sql/sql-pull-sync
    INSTANCE
    sample/Schema
    ["UserAccount"
     ["nickname"
      ["profile"
        ["first_name"]]]]
     (ut/sqlite-opts nil)))
   => empty?)
)
