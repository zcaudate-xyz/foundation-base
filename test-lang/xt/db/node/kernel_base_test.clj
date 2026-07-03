(ns xt.db.node.kernel-base-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [xt.db.node.kernel-base :as kernel]))

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
             [xt.db.node.kernel-base :as kernel]
             [xt.db.system.main :as impl-main]
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

(defn.js node-init-postgres
  [node]
  (:= node (or node (substrate/node-create {})))
  (return
   (kernel/kernel-setup-main
    node
    {"primary" {"type" "postgres"
                "defaults" (@! (local-min/+config+ :db))}
     "caching" {"type" "sqlite"
                "defaults" {"filename" ":memory:"}}}
    -/Schema
    -/SchemaLookup)))

(defn.js node-init-supabase
  [node]
  (:= node (or node (substrate/node-create {})))
  (return
   (kernel/kernel-setup-main
    node
    {"primary" {"type" "supabase"
                "defaults" (@! local-min/+config-supabase-anon+)}
     "caching" {"type" "sqlite"
                "defaults" {"filename" ":memory:"}}}
    -/Schema
    -/SchemaLookup)))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.kernel-base/get-primary-impl :added "4.1"}
(fact "gets the primary impl from a service id"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (repl/notify (kernel/get-primary-impl node "db/primary"))))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" map?,
       "::" "xt.db.system.impl_postgres/ImplPostgres",
       "metadata" {"caching_id" "db/caching", "common_id" "db/common"}}))

^{:refer xt.db.node.kernel-base/get-caching-impl :added "4.1"}
(fact "gets the caching impl from a primary service id"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (repl/notify (kernel/get-caching-impl node "db/primary"))))))
  => (contains-in
      {"schema" map?, "lookup" map?, "opts" map?,
       "::" "xt.db.system.impl_sqlite/ImplSqlite",
       "metadata" {"primary_id" "db/primary", "common_id" "db/common"}}))

^{:refer xt.db.node.kernel-base/kernel-create-config :added "4.1"}
(fact "creates a normalized config with default service ids"

  (!.js
    (kernel/kernel-create-config
     {"primary" {"type" "postgres"}
      "caching" {"type" "sqlite"}
      "common"  {"custom" true}}))
  => {"common" {"id" "db/common" "custom" true}
      "primary" {"id" "db/primary" "type" "postgres"}
      "caching" {"id" "db/caching" "type" "sqlite"}}

  (!.js
    (kernel/kernel-create-config
     {"primary" {"type" "memory"}
      "caching" {"type" "memory"}}))
  => {"common" {"id" "db/common"}
      "primary" {"id" "db/primary" "type" "memory"}
      "caching" {"id" "db/caching" "type" "memory"}})

^{:refer xt.db.node.kernel-base/kernel-check-exists :added "4.1"}
(fact "checks whether common, primary and caching services are all present"

  (!.js
    (var node (substrate/node-create {}))
    (kernel/kernel-check-exists
     node
     {"primary" {"type" "memory"}
      "caching" {"type" "memory"}}))
  => false

  (!.js
    (var node (substrate/node-create {}))
    (substrate/set-service node "db/common" {})
    (substrate/set-service node "db/primary" {})
    (substrate/set-service node "db/caching" {})
    (kernel/kernel-check-exists
     node
     {"primary" {"type" "memory"}
      "caching" {"type" "memory"}}))
  => true)

^{:refer xt.db.node.kernel-base/kernel-setup-single :added "4.1"}
(fact "installs a single live impl on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-single node "db/primary" "memory" {} -/Schema -/SchemaLookup)
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            (substrate/get-service node "db/primary"))))))
  => (contains-in
      {"schema" map?, "lookup" map?
       "::" "xt.db.system.impl_memory/ImplMemory"}))

^{:refer xt.db.node.kernel-base/kernel-teardown-single :added "4.1"}
(fact "tears down a single base service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-single node "db/primary" "memory" {} -/Schema -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/kernel-teardown-single node "db/primary")
           (repl/notify
            {"service_removed" (xt/x:nil? (substrate/get-service node "db/primary"))
             "node_returned" (xt/x:not-nil? node)})))))
  => {"service_removed" true, "node_returned" true})

