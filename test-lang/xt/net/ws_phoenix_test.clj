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
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

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
  (js-websocket/connect-ws client
                           {:path (xt/x:cat "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                            (@! (-> local-min/+config+ :api :anon-key)))})
  (js-websocket/add-listeners-ws
   client
   {"open"
    (fn [_]
      (phoenix/send-frame
       client
       (phoenix/make-frame-join
        {"config" {"broadcast" {"ack" false "self" false}}}
        {"topic" topic
         "ref" "::INIT"})))
    "message"
    (fn [event]
      (var frame (phoenix/decode-frame event))
      (when frames
        (xt/x:arr-push frames frame))
      (when (== "::INIT" (xt/x:get-key frame "ref"))
        (callback frame)))})
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
    (phoenix/get-frame-ref {"ref" "join-1"}))
  => "join-1")

^{:refer xt.net.ws-phoenix/make-frame :added "4.1"}
(fact "builds a generic phoenix frame"

  (!.js
    (phoenix/make-frame "realtime:room:test" "broadcast" {"hello" true} {"ref" "push-1"}))
  => {"topic" "realtime:room:test"
      "event" "broadcast"
      "payload" {"hello" true}
      "ref" "push-1"
      "join_ref" "push-1"})

^{:refer xt.net.ws-phoenix/make-frame-join :added "4.1"}
(fact "builds a join frame from a topic and payload"

  (!.js
   (phoenix/make-frame-join {"config" {}}
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
    (phoenix/make-frame-leave {"topic" "realtime:room:test" "ref" "leave-1"}))
  => {"topic" "realtime:room:test"
      "event" "phx_leave"
      "payload" {}
      "ref" "leave-1"
      "join_ref" "leave-1"})

^{:refer xt.net.ws-phoenix/send-join :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "connects to the local Supabase realtime websocket and joins a channel"

  (notify/wait-on [:js 2000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var joined false)
    (-> client
        (js-websocket/connect-ws {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                           (@! (-> local-min/+config+ :api :anon-key)))})
        (promise/x:promise-then
         (fn [client]
           (websocket/add-listeners
            client
            {"message"
             (phoenix/wrap-phoenix
              {"phx_reply"
               (fn [frame]
                 (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                            (not joined))
                   (:= joined true)
                   (websocket/disconnect client)
                   (repl/notify frame)))})})
           (phoenix/send-frame
            client
            (phoenix/make-frame-join
             {"config" {"broadcast" {"ack" false "self" false}}}
             {"topic" "realtime:room:send-join-test"
              "ref" "join-1"})))))
    true)
  => {"event" "phx_reply",
      "ref" "join-1",
      "payload" {"status" "ok",
                 "response" {"postgres_changes" []}},
      "topic" "realtime:room:send-join-test"})

^{:refer xt.net.ws-phoenix/send-leave :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "connects to the local Supabase realtime websocket and joins a channel"

  (notify/wait-on [:js 1000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var joined false)
    (var frames [])
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
          {"topic" "realtime:room:send-leave-test"
           "ref" "join-1"})))
      "message"
      (phoenix/wrap-phoenix
       {"phx_reply"
        (fn [frame]
          (when (and (== "ok" (xtd/get-in frame ["payload" "status"]))
                     (not joined))
            (:= joined true)
            (xt/x:arr-push frames frame)
            (phoenix/send-frame
             client
             (phoenix/make-frame-leave
              {"topic" "realtime:room:send-leave-test"
               "ref" "leave-1"}))
            (repl/notify "left")))
        "presence_state"
        (fn [frame]
          (xt/x:arr-push frames frame)
          (repl/notify frames))})})
    true)
  => "left")

