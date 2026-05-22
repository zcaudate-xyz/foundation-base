 (ns xt.db.node.instance-pair-test
   (:require [hara.lang :as l]
             [xt.lang.common-notify :as notify])
   (:use code.test))
 
 ^{:seedgen/root {:all true
                  :langs [:js]}}
 (l/script- :js
   {:runtime :basic
    :require [[xt.db.node.instance-model :as model]
              [xt.db.helpers.test-fixtures :as fixtures]
              [xt.db.runtime :as xdb]
              [xt.protocol.impl.connection-sql :as dbsql]
              [xt.db.text.sql-manage :as manage]
              [xt.db.text.sql-util :as ut]
              [xt.substrate :as event-node]
              [xt.lang.common-data :as xtd]
              [xt.lang.common-repl :as repl]
              [xt.lang.common-string :as str]
              [xt.lang.spec-base :as xt]
              [xt.lang.spec-promise :as promise]
              [js.lib.driver-sqlite :as js-sqlite]]})
 
 (fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})
 
 ^{:refer xt.db.node.instance-model/view-refresh :added "4.1"}
 (fact "pairs a local sqlite view with a remote sqlite-backed view using helper fixtures"
 
   (notify/wait-on [:js 10000]
     (var node (event-node/node-create {"id" "node-pair"}))
     (model/install node fixtures/InstallOpts)
     (model/model-put node "room/local" "local" fixtures/ModelSpec)
     (model/model-put node "room/local" "pair"
                      {"views"
                       {"main"
                        {"query" {:table "Task"
                                  :return-method "default"
                                  :return-id "00000000-0000-0000-0000-0000000000a1"}
                         "input" []
                         "remote" {"space" "room/remote"}}}})
     (model/model-put node "room/remote" "pair" fixtures/ModelSpec)
     (var local-state (model/ensure-space-state node "room/local"))
     (var remote-state (model/ensure-space-state node "room/remote"))
     (var opts (ut/sqlite-opts nil))
     (var create-sql (str/join "\n\n"
                               (manage/table-create-all fixtures/Schema
                                                        fixtures/Lookup
                                                        opts)))
     (var out {})
     (-> (dbsql/connect (js-sqlite/driver) {})
         (promise/x:promise-then
          (fn [local-conn]
            (var local-db (xdb/db-create {"::" "db.sql"
                                          :instance local-conn}
                                         fixtures/Schema
                                         fixtures/Lookup
                                         opts))
            (xt/x:set-key local-state "db" local-db)
            (xdb/db-exec-sync local-db create-sql)
            (xdb/sync-event local-db ["add" fixtures/Seed])
            (return (dbsql/connect (js-sqlite/driver) {}))))
         (promise/x:promise-then
          (fn [remote-conn]
            (var remote-db (xdb/db-create {"::" "db.sql"
                                           :instance remote-conn}
                                          fixtures/Schema
                                          fixtures/Lookup
                                          opts))
            (xt/x:set-key remote-state "db" remote-db)
            (xdb/db-exec-sync remote-db create-sql)
            (xdb/sync-event remote-db ["add" fixtures/Seed])
            (xdb/sync-event remote-db
                            ["add" {"Task"
                                    [{"id" "00000000-0000-0000-0000-0000000000a1"
                                      "status" "closed"
                                      "name" "alpha-task"}]}])
            (return (model/view-refresh node "room/local" "local" "main"))))
         (promise/x:promise-then
          (fn [local-refresh]
            (xt/x:set-key out "local" local-refresh)
            (return (model/view-refresh node "room/local" "pair" "main"))))
         (promise/x:promise-then
          (fn [remote-refresh]
            (xt/x:set-key out "remote" remote-refresh)
            (repl/notify
             {"local-status" (xtd/get-in (model/view-val node "room/local" "local" "main")
                                         [0 "status"])
              "remote-status" (xtd/get-in (model/view-val node "room/local" "pair" "main")
                                          [0 "status"])
              "local-query?" (xt/x:is-string? (xtd/get-in out ["local" "query_key"]))
              "remote-query?" (xt/x:is-string? (xtd/get-in out ["remote" "query_key"]))
              "local-query-count" (xt/x:len (xt/x:obj-keys (. local-state ["queries"])))
              "remote-query-count" (xt/x:len (xt/x:obj-keys (. remote-state ["queries"])))})))
         (promise/x:promise-catch
          (fn [err]
            (repl/notify err)))))
   => {"local-status" "open"
       "remote-status" "closed"
       "local-query?" true
       "remote-query?" true
       "local-query-count" 2
       "remote-query-count" 1})
