(ns xt.db.system.impl-neon-live-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:langs [:python :ruby :dart :lua.nginx]}}
(l/script- :js
  {:runtime :basic
   :config {:process {:timeout 120000}}
   :require [[xt.db.system.backend-neon :as backend]
             [xt.db.system.main :as main]
             [xt.db.system.impl-neon :as neon]
             [xt.db.system.impl-neon-session :as session]
             [xt.db.system.impl-neon-realtime :as realtime]
             [xt.db.system.impl-common :as common]
             [xt.db.system.impl-memory :as memory]
             [xt.db.helpers.neon-live :as fixture]
             [xt.db.helpers.neon-http :as test-util]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

(fact:global {:setup [(l/rt:restart)] :teardown [(l/rt:stop)]
              :skip (not (System/getenv "NEON_LIVE_EMAIL"))})

(defn.js make-impl
  [cache]
  (return
   (main/create-impl "neon"
    {"data_url" (@! (System/getenv "NEON_LIVE_DATA_URL"))
     "bridge_url" (@! (System/getenv "NEON_LIVE_BRIDGE_URL"))
     "caching_fn" (fn [] (return cache))}
    fixture/Schema fixture/Lookup {"neon" (backend/provider)})))

(defn.js sign-in [impl]
  (return (session/sign-in impl
    {"email" (@! (System/getenv "NEON_LIVE_EMAIL"))
     "password" (@! (System/getenv "NEON_LIVE_PASSWORD"))})))

^{:refer xt.db.system.impl-neon/pull-async :id live-auth-pull-rpc}
(fact "real Auth credentials authorize the expected Data API row and RPC result"
  (l/with:global-init
    (!.js
      (var impl (-/make-impl nil))
      (var row-id (@! (System/getenv "NEON_LIVE_ROW_ID")))
      (var result nil)
      (var task
        (-> (-/sign-in impl)
            (promise/x:promise-then (fn [_]
              (return (neon/pull-async impl ["NeonSmoke" {"id" row-id} ["id" "nickname"]]))))
            (promise/x:promise-then (fn [rows]
              (var record (xt/x:first rows))
              (:= result [(== row-id (xt/x:get-key record "id"))
                           (xt/x:get-key record "nickname")])
              (return (neon/rpc-call-async impl
                {"id" "echo" "schema" "neon_scratch" "input" [{"symbol" "value"}]} ["foundation-live"]))))
            (promise/x:promise-then (fn [echo]
              (:= result [(xt/x:get-idx result 0) (xt/x:get-idx result 1) echo])
              (return (session/sign-out impl))))
            (promise/x:promise-then (fn [_] (return result)))))
      (test-util/await-result task)))
  => [true "committed" {"value" "foundation-live"}])

^{:refer xt.db.system.impl-neon-realtime/subscribe :id live-realtime-cache}
(fact "a committed RPC mutation reaches the real live subscription and memory cache"
  (l/with:global-init
    (!.js
      (var cache (memory/impl-memory fixture/Schema fixture/Lookup))
      (var impl (-/make-impl cache))
      (var row-id (@! (System/getenv "NEON_LIVE_ROW_ID")))
      (common/add-db-listener cache "complete"
        {"guard" {"NeonSmoke" true}
         "callback" (fn [_] (return (realtime/stop impl)))})
      (var task
        (-> (-/sign-in impl)
            (promise/x:promise-then (fn [_]
              (return (realtime/subscribe impl "live" [(@! (System/getenv "NEON_LIVE_TOPIC"))]))))
            (promise/x:promise-then (fn [_]
              (return (neon/rpc-call-async impl
                {"id" "set_nickname" "schema" "neon_scratch"
                 "input" [{"symbol" "row_id"} {"symbol" "value"} {"symbol" "topic"}]}
                [row-id "adapter-live" (@! (System/getenv "NEON_LIVE_TOPIC"))]))))
            (promise/x:promise-then (fn [_]
              (return (xtd/get-in impl ["state" "connections" "live" "task"]))))
            (promise/x:promise-then (fn [_]
              (var rows (memory/pull cache ["NeonSmoke" {"id" row-id} ["nickname"]]))
              (return (-> (session/sign-out impl)
                (promise/x:promise-then (fn [_] (return rows)))))))))
      (test-util/await-result task)))
  => [{"nickname" "adapter-live"}])

^{:refer xt.db.system.impl-neon-realtime/unsubscribe :id live-realtime-delete}
(fact "a committed deletion removes the row from the subscribed memory cache"
  (l/with:global-init
    (!.js
      (var cache (memory/impl-memory fixture/Schema fixture/Lookup))
      (var impl (-/make-impl cache))
      (var row-id (@! (System/getenv "NEON_LIVE_ROW_ID")))
      (var before nil)
      (var task
        (-> (-/sign-in impl)
            (promise/x:promise-then (fn [_]
              (memory/record-add cache "NeonSmoke"
                [{"id" row-id "owner_id" (@! (System/getenv "NEON_LIVE_USER_ID")) "nickname" "adapter-live"}])
              (:= before (memory/pull cache ["NeonSmoke" {"id" row-id} ["nickname"]]))
              (common/add-db-listener cache "complete"
                {"guard" {"NeonSmoke" true}
                 "callback" (fn [_] (return (realtime/stop impl)))})
              (return (realtime/subscribe impl "live-delete" [(@! (System/getenv "NEON_LIVE_TOPIC"))]))))
            (promise/x:promise-then (fn [_]
              (return (neon/rpc-call-async impl
                {"id" "remove_row" "schema" "neon_scratch"
                 "input" [{"symbol" "row_id"} {"symbol" "topic"}]}
                [row-id (@! (System/getenv "NEON_LIVE_TOPIC"))]))))
            (promise/x:promise-then (fn [_]
              (return (xtd/get-in impl ["state" "connections" "live-delete" "task"]))))
            (promise/x:promise-then (fn [_]
              (var rows (memory/pull cache ["NeonSmoke" {"id" row-id} ["nickname"]]))
              (return (-> (session/sign-out impl)
                (promise/x:promise-then (fn [_] (return [before rows])))))))))
      (test-util/await-result task)))
  => [[{"nickname" "adapter-live"}] []])
