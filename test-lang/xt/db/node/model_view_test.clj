(ns xt.db.node.model-view-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.node.model-view :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.model-view/normalize-sources :added "4.1"}
(fact "normalizes shared primary and caching sources"

  (!.js
    (var out
         (model/normalize-sources
          {"primary" {"kind" "sqlite"}}
          {"cache_alt" {"sync_from" "primary"}}))
    [(. out ["primary"] ["kind"])
     (. out ["caching"] ["sync_from"])
     (. out ["cache_alt"] ["sync_from"])])
  => ["sqlite" "primary" "primary"])

^{:refer xt.db.node.model-view/normalize-view-source :added "4.1"}
(fact "normalizes view source declarations"
  (!.js
    [(model/normalize-view-source {"source" "primary"})
     (model/normalize-view-source {"use" {"source" "archive"}})
     (model/normalize-view-source {})])
  => ["primary" "archive" "caching"])

^{:refer xt.db.node.model-view/base-state :added "4.1"}
(fact "creates the base view state with primary and caching sources"

  (!.js
    (var out
         (model/base-state {"schema" {"Task" {}}
                            "sources" {"primary" {"kind" "postgres"}
                                       "caching" {"kind" "sqlite"}}}))
    [(. out ["::"])
     (. (. out ["sources"]) ["primary"] ["kind"])
     (. (. out ["sources"]) ["caching"] ["kind"])
     (xt/x:obj-keys (. out ["models"]))])
  => ["xt.db.state"
      "postgres"
      "sqlite"
      []])

^{:refer xt.db.node.model-view/put-model :added "4.1"}
(fact "normalizes model sources once and lets views declare a stable source role"

  (!.js
    (var out (model/base-state {}))
    (model/put-model out
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}}
                      "views" {"list" {"query" {"table" "Task"}}
                               "detail" {"query" {"table" "Task"}
                                         "default_input" ["alpha"]
                                         "source" "primary"}}})
    [(xt/x:obj-keys (. (. out ["models"]) ["entries"] ["sources"]))
     (xtd/get-in out ["models" "entries" "views" "detail" "input"])
     (xtd/get-in out ["models" "entries" "views" "detail" "source"])])
  => [["primary" "caching"]
      ["alpha"]
      "primary"])

^{:refer xt.db.node.model-view/sync-source :added "4.1"}
(fact "syncs caching from primary through the stable model source binding"

  (!.js
    (var out (model/base-state {}))
    (model/put-model out
                     "entries"
                     {"views" {"list" {"query" {"table" "Task"}}}})
    (model/set-source-data out
                           "entries"
                           "primary"
                           [{"id" "t1" "name" "alpha"}])
    (model/sync-source out "entries" "caching")
    [(xtd/get-in out ["models" "entries" "sources" "primary" "data" 0 "name"])
     (xtd/get-in out ["models" "entries" "sources" "caching" "data" 0 "name"])
     (xt/x:is-number? (xtd/get-in out ["models" "entries" "sources" "caching" "updated_at"]))])
  => ["alpha" "alpha" true])

^{:refer xt.db.node.model-view/model-put :added "4.1"}
(fact "stores a clean view model with structural primary and caching sources"

  (!.js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries-screen"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"query" {"table" "Task"}
                                       "source" "caching"}
                               "detail" {"query" {"table" "Task"}
                                         "source" "primary"
                                         "default_input" ["alpha"]}}})
    [(xt/x:obj-keys (. (model/model-get node "screen/admin" "entries-screen") ["sources"]))
     (. (model/view-get node "screen/admin" "entries-screen" "list") ["source"])
     (xt/x:obj-keys (. (. node ["spaces"]) ["screen/admin"] ["state"] ["models"]))])
  => [["primary" "caching"]
     "caching"
     ["entries-screen"]])

^{:refer xt.db.node/create :added "4.1"}
(fact "creates a node from a declarative snake_case manifest through xt.db.node"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-c"
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
              {"list" {"query" (@! fixtures/+model-query+)
                       "source" "caching"}
               "detail" {"query" (@! fixtures/+inline-query+)
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"node-id" (. node ["id"])
             "space-ids" (xt/x:obj-keys (. node ["spaces"]))
             "source-ids" (xt/x:obj-keys
                           (. (node/model-get node "screen/admin" "entries-screen") ["sources"]))
             "list-source" (. (node/view-get node "screen/admin" "entries-screen" "list") ["source"])})))))
  => {"node-id" "node-c"
      "space-ids" ["screen/admin"]
      "source-ids" ["primary" "caching"]
      "list-source" "caching"})

