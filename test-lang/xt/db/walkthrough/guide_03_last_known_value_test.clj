(ns xt.db.walkthrough.guide-03-last-known-value-test
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

^{:refer xt.db.walkthrough.guide-03-last-known-value/STEP.00-last-known-value :added "4.1"}
(fact "step 00: the local view keeps its last value while the next refresh is pending"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (var state (model/ensure-space-state node "room/a"))
           (instance-state/set-view-pending state "orders" "main")
           (repl/notify
            {"pending" (model/view-pending node "room/a" "orders" "main")
             "status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                  ["status"])
             "last-value" (xtd/get-in (model/view-val node "room/a" "orders" "main")
                                      [0 "status"])})))))
  => {"pending" true
      "status" "pending"
      "last-value" "open"})

^{:refer xt.db.walkthrough.guide-03-last-known-value/STEP.01-combined-view :added "4.1"}
(fact "step 01: combine select-entry and return-entry into one detailed collection view"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put
     node
     "room/a"
     "orders"
     {"views"
      {"open-detailed"
       {"query" {:table "Task"
                 :select-method "by_status"
                 :return-entry {"input" [{"symbol" "i_task_id" "type" "uuid"}]
                                "return" "jsonb"
                                "view" {"table" "Task"
                                        "type" "return"
                                        "tag" "detailed"
                                        "access" {"roles" {}}
                                        "guards" []
                                        "query" ["id" "status" "name"]}}}
        "default_input" ["open"]}}})
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "open-detailed"))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"status" (xtd/get-in
                       (model/view-get node "room/a" "orders" "open-detailed")
                       ["status"])
             "input" (model/view-input node "room/a" "orders" "open-detailed")
             "value" (model/view-val node "room/a" "orders" "open-detailed")})))))
  => {"status" "ready"
      "input" ["open"]
      "value" [{"id" "00000000-0000-0000-0000-0000000000a1"
                "status" "open"
                "name" "alpha-task"}]})

^{:refer xt.db.walkthrough.guide-03-last-known-value/STEP.02-input-change :added "4.1"}
(fact "step 02: simulate a component changing the view input from open to closed and refreshing the model"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put
     node
     "room/a"
     "orders"
     {"views"
      {"open-detailed"
       {"query" {:table "Task"
                 :select-method "by_status"
                 :return-entry {"input" [{"symbol" "i_task_id" "type" "uuid"}]
                                "return" "jsonb"
                                "view" {"table" "Task"
                                        "type" "return"
                                        "tag" "detailed"
                                        "access" {"roles" {}}
                                        "guards" []
                                        "query" ["id" "status" "name"]}}}
        "default_input" ["open"]}}})
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "open-detailed"))))
        (promise/x:promise-then
         (fn [_]
           (var before {"input" (model/view-input node "room/a" "orders" "open-detailed")
                       "value" (model/view-val node "room/a" "orders" "open-detailed")})
           (return
            (promise/x:promise-then
            (model/view-set-input node "room/a" "orders" "open-detailed" ["closed"])
            (fn [_]
              (return before))))))
        (promise/x:promise-then
         (fn [before]
           (repl/notify
            {"before" before
            "after" {"status" (xtd/get-in
                               (model/view-get node "room/a" "orders" "open-detailed")
                               ["status"])
                     "input" (model/view-input node "room/a" "orders" "open-detailed")
                     "value" (model/view-val node "room/a" "orders" "open-detailed")}})))))
  => {"before" {"input" ["open"]
               "value" [{"id" "00000000-0000-0000-0000-0000000000a1"
                         "status" "open"
                         "name" "alpha-task"}]}
      "after" {"status" "ready"
              "input" ["closed"]
              "value" [{"id" "00000000-0000-0000-0000-0000000000a2"
                        "status" "closed"
                        "name" "beta-task"}]}})


(comment

  
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        ))

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "open"))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [(model/view-val node "room/a" "orders" "main")
             (model/view-val node "room/a" "orders" "open")])))
        ))
  => [[{"status" "open"}] nil])
