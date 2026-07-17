(ns xt.db.system.impl-supabase-realtime-test
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
    (s/grant-usage #{"scratch_v0"}))

  (defn.pg ^{:- [:boolean]}
    send-realtime-roundtrip
    []
    (let [_ [:perform
             (s/realtime-send
              "room:roundtrip"
              "xt.db/event"
              {"hello" "from postgres"})]]
      (return true))))

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
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
                          [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(l/script- :lua.nginx
  {:runtime :nginx.instance
   :config {:program :resty}
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
                          [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]
             [lua.net.ws-native :as lua-websocket]]})

(fact:global
 {
  :setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "creates the connect-url"
  
  (!.js
    (realtime/prepare-connect-url
     {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}}
     {}))
  => #"ws://127.0.0.1:55121/realtime/v1/websocket")

^{:refer xt.db.system.impl-supabase-realtime/get-auth-token :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "resolves the auth token from client defaults when no session exists"

  (!.js
    (realtime/get-auth-token {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}}))
  => (-> local-min/+config+ :api :anon-key)

  (!.js
   (realtime/get-auth-token
    {"client" {"defaults" {"apikey" "anon" "token" "session-jwt"}}
     "state" {"realtimes" {}}}))
  => "session-jwt")

^{:refer xt.db.system.impl-supabase-realtime/topic-join-payload :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "builds a Phoenix join frame for a broadcast topic"

  (!.js
    (realtime/topic-join-payload {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}} "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_join"
      "ref" "#/join/realtime:room:test"
      "join_ref" "#/join/realtime:room:test"
      "payload" {"config" {"broadcast" {"ack" false "self" false}}
                 "access_token" (-> local-min/+config+ :api :anon-key)}}

  (!.js
    (realtime/topic-join-payload
     {"client" {"defaults" {"apikey" "anon" "token" "session-jwt"}}
      "state" {"realtimes" {}}}
     "realtime:User:00000000-0000-0000-0000-000000000001"))
  => (contains-in
      {"topic" "realtime:User:00000000-0000-0000-0000-000000000001"
       "payload" {"access_token" "session-jwt"
                  "config" {"private" true
                            "broadcast" {"ack" false "self" false}}}}))

^{:refer xt.db.system.impl-supabase-realtime/topic-join-payload.no-token :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "join payload omits access_token when token is nil"

  (!.js
    (realtime/topic-join-payload
     {"client" {"defaults" {"host" "127.0.0.1" "port" 55121 "secured" false}}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}}
     "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_join"
      "ref" "#/join/realtime:room:test"
      "join_ref" "#/join/realtime:room:test"
      "payload" {"config" {"broadcast" {"ack" false "self" false}}}})

^{:refer xt.db.system.impl-supabase-realtime/topic-leave-payload :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "builds a Phoenix leave frame for a broadcast topic"

  (!.js
    (realtime/topic-leave-payload {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}} "realtime:room:test"))
  => {"topic" "realtime:room:test"
      "event" "phx_leave"
      "ref" "#/leave/realtime:room:test"
      "join_ref" "#/leave/realtime:room:test"
      "payload" {}})

^{:refer xt.db.system.impl-supabase-realtime/create-realtime-on-message :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
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
      "data" 1}

  (!.js
    (var client (js-websocket/create {"id" "test"}))
    (var called nil)
    (xtd/set-in client ["state" "callbacks" "cb-1"] (fn [payload] (:= called payload)))
    (var handler (realtime/create-realtime-on-message client))
    (handler {"topic" "realtime:User:1"
              "event" "broadcast"
              "payload" {"event" "db/sync"
                         "topic" "realtime:User:1"
                         "payload" {"db/sync" {"User" [{"id" 1}]}}}})
    called)
  => {"topic" "realtime:User:1"
      "db/sync" {"User" [{"id" 1}]}})

^{:refer xt.db.system.impl-supabase-realtime/create-realtime-on-message.phx-reply :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
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
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "creates a realtime connection"

  (notify/wait-on :js
    (var client (realtime/create-realtime
                 {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}}
                 "realtime-create-test"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (websocket/disconnect client)
           (repl/notify "opened")))))
  => "opened")

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "gets a realtime connection"

  (!.js
    (realtime/get-realtime
     {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}}
     "hello"))
  => nil)

