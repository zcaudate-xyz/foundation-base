(ns js.supabase.db-impl-test
  (:use code.test)
  (:require [lang.core :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[js.supabase.db-impl :as supabase-adapter]
             [xt.db.system.impl-common :as impl-common]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as proto]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.text.sql-util :as sql-util]]})

(fact:global
 {:setup [(l/rt:restart :js)]
  :teardown [(l/rt:stop :js)]})

^{:refer js.supabase.db-impl/pull-async :added "4.1"}
(fact "applies the tree projection, schema, and equality filters through the SDK builder"
  (notify/wait-on :js
    (var calls [])
    (var builder {})
    (xt/x:set-key builder "select"
                  (fn [selection options]
                    (xt/x:arr-push calls ["select" selection])
                    (return builder)))
    (xt/x:set-key builder "eq"
                  (fn [column value]
                    (xt/x:arr-push calls ["eq" column value])
                    (return builder)))
    (xt/x:set-key builder "then"
                  (fn [on-resolve]
                    (return
                     (promise/x:promise-then
                      (promise/x:promise-run
                       {"data" [{"id" "asset-1"}]
                        "count" nil
                        "error" nil})
                      on-resolve))))
    (var scoped {})
    (xt/x:set-key scoped "from"
                  (fn [table]
                    (xt/x:arr-push calls ["from" table])
                    (return builder)))
    (var client {})
    (xt/x:set-key client "schema"
                  (fn [schema-name]
                    (xt/x:arr-push calls ["schema" schema-name])
                    (return scoped)))
    (var db (supabase-adapter/impl-supabase-js
             client
             {"Asset" {"id" {"type" "uuid" "ident" "id" "order" 0}
                       "name" {"type" "text" "ident" "name" "order" 1}}}
             {"Asset" {"schema" "archive"}}))
    (-> (supabase-adapter/pull-async
         db
         ["Asset" {"custom" []
                   "where" [{"id" "asset-1"}]
                   "links" []
                   "data" ["id" "name"]}])
        (promise/x:promise-then
         (fn [data]
           (repl/notify {"data" data "calls" calls})))))
  => {"data" [{"id" "asset-1"}]
      "calls" [["schema" "archive"]
               ["from" "Asset"]
               ["select" "id,name"]
               ["eq" "id" "asset-1"]]}
  (notify/wait-on :js
    (var calls [])
    (var builder {})
    (xt/x:set-key builder "select"
                  (fn [selection _options]
                    (xt/x:arr-push calls ["select" selection])
                    (return builder)))
    (xt/x:set-key builder "or"
                  (fn [filters]
                    (xt/x:arr-push calls ["or" filters])
                    (return builder)))
    (xt/x:set-key builder "order"
                  (fn [column options]
                    (xt/x:arr-push calls
                                   ["order" column (xt/x:get-key options "ascending")])
                    (return builder)))
    (xt/x:set-key builder "range"
                  (fn [start end]
                    (xt/x:arr-push calls ["range" start end])
                    (return builder)))
    (xt/x:set-key builder "then"
                  (fn [on-resolve]
                    (return
                     (promise/x:promise-then
                      (promise/x:promise-run
                       {"data" [{"id" "asset-2"}]
                        "count" nil
                        "error" nil})
                      on-resolve))))
    (var client {})
    (xt/x:set-key client "schema"
                  (fn [schema-name]
                    (xt/x:arr-push calls ["schema" schema-name])
                    (return client)))
    (xt/x:set-key client "from"
                  (fn [table]
                    (xt/x:arr-push calls ["from" table])
                    (return builder)))
    (var db (supabase-adapter/impl-supabase-js
             client
             {}
             {"Asset" {"schema" "archive"}}))
    (-> (supabase-adapter/pull-async
         db
         ["Asset" {"custom" [(sql-util/ORDER-BY ["name" "id"])
                             (sql-util/ORDER-SORT "desc")
                             (sql-util/LIMIT 5)
                             (sql-util/OFFSET 2)]
                   "where" [{"status" "open"}
                             {"status" "pending"}]
                   "links" []
                   "data" ["id"]}])
        (promise/x:promise-then
         (fn [data]
           (repl/notify {"data" data "calls" calls})))))
  => {"data" [{"id" "asset-2"}]
      "calls" [["schema" "archive"]
               ["from" "Asset"]
               ["select" "id"]
               ["or" "status.eq.open,status.eq.pending"]
               ["order" "name" false]
               ["order" "id" false]
               ["range" 2 6]]}
  (notify/wait-on :js
    (var calls [])
    (var builder {})
    (xt/x:set-key builder "select"
                  (fn [selection options]
                    (xt/x:arr-push calls
                                   ["select" selection options])
                    (return builder)))
    (xt/x:set-key builder "then"
                  (fn [on-resolve]
                    (return
                     (promise/x:promise-then
                      (promise/x:promise-run
                       {"data" nil "count" 7 "error" nil})
                      on-resolve))))
    (var client {})
    (xt/x:set-key client "schema"
                  (fn [schema-name]
                    (xt/x:arr-push calls ["schema" schema-name])
                    (return client)))
    (xt/x:set-key client "from"
                  (fn [table]
                    (xt/x:arr-push calls ["from" table])
                    (return builder)))
    (var db (supabase-adapter/impl-supabase-js
             client
             {}
             {"Asset" {"schema" "analytics"}}))
    (-> (supabase-adapter/pull-async
         db
         ["Asset" {"custom" [{"::" "sql/count"}]
                   "where" []
                   "links" []
                   "data" []}])
        (promise/x:promise-then
         (fn [data]
           (repl/notify {"data" data "calls" calls})))))
  => {"data" 7
      "calls" [["schema" "analytics"]
               ["from" "Asset"]
               ["select" "*" {"count" "exact" "head" true}]]})

