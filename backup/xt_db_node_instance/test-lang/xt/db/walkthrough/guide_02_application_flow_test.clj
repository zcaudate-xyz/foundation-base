(ns xt.db.walkthrough.guide-02-application-flow-test
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
   :require [[xt.db.runtime :as db-instance]
             [xt.db.node.instance-model :as model]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-util :as sql-util]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [js.lib.driver-postgres :as js-pg]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-02-application-flow/STEP.00-spaces :added "4.1"}
(fact "step 00: model an application as a local client space reading from a remote server space"

  (!.js
    (var node (event-node/node-create {"id" "task-app"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (model/install node install-opts)
    (model/model-put node
                     "room/server"
                     "entries"
                     (@! fixtures/+model-spec+))
    (model/model-put node
                     "room/local"
                     "entries"
                     {"views"
                      {"entries"
                       {"query" (@! fixtures/+model-query+)
                        "input" []
                        "remote" {"space" "room/server"}}}})
    {"spaces" (xt/x:obj-keys (. node ["spaces"]))
     "server-model" (. (model/model-get node "room/server" "entries") ["id"])
     "local-remote-space" (xtd/get-in (model/view-get node "room/local" "entries" "entries")
                                      ["remote" "space"])})
  => {"spaces" ["room/server" "room/local"]
      "server-model" "entries"
      "local-remote-space" "room/server"})

^{:refer xt.db.walkthrough.guide-02-application-flow/STEP.01-remote-refresh :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 01: pull live postgres rows in :js, mirror them into the server space, and refresh the client-facing cell through substrate"

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
    (var node (event-node/node-create {"id" "task-app"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var local-db-opts (sql-util/sqlite-opts nil))
    (model/install node install-opts)
    (model/model-put node "room/server" "entries" (@! fixtures/+model-spec+))
    (model/model-put node
                     "room/local"
                     "entries"
                     {"views"
                      {"entries"
                       {"query" (@! fixtures/+model-query+)
                        "input" []
                        "remote" {"space" "room/server"}}}})
    (-> (sql/connect (js-sqlite/driver) {})
        (promise/x:promise-then
         (fn [local-conn]
           (var local-db (db-instance/db-create
                          {"::" "db.sql"
                           :instance local-conn}
                          (@! fixtures/+schema+)
                          (@! fixtures/+lookup+)
                          local-db-opts))
           (xt/x:set-key (model/ensure-space-state node "room/local") "db" local-db)
           (db-instance/db-exec-sync
            local-db
            (xt/x:str-join
             "\n\n"
             (manage/table-create-all
              (@! fixtures/+schema+)
              (@! fixtures/+lookup+)
              local-db-opts)))
           (return
            (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
                (promise/x:promise-then
                 (fn [pg-conn]
                   (return
                    (-> (sql/query
                         pg-conn
                         "SELECT \"id\", \"name\", \"tags\", \"time_created\", \"time_updated\", \"__deleted__\" FROM \"scratch\".\"Entry\" WHERE \"__deleted__\" = false ORDER BY \"name\";")
                        (promise/x:promise-then
                         (fn [rows]
                           (return
                            (model/sync node "room/server" {"db/sync" {"Entry" rows}}))))
                        (promise/x:promise-then
                         (fn [_]
                           (return
                            (model/view-refresh node "room/local" "entries" "entries"))))
                        (promise/x:promise-then
                         (fn [result]
                           (repl/notify
                            {"status" "ready"
                             "local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                             "query-key?" (xt/x:is-string? (. result ["query_key"]))
                             "count" (xt/x:len (. result ["value"]))
                             "first-name" (xtd/get-in (. result ["value"]) [0 "name"])})))))))))))))
  => {"status" "ready"
      "local-dbtype" "db.sql"
      "query-key?" true
      "count" 2
      "first-name" "alpha"})

^{:refer xt.db.walkthrough.guide-02-application-flow/STEP.02-live-update :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 02: mutate postgres in :js, mirror the remote rows again, and re-refresh the client cell to read live server state"

  (notify/wait-on [:js 10000]
    (var node (event-node/node-create {"id" "task-app"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var local-db-opts (sql-util/sqlite-opts nil))
    (model/install node install-opts)
    (model/model-put node "room/server" "entries" (@! fixtures/+model-spec+))
    (model/model-put node
                     "room/local"
                     "entries"
                     {"views"
                      {"entries"
                       {"query" (@! fixtures/+model-query+)
                        "input" []
                        "remote" {"space" "room/server"}}}})
    (-> (sql/connect (js-sqlite/driver) {})
        (promise/x:promise-then
         (fn [local-conn]
           (var local-db (db-instance/db-create
                          {"::" "db.sql"
                           :instance local-conn}
                          (@! fixtures/+schema+)
                          (@! fixtures/+lookup+)
                          local-db-opts))
           (xt/x:set-key (model/ensure-space-state node "room/local") "db" local-db)
           (db-instance/db-exec-sync
            local-db
            (xt/x:str-join
             "\n\n"
             (manage/table-create-all
              (@! fixtures/+schema+)
              (@! fixtures/+lookup+)
              local-db-opts)))
           (return
            (-> (sql/connect (js-pg/driver) (@! fixtures/+scratch-env+))
                (promise/x:promise-then
                 (fn [pg-conn]
                   (return
                    (-> (sql/query
                         pg-conn
                         "SELECT \"id\", \"name\", \"tags\", \"time_created\", \"time_updated\", \"__deleted__\" FROM \"scratch\".\"Entry\" WHERE \"__deleted__\" = false ORDER BY \"name\";")
                        (promise/x:promise-then
                         (fn [rows]
                           (return
                            (model/sync node "room/server" {"db/sync" {"Entry" rows}}))))
                        (promise/x:promise-then
                         (fn [_]
                           (return
                            (model/view-refresh node "room/local" "entries" "entries"))))
                        (promise/x:promise-then
                         (fn [_]
                           (return
                            (sql/query
                             pg-conn
                             "UPDATE \"scratch\".\"Entry\" SET \"tags\" = '[\"app\",\"live\"]'::jsonb WHERE \"name\" = 'alpha';"))))
                        (promise/x:promise-then
                         (fn [_]
                           (return
                            (sql/query
                             pg-conn
                             "SELECT \"id\", \"name\", \"tags\", \"time_created\", \"time_updated\", \"__deleted__\" FROM \"scratch\".\"Entry\" WHERE \"__deleted__\" = false ORDER BY \"name\";"))))
                        (promise/x:promise-then
                         (fn [rows]
                           (return
                            (model/sync node "room/server" {"db/sync" {"Entry" rows}}))))
                        (promise/x:promise-then
                         (fn [_]
                           (return
                            (model/view-refresh node "room/local" "entries" "entries"))))
                        (promise/x:promise-then
                         (fn [_]
                           (repl/notify
                            {"status" "ready"
                             "local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                             "first-tags" (xtd/get-in
                                           (model/view-val node "room/local" "entries" "entries")
                                           [0 "tags"])
                             "remote-space" (xtd/get-in
                                             (model/view-get node "room/local" "entries" "entries")
                                             ["remote" "space"])})))))))))))))
  => {"status" "ready"
      "local-dbtype" "db.sql"
      "first-tags" ["app" "live"]
      "remote-space" "room/server"})
