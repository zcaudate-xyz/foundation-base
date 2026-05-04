(ns xt.db.instance.sql-js-test
  (:require [hara.lang :as l]
             [std.string.prose :as prose]
             [xt.lang.common-notify :as notify])
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

  (fact:global
   {:setup    [(l/rt:restart)
               (do (l/rt:scaffold :js)
                   true)
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
(fact "returns the expected nested sqlite output in js"

  (!.js
    (-/sqlite-parity-runtime-js))
  => +sqlite-parity-output+)
