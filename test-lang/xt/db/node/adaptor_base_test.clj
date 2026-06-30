(ns xt.db.node.adaptor-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do 
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-protocol :as proto]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.adaptor-base :as adaptor]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.db.helpers.data-main-test :as sample]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.net.http-fetch :as fetch]
             [js.net.http-fetch :as js-fetch]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(defn.js node-init-supabase
  []
  (var node (substrate/node-create {}))
  (return
   (adaptor/init-base-handler
    nil
    [{"primary" {"type" "supabase"
                 "defaults" (@! local-min/+config-supabase-anon+)}
      "caching" {"type" "sqlite"
                 "defaults" {"filename" ":memory:"}}}
     -/Schema
     -/SchemaLookup]
    nil
    node)))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.adaptor-base/init-base-type :added "4.1"}
(fact "init-base-type installs a live impl on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-base-type node
                                "db/primary"
                                "postgres"
                                (@! (local-min/+config+ :db))
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/primary"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_postgres/ImplPostgres"
       "client" {"::" "js.net.conn_postgres/PostgresClient"
                 "raw" map?}})
  
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-base-type node
                                "db/caching"
                                "sqlite"
                                {}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (repl/notify
            (substrate/get-service node "db/caching"))))))
  => (contains-in
      {"schema" map? "lookup" map? "opts" map?
       "::" "xt.db.system.impl_sqlite/ImplSqlite"
       "client" {"::" "js.net.conn_sqlite/SqliteClient"
                 "raw" map?}}))

^{:refer xt.db.node.adaptor-base/init-base-main :added "4.1"}
(fact "init-base-main sets metadata on primary and caching services"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-base-main {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           (repl/notify
            {"primary" (xtd/get-in primary ["metadata"])
             "caching" (xtd/get-in caching ["metadata"])})))))
  => {"caching" {"primary_id" "db/primary", "common_id" "db/common"},
      "primary" {"caching_id" "db/caching", "common_id" "db/common"}})

^{:refer xt.db.node.adaptor-base/init-base-handler :added "4.1"}
(fact "init-base-handler initialises services and returns a summary"

  (notify/wait-on :js
    (-> (adaptor/init-base-handler
         nil
         [{"primary" {"type" "postgres"
                      "defaults" (@! (local-min/+config+ :db))}
           "caching" {"type" "sqlite"
                      "defaults" {"filename" ":memory:"}}}
          -/Schema
          -/SchemaLookup]
         nil
         (substrate/node-create {}))
        (repl/notify)))
  => (contains-in
      {"handlers" {}, "services" {"db/caching" map?, "db/primary" map?, "db/common" map?},
       "id" string?
       "spaces" {},
       "::" "substrate",
       "transports" {},
       "triggers" {},
       "meta" {},
       "router" {"subscriptions" {}, "connections" {}}, "pending" {}, "listeners" {}}))

^{:refer xt.db.node.adaptor-base/get-primary-impl :added "4.1"}
(fact "gets the primary impl"

  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (repl/notify (adaptor/get-primary-impl node "db/primary"))))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" {},
       "::" "xt.db.system.impl_supabase/ImplSupabase", "metadata" {"caching_id" "db/caching", "common_id" "db/common"}})

  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (repl/notify (adaptor/get-primary-impl node "db/caching"))))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" {},
       "::" "xt.db.system.impl_supabase/ImplSupabase", "metadata" {"caching_id" "db/caching", "common_id" "db/common"}}))

^{:refer xt.db.node.adaptor-base/get-caching-impl :added "4.1"}
(fact "gets the caching impl"

  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (repl/notify (adaptor/get-caching-impl node "db/primary"))))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" map?,
       "::" "xt.db.system.impl_sqlite/ImplSqlite", "metadata" {"primary_id" "db/primary", "common_id" "db/common"}}))

^{:refer xt.db.node.adaptor-base/subscribe-db-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "subscribes to the db handler"

  ;;
  ;; PRECHECK
  ;;
  (notify/wait-on :js
    (var impl (xt.db.system.main/create-impl "supabase"
                                             (@! local-min/+config-supabase-anon+)
                                             -/Schema
                                             -/SchemaLookup))
    (repl/notify
     (realtime/subscribe impl "default" ["realtime:room:sub-test-1"
                                         "realtime:room:sub-test-2"])))

  => [true true]

  ;;
  ;; FROM HANDLER
  ;;
  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/subscribe-db-handler nil
                                          ["db/primary"
                                           "default"
                                           ["realtime:room:sub-test-1"
                                            "realtime:room:sub-test-2"]]
                                          nil
                                          node))))
        (repl/notify)))
  => [true true])