^{:refer xt.db.system.impl-supabase-realtime/set-realtime :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "stores the realtime websocket client in the impl state"

  (!.js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var client {"id" "test"})
    (realtime/set-realtime impl "test" client)
    (realtime/get-realtime impl "test"))
  => {"id" "test"})

^{:refer xt.db.system.impl-supabase-realtime/ensure-realtime :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "creates and returns the same realtime client on subsequent calls"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var client (realtime/ensure-realtime impl "default"))
    (var same-client (realtime/ensure-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (var result {"has_init" (xt/x:not-nil? (xtd/get-in client ["state" "init"]))
                        "same_client" (== client same-client)})
           (websocket/disconnect client)
           (repl/notify result)))))
  => {"has_init" true
      "same_client" true})

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "disconnects and removes the realtime client from the impl state"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var client (realtime/ensure-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (realtime/remove-realtime impl "default")
           (repl/notify {"removed" (xt/x:nil? (realtime/get-realtime impl "default"))})))))
  => {"removed" true})

^{:refer xt.db.system.impl-supabase-realtime/get-realtime-callback :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "retrieves a broadcast callback from the realtime client"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var handler (fn [event] (return event)))
    (realtime/add-realtime-callback impl "default" "cb-1" handler)
    (var client (realtime/get-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (var result {"has_callback" (xt/x:not-nil? (realtime/get-realtime-callback impl "default" "cb-1"))
                        "missing" (xt/x:nil? (realtime/get-realtime-callback impl "default" "missing"))})
           (websocket/disconnect client)
           (repl/notify result)))))
  => {"has_callback" true
      "missing" true})

^{:refer xt.db.system.impl-supabase-realtime/add-realtime-callback :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "adds a callback to the realtime client state"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var handler (fn [event] (return event)))
    (realtime/add-realtime-callback impl "default" "cb-1" handler)
    (var client (realtime/get-realtime impl "default"))
    (-> (xtd/get-in client ["state" "init"])
        (promise/x:promise-then
         (fn [_]
           (var callbacks (xtd/get-in client ["state" "callbacks"]))
           (var result {"has_callback" (xt/x:has-key? callbacks "cb-1")})
           (websocket/disconnect client)
           (repl/notify result)))))
  => {"has_callback" true})

^{:refer xt.db.system.impl-supabase-realtime/remove-realtime-callback :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "removes a callback from the realtime client state"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
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
           (var result {"before" before "after" after})
           (websocket/disconnect client)
           (repl/notify result)))))
  => {"before" true
      "after" false})

^{:refer xt.db.system.impl-supabase-realtime/get-topics :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {(js-websocket/create defaults) (dart-websocket/create (xt/x:obj-assign defaults {"background" true})) js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "returns subscribed topics for the realtime client"

  (!.js
    (realtime/get-topics {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}} "default"))
  => {}

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (-> (realtime/subscribe impl "default" ["realtime:room:topics-test"])
        (promise/x:promise-then
         (fn [_]
           (var client (realtime/get-realtime impl "default"))
           (var topics (realtime/get-topics impl "default"))
           (websocket/disconnect client)
           (repl/notify {"ready" (xtd/get-in topics ["realtime:room:topics-test" "ready"])})))))
  => {"ready" true})

