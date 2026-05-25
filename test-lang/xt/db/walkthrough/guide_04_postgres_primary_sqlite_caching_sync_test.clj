(ns xt.db.walkthrough.guide-04-postgres-primary-sqlite-caching-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]
            [postgres.core :as pg]
            [postgres.sample.scratch-v1 :as scratch]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.core :as pg]
             [postgres.sample.scratch-v1 :as scratch]]})

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [js.lib.driver-postgres :as js-pg]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-04-postgres-primary-sqlite-caching-sync/STEP.00-sync-primary-into-caching :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 00: sync postgres primary into sqlite caching through live source drivers"

  (pg/t:select scratch/Entry)
  => (contains-in
      [{:tags ["guide" "sql"]
        :name "alpha"
        :time-updated nil
        :time-created nil
        :id string?}
       {:tags ["guide"]
        :name "beta"
        :time-updated nil
        :time-created nil
        :id string?}])
  
  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "admin-screen"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "postgres"
                            "config" {"driver" (js-pg/driver)
                                      "database" "test-scratch"}}
                 "caching" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"sources"
              {"primary" {"resolver" (@! fixtures/+resolver-model-query+)}
               "caching" {"resolver" (@! fixtures/+resolver-model-query+)}} 
              "views"
              {"list" {"resolver" (@! fixtures/+resolver-model-query+)
                       "source" "caching"}
               "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (return
            (-> (node/model-sync node "screen/admin" "entries-screen")
                (promise/x:promise-then
                 (fn [_]
                   (return
                    (promise/x:promise-all
                     [(node/view-refresh node "screen/admin" "entries-screen" "list")
                      (node/view-refresh node "screen/admin" "entries-screen" "detail")]))))
                (promise/x:promise-then
                 (fn [[list-refresh detail-refresh]]
                   (repl/notify
                    {"primary_kind" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "primary")
                                     ["kind"])
                     "caching_kind" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "caching")
                                     ["kind"])
                     "list_source" (xtd/get-in list-refresh ["source"])
                     "detail_source" (xtd/get-in detail-refresh ["source"])
                     "node_id" (. node ["id"])
                     "cached_first" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "caching")
                                     ["data" 0 "name"])
                     "detail_default" (xtd/get-in
                                       (node/view-get node "screen/admin" "entries-screen" "detail")
                                       ["resolver" "select_args" 0])})))))))))
  => {"primary_kind" "postgres"
      "caching_kind" "sqlite"
      "list_source" "caching"
      "detail_source" "primary"
      "node_id" "admin-screen"
      "cached_first" "alpha"
      "detail_default" "alpha"})
