(ns xt.db.system.impl-supabase-realtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.network :as network]
            [net.http.websocket :as ws]))

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

^{:seedgen/root {:all true
                 :langs [:js :lua.nginx :python :dart]
                 :js {:extra [[js.net.ws-native :as js-websocket]]}
                 :lua.nginx {:extra [[lua.net.ws-native :as lua-websocket]]}
                 :python {:extra [[python.net.ws-native :as py-websocket]]}
                 :dart {:extra [[dart.net.ws-native :as dart-websocket]]}}}
(l/script- :js
  {:runtime :basic
   :require [^{:seedgen/extra true}
             [js.net.ws-native :as js-websocket]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.db.helpers.data-main-test :as sample]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :config {:program :resty}
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.impl-memory :as impl-memory]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.db.helpers.data-main-test :as sample]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]
             [lua.net.ws-native :as lua-websocket]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "creates the connect-url"
  
  (!.js
    (realtime/prepare-connect-url
     (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup)
     {}))
  => #"ws://127.0.0.1:55121/realtime/v1/websocket")

^{:refer xt.db.system.impl-supabase-realtime/get-auth-token :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "resolves the auth token from client defaults when no session exists"

  (!.js
    (realtime/get-auth-token (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup)))
  => (-> local-min/+config+ :api :anon-key))

^{:refer xt.db.system.impl-supabase-realtime/topic-join-payload :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "builds a Phoenix join frame for a broadcast topic"

  (!.js
    (realtime/topic-join-payload (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup) "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_join"
      "ref" "#/join/realtime:room:test"
      "join_ref" "#/join/realtime:room:test"
      "payload" {"config" {"broadcast" {"ack" false "self" false}}
                 "access_token" (-> local-min/+config+ :api :anon-key)}})

^{:refer xt.db.system.impl-supabase-realtime/topic-join-payload.no-token :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "join payload omits access_token when token is nil"

  (!.js
    (realtime/topic-join-payload
     (main/create-impl "supabase"
                       {"host" "127.0.0.1" "port" 55121 "secured" false}
                       -/Schema
                       -/SchemaLookup)
     "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_join"
      "ref" "#/join/realtime:room:test"
      "join_ref" "#/join/realtime:room:test"
      "payload" {"config" {"broadcast" {"ack" false "self" false}}
                 "access_token" nil}})

^{:refer xt.db.system.impl-supabase-realtime/topic-leave-payload :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "builds a Phoenix leave frame for a broadcast topic"

  (!.js
    (realtime/topic-leave-payload (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup) "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_leave"
      "ref" "#/leave/realtime:room:test"
      "join_ref" "#/leave/realtime:room:test"
      "payload" {}})

^{:refer xt.db.system.impl-supabase-realtime/create-realtime-on-message :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "dispatches xt.db/event broadcasts to topic callbacks"

  (!.js
    (var client (js-websocket/create {"id" "test"}))
    (var called nil)
    (xtd/set-in client ["state" "callbacks" "cb-1"] (fn [payload] (:= called payload)))
    (var handler (realtime/create-realtime-on-message client))
    (handler {"topic" "realtime:room:test"
              "event" "broadcast"
              "payload" {"event" "xt.db/event"
                         "topic" "realtime:room:test"
                         "payload" {"data" 1}}})
    called)
  => {"topic" "realtime:room:test"
      "data" 1})

^{:refer xt.db.system.impl-supabase-realtime/create-realtime-on-message.phx-reply :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "phx_reply resolves the topic init promise and marks the topic ready"

  (notify/wait-on :js
    (var client (js-websocket/create {"id" "phx-test"}))
    (var deferred {"resolve" nil "reject" nil})
    (var init (promise/x:promise-new
               (fn [resolve reject]
                 (xt/x:set-key deferred "resolve" resolve)
                 (xt/x:set-key deferred "reject" reject))))
    (xtd/set-in client ["state" "topics" "realtime:room:phx-test"]
                {"init" init
                 "join_ref" "#/join/realtime:room:phx-test"
                 "deferred" deferred
                 "ready" false})
    (var handler (realtime/create-realtime-on-message client))
    (handler {"topic" "realtime:room:phx-test"
              "event" "phx_reply"
              "ref" "#/join/realtime:room:phx-test"
              "join_ref" "#/join/realtime:room:phx-test"
              "payload" {"status" "ok"
                         "response" {"postgres_changes" []}}})
    (repl/notify
     {"resolved" true
      "ready" (xtd/get-in client ["state" "topics" "realtime:room:phx-test" "ready"])}))
  => {"resolved" true
      "ready" true})

^{:refer xt.db.system.impl-supabase-realtime/create-realtime :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "creates a realtime connection"

  (let [p (promise)]
    (ws/websocket
     (!.js
       (realtime/prepare-connect-url
        (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup)
        {}))
     {:on-open (fn [& args]
                 (deliver p "opened"))})
    @p)
  => "opened"

  (notify/wait-on :js
    (var client (realtime/create-realtime
                 (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup)
                 (xts/str-rand 8)))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify "opened")))))
  => "opened"
  
  (notify/wait-on :js
    (var client
         (js-websocket/create (@! local-min/+config-supabase-anon+)))
    (var joined false)
    (js-websocket/connect-ws client
                             {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                       (@! (-> local-min/+config+ :api :anon-key)))})
    (websocket/add-listeners
     client
     {"open"
      (fn [_]
        (phoenix/send-frame
         client
         (phoenix/make-frame-join
          {"config" {"broadcast" {"ack" false "self" false}}}
          {"topic" "realtime:room:send-join-test"
           "ref" "join-1"})))
      "message"
      (phoenix/wrap-phoenix
       {"phx_reply"
        (fn [frame]
          (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                     (not joined))
            (:= joined true)
            (websocket/disconnect client)
            (repl/notify frame)))})})
    true)
  => {"event" "phx_reply", "ref" "join-1", "payload" {"status" "ok", "response" {"postgres_changes" []}}, "topic" "realtime:room:send-join-test"})

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "gets a realtime connection"

  (!.js
    (realtime/get-realtime
     (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup)
     "hello"))
  => nil)

