(ns xt.db.walkthrough.guide-01-postgres-runtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]
             [postgres.sample.scratch-v1 :as scratch]]})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.schema-query :as schema-query]
             [xt.db.node.schema-state :as schema-state]
             [xt.db.runtime :as db-instance]
             [xt.db.runtime.sql :as impl-sql]
             [xt.db.text.sql-util :as sql-util]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [js.lib.driver-postgres :as js-pg]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.00-connect :added "4.1"}
(fact "step 00: connect xt.db.runtime to the scratch postgres scaffold"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (promise/x:promise-then
         (fn [conn]
           (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
           (var db (db-instance/db-create
                    {"::" "db.sql"
                     :instance conn}
                    (@! fixtures/+schema+)
                    (@! fixtures/+lookup+)
                    db-opts))
           (return
            (-> (sql/query conn "SELECT \"scratch\".ping();")
                (promise/x:promise-then
                 (fn [ping]
                   (repl/notify
                    {"dbtype" (xt/x:get-key db "::")
                     "ping" ping})))))))))
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

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (promise/x:promise-then
         (fn [conn]
           (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
           (var db-state (schema-state/base-state
                          {"schema" (@! fixtures/+schema+)
                           "lookup" (@! fixtures/+lookup+)
                           "views" {}}))
           (var [ok prepared]
                (schema-query/prepare-query
                 db-state
                 (@! fixtures/+inline-query+)
                 {"args" []}))
           (return
            (-> (sql/query
                 conn
                 "UPDATE \"scratch\".\"Entry\" SET \"tags\" = '[\"live\",\"db\"]'::jsonb WHERE \"name\" = 'alpha';")
                (promise/x:promise-then
                 (fn [_]
                   (return
                    (impl-sql/sql-pull
                     conn
                     (@! fixtures/+schema+)
                     (xt/x:get-key prepared "plan")
                     db-opts))))
                (promise/x:promise-then
                 (fn [value]
                   (repl/notify
                    {"ok" ok
                     "value" value})))))))))
  => {"ok" true
      "value" [{"name" "alpha"
                "tags" ["live" "db"]}]})

^{:refer xt.db.walkthrough.guide-001-postgres-runtime/STEP.02-model-view :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 02: bind a model view and refresh the full entry pull locally after materializing live postgres rows"

  (notify/wait-on [:js 10000]
    (var node (event-node/node-create {"id" "pg-runtime"}))
    (model/install node {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "views" {}})
    (model/model-put node "room/live" "entries" (@! fixtures/+model-spec+))
    (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
        (promise/x:promise-then
         (fn [conn]
           (return
            (-> (sql/query
                 conn
                 "SELECT \"id\", \"name\", \"tags\", \"time_created\", \"time_updated\", \"__deleted__\" FROM \"scratch\".\"Entry\" WHERE \"__deleted__\" = false ORDER BY \"name\";")
                (promise/x:promise-then
                 (fn [rows]
                   (return
                    (model/sync node "room/live" {"db/sync" {"Entry" rows}}))))
                (promise/x:promise-then
                 (fn [_]
                   (return
                    (model/view-refresh node "room/live" "entries" "entries"))))
                (promise/x:promise-then
                 (fn [_]
                   (repl/notify
                    {"status" (xtd/get-in
                               (model/view-get node "room/live" "entries" "entries")
                               ["status"])
                     "query-key?" (xt/x:is-string?
                                   (xtd/get-in
                                    (model/view-get node "room/live" "entries" "entries")
                                    ["query_key"]))
                     "value" (xtd/get-in
                              (model/view-get node "room/live" "entries" "entries")
                              ["value"])})))))))))
  => (contains-in
      {"status" "ready"
       "query-key?" true
       "value" [{"id" string?
                 "name" "alpha"
                 "tags" ["guide" "sql"]
                 "time_created" nil
                 "time_updated" nil}
                {"id" string?
                 "name" "beta"
                 "tags" ["guide"]
                 "time_created" nil
                 "time_updated" nil}]}))