^{:refer js.supabase.db-impl/rpc-call-async :added "4.1"}
(fact "maps positional RPC inputs to named arguments and returns SDK data"
  (notify/wait-on :js
    (var calls [])
    (var rpc-client {})
    (xt/x:set-key rpc-client "rpc"
                  (fn [name body]
                    (xt/x:arr-push calls ["rpc" name body])
                    (return
                     (promise/x:promise-run
                      {"data" {"ok" true}
                       "error" nil}))))
    (var client {})
    (xt/x:set-key client "schema"
                  (fn [schema-name]
                    (xt/x:arr-push calls ["schema" schema-name])
                    (return rpc-client)))
    (var db (supabase-adapter/impl-supabase-js client {} {}))
    (-> (supabase-adapter/rpc-call-async
         db
         {"id" "rebuild_asset"
          "schema" "tenant"
          "input" [{"symbol" "i_asset_id"}
                   {"name" "i_mode"}]}
         ["asset-1" "quick"])
        (promise/x:promise-then
         (fn [data]
           (repl/notify {"data" data "calls" calls})))))
  => {"data" {"ok" true}
      "calls" [["schema" "tenant"]
               ["rpc" "rebuild_asset"
                {"i_asset_id" "asset-1" "i_mode" "quick"}]]})

^{:refer js.supabase.db-impl/dispatch-event :added "4.1"}
(fact "dispatches sync payloads to listeners registered for changed tables"
  (!.js
    (var received nil)
    (var db (supabase-adapter/impl-supabase-js {} {} {}))
    (impl-common/add-db-listener
     db
     "asset-listener"
     {"guard" {"Asset" true}
      "callback" (fn [event]
                   (:= received event))})
    [(supabase-adapter/dispatch-event
      db
      {"payload" {"db/sync" {"Asset" [{"id" "asset-1"}]}}})
     received])
  => [true {"db/sync" {"Asset" [{"id" "asset-1"}]}}])

^{:refer js.supabase.db-impl/subscribe-db :added "4.1"}
(fact "creates a private broadcast channel once and resolves its subscribed status"
  (notify/wait-on :js
    (var calls [])
    (var handlers {})
    (var channel {})
    (xt/x:set-key channel "on"
                  (fn [type config handler]
                    (xt/x:set-key handlers (xt/x:get-key config "event") handler)
                    (xt/x:arr-push calls ["on" type (xt/x:get-key config "event")])
                    (return channel)))
    (xt/x:set-key channel "subscribe"
                  (fn [callback]
                    (callback "SUBSCRIBED" nil)
                    (return channel)))
    (var client {})
    (xt/x:set-key client "channel"
                  (fn [topic options]
                    (xt/x:arr-push calls ["channel" topic options])
                    (return channel)))
    (xt/x:set-key client "removeChannel"
                  (fn [unused]
                    (xt/x:arr-push calls ["remove"])
                    (return true)))
    (var db (supabase-adapter/impl-supabase-js client {} {}))
    (-> (supabase-adapter/subscribe-db db "conn-1" ["realtime:assets"])
        (promise/x:promise-then
         (fn [results]
           (var entry (xt/x:get-key (xt/x:get-key db "channels")
                                   "realtime:assets"))
           (repl/notify
            {"results" results
             "refs" (xt/x:get-key entry "refs")
             "broadcasts" (xt/x:len (xt/x:obj-keys handlers))
             "channel-config" (xt/x:second (xt/x:first calls))})))))
  => {"results" [true]
      "refs" 1
      "broadcasts" 3
      "channel-config" "realtime:assets"})

