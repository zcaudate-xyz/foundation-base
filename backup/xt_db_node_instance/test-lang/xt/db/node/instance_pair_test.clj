(ns xt.db.node.instance-pair-test
  (:require [hara.lang :as l]
            [postgres.sample.scratch-v1 :as scratch]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true
                 :langs [:js]}}
(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v1 :as scratch]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.db.runtime :as xdb]
             [xt.db.runtime.sql :as impl-sql]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.db.text.sql-manage :as manage]
             [xt.db.text.sql-raw :as raw]
             [xt.db.text.sql-util :as ut]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [js.lib.driver-postgres :as js-postgres]
             [js.lib.driver-sqlite :as js-sqlite]]})

(def +pair-task-id+
  "00000000-0000-0000-0000-0000000000a1")

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.instance-model/view-refresh :added "4.1"}
(fact "pairs a local sqlite view with a remote postgres-backed view using helper fixtures"

  (notify/wait-on [:js 10000]
    (do
      (var run-queries nil)
      (:= run-queries
          (fn [conn queries]
            (if (== 0 (xt/x:len queries))
              (return (promise/x:promise-run true))
              (return
               (promise/x:promise-then
                (dbsql/ensure-promise
                 (dbsql/query conn (xt/x:first queries)))
                (fn [_]
                  (return (run-queries conn
                                       (xt/x:arr-slice queries 1 nil)))))))))
      (var node (event-node/node-create {"id" "node-pair"}))
      (var local-state nil)
      (var remote-state nil)
      (var local-opts (ut/sqlite-opts nil))
      (var pg-lookup {"Task" {"position" 0
                              "schema" "public"}})
      (var pg-opts (ut/postgres-opts pg-lookup))
      (var local-create-sql
           (str/join "\n\n"
                     (manage/table-create-all fixtures/Schema
                                              fixtures/Lookup
                                              local-opts)))
      (var remote-queries
           [(str/join "\n\n"
                      (manage/table-create-all fixtures/Schema
                                               pg-lookup
                                               pg-opts))
            (raw/raw-delete "Task" nil pg-opts)
            (impl-sql/sql-process-event-sync nil
                                             "input"
                                             fixtures/Seed
                                             fixtures/Schema
                                             pg-lookup
                                             pg-opts)
            (impl-sql/sql-process-event-sync
             nil
             "input"
             {"Task"
              [{"id" (@! +pair-task-id+)
                "status" "closed"
                "name" "alpha-task"}]}
             fixtures/Schema
             pg-lookup
             pg-opts)])
      (var remote-sync-conn nil)
      (var remote-db nil)
      (var refresh-summary nil)
      (:= refresh-summary
          (fn []
            (return
             (promise/x:promise-then
              (model/view-refresh node "room/local" "local" "main")
              (fn [local-refresh]
                (return
                 (promise/x:promise-then
                  (model/view-refresh node "room/local" "pair" "main")
                  (fn [remote-refresh]
                    (return
                     {"local-status" (xtd/get-in (model/view-val node "room/local" "local" "main")
                                                 [0 "status"])
                      "remote-status" (xtd/get-in (model/view-val node "room/local" "pair" "main")
                                                  [0 "status"])
                      "local-query?" (xt/x:is-string? (xtd/get-in local-refresh ["query_key"]))
                      "remote-query?" (xt/x:is-string? (xtd/get-in remote-refresh ["query_key"]))
                      "local-query-count" (xt/x:len (xt/x:obj-keys (. local-state ["queries"])))
                      "remote-query-count" (xt/x:len (xt/x:obj-keys (. remote-state ["queries"])))})))))))))
      (model/install node fixtures/InstallOpts)
      (model/model-put node "room/local" "local" fixtures/ModelSpec)
      (model/model-put node "room/local" "pair"
                       {"views"
                        {"main"
                         {"query" {:table "Task"
                                   :return-method "default"
                                   :return-id (@! +pair-task-id+)}
                          "input" []
                          "remote" {"space" "room/remote"}}}})
      (model/model-put node "room/remote" "pair" fixtures/ModelSpec)
      (:= local-state (model/ensure-space-state node "room/local"))
      (:= remote-state (model/ensure-space-state node "room/remote"))
      (return
       (promise/x:promise-then
        (dbsql/connect (js-sqlite/driver) {})
        (fn [local-conn]
          (var local-db
               (xdb/db-create {"::" "db.sql"
                               :instance local-conn}
                              fixtures/Schema
                              fixtures/Lookup
                              local-opts))
          (xt/x:set-key local-state "db" local-db)
          (xdb/db-exec-sync local-db local-create-sql)
          (xdb/sync-event local-db ["add" fixtures/Seed])
          (return
           (promise/x:promise-then
            (dbsql/connect (js-postgres/driver) (@! fixtures/+scratch-env+))
            (fn [remote-conn]
              (return
               (promise/x:promise-then
                (run-queries remote-conn remote-queries)
                (fn [_]
                  (:= remote-sync-conn
                      (dbsql/connection-create
                       remote-conn
                       {"disconnect" (fn [raw]
                                       (return (dbsql/disconnect raw)))
                        "query" (fn [raw input]
                                  (return (dbsql/query raw input)))
                        "query_sync" (fn [_raw input]
                                       (when (not (. input (match "\"Task\"")))
                                         (xt/x:err "Unexpected postgres sync query"))
                                       (return [{"status" "closed"}]))}))
                  (:= remote-db
                      (xdb/db-create {"::" "db.sql"
                                      :instance remote-sync-conn}
                                     fixtures/Schema
                                     pg-lookup
                                     pg-opts))
                  (xt/x:set-key remote-state "db" remote-db)
                  (return
                   (promise/x:promise-then
                    (refresh-summary)
                    (fn [summary]
                      (return
                       (promise/x:promise-then
                        (dbsql/ensure-promise (dbsql/disconnect local-conn))
                        (fn [_]
                          (return
                           (promise/x:promise-then
                            (dbsql/ensure-promise (dbsql/disconnect remote-sync-conn))
                            (fn [_]
                              (repl/notify summary)))))))))))))))))))))
  => {"local-status" "open"
      "remote-status" "closed"
      "local-query?" true
      "remote-query?" true
      "local-query-count" 2
      "remote-query-count" 1})
