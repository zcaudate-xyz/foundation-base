(ns xt.db.walkthrough.guide-08-local-db-options-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

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
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-08-local-db-options/STEP.00-default-db-cache :added "4.1"}
(fact "step 00: with db.cache a local admin screen still keeps cached list and detail state on the same table"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "local-app"}))
    (var payload {"db/sync"
                  {"Entry" [{"id" "00000000-0000-0000-0000-0000000000b1"
                             "name" "alpha"
                             "tags" ["cache"]
                             "__deleted__" false}
                            {"id" "00000000-0000-0000-0000-0000000000b2"
                             "name" "beta"
                             "tags" ["cache"]
                             "__deleted__" false}]}})
    (var screen-spec {"views"
                      {"list"
                       {"query" (@! fixtures/+model-query+)
                        "input" []}
                       "detail"
                       {"query" (@! fixtures/+inline-query+)
                        "default_input" ["alpha"]}}})
    (model/install node {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "views" {}})
    (model/model-put node "room/cache" "entries-screen" screen-spec)
    (-> (model/sync node "room/cache" payload)
        (promise/x:promise-then
         (fn [_]
           (return
            (model/model-refresh node "room/cache" "entries-screen"))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"dbtype" (. (model/ensure-space-state node "room/cache") ["db"] ["::"])
             "list-count" (xt/x:len (model/view-val node "room/cache" "entries-screen" "list"))
             "detail-name" (xtd/get-in
                            (model/view-val node "room/cache" "entries-screen" "detail")
                            [0 "name"])})))))
  => {"dbtype" "db.cache"
      "list-count" 2
      "detail-name" "alpha"})

^{:refer xt.db.walkthrough.guide-08-local-db-options/STEP.01-explicit-sqlite :added "4.1"}
(fact "step 01: with sqlite the same local admin screen materializes list and detail state into db.sql"

  (notify/wait-on [:js 10000]
    (-> (sql/connect (js-sqlite/driver) {})
        (promise/x:promise-then
         (fn [conn]
           (var node (event-node/node-create {"id" "local-app"}))
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
                                    "__deleted__" false}
                                   {"id" "00000000-0000-0000-0000-0000000000b2"
                                    "name" "beta"
                                    "tags" ["sqlite"]
                                    "__deleted__" false}]}})
           (var screen-spec {"views"
                             {"list"
                              {"query" (@! fixtures/+model-query+)
                               "input" []}
                              "detail"
                              {"query" (@! fixtures/+inline-query+)
                               "default_input" ["alpha"]}}})
           (model/install node {"schema" (@! fixtures/+schema+)
                                "lookup" (@! fixtures/+lookup+)
                                "views" {}})
           (model/model-put node "room/sqlite" "entries-screen" screen-spec)
           (xt/x:set-key (model/ensure-space-state node "room/sqlite")
                         "db"
                         db)
           (db-instance/db-exec-sync
            db
            (xt/x:str-join
             "\n\n"
             (manage/table-create-all
              (@! fixtures/+schema+)
              (@! fixtures/+lookup+)
              db-opts)))
           (return
            (-> (model/sync node "room/sqlite" payload)
                (promise/x:promise-then
                 (fn [_]
                   (return
                    (model/model-refresh node "room/sqlite" "entries-screen"))))
                (promise/x:promise-then
                 (fn [_]
                   (repl/notify
                    {"dbtype" (. (model/ensure-space-state node "room/sqlite") ["db"] ["::"])
                     "row-count" (db-instance/db-exec-sync db "SELECT COUNT(*) FROM Entry;")
                     "list-count" (xt/x:len (model/view-val node "room/sqlite" "entries-screen" "list"))
                     "detail-name" (xtd/get-in
                                    (model/view-val node "room/sqlite" "entries-screen" "detail")
                                    [0 "name"])})))))))))
  => {"dbtype" "db.sql"
      "row-count" 2
      "list-count" 2
      "detail-name" "alpha"})
