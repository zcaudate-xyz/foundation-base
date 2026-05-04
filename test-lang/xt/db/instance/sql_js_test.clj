(ns xt.db.instance.sql-js-test
  (:require [hara.lang :as l]
              [std.string.prose :as prose]
              [xt.lang.common-notify :as notify]
              [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  (l/script- :xtalk
    {:require [[xt.lang.spec-base :as xt]
               [xt.lang.common-string :as str]
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
   :require [[xt.lang.spec-base :as xt]
             [xt.db.instance.sql :as impl-sql]
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
   :require [[xt.lang.spec-base :as xt]
             [xt.db.instance.sql :as impl-sql]
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
    :require [[xt.lang.spec-base :as xt]
              [xt.db.instance.sql :as impl-sql]
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
              [python.core.system :as pysys]
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

(def +user-profile-tree+
  ["UserAccount"
   ["nickname"
    ["profile"
     ["first_name"]]]])

(def +currency-bulk-tree+
  ["Currency"
   {"id" ["in" [["USD" "XLM"]]]}
   ["id" "name"]])

(def +sqlite-parity-output+
  "[[[\"root\",\"Root\"]],[[\"USD\",\"US Dollar\"],[\"XLM\",\"Stellar Coin\"]]]")

^{:seedgen/root {:all true}}
(defn.js sqlite-parity-runtime-js
  []
  (impl-sql/sql-process-event-sync INSTANCE
                                   "add"
                                   {"UserAccount" [sample/RootUser]}
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil))
  (var nested
       (xt/x:arr-map
        (impl-sql/sql-pull-sync INSTANCE
                                sample/Schema
                                (@! +user-profile-tree+)
                                (ut/sqlite-opts nil))
        (fn [row]
          (var profile (xt/x:first (. row ["profile"])))
          (return [(. row ["nickname"])
                   (. profile ["first_name"])]))))
  (var flat
       (xt/x:arr-map
        (xtd/arr-sort
         (impl-sql/sql-pull-sync INSTANCE
                                 sample/Schema
                                 (@! +currency-bulk-tree+)
                                 (ut/sqlite-opts nil))
         (fn [row]
           (return (. row ["id"])))
         k/lt)
        (fn [row]
          (return [(. row ["id"])
                   (. row ["name"])]))))
  (return (xt/x:json-encode [nested flat])))

^{:seedgen/derived true}
(defn.lua sqlite-parity-runtime-lua
  []
  (var conn (lua-sqlite/wrap-connection
             (lua-sqlite/connect-constructor {:memory true})))
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
  (var nested
       (xt/x:arr-map
        (impl-sql/sql-pull-sync conn
                                sample/Schema
                                (@! +user-profile-tree+)
                                (ut/sqlite-opts nil))
        (fn [row]
          (var profile (xt/x:first (. row ["profile"])))
          (return [(. row ["nickname"])
                   (. profile ["first_name"])]))))
  (var flat
       (xt/x:arr-map
        (xtd/arr-sort
         (impl-sql/sql-pull-sync conn
                                 sample/Schema
                                 (@! +currency-bulk-tree+)
                                 (ut/sqlite-opts nil))
         (fn [row]
           (return (. row ["id"])))
         k/lt)
        (fn [row]
          (return [(. row ["id"])
                   (. row ["name"])]))))
  (return (xt/x:json-encode [nested flat])))

^{:seedgen/derived true}
(defn.py sqlite-parity-runtime-py
  []
  (var conn (py-sqlite/wrap-connection
             (py-sqlite/connect-constructor {})))
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
  (var nested
       (xt/x:arr-map
        (impl-sql/sql-pull-sync conn
                                sample/Schema
                                (@! +user-profile-tree+)
                                (ut/sqlite-opts nil))
        (fn [row]
          (var profile (xt/x:first (. row ["profile"])))
          (return [(. row ["nickname"])
                   (. profile ["first_name"])]))))
  (var flat
       (xt/x:arr-map
        (xtd/arr-sort
         (impl-sql/sql-pull-sync conn
                                 sample/Schema
                                 (@! +currency-bulk-tree+)
                                 (ut/sqlite-opts nil))
         (fn [row]
           (return (. row ["id"])))
         k/lt)
        (fn [row]
          (return [(. row ["id"])
                   (. row ["name"])]))))
  (return (xt/x:json-encode [nested flat])))

(defn sqlite-parity-js
  []
  (notify/wait-on [:js 2000]
    (spec-promise/x:promise-then
     (dbsql/connect (js-sqlite/driver) {})
     (fn [conn]
       (try
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
         (var nested
              (xt/x:arr-map
               (impl-sql/sql-pull-sync conn
                                       sample/Schema
                                       (@! +user-profile-tree+)
                                       (ut/sqlite-opts nil))
               (fn [row]
                 (var profile (xt/x:first (. row ["profile"])))
                 (return [(. row ["nickname"])
                          (. profile ["first_name"])]))))
         (var flat
              (xt/x:arr-map
               (xtd/arr-sort
                (impl-sql/sql-pull-sync conn
                                        sample/Schema
                                        (@! +currency-bulk-tree+)
                                        (ut/sqlite-opts nil))
                (fn [row]
                  (return (. row ["id"])))
                k/lt)
               (fn [row]
                 (return [(. row ["id"])
                          (. row ["name"])]))))
         (repl/notify
          (xt/x:json-encode
           [nested flat]))
         (catch e
           (repl/notify e)))))
  )
)
)

(defn sqlite-parity-lua
  []
  (notify/wait-on [:lua.nginx 2000]
    (spec-promise/x:promise-then
     (dbsql/connect (lua-sqlite/driver) {:memory true})
     (fn [conn]
       (try
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
         (var nested
              (xt/x:arr-map
               (impl-sql/sql-pull-sync conn
                                       sample/Schema
                                       (@! +user-profile-tree+)
                                       (ut/sqlite-opts nil))
               (fn [row]
                 (var profile (xt/x:first (. row ["profile"])))
                 (return [(. row ["nickname"])
                          (. profile ["first_name"])]))))
         (var flat
              (xt/x:arr-map
               (xtd/arr-sort
                (impl-sql/sql-pull-sync conn
                                        sample/Schema
                                        (@! +currency-bulk-tree+)
                                        (ut/sqlite-opts nil))
                (fn [row]
                  (return (. row ["id"])))
                k/lt)
               (fn [row]
                 (return [(. row ["id"])
                          (. row ["name"])]))))
         (repl/notify
          (xt/x:json-encode
           [nested flat]))
         (catch e
           (repl/notify e)))))
  )
)

(defn sqlite-parity-python
  []
  (bootstrap-python)
  (!.py
   (impl-sql/sql-process-event-sync INSTANCE
                                    "add"
                                    {"UserAccount" [sample/RootUser]}
                                    sample/Schema
                                    sample/SchemaLookup
                                    (ut/sqlite-opts nil))
   (var nested
        (xt/x:arr-map
         (impl-sql/sql-pull-sync INSTANCE
                                 sample/Schema
                                 (@! +user-profile-tree+)
                                 (ut/sqlite-opts nil))
         (fn [row]
           (var profile (xt/x:first (. row ["profile"])))
           (return [(. row ["nickname"])
                    (. profile ["first_name"])]))))
   (var flat
        (xt/x:arr-map
         (xtd/arr-sort
          (impl-sql/sql-pull-sync INSTANCE
                                  sample/Schema
                                  (@! +currency-bulk-tree+)
                                  (ut/sqlite-opts nil))
          (fn [row]
            (return (. row ["id"])))
          k/lt)
         (fn [row]
           (return [(. row ["id"])
                    (. row ["name"])]))))
   (xt/x:json-encode [nested flat]))

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

^{:refer xt.db.instance.sql/sql-pull-sync :added "4.1"}
(fact "returns the same nested sqlite output across js lua and python"

  (!.js
   (-/sqlite-parity-runtime-js))
  => +sqlite-parity-output+

  (!.lua
   (-/sqlite-parity-runtime-lua))
  => +sqlite-parity-output+)