^{:refer xt.db.node.adaptor-base/unsubscribe-db-handler :added "4.1"}
(fact "unsubscribes from the db handler"

  ;;
  ;; FROM HANDLER
  ;;
  (notify/wait-on :js
    (var node nil)
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/subscribe-db-handler nil
                                          ["db/primary"
                                           "default"
                                           ["realtime:room:sub-test-1"
                                            "realtime:room:sub-test-2"]]
                                          nil
                                          node))))
        (promise/x:promise-then
         (fn [out]
           (return
            (adaptor/unsubscribe-db-handler nil
                                            ["db/primary"
                                             "default"
                                             ["realtime:room:sub-test-1"
                                              "realtime:room:sub-test-2"]]
                                            nil
                                            node))))
        (repl/notify)))
  => true)

^{:refer xt.db.node.adaptor-base/sync-caching-handler :added "4.1"
  :setup [(def +logs+ [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
                        "message" "hello"}
                       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
                        "message" "world"}])]}
(fact "sync-caching-handler applies db/sync payload to the paired caching db"

  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/sync-caching-handler nil
                                        ["db/primary"
                                         {"db/sync" {"Log" (@! +logs+)}}]
                                        nil
                                        node))))
        (repl/notify)))
  => true

  (notify/wait-on :js
    (var node nil)
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/sync-caching-handler nil
                                        ["db/primary"
                                         {"db/sync" {"Log" (@! +logs+)}}]
                                        nil
                                        node))))
        (promise/x:promise-then
         (fn [out]
           (return
            (impl-common/pull (substrate/get-service node "db/caching")
                              ["Log"]))))
        (repl/notify)))
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}]))

^{:refer xt.db.node.adaptor-base/attach-base-model :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "attach-base-model registers a db listener when options.refresh is set"
  
  (notify/wait-on :js
    (-> (-/node-init-supabase)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-base-model
            node
            "db/caching"
            "space/a"
            "group:a"
            "echo"
            {"handler" (fn [ctx]
                         (return ctx.args))
             "defaults" {"args" [1]}})
           (return
            (->  (page-core/refresh-model  node
                                           "space/a"
                                           "group:a"
                                           "echo"
                                           {}
                                           nil)
                 (promise/x:promise-then
                  (fn []
                    (repl/notify
                     (page-core/get-current-output node
                                                   "space/a"
                                                   "group:a"
                                                   "echo"))))))))
        (promise/x:promise-catch
         (fn [err] (repl/notify (. err message))))))
  => [1]
  
  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-base-main {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-base-model
            node
            "db/caching"
            "space/a"
            "group:a"
            "echo"
            {"handler" (fn [ctx]
                         (return (. ctx ["args"])))
             "defaults" {"args" [1]}})
           (return
            (page-core/model-set-input node
                                       "space/a"
                                       "group:a"
                                       "echo"
                                       {:data [1 2 3]}
                                       {}))))
        (repl/notify)))
  => {"path" ["group:a" "echo"], "post" [false], "::" "model.run", "main" [true [1 2 3]], "pre" [false]})

^{:refer xt.db.node.adaptor-base/attach-model-handler :added "4.1"}
(fact "attach-model-handler attaches a page model to the node"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-base-main {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-model-handler
            nil
            ["db/caching"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "echo"}
             {"handler" (fn [ctx]
                          (return (. ctx ["args"])))
              "defaults" {"args" [1 2 3]}}]
            nil
            node)
           (return
            (page-core/refresh-model node "room/a" "demo" "echo" {} nil))))
        (repl/notify)))
  => {"path" ["demo" "echo"], "post" [false], "::" "model.run", "main" [true [1 2 3]], "pre" [false]})

^{:refer xt.db.node.adaptor-base/detach-base-model :added "4.1"}
(fact "detach-base-model removes the model and its db listener"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-base-main {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-base-model
            node
            "db/primary"
            "room/a"
            "demo"
            "refresh-view"
            {"handler" (fn [ctx] (return []))
             "options" {"refresh" {"Log" true}}
             "defaults" {"args" []}})
           (var out (adaptor/detach-base-model
                     node "db/primary" "room/a" "demo" "refresh-view"))
           (var caching (substrate/get-service node "db/caching"))
           (var listener (impl-common/get-db-listener
                          caching
                          "room/a/demo/refresh-view"))
           (repl/notify
            {"status" (xt/x:get-key out "status")
             "listener_removed" (xt/x:nil? listener)})))))
  => {"status" "removed"
      "listener_removed" true})

