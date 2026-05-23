(ns xt.db.walkthrough.guide-06-invalidation-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.instance-sync :as instance-sync]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-06-invalidation/STEP.00-mark-stale :added "4.1"}
(fact "step 00: when the shared task table changes, both the admin detail view and list view become stale"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var install-opts {"schema" fixtures/Schema
                       "lookup" fixtures/Lookup
                       "views" fixtures/Views
                       "auto_refresh" false})
    (model/install node install-opts)
    (model/model-put node "room/a" "orders" fixtures/DependentModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/model-refresh node "room/a" "orders"))))
        (promise/x:promise-then
         (fn [_]
           (var state (model/ensure-space-state node "room/a"))
           (var query-key (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                      ["query_key"]))
           (instance-sync/handle-cache-invalidated
            (. node ["spaces"] ["room/a"])
            {"data" {"db/sync"
                     {"Task" [{"id" "00000000-0000-0000-0000-0000000000a1"}]}}}
            {"id" "node-1"})
           (repl/notify
            {"query-status" (xtd/get-in state ["queries" query-key "status"])
             "detail-status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                         ["status"])
             "list-status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                       ["status"])})))))
  => {"query-status" "stale"
      "detail-status" "stale"
      "list-status" "stale"})
