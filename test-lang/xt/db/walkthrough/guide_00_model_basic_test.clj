(ns xt.db.walkthrough.guide-00-model-basic-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.node.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.00-install :added "4.1"}
(fact "step 00: install xt.db.node into a substrate node"

  (!.js
   (var node (event-node/node-create {"id" "node-a"}))
   (model/install node fixtures/InstallOpts)
   {"installed?" (xt/x:not-nil? node)
    "schema-tables" (xt/x:obj-keys (. fixtures/InstallOpts ["schema"]))})
  => {"installed?" true
      "schema-tables" ["Order"]})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.01-model-put :added "4.1"}
(fact "step 01: register a self-contained model with two query views"

  (!.js
   (var node (event-node/node-create {"id" "node-a"}))
   (model/install node fixtures/InstallOpts)
   (var out (model/model-put node "room/a" "orders" fixtures/ModelSpec))
   {"model-id" (. out ["id"])
    "view-ids" (xt/x:obj-keys (. out ["views"]))
    "main-query" (xtd/get-in out ["views" "main" "query" "table"])
    "open-default-input" (model/view-input node "room/a" "orders" "open")})
  => {"model-id" "orders"
      "view-ids" ["main" "open"]
      "main-query" "Order"
      "open-default-input" ["open"]})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.02-sync :added "4.1"}
(fact "step 02: sync some rows into the local cache-backed db"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/snapshot node "room/a"))))
        (promise/x:promise-then
         (fn [snapshot]
           (repl/notify
            {"rows" (xt/x:obj-keys (. snapshot ["rows"] ["Order"]))})))))
  => {"rows" ["ord-1" "ord-2"]})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.03-view-refresh :added "4.1"}
(fact "step 03: refresh the main view and read back its cached value"

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
           (repl/notify
            {"status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                  ["status"])
             "value" (xtd/get-in (model/view-val node "room/a" "orders" "main")
                                 [0 "status"])})))))
  => {"status" "ready"
      "value" "open"})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.04-input :added "4.1"}
(fact "step 04: override a parameterized view input before refreshing it"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/ModelSpec)
    (model/view-set-input node "room/a" "orders" "open" ["closed"])
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "open"))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"input" (model/view-input node "room/a" "orders" "open")
             "count" (xt/x:len (. result ["value"]))
             "query-key?" (xt/x:is-string? (. result ["query_key"]))})))))
  => {"input" ["closed"]
      "count" 1
      "query-key?" true})

^{:refer xt.db.walkthrough.guide-00-model-basic/STEP.05-query :added "4.1"}
(fact "step 05: run a direct query through the public node API"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/query
             node
             "room/a"
             {:table "Order"
              :return-entry {"input" [{"symbol" "i_order_id" "type" "text"}]
                             "return" "jsonb"
                             "view" {"table" "Order"
                                     "type" "return"
                                     "query" ["status"]}}
              :return-id "ord-1"}))))
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"query-key?" (xt/x:is-string? (. result ["query_key"]))
             "value" (xtd/get-in (. result ["value"]) [0 "status"])
             "tables" (. (. result ["tables"]) ["Order"])})))))
  => {"query-key?" true
      "value" "open"
      "tables" true})
