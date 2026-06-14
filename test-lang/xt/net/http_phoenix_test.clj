(ns xt.net.ws-phoenix-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.docker-min :as docker-min]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch-v0]
             [postgres.core :as pg]
             [postgres.core.supabase :as s]]
   :config {:host   (-> docker-min/+config+ :db :host)
            :port   (-> docker-min/+config+ :db :port)
            :user   (-> docker-min/+config+ :db :user)
            :pass   (-> docker-min/+config+ :db :password)
            :dbname (-> docker-min/+config+ :db :database)
            :startup  docker-min/start-supabase
            :shutdown docker-min/stop-supabase}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-websocket :as js-ws]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(!.pg
  ())


^{:refer xt.net.ws-phoenix/extract-message-data :added "4.1"}
(fact "extracts text from raw websocket payload wrappers"

  (!.js
    [(phoenix/extract-message-data "plain")
     (phoenix/extract-message-data {"data" "{\"ok\":true}"})
     (phoenix/extract-message-data {"body" "fallback"})])
  => ["plain" "{\"ok\":true}" "fallback"])

^{:refer xt.net.ws-phoenix/decode-frame :added "4.1"}
(fact "decodes websocket payload text into a phoenix frame"

  (!.js
    [(phoenix/decode-frame "{\"event\":\"phx_reply\",\"topic\":\"realtime:room:test\"}")
     (phoenix/decode-frame {"data" "{\"payload\":{\"status\":\"ok\"}}"})])
  => [{"event" "phx_reply",
       "topic" "realtime:room:test"}
      {"payload" {"status" "ok"}}])

^{:refer xt.net.http-phoenix/get-frame-ref :added "4.1"}
(fact "uses the explicit ref when provided"

  (!.js
    (phoenix/get-frame-ref nil {"ref" "join-1"}))
  => "join-1")

^{:refer xt.net.http-phoenix/make-frame :added "4.1"}
(fact "builds a generic phoenix frame"

  (!.js
    (phoenix/make-frame nil "realtime:room:test" "broadcast" {"hello" true} {"ref" "push-1"}))
  => {"topic" "realtime:room:test"
      "event" "broadcast"
      "payload" {"hello" true}
      "ref" "push-1"
      "join_ref" "push-1"})

^{:refer xt.net.http-phoenix/make-frame-join :added "4.1"}
(fact "builds a join frame from a topic and payload"

  (!.js
    (phoenix/make-frame-join nil
                             {"config" {}}
                             {"topic" "realtime:room:test"
                              "ref" "join-1"}))
  => {"topic" "realtime:room:test"
      "event" "phx_join"
      "payload" {"config" {}}
      "ref" "join-1"
      "join_ref" "join-1"})

^{:refer xt.net.http-phoenix/make-frame-leave :added "4.1"}
(fact "builds a leave frame from a topic"

  (!.js
    (phoenix/make-frame-leave nil
                              {"topic" "realtime:room:test" "ref" "leave-1"}))
  => {"topic" "realtime:room:test"
      "event" "phx_leave"
      "payload" {}
      "ref" "leave-1"
      "join_ref" "leave-1"})

^{:refer xt.net.http-phoenix/send-join :added "4.1"}
(fact "serialises and sends a join frame"

  (!.js)
  
  (!.js
    (var sent [])
    (with-redefs [websocket/send
                  (fn [_ input]
                    (xt/x:arr-push sent input)
                    (return input))]
      (phoenix/send-join
       {}
       {"config" {"broadcast" {"ack" false "self" false}}
        "access_token" "token-1"}
       {"topic" "realtime:room:test" "ref" "join-1"})
      (xt/x:first sent)))
  => "{\"topic\":\"realtime:room:test\",\"event\":\"phx_join\",\"payload\":{\"config\":{\"broadcast\":{\"ack\":false,\"self\":false}},\"access_token\":\"token-1\"},\"ref\":\"join-1\",\"join_ref\":\"join-1\"}")

^{:refer xt.net.http-phoenix/send-leave :added "4.1"}
(fact "serialises and sends a leave frame"
  (!.js
   (var sent [])
   (with-redefs [websocket/send
                 (fn [_ input]
                   (xt/x:arr-push sent input)
                   (return input))]
     (phoenix/send-leave
      {}
      {"topic" "realtime:room:test" "ref" "leave-1"})
     (xt/x:first sent)))
  => "{\"topic\":\"realtime:room:test\",\"event\":\"phx_leave\",\"payload\":{},\"ref\":\"leave-1\",\"join_ref\":\"leave-1\"}")

^{:refer xt.net.http-phoenix/send :added "4.1"}
(fact "serialises and sends a push frame"
  (!.js
   (var sent [])
   (with-redefs [websocket/send
                 (fn [_ input]
                   (xt/x:arr-push sent input)
                   (return input))]
     (phoenix/send
      {}
      "broadcast"
      {"hello" true}
      {"topic" "realtime:room:test" "ref" "push-1"})
     (xt/x:first sent)))
  => "{\"topic\":\"realtime:room:test\",\"event\":\"broadcast\",\"payload\":{\"hello\":true},\"ref\":\"push-1\",\"join_ref\":\"push-1\"}")

^{:refer xt.net.http-phoenix/send :added "4.1"
  :setup []}
(fact "connects to the local Supabase realtime websocket and receives a broadcast after a pg send"
  (notify/wait-on [:js 10000]
    (var cache
         (db-main/create-impl
          "memory"
          {}
          (@! fixtures/+schema+)
          (@! fixtures/+lookup+)))
    (var topic "room:http-phoenix")
    (var requests [])
    (var statuses [])
    (var client
         (event-supabase/broadcast-client
           {"transport" (js-ws/driver {})
           "base_url" (xt/x:cat (or (-> docker-min/+config+ :api :protocol) "http")
                                 "://"
                                 (or (-> docker-min/+config+ :api :hostname) "127.0.0.1")
                                 ":"
                                 (or (-> docker-min/+config+ :api :port) 55121))
            "api_key" (-> docker-min/+config+ :api :service-key)
            "auth_token" (-> docker-min/+config+ :api :service-key)
            "topic" topic}))
    (future
      (Thread/sleep 1500)
      (!.pg
       [:select
        (realtime.send
         (js {"db/sync"
              {"Entry"
               [{"id" "00000000-0000-0000-0000-0000000000aa"
                 "name" "http-phoenix"
                 "tags" ["websocket"]}]}})
         "db/sync"
         topic
         false)]))
    (promise/x:promise-then
     (realtime/subscribe-broadcast
      {"client" client}
      cache
      {"on_status" (fn [status _frame]
                     (xt/x:arr-push statuses status))
       "on_request" (fn [request payload frame]
                      (xt/x:arr-push requests request)
                      (when request
                        (repl/notify
                         {"status" (xt/x:first statuses)
                          "topic" (xt/x:get-key frame "topic")
                          "frame_event" (xt/x:get-key frame "event")
                          "request_name" (xtd/get-in request ["db/sync" "Entry" 0 "name"])
                          "request_tags" (xtd/get-in request ["db/sync" "Entry" 0 "tags"])
                          "payload_event" (xt/x:get-key frame "event")})))})
     (fn [subscription]
       (return subscription)))))
  => {"status" "SUBSCRIBED"
      "topic" "realtime:room:http-phoenix"
      "frame_event" "broadcast"
      "request_name" "http-phoenix"
      "request_tags" ["websocket"]
      "payload_event" "broadcast"})