^{:refer xt.db.node.kernel-base/kernel-setup-main :added "4.1"}
(fact "sets up common, primary and caching services with metadata"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {"primary" {"type" "memory" "defaults" {}}
                                   "caching" {"type" "memory" "defaults" {}}}
                                  -/Schema
                                  -/SchemaLookup)
        (repl/notify)))
  => {"status" "setup", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})

^{:refer xt.db.node.kernel-base/kernel-setup-handler :added "4.1"}
(fact "explicitly sets up base services from handler args"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-handler
         nil
         [{"primary" {"type" "memory" "defaults" {}}
           "caching" {"type" "memory" "defaults" {}}}
          -/Schema
          -/SchemaLookup]
         nil
         node)
        (repl/notify)))
  => {"status" "setup", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})

^{:refer xt.db.node.kernel-base/kernel-teardown-main :added "4.1"}
(fact "tears down common, primary and caching services"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var config {"primary" {"type" "memory" "defaults" {}}
                 "caching" {"type" "memory" "defaults" {}}})
    (-> (kernel/kernel-setup-main node config -/Schema -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/kernel-teardown-main node config))))
        (repl/notify)))
  => {"status" "teardown", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})

^{:refer xt.db.node.kernel-base/kernel-teardown-handler :added "4.1"}
(fact "tears down base services from a handler arg"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var config {"primary" {"type" "memory" "defaults" {}}
                 "caching" {"type" "memory" "defaults" {}}})
    (-> (kernel/kernel-setup-main node config -/Schema -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/kernel-teardown-handler nil ["db/primary"] nil node))))
        (repl/notify)))
  => {"status" "teardown", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})

^{:refer xt.db.node.kernel-base/kernel-init-main :added "4.1"}
(fact "ensures base services are present"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (var config {"primary" {"type" "memory" "defaults" {}}
                 "caching" {"type" "memory" "defaults" {}}})
    (-> (kernel/kernel-init-main node config -/Schema -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (return (kernel/kernel-init-main node config -/Schema -/SchemaLookup))))
        (repl/notify)))
  => {"status" "no_change", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})

^{:refer xt.db.node.kernel-base/kernel-init-handler :added "4.1"}
(fact "initialises base services from handler args if needed"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-init-handler
         nil
         [{"primary" {"type" "memory" "defaults" {}}
           "caching" {"type" "memory" "defaults" {}}}
          -/Schema
          -/SchemaLookup]
         nil
         node)
        (repl/notify)))
  => {"status" "setup", "data" {"caching" {"id" "db/caching", "type" "memory", "defaults" {}}, "primary" {"id" "db/primary", "type" "memory", "defaults" {}}, "common" {"id" "db/common"}}})


^{:refer xt.db.node.kernel-base/subscribe-db-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "subscribes to the db handler"

  ;;
  ;; PRECHECK
  ;;
  (notify/wait-on :js
    (var impl (impl-main/create-impl "supabase"
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
    (var node (substrate/node-create {}))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/subscribe-db-handler nil
                                         ["db/primary"
                                          "default"
                                          ["realtime:room:sub-test-1"
                                           "realtime:room:sub-test-2"]]
                                         nil
                                         node))))
        (repl/notify)))
  => [true true])

^{:refer xt.db.node.kernel-base/unsubscribe-db-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "unsubscribes from the db handler"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/subscribe-db-handler nil
                                         ["db/primary"
                                          "default"
                                          ["realtime:room:sub-test-1"
                                           "realtime:room:sub-test-2"]]
                                         nil
                                         node))))
        (promise/x:promise-then
         (fn []
           (return
            (kernel/unsubscribe-db-handler nil
                                           ["db/primary"
                                            "default"
                                            ["realtime:room:sub-test-1"
                                             "realtime:room:sub-test-2"]]
                                           nil
                                           node))))
        (repl/notify)))
  => true)

