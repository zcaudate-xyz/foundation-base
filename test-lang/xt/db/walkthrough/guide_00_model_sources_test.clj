(ns xt.db.walkthrough.guide-00-model-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.lib.driver-sqlite :as js-sqlite]
             [xt.substrate :as main]
             [xt.db.system.impl-postgres :as pg-impl]
             [xt.db.system.impl-sqlite :as sqlite-impl]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-00-model-sources/STEP.00-structural-sources :added "4.1"}
(fact "step 00: set up an xt.substrate node with explicit db services while views keep the query shape"

  (!.js
   (var node
        (main/node-create
         {"services"
          {"db/primary"
           (pg-impl/client-postgres
            (@! fixtures/+schema+)
            (@! fixtures/+lookup+)
            {}
            {"schema_name" "scratch"})
           "db/caching"
           (sqlite-impl/client-sqlite
            (@! fixtures/+schema+)
            (@! fixtures/+lookup+)
            nil)}}))
   {"services"
    {"primary" {"dbtype" (. (main/get-service node "db/primary") ["::"])
                "sync_from" nil
                "data_count" 0}
     "caching" {"dbtype" (. (main/get-service node "db/caching") ["::"])
                "sync_from" "primary"
                "data_count" 0}}
    "views"
    {"list" {"source" "caching"
             "query_keys" (xt/x:obj-keys (@! fixtures/+resolver-model-query+))}
     "detail" {"source" "primary"
               "query_keys" (xt/x:obj-keys (@! fixtures/+resolver-inline-query+))}}})
  => {"services"
      {"primary" {"dbtype" "db.client.postgres"
                  "sync_from" nil
                  "data_count" 0}
       "caching" {"dbtype" "db.client.sqlite"
                  "sync_from" "primary"
                  "data_count" 0}}
      "views"
      {"list" {"source" "caching"
               "query_keys" ["type" "table" "select_entry" "return_entry"]}
       "detail" {"source" "primary"
                 "query_keys" ["type" "table" "select_entry" "select_args" "return_entry"]}}})

^{:refer xt.db.walkthrough.guide-00-model-sources/STEP.01-empty-query :added "4.1"}
(fact "step 01: querying the empty caching service on the xt.substrate node returns an empty result"

  (notify/wait-on [:js 10000]
    (-> (sqlite-impl/client-sqlite
         (@! fixtures/+schema+)
         (@! fixtures/+lookup+)
         nil
         {"filename" ":memory:"})
        (sqlite-impl/client-sqlite-init
         (js-sqlite/driver))
        (promise/x:promise-then
         (fn [caching-db]
           (var node
                (main/node-create
                 {"services"
                  {"db/primary"
                   (pg-impl/client-postgres
                    (@! fixtures/+schema+)
                    (@! fixtures/+lookup+)
                    {}
                    {"schema_name" "scratch"})
                   "db/caching" caching-db}}))
           (var result
                (sqlite-impl/pull (main/get-service node "db/caching")
                                  ["Entry" ["name"]]))
           (repl/notify
            {"value" result
             "count" (xt/x:len result)
             "dbtype" (. (main/get-service node "db/caching") ["::"])})))))
  => {"value" []
      "count" 0
      "dbtype" "db.client.sqlite"})
