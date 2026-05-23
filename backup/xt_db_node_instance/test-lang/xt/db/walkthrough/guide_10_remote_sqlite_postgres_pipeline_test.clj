(ns xt.db.walkthrough.guide-10-remote-sqlite-postgres-pipeline-test
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

^{:refer xt.db.walkthrough.guide-10-remote-sqlite-postgres-pipeline/STEP.00-topology :added "4.1"}
(fact "step 00: attach sqlite to room/local and postgres to room/server so remote-backed views can query across spaces"

  (notify/wait-on [:js 10000]
    (var node (event-node/node-create {"id" "admin-screen"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var local-db-opts (sql-util/sqlite-opts nil))
    (var server-db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var detail-query {:table "Entry"
                       :select-entry {"input" [{"symbol" "i_name" "type" "text"}]
                                      "view" {"query" {"name" "{{i_name}}"
                                                       "__deleted__" false}}}
                       :return-entry {"input" [{"symbol" "i_entry_id" "type" "text"}]
                                      "view" {"query" ["name" "tags"]}}})
    (var server-spec {"views"
                      {"list"
                       {"query" (@! fixtures/+model-query+)
                        "input" []}
                       "detail"
                       {"query" detail-query
                        "default_input" ["alpha"]}}})
    (var local-spec {"views"
                     {"list"
                      {"query" (@! fixtures/+model-query+)
                       "input" []
                       "remote" {"space" "room/server"}}
                      "detail"
                      {"query" detail-query
                       "default_input" ["alpha"]
                       "remote" {"space" "room/server"}}}})
    (model/install node install-opts)
    (model/model-put node "room/server" "entries-screen" server-spec)
    (model/model-put node "room/local" "entries-screen" local-spec)
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
                   (var server-db (db-instance/db-create
                                   {"::" "db.sql"
                                    :instance pg-conn}
                                   (@! fixtures/+schema+)
                                   (@! fixtures/+lookup+)
                                   server-db-opts))
                   (xt/x:set-key (model/ensure-space-state node "room/server") "db" server-db)
                   (repl/notify
                    {"spaces" (xt/x:obj-keys (. node ["spaces"]))
                     "local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                     "server-dbtype" (. (model/ensure-space-state node "room/server") ["db"] ["::"])
                     "remote-space" (xtd/get-in
                                     (model/view-get node "room/local" "entries-screen" "list")
                                     ["remote" "space"])})))))))))
  => {"spaces" ["room/server" "room/local"]
      "local-dbtype" "db.sql"
      "server-dbtype" "db.sql"
      "remote-space" "room/server"})

^{:refer xt.db.walkthrough.guide-10-remote-sqlite-postgres-pipeline/STEP.01-remote-pipeline-refresh :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 01: refresh the local sqlite-backed screen so the remote pipeline queries the postgres-backed server space directly"

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
    (var node (event-node/node-create {"id" "admin-screen"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var local-db-opts (sql-util/sqlite-opts nil))
    (var server-db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var detail-query {:table "Entry"
                       :select-entry {"input" [{"symbol" "i_name" "type" "text"}]
                                      "view" {"query" {"name" "{{i_name}}"
                                                       "__deleted__" false}}}
                       :return-entry {"input" [{"symbol" "i_entry_id" "type" "text"}]
                                      "view" {"query" ["name" "tags"]}}})
    (var server-spec {"views"
                      {"list"
                       {"query" (@! fixtures/+model-query+)
                        "input" []}
                       "detail"
                       {"query" detail-query
                        "default_input" ["alpha"]}}})
    (var local-spec {"views"
                     {"list"
                      {"query" (@! fixtures/+model-query+)
                       "input" []
                       "remote" {"space" "room/server"}}
                      "detail"
                      {"query" detail-query
                       "default_input" ["alpha"]
                       "remote" {"space" "room/server"}}}})
    (model/install node install-opts)
    (model/model-put node "room/server" "entries-screen" server-spec)
    (model/model-put node "room/local" "entries-screen" local-spec)
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
                   (var server-db (db-instance/db-create
                                   {"::" "db.sql"
                                    :instance pg-conn}
                                   (@! fixtures/+schema+)
                                   (@! fixtures/+lookup+)
                                   server-db-opts))
                   (xt/x:set-key (model/ensure-space-state node "room/server") "db" server-db)
                   (return
                    (-> (model/model-refresh node "room/local" "entries-screen")
                        (promise/x:promise-then
                         (fn [refreshes]
                           (repl/notify
                            {"local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                             "server-dbtype" (. (model/ensure-space-state node "room/server") ["db"] ["::"])
                             "remote-space" (xtd/get-in
                                             (model/view-get node "room/local" "entries-screen" "list")
                                             ["remote" "space"])
                             "refresh-count" (xt/x:len refreshes)
                             "list-status" (xtd/get-in
                                            (model/view-get node "room/local" "entries-screen" "list")
                                            ["status"])
                             "list-count" (xt/x:len (model/view-val node "room/local" "entries-screen" "list"))
                             "detail-input" (model/view-input node "room/local" "entries-screen" "detail")
                             "detail-name" (xtd/get-in
                                            (model/view-val node "room/local" "entries-screen" "detail")
                                            [0 "name"])})))))))))))))
  => {"local-dbtype" "db.sql"
      "server-dbtype" "db.sql"
      "remote-space" "room/server"
      "refresh-count" 2
      "list-status" "ready"
      "list-count" 2
      "detail-input" ["alpha"]
      "detail-name" "alpha"})
