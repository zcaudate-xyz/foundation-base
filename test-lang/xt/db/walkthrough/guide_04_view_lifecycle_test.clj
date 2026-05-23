(ns xt.db.walkthrough.guide-04-view-lifecycle-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.instance-state :as instance-state]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.00-lifecycle :added "4.1"}
(fact "step 00: a local view moves through idle, pending, ready, stale, and error"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var state nil)
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (:= state (model/ensure-space-state node "room/a"))
    (var initial (xtd/get-in (model/view-get node "room/a" "orders" "main")
                             ["status"]))
    (instance-state/set-view-pending state "orders" "main")
    (var pending (xtd/get-in (model/view-get node "room/a" "orders" "main")
                             ["status"]))
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (var ready (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                  ["status"]))
           (instance-state/set-view-stale state "orders" "main" {"tag" "demo/stale"})
           (var stale (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                  ["status"]))
           (instance-state/set-view-error state "orders" "main" {"tag" "demo/error"})
           (repl/notify
            {"lifecycle" [initial
                          pending
                          ready
                          stale
                          (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                      ["status"])]
             "error-tag" (xtd/get-in (model/view-error node "room/a" "orders" "main")
                                     ["tag"])})))))
  => {"lifecycle" ["idle" "pending" "ready" "stale" "error"]
      "error-tag" "demo/error"})

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.01-hook-render-state :added "4.1"}
(fact "step 01: a hook-style adapter can derive render state from the local view lifecycle"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var state nil)
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (:= state (model/ensure-space-state node "room/a"))
    (var use-orders-view
         (fn [status value error]
           (return
            {"status" status
            "show-skeleton?" (== status "idle")
            "show-spinner?" (== status "pending")
            "show-stale-badge?" (== status "stale")
            "show-error?" (== status "error")
            "rows" (:? value value [])
            "error-tag" (xtd/get-in error ["tag"])})))
    (var idle-ui (use-orders-view
                 (xtd/get-in (model/view-get node "room/a" "orders" "main")
                             ["status"])
                 (model/view-val node "room/a" "orders" "main")
                 (model/view-error node "room/a" "orders" "main")))
    (instance-state/set-view-pending state "orders" "main")
    (var pending-ui (use-orders-view
                    (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                ["status"])
                    (model/view-val node "room/a" "orders" "main")
                    (model/view-error node "room/a" "orders" "main")))
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (instance-state/set-view-stale state "orders" "main" {"tag" "demo/stale"})
           (var stale-ui (use-orders-view
                         (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                     ["status"])
                         (model/view-val node "room/a" "orders" "main")
                         (model/view-error node "room/a" "orders" "main")))
           (instance-state/set-view-error state "orders" "main" {"tag" "demo/error"})
           (repl/notify
            {"idle" idle-ui
            "pending" pending-ui
            "stale" stale-ui
            "error" (use-orders-view
                     (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                 ["status"])
                     (model/view-val node "room/a" "orders" "main")
                     (model/view-error node "room/a" "orders" "main"))})))))
  => {"idle" {"status" "idle"
             "show-skeleton?" true
             "show-spinner?" false
             "show-stale-badge?" false
             "show-error?" false
             "rows" []
             "error-tag" nil}
      "pending" {"status" "pending"
                "show-skeleton?" false
                "show-spinner?" true
                "show-stale-badge?" false
                "show-error?" false
                "rows" []
                "error-tag" nil}
      "stale" {"status" "stale"
              "show-skeleton?" false
              "show-spinner?" false
              "show-stale-badge?" true
              "show-error?" false
              "rows" [{"status" "open"}]
              "error-tag" "demo/stale"}
      "error" {"status" "error"
              "show-skeleton?" false
              "show-spinner?" false
              "show-stale-badge?" false
              "show-error?" true
              "rows" [{"status" "open"}]
              "error-tag" "demo/error"}})

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.02-hook-refresh-flow :added "4.1"}
(fact "step 02: hook-style refresh calls can move a cached view from ready to stale to ready"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var state nil)
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (:= state (model/ensure-space-state node "room/a"))
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (var ready-before {"status" (xtd/get-in
                                       (model/view-get node "room/a" "orders" "main")
                                       ["status"])
                             "value" (model/view-val node "room/a" "orders" "main")})
           ;; Simulate a hook receiving invalidation before requesting a fresh read.
           (instance-state/set-view-stale state "orders" "main" {"tag" "hook/reload"})
           (return
            (promise/x:promise-then
            (model/view-refresh node "room/a" "orders" "main")
            (fn [_]
              (return {"before" ready-before
                       "stale" {"status" "stale"
                                "error-tag" "hook/reload"}
                       "after" {"status" (xtd/get-in
                                          (model/view-get node "room/a" "orders" "main")
                                          ["status"])
                                "value" (model/view-val node "room/a" "orders" "main")}}))))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"before" {"status" "ready"
               "value" [{"status" "open"}]}
      "stale" {"status" "stale"
              "error-tag" "hook/reload"}
      "after" {"status" "ready"
              "value" [{"status" "open"}]}})
