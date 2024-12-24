(ns xt.db-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.db :as impl]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [lua.nginx.driver-sqlite :as lua-sqlite]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.db :as impl]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-lua
  []
  (!.lua
   (var ngxsqlite (require "lsqlite3complete"))
   (:= (!:G DBSQL) (impl/db-create {"::" "db.sql"
                                    :constructor lua-sqlite/connect-constructor
                                    :memory true}
                                   sample/Schema
                                   sample/SchemaLookup
                                   (ut/sqlite-opts nil)))
   (dbsql/query-sync (k/get-key DBSQL "instance") 
                     (k/join "\n\n"
                             (manage/table-create-all
                              sample/Schema
                              sample/SchemaLookup
                              (ut/sqlite-opts nil))))
   (:= (!:G DBCACHE) (impl/db-create {"::" "db.cache"}
                                     sample/Schema
                                     sample/SchemaLookup
                                     (ut/sqlite-opts nil)))
   true))

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (var initSql (require "sql.js"))
    (-> (initSql)
        (. (then (fn [SQL]
                   (try
                     (:= (!:G SQL) SQL)
                     (:= (!:G DBSQL) (impl/db-create
                                      {"::" "db.sql"
                                       :instance (js-sqlite/set-methods
                                                  (new SQL.Database))}
                                      sample/Schema
                                      sample/SchemaLookup
                                      (ut/sqlite-opts nil)))
                     (dbsql/query-sync (k/get-key DBSQL "instance") 
                                       (k/join "\n\n"
                                               (manage/table-create-all
                                                sample/Schema
                                                sample/SchemaLookup
                                                (ut/sqlite-opts nil))))
                     (:= (!:G DBCACHE) (impl/db-create
                                        {"::" "db.cache"}
                                        sample/Schema
                                        sample/SchemaLookup
                                        (ut/sqlite-opts nil)))
                     (repl/notify true)
                     (catch e (repl/notify e)))))))))

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (do (l/rt:scaffold :lua)
                 true)
             (bootstrap-js)
             (bootstrap-lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db/process-event :added "4.0"}
(fact "processes an event"
  ^:hidden
  
  (!.js
   [(k/sort (impl/process-event
             DBSQL
             ["add" {"UserAccount" [sample/RootUser]}]
             sample/Schema
             sample/SchemaLookup
             (ut/sqlite-opts nil)))
    (k/sort (impl/process-event
             DBCACHE
             ["add" {"UserAccount" [sample/RootUser]}]
             sample/Schema
             sample/SchemaLookup
             nil))])
  => [["UserAccount" "UserProfile"]
      ["UserAccount" "UserProfile"]]

  (!.lua
   [(k/sort (impl/process-event
             DBSQL
             ["add" {"UserAccount" [sample/RootUser]}]
             sample/Schema
             sample/SchemaLookup
             (ut/sqlite-opts nil)))
    (k/sort (impl/process-event
             DBCACHE
             ["add" {"UserAccount" [sample/RootUser]}]
             sample/Schema
             sample/SchemaLookup
             nil))])
  => [["UserAccount" "UserProfile"]
      ["UserAccount" "UserProfile"]])

^{:refer xt.db/process-triggers :added "4.0"
  :setup [(bootstrap-js)
          (bootstrap-lua)]}
(fact "process triggers"
  ^:hidden

  [(notify/wait-on :js
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "listen")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on :js
     (impl/remove-trigger DBCACHE "test")
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (k/get-key trigger "listen")))
                                       :listen ["UserProfile"]
                                       :async true})
     (impl/queue-event DBCACHE ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on [:js 500]
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "listen")))
                                     :listen ["HELLO"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))]
  => [["UserAccount"]
      ["UserProfile"]
      :timeout]

  [(notify/wait-on :lua
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "listen")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on :lua
     (impl/remove-trigger DBCACHE "test")
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (k/get-key trigger "listen")))
                                       :listen ["UserProfile"]
                                       :async true})
     (impl/queue-event DBCACHE ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on [:lua 500]
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "listen")))
                                     :listen ["HELLO"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))]
  => [["UserAccount"]
      ["UserProfile"]
      :timeout])

^{:refer xt.db/add-trigger :added "4.0"
  :setup [(bootstrap-js)
          (bootstrap-lua)]}
