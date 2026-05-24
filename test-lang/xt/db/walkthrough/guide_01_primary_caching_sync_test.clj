(ns xt.db.walkthrough.guide-01-primary-caching-sync-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-01-primary-caching-sync/STEP.00-model-sync :added "4.1"}
(fact "step 00: synchronize primary into caching before refreshing a fixture-backed caching view"

  (notify/wait-on :js
    (-> (node/create
        {"node_id" "admin-screen"
         "db" {"schema" (@! fixtures/+schema+)
               "lookup" (@! fixtures/+lookup+)
               "sources"
               {"primary" {"kind" "postgres"}
                "caching" {"kind" "sqlite"}}}
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
          (node/source-put node
                           "screen/admin"
                           "entries-screen"
                           "primary"
                           [{"id" "00000000-0000-0000-0000-0000000000c1"
                             "name" "alpha"
                             "tags" ["guide" "sql"]
                             "__deleted__" false}
                            {"id" "00000000-0000-0000-0000-0000000000c2"
                            "name" "beta"
                            "tags" ["guide"]
                            "__deleted__" false}])
          (return
           (-> (node/model-sync node "screen/admin" "entries-screen")
               (promise/x:promise-then
                (fn [_]
                  (return (node/view-refresh node "screen/admin" "entries-screen" "list"))))
               (promise/x:promise-then
                (fn [result]
                  (repl/notify
                   {"list-source" (. result ["source"])
                    "list-count" (xt/x:len (node/view-val node "screen/admin" "entries-screen" "list"))
                    "list-query-keys" (xt/x:obj-keys
                                       (. (node/view-get node "screen/admin" "entries-screen" "list")
                                          ["query"]))
                    "node-id" (. node ["id"])
                    "first-cached" (xtd/get-in
                                    (node/source-get node "screen/admin" "entries-screen" "caching")
                                    ["data" 0 "name"])})))))))))
  => {"list-source" "caching"
     "list-count" 2
     "list-query-keys" ["table" "select_entry" "return_entry"]
     "node-id" "admin-screen"
     "first-cached" "alpha"})
