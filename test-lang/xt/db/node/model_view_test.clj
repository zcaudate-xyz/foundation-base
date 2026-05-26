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
                      "views" {"list" {"resolver" {"type" "db/query" "table" "Task"}}
                               "detail" {"resolver" {"type" "db/query" "table" "Task"}
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
                     {"views" {"list" {"resolver" {"type" "db/query" "table" "Task"}}}})
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
                      "views" {"list" {"resolver" {"type" "db/query" "table" "Task"}
                                       "source" "caching"}
                               "detail" {"resolver" {"type" "db/query" "table" "Task"}
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
                       "source" "caching"}
               "detail" {"resolver" {"type" "db/query"
                                     "table" "Entry"
                                     "select_entry" {"input" [{"symbol" "i_name" "type" "text"}]
                                                     "view" {"query" {"name" "{{i_name}}"
                                                                      "__deleted__" false}}}
                                     "select_args" ["alpha"]
                                     "return_entry" {"input" [{"symbol" "i_entry_id" "type" "text"}]
                                                     "view" {"query" ["name" "tags"]}}}
                         "source" "primary"}}}}}}})
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            {"node_id" (. node ["id"])
             "space_ids" (xt/x:obj-keys (. node ["spaces"]))
             "source_ids" (xt/x:obj-keys
                           (. (node/model-get node "screen/admin" "entries-screen") ["sources"]))
             "list_source" (. (node/view-get node "screen/admin" "entries-screen" "list") ["source"])})))))
  => {"node_id" "node-c"
      "space_ids" ["screen/admin"]
      "source_ids" ["primary" "caching"]
      "list_source" "caching"})

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
                {"node_id" (. node ["id"])
                 "source_kind" (xtd/get-in
                                (node/source-get node "screen/admin" "entries-screen" "primary")
                                ["dbtype"])
                 "row_count" (xt/x:len (. result ["value"]))
                 "first_name" (xtd/get-in (. result ["value"]) [0 "name"])}))))))))
  => {"node_id" "node-live"
      "source_kind" "db.sql"
      "row_count" 2
      "first_name" "alpha"})

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
                {"row_count" (xt/x:len (. result ["value"]))
                 "first_name" (xtd/get-in (. result ["value"]) [0 "name"])
                 "status" (. result ["status"])}))))))))
  => {"row_count" 2
      "first_name" "alpha"
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
                    {"row_count" (xt/x:len (. result ["value"]))
                     "refresh_count" (xt/x:len (. refresh ["value"]))
                     "status" (. refresh ["status"])})))))))))))
  => {"row_count" 2
      "refresh_count" 2
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
                    {"row_count" (xt/x:len (. result ["value"]))
                     "names" [(xtd/get-in (. result ["value"]) [0 "name"])
                              (xtd/get-in (. result ["value"]) [1 "name"])]
                     "status" (. (node/view-get local-node "screen/admin" "entries-screen" "list")
                                 ["status"])})))))))))))
  => {"row_count" 2
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
            {"row_count" (xt/x:len (. result ["value"]))
             "first_name" (xtd/get-in (. result ["value"]) [0 "name"])
             "status" (. result ["status"])
             "source" (. result ["source"])})))))
  => {"row_count" 2
      "first_name" "alpha"
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
              {"primary" {"resolver" {"type" "db/query"
                                      "table" "Entry"
                                      "select_entry" {"input" []
                                                      "view" {"query" {"__deleted__" false}}}
                                      "return_entry" {"input" [{"symbol" "i_entry_id"
                                                                "type" "text"}]
                                                      "view" {"query" ["id"
                                                                       "name"
                                                                       "tags"
                                                                       "time_created"
                                                                       "time_updated"]}}}}
               "caching" {"resolver" {"type" "db/query"
                                      "table" "Entry"
                                      "select_entry" {"input" []
                                                      "view" {"query" {"__deleted__" false}}}
                                      "return_entry" {"input" [{"symbol" "i_entry_id"
                                                                "type" "text"}]
                                                      "view" {"query" ["id"
                                                                       "name"
                                                                       "tags"
                                                                       "time_created"
                                                                       "time_updated"]}}}}}
              "views"
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
                    {"row_count" (xt/x:len (. result ["value"]))
                     "cached_first" (xtd/get-in
                                     (node/source-get node "screen/admin" "entries-screen" "caching")
                                     ["data" 0 "name"])
                     "list_source" (. result ["source"])})))))))))))
   => {"row_count" 2
       "cached_first" "alpha"
       "list_source" "caching"})