^{:refer xt.db.node.kernel-base/sync-cached-handler :added "4.1"
  :setup [(l/rt:restart :js)
          (def +logs+ [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
                        "message" "hello"}
                       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
                        "message" "world"}])]}
(fact "sync-cached-handler applies db/sync payload to the paired caching db"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/sync-cached-handler nil
                                        ["db/primary"
                                         {"db/sync" {"Log" (@! +logs+)}}]
                                        nil
                                        node))))
        (repl/notify)))
  => true

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-supabase node)
        (promise/x:promise-then
         (fn [out]
           (return
            (kernel/sync-cached-handler nil
                                        ["db/primary"
                                         {"db/sync" {"Log" (@! +logs+)}}]
                                        nil
                                        node))))
        (promise/x:promise-then
         (fn [out]
           (return
            (impl-common/pull (kernel/get-caching-impl node "db/primary")
                              ["Log"]))))
        (repl/notify)))
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}]))

^{:refer xt.db.node.kernel-base/attach-base-model :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "attach-base-model registers a db listener when options.refresh is set"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (kernel/attach-base-model
            node
            "db/caching"
            "space/a"
            "group:a"
            "echo"
            {"handler" (fn [ctx]
                         (return ctx.args))
             "defaults" {"args" [1]}})
           (return
            (-> (page-core/model-refresh  node
                                          "space/a"
                                          "group:a"
                                          "echo"
                                          {}
                                          nil)))))
        (promise/x:promise-then
         (fn []
           (return
            (page-core/model-get-output node
                                        "space/a"
                                        "group:a"
                                        "echo"))))
        (repl/notify)))
  => [1]

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {"primary" {"type" "memory" "defaults" {}}
                                   "caching" {"type" "memory" "defaults" {}}}
                                  -/Schema
                                  -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/attach-base-model
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

^{:refer xt.db.node.kernel-base/attach-model-handler :added "4.1"}
(fact "attach-model-handler attaches a page model to the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {"primary" {"type" "memory" "defaults" {}}
                                   "caching" {"type" "memory" "defaults" {}}}
                                  -/Schema
                                  -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/attach-model-handler
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
            (page-core/model-refresh node "room/a" "demo" "echo" {} nil))))
        (repl/notify)))
  => {"path" ["demo" "echo"], "post" [false], "::" "model.run", "main" [true [1 2 3]], "pre" [false]})

^{:refer xt.db.node.kernel-base/detach-base-model :added "4.1"}
(fact "detach-base-model removes the model and its db listener"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {"primary" {"type" "memory" "defaults" {}}
                                   "caching" {"type" "memory" "defaults" {}}}
                                  -/Schema
                                  -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/attach-base-model
            node
            "db/primary"
            "room/a"
            "demo"
            "refresh-view"
            {"handler" (fn [ctx] (return []))
             "options" {"refresh" {"Log" true}}
             "defaults" {"args" []}})
           (repl/notify
            (kernel/detach-base-model
             node "db/primary" "room/a" "demo" "refresh-view"))))))
  => {"space" "room/a", "model" "refresh-view", "group" "demo", "status" "removed"})

^{:refer xt.db.node.kernel-base/detach-model-handler :added "4.1"}
(fact "detach-model-handler detaches a page model from the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                  {"primary" {"type" "memory" "defaults" {}}
                                   "caching" {"type" "memory" "defaults" {}}}
                                  -/Schema
                                  -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/attach-base-model
            node
            "db/primary"
            "room/a"
            "demo"
            "echo"
            {"handler" (fn [ctx] (return [1]))
             "defaults" {"args" []}})
           (return
            (kernel/detach-model-handler
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

^{:refer xt.db.node.kernel-base/rpc-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "rpc-call-baseline-fn routes rpc args and syncs result to caching"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/rpc-call-baseline-fn
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
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "hello", "author_id" nil, "id" string?}
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/rpc-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "rpc-call-handler routes rpc args through a named service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/rpc-call-handler
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
            (impl-common/pull (kernel/get-caching-impl node "db/primary")
                              ["Log"]))))
        (repl/notify)))
  => (contains-in [{"message" "hello"}])

  ;;
  ;; WITHOUT TABLE will not sync
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (return
            (kernel/rpc-call-handler
             nil
             ["db/primary"
              {"input" [{"symbol" "i_message" "type" "text"}]
               "return" "jsonb"
               "schema" "scratch_v0"
               "id" "log_append_public"
               ;; NO TABLE
               "flags" {}}
              ["world"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "world", "author_id" nil, "id" string?}
       []]))