^{:refer xt.db.system.impl-supabase-realtime/subscribe :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {(js-websocket/create defaults) (dart-websocket/create (xt/x:obj-assign defaults {"background" true})) js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "subscribes to topics after the websocket is initialized"

  (notify/wait-on :js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (-> (realtime/subscribe impl "default" ["realtime:room:sub-test-1"
                                            "realtime:room:sub-test-2"])
        (promise/x:promise-then
         (fn [ok]
           (var client (realtime/get-realtime impl "default"))
           (var topics (realtime/get-topics impl "default"))
           (var result {"ok" ok
                        "ready" [(xtd/get-in topics ["realtime:room:sub-test-1" "ready"])
                                 (xtd/get-in topics ["realtime:room:sub-test-2" "ready"])]})
           (websocket/disconnect client)
           (repl/notify result)))))
  => {"ok" [true true]
      "ready" [true true]})

^{:refer xt.db.system.impl-supabase-realtime/create-sync-callback :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "applies db/sync payloads to the linked caching impl"

  (!.js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var caching-impl
         {"listeners" {}
          "::/override"
          {"process_add_event"
           (fn [target data]
             (xt/x:set-key target "sync" data)
             (return true))
           "record_delete"
           (fn [target table-name ids]
             (xt/x:set-key target "remove"
                           {"table" table-name "ids" ids})
             (return true))}})
    (xtd/set-in impl ["metadata" "caching_fn"]
                (fn [] (return caching-impl)))
    (var callback (realtime/create-sync-callback impl))
    (callback {"db/sync" {"UserAccount" [{"id" 1 "name" "root"}]}})
    {"synced" (xt/x:get-key caching-impl "sync")})
  => {"synced" {"UserAccount" [{"id" 1 "name" "root"}]}})

^{:refer xt.db.system.impl-supabase-realtime/subscribe.sync :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "subscribe syncs db/sync events to a linked caching impl"

  (!.js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var caching-impl
         {"listeners" {}
          "::/override"
          {"process_add_event"
           (fn [target data]
             (xt/x:set-key target "sync" data)
             (return true))
           "record_delete"
           (fn [target table-name ids]
             (xt/x:set-key target "remove"
                           {"table" table-name "ids" ids})
             (return true))}})
    (xtd/set-in impl ["state" "caching_fn"] (fn [] (return caching-impl)))
    (var client (js-websocket/create {"id" "sync-test"}))
    (xtd/set-in client ["state" "init"] (promise/x:promise-run client))
    (xtd/set-in client ["::/override" "send"]
                (fn [_client input] (return nil)))
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
                         "payload" {"db/sync" {"UserAccount" [{"id" 1 "name" "root"}]}}}})
    {"synced" (xt/x:get-key caching-impl "sync")})
  => {"synced" {"UserAccount" [{"id" 1 "name" "root"}]}})

^{:refer xt.db.system.impl-supabase-realtime/subscribe.remove :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "subscribe syncs db/remove events to a linked caching impl"

  (!.js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var caching-impl
         {"listeners" {}
          "::/override"
          {"process_add_event"
           (fn [target data]
             (xt/x:set-key target "sync" data)
             (return true))
           "record_delete"
           (fn [target table-name ids]
             (xt/x:set-key target "remove"
                           {"table" table-name "ids" ids})
             (return true))}})
    (xtd/set-in impl ["state" "caching_fn"] (fn [] (return caching-impl)))
    (var client (js-websocket/create {"id" "sync-remove-test"}))
    (xtd/set-in client ["state" "init"] (promise/x:promise-run client))
    (xtd/set-in client ["::/override" "send"]
                (fn [_client input] (return nil)))
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
                         "payload" {"db/sync" {"UserAccount" [{"id" 1 "name" "root"}]}}}})
    (handler {"topic" "realtime:room:sync-remove-test"
              "event" "broadcast"
              "payload" {"event" "xt.db/event"
                         "topic" "realtime:room:sync-remove-test"
                         "payload" {"db/remove" {"UserAccount" ["00000000-0000-0000-0000-000000000000"]}}}})
    {"removed" (xt/x:get-key caching-impl "remove")})
  => {"removed" {"table" "UserAccount"
                 "ids" ["00000000-0000-0000-0000-000000000000"]}})