^{:refer xt.db.node.model-view/model-sync :added "4.1"}
(fact "syncs caching from primary and refreshes views from their declared source roles"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-b"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries-screen"
                     {"views" {"list" {"resolver" {"type" "db/query" "table" "Task"}
                                       "source" "caching"}
                               "detail" {"resolver" {"type" "db/query" "table" "Task"}
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
             {"caching_name" (xtd/get-in
                             (model/source-get node "screen/admin" "entries-screen" "caching")
                             ["data" 0 "name"])
              "list_source" (. (model/view-get node "screen/admin" "entries-screen" "list") ["source"])
              "detail_name" (xtd/get-in
                            (model/view-val node "screen/admin" "entries-screen" "detail")
                            [0 "name"])})))))
  => {"caching_name" "alpha"
      "list_source" "caching"
      "detail_name" "alpha"})


^{:refer xt.db.node.model-view/source-base :added "4.1"}
(fact "creates the base structural shape for a source role"
  (!.js
    [(model/source-base "primary")
     (model/source-base "caching")])
  => [{"id" "primary"
       "data" []
       "updated_at" nil
       "synced_at" nil
       "sync_from" nil}
      {"id" "caching"
       "data" []
       "updated_at" nil
       "synced_at" nil
       "sync_from" "primary"}])

^{:refer xt.db.node.model-view/normalize-source :added "4.1"}
(fact "merges current and incoming source state"
  (!.js
    (model/normalize-source
     "primary"
     {"config" {"filename" ":memory:"}
      "setup" {"schema" true}
      "sync_from" nil}
     {"kind" "sqlite"
      "config" {"driver" "sqlite.driver"}
      "setup" {"seed" [{"id" "e1"}]}}))
  => {"id" "primary"
      "data" []
      "updated_at" nil
      "synced_at" nil
      "sync_from" nil
      "kind" "sqlite"
      "config" {"driver" "sqlite.driver"}
      "setup" {"seed" [{"id" "e1"}]}})

^{:refer xt.db.node.model-view/normalize-dep :added "4.1"}
(fact "normalizes dependency declarations into [model-id view-id]"
  (!.js
    [(model/normalize-dep "task" ["detail"])
     (model/normalize-dep "task" ["other" "list"])
     (model/normalize-dep "task" "summary")
     (model/normalize-dep "task" {"model" "remote" "view" "list"})
     (model/normalize-dep "task" nil)])
  => [["task" "detail"]
      ["other" "list"]
      ["task" "summary"]
      ["remote" "list"]
      nil])

^{:refer xt.db.node.model-view/get-view-deps :added "4.1"}
(fact "returns normalized dependencies for a view"
  (!.js
    (model/get-view-deps
     "task"
     {"deps" ["detail" ["other" "list"] {"id" "summary"} nil]}))
  => [["task" "detail"]
      ["other" "list"]
      ["task" "summary"]])

^{:refer xt.db.node.model-view/get-model-deps :added "4.1"}
(fact "indexes dependent views by source model and view"
  (!.js
    (model/get-model-deps
     "task"
     {"detail" {"deps" ["list"]}
      "summary" {"deps" [["other" "remote"]]}}))
  => {"task" {"list" {"detail" true}}
      "other" {"remote" {"summary" true}}})

^{:refer xt.db.node.model-view/get-unknown-deps :added "4.1"}
(fact "finds dependency paths that are not present in state"
  (!.js
    (model/get-unknown-deps
     {"models" {"other" {"views" {"remote" {}}}}}
     "task"
     {"detail" {}}
     {"task" {"detail" true
              "missing" true}
      "other" {"remote" true
               "gone" true}
      "ghost" {"view" true}}))
  => [["task" "missing"]
      ["other" "gone"]
      ["ghost" "view"]])

^{:refer xt.db.node.model-view/model-views :added "4.1"}
(fact "accepts either a model spec or a direct views map"
  (!.js
    [(xt/x:obj-keys
      (model/model-views {"views" {"list" {} "detail" {}}}))
     (xt/x:obj-keys
      (model/model-views {"list" {} "detail" {}}))])
  => [["list" "detail"]
      ["list" "detail"]])

^{:refer xt.db.node.model-view/normalize-view :added "4.1"}
(fact "normalizes view defaults and strips use/default_input helpers"
  (!.js
    (model/normalize-view
     "detail"
     {"default_input" ["alpha"]
      "use" {"source" "primary"}
      "meta" {"role" "test"}
      "resolver" {"type" "db/query" "table" "Task"}}))
  => {"id" "detail"
      "source" "primary"
      "input" ["alpha"]
      "pending" false
      "status" "idle"
      "error" nil
      "meta" {"role" "test"}
      "tables" {}
      "query_key" nil
      "resolver" {"type" "db/query" "table" "Task"}})

