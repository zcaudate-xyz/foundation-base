(ns xt.db.runtime.parity-sqlite-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

^{:seedgen/root {:all true
                 :langs [:lua.nginx :python :dart]
                 :js           {:extra [[js.lib.driver-sqlite :as js-sqlite]]}
                 :lua.nginx    {:extra [[lua.nginx.driver-sqlite :as lua-sqlite]]}
                 :python       {:extra [[python.lib.driver-sqlite :as py-sqlite]]}
                 :dart         {:extra [[dart.lib.driver-sqlite :as dart-sqlite]]}
                 :ruby         {:extra [[ruby.lib.driver-sqlite :as ruby-sqlite]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             ^{:seedgen/extra true}
             [js.lib.driver-sqlite :as js-sqlite]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:ref xt.db.runtime.sql/sql-process-event-sync
  :setup [(def +sqlite-touched-output+
            ["UserAccount" "UserProfile"])]}
(fact "js runtime reports the touched sqlite tables"


  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
                (xtd/arr-sort
                 (impl-sql/sql-process-event-sync conn
                                                  "add"
                                                  {"UserAccount" [sample/RootUser]}
                                                  sample/Schema
                                                  sample/SchemaLookup
                                                  (ut/sqlite-opts nil))
                 k/identity
                 k/lt))
           (repl/notify out)))))
  => +sqlite-touched-output+)

^{:ref xt.db.runtime.sql/sql-pull-sync
  :setup [(def +user-profile-tree+
            ["UserAccount"
             ["nickname"
              ["profile"
               ["first_name"]]]])
          (def +sqlite-nested-output+
            [["root" "Root"]])]}
(fact "js runtime pulls nested sqlite sample data"


  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (impl-sql/sql-process-event-sync conn
                                            "add"
                                            {"UserAccount" [sample/RootUser]}
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))
           (var out
                (xt/x:arr-map
                 (impl-sql/sql-pull-sync conn
                                         sample/Schema
                                         (@! +user-profile-tree+)
                                         (ut/sqlite-opts nil))
                 (fn [row]
                   (var profile (xt/x:first (. row ["profile"])))
                   (return [(. row ["nickname"])
                            (. profile ["first_name"])]))))
           (repl/notify out)))))
  => +sqlite-nested-output+)

^{:ref xt.db.runtime.sql/sql-pull-sync.currencies
  :setup [(def +currency-bulk-tree+
            ["Currency"
             {"id" ["in" [["USD" "XLM"]]]}
             ["id" "name"]])

          (def +sqlite-currencies-output+
            [["USD" "US Dollar"]
             ["XLM" "Stellar Coin"]])]}
(fact "js runtime pulls sorted sqlite currencies"

  ^{:seedgen/base {:lua.nginx    {:transform '{(js-sqlite/driver) (lua-sqlite/driver)}}
                   :python       {:transform '{(js-sqlite/driver) (py-sqlite/driver)}}
                   :dart         {:transform '{(js-sqlite/driver) (dart-sqlite/driver)}}}}
  (notify/wait-on [:js 5000]
    (-> (dbsql/connect (js-sqlite/driver) {})
        (spec-promise/x:promise-then
         (fn [conn]
           (dbsql/query conn
                             (str/join "\n\n"
                                       (manage/table-create-all
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil))))
           (dbsql/query conn
                             (raw/raw-insert "Currency"
                                             ["id" "type" "symbol" "native" "decimal"
                                              "name" "plural" "description"]
                                             (@! sample/+currency+)
                                             (ut/sqlite-opts nil)))
           (var out
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
           (repl/notify out)))))
  => +sqlite-currencies-output+)
