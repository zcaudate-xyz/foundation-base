(ns xt.db.walkthrough.guide-05-dependency-graph-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.00-dependency-index :added "4.1"}
(fact "step 00: an admin screen can record how its detail and filtered list views are linked"

  (!.js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/DependentModelSpec)
    {"detail-dependents" (model/view-dependents node "room/a" "orders" "main")
     "list-filter" (model/view-input node "room/a" "orders" "open")})
  => {"detail-dependents" {"orders" ["open"]}
      "list-filter" ["open"]})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.01-dependent-refresh :added "4.1"}
(fact "step 01: refreshing the selected detail view can also refresh the admin list state that depends on it"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/DependentModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"detail-status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                         ["status"])
             "list-status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                       ["status"])
             "list-query-key?" (xt/x:is-string?
                                (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                            ["query_key"]))
             "list-count" (xt/x:len (or (model/view-val node "room/a" "orders" "open")
                                        []))})))))
  => {"detail-status" "ready"
      "list-status" "ready"
      "list-query-key?" true
      "list-count" 1})
