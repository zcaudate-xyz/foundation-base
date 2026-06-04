(ns xt.db.system.client-sql-variant-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.protocol.impl.graphdb :as graphdb]
             [xt.db.system.client-memory :as memory]
             [xt.db.system.base-sql :as sql-base]
             [xt.db.system.client-postgres :as postgres]
             [xt.db.system.client-sqlite :as sqlite]
             [xt.db.system.client-supabase :as supabase]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.client-postgres/client :added "4.1"}
(fact "all xt.db.system clients expose the expected graphdb runtime/driver surface"

  (!.js
   (var mem (memory/client {"rows" {}}))
   (var sql (sql-base/client {"instance" {"id" "sql-1"}
                              "schema" {"UserAccount" {"primary" ["id"]}}
                              "lookup" {"UserAccount" {"position" 0}}
                              "host" "db.sql.test"
                              "port" 5432
                              "opts" {"dialect" "postgres"}}))
   (var pg  (postgres/client {"instance" {"id" "pg-1"}
                              "schema" {"UserAccount" {"primary" ["id"]}}
                              "lookup" {"UserAccount" {"position" 0}}
                              "host" "db.pg.test"
                              "port" 5432}))
   (var sq  (sqlite/client {"instance" {"id" "sq-1"}
                            "schema" {"UserAccount" {"primary" ["id"]}}
                            "lookup" {"UserAccount" {"position" 0}}
                            "filename" "local.sqlite"}))
   (var sb  (supabase/client {"base_url" "https://db.test"}))
   {"drivers" {"memory" [(graphdb/driver? memory/DRIVER)
                         (xt/x:not-nil? (graphdb/driver-op memory/DRIVER "add"))
                         (xt/x:not-nil? (graphdb/driver-op memory/DRIVER "remove"))
                         (xt/x:not-nil? (graphdb/driver-op memory/DRIVER "delete_sync"))]
               "sql" [(graphdb/driver? sql-base/DRIVER)
                      (xt/x:not-nil? (graphdb/driver-op sql-base/DRIVER "add"))
                      (xt/x:not-nil? (graphdb/driver-op sql-base/DRIVER "remove"))
                      (xt/x:not-nil? (graphdb/driver-op sql-base/DRIVER "delete_sync"))
                      (xt/x:not-nil? (graphdb/driver-op sql-base/DRIVER "exec_sync"))]
               "postgres" [(graphdb/driver? postgres/DRIVER)
                           (xt/x:not-nil? (graphdb/driver-op postgres/DRIVER "add"))
                           (xt/x:not-nil? (graphdb/driver-op postgres/DRIVER "remove"))
                           (xt/x:not-nil? (graphdb/driver-op postgres/DRIVER "delete_sync"))
                           (xt/x:not-nil? (graphdb/driver-op postgres/DRIVER "exec_sync"))]
               "sqlite" [(graphdb/driver? sqlite/DRIVER)
                         (xt/x:not-nil? (graphdb/driver-op sqlite/DRIVER "add"))
                         (xt/x:not-nil? (graphdb/driver-op sqlite/DRIVER "remove"))
                         (xt/x:not-nil? (graphdb/driver-op sqlite/DRIVER "delete_sync"))
                         (xt/x:not-nil? (graphdb/driver-op sqlite/DRIVER "exec_sync"))]
               "supabase" [(graphdb/driver? supabase/DRIVER)
                           (graphdb/driver-op supabase/DRIVER "add")
                           (graphdb/driver-op supabase/DRIVER "remove")
                           (graphdb/driver-op supabase/DRIVER "delete_sync")
                           (graphdb/driver-op supabase/DRIVER "exec_sync")]}
    "clients" {"memory" [(memory/client? mem)
                         (graphdb/db? mem)
                         (xt/x:not-nil? (xt/proto:method mem "pull"))
                         (xt/x:not-nil? (xt/proto:method mem "pull_sync"))
                         (xt/x:not-nil? (xt/proto:method mem "record_add"))
                         (xt/x:not-nil? (xt/proto:method mem "record_delete"))]
               "sql" [(sql-base/client? sql)
                      (graphdb/db? sql)
                      (xtd/get-in sql ["schema" "UserAccount" "primary" 0])
                      (xtd/get-in sql ["lookup" "UserAccount" "position"])
                      (xtd/get-in sql ["opts" "dialect"])
                      (xtd/get-in sql ["settings" "host"])
                      (xtd/get-in sql ["settings" "port"])
                      (xt/x:not-nil? (xt/proto:method sql "pull"))
                      (xt/x:not-nil? (xt/proto:method sql "pull_sync"))
                      (xt/x:not-nil? (xt/proto:method sql "record_add"))
                      (xt/x:not-nil? (xt/proto:method sql "record_delete"))]
               "postgres" [(postgres/client? pg)
                           (graphdb/db? pg)
                           (. pg ["::"])
                           (xtd/get-in pg ["schema" "UserAccount" "primary" 0])
                           (xtd/get-in pg ["lookup" "UserAccount" "position"])
                           (xtd/get-in pg ["settings" "host"])
                           (xtd/get-in pg ["settings" "port"])
                           (xt/x:not-nil? (xt/proto:method pg "pull"))
                           (xt/x:not-nil? (xt/proto:method pg "pull_sync"))
                           (xt/x:not-nil? (xt/proto:method pg "record_add"))
                           (xt/x:not-nil? (xt/proto:method pg "record_delete"))]
               "sqlite" [(sqlite/client? sq)
                         (graphdb/db? sq)
                         (. sq ["::"])
                         (xtd/get-in sq ["schema" "UserAccount" "primary" 0])
                         (xtd/get-in sq ["lookup" "UserAccount" "position"])
                         (xtd/get-in sq ["settings" "filename"])
                         (xt/x:not-nil? (xt/proto:method sq "pull"))
                         (xt/x:not-nil? (xt/proto:method sq "pull_sync"))
                         (xt/x:not-nil? (xt/proto:method sq "record_add"))
                         (xt/x:not-nil? (xt/proto:method sq "record_delete"))]
               "supabase" [(supabase/client? sb)
                           (graphdb/db? sb)
                           (. sb ["::"])
                           (xt/x:not-nil? (xt/proto:method sb "pull"))
                           (xt/proto:method sb "pull_sync")
                           (xt/proto:method sb "record_add")
                           (xt/proto:method sb "record_delete")]}})
  => {"drivers" {"memory" [true true true true]
                "sql" [true true true true true]
                "postgres" [true true true true true]
                "sqlite" [true true true true true]
                "supabase" [true nil nil nil nil]}
      "clients" {"memory" [true true true true true true]
                 "sql" [true true "id" 0 "postgres" "db.sql.test" 5432 true true true true]
                 "postgres" [true true "db.client.postgres" "id" 0 "db.pg.test" 5432 true true true true]
                 "sqlite" [true true "db.client.sqlite" "id" 0 "local.sqlite" true true true true]
                 "supabase" [true true "db.client.supabase" true nil nil nil]}})