^{:refer xt.db.system.impl-supabase-realtime/set-realtime :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "stores the realtime websocket client in the impl state"

  (!.js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var client {"id" "test"})
    (realtime/set-realtime impl "test" client)
    (realtime/get-realtime impl "test"))
  => {"id" "test"})

^{:refer xt.db.system.impl-supabase-realtime/ensure-realtime :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "creates and returns the same realtime client on subsequent calls"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var client (realtime/ensure-realtime impl "default"))
    (var same-client (realtime/ensure-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify {"has_init" (xt/x:not-nil? (xtd/get-in client ["state" "init"]))
                         "same_client" (== client same-client)})))))
  => {"has_init" true
      "same_client" true})

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "disconnects and removes the realtime client from the impl state"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var client (realtime/ensure-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (realtime/remove-realtime impl "default")
           (repl/notify {"removed" (xt/x:nil? (realtime/get-realtime impl "default"))})))))
  => {"removed" true})

^{:refer xt.db.system.impl-supabase-realtime/get-realtime-callback :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "retrieves a broadcast callback from the realtime client"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var handler (fn [event] (return event)))
    (realtime/add-realtime-callback impl "default" "cb-1" handler)
    (var client (realtime/get-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify {"has_callback" (xt/x:not-nil? (realtime/get-realtime-callback impl "default" "cb-1"))
                         "missing" (xt/x:nil? (realtime/get-realtime-callback impl "default" "missing"))})))))
  => {"has_callback" true
      "missing" true})

