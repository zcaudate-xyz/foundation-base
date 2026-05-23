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

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.00-detail-lifecycle :added "4.1"}
(fact "step 00: the selected detail view in an admin screen moves through idle, pending, ready, stale, and error"

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
           (instance-state/set-view-stale state "orders" "main" {"tag" "screen/detail-stale"})
           (var stale (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                  ["status"]))
           (instance-state/set-view-error state "orders" "main" {"tag" "screen/detail-error"})
           (repl/notify
            {"detail-lifecycle" [initial
                                 pending
                                 ready
                                 stale
                                 (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                             ["status"])]
             "list-filter" (model/view-input node "room/a" "orders" "open")
             "error-tag" (xtd/get-in (model/view-error node "room/a" "orders" "main")
                                     ["tag"])})))))
  => {"detail-lifecycle" ["idle" "pending" "ready" "stale" "error"]
      "list-filter" ["open"]
      "error-tag" "screen/detail-error"})

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.01-hook-screen-state :added "4.1"}
(fact "step 01: a hook-style adapter can read the admin screen as a detail panel plus filtered list"
  
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var state nil)
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (:= state (model/ensure-space-state node "room/a"))
    (var use-admin-screen
         (fn []
           (return
            {"detail" {"status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                            ["status"])
                       "value" (model/view-val node "room/a" "orders" "main")
                       "error-tag" (xtd/get-in (model/view-error node "room/a" "orders" "main")
                                               ["tag"])}
             "list" {"status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                          ["status"])
                     "input" (model/view-input node "room/a" "orders" "open")
                     "value" (model/view-val node "room/a" "orders" "open")
                     "error-tag" (xtd/get-in (model/view-error node "room/a" "orders" "open")
                                             ["tag"])}})))
    (var idle-screen (use-admin-screen))
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/model-refresh node "room/a" "orders"))))
        (promise/x:promise-then
         (fn [_]
           (var ready-screen (use-admin-screen))
           (instance-state/set-view-stale state "orders" "main" {"tag" "screen/detail-stale"})
           (repl/notify
            {"idle" idle-screen
             "ready" ready-screen
             "detail-stale" (use-admin-screen)})))))
  => {"idle" {"detail" {"status" "idle"
                        "value" nil
                        "error-tag" nil}
              "list" {"status" "idle"
                      "input" ["open"]
                      "value" nil
                      "error-tag" nil}}
      "ready" {"detail" {"status" "ready"
                         "value" [{"status" "open"}]
                         "error-tag" nil}
               "list" {"status" "ready"
                       "input" ["open"]
                       "value" [{"id" "00000000-0000-0000-0000-0000000000a1"}]
                       "error-tag" nil}}
      "detail-stale" {"detail" {"status" "stale"
                                "value" [{"status" "open"}]
                                "error-tag" "screen/detail-stale"}
                      "list" {"status" "ready"
                              "input" ["open"]
                              "value" [{"id" "00000000-0000-0000-0000-0000000000a1"}]
                              "error-tag" nil}}})

^{:refer xt.db.walkthrough.guide-04-view-lifecycle/STEP.02-hook-refresh-flow :added "4.1"}
(fact "step 02: hook-style refresh calls can move both the admin detail and list panes from stale back to ready"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (var data-fn
         (fn [node]
           (return
            {"detail" {"status" (xtd/get-in
                                 (model/view-get node "room/a" "orders" "main")
                                 ["status"])
                       "value" (model/view-val node "room/a" "orders" "main")
                       "error" (model/view-error node "room/a" "orders" "main")}
             "list" {"status" (xtd/get-in
                               (model/view-get node "room/a" "orders" "open")
                               ["status"])
                     "value" (model/view-val node "room/a" "orders" "open")
                     "error" (model/view-error node "room/a" "orders" "open")}})))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (var state (model/ensure-space-state node "room/a"))
    (var stages [])
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/model-refresh node "room/a" "orders"))))
        (promise/x:promise-then
         (fn [_]
           (xtd/arr-pushr stages (data-fn node))
           (instance-state/set-view-stale state "orders" "main" {"tag" "hook/detail-reload"})
           (instance-state/set-view-stale state "orders" "open" {"tag" "hook/list-reload"})
           (xtd/arr-pushr stages (data-fn node))
           (return
            (model/model-refresh node "room/a" "orders"))))
        (promise/x:promise-then
         (fn [_]
           (xtd/arr-pushr stages (data-fn node))
           (repl/notify stages)))))
  => [{"list"
       {"error" nil,
        "value" [{"id" "00000000-0000-0000-0000-0000000000a1"}],
        "status" "ready"},
       "detail"
       {"error" nil, "value" [{"status" "open"}], "status" "ready"}}
      {"list"
       {"error" {"tag" "hook/list-reload"},
        "value" [{"id" "00000000-0000-0000-0000-0000000000a1"}],
        "status" "stale"},
       "detail"
       {"error" {"tag" "hook/detail-reload"},
        "value" [{"status" "open"}],
        "status" "stale"}}
      {"list"
       {"error" nil,
        "value" [{"id" "00000000-0000-0000-0000-0000000000a1"}],
        "status" "ready"},
       "detail"
       {"error" nil, "value" [{"status" "open"}], "status" "ready"}}])
  
