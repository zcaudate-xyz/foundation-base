(ns xt.db.substrate-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config {:dbname "test-scratch"}
   :require [[postgres.sample.scratch-v0 :as v0]]})

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-view :as event-view]
             [xt.substrate :as event-node]
             [xt.substrate.base-page :as base-page]
             [js.lib.driver-postgres :as js-postgres]
             [xt.db.substrate :as db-helper]
             [postgres.sample.scratch-v0.route-entries :as entries]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.substrate/call-db-handler :added "4.1"}
(fact "creates the db helper for sql"

  (notify/wait-on :js
    (-> (event-node/node-create
         {"services"
          {"db/primary"
           {"database" "test-scratch"}}
          "handlers"
          {"db/fn.primary"
           {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
            "meta" {"kind" "request"}}}})
        (event-node/request nil
                            "db/fn.primary"
                            [{"template" entries/ping
                              "args" []}]
                            {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => "pong")

^{:refer xt.db.substrate/call-view-request :added "4.1"}
(fact "issues a request through a page view handler"

  (notify/wait-on :js
    (var node
         (event-node/node-create
          {"services"
           {"db/primary"
            {"database" "test-scratch"}}
           "handlers"
           {"db/fn.primary"
            {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
             "meta" {"kind" "request"}}}}))
    (base-page/add-model-attach node
                                nil
                                "page"
                                {"ping" {"handler" (db-helper/call-view-request entries/ping
                                                                                "db/fn.primary"
                                                                                "action/ping.primary"
                                                                                {})
                                         "defaultArgs" []}})
    (var [_model view] (base-page/view-ensure node nil "page" "ping"))
    (repl/notify view))
  => (contains-in
      {"output"
       {"elapsed" nil, "current" nil, "type" "output", "updated" nil},
       "remote"
       {"elapsed" nil, "current" nil, "type" "remote", "updated" nil},
       "::" "event.view",
       "pipeline" {"remote" {}, "main" {}, "sync" {}},
       "input" {"current" {"data" []}, "updated" number?},
       "options" {},
       "listeners"
       {"@/page"
        {"pred" nil,
         "meta" {"listener/id" "@/page", "listener/type" "view"}}}})
  
  (notify/wait-on :js
    (var node
         (event-node/node-create
          {"services"
           {"db/primary"
            {"database" "test-scratch"}}
           "handlers"
           {"db/fn.primary"
            {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
             "meta" {"kind" "request"}}}}))
    (base-page/add-model-attach node
                                nil
                                "page"
                                {"ping" {"handler" (db-helper/call-view-request entries/ping
                                                                                "db/fn.primary"
                                                                                "action/ping.primary"
                                                                                {})
                                         "defaultArgs" []}})
    (-> (event-node/page-view-update node nil "page" "ping" {})
        (promise/x:promise-then
         (fn [out]
           (var [_model view] (base-page/view-ensure node nil "page" "ping"))
           (repl/notify {:out out
                         :value (event-view/get-current view nil)})))))
  => {"value" "pong",
      "out" {"path" ["page" "ping"], "post" [false], "::" "view.run", "main" [true "pong"], "pre" [false]}}
  
  (notify/wait-on :js
    (var node
         (event-node/node-create
          {"services"
           {"db/primary"
            {"database" "test-scratch"}}
           "handlers"
           {"db/fn.primary"
            {"fn"   (db-helper/call-db-handler js-postgres/driver "db/primary")
             "meta" {"kind" "request"}}}}))
    (base-page/add-model-attach node
                                nil
                                "page"
                                {"ping" {"handler" (db-helper/call-view-request entries/ping
                                                                                "db/fn.primary"
                                                                                "action/ping.primary"
                                                                                {})
                                         "defaultArgs" []}})
    (-> (event-node/page-view-update node nil "page" "ping" {})
        (promise/x:promise-then
         (fn [_]
           (repl/notify (base-page/view-ensure node nil "page" "ping"))))))
  => (contains-in
      [{"name" "page",
        "views"
        {"ping"
         {"output"
          {"tag" "main",
           "current" "pong",
           "type" "output",
           "updated" number?},
          "remote"
          {"elapsed" nil, "current" nil, "type" "remote", "updated" nil},
          "::" "event.view",
          "pipeline" {"remote" {}, "main" {}, "sync" {}},
          "input" {"current" {"data" []}, "updated" number?},
          "options" {},
          "listeners"
          {"@/page"
           {"pred" nil,
            "meta" {"listener/id" "@/page", "listener/type" "view"}}}}},
        "deps" {},
        "throttle" {"queued" {}, "active" {}}}
       {"output"
        {"tag" "main",
         "current" "pong",
         "type" "output",
         "updated" number?},
        "remote"
        {"elapsed" nil, "current" nil, "type" "remote", "updated" nil},
        "::" "event.view",
        "pipeline" {"remote" {}, "main" {}, "sync" {}},
        "input" {"current" {"data" []}, "updated" number?},
        "options" {},
        "listeners"
        {"@/page"
         {"pred" nil,
          "meta" {"listener/id" "@/page", "listener/type" "view"}}}}]))
