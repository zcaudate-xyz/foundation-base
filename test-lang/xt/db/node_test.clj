(ns xt.db.node-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.db.helpers.test-fixtures :as fixtures])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node :as node]
             [xt.db.helpers.test-fixtures :as fixtures]
             [js.lib.driver-sqlite :as js-sqlite]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node/create-space :added "4.1"}
(fact "registers all declarative models for a space"
  (!.js
   (var n (event-node/node-create {"id" "node-a"}))
   (node/install n {"schema" {}
                    "lookup" {}})
   (node/create-space
    n
    "main"
    {"models"
     {"Task" {"sources" {"cache" {"kind" "cache"}}}
      "Entry" {"views" {"all" {"source" "cache"}}}}})
   [(xt/x:not-nil? (node/model-get n "main" "Task"))
    (xt/x:not-nil? (node/model-get n "main" "Entry"))
    (. (node/view-get n "main" "Entry" "all") ["source"])])
  => [true true "cache"])

^{:refer xt.db.node/source-config :added "4.1"}
(fact "prefers nested config and falls back to the source map"
  (!.js
   [(node/source-config {"config" {"path" "db.sqlite"}})
    (node/source-config {"path" "db.sqlite"})])
  => [{"path" "db.sqlite"}
      {"path" "db.sqlite"}])

^{:refer xt.db.node/source-setup :added "4.1"}
(fact "prefers nested setup and falls back to legacy setup keys"
  (!.js
   [(node/source-setup {"setup" {"schema" true
                                 "seed" {"Entry" []}}})
    (node/source-setup {"setup_schema" true
                        "seed" {"Entry" []}})])
  => [{"schema" true
       "seed" {"Entry" []}}
      {"schema" true
       "seed" {"Entry" []}}])

^{:refer xt.db.node/source-driver :added "4.1"}
(fact "prefers the nested config driver then legacy top-level driver"
  (!.js
   [(node/source-driver {"config" {"driver" "pg.driver"}
                         "driver" "legacy.driver"})
    (node/source-driver {"driver" "legacy.driver"})
    (node/source-driver {"config" {}})])
  => ["pg.driver" "legacy.driver" nil])

^{:refer xt.db.node/source-db-opts :added "4.1"}
(fact "resolves db opts from the kind registry before legacy config"
  (!.js
   [(node/source-db-opts {"kind" "cache"} {"schema" {}
                                            "lookup" {}})
    (node/source-db-opts {"kind" "custom"
                          "config" {"db_opts" {"mode" "config"}}}
                         {})
    (node/source-db-opts {"kind" "custom"
                          "db_opts" {"mode" "legacy"}}
                         {})])
  => [nil
      {"mode" "config"}
      {"mode" "legacy"}])

^{:refer xt.db.node/source-live? :added "4.1"}
(fact "marks sources live when their kind or legacy hooks require runtime materialization"
  (!.js
   [(node/source-live? {"kind" "cache"})
    (node/source-live? {"kind" "postgres"
                        "config" {"driver" "pg.driver"}})
    (node/source-live? {"kind" "legacy"
                        "driver" "legacy.driver"})
    (node/source-live? {"kind" "legacy"})])
  => [true true true true])

^{:refer xt.db.node/materialize-source :added "4.1"}
(fact "materializes a live cache source into a db runtime"
  (notify/wait-on [:js 10000]
    (var n (event-node/node-create {"id" "node-a"}))
    (node/install n {"schema" (@! fixtures/+schema+)
                     "lookup" (@! fixtures/+lookup+)})
    (node/model-put
     n
     "main"
     "Task"
     {"sources"
      {"primary"
       {"kind" "sqlite"
        "config" {"driver" (js-sqlite/driver)
                  "filename" ":memory:"}
        "setup" {"schema" true
                 "seed" (@! fixtures/+entry-seed+)}}}})
    (promise/x:promise-then
     (node/materialize-source n "main" "Task" "primary")
     (fn [source]
       (repl/notify
        {"live" (. source ["live"])
         "dbtype" (. source ["dbtype"])
         "has_db" (xt/x:not-nil? (. source ["db"]))}))))
  => {"live" true
      "dbtype" "db.sql"
      "has_db" true})

^{:refer xt.db.node/materialize-space :added "4.1"}
(fact "materializes all live sources for a space"
  (notify/wait-on [:js 10000]
    (var n (event-node/node-create {"id" "node-a"}))
    (node/install n {"schema" (@! fixtures/+schema+)
                     "lookup" (@! fixtures/+lookup+)})
    (node/model-put
     n
     "main"
     "Task"
     {"sources" {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}
                 "legacy" {"kind" "legacy"}}})
    (promise/x:promise-then
     (promise/x:promise-all (node/materialize-space n "main"))
     (fn [running]
       (var cache-source (node/source-get n "main" "Task" "primary"))
       (var legacy-source (node/source-get n "main" "Task" "legacy"))
       (repl/notify
        {"running" (. running ["length"])
         "cache_live" (. cache-source ["live"])
         "legacy_live" (. legacy-source ["live"])}))))
  => {"running" 3
      "cache_live" true
      "legacy_live" true})