^{:refer xt.db.node.model-view/normalize-model :added "4.1"}
(fact "normalizes model sources, views, deps, and unknown deps"
  (!.js
    (var out
         (model/normalize-model
          {"primary" {"kind" "postgres"}}
          "entries"
          {"views" {"list" {"resolver" {"type" "db/query" "table" "Task"}}
                    "detail" {"deps" ["list"]
                              "default_input" ["alpha"]}}}))
    {"source_ids" (xt/x:obj-keys (. out ["sources"]))
     "view_ids" (xt/x:obj-keys (. out ["views"]))
     "detail_input" (. (. out ["views"] ["detail"]) ["input"])
     "deps" (. out ["deps"])
     "unknown_deps" (. out ["unknown_deps"])})
  => {"source_ids" ["primary" "caching"]
      "view_ids" ["list" "detail"]
      "detail_input" ["alpha"]
      "deps" {}
      "unknown_deps" []})

^{:refer xt.db.node.model-view/get-model :added "4.1"}
(fact "gets a registered model from state"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    [(xt/x:not-nil? (model/get-model state "entries"))
     (model/get-model state "missing")])
  => [true nil])

^{:refer xt.db.node.model-view/ensure-model :added "4.1"}
(fact "returns a registered model"
  (!.js
    (xt/x:not-nil?
     (model/ensure-model
      {"models" {"entries" {"id" "entries"}}}
      "entries")))
  => true)

^{:refer xt.db.node.model-view/get-view :added "4.1"}
(fact "gets a registered view from state"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    [(xt/x:not-nil? (model/get-view state "entries" "list"))
     (model/get-view state "entries" "detail")])
  => [true nil])

^{:refer xt.db.node.model-view/ensure-view :added "4.1"}
(fact "returns a registered view"
  (!.js
    (xt/x:not-nil?
     (model/ensure-view
      {"models" {"entries" {"views" {"list" {"id" "list"}}}}}
      "entries"
      "list")))
  => true)

^{:refer xt.db.node.model-view/rebuild-model :added "4.1"}
(fact "recomputes dependency indexes and missing links"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "other" {"views" {"remote" {}}})
    (model/put-model state "entries" {"views" {"detail" {"deps" ["list" ["other" "remote"] ["ghost" "gone"]]}
                                               "list" {}}})
    (var out (model/rebuild-model state "entries"))
    {"deps" (. out ["deps"])
     "unknown_deps" (. out ["unknown_deps"])})
  => {"deps" {"entries" {"list" {"detail" true}}
              "other" {"remote" {"detail" true}}
              "ghost" {"gone" {"detail" true}}}
      "unknown_deps" [["ghost" "gone"]]})

^{:refer xt.db.node.model-view/put-view :added "4.1"}
(fact "adds a normalized view onto an existing model"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    (model/put-view state "entries" "detail" {"deps" ["list"]
                                              "source" "primary"})
    {"view_ids" (xt/x:obj-keys (. (. state ["models"]) ["entries"] ["views"]))
     "deps" (xtd/get-in state ["models" "entries" "deps"])})
  => {"view_ids" ["list" "detail"]
      "deps" {"entries" {"list" {"detail" true}}}})

^{:refer xt.db.node.model-view/set-view-input :added "4.1"}
(fact "updates a view input vector"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"detail" {}}})
    (. (model/set-view-input state "entries" "detail" ["alpha"]) ["input"]))
  => ["alpha"])

^{:refer xt.db.node.model-view/set-view-ready :added "4.1"}
(fact "marks a view ready and clears errors"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"detail" {}}})
    (model/set-view-error state "entries" "detail" {"message" "boom"})
    (var out (model/set-view-ready state "entries" "detail"))
    [(. out ["status"])
     (. out ["pending"])
     (xt/x:is-number? (. out ["updated_at"]))
     (. out ["error"])])
  => ["ready" false true nil])

^{:refer xt.db.node.model-view/set-view-error :added "4.1"}
(fact "marks a view errored"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"detail" {}}})
    (var out (model/set-view-error state "entries" "detail" {"message" "boom"}))
    [(. out ["status"])
     (. out ["pending"])
     (xt/x:is-number? (. out ["updated_at"]))
     (. out ["error"] ["message"])])
  => ["error" false true "boom"])

^{:refer xt.db.node.model-view/set-view-value :added "4.1"}
(fact "stores the current view value and source"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    (var out (model/set-view-value state "entries" "list" "primary" [{"id" "t1"}]))
    [(. out ["source"])
     (. out ["status"])
     (xt/x:len (. out ["value"]))])
  => ["primary" "ready" 1])

^{:refer xt.db.node.model-view/get-source :added "4.1"}
(fact "gets a source binding from a model"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}
                                      "sources" {"primary" {"kind" "sqlite"}}})
    [(. (model/get-source state "entries" "primary") ["kind"])
     (model/get-source state "entries" "archive")])
  => ["sqlite" nil])

^{:refer xt.db.node.model-view/ensure-source :added "4.1"}
(fact "returns a registered source binding"
  (!.js
    (xt/x:not-nil?
     (model/ensure-source
      {"models" {"entries" {"sources" {"primary" {"id" "primary"}}}}}
      "entries"
      "primary")))
  => true)

