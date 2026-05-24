(ns xt.db.node.view-model-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.node.view-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.db.text.sql-util :as sql-util]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.view-model/model-put :added "4.1"}
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
(fact "creates a live wrapper-backed sqlite source and refreshes a real view from it"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"constructor" js-sqlite/connect-constructor
                            "wrapper" js-sqlite/wrap-connection
                            "query_live" true
                            "dbtype" "db.sql"
                            "db_opts" (sql-util/sqlite-opts nil)
                            "setup_schema" true
                            "seed" (@! fixtures/+entry-seed+)}}}
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

^{:refer xt.db.node.view-model/source-refresh :added "4.1"}
(fact "syncs a live primary sqlite source into a live caching sqlite source"

  (notify/wait-on [:js 10000]
    (-> (node/create
         {"node_id" "node-live-sync"
          "db" {"schema" (@! fixtures/+schema+)
                "lookup" (@! fixtures/+lookup+)
                "sources"
                {"primary" {"constructor" js-sqlite/connect-constructor
                            "wrapper" js-sqlite/wrap-connection
                            "query_live" true
                            "dbtype" "db.sql"
                            "db_opts" (sql-util/sqlite-opts nil)
                            "setup_schema" true
                            "seed" (@! fixtures/+entry-seed+)}
                 "caching" {"constructor" js-sqlite/connect-constructor
                            "wrapper" js-sqlite/wrap-connection
                            "query_live" true
                            "dbtype" "db.sql"
                            "db_opts" (sql-util/sqlite-opts nil)
                            "setup_schema" true}}}
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

^{:refer xt.db.node.view-model/model-sync :added "4.1"}
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