^{:refer xt.db.node/create.wrapper :added "4.1"}
(fact "creates a live sqlite source through kind + config and refreshes a real view from it"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"views"
              {"list" {"query" (@! fixtures/+model-query+)
                       "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (return
            (promise/x:promise-then
             (node/view-refresh node "screen/admin" "entries-screen" "list")
             (fn [result]
               (repl/notify
                {"node-id" (. node ["id"])
                 "source-kind" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "primary")
                                ["dbtype"])
                 "row-count" (xt/x:len (. result ["value"]))
                 "first-name" (xtd/get-in (. result ["value"]) [0 "name"])}))))))))
  => {"node-id" "node-live"
      "source-kind" "db.sql"
      "row-count" 2
      "first-name" "alpha"})

^{:refer xt.db.node.model-view/view-refresh.resolver :added "4.1"}
(fact "refreshes a live sqlite view from a resolver with type db/query"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live-resolver"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"views"
              {"list" {"resolver" {"type" "db/query"
                                   "table" "Entry"
                                   "select_entry" {"input" []
                                                   "view" {"query" {"__deleted__" false}}}
                                   "return_entry" {"input" [{"symbol" "i_entry_id"
                                                             "type" "text"}]
                                                   "view" {"query" ["id"
                                                                    "name"
                                                                    "tags"
                                                                    "time_created"
                                                                    "time_updated"]}}}
                       "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (return
            (promise/x:promise-then
             (node/view-refresh node "screen/admin" "entries-screen" "list")
             (fn [result]
               (repl/notify
                {"row-count" (xt/x:len (. result ["value"]))
                 "first-name" (xtd/get-in (. result ["value"]) [0 "name"])
                 "status" (. result ["status"])}))))))))
  => {"row-count" 2
      "first-name" "alpha"
      "status" "ready"})

^{:refer xt.db.node.model-view/query.wrapper :added "4.1"}
(fact "queries and refreshes a registered resolver-backed view through request handlers"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live-query"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"views"
              {"list" {"resolver" {"type" "db/query"
                                   "table" "Entry"
                                   "select_entry" {"input" []
                                                   "view" {"query" {"__deleted__" false}}}
                                   "return_entry" {"input" [{"symbol" "i_entry_id"
                                                             "type" "text"}]
                                                   "view" {"query" ["id"
                                                                    "name"
                                                                    "tags"
                                                                    "time_created"
                                                                    "time_updated"]}}}
                       "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (return
            (promise/x:promise-then
             (node/query
              node
              "screen/admin"
              {"view" {"model_id" "entries-screen"
                       "view_id" "list"}})
             (fn [result]
               (return
                (promise/x:promise-then
                 (node/query-refresh
                  node
                  "screen/admin"
                  {"query_key" (. result ["query_key"])})
                 (fn [refresh]
                   (repl/notify
                    {"row-count" (xt/x:len (. result ["value"]))
                     "refresh-count" (xt/x:len (. refresh ["value"]))
                     "status" (. refresh ["status"])})))))))))))
  => {"row-count" 2
      "refresh-count" 2
      "status" "ready"})

^{:refer xt.db.node.model-view/sync.wrapper :added "4.1"}
(fact "applies db/sync and db/remove through request handlers on model sources"

  (notify/wait-on :js
    (var local-node (event-node/node-create {"id" "node-live-events"}))
    (model/install local-node nil)
    (model/model-put
     local-node
     "screen/admin"
     "entries-screen"
     {"views"
      {"list" {"source" "primary"}}})
    (model/source-put
     local-node
     "screen/admin"
     "entries-screen"
     "primary"
     [{"id" "00000000-0000-0000-0000-0000000000c1"
       "name" "alpha"}
      {"id" "00000000-0000-0000-0000-0000000000c2"
       "name" "beta"}])
    (-> (node/sync
         local-node
         "screen/admin"
         {"db/sync"
          {"Entry" [{"id" "00000000-0000-0000-0000-0000000000c3"
                     "name" "gamma"}]}})
        (promise/x:promise-then
         (fn [_]
           (return
            (promise/x:promise-then
             (node/remove
              local-node
              "screen/admin"
              {"db/remove"
               {"Entry" ["00000000-0000-0000-0000-0000000000c2"]}})
             (fn [_]
               (return
                (promise/x:promise-then
                 (node/view-refresh local-node "screen/admin" "entries-screen" "list")
                 (fn [result]
                   (repl/notify
                    {"row-count" (xt/x:len (. result ["value"]))
                     "names" [(xtd/get-in (. result ["value"]) [0 "name"])
                              (xtd/get-in (. result ["value"]) [1 "name"])]
                     "status" (. (node/view-get local-node "screen/admin" "entries-screen" "list")
                                 ["status"])})))))))))))
  => {"row-count" 2
      "names" ["alpha" "gamma"]
      "status" "ready"})