^{:refer xt.db.node.model-view/set-source-data :added "4.1"}
(fact "stores cloned source data and sets updated_at"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    (var out (model/set-source-data state "entries" "primary" [{"id" "t1" "name" "alpha"}]))
    [(xtd/get-in out ["data" 0 "name"])
     (xt/x:is-number? (. out ["updated_at"]))])
  => ["alpha" true])

^{:refer xt.db.node.model-view/sync-model-sources :added "4.1"}
(fact "synchronizes every source with a sync_from binding"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}
                                      "sources" {"primary" {"kind" "cache"}
                                                 "archive" {"sync_from" "primary"}}})
    (model/set-source-data state "entries" "primary" [{"id" "t1" "name" "alpha"}])
    (var out (model/sync-model-sources state "entries"))
    {"synced" (xt/x:obj-keys out)
     "archive_name" (xtd/get-in state ["models" "entries" "sources" "archive" "data" 0 "name"])})
  => {"synced" ["caching" "archive"]
      "archive_name" "alpha"})

^{:refer xt.db.node.model-view/clear-state :added "4.1"}
(fact "clears cached queries and resets views to idle"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {} "detail" {}}})
    (xtd/set-in state ["queries" "q1"] {"id" "q1"})
    (model/set-view-error state "entries" "list" {"message" "boom"})
    (model/set-view-value state "entries" "detail" "primary" [{"id" "t1"}])
    (model/clear-state state)
    {"queries" (xt/x:obj-keys (. state ["queries"]))
     "list_status" (xtd/get-in state ["models" "entries" "views" "list" "status"])
     "detail_status" (xtd/get-in state ["models" "entries" "views" "detail" "status"])
     "list_error" (xtd/get-in state ["models" "entries" "views" "list" "error"])})
  => {"queries" []
      "list_status" "idle"
      "detail_status" "idle"
      "list_error" nil})

^{:refer xt.db.node.model-view/get-view-dependents :added "4.1"}
(fact "finds all views that depend on a given view"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}
                                               "detail" {"deps" ["list"]}}})
    (model/put-model state "other" {"views" {"mirror" {"deps" [["entries" "list"]]}}})
    (model/get-view-dependents state "entries" "list"))
  => {"entries" ["detail"]
      "other" ["mirror"]})

^{:refer xt.db.node.model-view/get-model-dependents :added "4.1"}
(fact "finds models that depend on a source model"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {}}})
    (model/put-model state "other" {"views" {"mirror" {"deps" [["entries" "list"]]}}})
    (model/get-model-dependents state "entries"))
  => {"other" true})

^{:refer xt.db.node.model-view/snapshot-state :added "4.1"}
(fact "returns a public snapshot of state"
  (!.js
    (var state (model/base-state {"schema" {"Task" {}}
                                  "lookup" {"Task" {"table" "tasks"}}}))
    (model/put-model state "entries" {"views" {"list" {}}})
    (xt/x:obj-keys (model/snapshot-state state)))
  => ["schema" "lookup" "sources" "queries" "models"])

^{:refer xt.db.node.model-view/not-implemented :added "4.1"}
(fact "returns a standard error payload"
  (!.js
    (model/not-implemented "xt.db/test" {"id" "p1"}))
  => {"status" "error"
      "tag" "xt.db/test"
      "data" {"id" "p1"}})

^{:refer xt.db.node.model-view/view-refresh-result :added "4.1"}
(fact "returns the public refresh shape from a view"
  (!.js
    (model/view-refresh-result
     {"query_key" "q-1"
      "source" "primary"
      "value" [{"id" "t1"}]
      "status" "ready"}))
  => {"query_key" "q-1"
      "source" "primary"
      "value" [{"id" "t1"}]
      "status" "ready"})

^{:refer xt.db.node.model-view/view-remote-spec :added "4.1"}
(fact "returns a configured remote view spec only when actionable"
  (!.js
    [(model/view-remote-spec {"remote" {"space" "screen/server"
                                        "model_id" "entries"
                                        "view_id" "list"}})
     (model/view-remote-spec {"remote" {"foo" "bar"}})])
  => [{"space" "screen/server"
       "model_id" "entries"
       "view_id" "list"}
      nil])

^{:refer xt.db.node.model-view/normalize-remote :added "4.1"}
(fact "merges state, view, and call-level remote settings"
  (!.js
    (model/normalize-remote
     {"remote" {"space" "screen/default"
                "transport" "ws-default"}
      "opts" {"remote" {"target" "screen/fallback"}}}
     {"model_id" "entries"
      "view_id" "list"}
     {"remote" {"transport" "ws-call"}}))
  => {"space" "screen/default"
      "transport" "ws-call"
      "model_id" "entries"
      "view_id" "list"})

