(ns xt.db.walkthrough.guide-07-projection-layer-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :python
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.helpers.test-fixtures :as fixtures]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-07-projection-layer/STEP.00-local-projection :added "4.1"}
(fact "step 00: the local space can project remote server state into a client-specific filtered view"

  (notify/wait-on :python
    (var node (event-node/node-create {"id" "task-app"}))
    (var payload {"db/sync"
                  {"Entry" [{"id" "00000000-0000-0000-0000-0000000000c1"
                             "name" "alpha"
                             "tags" ["guide" "client"]
                             "__deleted__" false}
                            {"id" "00000000-0000-0000-0000-0000000000c2"
                             "name" "beta"
                             "tags" ["guide"]
                             "__deleted__" false}]}})
    (model/install node {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "views" {}})
    (model/model-put node
                     "room/server"
                     "entries"
                     (@! fixtures/+model-spec+))
    (model/model-put node
                     "room/local"
                     "entries"
                     {"views"
                      {"entries"
                       {"query" (@! fixtures/+inline-query+)
                        "input" ["alpha"]
                        "remote" {"space" "room/server"}}}})
    (promise/x:promise-catch
     (-> (model/sync node "room/server" payload)
         (promise/x:promise-then
          (fn [_]
            (return
             (model/view-refresh node "room/local" "entries" "entries"))))
         (promise/x:promise-then
          (fn [result]
            (repl/notify
             {"server-model" (. (model/model-get node "room/server" "entries")
                                ["id"])
              "local-model" (. (model/model-get node "room/local" "entries")
                               ["id"])
              "remote-space" (xtd/get-in
                              (model/view-get node "room/local" "entries" "entries")
                              ["remote" "space"])
              "local-input" (model/view-input node "room/local" "entries" "entries")
              "count" (xt/x:len (. result ["value"]))
              "first-name" (xtd/get-in (. result ["value"]) [0 "name"])}))))
     (fn [err]
       (repl/notify err))))
  => {"server-model" "entries"
      "local-model" "entries"
      "remote-space" "room/server"
      "local-input" ["alpha"]
      "count" 1
      "first-name" "alpha"})
