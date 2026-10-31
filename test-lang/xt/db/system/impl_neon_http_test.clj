(ns xt.db.system.impl-neon-http-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:langs [:python :ruby :dart :lua.nginx]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.backend-neon :as backend-neon]
             [xt.db.system.main :as main]
             [xt.db.node.kernel-neon :as kernel]
             [xt.db.node.client-neon :as client]
             [xt.substrate :as substrate]
             [xt.db.helpers.neon-http :as test-util]
             [xt.db.system.impl-neon :as neon]
             [xt.db.system.impl-neon-session :as session]
             [xt.db.system.impl-neon-realtime :as realtime]
             [xt.db.system.impl-common :as common]
             [xt.db.system.impl-memory :as memory]
             [xt.db.helpers.data-main-test :as sample]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-base :as xt]]})
(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]
              :skip (not (System/getenv "NEON_FIXTURE_PORT"))})

^{:refer xt.db.system.impl-neon/rpc-call-async :id http-auth-rpc}
(fact "authenticates over HTTP and renews the JWT before an RPC"
  (l/with:global-init
    (!.js
      (var impl (main/create-impl "neon"
        {"data_url" (@! (str "http://127.0.0.1:" (System/getenv "NEON_FIXTURE_PORT") "/data"))
         "bridge_url" (@! (str "http://127.0.0.1:" (System/getenv "NEON_FIXTURE_PORT")))} nil nil {"neon" (xt.db.system.backend-neon/provider)}))
      (var node (substrate/node-create {}))
      (substrate/set-service node "neon" impl)
      (kernel/init-handlers node)
      (var task (-> (client/sign-in node "neon" {"email" "test@example.test" "password" "fixture"} {})
          (promise/x:promise-then (fn [_]
            (return (neon/rpc-call-async impl {"id" "echo" "input" [{"symbol" "value"}]} ["hello"]))))))
      (test-util/await-result task)))
  => {"value" "hello" "authorization" "Bearer renewed"})

^{:refer xt.db.system.impl-neon-realtime/subscribe :id http-realtime-cache}
(fact "polling delivers cache updates and stops without applying late responses"
  (l/with:global-init
    (!.js
      (var cache (memory/impl-memory sample/Schema sample/SchemaLookup))
      (var impl (main/create-impl "neon"
        {"bridge_url" (@! (str "http://127.0.0.1:" (System/getenv "NEON_FIXTURE_PORT")))
         "caching_fn" (fn [] (return cache))} sample/Schema sample/SchemaLookup {"neon" (xt.db.system.backend-neon/provider)}))
      (common/add-db-listener cache "stop" {"guard" {"UserAccount" true}
        "callback" (fn [_] (realtime/stop impl))})
      (var task (-> (realtime/subscribe impl "connection" ["t"])
        (promise/x:promise-then (fn [_]
          (return (xtd/get-in impl ["state" "connections" "connection" "task"]))))
        (promise/x:promise-then (fn [_]
          (return (memory/pull cache ["UserAccount" {"id" "00000000-0000-0000-0000-000000000000"} ["nickname"]]))))))
      (test-util/await-result task)))
  => [{"nickname" "live"}])

^{:refer xt.db.system.impl-neon-realtime/subscribe :id expired-cursor-resync}
(fact "expired cursor establishes a baseline and refreshes the cache before replay"
  (l/with:global-init
    (!.js
      (var recoveries [])
      (var cache (memory/impl-memory sample/Schema sample/SchemaLookup))
      (var impl (main/create-impl "neon"
        {"bridge_url" (@! (str "http://127.0.0.1:" (System/getenv "NEON_FIXTURE_PORT")))
         "caching_fn" (fn [] (return cache))
         "resync" (fn [_ topics] (xt/x:arr-push recoveries topics) (common/clear-db cache) (return true))} sample/Schema sample/SchemaLookup {"neon" (xt.db.system.backend-neon/provider)}))
      (xtd/set-in impl ["state" "connections" "connection"]
        {"active" false "topics" ["t"] "cursor" "expired"})
      (common/add-db-listener cache "stop" {"guard" {"UserAccount" true}
        "callback" (fn [_] (realtime/stop impl))})
      (var task (-> (realtime/subscribe impl "connection" ["t"])
        (promise/x:promise-then (fn [_]
          (return (xtd/get-in impl ["state" "connections" "connection" "task"]))))
        (promise/x:promise-then (fn [_]
          (return [(xt/x:len recoveries) (memory/pull cache ["UserAccount" {"id" "00000000-0000-0000-0000-000000000000"} ["nickname"]])])))))
      (test-util/await-result task)))
  => [1 [{"nickname" "live"}]])