^{:refer xt.db.system.impl-supabase-realtime/add-realtime-callback :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "adds a callback to the realtime client state"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var handler (fn [event] (return event)))
    (realtime/add-realtime-callback impl "default" "cb-1" handler)
    (var client (realtime/get-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (var callbacks (xtd/get-in client ["state" "callbacks"]))
           (repl/notify {"has_callback" (xt/x:has-key? callbacks "cb-1")})))))
  => {"has_callback" true})

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime-callback :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "removes a callback from the realtime client state"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var handler (fn [event] (return event)))
    (realtime/add-realtime-callback impl "default" "cb-1" handler)
    (var client (realtime/get-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (var callbacks (xtd/get-in client ["state" "callbacks"]))
           (var before (xt/x:has-key? callbacks "cb-1"))
           (realtime/remove-realtime-callback impl "default" "cb-1")
           (var after (xt/x:has-key? callbacks "cb-1"))
           (repl/notify {"before" before
                         "after" after})))))
  => {"before" true
      "after" false})

^{:refer xt.db.system.impl-supabase-realtime/get-topics :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "returns subscribed topics for the realtime client"

  (!.js
    (realtime/get-topics (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup) "default"))
  => {}

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (-> (realtime/subscribe impl "default" ["realtime:room:topics-test"])
        (promise/x:promise-then
         (fn [_]
           (repl/notify (realtime/get-topics impl "default"))))))
  => (contains-in
      {"realtime:room:topics-test" {"ready" true}}))

^{:refer xt.db.system.impl-supabase-realtime/subscribe :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "subscribes to topics after the websocket is initialized"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (-> (realtime/subscribe impl "default" ["realtime:room:sub-test-1"
                                            "realtime:room:sub-test-2"])
        (promise/x:promise-then
         (fn [ok]
           (repl/notify {"ok" ok
                         "topics" (realtime/get-topics impl "default")})))))
  => (contains-in
      {"ok" [true true]
       "topics" {"realtime:room:sub-test-1" {"ready" true}
                 "realtime:room:sub-test-2" {"ready" true}}}))

^{:refer xt.db.system.impl-supabase-realtime/subscribe.await-topic-init :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "subscribe waits for each topic init promise to resolve"

  (notify/wait-on :js
    (promise/x:promise-then
     (do
       (var impl (main/create-impl "supabase"
                                  (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                                  -/Schema
                                  -/SchemaLookup))
       (var client (js-websocket/create {"id" "subscribe-test"}))
       (xtd/set-in client ["state" "init"] (promise/x:promise-run client))
       (xtd/set-in client ["raw"] {"send" (fn [input] nil)})
       (xtd/set-in impl ["state" "realtimes" "default"] client)
       (var sub (realtime/subscribe impl "default" ["realtime:room:isolated-test"]))
       (var deferred (xtd/get-in client ["state" "topics" "realtime:room:isolated-test" "deferred"]))
       ((xtd/get-in deferred ["resolve"]) true)
       (xtd/set-in client ["state" "topics" "realtime:room:isolated-test" "ready"] true)
       (:= (!:G RT_ISOLATED_CLIENT) client)
       sub)
     (fn [ok]
       (repl/notify {"ok" ok
                     "ready" (xtd/get-in (!:G RT_ISOLATED_CLIENT)
                                         ["state" "topics" "realtime:room:isolated-test" "ready"])}))))
  => {"ok" [true]
      "ready" true})

^{:refer xt.db.system.impl-supabase-realtime/create-sync-callback :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "applies db/sync payloads to the linked caching impl"

  (!.js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var caching-impl (impl-memory/impl-memory sample/Schema sample/SchemaLookup))
    (xtd/set-in impl ["state" "caching_fn"]
                (fn [] (return caching-impl)))
    (var callback (realtime/create-sync-callback impl))
    (callback {"db/sync" {"UserAccount" [sample/RootUser]}})
    {"synced" (impl-common/pull caching-impl ["UserAccount" {"id" "00000000-0000-0000-0000-000000000000"} ["nickname"]])})
  => (contains-in
      {"synced" [{"nickname" "root"}]}))