^{:refer xt.db.system.impl-supabase-realtime/unsubscribe :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "unsubscribes from topics and removes them from state"

  (notify/wait-on :js
    (var impl {"state" {"realtimes" {}}})
    (var client
         {"state" {"topics" {"realtime:room:unsub-test" {"ready" true}}}
          "::/override"
          {"send" (fn [_client input] (return input))}})
    (realtime/set-realtime impl "default" client)
    (-> (realtime/unsubscribe impl "default" ["realtime:room:unsub-test"])
        (promise/x:promise-then
         (fn [ok]
           (repl/notify {"ok" ok
                         "topics" (realtime/get-topics impl "default")})))))
  => {"ok" true
      "topics" {}}

  (notify/wait-on :js
    (var sent [])
    (var impl {"state" {"realtimes" {}}})
    (var client
         {"state" {"topics" {"realtime:User:1" {"ready" true
                                                   "refs" 2}}}
          "::/override"
          {"send" (fn [_client input]
                    (xt/x:arr-push sent input)
                    (return input))}})
    (realtime/set-realtime impl "default" client)
    (-> (realtime/unsubscribe impl "default" ["realtime:User:1"])
        (promise/x:promise-then
         (fn [_]
           (return
            (realtime/unsubscribe impl "default" ["realtime:User:1"]))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify {"sent" (xt/x:len sent)
                         "topics" (realtime/get-topics impl "default")})))))
  => {"sent" 1
      "topics" {}})

^{:refer xt.db.system.impl-supabase-realtime/subscribe.add-realtime-callback-00 :added "4.1"
  ;; The shared Supabase/Postgres delivery path is exercised once by the
  ;; primary JS seed. Native websocket delivery has its own runtime tests;
  ;; generated facts below focus on the portable realtime functions.
  :seedgen/base {:lua.nginx {:suppress true}
                 :python {:suppress true}
                 :dart {:suppress true}}
}
(fact "receives xt.db/event broadcasts from postgres after subscribing"

  (notify/wait-on :js
    (:= (!:G RT_EVENTS) [])
    (:= (!:G RT_IMPL) {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (realtime/add-realtime-callback (!:G RT_IMPL) "roundtrip" "cb-1"
                                    (fn [event]
                                      (xt/x:arr-push (!:G RT_EVENTS) event)))
    (repl/notify
     (realtime/subscribe (!:G RT_IMPL) "roundtrip" ["realtime:room:roundtrip"])))
  => [true]

  (do (send-realtime-roundtrip)
      (!.js
        RT_EVENTS))
  => (contains-in
      [{"topic" "realtime:room:roundtrip"
        "hello" "from postgres"}]))

^{:refer xt.db.system.impl-supabase-realtime/create-sync-callback.remove :added "4.1"
  :seedgen/base {:lua.nginx {:transform (quote {js-websocket/create lua-websocket/create js-websocket/connect-ws lua-websocket/connect-ws})}
                 :python {:transform (quote {js-websocket/create py-websocket/create js-websocket/connect-ws py-websocket/connect-ws})}
                 :dart {:transform (quote {js-websocket/create dart-websocket/create js-websocket/connect-ws dart-websocket/connect-ws})}}
}
(fact "applies db/remove payloads to the linked caching impl"

  (!.js
    (var impl {"client" {"defaults" (xt/x:obj-assign (@! local-min/+config-supabase-anon+) {})}
      "state" {"realtimes" {}}
      "::/override"
      {"create_ws_client"
       (fn [_impl defaults]
         (return (js-websocket/create defaults)))}})
    (var caching-impl
         {"listeners" {}
          "::/override"
          {"process_add_event"
           (fn [target data]
             (xt/x:set-key target "sync" data)
             (return true))
           "record_delete"
           (fn [target table-name ids]
             (xt/x:set-key target "remove"
                           {"table" table-name "ids" ids})
             (return true))}})
    (xtd/set-in impl ["state" "caching_fn"]
                (fn [] (return caching-impl)))
    (var callback (realtime/create-sync-callback impl))
    (callback {"db/sync" {"UserAccount" [{"id" 1 "name" "root"}]}})
    (callback {"db/remove" {"UserAccount" ["00000000-0000-0000-0000-000000000000"]}})
    {"removed" (xt/x:get-key caching-impl "remove")})
  => {"removed" {"table" "UserAccount"
                 "ids" ["00000000-0000-0000-0000-000000000000"]}})
