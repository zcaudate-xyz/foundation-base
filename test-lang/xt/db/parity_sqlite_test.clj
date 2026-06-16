(ns xt.db.parity-sqlite-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-promise :as spec-promise])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as impl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-lib :as k]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as spec-promise]
             [xt.net.conn-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             [js.net.conn-sqlite :as js-sqlite]
             [js.net.conn-sqlite :as js-sqlite-wasm]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system/db-exec-sync :added "4.1"}
(fact "sqlite and sqlite-wasm execute raw xt.db.system sql the same way"

  (notify/wait-on [:js 5000]
    (var init-system-db
         (fn [driver]
           (return
            (spec-promise/x:promise-then
             (dbsql/connect driver {})
             (fn [conn]
               (var db (impl/db-create
                        {"::" "db.sql"
                         :instance conn}
                        sample/Schema
                        sample/SchemaLookup
                        (ut/sqlite-opts nil)))
               (dbsql/query (xt/x:get-key db "instance")
                                 (str/join "\n\n"
                                           (manage/table-create-all
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))))
               (return db))))))
    (-> (init-system-db (js-sqlite/create {}))
        (spec-promise/x:promise-then
         (fn [sqlite-db]
           (var sqlite-out (impl/db-exec-sync sqlite-db "SELECT 1;"))
           (-> (init-system-db (js-sqlite-wasm/create {}))
               (spec-promise/x:promise-then
                (fn [wasm-db]
                  (var wasm-out (impl/db-exec-sync wasm-db "SELECT 1;"))
                  (repl/notify [sqlite-out wasm-out]))))))))
  => [1 1])

^{:refer xt.db.system/db-pull-sync :added "4.1"}
(fact "sqlite and sqlite-wasm sync and pull the same xt.db.system rows"

  (notify/wait-on [:js 5000]
    (var init-system-db
         (fn [driver]
           (return
            (spec-promise/x:promise-then
             (dbsql/connect driver {})
             (fn [conn]
               (var db (impl/db-create
                        {"::" "db.sql"
                         :instance conn}
                        sample/Schema
                        sample/SchemaLookup
                        (ut/sqlite-opts nil)))
               (dbsql/query (xt/x:get-key db "instance")
                                 (str/join "\n\n"
                                           (manage/table-create-all
                                            sample/Schema
                                            sample/SchemaLookup
                                            (ut/sqlite-opts nil))))
               (return db))))))
    (-> (init-system-db (js-sqlite/create {}))
        (spec-promise/x:promise-then
         (fn [sqlite-db]
           (impl/sync-event sqlite-db
                            ["add" {"Currency" (@! sample/+currency+)}])
           (impl/sync-event sqlite-db
                            ["add" {"UserAccount" [sample/RootUser]}])
           (var sqlite-out
           [(xtd/arr-sort
             (impl/db-pull-sync sqlite-db
                                sample/Schema
                                ["Currency"
                                 ["id"]])
             (fn [row]
               (return (xt/x:get-key row "id")))
             k/lt)
            (impl/db-pull-sync sqlite-db
                               sample/Schema
                               ["UserAccount"
                                ["nickname"
                                      ["profile"
                                       ["first_name"]]]])])
           (-> (init-system-db (js-sqlite-wasm/create {}))
               (spec-promise/x:promise-then
                (fn [wasm-db]
                  (impl/sync-event wasm-db
                                   ["add" {"Currency" (@! sample/+currency+)}])
                  (impl/sync-event wasm-db
                                   ["add" {"UserAccount" [sample/RootUser]}])
                  (var wasm-out
                       [(xtd/arr-sort
                         (impl/db-pull-sync wasm-db
                                            sample/Schema
                                            ["Currency"
                                             ["id"]])
                         (fn [row]
                           (return (xt/x:get-key row "id")))
                         k/lt)
                        (impl/db-pull-sync wasm-db
                                           sample/Schema
                                           ["UserAccount"
                                            ["nickname"
                                             ["profile"
                                              ["first_name"]]]])])
                  (repl/notify [sqlite-out wasm-out]))))))))
  => [[[{"id" "STATS"} {"id" "USD"} {"id" "XLM"} {"id" "XLM.T"}]
       [{"nickname" "root", "profile" [{"first_name" "Root"}]}]]
      [[{"id" "STATS"} {"id" "USD"} {"id" "XLM"} {"id" "XLM.T"}]
       [{"nickname" "root", "profile" [{"first_name" "Root"}]}]]])