^{:refer xt.db.node.adaptor-base/detach-model-handler :added "4.1"}
(fact "detach-model-handler detaches a page model from the node"

  (notify/wait-on :js
    (-> (substrate/node-create {})
        (adaptor/init-base-main {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/attach-base-model
            node
            "db/primary"
            "room/a"
            "demo"
            "echo"
            {"handler" (fn [ctx] (return [1]))
             "defaults" {"args" []}})
           (return
            (adaptor/detach-model-handler
             nil
             ["db/primary"
              {"space_id" "room/a"
               "group_id" "demo"
               "model_id" "echo"}]
             nil
             node))))
        (repl/notify)))
  => {"status" "removed"
      "space" "room/a"
      "group" "demo"
      "model" "echo"})

^{:refer xt.db.node.adaptor-base/rpc-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "rpc-call-baseline-fn routes rpc args and syncs result to caching"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-base-main
         node
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "table" {"base" "Log"
                       "type" "db/sync"}
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "hello", "author_id" nil, "id" string?}
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/rpc-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "rpc-call-handler routes rpc args through a named service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (adaptor/init-base-main
         node
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (return
            (adaptor/rpc-call-handler
             nil
             ["db/primary"
              {"input" [{"symbol" "i_message" "type" "text"}]
               "return" "jsonb"
               "schema" "scratch_v0"
               "id" "log_append_public"
               "flags" {}
               "table" {"base" "Log"
                        "type" "db/sync"}}
              ["hello"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (return
            (impl-common/pull (substrate/get-service node "db/caching")
                              ["Log"]))))
        (repl/notify)))
  => (contains-in [{"message" "hello"}])

  
  ;;
  ;; WITH TABLE will sync
  ;; 
  (notify/wait-on :js
    (var node (-> (substrate/node-create {})
                  (adaptor/init-handlers)))
    (-> (promise/x:promise-run nil)
        (promise/x:promise-then
         (fn []
           (return (substrate/request node
                                      nil
                                      "@xt.db/init-base"
                                      [{"primary" {"type" "postgres"
                                                   "defaults" (@! (local-min/+config+ :db))}
                                        "caching" {"type" "sqlite"
                                                   "defaults" {"filename" ":memory:"}}}
                                       -/Schema
                                       -/SchemaLookup]))))
        (promise/x:promise-then
         (fn []
           (return
            (-> (substrate/request node
                                   nil
                                   "@xt.db/rpc-call"
                                   ["db/primary"
                                    {"input" [{"symbol" "i_message" "type" "text"}]
                                     "return" "jsonb"
                                     "schema" "scratch_v0"
                                     "id" "log_append_public"
                                     ;; TABLE
                                     "table" {"base" "Log"
                                              "type" "db/sync"}
                                     "flags" {}}
                                    ["world"]])))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in [{"message" "world", "author_id" nil, "id" string?}
                   [{"message" "world", "author_id" nil, "id" string?}]])


  ;;
  ;; WITHOUT TABLE will not sync
  ;; 
  (notify/wait-on :js
    (var node (-> (substrate/node-create {})
                  (adaptor/init-handlers)))
    (-> (promise/x:promise-run nil)
        (promise/x:promise-then
         (fn []
           (return (substrate/request node
                                      nil
                                      "@xt.db/init-base"
                                      [{"primary" {"type" "postgres"
                                                   "defaults" (@! (local-min/+config+ :db))}
                                        "caching" {"type" "sqlite"
                                                   "defaults" {"filename" ":memory:"}}}
                                       -/Schema
                                       -/SchemaLookup]))))
        (promise/x:promise-then
         (fn []
           (return
            (-> (substrate/request node
                                   nil
                                   "@xt.db/rpc-call"
                                   ["db/primary"
                                    {"input" [{"symbol" "i_message" "type" "text"}]
                                     "return" "jsonb"
                                     "schema" "scratch_v0"
                                     "id" "log_append_public"
                                     ;; NO TABLE
                                     "flags" {}}
                                    ["world"]])))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "world", "author_id" nil, "id" string?}
       []]))

^{:refer xt.db.node.adaptor-base/rpc-create-model :added "4.1"}
(fact "rpc-create-model builds a page model spec with an rpc handler"

  (!.js
    (adaptor/rpc-create-model
               "db/primary"
               {"input" [{"symbol" "i_message" "type" "text"}]
                "return" "jsonb"
                "schema" "scratch_v0"
                "id" "log_append_public"
                "flags" {}}
               {"pipeline" {}
                "options" {}
                "defaults" {"args" ["hello"]}}))
  => {"pipeline" {}, "options" {}, "defaults" {"args" ["hello"]}})

^{:refer xt.db.node.adaptor-base/rpc-attach-model :added "4.1"
  :setup [(l/rt:restart :js)
          (pg/t:delete scratch-v0/Log)]}
(fact "rpc-attach-model attaches and invokes an rpc model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> node
        (adaptor/init-base-main {"primary" {"type" "postgres"
                                            "defaults" (@! (local-min/+config+ :db))}
                                 "caching" {"type" "sqlite"
                                            "defaults" {"filename" ":memory:"}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]
           (adaptor/rpc-attach-model
            nil
            ["db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "rpc-view"
              "service" "db/primary"}
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "table" {"base" "Log"
                       "type" "db/sync"}
              "flags" {}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" ["hello"]}}]
            nil
            node)
           (return
            (page-core/refresh-model node "room/a" "demo" "rpc-view" {} nil))))
        
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))))
  => (contains-in
      [{"path" ["demo" "rpc-view"], "post" [false], "::" "model.run", "main" [true {"message" "hello", "author_id" nil, "id" string?}], "pre" [false]}
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/pull-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "pull-call-baseline-fn pulls data and syncs result to caching"

  (notify/wait-on :js
    (var node nil)
    (-> (adaptor/init-base-main
         (substrate/node-create {})
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor/pull-call-baseline-fn node "db/primary" ["Log"]))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/pull-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "pull-call-handler routes pull args through a named service"

  (notify/wait-on :js
    (var node nil)
    (-> (adaptor/init-base-main
         (substrate/node-create {})
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor/pull-call-handler
             nil
             ["db/primary" ["Log"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/pull-create-model :added "4.1"}
(fact "pull-create-model builds a page model spec with local and remote handlers"

  (!.js
    (var spec (adaptor/pull-create-model
               {"caching_id" "db/caching"
                "primary_id" "db/primary"}
               ["Log"]
               {"pipeline" {}
                "options" {}
                "defaults" {}}))
    {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
     "has-remote" (xt/x:is-function? (xtd/get-in spec ["pipeline" "remote" "handler"]))
     "defaults" (. spec ["defaults"])})
  => {"has-remote" true, "has-main" true, "defaults" {}})

^{:refer xt.db.node.adaptor-base/pull-attach-model :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "pull-attach-model attaches and invokes a pull-view model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (:= (!:G NODE) node)
    (-> node
        (adaptor/init-base-main {"primary" {"type" "postgres"
                                            "defaults" (@! (local-min/+config+ :db))}
                                 "caching" {"type" "sqlite"
                                            "defaults" {"filename" ":memory:"}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [node]

           ;;
           ;; ACTIVE
           ;;
           (adaptor/pull-attach-model
            nil
            ["db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "active-view"}
             ["Log"]
             {"pipeline" {}
              "options"  {"refresh" {"Log" true}}
              "defaults" {"args" []}}]
            nil
            node)

           
           ;;
           ;; PASSIVE
           ;;
           (adaptor/pull-attach-model
            nil
            ["db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "passive-view"}
             ["Log"]
             {"pipeline" {}
              "options"  {"refresh" {"Log" true}}
              "defaults" {"args" []}}]
            nil
            node)
           (return
            ;; REFRESH ACTIVE
            (page-core/refresh-model-remote node "room/a" "demo" "active-view" nil))))
        (repl/notify)))
  => (contains-in
      {"path" ["demo" "active-view"],
       "remote" [true [{"message" "hello", "author_id" nil, "id" string?}]],
       "post" [false], "::" "model.run", "pre" [false]})

  (!.js
    ;; PASSIVE STILL UPDATES
    (page-core/get-current-output NODE "room/a" "demo" "passive-view"))
  => (contains-in
      [{"message" "hello", "author_id" nil, "id" string?}]))

^{:refer xt.db.node.adaptor-base/dataview-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)
          (l/rt:restart :js)]}
(fact "dataview-call-baseline-fn executes a dataview query and syncs to caching"

  (notify/wait-on :js
    (var node nil)
    (-> (adaptor/init-base-main
         (substrate/node-create {})
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor/dataview-call-baseline-fn
             node
             "db/primary"
             {"table" "Log"
              "select_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "select"
                                      "query" {}}}
              "return_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "return"
                                      "query" ["id" "message"]}}}))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/dataview-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "dataview-call-handler routes dataview args through a named service"

  (notify/wait-on :js
    (var node nil)
    (-> (adaptor/init-base-main
         (substrate/node-create {})
         {"primary" {"type" "postgres"
                     "defaults" (@! (local-min/+config+ :db))}
          "caching" {"type" "sqlite"
                     "defaults" {"filename" ":memory:"}}}
         -/Schema
         -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (:= node out)
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor/dataview-call-handler
             nil
             ["db/primary"
              {"table" "Log"
               "select_entry" {"input" []
                               "view" {"table" "Log"
                                       "type" "select"
                                       "query" {}}}
               "return_entry" {"input" []
                               "view" {"table" "Log"
                                       "type" "return"
                                       "query" ["id" "message"]}}}]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [out
             (impl-common/pull (substrate/get-service node "db/caching")
                               ["Log"])])))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))
        #_(repl/notify)))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.adaptor-base/dataview-create-model :added "4.1"}
(fact "dataview-create-model builds a page model spec with local and remote handlers"

  (!.js
    (var spec (adaptor/dataview-create-model
               "db/primary"
               {"table" "Log"
                "select_entry" {"input" []
                                "view" {"table" "Log"
                                        "type" "select"
                                        "query" {}}}
                "return_entry" {"input" []
                                "view" {"table" "Log"
                                        "type" "return"
                                        "query" ["id" "message"]}}}
               {"pipeline" {}
                "options" {}
                "defaults" {"args" [[] []]}}))
    {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
     "has-remote" (xt/x:is-function? (xtd/get-in spec ["pipeline" "remote" "handler"]))
     "defaults" (. spec ["defaults"])})
  => {"has-main" true
      "has-remote" true
      "defaults" {"args" [[] []]}})

^{:refer xt.db.node.adaptor-base/dataview-attach-model :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "dataview-attach-model attaches and invokes a dataview model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> node
        (adaptor/init-base-main {"primary" {"type" "postgres"
                                            "defaults" (@! (local-min/+config+ :db))}
                                 "caching" {"type" "sqlite"
                                            "defaults" {"filename" ":memory:"}}}
                                -/Schema
                                -/SchemaLookup)
        (promise/x:promise-then
         (fn [_]
           (return
            (adaptor/rpc-call-baseline-fn
             node
             "db/primary"
             {"input" [{"symbol" "i_message" "type" "text"}]
              "return" "jsonb"
              "schema" "scratch_v0"
              "id" "log_append_public"
              "flags" {}}
             ["hello"]))))
        (promise/x:promise-then
         (fn [_]
           (adaptor/dataview-attach-model
            nil
            ["db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "dataview-view"}
             {"table" "Log"
              "select_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "select"
                                      "query" {}}}
              "return_entry" {"input" []
                              "view" {"table" "Log"
                                      "type" "return"
                                      "query" ["id" "message"]}}}
             {"pipeline" {}
              "options" {}
              "defaults" {"args" [[] []]}}]
            nil
            node)
           (return
            (page-core/refresh-model-remote node "room/a" "demo" "dataview-view" nil))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))
        (repl/notify)))
  => (contains-in
      {"path" ["demo" "dataview-view"],
       "remote" [true [{"message" "hello", "author_id" nil, "id" string?}]],
       "post" [false], "::" "model.run", "pre" [false]}))

