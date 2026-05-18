(ns xt.db.walkthrough.guide-001-postgres-runtime-test
  (:require [hara.lang :as l]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch])
  (:use code.test))

(def +scratch-env+
  {"host" "127.0.0.1"
   "port" "5432"
   "user" "postgres"
   "password" "postgres"
   "database" "test-scratch"})

(def +schema+
  {"Entry"
   {"id" {"ident" "id" "type" "text" "order" 0 "primary" true}
    "name" {"ident" "name" "type" "text" "order" 1}
    "tags" {"ident" "tags" "type" "array" "order" 2}
    "time_created" {"ident" "time_created" "type" "long" "order" 3}
    "time_updated" {"ident" "time_updated" "type" "long" "order" 4}
    "op_created" {"ident" "op_created" "type" "text" "order" 5}
    "op_updated" {"ident" "op_updated" "type" "text" "order" 6}
    "__deleted__" {"ident" "__deleted__" "type" "boolean" "order" 7}}})

(def +lookup+
  {"Entry" {"position" 0
            "schema" "scratch"}})

(def +inline-query+
  {:table "Entry"
   :select-entry {"input" [{"symbol" "i_name" "type" "text"}]
                  "view" {"query" {"name" "{{i_name}}"
                                   "__deleted__" false}}}
   :select-args ["alpha"]
   :return-entry {"input" [{"symbol" "i_entry_id" "type" "text"}]
                  "view" {"query" ["name" "tags"]}}})

(def +model-spec+
  {"views"
   {"alpha"
    {"query" +inline-query+
     "input" ["alpha"]}}})

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
             [xt.lang.spec-promise :as promise]
             [python.lib.driver-postgres :as py-pg]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.00-connect :added "4.1"}
(fact "step 00: connect xt.db.runtime to the scratch postgres scaffold"

  (!.py
    (var raw (py-pg/connect-constructor (@! +scratch-env+)))
    (var conn
         (sql/connection-create
          raw
          {"disconnect" (fn [inner]
                          (. inner (close))
                          (return true))
           "query" (fn [inner query]
                     (var out (py-pg/raw-query inner query))
                     (return
                      (promise/x:promise-run
                       (:? (or (xt/x:nil? out)
                               (xt/x:is-string? out))
                           out
                           (xt/x:json-encode out)))))
           "query_sync" (fn [inner query]
                          (var out (py-pg/raw-query inner query))
                          (return
                           (:? (or (xt/x:nil? out)
                                   (xt/x:is-string? out))
                               out
                               (xt/x:json-encode out))))}))
    (var db-opts (sql-util/postgres-opts (@! +lookup+)))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! +schema+)
             (@! +lookup+)
             db-opts))
    (var ping (db-instance/db-exec-sync db "SELECT \"scratch\".ping();"))
    (sql/disconnect conn)
    {"dbtype" (xt/x:get-key db "::")
     "ping" ping})
  => {"dbtype" "db.sql"
      "ping" "pong"})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.01-query :added "4.1"
  :setup [(pg/t:delete scratch/Entry)
          (scratch/insert-entry "alpha" ^:js ["guide" "sql"] {})
          (scratch/insert-entry "beta" ^:js ["guide"] {})]}
(fact "step 01: prepare and run inline select and return entries against postgres"

  (!.py
    (var raw (py-pg/connect-constructor (@! +scratch-env+)))
    (var conn
         (sql/connection-create
          raw
          {"disconnect" (fn [inner]
                          (. inner (close))
                          (return true))
           "query" (fn [inner query]
                     (var out (py-pg/raw-query inner query))
                     (return
                      (promise/x:promise-run
                       (:? (or (xt/x:nil? out)
                               (xt/x:is-string? out))
                           out
                           (xt/x:json-encode out)))))
           "query_sync" (fn [inner query]
                          (var out (py-pg/raw-query inner query))
                          (return
                           (:? (or (xt/x:nil? out)
                                   (xt/x:is-string? out))
                               out
                               (xt/x:json-encode out))))}))
    (var db-opts (sql-util/postgres-opts (@! +lookup+)))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! +schema+)
             (@! +lookup+)
             db-opts))
    (var db-state (schema-state/base-state
                   {"schema" (@! +schema+)
                    "lookup" (@! +lookup+)
                    "views" {}
                    "db" db
                    "db_opts" db-opts}))
    (var [ok result]
         (instance-query/run-local-query
          db-state
          (@! +inline-query+)
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
  :setup [(pg/t:delete scratch/Entry)
          (scratch/insert-entry "alpha" ^:js ["guide" "sql"] {})
          (scratch/insert-entry "beta" ^:js ["guide"] {})]}
(fact "step 02: bind the same inline query to a model view and refresh it locally"

  (!.py
    (var raw (py-pg/connect-constructor (@! +scratch-env+)))
    (var conn
         (sql/connection-create
          raw
          {"disconnect" (fn [inner]
                          (. inner (close))
                          (return true))
           "query" (fn [inner query]
                     (var out (py-pg/raw-query inner query))
                     (return
                      (promise/x:promise-run
                       (:? (or (xt/x:nil? out)
                               (xt/x:is-string? out))
                           out
                           (xt/x:json-encode out)))))
           "query_sync" (fn [inner query]
                          (var out (py-pg/raw-query inner query))
                          (return
                           (:? (or (xt/x:nil? out)
                                   (xt/x:is-string? out))
                               out
                               (xt/x:json-encode out))))}))
    (var db-opts (sql-util/postgres-opts (@! +lookup+)))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! +schema+)
             (@! +lookup+)
             db-opts))
    (var db-state (schema-state/base-state
                   {"schema" (@! +schema+)
                    "lookup" (@! +lookup+)
                    "views" {}
                    "db" db
                    "db_opts" db-opts}))
    (instance-state/put-model db-state "entries" (@! +model-spec+))
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