^{:refer xt.db.node.kernel-base/rpc-create-model :added "4.1"}
(fact "rpc-create-model builds a page model spec with an rpc handler"

  (!.js
    (var spec (kernel/rpc-create-model
               "db/primary"
               {"input" [{"symbol" "i_message" "type" "text"}]
                "return" "jsonb"
                "schema" "scratch_v0"
                "id" "log_append_public"
                "flags" {}}
               {"pipeline" {}
                "options" {}
                "defaults" {"args" ["hello"]}}))
    {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
     "defaults" (. spec ["defaults"])})
  => {"has-main" true, "defaults" {"args" ["hello"]}})

^{:refer xt.db.node.kernel-base/rpc-attach-model :added "4.1"
  :setup [(l/rt:restart :js)
          (pg/t:delete scratch-v0/Log)]}
(fact "rpc-attach-model attaches and invokes an rpc model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []
           (kernel/rpc-attach-model
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
            (page-core/model-refresh node "room/a" "demo" "rpc-view" {} nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [out
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))))
  => (contains-in
      [{"path" ["demo" "rpc-view"], "post" [false], "::" "model.run", "main" [true {"message" "hello", "author_id" nil, "id" string?}], "pre" [false]}
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/pull-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "pull-call-baseline-fn pulls data and syncs result to caching"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn [out]
           (return
            (kernel/rpc-call-baseline-fn
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
            (kernel/pull-call-baseline-fn node "db/primary" ["Log"]))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/pull-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "pull-call-handler routes pull args through a named service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn [out]
           (return
            (kernel/rpc-call-baseline-fn
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
            (kernel/pull-call-handler
             nil
             ["db/primary" ["Log"]]
             nil
             node))))
        (promise/x:promise-then
         (fn [out]
           (return
            [out
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/pull-create-model :added "4.1"}
(fact "pull-create-model builds a page model spec with local and remote handlers"

  (!.js
    (var spec (kernel/pull-create-model
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

^{:refer xt.db.node.kernel-base/pull-attach-model :added "4.1"
  :setup [(l/rt:restart :js)
          (pg/t:delete scratch-v0/Log)]}
(fact "pull-attach-model attaches and invokes a pull-view model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (:= (!:G NODE) node)
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn []

           ;;
           ;; ACTIVE
           ;;
           (kernel/pull-attach-model
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
           (kernel/pull-attach-model
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

           ;;
           ;; SEED DATA
           ;;
           (return
            (kernel/rpc-call-baseline-fn
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
           ;; REFRESH ACTIVE
           (return
            (page-core/model-refresh-remote node "room/a" "demo" "active-view" nil))))
        (repl/notify)))
  => (contains-in
      {"path" ["demo" "active-view"],
       "remote" [true [{"message" "hello", "author_id" nil, "id" string?}]],
       "post" [false], "::" "model.run", "pre" [false]})

  (!.js
    ;; PASSIVE STILL UPDATES
    (page-core/model-get-output NODE "room/a" "demo" "passive-view"))
  => (contains-in
      [{"message" "hello", "author_id" nil, "id" string?}]))

^{:refer xt.db.node.kernel-base/dataview-call-baseline-fn :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)
          (l/rt:restart :js)]}
(fact "dataview-call-baseline-fn executes a dataview query and syncs to caching"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn [out]
           (return
            (kernel/rpc-call-baseline-fn
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
            (kernel/dataview-call-baseline-fn
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
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/dataview-call-handler :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "dataview-call-handler routes dataview args through a named service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn [out]
           (return
            (kernel/rpc-call-baseline-fn
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
            (kernel/dataview-call-handler
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
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))))
  => (contains-in
      [[{"message" "hello", "author_id" nil, "id" string?}]
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/dataview-create-model :added "4.1"}
(fact "dataview-create-model builds a page model spec with local and remote handlers"

  (!.js
    (var spec (kernel/dataview-create-model
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
                "defaults" {"args" [{"select_args" []
                                     "return_args" []}]}}))
    {"has-main" (xt/x:is-function? (xtd/get-in spec ["handler"]))
     "has-remote" (xt/x:is-function? (xtd/get-in spec ["pipeline" "remote" "handler"]))
     "defaults" (. spec ["defaults"])})
  => {"has-remote" true, "has-main" true, "defaults" {"args" [{"return_args" [], "select_args" []}]}})

^{:refer xt.db.node.kernel-base/dataview-attach-model :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "dataview-attach-model attaches and invokes a dataview model"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/node-init-postgres node)
        (promise/x:promise-then
         (fn [_]
           (return
            (kernel/rpc-call-baseline-fn
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
           (kernel/dataview-attach-model
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
              "defaults" {"args" [{"select_args" []
                                   "return_args" []}]}}]
            nil
            node)
           (return
            (page-core/model-refresh-remote node "room/a" "demo" "dataview-view" nil))))
        (promise/x:promise-then
         (fn [out]
           (repl/notify
            [out
             (impl-common/pull (kernel/get-caching-impl node "db/primary")
                               ["Log"])])))))
  => (contains-in
      [{"path" ["demo" "dataview-view"],
        "remote" [true [{"message" "hello", "author_id" nil, "id" string?}]],
        "post" [false], "::" "model.run", "pre" [false]}
       [{"message" "hello", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.kernel-base/init-handlers :added "4.1"}
(fact "init-handlers registers the @xt.db handlers"

  (!.js
    (var node (substrate/node-create {}))
    (kernel/init-handlers node)
    (xt/x:obj-keys (. node ["handlers"])))
  => (contains ["@xt.db/kernel-init"
                "@xt.db/kernel-setup"
                "@xt.db/kernel-teardown"
                "@xt.db/subscribe-db"
                "@xt.db/unsubscribe-db"
                "@xt.db/sync-cached"
                "@xt.db/attach-model"
                "@xt.db/detach-model"
                "@xt.db/rpc-call"
                "@xt.db/rpc-attach-model"
                "@xt.db/pull-call"
                "@xt.db/pull-cached"
                "@xt.db/pull-attach-model"
                "@xt.db/dataview-call"
                "@xt.db/dataview-cached"
                "@xt.db/dataview-attach-model"]
               :in-any-order))

^{:refer xt.db.node.kernel-base/list-substrate-fn :added "4.1"}
(fact "list-substrate-fn lists public vars tagged with :substrate/fn"
  
  (sort (map (comp :substrate/fn meta second)
             (kernel/list-substrate-fn 'xt.db.node.kernel-base)))
  => '("@xt.db/attach-model"
       "@xt.db/dataview-attach-model"
       "@xt.db/dataview-call"
       "@xt.db/detach-model"
       "@xt.db/kernel-init"
       "@xt.db/kernel-setup"
       "@xt.db/kernel-teardown"
       "@xt.db/pull-attach-model"
       "@xt.db/pull-cached"
       "@xt.db/pull-cached"
       "@xt.db/pull-call"
       "@xt.db/rpc-attach-model"
       "@xt.db/rpc-call"
       "@xt.db/subscribe-db"
       "@xt.db/sync-cached"
       "@xt.db/unsubscribe-db"))


^{:refer xt.db.node.kernel-base/pull-cached-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-prep-tree :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-cached-handler :added "4.1"}
(fact "TODO")