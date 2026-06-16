(ns xt.db-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as impl]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.net.conn-sql :as dbsql]
             [xt.db.text.sql-util :as ut]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-manage :as manage]
             [xt.db.helpers.data-main-test :as sample]
             [js.net.conn-sqlite :as js-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (. (dbsql/connect (js-sqlite/create {}) {})
       (then (fn [conn]
               (try
                 (:= (!:G DBSQL) (impl/db-create
                                  {"::" "db.sql"
                                   :instance conn}
                                  sample/Schema
                                  sample/SchemaLookup
                                  (ut/sqlite-opts nil)))
                 (dbsql/query (xt/x:get-key DBSQL "instance")
                                   (str/join "\n\n"
                                             (manage/table-create-all
                                              sample/Schema
                                              sample/SchemaLookup
                                              (ut/sqlite-opts nil))))
                 (repl/notify true)
                 (catch e (repl/notify e))))))))

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system/db-exec-sync :added "4.1"}
(fact "executes raw sql through xt.db"

  (!.js
   (impl/db-exec-sync DBSQL "SELECT 1;"))
  => 1)

^{:refer xt.db.system/db-pull-sync :added "4.1"}
(fact "syncs rows and pulls them back"

  [(set (!.js
         (impl/sync-event
          DBSQL
          ["add" {"Currency" (@! sample/+currency+)}])
         (impl/db-pull-sync DBSQL
                            sample/Schema
                            ["Currency"
                             ["id"]])))
   (!.js
    (impl/sync-event DBSQL
                     ["add" {"UserAccount" [sample/RootUser]}])
    (impl/db-pull-sync DBSQL
                       sample/Schema
                       ["UserAccount"
                        ["nickname"
                         ["profile"
                          ["first_name"]]]]))]
  => [#{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}}
      [{"nickname" "root", "profile" [{"first_name" "Root"}]}]])