^{:refer xt.db.node/model-materialize :added "4.1"}
(fact "materializes all live sources for a single model"
  (notify/wait-on [:js 10000]
    (var n (event-node/node-create {"id" "node-a"}))
    (node/install n {"schema" (@! fixtures/+schema+)
                     "lookup" (@! fixtures/+lookup+)})
    (node/model-put
     n
     "main"
     "Task"
     {"sources" {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}
                 "legacy" {"kind" "legacy"}}})
    (promise/x:promise-then
     (node/model-materialize n "main" "Task")
     (fn [out]
       (repl/notify
        {"keys" (xt/x:obj-keys out)
         "cache_live" (. (node/source-get n "main" "Task" "primary") ["live"])}))))
  => {"keys" ["primary" "caching" "legacy"]
      "cache_live" true})

^{:refer xt.db.node/source-share :added "4.1"}
(fact "shares live source runtime fields across spaces"
  (notify/wait-on [:js 10000]
    (var n (event-node/node-create {"id" "node-a"}))
    (node/install n {"schema" (@! fixtures/+schema+)
                     "lookup" (@! fixtures/+lookup+)})
    (node/model-put
     n
     "source"
     "Task"
     {"sources" {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true
                                     "seed" (@! fixtures/+entry-seed+)}}}})
    (node/model-put
     n
     "target"
     "Task"
     {"sources" {"primary" {"kind" "sqlite"
                            "config" {"driver" (js-sqlite/driver)
                                      "filename" ":memory:"}
                            "setup" {"schema" true}}}})
    (promise/x:promise-then
     (node/materialize-source n "source" "Task" "primary")
     (fn [_]
       (var out (node/source-share n "source" "target" "Task" "primary"))
       (var shared (node/source-get n "target" "Task" "primary"))
       (repl/notify
        {"keys" (xt/x:obj-keys out)
         "live" (. shared ["live"])
         "dbtype" (. shared ["dbtype"])}))))
  => {"keys" ["primary"]
      "live" true
      "dbtype" "db.sql"})

^{:refer xt.db.node/remote-payload :added "4.1"}
(fact "returns the first remote control payload or an empty map"
  (!.js
   [(node/remote-payload [{"model_id" "Task"}
                          {"model_id" "Entry"}])
    (node/remote-payload [])])
  => [{"model_id" "Task"} {}])

^{:refer xt.db.node/install-remote-handlers :added "4.1"}
(fact "registers remote xt.db control handlers on the node"
  (!.js
   (var n (event-node/node-create {"id" "node-a"}))
   (node/install-remote-handlers n)
   (var handlers (event-node/list-handlers n))
   [(. handlers ["length"])
    (xt/x:not-nil? (event-node/get-handler n "xt.db/model-put"))
    (xt/x:not-nil? (event-node/get-handler n "xt.db/node-summary"))])
  => [15 true true])

^{:refer xt.db.node/create :added "4.1"}
(fact "creates a node from declarative spec and materializes live sources"
  (notify/wait-on [:js 10000]
    (promise/x:promise-then
     (node/create {"node_id" "node-a"
                   "db" {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "sources"
                         {"primary"
                          {"kind" "sqlite"
                           "config" {"driver" (js-sqlite/driver)
                                     "filename" ":memory:"}
                           "setup" {"schema" true
                                    "seed" (@! fixtures/+entry-seed+)}}}}
                   "spaces"
                   {"main"
                    {"models"
                     {"Task" {"views"
                              {"all" {"resolver" (@! fixtures/+resolver-model-query+)
                                      "source" "primary"}}}}}}})
     (fn [n]
       (repl/notify
        {"id" (. n ["id"])
         "handlers" (. (event-node/list-handlers n) ["length"])
         "cache_live" (. (node/source-get n "main" "Task" "primary") ["live"])}))))
  => {"id" "node-a"
      "handlers" 21
      "cache_live" true})

^{:refer xt.db.node/summarise :added "4.1"}
(fact "summarises spaces, models, sources, and views for inspection"
  (notify/wait-on [:js 10000]
    (promise/x:promise-then
     (node/create {"node_id" "node-a"
                   "db" {"schema" (@! fixtures/+schema+)
                         "lookup" (@! fixtures/+lookup+)
                         "sources"
                         {"primary"
                          {"kind" "sqlite"
                           "config" {"driver" (js-sqlite/driver)
                                     "filename" ":memory:"}
                           "setup" {"schema" true
                                    "seed" (@! fixtures/+entry-seed+)}}}}
                   "spaces"
                   {"main"
                    {"models"
                     {"Task"
                      {"views" {"all" {"resolver" (@! fixtures/+resolver-model-query+)
                                       "source" "primary"}}}}}}})
     (fn [n]
       (repl/notify
        (node/summarise n)))))
  => {"id" "node-a"
      "spaces"
      {"main"
       {"models"
        {"Task"
         {"sources"
          {"caching"
           {"sync_from" "primary"
            "live" true
            "data_count" 0}
           "primary"
           {"kind" "sqlite"
            "sync_from" nil
            "live" true
            "data_count" 0}}
          "views"
          {"all"
           {"source" "primary"
            "status" "idle"
            "resolver_keys" ["type" "table" "select_entry" "return_entry"]}}}}}}})
