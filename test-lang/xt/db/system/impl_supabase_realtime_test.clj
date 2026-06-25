(ns xt.db.system.impl-supabase-realtime-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [std.lib.network :as network]))

(defn wait-for-postgrest
  []
  (network/wait-for-port
   (-> local-min/+config+ :api :hostname)
   (-> local-min/+config+ :api :port)))

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
             [js.net.ws-native :as js-ws]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.system.main :as main]
             [xt.db.system.impl-supabase-realtime :as realtime]
             [xt.net.addon-supabase :as addon]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)
          (wait-for-postgrest)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js default-client
  []
  (return
   (js-fetch/create
    {:host (@! (-> local-min/+config+ :api :hostname))
     :port (@! (-> local-min/+config+ :api :port))
     :secured false
     :apikey (@! (-> local-min/+config+ :api :anon-key))}
    (addon/middleware-supabase))))

(defn.js default-impl
  []
  (var client (-/default-client))
  (return (main/create-impl "supabase"
                            (xt/x:get-key client "defaults")
                            nil
                            nil)))

(defn.js fake-client
  "creates a ws-native client with a mocked raw socket for testing"
  {:added "4.1"}
  []
  (var client (js-ws/create {}))
  (var sent [])
  (var closed [])
  (var listeners {})
  (xt/x:set-key client "raw"
                {"send" (fn [payload]
                          (xt/x:arr-push sent payload)
                          (return true))
                 "close" (fn [code reason]
                           (xt/x:arr-push closed [code reason])
                           (return true))
                 "addEventListener" (fn [event handler]
                                      (xt/x:set-key listeners event handler)
                                      (return true))})
  (xt/x:set-key client "sent" sent)
  (xt/x:set-key client "closed" closed)
  (xt/x:set-key client "listeners" listeners)
  (return client))

(defn.js fake-realtime
  "creates a realtime wrapper around a fake client"
  {:added "4.1"}
  [client impl]
  (return {"::" "xt.db.system.impl_supabase_realtime/RealtimeClient"
          "id" "default"
          "impl" (or impl {})
          "client" client
          "id_counter" 0}))

^{:refer xt.db.system.impl-supabase-realtime/get-realtime :added "4.1"}
(fact "returns the realtime client for an id"

  (!.js
   (var impl {"state" {"realtime" {"default" {"id" "default"}}}})
   (realtime/get-realtime impl "default"))
  => {"id" "default"}

  (!.js
   (var impl {"state" {"realtime" {}}})
   (realtime/get-realtime impl "default"))
  => nil)

^{:refer xt.db.system.impl-supabase-realtime/ensure-realtime :added "4.1"}
(fact "connects to the local supabase realtime websocket"

  (notify/wait-on :js
    (var impl (-/default-impl))
    (var rt (realtime/ensure-realtime impl "default" {}))
    (var client (xt/x:get-key rt "client"))
    (promise/x:with-delay 500
      (fn []
        (var defaults (xt/x:get-key client "defaults"))
        (repl/notify {"connected" (xt/x:not-nil? (xt/x:get-key client "raw"))
                      "url" (xt/x:get-key defaults "url")}))))
  => (contains-in {"connected" true
                   "url" string?}))

