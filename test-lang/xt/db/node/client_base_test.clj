(ns xt.db.node.client-base-test
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
   :require [[js.net.http-fetch :as js-fetch]
             [xt.net.http-fetch :as http-fetch]
             [xt.net.http-util :as http-util]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.runtime :as runtime]
             [xt.db.node.client-base :as client]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as impl-common]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-memory :as transport-memory]
             [xt.net.addon-supabase :as addon]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js CONFIG
  {"postgres"  {"type" "postgres"
                "defaults" (@! (local-min/+config+ :db))}
   "sqlite"    {"type" "sqlite"
                "defaults" {"filename" ":memory:"}}
   "supabase"  {"type" "supabase"
                "defaults" (@! local-min/+config-supabase-anon+)}
   "memory"    {"type" "memory" "defaults" {}}})

(defn.js init-kernel
  [node primary caching]
  (kernel-base/init-handlers node)
  (return
   (client/kernel-init node
                       {"primary" (. -/CONFIG [primary])
                        "caching" (. -/CONFIG [caching])}
                       -/Schema
                       -/SchemaLookup
                       {})))

(defn.js init-proxy
  [client primary caching]
  (var server (substrate/node-create {}))
  (runtime/init-server server)
  (runtime/init-server-proxy client)
  (return
   (-> (transport-memory/link-pair server client)
       (promise/x:promise-then
        (fn []
          (return
           (client/kernel-init client {"primary" (. -/CONFIG [primary])
                                       "caching" (. -/CONFIG [caching])}
                                  -/Schema
                                  -/SchemaLookup
                                  {})))))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.node.client-base/kernel-init.primitive :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "invokes kernel-init over wire"

  ;;
  ;; BASELINE
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (kernel-base/init-handlers node)
    (-> (substrate/request node
                           nil
                           "@xt.db/kernel-init"
                           [{"primary" {"type" "supabase"
                                        "defaults" (@! local-min/+config-supabase-anon+)}
                             "caching" {"type" "sqlite"
                                        "defaults" {"filename" ":memory:"}}}
                            -/Schema
                            -/SchemaLookup])
        (repl/notify)))
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base/kernel-init :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "invokes a local base handler"
  
  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-init node
                                 {"primary"  (. -/CONFIG ["supabase"])
                                  "caching"  (. -/CONFIG ["sqlite"])}
                                 -/Schema
                                 -/SchemaLookup
                                 {}))))
        (repl/notify)))
  => (contains-in
      {"status" "no_change", "data" {"caching" map?, "primary" map?, "common" map?}})

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-init client
                                {"primary"  (. -/CONFIG ["supabase"])
                                 "caching"  (. -/CONFIG ["sqlite"])}
                                -/Schema
                                -/SchemaLookup
                                {}))))
        (repl/notify)))
  => (contains-in
      {"status" "no_change", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base/kernel-setup :added "4.1"}
(fact "sets up base db services through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-setup node
                                 {"primary"  (. -/CONFIG ["supabase"])
                                  "caching"  (. -/CONFIG ["sqlite"])}
                                 -/Schema
                                 -/SchemaLookup
                                 {}))))
        (repl/notify)))
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}})
  
  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-setup client
                                 {"primary"  (. -/CONFIG ["supabase"])
                                  "caching"  (. -/CONFIG ["sqlite"])}
                                 -/Schema
                                 -/SchemaLookup
                                 {}))))
        (repl/notify)))
  => (contains-in
      {"status" "setup", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base/kernel-teardown :added "4.1"}
(fact "sets up base db services through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-teardown node
                                    "db/primary"
                                    {}))))
        (repl/notify)))
  => (contains-in
      {"status" "teardown", "data" {"caching" map?, "primary" map?, "common" map?}})

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/kernel-teardown client
                                    "db/primary"
                                    {}))))
        (repl/notify)))
  => (contains-in
      {"status" "teardown", "data" {"caching" map?, "primary" map?, "common" map?}}))

