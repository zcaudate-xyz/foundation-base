(ns xt.db.walkthrough.guide-08-external-caching-sync-view-test
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
             [xt.db.system :as xdb]
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

^{:refer xt.db.walkthrough.guide-08-external-caching-sync-view/STEP.00-receive-external-sync-and-refresh-existing-view
  :added "4.1"
  :setup [(fixtures/seed-entry-rows)]}
(fact "step 00: external db/sync events update the existing caching-backed list view"

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
                   (return (node/view-refresh node "screen/admin" "entries-screen" "list"))))
                (promise/x:promise-then
                 (fn [initial]
                   (var caching-db
                        (xtd/get-in
                         (node/source-get node "screen/admin" "entries-screen" "caching")
                         ["db"]))
                   (xdb/sync-event
                    caching-db
                    ["add"
                     {"Entry"
                      [{"id" "00000000-0000-0000-0000-0000000000d8"
                        "name" "gamma"
                        "tags" ["external" "sync"]
                        "__deleted__" false}]}])
                   (return
                    (promise/x:promise-then
                     (node/view-refresh node "screen/admin" "entries-screen" "list")
                     (fn [updated]
                       (var updated-value (. updated ["value"]))
                       (var gamma-idx
                            (xtd/arr-find
                             updated-value
                             (fn [entry]
                               (return (== "gamma" (xt/x:get-key entry "name"))))))
                       (repl/notify
                        {"summary" (node/summarise node)
                         "initial_count" (xt/x:len (. initial ["value"]))
                         "updated_count" (xt/x:len updated-value)
                         "cached_count" (xt/x:len
                                         (xtd/get-in
                                          (node/source-get node "screen/admin" "entries-screen" "caching")
                                          ["data"]))
                         "has_gamma" (xt/x:arr-some
                                      updated-value
                                      (fn [entry]
                                        (return (== "gamma" (xt/x:get-key entry "name")))))
                         "gamma_tags" (xtd/get-in
                                      updated-value
                                      [gamma-idx "tags"])}))))))))))))
  => {"summary"
      {"id" "admin-screen"
       "spaces"
       {"screen/admin"
        {"models"
         {"entries-screen"
          {"sources"
           {"primary" {"kind" "postgres"
                       "sync_from" nil
                       "live" true
                       "data_count" 2}
            "caching" {"kind" "sqlite"
                       "sync_from" "primary"
                       "live" true
                       "data_count" 3}}
           "views"
           {"list" {"source" "caching"
                    "status" "ready"
                    "query_keys" ["type" "table" "select_entry" "return_entry"]}
            "detail" {"source" "primary"
                      "status" "idle"
                      "query_keys" ["type" "table" "select_entry" "select_args" "return_entry"]}}}}}}}
      "initial_count" 2
      "updated_count" 3
      "cached_count" 3
      "has_gamma" true
      "gamma_tags" "[\"external\",\"sync\"]"})
