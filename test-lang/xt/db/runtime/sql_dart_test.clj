(ns xt.db.runtime.sql-dart-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l])
  (:use code.test))

^{:seedgen/scaffold {:all true}}
(do
  ^{:seedgen/root {:all true}}
  (l/script- :dart
    {:runtime :twostep
     :require [[xt.lang.spec-base :as xt]
                [xt.db.runtime.sql :as impl-sql]
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
                [dart.lib.driver-sqlite :as dart-sqlite]]})

  (def CANARY-DART
    (common/program-exists? "dart"))

  (fact:global
   {:setup    [(l/rt:restart)
               (if CANARY-DART
                 (do (l/rt:scaffold :dart)
                     true)
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
  (defn.dt sqlite-parity-runtime-dt
    []
    (var conn nil)
    (dart-sqlite/connect-constructor
     {:memory true}
     (fn [err raw]
       (when (xt/x:not-nil? err)
         (throw err))
       (:= conn (dart-sqlite/wrap-connection raw))
       (return conn)))
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
           xt/x:str-comp)
          (fn [row]
            (return [(. row ["id"])
                     (. row ["name"])]))))
    (return (xt/x:json-encode [nested flat])))

  ^{:refer xt.db.runtime.sql/sql-pull-sync :added "4.1"}
  (fact "returns the expected nested sqlite output in dart"

    (if CANARY-DART
      (!.dt
        (-/sqlite-parity-runtime-dt))
      :dart-unavailable)
    => (if CANARY-DART
         +sqlite-parity-output+
         :dart-unavailable)))
