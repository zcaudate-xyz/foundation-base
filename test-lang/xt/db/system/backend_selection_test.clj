(ns xt.db.system.backend-selection-test
  (:use code.test)
  (:require [clojure.string :as str]
            [lang.core :as l]
            [xt.db.node.runtime :as runtime]))

^{:seedgen/root {:langs [:python :ruby :dart :lua.nginx]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.db.system.main :as main]
             [xt.db.system.main-client :as clients]
             [xt.db.system.impl-common :as common]
             [xt.db.system.backend-postgres :as postgres]
             [xt.db.system.backend-sqlite :as sqlite]
             [xt.db.system.backend-supabase :as supabase]
             [xt.db.system.backend-neon :as neon]
             [xt.db.node.kernel-base :as kernel]
             [xt.db.node.runtime :as runtime]
             [xt.db.helpers.neon-http :as test-util]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client]
             [xt.substrate.transport-memory :as transport]]})

(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

^{:refer xt.db.system.main/create-impl :id memory-default}
(fact "memory remains available without optional providers"
  (l/with:global-init
    (!.js (xt/x:get-key (main/create-impl "memory" {} {} {}) "::" nil)))
  => "xt.db.system.impl_memory/ImplMemory")

^{:refer xt.db.system.main-client/get-provider :id explicit-selection}
(fact "unselected backend errors identify the requested and selected types"
  (l/with:global-init
    (!.js (var result "unexpected success")
      (try (main/create-impl "supabase" {} {} {} {"neon" {}})
        (catch err (:= result (xt/x:ex-data err))))
      result))
  => {"type" "supabase" "selected" ["neon"]})

^{:refer xt.db.system.main-client/create-client :id provider-validation}
(fact "a selected provider cannot silently return an unsupported client"
  (!.js (var rejected false)
    (try (clients/create-client "custom" {} {"custom" {"create_client" (fn [_] (return nil))}})
      (catch err (:= rejected true)))
    rejected)
  => true)

^{:refer xt.db.node.kernel-base/set-backends :id node-local-providers}
(fact "nodes use their own provider maps and transport config stays serializable"
  (l/with:global-init
    (!.js
      (var a (substrate/node-create {}))
      (var b (substrate/node-create {}))
      (var factory (fn [marker]
        (return {"create_client" (fn [_] (return {}))
                 "create_impl" (fn [_client _schema _lookup]
                   (return {"metadata" {} "marker" marker}))})))
      (kernel/set-backends a {"custom" (factory "a")})
      (kernel/set-backends b {"custom" (factory "b")})
      (var config {"primary" {"type" "custom" "defaults" {}}
                   "caching" {"type" "memory" "defaults" {}}})
      (var task (-> (kernel/kernel-setup-main a config {} {})
        (promise/x:promise-then (fn [_] (return (kernel/kernel-setup-main b config {} {}))))
        (promise/x:promise-then (fn [result]
          (return [(xtd/get-in (substrate/get-service a "db/primary") ["marker"])
                   (xtd/get-in (substrate/get-service b "db/primary") ["marker"])
                   (xtd/get-in result ["data" "primary" "type"])
                   (xt/x:has-key? (xt/x:get-key result "data" nil) "backends")])))))
      (test-util/await-result task)))
  => ["a" "b" "custom" false])

^{:refer xt.db.system.main/create-impl-init :id protocol-init}
(fact "selected Postgres initializes through the common protocol"
  (l/with:global-init
    (!.js
      (var calls [])
      (var descriptor (postgres/provider))
      (xt/x:set-key descriptor "create_client" (fn [_]
        (return {"::/override" {"connect" (fn [client _opts]
          (xt/x:arr-push calls "connected")
          (return (promise/x:promise-run client)))}})))
      (var impl (main/create-impl "postgres" {} {} {} {"postgres" descriptor}))
      (var task (-> (main/create-impl-init impl)
        (promise/x:promise-then (fn [result]
          (return [calls (xt/x:get-key result "::" nil)])))))
      (test-util/await-result task)))
  => [["connected"] "xt.db.system.impl_postgres/ImplPostgres"])

^{:refer xt.db.node.runtime/init-server :id optional-handlers}
(fact "runtime installs only explicitly supplied backend handlers"
  (l/with:global-init
    (!.js
      (var node (substrate/node-create {}))
      (runtime/init-server node)
      (var absent (xt/x:nil? (substrate/get-handler node "@test/backend")))
      (runtime/init-server node {"init_handlers" [(fn [target]
        (substrate/register-handler target "@test/backend" (fn [_s _a _r _n] (return "ok")) nil))]})
      [absent (xt/x:not-nil? (substrate/get-handler node "@test/backend"))
       (xt/x:nil? (substrate/get-handler node "@xt.supabase/sign-in"))]))
  => [true true true])

^{:refer xt.db.system.main/create-impl :id emitted-dependency-isolation :seedgen/base {:all {:suppress true}}}
(fact "memory and Neon emission exclude unselected native drivers"
  (let [memory (l/emit-script '(xt.db.system.main/create-impl-init (xt.db.system.main/create-impl "memory" {} {} {})) {:lang :js :layout :full})
        neon (l/emit-script '(xt.db.system.main/create-impl "neon" {} {} {} {"neon" (xt.db.system.backend-neon/provider)}) {:lang :js :layout :full})
        worker (runtime/sharedworker-init-string)]
    [(not-any? #(str/includes? memory %) ["impl_supabase" "impl_neon" "js_net_conn_postgres" "@sqlite.org/sqlite-wasm"])
     (str/includes? neon "impl_neon")
     (not-any? #(str/includes? neon %) ["impl_supabase" "js_net_conn_postgres" "@sqlite.org/sqlite-wasm" "js_net_ws_native"])
     (not-any? #(str/includes? worker %) ["impl_supabase" "impl_neon" "@sqlite.org/sqlite-wasm" "js_net_conn_postgres"])])
  => [true true true true])

^{:refer xt.db.system.main/create-impl :id selected-providers}
(fact "each selected backend constructs its own implementation without connecting"
  (l/with:global-init
    (!.js
      (var providers {"sqlite" (sqlite/provider) "postgres" (postgres/provider)
                       "supabase" (supabase/provider) "neon" (neon/provider)})
      (var out [])
      (var types ["sqlite" "postgres" "supabase" "neon"])
      (xt/for:array [type types]
        (xt/x:arr-push out (xt/x:get-key (main/create-impl type {} {} {} providers) "::" nil)))
      out))
  => ["xt.db.system.impl_sqlite/ImplSqlite" "xt.db.system.impl_postgres/ImplPostgres"
      "xt.db.system.impl_supabase/ImplSupabase" "xt.db.system.impl_neon/ImplNeon"])

^{:refer xt.db.system.main/create-impl-init :id sqlite-protocol-init}
(fact "SQLite initializes through the common protocol"
  (l/with:global-init
    (!.js
      (var calls [])
      (var descriptor (sqlite/provider))
      (xt/x:set-key descriptor "create_client" (fn [_]
        (return {"::/override" {"connect" (fn [client _opts]
          (xt/x:arr-push calls "connected")
          (return (promise/x:promise-run client)))}})))
      (var impl (main/create-impl "sqlite" {} {} {} {"sqlite" descriptor}))
      (var task (-> (main/create-impl-init impl)
        (promise/x:promise-then (fn [result]
          (return [calls (xt/x:get-key result "::" nil)])))))
      (test-util/await-result task)))
  => [["connected"] "xt.db.system.impl_sqlite/ImplSqlite"])

^{:refer xt.db.node.kernel-base/set-backends :id remote-local-selection}
(fact "remote setup uses server-local factories with ordinary wire configuration"
  (l/with:global-init
    (!.js
      (var server (substrate/node-create {}))
      (var remote (substrate/node-create {}))
      (kernel/set-backends server {"custom" {
        "create_client" (fn [_] (return {}))
        "create_impl" (fn [_client _schema _lookup] (return {"metadata" {} "marker" "server-local"}))}})
      (kernel/init-handlers server)
      (var config {"primary" {"type" "custom" "defaults" {}}
                   "caching" {"type" "memory" "defaults" {}}})
      (var task (-> (transport/link-pair server remote)
        (promise/x:promise-then (fn [_]
          (return (client/kernel-setup remote config {} {} {"transport_id" "server"}))))
        (promise/x:promise-then (fn [result]
          (return [(xt/x:get-key result "status" nil)
                   (xtd/get-in result ["data" "primary" "type"])
                   (xtd/get-in (substrate/get-service server "db/primary") ["marker"])
                   (xt/x:nil? (xtd/get-in remote ["state" "db_backends"]))])))))
      (test-util/await-result task)))
  => ["setup" "custom" "server-local" true])

^{:refer xt.db.node.kernel-base/kernel-teardown-main :id memory-only-lifecycle}
(fact "memory-only nodes initialize and tear down without a native lifecycle provider"
  (l/with:global-init
    (!.js
      (var node (substrate/node-create {}))
      (var config {"primary" {"type" "memory" "defaults" {}}
                   "caching" {"type" "memory" "defaults" {}}})
      (var task (-> (kernel/kernel-setup-main node config {} {})
        (promise/x:promise-then (fn [_]
          (var result (kernel/kernel-teardown-main node config))
          (return [(xt/x:get-key result "status" nil)
                   (xt/x:nil? (substrate/get-service node "db/primary"))])))))
      (test-util/await-result task)))
  => ["teardown" true])