^{:refer js.supabase.db-impl/unsubscribe-db :added "4.1"}
(fact "retains a shared topic until its final connection unsubscribes"
  (notify/wait-on :js
    (var removed 0)
    (var channel {})
    (xt/x:set-key channel "on"
                  (fn [_type _config _handler]
                    (return channel)))
    (xt/x:set-key channel "subscribe"
                  (fn [callback]
                    (callback "SUBSCRIBED" nil)
                    (return channel)))
    (var client {})
    (xt/x:set-key client "channel"
                  (fn [_topic _options]
                    (return channel)))
    (xt/x:set-key client "removeChannel"
                  (fn [_channel]
                    (:= removed (+ removed 1))
                    (return true)))
    (var db (supabase-adapter/impl-supabase-js client {} {}))
    (-> (supabase-adapter/subscribe-db db "conn-a" ["shared"])
        (promise/x:promise-then
         (fn [_]
           (return (supabase-adapter/subscribe-db db "conn-b" ["shared"]))))
        (promise/x:promise-then
         (fn [_]
           (return (promise/x:promise-then
                    (supabase-adapter/unsubscribe-db db "conn-a" ["shared"])
                    (fn [first-result]
                      (var refs (xt/x:get-path db ["channels" "shared" "refs"]))
                      (return (promise/x:promise-then
                               (supabase-adapter/unsubscribe-db db "conn-b" ["shared"])
                               (fn [second-result]
                                 (repl/notify
                                  {"results" [first-result second-result]
                                   "remaining-refs" refs
                                   "removed" removed
                                   "channels" (xt/x:obj-keys (xt/x:get-key db "channels"))
                                   "connections" (xt/x:obj-keys (xt/x:get-key db "connections"))})))))))))))
  => {"results" [true true]
      "remaining-refs" 1
      "removed" 1
      "channels" []
      "connections" []})

^{:refer js.supabase.db-impl/stop-db :added "4.1"}
(fact "removes every owned channel and can be repeated without duplicate teardown"
  (notify/wait-on :js
    (var removed [])
    (var channel-a {})
    (var channel-b {})
    (var client {})
    (xt/x:set-key client "removeChannel"
                  (fn [channel]
                    (xt/x:arr-push removed channel)
                    (return true)))
    (var db {"client" client
             "channels" {"a" {"channel" channel-a}
                         "b" {"channel" channel-b}}
             "connections" {"conn" {"a" true "b" true}}
             "listeners" {"listener" {}}})
    (-> (supabase-adapter/stop-db db)
        (promise/x:promise-then
         (fn [first-result]
           (return (promise/x:promise-then
                    (supabase-adapter/stop-db db)
                    (fn [second-result]
                      (repl/notify
                       {"results" [first-result second-result]
                        "removed-count" (xt/x:len removed)
                        "channels" (xt/x:obj-keys (xt/x:get-key db "channels"))
                        "connections" (xt/x:obj-keys (xt/x:get-key db "connections"))
                        "listeners" (xt/x:obj-keys (xt/x:get-key db "listeners"))}))))))))
  => {"results" [true true]
      "removed-count" 2
      "channels" []
      "connections" []
      "listeners" []})

^{:refer js.supabase.db-impl/impl-supabase-js :added "4.1"}
(fact "registers every applicable common source protocol"
  (!.js
    (var db (supabase-adapter/impl-supabase-js {} {} {}))
    [(proto/protocol-implements db "xt.db.system.impl_common/ISourceRemote")
     (proto/protocol-implements db "xt.db.system.impl_common/ISourceListener")
     (proto/protocol-implements db "xt.db.system.impl_common/ISourceRealtime")
     (proto/protocol-implements db "xt.db.system.impl_common/ISourceLifecycle")])
  => [true true true true])

^{:refer js.supabase.db-impl/create :added "4.1"}
(fact "constructs a protocol implementation with the supabase-js client"
  (!.js
    (var db (supabase-adapter/create
             "http://127.0.0.1:54321"
             "test-anon-key"
             {}
             {}
             {}))
    {"remote" (proto/protocol-implements
               db "xt.db.system.impl_common/ISourceRemote")
     "client-from" (xt/x:is-function? (. (. db ["client"]) ["from"]))})
  => {"remote" true "client-from" true})
