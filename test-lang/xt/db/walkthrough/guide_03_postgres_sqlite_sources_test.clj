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
            (node/summarise node))))))
  => {"id" "admin-screen"
      "spaces"
      {"screen/admin"
       {"models"
        {"entries-screen"
         {"sources"
          {"primary" {"kind" "postgres"
                     "sync_from" nil
                     "live" false
                     "data_count" 0}
           "caching" {"kind" "sqlite"
                     "sync_from" "primary"
                     "live" false
                     "data_count" 0}}
          "views"
          {"list" {"source" "caching"
                  "status" "idle"
                  "resolver_keys" ["type" "table" "select_entry" "return_entry"]}
           "detail" {"source" "primary"
                    "status" "idle"
                    "resolver_keys" ["type" "table" "select_entry" "select_args" "return_entry"]}}}}}}})
