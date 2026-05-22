(ns xt.db.walkthrough.guide-01-postgres-runtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.db.helpers.fixture-00-postgres :as fixtures]
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
             [xt.db.node.schema-query :as schema-query]
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
(fact "step 01: prepare an inline query, pull directly from postgres, and return live row data"

  (pg/t:select scratch/Entry)
  => (contains-in
      [{:tags ["guide" "sql"],
        :name "alpha",
        :time-updated nil,
        :time-created nil,
        :id string?}
       {:tags ["guide"],
        :name "beta",
        :time-updated nil,
        :time-created nil,
        :id string?}])
  
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
    ;; Mutate the seeded row through SQL so the walkthrough proves the pull
    ;; reads live database state rather than fixture literals.
    (db-instance/db-exec-sync
     db
     "UPDATE \"scratch\".\"Entry\" SET \"tags\" = '[\"live\",\"db\"]'::jsonb WHERE \"name\" = 'alpha';")
    (var [ok prepared]
         (schema-query/prepare-query
          db-state
          (@! fixtures/+inline-query+)
          {"args" []}))
    (var value
         (db-instance/db-pull-sync
          db
          (@! fixtures/+schema+)
          (xt/x:get-key prepared "plan")))
    (sql/disconnect conn)
    {"ok" ok
     "value" value})
  => {"ok" true
      "value" [{"name" "alpha"
                "tags" ["live" "db"]}]})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.02-model-view :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 02: bind a model view and refresh the full entry pull locally"

  (let [result (!.py
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
                 (instance-query/refresh-view-local db-state "entries" "entries" nil)
                 (sql/disconnect conn)
                 {"status" (xtd/get-in db-state ["models" "entries" "views" "entries" "status"])
                  "query-key?" (xt/x:is-string? (xtd/get-in db-state ["models" "entries" "views" "entries" "query_key"]))
                  "value" (xtd/get-in db-state ["models" "entries" "views" "entries" "value"])})]
    {"status" (get result "status")
     "query-key?" (get result "query-key?")
     "value" (mapv (fn [row]
                     {:id (get row "id")
                      :name (get row "name")
                      :tags (get row "tags")
                      :time-created (get row "time_created")
                      :time-updated (get row "time_updated")})
                   (get result "value"))})
  => (contains-in
      {"status" "ready"
       "query-key?" true
       "value" (contains-in
                [{:tags ["guide" "sql"],
                  :name "alpha",
                  :time-updated nil,
                  :time-created nil,
                  :id string?}
                 {:tags ["guide"],
                  :name "beta",
                  :time-updated nil,
                  :time-created nil,
                  :id string?}])}))