^{:refer xt.db.node.model-view/request-remote :added "4.1"}
(fact "issues a remote request against another node space"
  (notify/wait-on :js
    (var node
         (event-node/node-create
          {"id" "node-remote-request"
           "spaces" {"server" {"state" {}}}
           "handlers"
           {"remote/ping"
            {"fn"
             (fn [space args _request _node]
               (return {"space_id" (. space ["id"])
                        "value" (xtd/get-in (xt/x:first args) ["value"])}))}}}))
    (-> (model/request-remote
         node
         "screen/client"
         {"space" "server"}
         "remote/ping"
         {"value" 42})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"space_id" "server"
      "value" 42})

^{:refer xt.db.node.model-view/apply-remote-events :added "4.1"}
(fact "applies db/sync and db/remove envelopes into local source state"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state
                     "entries"
                     {"views" {"list" {}}
                      "sources" {"primary" {"table" "Entry"}}})
    (model/set-source-data state "entries" "primary"
                           [{"id" "e1" "name" "alpha"}
                            {"id" "e2" "name" "beta"}])
    (model/apply-remote-events
     state
     {"db/sync" {"Entry" [{"id" "e3" "name" "gamma"}]}
      "db/remove" {"Entry" ["e2"]}})
    [(xt/x:len (xtd/get-in state ["models" "entries" "sources" "primary" "data"]))
     (xtd/get-in state ["models" "entries" "sources" "primary" "data" 1 "name"])])
  => [2 "gamma"])

^{:refer xt.db.node.model-view/remote-view-payload :added "4.1"}
(fact "builds a remote query payload from the local view state"
  (!.js
    (model/remote-view-payload
     {"input" ["alpha"]
      "resolver" {"type" "db/query" "table" "Entry"}}
     {"model_id" "remote-entries"
      "view_id" "detail"}
     "entries"
     "list"))
  => {"view" {"model_id" "remote-entries"
              "view_id" "detail"
              "args" ["alpha"]}
      "resolver" {"type" "db/query"
                  "table" "Entry"}})

^{:refer xt.db.node.model-view/find-view-by-query-key :added "4.1"}
(fact "finds a registered view by query key"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {} "detail" {}}})
    (xtd/set-in state ["models" "entries" "views" "detail" "query_key"] "q-42")
    (model/find-view-by-query-key state "q-42"))
  => {"model_id" "entries"
      "view_id" "detail"
      "view" {"id" "detail"
              "source" "caching"
              "input" []
              "pending" false
              "status" "idle"
              "error" nil
              "meta" {}
              "tables" {}
              "query_key" "q-42"}})

^{:refer xt.db.node.model-view/mark-view-stale :added "4.1"}
(fact "marks a single view stale"
  (!.js
    (var out
         (model/mark-view-stale
          {"status" "ready"
           "pending" true
           "error" {"message" "boom"}}))
    [(. out ["status"])
     (. out ["pending"])
     (. out ["error"])
     (xt/x:is-number? (. out ["updated_at"]))])
  => ["stale" false nil true])

^{:refer xt.db.node.model-view/mark-stale-by-tables :added "4.1"}
(fact "marks matching views stale by table or query key"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state "entries" {"views" {"list" {} "detail" {}}})
    (xtd/set-in state ["models" "entries" "views" "list" "tables"] {"Entry" true})
    (xtd/set-in state ["models" "entries" "views" "detail" "query_key"] "q-1")
    (model/mark-stale-by-tables state {"Entry" true})
    [(xtd/get-in state ["models" "entries" "views" "list" "status"])
     (xtd/get-in state ["models" "entries" "views" "detail" "status"])])
  => ["stale" "stale"])

^{:refer xt.db.node.model-view/sync-state-event :added "4.1"}
(fact "applies sync and remove events across matching model sources"
  (!.js
    (var state (model/base-state {}))
    (model/put-model state
                     "entries"
                     {"views" {"list" {}}
                      "sources" {"primary" {"table" "Entry"}}})
    (model/sync-state-event state "add" {"Entry" [{"id" "e1" "name" "alpha"}]})
    (model/sync-state-event state "remove" {"Entry" ["e1"]})
    [(xtd/get-in state ["models" "entries" "views" "list" "status"])
     (xt/x:len (xtd/get-in state ["models" "entries" "sources" "primary" "data"]))])
  => ["idle" 0])

^{:refer xt.db.node.model-view/ensure-space-state :added "4.1"}
(fact "creates xt.db state on demand for a node space"
  (!.js
    (var node (event-node/node-create {"id" "node-space"}))
    (var state (model/ensure-space-state node "screen/admin"))
    [(. state ["::"])
     (xt/x:not-nil? (. (. node ["spaces"]) ["screen/admin"] ["state"]))])
  => ["xt.db.state" true])

^{:refer xt.db.node.model-view/query :added "4.1"}
(fact "issues a structural query through installed handlers"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-query"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/query node "screen/admin" {"view" {"model_id" "entries"
                                                  "view_id" "list"}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"status" (. out ["status"])
             "name" (xtd/get-in (. out ["value"]) [0 "name"])})))))
  => {"status" "ready"
      "name" "alpha"})