^{:refer xt.db.node.client-base/subscribe-db :added "4.1"}
(fact "subscribes to db topics through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/subscribe-db node
                                 "db/primary"
                                 "default"
                                 ["realtime:room:client-sub-1"
                                  "realtime:room:client-sub-2"]
                                 {}))))
        (repl/notify)))
  => [true true]

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/subscribe-db client
                                 "db/primary"
                                 "default"
                                 ["realtime:room:client-sub-1"
                                  "realtime:room:client-sub-2"]
                                 {}))))
        (repl/notify)))
  => [true true])

^{:refer xt.db.node.client-base/unsubscribe-db :added "4.1"}
(fact "unsubscribes from db topics through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/subscribe-db node
                                 "db/primary"
                                 "default"
                                 ["realtime:room:client-unsub-1"
                                  "realtime:room:client-unsub-2"]
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/unsubscribe-db node
                                   "db/primary"
                                   "default"
                                   ["realtime:room:client-unsub-1"
                                    "realtime:room:client-unsub-2"]
                                   {}))))
        (repl/notify)))
  => true

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/subscribe-db client
                                 "db/primary"
                                 "default"
                                 ["realtime:room:client-unsub-1"
                                  "realtime:room:client-unsub-2"]
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/unsubscribe-db client
                                   "db/primary"
                                   "default"
                                   ["realtime:room:client-unsub-1"
                                    "realtime:room:client-unsub-2"]
                                   {}))))
        (repl/notify)))
  => true)

^{:refer xt.db.node.client-base/sync-cached :added "4.1"}
(fact "applies a db/sync payload to the paired caching db"
  {:setup [(def +logs+ [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
                        "message" "hello"}
                       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
                        "message" "world"}])]}

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/sync-cached node
                                 "db/primary"
                                 {"db/sync" {"Log" (@! +logs+)}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/pull-cached node "db/primary" ["Log"]))))
        (repl/notify)))
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "supabase" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/sync-cached client
                                 "db/primary"
                                 {"db/sync" {"Log" (@! +logs+)}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (client/pull-cached node "db/primary" ["Log"])))
        (repl/notify)))
  => (contains-in
      [{"id" "257553c1-c4f4-44ad-b1b5-092bf825a690"
        "message" "hello"}
       {"id" "257553c1-c4f4-44ad-b1b5-092bf825a691"
        "message" "world"}]))

^{:refer xt.db.node.client-base/attach-model :added "4.1"}
(fact "attaches a page model through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "memory" "memory")
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model node
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {"handler" (fn [ctx]
                                              (return (. ctx ["args"])))
                                  "defaults" {"args" [1 2 3]}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            ;; set input
            )))
        (promise/x:promise-then
         (fn []
           (return
            (page-core/get-current-output node "room/a" "demo" "echo"))))
        (repl/notify)))
  => {"status" "attached"
      "space" "room/a"
      "group" "demo"
      "model" "echo"}

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "memory" "memory")
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model client
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {"handler" (fn [ctx]
                                              (return (. ctx ["args"])))
                                  "defaults" {"args" [1 2 3]}}
                                 {}))))
        (repl/notify)))
  => {"status" "attached"
      "space" "room/a"
      "group" "demo"
      "model" "echo"})

^{:refer xt.db.node.client-base/detach-model :added "4.1"}
(fact "detaches a page model through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "memory" "memory")
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model node
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {"handler" (fn [ctx]
                                              (return [1]))
                                  "defaults" {"args" []}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/detach-model node
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {}))))
        (repl/notify)))
  => {"status" "removed"
      "space" "room/a"
      "group" "demo"
      "model" "echo"}

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "memory" "memory")
        (promise/x:promise-then
         (fn []
           (return
            (client/attach-model client
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {"handler" (fn [ctx]
                                              (return [1]))
                                  "defaults" {"args" []}}
                                 {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/detach-model client
                                 "db/caching"
                                 {"space_id" "room/a"
                                  "group_id" "demo"
                                  "model_id" "echo"}
                                 {}))))
        (repl/notify)))
  => {"status" "removed"
      "space" "room/a"
      "group" "demo"
      "model" "echo"})

