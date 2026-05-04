(ns xt.db.instance.sql-lua-test
  (:require [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
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

  (fact:global
   {:setup    [(l/rt:restart)
                (do (l/rt:scaffold :lua.nginx)
                    true)]
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

  ^{:refer xt.db.instance.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in lua"

    (!.lua
      (-/sqlite-parity-runtime-lua))
    => +sqlite-parity-output+))
