(ns xt.db.walkthrough.guide-00-model-sources-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-00-model-sources/STEP.00-structural-sources :added "4.1"}
(fact "step 00: define primary and caching once at the model level while views keep the sql-pull query shape"

 (notify/wait-on [:js 10000]
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
          (repl/notify
           {"sources" (xt/x:obj-keys (. (node/model-get node "screen/admin" "entries-screen") ["sources"]))
            "list-source" (. (node/view-get node "screen/admin" "entries-screen" "list") ["source"])
            "detail-source" (. (node/view-get node "screen/admin" "entries-screen" "detail") ["source"])
            "list-query-keys" (xt/x:obj-keys (. (node/view-get node "screen/admin" "entries-screen" "list") ["query"]))
            "detail-query-keys" (xt/x:obj-keys (. (node/view-get node "screen/admin" "entries-screen" "detail") ["query"]))})))))
 => {"sources" ["primary" "caching"]
     "list-source" "caching"
     "detail-source" "primary"
     "list-query-keys" ["table" "select_entry" "return_entry"]
     "detail-query-keys" ["table" "select_entry" "select_args" "return_entry"]})
