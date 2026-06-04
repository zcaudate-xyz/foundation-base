(ns xt.db.system-backends-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [scaffold.supabase.event-host-util :as live]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system :as db-system]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-util :as sql-util]
             [xt.db.text.sql-manage :as sql-manage]
             [js.lib.driver-sqlite :as js-sqlite]
             ^{:seedgen/extra true}
             [js.lib.driver-postgres :as js-pg]
             [js.lib.client-fetch :as js-fetch]
             [xt.db.helpers.test-fixtures :as fixtures]
             [scaffold.supabase.event-host-util :as live]]})

(fact:global
 {:setup [(l/rt:restart)
          (do (live/init-live-postgres-runtime!)
              (l/rt:setup (live/pg-rt) live/+postgres-module+)
              (live/grant-scratch-schema!)
              (live/reload-postgrest!)
              (live/refresh-live-supabase-config!)
              true)]
  :teardown [(do (l/rt:teardown (live/pg-rt) live/+postgres-module+)
                 (alter-var-root #'live/+postgres-runtime+ (constantly nil))
                 (alter-var-root #'live/+live-supabase-config+ (constantly nil))
                 true)
             (l/rt:stop)]})

^{:refer xt.db.system-backends-test/memory-backend :added "4.1"}
(fact "memory backend: create, sync, pull, and clear"

  (!.js
    (var db (db-system/db-create
             {"::" "db.cache"}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             nil))
    (db-system/sync-event
     db
     ["add" (@! fixtures/+entry-seed+)])
    (var rows (db-system/db-pull-sync
               db
               (@! fixtures/+schema+)
               ["Entry" ["id" "name"]]))
    (var count-before (xt/x:len rows))
    (db-system/db-clear db)
    (var rows-after (db-system/db-pull-sync
                     db
                     (@! fixtures/+schema+)
                     ["Entry" ["id" "name"]]))
    (var count-after (xt/x:len rows-after))
    {"count_before" count-before
     "count_after" count-after
     "first_name" (xtd/get-in rows [0 "name"])})
  => {"count_before" 2
      "count_after" 0
      "first_name" "alpha"})

^{:refer xt.db.system-backends-test/sqlite-backend :added "4.1"}
(fact "sqlite backend: connect, create tables, sync, exec, and pull"

  (notify/wait-on [:js 10000]
    (promise/x:promise-then
     (dbsql/connect (js-sqlite/driver) {})
     (fn [conn]
       (var db (db-system/db-create
                {"::" "db.sql"
                 :instance conn}
                (@! fixtures/+schema+)
                (@! fixtures/+lookup+)
                (sql-util/sqlite-opts nil)))
       (dbsql/query
        conn
        (str/join "\n\n"
                  (sql-manage/table-create-all
                   (@! fixtures/+schema+)
                   (@! fixtures/+lookup+)
                   (sql-util/sqlite-opts nil))))
       (db-system/sync-event
        db
        ["add" (@! fixtures/+entry-seed+)])
       (var count (db-system/db-exec-sync
                   db
                   "SELECT count(*) FROM Entry;"))
       (var rows (db-system/db-pull-sync
                  db
                  (@! fixtures/+schema+)
                  ["Entry" ["id" "name"]]))
       (repl/notify
        {"count" count
         "names" [(xtd/get-in rows [0 "name"])
                  (xtd/get-in rows [1 "name"])]}))))
  => {"count" 2
      "names" ["alpha" "beta"]})

^{:refer xt.db.system-backends-test/postgres-backend :added "4.1"}
(fact "postgres backend: connect, create runtime, and query"

  (notify/wait-on [:js 10000]
    (-> (dbsql/connect (js-pg/driver)
                       {"host" (@! live/+postgres-host+)
                        "port" (@! live/+postgres-port+)
                        "user" "postgres"
                        "password" "postgres"
                        "database" (@! live/+postgres-database+)})
        (promise/x:promise-then
         (fn [conn]
           (var db (db-system/db-create
                    {"::" "db.sql"
                     :instance conn}
                    (@! fixtures/+schema+)
                    (@! fixtures/+lookup+)
                    (sql-util/postgres-opts (@! fixtures/+lookup+))))
           (promise/x:promise-then
            (dbsql/query-async conn "SELECT 1 as n;")
            (fn [result]
              (repl/notify
               {"has_db" (xt/x:not-nil? db)
                "dbtype" (xt/x:get-key db "::")
                "query_result" result})))))))
  => {"has_db" true
      "dbtype" "db.sql"
      "query_result" 1})

^{:refer xt.db.system-backends-test/supabase-backend :added "4.1"
  }
(fact "supabase backend: create and query through local postgrest"

  (notify/wait-on [:js 15000]
    (-> (dbsql/connect (js-pg/driver)
                       {"host" (@! live/+postgres-host+)
                        "port" (@! live/+postgres-port+)
                        "user" "postgres"
                        "password" "postgres"
                        "database" (@! live/+postgres-database+)})
        (promise/x:promise-then
         (fn [conn]
           (promise/x:promise-then
            (dbsql/query-async
             conn
             (xt/x:cat
              "INSERT INTO \"scratch\".\"Entry\" (name, tags) "
              "VALUES ('supabase-backend-test', '[\"test\"]'::jsonb) "
              "ON CONFLICT (name) DO UPDATE SET tags = EXCLUDED.tags;"))
            (fn [_]
              (dbsql/disconnect conn)))))
        (promise/x:promise-then
         (fn [_]
           (var live-config (@! live/+live-supabase-config+))
           (var live-client (. live-config ["client"]))
           (var client-config
                {"base_url"    (. live-client ["base_url"])
                 "schema_name" (. live-client ["schema_name"])
                 "api_key"     (. live-client ["api_key"])
                 "auth_token"  (. live-client ["auth_token"])
                 "transport"   (js-fetch/client {})})
           (var db (db-system/db-create
                    {"::" "db.supabase"
                     :instance client-config}
                    (@! fixtures/+schema+)
                    (@! fixtures/+lookup+)
                    nil))
           (var pull-promise (db-system/db-pull
                              db
                              (@! fixtures/+schema+)
                              ["Entry"
                               {"name" "supabase-backend-test"}
                               ["name" "tags"]]))
           (promise/x:promise-then
            pull-promise
            (fn [result]
              (repl/notify
               {"has_db" true
                "dbtype" "db.supabase"
                "name" (xtd/get-in result [0 "name"])
                "tag" (xtd/get-in result [0 "tags" 0])})))))))
  => {"has_db" true
      "dbtype" "db.supabase"
      "name" "supabase-backend-test"
      "tag" "test"})
