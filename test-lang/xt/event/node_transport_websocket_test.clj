(ns xt.event.node-transport-websocket-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.node-frame :as frame]
             [xt.event.node-json :as node-json]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.node-transport-websocket :as ws-transport]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.event.node-transport-websocket/event-text :added "4.1"}
(fact "unwraps websocket message events and passes raw payloads through"
  (!.js
   [(ws-transport/event-text {"data" "ping"})
    (ws-transport/event-text {"text" "pong"})
    (ws-transport/event-text "echo")])
  => ["ping" "pong" "echo"])

^{:refer xt.event.node-transport-websocket/websocket-endpoint :added "4.1"}
(fact "adapts websocket-like sockets to the JSON node transport contract"
  (notify/wait-on :js
   (var handlers {})
   (var removed [])
   (var sent [])
   (var closed [])
   (var received [])
   (var socket {"readyState" 1
                "send" (fn [text]
                         (xt/x:arr-push sent text)
                         (return true))
                "close" (fn []
                          (xt/x:arr-push closed true)
                          (return true))
                "addEventListener" (fn [event handler capture]
                                     (xt/x:set-key handlers event handler)
                                     (return true))
                "removeEventListener" (fn [event handler capture]
                                        (xt/x:arr-push removed event)
                                        (return true))})
   (var endpoint (ws-transport/websocket-endpoint socket))
   (-> ((. endpoint ["start_fn"])
        (fn [frame ctx]
          (xt/x:arr-push received frame)
          (return true)))
       (promise/x:promise-then
        (fn [_]
          ((. endpoint ["send_fn"])
           (frame/request-frame
            "room/a"
            "demo/echo"
            ["ping"]
            {"id" "req-1"}))
          ((xt/x:get-key handlers "message")
           {"data" (node-json/encode-frame
                    (frame/response-ok-frame
                     "req-1"
                     "room/a"
                     {"pong" true}
                     nil))})
          (return ((. endpoint ["stop_fn"]) nil))))
       (promise/x:promise-then
        (fn [_]
          (repl/notify
           {"sent" (node-json/decode-frame (xt/x:first sent))
            "received" (xt/x:first received)
            "removed" removed
            "closed" (xt/x:len closed)})))))
  => (contains-in
      {"sent" {"kind" "request"
               "space" "room/a"
               "action" "demo/echo"
               "args" ["ping"]
               "id" "req-1"}
       "received" {"kind" "response"
                   "reply_to" "req-1"
                   "space" "room/a"
                   "status" "ok"
                   "data" {"pong" true}}
       "removed" ["message"]
       "closed" 1}))
