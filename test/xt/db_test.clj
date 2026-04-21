(ns xt.db-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.db :as impl]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.sys.conn-dbsql :as dbsql]
             [xt.db.base-flatten :as f]
             [xt.db.sql-util :as ut]
             [xt.db.sql-raw :as raw]
             [xt.db.sql-manage :as manage]
             [xt.db.sql-table :as table]
             [xt.db.sample-test :as sample]
             [js.lib.driver-sqlite :as js-sqlite]]})

(defn bootstrap-js
  []
  (notify/wait-on [:js 2000]
    (dbsql/connect {:constructor js-sqlite/connect-constructor}
                   {:success (fn [conn]
                               (try
                                 (:= (!:G DBSQL) (impl/db-create
                                                  {"::" "db.sql"
                                                   :instance conn}
                                                  sample/Schema
                                                  sample/SchemaLookup
                                                  (ut/sqlite-opts nil)))
                                 (dbsql/query-sync (xt/x:get-key DBSQL "instance")
                                                   (str/join "\n\n"
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
                                 (catch e (repl/notify e))))})))

(fact:global
 {:setup    [(l/rt:restart)
             (do (l/rt:scaffold :js)
                 true)
             (bootstrap-js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db/process-event :added "4.0"}
(fact "processes an event"

  (!.js
   [(xtd/arr-sort (impl/process-event
                   DBSQL
                   ["add" {"UserAccount" [sample/RootUser]}]
                   sample/Schema
                   sample/SchemaLookup
                   (ut/sqlite-opts nil))
                  (fn:> [x] x)
                  xt/x:lt)
    (xtd/arr-sort (impl/process-event
                   DBCACHE
                   ["add" {"UserAccount" [sample/RootUser]}]
                   sample/Schema
                   sample/SchemaLookup
                   nil)
                  (fn:> [x] x)
                  xt/x:lt)])
  => [["UserAccount" "UserProfile"]
      ["UserAccount" "UserProfile"]])

^{:refer xt.db/process-triggers :added "4.0"
  :setup [(bootstrap-js)]}
(fact "process triggers"

  [(notify/wait-on :js
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (xt/x:get-key trigger "listen")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on :js
     (impl/remove-trigger DBCACHE "test")
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (xt/x:get-key trigger "listen")))
                                       :listen ["UserProfile"]
                                       :async true})
     (impl/queue-event DBCACHE ["add" {"UserAccount" [sample/RootUser]}]))
   (notify/wait-on [:js 500]
     (impl/remove-trigger DBSQL "test")
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (xt/x:get-key trigger "listen")))
                                     :listen ["HELLO"]
                                     :async true})
     (impl/queue-event DBSQL ["add" {"UserAccount" [sample/RootUser]}]))]
  => [["UserAccount"]
      ["UserProfile"]
      :timeout])

^{:refer xt.db/add-trigger :added "4.0"
  :setup [(bootstrap-js)]}
(fact "adds a trigger to db"

  [(notify/wait-on :js
     (impl/add-trigger DBSQL "test" {:id "test"
                                     :callback (fn [instance trigger]
                                                 (repl/notify (xt/x:get-key trigger "id")))
                                     :listen ["UserAccount"]
                                     :async true})
     (impl/db-trigger DBSQL {"UserAccount" true}))
   (notify/wait-on :js
     (impl/add-trigger DBCACHE "test" {:id "test"
                                       :callback (fn [instance trigger]
                                                   (repl/notify (xt/x:get-key trigger "id")))
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

  (!.js
   (impl/db-exec-sync DBSQL "Select 1;"))
  => 1)

^{:refer xt.db/db-pull-sync :added "4.0"
  :setup [(bootstrap-js)
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
         (impl/sync-event
          DBCACHE
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
  => (contains [+countries+]))

^{:refer xt.db/db-delete-sync :added "4.0"}
(fact "deletes rows from the db")

^{:refer xt.db/db-clear :added "4.0"}
(fact "clears the db")

^{:refer xt.db/add-view-trigger :added "4.0"}
(fact "adds a view trigger to the db")


^{:refer xt.db/get-dbtype :added "4.1"}
(fact "TODO")