^{:refer xt.net.ws-phoenix/send-frame :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "connects to the local Supabase realtime websocket and receives a broadcast after a pg send"

  ;;
  ;; CREATE GLOBAL CLIENT AND FRAMES
  ;; 
  (notify/wait-on [:js 2000]
    (:= (!:G FRAMES) [])
    (:= (!:G CLIENT) (-/client-init "realtime:room:db-send"
                                    (fn [out]
                                      (repl/notify out))
                                    (!:G FRAMES))))
  => {"event" "phx_reply", "ref" "::INIT", "payload" {"status" "ok", "response" {"postgres_changes" []}}, "topic" "realtime:room:db-send"}
  
  (do (!.pg
       (s/realtime-send "room:db-send" "my-event" {"hello" "from postgres"}))
      (!.js
        FRAMES))
  => (contains-in
      [{"event" "phx_reply",
        "ref" "::INIT",
        "payload" {"status" "ok", "response" {"postgres_changes" []}},
        "topic" "realtime:room:db-send"}
       {"event" "broadcast",
        "ref" nil,
        "topic" "realtime:room:db-send"
        "payload" {"event" "my-event",
                   "type" "broadcast",
                   "meta" {"id" string?},
                   "payload"
                   {"hello" "from postgres",
                    "id" string?}},}]))




^{:refer xt.net.ws-phoenix/make-frame-heartbeat :added "4.1"}
(fact "builds a heartbeat frame on the phoenix topic"

  (!.js
    (phoenix/make-frame-heartbeat {"ref" "hb-1"}))
  => {"topic" "phoenix"
      "event" "heartbeat"
      "payload" {}
      "ref" "hb-1"
      "join_ref" "hb-1"})

^{:refer xt.net.ws-phoenix/encode-frame :added "4.1"}
(fact "encodes a frame map to the phoenix v1.0.0 wire object"

  (!.js
    (phoenix/encode-frame {"join_ref" "join-1"
                           "ref" "ref-1"
                           "topic" "realtime:room:test"
                           "event" "phx_join"
                           "payload" {"config" {}}}))
  => {"join_ref" "join-1"
      "ref" "ref-1"
      "topic" "realtime:room:test"
      "event" "phx_join"
      "payload" {"config" {}}})

^{:refer xt.net.ws-phoenix/send-frame :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "sends an encoded frame over the websocket and receives a phx_reply"

  (notify/wait-on [:js 2000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var replied false)
    (js-websocket/connect-ws client
                             {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                       (@! (-> local-min/+config+ :api :anon-key)))})
    (js-websocket/add-listeners-ws
     client
     {"open"
      (fn [_]
        (phoenix/send-frame
         client
         {"topic" "realtime:room:send-frame-test"
          "event" "phx_join"
          "payload" {"config" {"broadcast" {"ack" false "self" false}}}
          "ref" "join-1"
          "join_ref" "join-1"}))
      "message"
      (fn [event]
        (var frame (phoenix/decode-frame event))
        (when (and (== "phx_reply" (xt/x:get-key frame "event"))
                   (== "join-1" (xt/x:get-key frame "ref"))
                   (not replied))
          (:= replied true)
          (websocket/disconnect client)
          (repl/notify frame)))})
    true)
  => {"event" "phx_reply",
      "ref" "join-1",
      "payload" {"status" "ok",
                 "response" {"postgres_changes" []}},
      "topic" "realtime:room:send-frame-test"})

^{:refer xt.net.ws-phoenix/send-heartbeat :added "4.1"
  :setup [(l/rt:restart :js)]}
(fact "sends a heartbeat frame over the websocket and receives a phx_reply"

  (notify/wait-on [:js 2000]
    (var client
         (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                               :port (@! (-> local-min/+config+ :api :port))}))
    (var replied false)
    (js-websocket/connect-ws client
                             {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                       (@! (-> local-min/+config+ :api :anon-key)))})
    (js-websocket/add-listeners-ws
     client
     {"open"
      (fn [_]
        (phoenix/send-frame client
                            (phoenix/make-frame-heartbeat {"ref" "hb-1"})))
      "message"
      (fn [event]
        (var frame (phoenix/decode-frame event))
        (when (and (== "phx_reply" (xt/x:get-key frame "event"))
                   (== "hb-1" (xt/x:get-key frame "ref"))
                   (not replied))
          (:= replied true)
          (websocket/disconnect client)
          (repl/notify frame)))})
    true)
  => {"event" "phx_reply",
      "ref" "hb-1",
      "payload" {"status" "ok",
                 "response" {}},
      "topic" "phoenix"})


^{:refer xt.net.ws-phoenix/wrap-phoenix :added "4.1"}
(fact "TODO")

^{:refer xt.net.ws-phoenix/start-heartbeat :added "4.1"}
(fact "TODO")