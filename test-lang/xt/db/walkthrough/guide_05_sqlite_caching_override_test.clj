(ns xt.db.walkthrough.guide-05-sqlite-caching-override-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.runtime :as db-instance]
             [xt.db.node :as node]
             [xt.db.text.sql-util :as sql-util]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-05-sqlite-caching-override/STEP.00-model-override :added "4.1"}
(fact "step 00: refresh a live sqlite caching source while overriding its filename at the model level"

 (notify/wait-on [:js 10000]
   (var sqlite-opts (sql-util/sqlite-opts nil))
   (-> (node/create
        {"node_id" "admin-screen"
         "db" {"schema" (@! fixtures/+schema+)
               "lookup" (@! fixtures/+lookup+)
               "sources"
               {"primary" {"kind" "postgres"
                           "database" "test-scratch"
                           "db_opts" (sql-util/postgres-opts (@! fixtures/+lookup+))}
                "caching" {"kind" "sqlite"
                           "constructor" js-sqlite/connect-constructor
                           "wrapper" js-sqlite/wrap-connection
                           "query_live" true
                           "filename" ":memory:"
                           "db_opts" sqlite-opts
                           "setup_schema" true
                           "seed" (@! fixtures/+entry-seed+)
                           "query" (@! fixtures/+model-query+)}}}
         "spaces"
         {"screen/admin"
          {"models"
           {"entries-screen"
            {"sources" {"caching" {"filename" "admin-screen.sqlite"}}
             "views"
             {"summary" {"query" (@! fixtures/+model-query+)}
              "detail" {"query" (@! fixtures/+inline-query+)
                        "source" "primary"}}}}}}})
       (promise/x:promise-then
        (fn [node]
          (return
           (promise/x:promise-then
            (node/view-refresh node "screen/admin" "entries-screen" "summary")
            (fn [_]
              (var sqlite-db (xtd/get-in
                              (node/source-get node "screen/admin" "entries-screen" "caching")
                              ["db"]))
              (repl/notify
               {"primary-database" (xtd/get-in
                                    (node/source-get node "screen/admin" "entries-screen" "primary")
                                    ["database"])
                "caching-file" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "caching")
                                ["filename"])
                "node-id" (. node ["id"])
                "sqlite-row-count" (db-instance/db-exec-sync sqlite-db "SELECT COUNT(*) FROM Entry;")
                "sqlite-first" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "caching")
                                ["data" 0 "name"])
                "summary-source" (xtd/get-in
                                  (node/view-get node "screen/admin" "entries-screen" "summary")
                                  ["source"])}))))))))
  => {"primary-database" "test-scratch"
      "caching-file" "admin-screen.sqlite"
      "node-id" "admin-screen"
      "sqlite-row-count" 2
      "sqlite-first" "alpha"
      "summary-source" "caching"})
