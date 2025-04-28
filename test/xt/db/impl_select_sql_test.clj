(ns xt.db.impl-select-sql-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres.script.scratch :as scratch]
             [rt.postgres :as pg]]})

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.db.base-schema :as sch]
             [xt.lang.base-lib :as k]
             [xt.db.sql-util :as ut]
             [xt.db.sql-graph :as graph]
             [xt.db.sql-util :as ut]
             [xt.db.sql-manage :as manage]
             [xt.db.sample-scratch-test :as sample-scratch]
             [xt.sys.conn-dbsql :as dbsql]
             [lua.nginx.driver-postgres :as lua-postgres]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)
             (l/rt:scaffold :lua)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.impl-select-sql-test/CONNECTION :adopt true :added "4.0"}
(fact "CONNECTED"
  
  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (dbsql/query conn "SELECT 1;"))
  => 1)


^{:refer xt.db.impl-select-sql-test/CONNECTION :adopt true :added "4.0"
  :setup [(pg/t:delete scratch/Entry)
          (doseq [i (range 30)]
            (pg/t:insert scratch/Entry
              {:name (str "A-" i)
               :tags '(js ["A"])}
              {:track {}}))]}
(fact "CONNECTED"

  (!.lua
   (graph/select sample-scratch/Schema
                 ["Entry"
                  ["name"
                   (ut/LIMIT 1)]]
                 (ut/postgres-opts sample-scratch/SchemaLookup)))
  
  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn (graph/select sample-scratch/Schema
                                    ["Entry"
                                     ["name"
                                      (ut/ORDER-BY ["name"])
                                      (ut/LIMIT 5)]]
                                    (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-0"} {"name" "A-1"} {"name" "A-10"} {"name" "A-11"} {"name" "A-12"}]
  
  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn (graph/select sample-scratch/Schema
                                    ["Entry"
                                     ["name"
                                      (ut/ORDER-BY ["name"])
                                      (ut/LIMIT 5)
                                      (ut/OFFSET 5)]]
                                    (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-13"} {"name" "A-14"} {"name" "A-15"} {"name" "A-16"} {"name" "A-17"}]

  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn (graph/select sample-scratch/Schema
                                    ["Entry"
                                     ["name"
                                      (ut/ORDER-BY ["name"])
                                      (ut/ORDER-SORT "desc")
                                      (ut/LIMIT 5)
                                      (ut/OFFSET 5)]]
                                    (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-4"} {"name" "A-3"} {"name" "A-29"} {"name" "A-28"} {"name" "A-27"}]

  (!.lua
   (local conn (lua-postgres/connect-constructor {:database "test-scratch"}))
   (k/json-decode
    (dbsql/query conn (graph/select sample-scratch/Schema
                                    ["Entry"
                                     ["name"
                                      (ut/ORDER-BY ["name"])
                                      (ut/ORDER-SORT "desc")
                                      (ut/LIMIT 5)]]
                                    (ut/postgres-opts sample-scratch/SchemaLookup)))))
  => [{"name" "A-9"} {"name" "A-8"} {"name" "A-7"} {"name" "A-6"} {"name" "A-5"}])
