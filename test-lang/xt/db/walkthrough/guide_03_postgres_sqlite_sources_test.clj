(ns xt.db.walkthrough.guide-03-postgres-sqlite-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.text.sql-util :as sql-util]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-03-postgres-sqlite-sources/STEP.00-source-descriptors :added "4.1"}
(fact "step 00: keep postgres primary and sqlite caching as stable model source bindings"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "admin-screen"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "postgres"
                            "config" {"database" "test-scratch"
                                      "db_opts" (sql-util/postgres-opts (@! fixtures/+lookup+))}}
                 "caching" {"kind" "sqlite"
                            "config" {"filename" ":memory:"
                                      "db_opts" (sql-util/sqlite-opts nil)}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"views"
              {"list" {"query" (@! fixtures/+model-query+)
                       "source" "caching"}
               "detail" {"query" (@! fixtures/+inline-query+)
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"primary-kind" (xtd/get-in
                            (node/source-get node "screen/admin" "entries-screen" "primary")
                            ["kind"])
             "primary-strict" (xtd/get-in
                              (node/source-get node "screen/admin" "entries-screen" "primary")
                              ["config" "db_opts" "strict"])
             "caching-kind" (xtd/get-in
                            (node/source-get node "screen/admin" "entries-screen" "caching")
                            ["kind"])
             "caching-strict" (xtd/get-in
                              (node/source-get node "screen/admin" "entries-screen" "caching")
                              ["config" "db_opts" "strict"])
             "list-table" (xtd/get-in
                          (node/view-get node "screen/admin" "entries-screen" "list")
                          ["query" "table"])
             "detail-query-keys" (xt/x:obj-keys
                                 (. (node/view-get node "screen/admin" "entries-screen" "detail")
                                    ["query"]))
             "node-id" (. node ["id"])
             "caching-sync-from" (xtd/get-in
                                 (node/source-get node "screen/admin" "entries-screen" "caching")
                                 ["sync_from"])})))))
  => {"primary-kind" "postgres"
      "primary-strict" true
      "caching-kind" "sqlite"
      "caching-strict" false
      "list-table" "Entry"
      "detail-query-keys" ["table" "select_entry" "select_args" "return_entry"]
      "node-id" "admin-screen"
      "caching-sync-from" "primary"})