^{:refer xt.db.system.impl-supabase-realtime/route-frame :added "4.1"}
(fact "routes xt.db/event broadcast payloads to the topic callback"

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (realtime/route-frame realtime
                         {"topic" "User:u-1"
                          "event" "broadcast"
                          "payload" {"event" "xt.db/event"
                                     "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}})
   captured)
  => [{"db/sync" {"User" [{"id" "u-1"}]}}]

  (!.js
   (var captured [])
   (var client {"state" {"pubsub" {"topics" {"User:u-1" {"callback" (fn [event] (xt/x:arr-push captured event))}}}}})
   (var realtime {"client" client})
   (realtime/route-frame realtime
                         {"topic" "User:u-1"
                          "event" "broadcast"
                          "payload" {"event" "some-other-event"
                                     "payload" {"db/sync" {"User" [{"id" "u-1"}]}}}})
   captured)
  => [])


^{:refer xt.db.system.impl-supabase-realtime/prepare-connect-url :added "4.1"}
(fact "prepares the websocket url used to connect to realtime"

  (!.js
    [(realtime/prepare-connect-url {"host" "127.0.0.1"
                                    "port" 55121
                                    "secured" false
                                    "api_key" "test-key"})
     (realtime/prepare-connect-url {"host" "127.0.0.1"
                                    "port" 443
                                    "secured" true
                                    "params" {"custom" "value"}})
     (realtime/prepare-connect-url {"host" "127.0.0.1"
                                    "websocket_url" "wss://custom.host/rt"})])
  => ["ws://127.0.0.1:55121/realtime/v1/websocket?vsn=2.0.0&apikey=test-key"
      "wss://127.0.0.1:443/realtime/v1/websocket?vsn=2.0.0&custom=value"
      "wss://custom.host/rt?vsn=2.0.0"])

^{:refer xt.db.system.impl-supabase-realtime/resolve-api-key :added "4.1"}
(fact "resolves the api key for websocket auth"

  (!.js
    [(realtime/resolve-api-key {"impl" {"client" {"defaults" {"apikey" "default-key"}}}}
                               {"apikey" "opt-key"})
     (realtime/resolve-api-key {"impl" {"client" {"defaults" {"apikey" "default-key"}}}}
                               {})
     (realtime/resolve-api-key {"impl" {"client" {"defaults" {}}}}
                               {})])
  => ["opt-key" "default-key" nil])

^{:refer xt.db.system.impl-supabase-realtime/resolve-auth-token :added "4.1"}
(fact "resolves the realtime auth token from the impl session or client defaults"

  (!.js
    [(realtime/resolve-auth-token {"impl" {"state" {"session" {"access_token" "session-token"}}
                                           "client" {"defaults" {"token" "default-token"}}}}
                                  {"token" "opt-token"})
     (realtime/resolve-auth-token {"impl" {"state" {"session" {"access_token" "session-token"}}
                                           "client" {"defaults" {"token" "default-token"}}}}
                                  {})
     (realtime/resolve-auth-token {"impl" {"state" {}
                                           "client" {"defaults" {"token" "default-token"}}}}
                                  {})
     (realtime/resolve-auth-token {"impl" {"state" {}
                                           "client" {"defaults" {}}}}
                                  {})])
  => ["opt-token" "session-token" "default-token" nil])

^{:refer xt.db.system.impl-supabase-realtime/broadcast-join-payload :added "4.1"}
(fact "builds the Phoenix join payload for a broadcast-only channel"

  (!.js
    [(realtime/broadcast-join-payload {"impl" {}} {})
     (realtime/broadcast-join-payload {"impl" {"client" {"defaults" {"token" "default-token"}}}}
                                      {})
     (realtime/broadcast-join-payload {"impl" {}}
                                      {"join_payload" {"config" {"broadcast" {"self" true}}}})])
  => [{"config" {"broadcast" {"ack" false "self" false}
                 "presence" {"key" ""}}}
      {"config" {"broadcast" {"ack" false "self" false}
                 "presence" {"key" ""}}
       "access_token" "default-token"}
      {"config" {"broadcast" {"self" true}}}])

^{:refer xt.db.system.impl-supabase-realtime/client-topics :added "4.1"}
(fact "returns the topic map from the websocket client state"

  (!.js
    [(realtime/client-topics {"state" {"pubsub" {"topics" {"User:u-1" {"id" "sub-0"}}}}})
     (realtime/client-topics {"state" {"pubsub" {}}})
     (realtime/client-topics {"state" {}})])
  => [{"User:u-1" {"id" "sub-0"}}
      {}
      {}])

^{:refer xt.db.system.impl-supabase-realtime/topic-entry :added "4.1"}
(fact "gets the subscription entry for a Phoenix topic"

  (!.js
    [(realtime/topic-entry {"state" {"pubsub" {"topics" {"User:u-1" {"id" "sub-0"}}}}}
                            "User:u-1")
     (realtime/topic-entry {"state" {"pubsub" {"topics" {"User:u-1" {"id" "sub-0"}}}}}
                            "User:u-2")])
  => [{"id" "sub-0"} nil])

^{:refer xt.db.system.impl-supabase-realtime/on-open :added "4.1"}
(fact "sends pending join frames and starts heartbeat when the socket opens"

  (!.js
    (var client (-/fake-client))
    (xtd/set-in client ["state" "pubsub" "topics" "User:u-1"]
                {"join_frame" (phoenix/make-frame-join client
                                                         {}
                                                         {"topic" "User:u-1"})})
    (var realtime (-/fake-realtime client {}))
    (var result (realtime/on-open client realtime))
    {"opened" result
     "sent_count" (xt/x:len (xt/x:get-key client "sent"))
     "has_heartbeat" (xt/x:not-nil? (xtd/get-in client ["state" "heartbeats" "pubsub"]))})
  => {"opened" true
      "sent_count" 1
      "has_heartbeat" true})

^{:refer xt.db.system.impl-supabase-realtime/subscribe :added "4.1"}
(fact "subscribes to a broadcast topic on the realtime websocket"

  (!.js
    (var client (-/fake-client))
    (var realtime (-/fake-realtime client {}))
    (var captured [])
    (var handle (realtime/subscribe realtime
                                    "User:u-1"
                                    {}
                                    (fn [event] (xt/x:arr-push captured event))))
    {"topic" (xt/x:get-key handle "topic")
     "active" (xt/x:get-key handle "active")
     "sent_count" (xt/x:len (xt/x:get-key client "sent"))
     "has_topic" (xt/x:not-nil? (xtd/get-in client ["state" "pubsub" "topics" "User:u-1"]))})
  => {"topic" "User:u-1"
      "active" true
      "sent_count" 1
      "has_topic" true})

^{:refer xt.db.system.impl-supabase-realtime/unsubscribe :added "4.1"}
(fact "leaves a topic on the realtime websocket"

  (!.js
    (var client (-/fake-client))
    (var realtime (-/fake-realtime client {}))
    (realtime/subscribe realtime
                        "User:u-1"
                        {}
                        (fn [event] (return event)))
    (var handle {"topic" "User:u-1" "active" true})
    (realtime/unsubscribe realtime handle)
    {"active" (xt/x:get-key handle "active")
     "sent_count" (xt/x:len (xt/x:get-key client "sent"))
     "has_topic" (xt/x:not-nil? (xtd/get-in client ["state" "pubsub" "topics" "User:u-1"]))})
  => {"active" false
      "sent_count" 2
      "has_topic" false})

^{:refer xt.db.system.impl-supabase-realtime/publish :added "4.1"}
(fact "publishing is not supported by the supabase realtime abstraction"

  (notify/wait-on :js
    (var client (-/fake-client))
    (var realtime (-/fake-realtime client {}))
    (-> (realtime/publish realtime "User:u-1" {"db/sync" {}} {})
        (promise/x:promise-then
         (fn [result]
           (repl/notify {"result" result
                         "sent_count" (xt/x:len (xt/x:get-key client "sent"))})))))
  => {"result" nil
      "sent_count" 0})