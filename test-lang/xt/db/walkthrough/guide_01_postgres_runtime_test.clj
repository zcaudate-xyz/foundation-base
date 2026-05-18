(ns xt.db.walkthrough.guide-01-postgres-runtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.walkthrough.fixture-00-postgres :as fixtures]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]
             [postgres.sample.scratch-v1 :as scratch]]})

^{:seedgen/root {:all true}}
(l/script- :python
  {:runtime :basic
   :require [[xt.db.runtime :as db-instance]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.node.instance-query :as instance-query]
             [xt.db.text.sql-util :as sql-util]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [python.lib.driver-postgres :as py-pg]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.00-connect :added "4.1"}
(fact "step 00: connect xt.db.runtime to the scratch postgres scaffold"

  (!.py
    (var conn (py-pg/wrap-connection
              (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             db-opts))
    (var ping (db-instance/db-exec-sync db "SELECT \"scratch\".ping();"))
    (sql/disconnect conn)
    {"dbtype" (xt/x:get-key db "::")
     "ping" ping})
  => {"dbtype" "db.sql"
      "ping" "pong"})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.01-query :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 01: prepare and run inline select and return entries against postgres"

  (!.py
    (var conn (py-pg/wrap-connection
              (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var db (db-instance/db-create
            {"::" "db.sql"
              :instance conn}
            (@! fixtures/+schema+)
            (@! fixtures/+lookup+)
            db-opts))
    (var db-state (schema-state/base-state
                  {"schema" (@! fixtures/+schema+)
                   "lookup" (@! fixtures/+lookup+)
                    "views" {}
                    "db" db
                    "db_opts" db-opts}))
    (var [ok result]
         (instance-query/run-local-query
          db-state
          (@! fixtures/+inline-query+)
          {"args" []}
          nil
          nil))
    (sql/disconnect conn)
    {"ok" ok
     "query-key?" (xt/x:is-string? (xtd/get-in result ["query_key"]))
     "name" (xtd/get-in result ["value" 0 "name"])
     "tags" (xtd/get-in result ["value" 0 "tags"])
     "tables" (xtd/get-in result ["tables" "Entry"])})
  => {"ok" true
      "query-key?" true
      "name" "alpha"
      "tags" ["guide" "sql"]
      "tables" true})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.02-model-view :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 02: bind the same inline query to a model view and refresh it locally"

  (!.py
    (var conn (py-pg/wrap-connection
              (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             db-opts))
    (var db-state (schema-state/base-state
                  {"schema" (@! fixtures/+schema+)
                   "lookup" (@! fixtures/+lookup+)
                    "views" {}
                    "db" db
                    "db_opts" db-opts}))
    (instance-state/put-model db-state "entries" (@! fixtures/+model-spec+))
    (instance-query/refresh-view-local db-state "entries" "alpha" nil)
    (sql/disconnect conn)
    {"status" (xtd/get-in db-state ["models" "entries" "views" "alpha" "status"])
     "query-key?" (xt/x:is-string? (xtd/get-in db-state ["models" "entries" "views" "alpha" "query_key"]))
     "name" (xtd/get-in db-state ["models" "entries" "views" "alpha" "value" 0 "name"])
     "tags" (xtd/get-in db-state ["models" "entries" "views" "alpha" "value" 0 "tags"])})
  => {"status" "ready"
      "query-key?" true
      "name" "alpha"
      "tags" ["guide" "sql"]})
