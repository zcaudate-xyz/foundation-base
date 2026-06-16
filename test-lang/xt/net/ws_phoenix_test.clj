(ns xt.net.ws-phoenix-test
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
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}}))

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]
             [js.net.ws-native :as js-websocket]]})

(defn.js client-init
  [topic callback frames]
  (var client
       (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                             :port (@! (-> local-min/+config+ :api :port))}))
  (-> client
      (js-websocket/connect-ws {:path (xt/x:cat "/realtime/v1/websocket?vsn=2.0.0&apikey="
                                             (@! (-> local-min/+config+ :api :anon-key)))})
      (js-websocket/add-listeners-ws
       {"open"
        (fn [_]
          (phoenix/send-join
           client
           {"config" {"broadcast" {"ack" false "self" false}}}
           {"topic" topic
            "ref" "::INIT"}))
        "message"
        (fn [event]
          (var frame (phoenix/decode-frame event))
          (when frames
            (xt/x:arr-push frames frame))
          #_(callback frame)
          (when (== "::INIT" (xt/x:get-key frame "ref"))
            (callback frame)))}))
  (return client))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

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

^{:refer xt.net.ws-phoenix/get-frame-ref :added "4.1"}
(fact "uses the explicit ref when provided"

  (!.js
    (phoenix/get-frame-ref nil {"ref" "join-1"}))
  => "join-1")

^{:refer xt.net.ws-phoenix/make-frame :added "4.1"}
(fact "builds a generic phoenix frame"

  (!.js
    (phoenix/make-frame nil "realtime:room:test" "broadcast" {"hello" true} {"ref" "push-1"}))
  => {"topic" "realtime:room:test"
      "event" "broadcast"
      "payload" {"hello" true}
      "ref" "push-1"
      "join_ref" "push-1"})

^{:refer xt.net.ws-phoenix/make-frame-join :added "4.1"}
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

^{:refer xt.net.ws-phoenix/make-frame-leave :added "4.1"}
(fact "builds a leave frame from a topic"

  (!.js
    (phoenix/make-frame-leave nil
                              {"topic" "realtime:room:test" "ref" "leave-1"}))
  => {"topic" "realtime:room:test"
      "event" "phx_leave"
      "payload" {}
      "ref" "leave-1"
      "join_ref" "leave-1"})

^{:refer xt.net.ws-phoenix/wrap-phoenix :added "4.1"}
(fact "decodes phoenix frames and dispatches by event name"

  (!.js
    (var results [])
    (var handler
         (phoenix/wrap-phoenix
          {"phx_reply" (fn [frame] (xt/x:arr-push results ["reply" frame]))
           "presence_state" (fn [frame] (xt/x:arr-push results ["presence" frame]))}))
    (handler "[\"join-1\",\"join-1\",\"realtime:room:test\",\"phx_reply\",{\"status\":\"ok\"}]")
    (handler "[\"join-1\",null,\"realtime:room:test\",\"presence_state\",{}]")
    (handler "[\"join-1\",null,\"realtime:room:test\",\"broadcast\",{}]")
    results)
  => [["reply" {"join_ref" "join-1", "ref" "join-1",
                "topic" "realtime:room:test", "event" "phx_reply",
                "payload" {"status" "ok"}}]
      ["presence" {"join_ref" "join-1", "ref" nil,
                   "topic" "realtime:room:test", "event" "presence_state",
                   "payload" {}}]])

