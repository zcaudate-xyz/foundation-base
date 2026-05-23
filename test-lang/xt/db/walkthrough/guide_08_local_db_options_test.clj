(ns xt.db.walkthrough.guide-08-local-db-options-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

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
             [python.lib.driver-sqlite :as py-sqlite]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-08-local-db-options/STEP.00-default-db-cache :added "4.1"}
(fact "step 00: without an explicit db attachment the local space lazily uses db.cache"

  (notify/wait-on :python
    (var node (event-node/node-create {"id" "local-app"}))
    (var payload {"db/sync"
                  {"Entry" [{"id" "00000000-0000-0000-0000-0000000000b1"
                             "name" "alpha"
                             "tags" ["cache"]
                             "__deleted__" false}]}})
    (model/install node {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "views" {}})
    (model/model-put node "room/cache" "entries" (@! fixtures/+model-spec+))
    (-> (model/sync node "room/cache" payload)
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/cache" "entries" "entries"))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"dbtype" (. (model/ensure-space-state node "room/cache") ["db"] ["::"])
             "count" (xt/x:len (. result ["value"]))
             "first-name" (xtd/get-in (. result ["value"]) [0 "name"])})))))
  => {"dbtype" "db.cache"
      "count" 1
      "first-name" "alpha"})

^{:refer xt.db.walkthrough.guide-08-local-db-options/STEP.01-explicit-sqlite :added "4.1"}
(fact "step 01: with sqlite the local space stores sync rows in db.sql and refreshes from that materialized cache"

  (notify/wait-on :python
    (var node (event-node/node-create {"id" "local-app"}))
    (var conn (py-sqlite/wrap-connection
               (py-sqlite/connect-constructor {})))
    (var db-opts (sql-util/sqlite-opts nil))
    (var db (db-instance/db-create
             {"::" "db.sql"
              :instance conn}
             (@! fixtures/+schema+)
             (@! fixtures/+lookup+)
             db-opts))
    (var payload {"db/sync"
                  {"Entry" [{"id" "00000000-0000-0000-0000-0000000000b1"
                             "name" "alpha"
                             "tags" ["sqlite"]
                             "__deleted__" false}]}})
    (model/install node {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "views" {}})
    (model/model-put node "room/sqlite" "entries" (@! fixtures/+model-spec+))
    (xt/x:set-key (model/ensure-space-state node "room/sqlite")
                  "db"
                  db)
    (sql/query-sync
     conn
     (xt/x:str-join
      "\n\n"
      (manage/table-create-all
       (@! fixtures/+schema+)
       (@! fixtures/+lookup+)
       db-opts)))
    (promise/x:promise-catch
     (-> (model/sync node "room/sqlite" payload)
         (promise/x:promise-then
          (fn [_]
            (return
             (model/view-refresh node "room/sqlite" "entries" "entries"))))
         (promise/x:promise-then
          (fn [result]
            (var out {"dbtype" (. (model/ensure-space-state node "room/sqlite") ["db"] ["::"])
                      "row-count" (sql/query-sync conn "SELECT COUNT(*) FROM Entry;")
                      "count" (xt/x:len (. result ["value"]))
                      "first-name" (xtd/get-in (. result ["value"]) [0 "name"])})
            (sql/disconnect conn)
            (repl/notify out))))
     (fn [err]
       (sql/disconnect conn)
       (repl/notify err))))
  => {"dbtype" "db.sql"
      "row-count" 1
      "count" 1
      "first-name" "alpha"})
