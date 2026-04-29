(ns xt.old.db-lua.impl-select-view-test
  (:require [std.lang :as l]
            [std.string.prose :as prose]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.old.db.base-schema :as sch]
             [xt.lang.common-lib :as k]
             [xt.old.db.sql-util :as ut]
             [xt.old.db.sql-graph :as graph]
             [xt.old.db.sql-view :as view]
             [xt.old.db.sql-manage :as manage]
             [xt.old.db.sample-scratch-test :as sample-scratch]
             [xt.old.sys.conn-dbsql :as dbsql]
             [lua.nginx.driver-postgres :as lua-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :lua.nginx)]
  :teardown [(l/rt:stop)]})

^{:refer xt.old.db-lua.impl-select-view-test/CONNECTION :adopt true :added "4.0"}
(fact "CONNECTED"

  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (dbsql/query conn "SELECT 1;"))
  => 1)

^{:refer xt.old.db-lua.impl-select-view-test/VIEW-QUERY :added "4.0"}
(fact "queries views"

  (!.lua
   (view/query-select sample-scratch/Schema
                      {:view {:table "Entry"
                              :type "select"
                              :tag "by-name"
                              :query ["id" "name"]}
                       :input [{:symbol "i-name" :type "text"}]}
                      ["A-1"]
                      {}
                      true))
  => vector?)
