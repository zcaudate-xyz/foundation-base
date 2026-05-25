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
   (-> (node/create
        {"node_id" "admin-screen"
         "db" {"schema" (@! fixtures/+schema+)
               "lookup" (@! fixtures/+lookup+)
               "sources"
               {"primary" {"kind" "postgres"
                           "config" {"database" "test-scratch"}}
                "caching" {"kind" "sqlite"
                           "config" {"driver" (js-sqlite/driver)
                                     "filename" ":memory:"}
                           "setup" {"schema" true
                                    "seed" (@! fixtures/+entry-seed+)}
                           "resolver" (@! fixtures/+resolver-model-query+)}}}
         "spaces"
         {"screen/admin"
          {"models"
           {"entries-screen"
            {"sources" {"caching" {"config" {"driver" (js-sqlite/driver)
                                             "filename" "admin-screen.sqlite"}}}
             "views"
             {"summary" {"resolver" (@! fixtures/+resolver-model-query+)}
              "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
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
               {"primary_database" (xtd/get-in
                                    (node/source-get node "screen/admin" "entries-screen" "primary")
                                   ["config" "database"])
                "caching_file" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "caching")
                                ["config" "filename"])
                "node_id" (. node ["id"])
                "sqlite_row_count" (db-instance/db-exec-sync sqlite-db "SELECT COUNT(*) FROM Entry;")
                "sqlite_first" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "caching")
                                ["data" 0 "name"])
                "summary_source" (xtd/get-in
                                  (node/view-get node "screen/admin" "entries-screen" "summary")
                                  ["source"])}))))))))
  => {"primary_database" "test-scratch"
      "caching_file" "admin-screen.sqlite"
      "node_id" "admin-screen"
      "sqlite_row_count" 2
      "sqlite_first" "alpha"
      "summary_source" "caching"})