^{:refer xt.net.ws-phoenix/send-join :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "connects to the local Supabase realtime websocket and joins a channel"

  (notify/wait-on [:js 2000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var joined false)
    (-> client
        (js-websocket/connect-ws {:path (+ "/realtime/v1/websocket?vsn=2.0.0&apikey="
                                           (@! (-> local-min/+config+ :api :anon-key)))})
        (websocket/add-listeners
         {"open"
          (fn [_]
            (phoenix/send-join
             client
             {"config" {"broadcast" {"ack" false "self" false}}}
             {"topic" "realtime:room:send-join-test"
              "ref" "join-1"}))
          "message"
          (phoenix/wrap-phoenix
           {"phx_reply"
            (fn [frame]
              (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                         (not joined))
                (:= joined true)
                (websocket/disconnect client)
                (repl/notify frame)))})}))
    true)
  => {"join_ref" "join-1",
      "event" "phx_reply",
      "ref" "join-1",
      "payload" {"status" "ok",
                 "response" {"postgres_changes" []}},
      "topic" "realtime:room:send-join-test"})

^{:refer xt.net.ws-phoenix/send-leave :added "4.1"
  :setup [(l/rt:restart :js)]}
(comment "SKIPPED: requires Supabase local-min realtime websocket"

  (notify/wait-on [:js 1000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var joined false)
    (var frames [])
    (-> client
        (js-websocket/connect-ws {:path (+ "/realtime/v1/websocket?vsn=2.0.0&apikey="
                                           (@! (-> local-min/+config+ :api :anon-key)))})
        (websocket/add-listeners
         {"open"
          (fn [_]
            (phoenix/send-join
             client
             {"config" {"broadcast" {"ack" false "self" false}}}
             {"topic" "realtime:room:send-leave-test"
              "ref" "join-1"}))
          "message"
          (phoenix/wrap-phoenix
           {"phx_reply"
            (fn [frame]
              (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                         (not joined))
                (:= joined true)
                (xt/x:arr-push frames frame)
                (phoenix/send-leave
                 client
                 {"topic" "realtime:room:send-leave-test"
                  "ref" "leave-1"})))
            "presence_state"
            (fn [frame]
              (xt/x:arr-push frames frame)
              (repl/notify frames))})}))
    true)
  => [{"join_ref" "join-1",
       "event" "phx_reply",
       "ref" "join-1",
       "payload" {"status" "ok", "response"
                  {"postgres_changes" []}},
       "topic" "realtime:room:send-leave-test"}
      {"join_ref" "join-1", "event" "presence_state", "ref" nil, "payload" {},
       "topic" "realtime:room:send-leave-test"}])

^{:refer xt.net.ws-phoenix/send :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "connects to the local Supabase realtime websocket and receives a broadcast after a pg send"

  
  (notify/wait-on [:js 2000]
    (:= (!:G FRAMES) [])
    (:= (!:G CLIENT) (-/client-init "realtime:room:db-send"
                                    (fn [out]
                                      (repl/notify out))
                                    (!:G FRAMES))))
  {"join_ref" "::INIT", "event" "phx_reply", "ref" "::INIT", "payload" {"status" "error", "response" {"reason" "Unknown Error on Channel"}}, "topic" "realtime:room:db-send"}
  (!.js FRAMES)
  
  (!.pg
    (s/realtime-send "room:db-send" "my-event" {"hello" "from postgres"}))
  
  (!.pg
    (s/realtime-send "room:db-send" "my-event" {"hello" "from postgres"}))
  
  
  (!.js
    CLIENT)
  
  (notify/wait-on [:js 2000]
    (-/client-init (fn [out]
                     (repl/notify out))))
  
  (!.js (+ 1 2))
  
  
  
  (!.js
    (-/client-init (fn [out]
                     out)))
  )


(fact "send stuff"
  
  (!.pg
    [:select (realtime.send {"hello" "from postgres"}
                            (:text "my-event")
                            (:text "room:db-send")
                            false)]))


^{:refer xt.net.ws-phoenix/make-frame-heartbeat :added "4.1"}
(fact "TODO")

^{:refer xt.net.ws-phoenix/encode-frame :added "4.1"}
(fact "TODO")

^{:refer xt.net.ws-phoenix/send-frame :added "4.1"}
(fact "TODO")

^{:refer xt.net.ws-phoenix/send-heartbeat :added "4.1"}
(fact "TODO")