^{:refer xt.db.node.model-view/query-refresh :added "4.1"}
(fact "refreshes a cached structural view by query key"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-query-refresh"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (xtd/set-in (. (. node ["spaces"]) ["screen/admin"] ["state"])
                ["models" "entries" "views" "list" "query_key"]
                "q-1")
    (-> (model/query-refresh node "screen/admin" {"query_key" "q-1"})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"status" (. out ["status"])
             "name" (xtd/get-in (. out ["value"]) [0 "name"])})))))
  => {"status" "ready"
      "name" "alpha"})

^{:refer xt.db.node.model-view/sync :added "4.1"}
(fact "issues db/sync through installed handlers"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-sync"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"table" "Entry"}}
                      "views" {"list" {"source" "primary"}}})
    (-> (model/sync node "screen/admin" {"db/sync" {"Entry" [{"id" "e1"
                                                              "name" "alpha"}]}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"tables" (xt/x:obj-keys (. out ["tables"]))
             "name" (xtd/get-in
                     (. (. node ["spaces"]) ["screen/admin"] ["state"])
                     ["models" "entries" "sources" "primary" "data" 0 "name"])})))))
  => {"tables" ["Entry"]
      "name" "alpha"})

^{:refer xt.db.node.model-view/remove :added "4.1"}
(fact "issues db/remove through installed handlers"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-remove"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"table" "Entry"}}
                      "views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}
                       {"id" "e2" "name" "beta"}])
    (-> (model/remove node "screen/admin" {"db/remove" {"Entry" ["e2"]}})
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"tables" (xt/x:obj-keys (. out ["tables"]))
             "count" (xt/x:len
                      (xtd/get-in
                       (. (. node ["spaces"]) ["screen/admin"] ["state"])
                       ["models" "entries" "sources" "primary" "data"]))})))))
  => {"tables" ["Entry"]
      "count" 1})

^{:refer xt.db.node.model-view/clear :added "4.1"}
(fact "clears query caches through installed handlers"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-clear"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"list" {"source" "primary"}}})
    (xtd/set-in (. (. node ["spaces"]) ["screen/admin"] ["state"])
                ["queries" "q-1"]
                {"id" "q-1"})
    (-> (model/clear node "screen/admin")
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"cleared" out
             "queries" (xt/x:obj-keys
                        (. (. node ["spaces"]) ["screen/admin"] ["state"] ["queries"]))})))))
  => {"cleared" true
      "queries" []})

^{:refer xt.db.node.model-view/snapshot :added "4.1"}
(fact "requests the current state snapshot"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-snapshot"}))
    (model/install node {"schema" {"Entry" {}}})
    (model/model-put node "screen/admin" "entries" {"views" {"list" {}}})
    (-> (model/snapshot node "screen/admin")
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"keys" (xt/x:obj-keys out)
             "models" (xt/x:obj-keys (. out ["models"]))})))))
  => {"keys" ["schema" "lookup" "sources" "queries" "models"]
      "models" ["entries"]})

^{:refer xt.db.node.model-view/view-put :added "4.1"}
(fact "registers a view on an installed node space"
  (!.js
    (var node (event-node/node-create {"id" "node-view-put"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"resolver" {"type" "db/query" "table" "Task"}
                                       "source" "caching"}}})
    (model/view-put node
                    "screen/admin"
                    "entries"
                    "detail"
                    {"resolver" {"type" "db/query" "table" "Task"}
                     "source" "primary"
                     "default_input" ["alpha"]})
    [(xt/x:obj-keys (. (model/model-get node "screen/admin" "entries") ["views"]))
     (. (model/view-get node "screen/admin" "entries" "detail") ["source"])])
  => [["list" "detail"]
      "primary"])

^{:refer xt.db.node.model-view/model-get :added "4.1"}
(fact "gets a registered node model"
  (!.js
    (var node (event-node/node-create {"id" "node-model-get"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"resolver" {"type" "db/query" "table" "Task"}
                                       "source" "caching"}}})
    [(xt/x:obj-keys (. (model/model-get node "screen/admin" "entries") ["sources"]))
     (xt/x:obj-keys (. (. node ["spaces"]) ["screen/admin"] ["state"] ["models"]))])
  => [["primary" "caching"]
      ["entries"]])

^{:refer xt.db.node.model-view/view-get :added "4.1"}
(fact "gets a registered node view"
  (!.js
    (var node (event-node/node-create {"id" "node-view-get"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "postgres"}
                                 "caching" {"kind" "sqlite"}}
                      "views" {"list" {"resolver" {"type" "db/query" "table" "Task"}
                                       "source" "caching"}}})
    (. (model/view-get node "screen/admin" "entries" "list") ["source"]))
  => "caching")