^{:refer xt.db.system.impl-supabase-realtime/subscribe.sync :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "subscribe syncs db/sync events to a linked caching impl"

  (!.js
    (var impl (main/create-impl "supabase"
                               (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                               -/Schema
                               -/SchemaLookup))
    (var caching-impl (impl-memory/impl-memory sample/Schema sample/SchemaLookup))
    (xtd/set-in impl ["state" "caching_fn"] (fn [] (return caching-impl)))
    (var client (js-websocket/create {"id" "sync-test"}))
    (xtd/set-in client ["state" "init"] (promise/x:promise-run client))
    (xtd/set-in client ["raw"] {"send" (fn [input] nil)})
    (xtd/set-in impl ["state" "realtimes" "default"] client)
    (realtime/subscribe impl "default" ["realtime:room:sync-test"])
    (var deferred (xtd/get-in client ["state" "topics" "realtime:room:sync-test" "deferred"]))
    ((xtd/get-in deferred ["resolve"]) true)
    (xtd/set-in client ["state" "topics" "realtime:room:sync-test" "ready"] true)
    (var handler (realtime/create-realtime-on-message client))
    (handler {"topic" "realtime:room:sync-test"
              "event" "broadcast"
              "payload" {"event" "xt.db/event"
                         "topic" "realtime:room:sync-test"
                         "payload" {"db/sync" {"UserAccount" [sample/RootUser]}}}})
    {"synced" (impl-common/pull caching-impl ["UserAccount" {"id" "00000000-0000-0000-0000-000000000000"} ["nickname"]])
     "events" (xtd/get-in caching-impl ["rows" "UserAccount" "00000000-0000-0000-0000-000000000000" "record" "data"])})
  => (contains-in
      {"synced" [{"nickname" "root"}]}))

^{:refer xt.db.system.impl-supabase-realtime/subscribe.remove :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}
                 :ruby {:suppress true}}}
