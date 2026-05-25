(ns xt.db.walkthrough.guide-03-postgres-sqlite-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
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
                            "config" {"database" "test-scratch"}}
                 "caching" {"kind" "sqlite"
                            "config" {"filename" ":memory:"}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"views"
              {"list" {"resolver" (@! fixtures/+resolver-model-query+)
                       "source" "caching"}
               "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"primary_kind" (xtd/get-in
                            (node/source-get node "screen/admin" "entries-screen" "primary")
                            ["kind"])
             "caching_kind" (xtd/get-in
                            (node/source-get node "screen/admin" "entries-screen" "caching")
                            ["kind"])
             "list_table" (xtd/get-in
                          (node/view-get node "screen/admin" "entries-screen" "list")
                          ["resolver" "table"])
             "detail_query_keys" (xt/x:obj-keys
                                 (. (node/view-get node "screen/admin" "entries-screen" "detail")
                                    ["resolver"]))
             "node_id" (. node ["id"])
             "caching_sync_from" (xtd/get-in
                                 (node/source-get node "screen/admin" "entries-screen" "caching")
                                 ["sync_from"])})))))
  => {"primary_kind" "postgres"
      "caching_kind" "sqlite"
      "list_table" "Entry"
      "detail_query_keys" ["type" "table" "select_entry" "select_args" "return_entry"]
      "node_id" "admin-screen"
      "caching_sync_from" "primary"})
