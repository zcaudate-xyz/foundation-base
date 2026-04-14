(ns xt.db-lua.impl-select-sql-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.db.base-schema :as sch]
             [xt.lang.common-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]
             [xt.sys.conn-dbsql :as dbsql]
             [lua.nginx.driver-postgres :as lua-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db-lua.impl-select-sql-test/CONNECTION :adopt true :added "4.0"}
(fact "CONNECTED"

  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (dbsql/query conn "SELECT 1;"))
  => 1)

^{:refer xt.db-lua.impl-select-sql-test/QUERY :adopt true :added "4.0"}
(fact "runs select queries"
  ^:hidden

  (!.lua
   (graph/select sample-scratch/Schema
                 ["Entry"
                  ["name"
                   (ut/LIMIT 1)]]
                 (ut/postgres-opts sample-scratch/SchemaLookup)))
  => string?)