(fact "adds a trigger to db"
  ^:hidden
  
  [(notify/wait-on :js
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "id")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/db-trigger DBSQL {"UserAccount" true}))
   (notify/wait-on :js
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (k/get-key trigger "id")))
                                       :listen ["UserAccount"]
                                       :async true})
     (impl/db-trigger DBCACHE {"UserAccount" true}))]
  => ["test" "test"]

  
  [(notify/wait-on :lua
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (k/get-key trigger "id")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/db-trigger DBSQL {"UserAccount" true}))
   (notify/wait-on :lua
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (k/get-key trigger "id")))
                                       :listen ["UserAccount"]
                                       :async true})
     (impl/db-trigger DBCACHE {"UserAccount" true}))]
  => ["test" "test"])

^{:refer xt.db/remove-trigger :added "4.0"}
(fact "removes the trigger")

^{:refer xt.db/db-trigger :added "4.0"}
(fact "performs the trigger")

^{:refer xt.db/db-create :added "4.0"}
(fact "creates the db")

^{:refer xt.db/queue-event :added "4.0"}
(fact "queues an event to the db")

^{:refer xt.db/sync-event :added "4.0"}
(fact "syncs an event to the db")

^{:refer xt.db/db-exec-sync :added "4.0"}
(fact "runs a raw statement"
  ^:hidden
  
  (!.js
   (impl/db-exec-sync DBSQL "Select 1;"))
  => 1

  (!.lua
   (impl/db-exec-sync DBSQL "Select 1;"))
  => 1)

^{:refer xt.db/db-pull-sync :added "4.0"
  :setup [(bootstrap-js)
          (bootstrap-lua)
          (def +countries+
            #{{"id" "USD"} {"id" "XLM.T"} {"id" "STATS"} {"id" "XLM"}})
          (def +account0+
            (contains-in
             [{"is_official" 0,
               "nickname" "root",
               "profile"
               [{"city" nil,
                 "about" nil,
                 "id" "c4643895-b0ce-44cc-b07b-2386bf18d43b",
                 "last_name" "User",
                 "first_name" "Root",
                 "language" "en"}],
               "id" "00000000-0000-0000-0000-000000000000",
               "is_suspended" 0,
               "password_updated" number?
               "is_super" 1}]))]}
(fact "runs a pull statement"
  ^:hidden
  
  [(set (!.js
         (impl/sync-event
          DBSQL
          ["add"
           {"Currency" (@! sample/+currency+)}])
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
                        ["*/data"
                         ["profile"]]]))]
  => (contains [+countries+
                +account0+])

  [(set (!.js
         (impl/sync-event DBCACHE
          ["add" {"Currency" (@! sample/+currency+)}])
         (impl/db-pull-sync DBCACHE
                            sample/Schema
                            ["Currency"
                             ["id"]])))
   (!.js
    (impl/sync-event DBCACHE
                     ["add" {"UserAccount" [sample/RootUser]}])
    (impl/db-pull-sync DBCACHE
                       sample/Schema
                       ["UserAccount"
                        ["*/data"
                         ["profile"]]]))]
  => (contains [+countries+])
  
  [(set (!.lua
         (impl/sync-event
          DBSQL
          ["add"
           {"Currency" (@! sample/+currency+)}])
         (impl/db-pull-sync DBSQL
                           sample/Schema
                           ["Currency"
                            ["id"]]
                           nil)))
   (!.lua
    (impl/sync-event DBSQL
                     ["add" {"UserAccount" [sample/RootUser]}])
   (impl/db-pull-sync DBSQL
                     sample/Schema
                     ["UserAccount"
                      ["*/data"
                       ["profile"]]]
                     nil))]
  => (contains [+countries+
                +account0+])

  [(set (!.lua
         (impl/sync-event DBCACHE
          ["add" {"Currency" (@! sample/+currency+)}])
         (impl/db-pull-sync DBCACHE
                            sample/Schema
                            ["Currency"
                             ["id"]])))
   (!.lua
    (impl/sync-event DBCACHE
                     ["add" {"UserAccount" [sample/RootUser]}])
    (impl/db-pull-sync DBCACHE
                       sample/Schema
                       ["UserAccount"
                        ["*/data"
                         ["profile"]]]))]
  => (contains [+countries+]))

^{:refer xt.db/db-delete-sync :added "4.0"}
(fact "deletes rows from the db")

^{:refer xt.db/db-clear :added "4.0"}
(fact "clears the db")

^{:refer xt.db/add-view-trigger :added "4.0"}
(fact "adds a view trigger to the db")

(comment
  ^{:refer xt.db/db-exec-sync :added "4.0"}
(fact "runs a raw statement"

  (!.js
   (impl/db-exec-sync DB "Select 1;"))
  => 1

  (!.lua
   (impl/db-exec-sync DB "Select 1;"))
  => 1))