^{:refer xt.db.node.model-view/view-val :added "4.1"}
(fact "gets the current node view value"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-view-val"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/view-refresh node "screen/admin" "entries" "list")
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in (model/view-val node "screen/admin" "entries" "list")
                        [0 "name"]))))))
  => "alpha")

^{:refer xt.db.node.model-view/view-input :added "4.1"}
(fact "gets the current node view input"
  (!.js
    (var node (event-node/node-create {"id" "node-view-input"}))
    (model/install node {"sources" {"primary" {"kind" "postgres"}
                                    "caching" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"detail" {"resolver" {"type" "db/query" "table" "Task"}
                                         "source" "primary"
                                         "default_input" ["alpha"]}}})
    (model/view-input node "screen/admin" "entries" "detail"))
  => ["alpha"])

^{:refer xt.db.node.model-view/view-pending :added "4.1"}
(fact "gets the current node view pending flag"
  (!.js
    (var node (event-node/node-create {"id" "node-view-pending"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (model/view-pending node "screen/admin" "entries" "list"))
  => false)

^{:refer xt.db.node.model-view/view-error :added "4.1"}
(fact "gets the current node view error"
  (!.js
    (var node (event-node/node-create {"id" "node-view-error"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (xtd/set-in (. (. node ["spaces"]) ["screen/admin"] ["state"])
                ["models" "entries" "views" "list" "error"]
                {"message" "boom"})
    (. (model/view-error node "screen/admin" "entries" "list") ["message"]))
  => "boom")

^{:refer xt.db.node.model-view/view-dependents :added "4.1"}
(fact "gets dependent views for a node view"
  (!.js
    (var node (event-node/node-create {"id" "node-view-dependents"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"list" {}
                               "detail" {"deps" ["list"]}}})
    (model/view-dependents node "screen/admin" "entries" "list"))
  => {"entries" ["detail"]})

^{:refer xt.db.node.model-view/model-dependents :added "4.1"}
(fact "gets dependent models for a node model"
  (!.js
    (var node (event-node/node-create {"id" "node-model-dependents"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {}}})
    (model/model-put node "screen/admin" "other" {"views" {"mirror" {"deps" [["entries" "list"]]}}})
    (model/model-dependents node "screen/admin" "entries"))
  => {"other" true})

^{:refer xt.db.node.model-view/source-get :added "4.1"}
(fact "gets a source binding from a node model"
  (!.js
    (var node (event-node/node-create {"id" "node-source-get"}))
    (model/install node {"sources" {"primary" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "sqlite"}}
                      "views" {"list" {"source" "primary"}}})
    (. (model/source-get node "screen/admin" "entries" "primary") ["kind"]))
  => "sqlite")

^{:refer xt.db.node.model-view/source-put :added "4.1"}
(fact "stores source data on a node model"
  (!.js
    (var node (event-node/node-create {"id" "node-source-put"}))
    (model/install node {"sources" {"primary" {"kind" "sqlite"}}})
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"kind" "sqlite"}}
                      "views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (xtd/get-in (model/source-get node "screen/admin" "entries" "primary")
                ["data" 0 "name"]))
  => "alpha")

^{:refer xt.db.node.model-view/source-sync :added "4.1"}
(fact "synchronizes a node source from its upstream source"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-source-sync"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"archive" {"sync_from" "primary"}}
                      "views" {"list" {"source" "archive"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/source-sync node "screen/admin" "entries" "archive")
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            (xtd/get-in (model/source-get node "screen/admin" "entries" "archive")
                        ["data" 0 "name"]))))))
  => "alpha")

^{:refer xt.db.node.model-view/view-refresh :added "4.1"}
(fact "refreshes a structural node view from its source role"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-view-refresh"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/view-refresh node "screen/admin" "entries" "list")
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"status" (. out ["status"])
             "name" (xtd/get-in (. out ["value"]) [0 "name"])})))))
  => {"status" "ready"
      "name" "alpha"})

^{:refer xt.db.node.model-view/model-refresh :added "4.1"}
(fact "refreshes all views in a node model"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-model-refresh"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"list" {"source" "caching"}
                               "detail" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (model/source-put node "screen/admin" "entries" "caching"
                      [{"id" "e2" "name" "beta"}])
    (-> (model/model-refresh node "screen/admin" "entries")
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"count" (. out ["length"])
             "list_name" (xtd/get-in (model/view-val node "screen/admin" "entries" "list")
                                     [0 "name"])
             "detail_name" (xtd/get-in (model/view-val node "screen/admin" "entries" "detail")
                                       [0 "name"])})))))
  => {"count" 2
      "list_name" "beta"
      "detail_name" "alpha"})

^{:refer xt.db.node.model-view/view-set-input :added "4.1"}
(fact "sets view input and refreshes the node view"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-view-set-input"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"views" {"detail" {"source" "primary"
                                         "default_input" ["alpha"]}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/view-set-input node "screen/admin" "entries" "detail" ["beta"])
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            {"input" (model/view-input node "screen/admin" "entries" "detail")
             "status" (. out ["status"])
             "name" (xtd/get-in (. out ["value"]) [0 "name"])})))))
  => {"input" ["beta"]
      "status" "ready"
      "name" "alpha"})