^{:refer xt.db.node.adaptor-base/init-handlers :added "4.1"}
(fact "init-handlers registers the @xt.db handlers"

  (notify/wait-on :js
    (var node (substrate/node-create {"schema" -/Schema
                                      "lookup" -/SchemaLookup
                                      "services" {}}))
    (adaptor/init-handlers node)
    (substrate/set-service node "db/common" {"schema" -/Schema
                                             "lookup" -/SchemaLookup})
    (-> (substrate/request node "room/a" "@xt.db/init-base"
                           [{"primary" {"type" "memory" "defaults" {}}
                             "caching" {"type" "memory" "defaults" {}}}
                            -/Schema
                            -/SchemaLookup]
                           {})
        (promise/x:promise-then
         (fn [out]
           (repl/notify out)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in {"services" {"db/primary" map?
                               "db/caching" map?}}))


^{:refer xt.db.node.adaptor-base/list-substrate-fn :added "4.1"}
(fact "list-substrate-fn lists public vars tagged with :substrate/fn"
  
  (map first (adaptor/list-substrate-fn 'xt.db.node.adaptor-base))
  => '(attach-model-handler
       dataview-attach-model
       dataview-call-handler
       detach-model-handler
       init-base-handler
       pull-attach-model
       pull-call-handler
       rpc-attach-model
       rpc-call-handler
       subscribe-db-handler
       sync-caching-handler
       unsubscribe-db-handler))