(fact "subscribe syncs db/remove events to a linked caching impl"

  (!.js
    (var impl (main/create-impl "supabase"
                               (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                               -/Schema
                               -/SchemaLookup))
    (var caching-impl (impl-memory/impl-memory sample/Schema sample/SchemaLookup))
    (xtd/set-in impl ["state" "caching_fn"] (fn [] (return caching-impl)))
    (var client (js-websocket/create {"id" "sync-remove-test"}))
    (xtd/set-in client ["state" "init"] (promise/x:promise-run client))
    (xtd/set-in client ["raw"] {"send" (fn [input] nil)})
    (xtd/set-in impl ["state" "realtimes" "default"] client)
    (realtime/subscribe impl "default" ["realtime:room:sync-remove-test"])
    (var deferred (xtd/get-in client ["state" "topics" "realtime:room:sync-remove-test" "deferred"]))
    ((xtd/get-in deferred ["resolve"]) true)
    (xtd/set-in client ["state" "topics" "realtime:room:sync-remove-test" "ready"] true)
    (var handler (realtime/create-realtime-on-message client))
    (handler {"topic" "realtime:room:sync-remove-test"
              "event" "broadcast"
              "payload" {"event" "xt.db/event"
                         "topic" "realtime:room:sync-remove-test"
                         "payload" {"db/sync" {"UserAccount" [sample/RootUser]}}}})
    (handler {"topic" "realtime:room:sync-remove-test"
              "event" "broadcast"
              "payload" {"event" "xt.db/event"
                         "topic" "realtime:room:sync-remove-test"
                         "payload" {"db/remove" {"UserAccount" ["00000000-0000-0000-0000-000000000000"]}}}})
    {"remaining" (impl-common/pull caching-impl ["UserAccount" {} ["nickname"]])})
  => (contains-in
      {"remaining" []}))

^{:refer xt.db.system.impl-supabase-realtime/unsubscribe :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "unsubscribes from topics and removes them from state"

  (notify/wait-on :js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (-> (realtime/subscribe impl "default" ["realtime:room:unsub-test"])
        (promise/x:promise-then
         (fn [_]
           (-> (realtime/unsubscribe impl "default" ["realtime:room:unsub-test"])
               (promise/x:promise-then
                (fn [ok]
                  (repl/notify {"ok" ok
                                "topics" (realtime/get-topics impl "default")}))))))))
  => {"ok" true
      "topics" {}})

^{:refer xt.db.system.impl-supabase-realtime/subscribe.add-realtime-callback-00 :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "receives xt.db/event broadcasts from postgres after subscribing"

  (notify/wait-on :js
    (:= (!:G RT_EVENTS) [])
    (:= (!:G RT_IMPL) (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (realtime/add-realtime-callback (!:G RT_IMPL) "roundtrip" "cb-1"
                                    (fn [event]
                                      (xt/x:arr-push (!:G RT_EVENTS) event)))
    (repl/notify
     (realtime/subscribe (!:G RT_IMPL) "roundtrip" ["realtime:room:roundtrip"])))
  => [true]

  (do (!.pg
        (s/realtime-send "room:roundtrip" "xt.db/event" {"hello" "from postgres"}))
      (!.js
        RT_EVENTS))
  => (contains-in
      [{"topic" "realtime:room:roundtrip"
        "hello" "from postgres"}]))

^{:refer xt.db.system.impl-supabase-realtime/subscribe.add-realtime-callback-01 :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "receives db/sync payloads from postgres"

  (notify/wait-on :js
    (:= (!:G RT_EVENTS) [])
    (:= (!:G RT_IMPL) (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (realtime/add-realtime-callback (!:G RT_IMPL) "sync-test" "cb-1"
                                    (fn [event]
                                      (xt/x:arr-push (!:G RT_EVENTS) event)))
    (repl/notify
     (realtime/subscribe (!:G RT_IMPL) "sync-test" ["realtime:room:sync-test"])))
  => [true]

  (do (!.pg
        (s/realtime-send "room:sync-test" "xt.db/event"
                         {"db/sync" {"User" [{"id" 1 "name" "Alice"}]}}))
      (!.js
        RT_EVENTS))
  => (contains-in
      [{"topic" "realtime:room:sync-test"
        "db/sync" {"User" [{"id" 1 "name" "Alice"}]}}]))

^{:refer xt.db.system.impl-supabase-realtime/add-realtime-callback-02 :added "4.1"

  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "receives db/remove payloads from postgres"

  (notify/wait-on :js
    (:= (!:G RT_EVENTS) [])
    (:= (!:G RT_IMPL) (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (realtime/add-realtime-callback (!:G RT_IMPL) "remove-test" "cb-1"
                                    (fn [event]
                                      (xt/x:arr-push (!:G RT_EVENTS) event)))
    (repl/notify
     (realtime/subscribe (!:G RT_IMPL) "remove-test" ["realtime:room:remove-test"])))
  => [true]

  (do (!.pg
        (s/realtime-send "room:remove-test" "xt.db/event"
                         {"db/remove" {"User" [1 2 3]}}))
      (!.js
        RT_EVENTS))
  => (contains-in
      [{"topic" "realtime:room:remove-test"
        "db/remove" {"User" [1 2 3]}}]))


^{:refer xt.db.system.impl-supabase-realtime/create-sync-callback.remove :added "4.1"
  ;; ruby: suppressed - needs local supabase infra + ruby http-fetch/create-client adapters
  :seedgen/base {:ruby {:suppress true}}}
(fact "applies db/remove payloads to the linked caching impl"

  (!.js
    (var impl (main/create-impl "supabase"
                       (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})
                       -/Schema
                       -/SchemaLookup))
    (var caching-impl (impl-memory/impl-memory sample/Schema sample/SchemaLookup))
    (xtd/set-in impl ["state" "caching_fn"]
                (fn [] (return caching-impl)))
    (var callback (realtime/create-sync-callback impl))
    (callback {"db/sync" {"UserAccount" [sample/RootUser]}})
    (callback {"db/remove" {"UserAccount" ["00000000-0000-0000-0000-000000000000"]}})
    {"remaining" (impl-common/pull caching-impl ["UserAccount" {} ["nickname"]])})
  => (contains-in
      {"remaining" []}))