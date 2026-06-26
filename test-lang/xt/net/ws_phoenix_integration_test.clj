(ns xt.net.ws-phoenix-integration-test
  (:use code.test)
  (:require [hara.lang :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core.supabase :as s]
            [xt.lang.common-notify :as notify]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}}))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]
             [xt.net.ws-phoenix :as phoenix]
             [js.net.ws-native :as js-websocket]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

(defn.js join-payload
  "builds the phx_join payload required by Realtime v2.40+"
  {:added "4.1"}
  []
  (return {"access_token" (@! (-> local-min/+config+ :api :anon-key))
           "config" {"broadcast" {"ack" false "self" false}}}))

(defn.js make-client
  "creates a websocket client connected to the local Supabase realtime endpoint"
  {:added "4.1"}
  [listeners]
  (var client
       (js-websocket/create {:host (@! (-> local-min/+config+ :api :hostname))
                             :port (@! (-> local-min/+config+ :api :port))}))
  (js-websocket/connect-ws client
                           {:path (+ "/realtime/v1/websocket?vsn=1.0.0&apikey="
                                     (@! (-> local-min/+config+ :api :anon-key)))
                            "listeners" listeners})
  (return client))

^{:refer xt.net.ws-phoenix/send-join :added "4.1"
  :setup [(l/rt:restart :js)
          (Thread/sleep 2000)]}
(comment "SKIPPED: requires Supabase local-min realtime websocket"
  
  (notify/wait-on [:js 2000]
    (var client
         (-/make-client
          {"open"
           (fn [_]
             (phoenix/send-join
              client
              (-/join-payload)
              {"topic" "realtime:room:send-join-test"
               "ref" "join-1"}))
           "message"
           (phoenix/wrap-phoenix
            {"phx_reply"
             (fn [frame]
               (when (== "join-1" (xt/x:get-key frame "ref"))
                 (repl/notify frame)))})}))
    true)
  => {"join_ref" "join-1"
      "event" "phx_reply"
      "ref" "join-1"
      "payload" {"status" "ok"
                 "response" {"postgres_changes" []}}
      "topic" "realtime:room:send-join-test"})

^{:refer xt.net.ws-phoenix/send-leave :added "4.1"
  :setup [(l/rt:restart :js)
          (Thread/sleep 2000)]}
(comment "SKIPPED: requires Supabase local-min realtime websocket"

  (notify/wait-on [:js 15000]
    (var client
         (-/make-client
          {"open"
           (fn [_]
             (phoenix/send-join
              client
              (-/join-payload)
              {"topic" "realtime:room:send-leave-test"
               "ref" "join-1"}))
           "message"
           (phoenix/wrap-phoenix
            {"phx_reply"
             (fn [frame]
               (var ref (xt/x:get-key frame "ref"))
               (cond (== "join-1" ref)
                     (when (== "ok" (xtd/get-in frame ["payload" "status"]))
                       (phoenix/send-leave
                        client
                        {"topic" "realtime:room:send-leave-test"
                         "ref" "leave-1"}))

                     (== "leave-1" ref)
                     (when (== "ok" (xtd/get-in frame ["payload" "status"]))
                       (phoenix/send-heartbeat
                        client
                        {"ref" "heartbeat-1"}))

                     (== "heartbeat-1" ref)
                     (repl/notify frame)))})}))
    true)
  => {"join_ref" "heartbeat-1"
      "event" "phx_reply"
      "ref" "heartbeat-1"
      "payload" {"status" "ok"}
      "topic" "phoenix"})

^{:refer xt.net.ws-phoenix/send :added "4.1"
  :setup [(l/rt:restart :js)
          (Thread/sleep 2000)]}
(comment "SKIPPED: requires Supabase local-min realtime websocket"

  (notify/wait-on [:js 15000]
    (var client
         (-/make-client
          {"open"
           (fn [_]
             (phoenix/send-join
              client
              (-/join-payload)
              {"topic" "realtime:room:db-send"
               "ref" "join-1"}))
           "message"
           (phoenix/wrap-phoenix
            {"phx_reply"
             (fn [frame]
               (when (== "join-1" (xt/x:get-key frame "ref"))
                 (when (== "ok" (xtd/get-in frame ["payload" "status"]))
                   (repl/notify "joined"))))
             "broadcast"
             (fn [frame]
               (when (== "my-event" (xtd/get-in frame ["payload" "event"]))
                 (var inner (xtd/get-in frame ["payload" "payload"]))
                 (xt/x:del-key inner "id")
                 (repl/notify frame)))})}))
    true)

  (future
    (Thread/sleep 1500)
    (!.pg
      (s/realtime-send "room:db-send" "my-event" {"hello" "from postgres"})))

  (notify/wait-on [:js 15000]
    true)
  => {"join_ref" "join-1"
      "event" "broadcast"
      "ref" nil
      "payload" {"event" "my-event"
                 "payload" {"hello" "from postgres"}
                 "type" "broadcast"}
      "topic" "realtime:room:db-send"})
