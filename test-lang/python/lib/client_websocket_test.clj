(ns python.lib.client-websocket-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [python.lib.client-websocket :as py-ws]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.lib.client-websocket/client :added "4.1.3"}
(fact "wraps raw python websocket clients with sync and async client methods"

  (!.py
   (var handlers {})
   (var sent [])
   (var closed [])
   (var async-client
        (py-ws/client
         {"send" (fn [_payload]
                   (return true))
          "close" (fn [_code _reason]
                    (return true))
          "addEventListener" (fn [_event _handler]
                               (return true))}))
   (var client
        (py-ws/client
         {"send" (fn [payload]
                   (xt/x:arr-push sent payload)
                   (return true))
          "close" (fn [code reason]
                    (xt/x:arr-push closed [code reason])
                    (return true))
           "addEventListener" (fn [event handler]
                                (xt/x:set-key handlers event handler)
                                (return true))}))
    [(ws/client? client)
     (promise/x:promise-native? (ws/send async-client "hello"))
     (ws/send-sync client "world")
     (promise/x:promise-native? (ws/disconnect async-client 1000 "done"))
     (ws/disconnect-sync client 1001 "done-sync")
     (promise/x:promise-native? (ws/add-listener async-client "message" (fn [payload] (return payload))))
     (xt/x:is-object? (ws/add-listener-sync client "message" (fn [payload] (return payload))))
     sent
     closed
     (xt/x:has-key? handlers "message")])
  => [true true true true true true true
      ["world"]
      [[1001 "done-sync"]]
      true])

^{:refer python.lib.client-websocket/driver :added "4.1.3"}
(fact "connects through the python websocket driver with promise and sync variants"

  [(!.py
     (var driver
          (py-ws/driver
           (fn [url]
             (return {"url" url
                      "send" (fn [_payload] (return true))
                      "close" (fn [_code _reason] (return true))
                      "addEventListener" (fn [_event _handler] (return true))}))))
     (promise/x:promise-native? (ws/connect driver "wss://demo.test/socket")))
    (!.py
     (var driver
          (py-ws/driver
           (fn [url]
             (return {"url" url
                      "send" (fn [_payload] (return true))
                      "close" (fn [_code _reason] (return true))
                      "addEventListener" (fn [_event _handler] (return true))}))))
      (var client (ws/connect-sync driver "wss://demo.test/socket"))
      [(ws/client? client)
       (. (. client ["_raw"]) ["url"])])]
  => [true
      [true "wss://demo.test/socket"]])
