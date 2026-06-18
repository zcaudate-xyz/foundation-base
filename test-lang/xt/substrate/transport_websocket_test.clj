^{:seedgen/skip true}
(ns xt.substrate.transport-websocket-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-json :as node-json]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.transport-websocket :as ws-transport]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.transport-websocket/event-text :added "4.1"}
(fact "unwraps websocket message events and passes raw payloads through"
  (!.js
   [(ws-transport/event-text {"data" "ping"})
    (ws-transport/event-text {"text" "pong"})
    (ws-transport/event-text "echo")])
  => ["ping" "pong" "echo"])

^{:refer xt.substrate.transport-websocket/websocket-endpoint :added "4.1"}
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


^{:refer xt.substrate.transport-websocket/socket-open? :added "4.1"}
(fact "treats readyState 1 and missing readyState as open"
  (!.js
   [(ws-transport/socket-open? {"readyState" 1})
    (ws-transport/socket-open? {"readyState" 0})
    (ws-transport/socket-open? {})])
  => [true false true])

^{:refer xt.substrate.transport-websocket/add-socket-listener :added "4.1"}
(fact "adds listeners through DOM, EventEmitter, or on* fallback APIs"
  (!.js
   (var calls [])
   (var handler (fn [event] event))
   (var socket-a {"addEventListener" (fn [event cb capture]
                                       (xt/x:arr-push calls ["dom" event capture]))})
   (var socket-b {"on" (fn [event cb]
                         (xt/x:arr-push calls ["emitter" event (xt/x:is-function? cb)]))})
   (var socket-c {})
   (ws-transport/add-socket-listener socket-a "message" handler)
   (ws-transport/add-socket-listener socket-b "open" handler)
   (ws-transport/add-socket-listener socket-c "close" handler)
   [calls
    (xt/x:is-function? (. socket-c ["onclose"]))])
  => [[["dom" "message" false]
       ["emitter" "open" true]]
      true])

^{:refer xt.substrate.transport-websocket/remove-socket-listener :added "4.1"}
(fact "removes listeners through DOM, EventEmitter, removeListener, or on* fallback APIs"
  (!.js
   (var calls [])
   (var handler (fn [event] event))
   (var socket-a {"removeEventListener" (fn [event cb capture]
                                          (xt/x:arr-push calls ["dom" event capture]))})
   (var socket-b {"off" (fn [event cb]
                          (xt/x:arr-push calls ["off" event (xt/x:is-function? cb)]))})
   (var socket-c {"removeListener" (fn [event cb]
                                     (xt/x:arr-push calls ["removeListener" event (xt/x:is-function? cb)]))})
   (var socket-d {"onmessage" handler})
   (ws-transport/remove-socket-listener socket-a "message" handler)
   (ws-transport/remove-socket-listener socket-b "open" handler)
   (ws-transport/remove-socket-listener socket-c "close" handler)
   (ws-transport/remove-socket-listener socket-d "message" handler)
   [calls
    (. socket-d ["onmessage"])])
  => [[["dom" "message" false]
       ["off" "open" true]
       ["removeListener" "close" true]]
      nil])

^{:refer xt.substrate.transport-websocket/await-open :added "4.1"}
(fact "waits for socket open and rejects opening errors"
  (notify/wait-on :js
    (var state {"status" "opening"
                "socket" {"id" "socket-1"}
                "error" nil})
    (setTimeout
     (fn []
       (xt/x:set-key state "status" "open"))
     20)
    (promise/x:promise-then
     (ws-transport/await-open state)
     (fn [socket]
       (return
        (promise/x:promise-catch
         (ws-transport/await-open {"status" "error"
                                   "error" "denied"})
         (fn [err]
           (repl/notify
            {"socket" socket
             "error" err})))))))
  => {"socket" {"id" "socket-1"}
      "error" "denied"})

^{:refer xt.substrate.transport-websocket/connect-socket :added "4.1"}
(fact "connects through explicit connect_fn or a supplied WebSocket constructor"
  (!.js
   [(ws-transport/connect-socket
     {"url" "ws://demo.test/a"
      "connect_fn" (fn [url]
                     (return {"url" url
                              "kind" "connect"}))})
    (ws-transport/connect-socket
     {"url" "ws://demo.test/b"
      "WebSocket" (fn [url]
                    (return {"url" url
                             "kind" "ctor"}))})])
  => [{"url" "ws://demo.test/a"
       "kind" "connect"}
      {"url" "ws://demo.test/b"
       "kind" "ctor"}])

^{:refer xt.substrate.transport-websocket/resolve-socket :added "4.1"}
(fact "resolves create_fn, url-backed, and raw socket sources"
  (!.js
   [(ws-transport/resolve-socket
     {"create_fn" (fn []
                    (return {"kind" "create"}))})
    (ws-transport/resolve-socket
     {"url" "ws://demo.test/c"
      "connect_fn" (fn [url]
                     (return {"kind" "connect"
                              "url" url}))})
    (ws-transport/resolve-socket
     {"kind" "raw"})])
  => [{"kind" "create"}
      {"kind" "connect"
       "url" "ws://demo.test/c"}
      {"kind" "raw"}])

^{:refer xt.substrate.transport-websocket/websocket-url :added "4.1"}
(fact "returns websocket urls from strings or source maps"
  (!.js
   [(ws-transport/websocket-url "ws://demo.test/a")
    (ws-transport/websocket-url {"url" "ws://demo.test/b"})
    (ws-transport/websocket-url {"kind" "raw"})])
  => ["ws://demo.test/a"
      "ws://demo.test/b"
      nil])

^{:refer xt.substrate.transport-websocket/websocket-source :added "4.1"}
(fact "creates a text transport source around a websocket-like socket"
  (notify/wait-on :js
    (var sent [])
    (var listeners {})
    (var removed [])
    (var closed [])
    (var received [])
    (var source
         (ws-transport/websocket-source
          {"create_fn"
           (fn []
             (return {"readyState" 1
                      "send" (fn [text]
                               (xt/x:arr-push sent text))
                      "close" (fn []
                                (xt/x:arr-push closed true))
                      "addEventListener" (fn [event handler capture]
                                           (xt/x:set-key listeners event handler))
                      "removeEventListener" (fn [event handler capture]
                                              (xt/x:arr-push removed event))}))}))
    (promise/x:promise-then
     ((. source ["start_fn"])
      (fn [event ctx]
        (xt/x:arr-push received (ws-transport/event-text event))))
     (fn [_]
       ((. source ["write_fn"]) "ping")
       ((xt/x:get-key listeners "message")
        {"data" "pong"})
       ((. source ["stop_fn"]) nil)
       (repl/notify
        {"sent" sent
         "received" received
         "removed" removed
         "closed" (xt/x:len closed)}))))
  => {"sent" ["ping"]
      "received" ["pong"]
      "removed" ["message"]
      "closed" 1})
