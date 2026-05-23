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
(l/script- :python
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
             [python.lib.driver-postgres :as py-pg]
             [python.lib.driver-sqlite :as py-sqlite]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-02-application-flow/STEP.00-spaces :added "4.1"}
(fact "step 00: model an application as a local client space reading from a remote server space"

  (!.py
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
(fact "step 01: bind the server space to postgres and refresh the client-facing cell through substrate"

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

  (notify/wait-on :python
    (var node (event-node/node-create {"id" "task-app"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var local-conn (py-sqlite/wrap-connection
                     (py-sqlite/connect-constructor {})))
    (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var local-db-opts (sql-util/sqlite-opts nil))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             db-opts))
    (var local-db (db-instance/db-create
                   {"::" "db.sql"
                    :instance local-conn}
                   (@! fixtures/+schema+)
                   (@! fixtures/+lookup+)
                   local-db-opts))
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
    (xt/x:set-key (model/ensure-space-state node "room/server")
                  "db"
                  db)
    (xt/x:set-key (model/ensure-space-state node "room/local")
                 "db"
                 local-db)
    (sql/query-sync
     local-conn
     (xt/x:str-join
      "\n\n"
      (manage/table-create-all
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       local-db-opts)))
    (promise/x:promise-catch
     (-> (model/view-refresh node "room/local" "entries" "entries")
        (promise/x:promise-then
         (fn [result]
           (var out {"status" (xtd/get-in (model/view-get node "room/local" "entries" "entries")
                                          ["status"])
                     "local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                     "query-key?" (xt/x:is-string? (. result ["query_key"]))
                     "count" (xt/x:len (. result ["value"]))
                     "first-name" (xtd/get-in (. result ["value"]) [0 "name"])})
           (sql/disconnect conn)
           (sql/disconnect local-conn)
           (repl/notify out))))
     (fn [err]
       (sql/disconnect conn)
       (sql/disconnect local-conn)
       (repl/notify err))))
  => {"status" "ready"
      "local-dbtype" "db.sql"
      "query-key?" true
      "count" 2
      "first-name" "alpha"})

^{:refer xt.db.walkthrough.guide-02-application-flow/STEP.02-live-update :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 02: mutate postgres directly and re-refresh the client cell to read live server state"

  (notify/wait-on :python
    (var node (event-node/node-create {"id" "task-app"}))
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var conn (py-pg/wrap-connection
               (py-pg/connect-constructor (@! fixtures/+scratch-env+))))
    (var local-conn (py-sqlite/wrap-connection
                     (py-sqlite/connect-constructor {})))
    (var db-opts (sql-util/postgres-opts (@! fixtures/+lookup+)))
    (var local-db-opts (sql-util/sqlite-opts nil))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             db-opts))
    (var local-db (db-instance/db-create
                   {"::" "db.sql"
                    :instance local-conn}
                   (@! fixtures/+schema+)
                   (@! fixtures/+lookup+)
                   local-db-opts))
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
    (xt/x:set-key (model/ensure-space-state node "room/server")
                  "db"
                  db)
    (xt/x:set-key (model/ensure-space-state node "room/local")
                  "db"
                  local-db)
    (sql/query-sync
     local-conn
     (xt/x:str-join
      "\n\n"
      (manage/table-create-all
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       local-db-opts)))
    (promise/x:promise-catch
     (-> (model/view-refresh node "room/local" "entries" "entries")
         (promise/x:promise-then
          (fn [_]
            (db-instance/db-exec-sync
             db
             "UPDATE \"scratch\".\"Entry\" SET \"tags\" = '[\"app\",\"live\"]'::jsonb;")
            (return
             (model/view-refresh node "room/local" "entries" "entries"))))
         (promise/x:promise-then
          (fn [_]
            (var out {"status" (xtd/get-in (model/view-get node "room/local" "entries" "entries")
                                           ["status"])
                     "local-dbtype" (. (model/ensure-space-state node "room/local") ["db"] ["::"])
                     "first-tags" (xtd/get-in (model/view-val node "room/local" "entries" "entries")
                                              [0 "tags"])
                     "remote-space" (xtd/get-in (model/view-get node "room/local" "entries" "entries")
                                                ["remote" "space"])})
           (sql/disconnect conn)
           (sql/disconnect local-conn)
           (repl/notify out))))
     (fn [err]
       (sql/disconnect conn)
       (sql/disconnect local-conn)
       (repl/notify err))))
  => {"status" "ready"
      "local-dbtype" "db.sql"
      "first-tags" ["app" "live"]
      "remote-space" "room/server"})
