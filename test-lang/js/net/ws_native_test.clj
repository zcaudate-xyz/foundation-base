(ns js.net.ws-native-test
  (:use code.test)
  (:require [hara.lang :as l]
            [net.http.websocket :as ws]
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
   :require [[js.net.ws-native :as js-ws]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.ws-phoenix :as phoenix]
             [xt.net.ws-native :as websocket]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)
          (l/rt:setup :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer js.net.ws-native/connect-ws :added "4.1"}
(comment "SKIPPED: requires Supabase local-min realtime websocket"
  (notify/wait-on [:js 15000]
    (var topic "room:http-websocket")
    (var api (-> local-min/+config+ :api))
    (var protocol (or (xt/x:get-key api "protocol") "http"))
    (var websocket-url
         (xt/x:cat (:? (== protocol "https") "wss" "ws")
                   "://"
                   (or (xt/x:get-key api "hostname") "127.0.0.1")
                   ":"
                   (or (xt/x:get-key api "port") 55121)
                   "/realtime/v1/websocket?vsn=2.0.0&apikey="
                   (xt/x:get-key api "service-key")))
    (var client
         (js-ws/create
          {"base_url" (xt/x:cat (or (-> local-min/+config+ :api :protocol) "http")
                                "://"
                                (or (-> local-min/+config+ :api :hostname) "127.0.0.1")
                                ":"
                                (or (-> local-min/+config+ :api :port) 55121))
           "api_key" (-> local-min/+config+ :api :service-key)
           "auth_token" (-> local-min/+config+ :api :service-key)
           "topic" topic}))
    (var joined false)
    (var raw (new WebSocket websocket-url))
    (xt/x:set-key client "raw" raw)
    (websocket/add-listeners
     client
     {"open"
      (fn [_]
        (phoenix/send-join
         client
         {"config" {"broadcast" {"ack" false "self" false}}}
         {"topic" (xt/x:cat "realtime:" topic)
          "ref" "join-1"}))
      "message"
      (fn [event]
        (var frame (phoenix/decode-frame event))
        (when (and (== "phx_reply" (xt/x:get-key frame "event"))
                   (== "ok" (xtd/get-in frame ["payload" "status"]))
                   (not joined))
          (:= joined true)
          (future
            (Thread/sleep 1500)
            (!.pg
             [:select
              (realtime.send
               (js {"db/sync"
                    {"Entry"
                     [{"id" "00000000-0000-0000-0000-0000000000aa"
                       "name" "http-websocket"
                       "tags" ["websocket"]}]}})
               "db/sync"
               topic
               false)])))
        (when (== "broadcast" (xt/x:get-key frame "event"))
          (websocket/disconnect client)
          (repl/notify
           {"topic" (xt/x:get-key frame "topic")
            "event" (xt/x:get-key frame "event")
            "request_name" (xtd/get-in frame ["payload" "data" "db/sync" "Entry" 0 "name"])
            "request_tags" (xtd/get-in frame ["payload" "data" "db/sync" "Entry" 0 "tags"])})))})
    true)
  => {"topic" "realtime:room:http-websocket"
      "event" "broadcast"
      "request_name" "http-websocket"
      "request_tags" ["websocket"]})

^{:refer js.net.ws-native/disconnect-ws :added "4.1"}
(fact "disconnects a wrapped websocket client"

  (!.js
    (var closed [])
    (var client (js-ws/create {}))
    (xt/x:set-key client "raw"
                  {"close" (fn [code reason]
                             (. closed (push [code reason]))
                             (return true))})
    (websocket/disconnect client)
    closed)
  => [[1000 "done"]])

^{:refer js.net.ws-native/send-ws :added "4.1"}
(fact "sends through the wrapped websocket client"
  (!.js
   (var sent [])
   (var client (js-ws/create {}))
   (xt/x:set-key client "raw"
                 {"send" (fn [payload]
                           (. sent (push payload))
                           (return true))})
   (websocket/send client "hello")
   sent)
  => ["hello"])

^{:refer js.net.ws-native/add-listeners-ws :added "4.1"}
(fact "adds listeners to the wrapped websocket client"

  (!.js
   (var handlers {})
   (var client (js-ws/create {}))
   (xt/x:set-key client "raw"
                 {"addEventListener" (fn [event handler]
                                       (xtd/obj-set handlers event handler)
                                       (return true))})
   (websocket/add-listeners client {"open" (fn [_] (return true))
                                    "message" (fn [_] (return true))})
   (xtd/obj-keys handlers))
  => ["open" "message"])

^{:refer js.net.ws-native/default-heartbeat-fn :added "4.1"}
(fact "sends a heartbeat string"

  (!.js
   (var sent [])
   (var client {"raw" {"send" (fn [x] (. sent (push x)))}})
   (js-ws/default-heartbeat-fn client "ping")
   sent)
  => ["heartbeat"])

^{:refer js.net.ws-native/start-heartbeat-ws :added "4.1"}
(fact "starts a heartbeat interval"

  (notify/wait-on [:js 200]
    (var client (js-ws/create {}))
    (var beats [])
    (js-ws/start-heartbeat-ws
     client
     "ping"
     (fn [c n] (. beats (push n)))
     10)
    (setTimeout (fn [] (repl/notify (. beats length))) 50))
  => #(> % 0))

^{:refer js.net.ws-native/stop-heartbeat-ws :added "4.1"}
(fact "stops a heartbeat interval"

  (notify/wait-on [:js 200]
    (var client (js-ws/create {}))
    (var beats [])
    (js-ws/start-heartbeat-ws
     client
     "ping"
     (fn [c n] (. beats (push n)))
     10)
    (js-ws/stop-heartbeat-ws client "ping")
    (var before (. beats length))
    (setTimeout (fn [] (repl/notify (== before (. beats length)))) 50))
  => true)

^{:refer js.net.ws-native/create :added "4.1"}
(fact "creates a websocket wrapper"
  (!.js
   (xt/x:get-key (xt/x:get-key (js-ws/create {"topic" "room:http-websocket"}) "defaults") "topic"))
  => "room:http-websocket")

(comment

  @(ws/websocket (str "ws://127.0.0.1:55121/realtime/v1/websocket?vsn=2.0.0&apikey=" (-> local-min/+config+ :api :anon-key))
                {:on-open (fn [& args] (std.lib/prn args))})

  (notify/wait-on :js
    (var client
         (js-ws/create
          {:host  "127.0.0.1"
           :port  55121
           :path  (+ "/realtime/v1/websocket?vsn=2.0.0&apikey=" (@! (-> local-min/+config+ :api :anon-key)))
           :token (@! (-> local-min/+config+ :api :anon-key))}))
    (js-ws/connect-ws client)
    (js-ws/add-listeners-ws client
                            {"open"
                             (fn [_]
                               (phoenix/send-join
                                client
                                {"config" {"broadcast" {"ack" false "self" false}}}
                                {"topic"  "realtime:room:example-1"
                                 "ref"    "join-1"})
                               (repl/notify "opened"))}))

  {"base_url" (xt/x:cat (or (-> local-min/+config+ :api :protocol) "http")
                           "://"
                           (or (-> local-min/+config+ :api :hostname) "127.0.0.1")
                           ":"
                           (or (-> local-min/+config+ :api :port) 55121))
      "api_key" (-> local-min/+config+ :api :service-key)
      "auth_token" (-> local-min/+config+ :api :service-key)
      "topic" topic}

  )
