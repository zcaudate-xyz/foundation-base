(ns xt.db.walkthrough.guide-07-projection-layer-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures]))

^{:seedgen/root {:all true}}
(l/script- :js
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

^{:refer xt.db.walkthrough.guide-07-projection-layer/STEP.00-admin-screen-projection :added "4.1"}
(fact "step 00: the local space can project remote server state into an admin screen with list and detail views on the same table"

  (notify/wait-on :js
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
    (var install-opts {"schema" (@! fixtures/+schema+)
                       "lookup" (@! fixtures/+lookup+)
                       "views" {}})
    (var server-spec {"views"
                      {"list"
                       {"query" (@! fixtures/+model-query+)
                        "input" []}
                       "detail"
                       {"query" (@! fixtures/+inline-query+)
                        "default_input" ["alpha"]}}})
    (var local-spec {"views"
                     {"list"
                      {"query" (@! fixtures/+model-query+)
                       "input" []
                       "remote" {"space" "room/server"}}
                      "detail"
                      {"query" (@! fixtures/+inline-query+)
                       "default_input" ["alpha"]
                       "remote" {"space" "room/server"}}}})
    (model/install node install-opts)
    (model/model-put node "room/server" "entries-screen" server-spec)
    (model/model-put node "room/local" "entries-screen" local-spec)
    (promise/x:promise-catch
     (-> (model/sync node "room/server" payload)
         (promise/x:promise-then
          (fn [_]
            (return
             (model/model-refresh node "room/local" "entries-screen"))))
         (promise/x:promise-then
          (fn [_]
            (repl/notify
             {"remote-space" (xtd/get-in
                              (model/view-get node "room/local" "entries-screen" "list")
                              ["remote" "space"])
              "list-status" (xtd/get-in
                             (model/view-get node "room/local" "entries-screen" "list")
                             ["status"])
              "list-count" (xt/x:len (model/view-val node "room/local" "entries-screen" "list"))
              "detail-input" (model/view-input node "room/local" "entries-screen" "detail")
              "detail-name" (xtd/get-in
                             (model/view-val node "room/local" "entries-screen" "detail")
                             [0 "name"])}))))
     (fn [err]
       (repl/notify err))))
  => {"remote-space" "room/server"
      "list-status" "ready"
      "list-count" 2
      "detail-input" ["alpha"]
      "detail-name" "alpha"})
