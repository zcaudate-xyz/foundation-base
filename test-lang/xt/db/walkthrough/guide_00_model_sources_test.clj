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
             {"list" {"resolver" (@! fixtures/+resolver-model-query+)
                      "source" "caching"}
              "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
                        "source" "primary"}}}}}}})
       (promise/x:promise-then
        (fn [node]
          (repl/notify
           {"sources" (xt/x:obj-keys (. (node/model-get node "screen/admin" "entries-screen") ["sources"]))
            "list_source" (. (node/view-get node "screen/admin" "entries-screen" "list") ["source"])
            "detail_source" (. (node/view-get node "screen/admin" "entries-screen" "detail") ["source"])
            "list_query_keys" (xt/x:obj-keys (. (node/view-get node "screen/admin" "entries-screen" "list") ["resolver"]))
            "detail_query_keys" (xt/x:obj-keys (. (node/view-get node "screen/admin" "entries-screen" "detail") ["resolver"]))})))))
 => {"sources" ["primary" "caching"]
     "list_source" "caching"
     "detail_source" "primary"
     "list_query_keys" ["type" "table" "select_entry" "return_entry"]
     "detail_query_keys" ["type" "table" "select_entry" "select_args" "return_entry"]})

(comment
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
              {"list" {"resolver" (@! fixtures/+resolver-model-query+)
                       "source" "caching"}
               "detail" {"resolver" (@! fixtures/+resolver-inline-query+)
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            node))))))


(comment
  ;; what I'd like to happen is that a shared worker is setup, and all browser 
  ;;
  )