^{:refer xt.db.node.client-base/rpc-call :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "calls an rpc entry through the client and syncs result to caching"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call node
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "table" {"base" "Log"
                                       "type" "db/sync"}
                              "flags" {}}
                             ["hello-client"]
                             {}))))
        (promise/x:promise-then
         (fn [out]
           (var caching (kernel-base/get-caching-impl node "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "hello-client", "author_id" nil, "id" string?}
       [{"message" "hello-client", "author_id" nil, "id" string?}]])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call client
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "table" {"base" "Log"
                                       "type" "db/sync"}
                              "flags" {}}
                             ["hello-client"]
                             {}))))
        (promise/x:promise-then
         (fn [out]
           (var server (xtd/get-in client ["state" "test-server"]))
           (var caching (kernel-base/get-caching-impl server "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"message" "hello-client", "author_id" nil, "id" string?}
       [{"message" "hello-client", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.client-base/rpc-attach-model :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "attaches and invokes an rpc model through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-attach-model node
                                     "db/primary"
                                     {"space_id" "room/a"
                                      "group_id" "demo"
                                      "model_id" "rpc-view"}
                                     {"input" [{"symbol" "i_message" "type" "text"}]
                                      "return" "jsonb"
                                      "schema" "scratch_v0"
                                      "id" "log_append_public"
                                      "table" {"base" "Log"
                                               "type" "db/sync"}
                                      "flags" {}}
                                     {"pipeline" {}
                                      "options" {}
                                      "defaults" {"args" ["hello-attach"]}}
                                     {}))))
        (promise/x:promise-then
         (fn []
           (return (page-core/refresh-model node "room/a" "demo" "rpc-view" {} nil))))
        (promise/x:promise-then
         (fn [out]
           (var caching (kernel-base/get-caching-impl node "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"path" ["demo" "rpc-view"], "post" [false], "::" "model.run", "main" [true {"message" "hello-attach", "author_id" nil, "id" string?}], "pre" [false]}
       [{"message" "hello-attach", "author_id" nil, "id" string?}]])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-attach-model client
                                     "db/primary"
                                     {"space_id" "room/a"
                                      "group_id" "demo"
                                      "model_id" "rpc-view"}
                                     {"input" [{"symbol" "i_message" "type" "text"}]
                                      "return" "jsonb"
                                      "schema" "scratch_v0"
                                      "id" "log_append_public"
                                      "table" {"base" "Log"
                                               "type" "db/sync"}
                                      "flags" {}}
                                     {"pipeline" {}
                                      "options" {}
                                      "defaults" {"args" ["hello-attach"]}}
                                     {}))))
        (promise/x:promise-then
         (fn []
           (var server (xtd/get-in client ["state" "test-server"]))
           (return (page-core/refresh-model server "room/a" "demo" "rpc-view" {} nil))))
        (promise/x:promise-then
         (fn [out]
           (var server (xtd/get-in client ["state" "test-server"]))
           (var caching (kernel-base/get-caching-impl server "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"path" ["demo" "rpc-view"], "post" [false], "::" "model.run", "main" [true {"message" "hello-attach", "author_id" nil, "id" string?}], "pre" [false]}
       [{"message" "hello-attach", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.client-base/pull-call :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "pulls data through the client and syncs result to caching"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call node
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-pull"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return (client/pull-call node "db/primary" ["Log"] {}))))
        (promise/x:promise-then
         (fn [out]
           (var caching (kernel-base/get-caching-impl node "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello-pull", "author_id" nil, "id" string?}]
       [{"message" "hello-pull", "author_id" nil, "id" string?}]])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call client
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-pull"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return (client/pull-call client "db/primary" ["Log"] {}))))
        (promise/x:promise-then
         (fn [out]
           (var server (xtd/get-in client ["state" "test-server"]))
           (var caching (kernel-base/get-caching-impl server "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello-pull", "author_id" nil, "id" string?}]
       [{"message" "hello-pull", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.client-base/pull-cached :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.client-base/pull-attach-model :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "attaches and invokes a pull-view model through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/pull-attach-model node
                                      "db/primary"
                                      {"space_id" "room/a"
                                       "group_id" "demo"
                                       "model_id" "pull-view"}
                                      ["Log"]
                                      {"pipeline" {}
                                       "options" {}
                                       "defaults" {"args" []
                                                   "output" {}}}
                                      {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call node
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-pull-attach"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return (page-core/refresh-model-remote node "room/a" "demo" "pull-view" nil))))
        (repl/notify)))
  => (contains-in
      {"path" ["demo" "pull-view"]
       "remote" [true [{"message" "hello-pull-attach", "author_id" nil, "id" string?}]]
       "post" [false], "::" "model.run", "pre" [false]})

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/pull-attach-model client
                                      "db/primary"
                                      {"space_id" "room/a"
                                       "group_id" "demo"
                                       "model_id" "pull-view"}
                                      ["Log"]
                                      {"pipeline" {}
                                       "options" {}
                                       "defaults" {"args" []
                                                   "output" {}}}
                                      {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call client
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-pull-attach"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (var server (xtd/get-in client ["state" "test-server"]))
           (return (page-core/refresh-model-remote server "room/a" "demo" "pull-view" nil))))
        (repl/notify)))
  => (contains-in
      {"path" ["demo" "pull-view"]
       "remote" [true [{"message" "hello-pull-attach", "author_id" nil, "id" string?}]]
       "post" [false], "::" "model.run", "pre" [false]}))

^{:refer xt.db.node.client-base/dataview-call :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "executes a dataview query through the client and syncs to caching"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call node
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-dataview"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/dataview-call node
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
                                  {}))))
        (promise/x:promise-then
         (fn [out]
           (var caching (kernel-base/get-caching-impl node "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello-dataview", "author_id" nil, "id" string?}]
       [{"message" "hello-dataview", "author_id" nil, "id" string?}]])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call client
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-dataview"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/dataview-call client
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
                                  {}))))
        (promise/x:promise-then
         (fn [out]
           (var server (xtd/get-in client ["state" "test-server"]))
           (var caching (kernel-base/get-caching-impl server "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [[{"message" "hello-dataview", "author_id" nil, "id" string?}]
       [{"message" "hello-dataview", "author_id" nil, "id" string?}]]))

^{:refer xt.db.node.client-base/dataview-cached :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.client-base/dataview-attach-model :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log)]}
(fact "attaches and invokes a dataview model through the client"

  ;;
  ;; DIRECT
  ;;
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (-/init-kernel node "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call node
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-dataview-attach"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/dataview-attach-model node
                                          "db/primary"
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
                                           "defaults" {"select_args" []
                                                       "return_args" []}}
                                          {}))))
        (promise/x:promise-then
         (fn []
           (return (page-core/refresh-model-remote node "room/a" "demo" "dataview-view" nil))))
        (promise/x:promise-then
         (fn [out]
           (var caching (kernel-base/get-caching-impl node "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"path" ["demo" "dataview-view"]
        "remote" [true [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]]
        "post" [false], "::" "model.run", "pre" [false]}
       [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]])

  ;;
  ;; PROXY
  ;;
  (notify/wait-on :js
    (var client (substrate/node-create {}))
    (-> (-/init-proxy client "postgres" "sqlite")
        (promise/x:promise-then
         (fn []
           (return
            (client/rpc-call client
                             "db/primary"
                             {"input" [{"symbol" "i_message" "type" "text"}]
                              "return" "jsonb"
                              "schema" "scratch_v0"
                              "id" "log_append_public"
                              "flags" {}}
                             ["hello-dataview-attach"]
                             {}))))
        (promise/x:promise-then
         (fn []
           (return
            (client/dataview-attach-model client
                                          "db/primary"
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
                                           "defaults" {"select_args" []
                                                       "return_args" []}}
                                          {}))))
        (promise/x:promise-then
         (fn []
           (var server (xtd/get-in client ["state" "test-server"]))
           (return (page-core/refresh-model-remote server "room/a" "demo" "dataview-view" nil))))
        (promise/x:promise-then
         (fn [out]
           (var server (xtd/get-in client ["state" "test-server"]))
           (var caching (kernel-base/get-caching-impl server "db/primary"))
           (return
            [out
             (impl-common/pull caching ["Log"])])))
        (repl/notify)))
  => (contains-in
      [{"path" ["demo" "dataview-view"]
        "remote" [true [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]]
        "post" [false], "::" "model.run", "pre" [false]}
       [{"message" "hello-dataview-attach", "author_id" nil, "id" string?}]]))
