(ns xt.db.walkthrough.guide-02-primary-view-test
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
   [xt.lang.spec-base :as xt]
   [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-02-primary-view/STEP.00-view-role :added "4.1"}
(fact "step 00: a primary-backed fixture view can bypass caching while caching-backed views keep using the replica"

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
                             "__deleted__" false}])
          (node/source-put node
                           "screen/admin"
                           "entries-screen"
                           "caching"
                           [{"id" "00000000-0000-0000-0000-0000000000c1"
                             "name" "alpha-cached"
                             "tags" ["guide" "sql"]
                             "__deleted__" false}])
          (return
           (-> (promise/x:promise-all
                [(node/view-refresh node "screen/admin" "entries-screen" "list")
                 (node/view-refresh node "screen/admin" "entries-screen" "detail")])
               (promise/x:promise-then
                (fn [_]
                  (repl/notify
                   {"list-name" (xtd/get-in
                                 (node/view-val node "screen/admin" "entries-screen" "list")
                                 [0 "name"])
                    "node-id" (. node ["id"])
                    "detail-query-keys" (xt/x:obj-keys
                                         (. (node/view-get node "screen/admin" "entries-screen" "detail")
                                            ["query"]))
                    "detail-name" (xtd/get-in
                                   (node/view-val node "screen/admin" "entries-screen" "detail")
                                   [0 "name"])})))))))))
  => {"list-name" "alpha-cached"
     "node-id" "admin-screen"
     "detail-query-keys" ["table" "select_entry" "select_args" "return_entry"]
     "detail-name" "alpha"})
