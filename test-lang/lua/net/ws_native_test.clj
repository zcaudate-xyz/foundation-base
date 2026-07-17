(ns lua.net.ws-native-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :test-mode true
   :config {:program :resty}
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [lua.net.ws-native :as ws]]})

(fact:global
 {:skip (not (env/program-exists? "resty"))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.net.ws-native/dispatch-ws :added "4.1"}
(fact "dispatches an event to its registered listener and returns the payload"

  (!.lua
   (var client (ws/create {}))
   (ws/add-listeners-ws
    client
    {"message" (fn [payload]
                 (xt/x:set-key (xt/x:get-key client "state")
                               "seen"
                               (xt/x:get-key payload "data")))})
   [(ws/dispatch-ws client "message" {"data" "hello"})
    (xt/x:get-key (xt/x:get-key client "state") "seen")])
  => [{"data" "hello"} "hello"])

^{:refer lua.net.ws-native/receive-loop :added "4.1"}
(fact "receives text frames until close and dispatches lifecycle events"

  (!.lua
   (var state {"reads" 0 "events" []})
   (var raw
        {"recv_frame"
         (fn [self]
           (var reads (+ 1 (xt/x:get-key state "reads")))
           (xt/x:set-key state "reads" reads)
           (if (== reads 1)
             (return "hello" "text" nil)
             (return nil "close" nil)))})
   (var client (ws/create {}))
   (xt/x:set-key client "raw" raw)
   (ws/add-listeners-ws
    client
    {"message" (fn [payload]
                 (xt/x:arr-push (xt/x:get-key state "events")
                                (xt/x:get-key payload "data")))
     "close" (fn [payload]
               (xt/x:arr-push (xt/x:get-key state "events") "closed"))})
   [(== (ws/receive-loop client) client)
    (xt/x:get-key state "reads")
    (xt/x:get-key state "events")])
  => [true 2 ["hello" "closed"]])

^{:refer lua.net.ws-native/connect-ws :added "4.1"}
(fact "prepares the URL, configures the raw client, and stores it"

  (!.lua
   (local native (require "resty.websocket.client"))
   (var original (xt/x:get-key native "new"))
   (var state {})
   (var raw
        {"set_timeout" (fn [self timeout]
                        (xt/x:set-key state "timeout" timeout))
         "connect" (fn [self url]
                     (xt/x:set-key state "url" url)
                     (return true nil))})
   (xt/x:set-key native "new" (fn [] (return raw)))
   (var client (ws/create {"host" "example.com" "port" 9000}))
   (var output (ws/connect-ws client {"path" "/socket" "timeout" 250}))
   (xt/x:set-key native "new" original)
   [(== output client)
    (== (xt/x:get-key client "raw") raw)
    (xt/x:get-key client "thread")
    (xt/x:get-key state "timeout")
    (xt/x:get-key state "url")])
  => [true true nil 250 "ws://example.com:9000/socket"])

^{:refer lua.net.ws-native/disconnect-ws :added "4.1"}
(fact "closes and clears an active raw client"

  (!.lua
   (var state {"closed" false})
   (var raw {"send_close" (fn [self]
                            (xt/x:set-key state "closed" true))})
   (var client (ws/create {}))
   (xt/x:set-key client "raw" raw)
   [(== (ws/disconnect-ws client) client)
    (xt/x:get-key client "raw")
    (xt/x:get-key state "closed")])
  => [true nil true])

^{:refer lua.net.ws-native/send-ws :added "4.1"}
(fact "sends a frame and synchronously dispatches its reply without a reader thread"

  (!.lua
   (var state {})
   (var raw
        {"send_text" (fn [self input]
                       (xt/x:set-key state "sent" input))
         "recv_frame" (fn [self]
                        (return "reply" "text" nil))})
   (var client (ws/create {}))
   (xt/x:set-key client "raw" raw)
   (ws/add-listeners-ws
    client
    {"message" (fn [payload]
                 (xt/x:set-key state "received"
                               (xt/x:get-key payload "data")))})
   [(== (ws/send-ws client "request") client)
    (xt/x:get-key state "sent")
    (xt/x:get-key state "received")])
  => [true "request" "reply"])

^{:refer lua.net.ws-native/add-listeners-ws :added "4.1"}
(fact "registers every listener and returns their event names"

  (!.lua
   (var client (ws/create {}))
   (var message-fn (fn [payload] (return payload)))
   (var close-fn (fn [payload] (return payload)))
   (var events (ws/add-listeners-ws
                client
                {"message" message-fn "close" close-fn}))
   (xtd/arr-sort events (fn [x] (return x)) xt/x:str-lt)
   [events
    (== (xt/x:get-key (xt/x:get-key client "callbacks") "message")
        message-fn)
    (== (xt/x:get-key (xt/x:get-key client "callbacks") "close")
        close-fn)])
  => [["close" "message"] true true])

^{:refer lua.net.ws-native/start-heartbeat-ws :added "4.1"}
(fact "reports that heartbeat scheduling is unsupported"

  (!.lua (ws/start-heartbeat-ws (ws/create {}) "ping" (fn []) 1000))
  => nil)

^{:refer lua.net.ws-native/stop-heartbeat-ws :added "4.1"}
(fact "returns the client when stopping a heartbeat"

  (!.lua
   (var client (ws/create {}))
   (== (ws/stop-heartbeat-ws client "ping") client))
  => true)

^{:refer lua.net.ws-native/create :added "4.1"}
(fact "constructs an initialized client and installs protocol overrides"

  (!.lua
   (var defaults {"host" "example.com"})
   (var client (ws/create defaults))
   (var override (xt/x:get-key client "::/override"))
   [(xt/x:get-key client "raw")
    (== (xt/x:get-key client "defaults") defaults)
    (xt/x:get-key client "state")
    (xt/x:get-key client "callbacks")
    (xt/x:get-key client "thread")
    (xt/x:is-function? (xt/x:get-key override "connect"))
    (xt/x:is-function? (xt/x:get-key override "disconnect"))
    (xt/x:is-function? (xt/x:get-key override "send"))
    (xt/x:is-function? (xt/x:get-key override "add_listeners"))
    (xt/x:is-function? (xt/x:get-key override "start_heartbeat"))
    (xt/x:is-function? (xt/x:get-key override "stop_heartbeat"))])
  => [nil true {} {} nil true true true true true true])
