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
             [xt.db.runtime :as xdb]
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
              {"primary" {"query" (@! fixtures/+model-query+)}
               "caching" {"query" (@! fixtures/+model-query+)}}
              "views"
              {"list" {"query" (@! fixtures/+model-query+)
                       "source" "caching"}
               "detail" {"query" (@! fixtures/+inline-query+)
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
                        {"initial-count" (xt/x:len (. initial ["value"]))
                         "updated-count" (xt/x:len updated-value)
                         "view-source" (. updated ["source"])
                         "cached-count" (xt/x:len
                                         (xtd/get-in
                                          (node/source-get node "screen/admin" "entries-screen" "caching")
                                          ["data"]))
                         "has-gamma" (xt/x:arr-some
                                      updated-value
                                      (fn [entry]
                                        (return (== "gamma" (xt/x:get-key entry "name")))))
                         "gamma-tags" (xtd/get-in
                                      updated-value
                                      [gamma-idx "tags"])}))))))))))))
  => {"initial-count" 2
      "updated-count" 3
      "view-source" "caching"
      "cached-count" 3
      "has-gamma" true
      "gamma-tags" "[\"external\",\"sync\"]"})