^{:refer xt.db.node.model-view/handle-query :added "4.1"}
(fact "handles a direct query request for a registered view"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-handle-query"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (-> (model/handle-query {"id" "screen/admin"}
                            [{"view" {"model_id" "entries"
                                      "view_id" "list"}}]
                            nil
                            node)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"query_key" nil
      "source" "primary"
      "value" [{"id" "e1" "name" "alpha"}]
      "status" "ready"})

^{:refer xt.db.node.model-view/handle-query-refresh :added "4.1"}
(fact "handles a direct cached query refresh request"
  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-handle-query-refresh"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (xtd/set-in (. (. node ["spaces"]) ["screen/admin"] ["state"])
                ["models" "entries" "views" "list" "query_key"]
                "q-1")
    (-> (model/handle-query-refresh {"id" "screen/admin"}
                                    [{"query_key" "q-1"}]
                                    nil
                                    node)
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))))
  => {"query_key" "q-1"
      "source" "primary"
      "value" [{"id" "e1" "name" "alpha"}]
      "status" "ready"})

^{:refer xt.db.node.model-view/handle-sync :added "4.1"}
(fact "handles a direct db/sync request"
  (!.js
    (var node (event-node/node-create {"id" "node-handle-sync"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"table" "Entry"}}
                      "views" {"list" {"source" "primary"}}})
    (model/handle-sync {"id" "screen/admin"}
                       [{"db/sync" {"Entry" [{"id" "e1" "name" "alpha"}]}}]
                       nil
                       node))
  => {"db/sync" {"Entry" [{"id" "e1" "name" "alpha"}]}
      "tables" {"Entry" true}})

^{:refer xt.db.node.model-view/handle-remove :added "4.1"}
(fact "handles a direct db/remove request"
  (!.js
    (var node (event-node/node-create {"id" "node-handle-remove"}))
    (model/install node nil)
    (model/model-put node
                     "screen/admin"
                     "entries"
                     {"sources" {"primary" {"table" "Entry"}}
                      "views" {"list" {"source" "primary"}}})
    (model/source-put node "screen/admin" "entries" "primary"
                      [{"id" "e1" "name" "alpha"}])
    (model/handle-remove {"id" "screen/admin"}
                         [{"db/remove" {"Entry" ["e1"]}}]
                         nil
                         node))
  => {"db/remove" {"Entry" ["e1"]}
      "tables" {"Entry" true}})

^{:refer xt.db.node.model-view/handle-clear :added "4.1"}
(fact "handles a direct clear request"
  (!.js
    (var node (event-node/node-create {"id" "node-handle-clear"}))
    (model/install node nil)
    (model/model-put node "screen/admin" "entries" {"views" {"list" {"source" "primary"}}})
    (xtd/set-in (. (. node ["spaces"]) ["screen/admin"] ["state"])
                ["queries" "q-1"]
                {"id" "q-1"})
    (model/handle-clear {"id" "screen/admin"} [{}] nil node))
  => true)

^{:refer xt.db.node.model-view/handle-snapshot :added "4.1"}
(fact "handles a direct snapshot request"
  (!.js
    (var node (event-node/node-create {"id" "node-handle-snapshot"}))
    (model/install node {"schema" {"Entry" {}}})
    (model/model-put node "screen/admin" "entries" {"views" {"list" {}}})
    {"keys" (xt/x:obj-keys
             (model/handle-snapshot {"id" "screen/admin"} [{}] nil node))
     "models" (xt/x:obj-keys
               (. (model/handle-snapshot {"id" "screen/admin"} [{}] nil node)
                  ["models"]))})
  => {"keys" ["schema" "lookup" "sources" "queries" "models"]
      "models" ["entries"]})

^{:refer xt.db.node.model-view/install :added "4.1"}
(fact "installs xt.db model-view handlers on a node"
  (!.js
    (var node (event-node/node-create {"id" "node-install"}))
    (model/install node nil)
    [(xt/x:not-nil? (event-node/get-handler node "xt.db/query"))
     (xt/x:not-nil? (event-node/get-handler node "xt.db/query-refresh"))
     (xt/x:not-nil? (event-node/get-handler node "xt.db/snapshot"))])
  => [true true true])

^{:refer xt.db.node.model-view/uninstall :added "4.1"}
(fact "removes xt.db model-view handlers from a node"
  (!.js
    (var node (event-node/node-create {"id" "node-uninstall"}))
    (model/install node nil)
    (model/uninstall node)
    [(event-node/get-handler node "xt.db/query")
     (event-node/get-handler node "xt.db/query-refresh")
     (event-node/get-handler node "xt.db/snapshot")])
  => [nil nil nil])