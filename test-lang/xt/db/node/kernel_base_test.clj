(ns xt.db.node.kernel-base-test
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
             [xt.db.node.kernel-base :as kernel]
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

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.node.kernel-base/get-primary-impl :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/get-caching-impl :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/kernel-create-config :added "4.1"}
(fact "creates a normalized config map with default ids"

  (!.js
    (kernel/kernel-create-config
     {"common"  {}
      "primary" {"type" "postgres" "defaults" {"host" "localhost"}}
      "caching" {"type" "sqlite" "defaults" {"filename" ":memory:"}}}))
  => {"common"  {"id" "db/common"}
      "primary" {"id" "db/primary" "type" "postgres" "defaults" {"host" "localhost"}}
      "caching" {"id" "db/caching" "type" "sqlite" "defaults" {"filename" ":memory:"}}})

^{:refer xt.db.node.kernel-base/kernel-check-exists :added "4.1"}
(fact "checks whether all base services exist on the node"

  (!.js
    (var node (substrate/node-create {}))
    (substrate/set-service node "db/common" {})
    (substrate/set-service node "db/primary" {})
    (kernel/kernel-check-exists node
                                {"common"  {"id" "db/common"}
                                 "primary" {"id" "db/primary"}
                                 "caching" {"id" "db/caching"}}))
  => false

  (!.js
    (var node (substrate/node-create {}))
    (substrate/set-service node "db/common" {})
    (substrate/set-service node "db/primary" {})
    (substrate/set-service node "db/caching" {})
    (kernel/kernel-check-exists node
                                {"common"  {"id" "db/common"}
                                 "primary" {"id" "db/primary"}
                                 "caching" {"id" "db/caching"}}))
  => true)

^{:refer xt.db.node.kernel-base/kernel-setup-single :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "installs a live impl on the node"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-single node
                                    "db/primary"
                                    "memory"
                                    {}
                                    -/Schema
                                    -/SchemaLookup)
        (promise/x:promise-then
           (fn []
             (repl/notify
              (substrate/get-service node "db/primary"))))))
  => (contains-in
      {"schema" map? "lookup" map?
       "::" "xt.db.system.impl_memory/ImplMemory"}))

^{:refer xt.db.node.kernel-base/kernel-teardown-single :added "4.1"}
(fact "tears down a single base service"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-single node
                                    "db/primary"
                                    "memory"
                                    {}
                                    -/Schema
                                    -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/kernel-teardown-single node "db/primary")
           (repl/notify
            (substrate/get-service node "db/primary"))))))
  => nil)

^{:refer xt.db.node.kernel-base/kernel-setup-main :added "4.1"}
(fact "sets up common, primary and caching services"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                 {"primary" {"type" "memory" "defaults" {}}
                                  "caching" {"type" "memory" "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (var primary (substrate/get-service node "db/primary"))
           (var caching (substrate/get-service node "db/caching"))
           (repl/notify
            {"primary" (xtd/get-in primary ["metadata"])
             "caching" (xtd/get-in caching ["metadata"])})))))
  => {"caching" {"primary_id" "db/primary", "common_id" "db/common"},
      "primary" {"caching_id" "db/caching", "common_id" "db/common"}})

^{:refer xt.db.node.kernel-base/kernel-setup-handler :added "4.1"}
(fact "explicitly sets up base services through the handler"

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
  => (contains-in
      {"status" "setup"
       "data"   {"common"  {"id" "db/common"}
                 "primary" {"id" "db/primary"}
                 "caching" {"id" "db/caching"}}}))

^{:refer xt.db.node.kernel-base/kernel-teardown-main :added "4.1"}
(fact "tears down common, primary and caching services"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                 {"primary" {"type" "memory" "defaults" {}}
                                  "caching" {"type" "memory" "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/kernel-teardown-main node
                                        {"common"  {"id" "db/common"}
                                         "primary" {"id" "db/primary"}
                                         "caching" {"id" "db/caching"}})
           (repl/notify 
            {"common"  (xt/x:is-object? (substrate/get-service node "db/common"))
             "primary" (xt/x:is-object? (substrate/get-service node "db/primary"))
             "caching" (xt/x:is-object? (substrate/get-service node "db/caching"))})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify (. err message))))))
  => {"caching" false, "primary" false, "common" false})

^{:refer xt.db.node.kernel-base/kernel-teardown-handler :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "tears down base services through the handler"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                 {"primary" {"type" "memory" "defaults" {}}
                                  "caching" {"type" "memory" "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/kernel-teardown-handler
            nil
            [{"common"  {"id" "db/common"}
              "primary" {"id" "db/primary"}
              "caching" {"id" "db/caching"}}]
            nil
            node)
           (repl/notify 
            {"common"  (xt/x:is-object? (substrate/get-service node "db/common"))
             "primary" (xt/x:is-object? (substrate/get-service node "db/primary"))
             "caching" (xt/x:is-object? (substrate/get-service node "db/caching"))})))))
  => {"caching" false, "primary" false, "common" false})

^{:refer xt.db.node.kernel-base/kernel-init-main :added "4.1"}
(fact "ensures base services are present"

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-init-main node
                                {"primary" {"type" "memory" "defaults" {}}
                                 "caching" {"type" "memory" "defaults" {}}}
                                -/Schema
                                -/SchemaLookup)
        (repl/notify)))
  => (contains-in
      {"status" "setup"
       "data"   {"common"  {"id" "db/common"}
                 "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
                 "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (-> (kernel/kernel-setup-main node
                                 {"primary" {"type" "memory" "defaults" {}}
                                  "caching" {"type" "memory" "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)
        (promise/x:promise-then
         (fn []
           (kernel/kernel-init-main node
                                    {"primary" {"type" "memory" "defaults" {}}
                                     "caching" {"type" "memory" "defaults" {}}}
                                    -/Schema
                                    -/SchemaLookup)
           (repl/notify
            {"common"  (substrate/get-service node "db/common")
             "primary" (substrate/get-service node "db/primary")
             "caching" (substrate/get-service node "db/caching")})))))
  => (contains-in
      {"common" map? "primary" map? "caching" map?}))

^{:refer xt.db.node.kernel-base/kernel-init-handler :added "4.1"}
(fact "initialises base services through the handler"

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
  => {"status" "setup"
      "data"   {"common"  {"id" "db/common"}
                "primary" {"id" "db/primary" "type" "memory" "defaults" {}}
                "caching" {"id" "db/caching" "type" "memory" "defaults" {}}}})

^{:refer xt.db.node.kernel-base/subscribe-db-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/unsubscribe-db-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/sync-caching-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/attach-base-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/attach-model-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/detach-base-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/detach-model-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/rpc-call-baseline-fn :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/rpc-call-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/rpc-create-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/rpc-attach-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/pull-call-baseline-fn :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/pull-call-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/pull-create-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/pull-attach-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-call-baseline-fn :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-call-handler :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-create-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/dataview-attach-model :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/init-handlers :added "4.1"}
(fact "TODO")

^{:refer xt.db.node.kernel-base/list-substrate-fn :added "4.1"}
(fact "TODO")