^{:refer xt.db.node.model-view/view-refresh.remote :added "4.1"}
(fact "refreshes a local view from a remote registered view in another space"

  (notify/wait-on :js
    (var local-node (event-node/node-create {"id" "node-remote"}))
    (model/install local-node nil)
    (model/model-put
     local-node
     "screen/server"
     "entries-server"
     {"views"
      {"list-remote" {"source" "primary"}}})
    (model/source-put
     local-node
     "screen/server"
     "entries-server"
     "primary"
     [{"id" "00000000-0000-0000-0000-0000000000d1"
       "name" "alpha"}
      {"id" "00000000-0000-0000-0000-0000000000d2"
       "name" "beta"}])
    (model/model-put
     local-node
     "screen/client"
     "entries-client"
     {"views"
      {"list-local"
       {"source" "primary"
        "remote" {"space" "screen/server"
                  "model_id" "entries-server"
                  "view_id" "list-remote"}}}})
    (-> (node/view-refresh local-node "screen/client" "entries-client" "list-local")
        (promise/x:promise-then
         (fn [result]
           (repl/notify
            {"row-count" (xt/x:len (. result ["value"]))
             "first-name" (xtd/get-in (. result ["value"]) [0 "name"])
             "status" (. result ["status"])
             "source" (. result ["source"])})))))
  => {"row-count" 2
      "first-name" "alpha"
      "status" "ready"
      "source" "primary"})

^{:refer xt.db.node.model-view/source-refresh :added "4.1"}
(fact "syncs a live primary sqlite source into a live caching sqlite source"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live-sync"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}
                 "caching" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true}}}}
          "spaces"
          {"screen/admin"
           {"models"
            {"entries-screen"
             {"sources"
              {"primary" {"query" (@! fixtures/+model-query+)}
               "caching" {"query" (@! fixtures/+model-query+)}}
              "views"
              {"list" {"query" (@! fixtures/+model-query+)
                       "source" "caching"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (return
            (promise/x:promise-then
             (node/model-sync node "screen/admin" "entries-screen")
             (fn [_]
               (return
                (promise/x:promise-then
                 (node/view-refresh node "screen/admin" "entries-screen" "list")
                 (fn [result]
                   (repl/notify
                    {"row-count" (xt/x:len (. result ["value"]))
                     "cached-first" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "caching")
                                     ["data" 0 "name"])
                     "list-source" (. result ["source"])})))))))))))
   => {"row-count" 2
       "cached-first" "alpha"
       "list-source" "caching"})

^{:refer xt.db.node.model-view/model-sync :added "4.1"}
(fact "syncs caching from primary and refreshes views from their declared source roles"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-b"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries-screen"
                     {"views" {"list" {"query" {"table" "Task"}
                                       "source" "caching"}
                               "detail" {"query" {"table" "Task"}
                                         "source" "primary"
                                         "default_input" ["alpha"]}}})
    (model/source-put node
                      "screen/admin"
                      "entries-screen"
                      "primary"
                      [{"id" "t1" "name" "alpha"}])
    (-> (model/model-sync node "screen/admin" "entries-screen")
         (promise/x:promise-then
          (fn [_]
            (return
             (promise/x:promise-all
              [(model/view-refresh node "screen/admin" "entries-screen" "list")
               (model/view-refresh node "screen/admin" "entries-screen" "detail")]))))
         (promise/x:promise-then
          (fn [_]
            (repl/notify
             {"caching-name" (xtd/get-in
                             (model/source-get node "screen/admin" "entries-screen" "caching")
                             ["data" 0 "name"])
              "list-source" (. (model/view-get node "screen/admin" "entries-screen" "list") ["source"])
              "detail-name" (xtd/get-in
                            (model/view-val node "screen/admin" "entries-screen" "detail")
                            [0 "name"])})))))
  => {"caching-name" "alpha"
      "list-source" "caching"
      "detail-name" "alpha"})
