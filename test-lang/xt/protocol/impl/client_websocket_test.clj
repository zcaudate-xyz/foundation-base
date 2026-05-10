(ns xt.protocol.impl.client-websocket-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.client-websocket/ensure-promise :added "4.1.3"}
(fact "normalises raw values into native promises"

  (!.js
   (promise/x:promise-native? (ws/ensure-promise true)))
  => true)

^{:refer xt.protocol.impl.client-websocket/driver-create :added "4.1.3"}
(fact "wraps websocket drivers and connections with async and sync methods"

  (!.js
   (var handlers {})
   (var sent [])
   (var closed [])
   (var connected [])
   (var driver
        (ws/driver-create
         {"connect_sync"
          (fn [url]
            (xt/x:arr-push connected url)
            (return {"url" url
                     "send" (fn [payload]
                              (xt/x:arr-push sent payload)
                              (return true))
                     "close" (fn [code reason]
                               (xt/x:arr-push closed [code reason])
                               (return true))
                     "addEventListener" (fn [event handler]
                                          (xt/x:set-key handlers event handler)
                                          (return true))}))}))
   (var client (ws/connect-sync driver "wss://demo.test/socket"))
    [(ws/driver? driver)
     (ws/client? client)
     (promise/x:promise-native? (ws/connect driver "wss://demo.test/socket"))
     (promise/x:promise-native? (ws/add-listener client "message" (fn [payload] (return payload))))
     (xt/x:is-object? (ws/add-listener-sync client "custom" (fn [payload] (return payload))))
     (promise/x:promise-native? (ws/send client "hello-1"))
     (ws/send-sync client "hello-2")
     (promise/x:promise-native? (ws/disconnect client 1000 "done"))
     (ws/disconnect-sync client 1001 "done-sync")
    connected
    sent
    closed
    (xt/x:has-key? handlers "message")
    (xt/x:has-key? handlers "custom")])
  => [true true true true true true true true true
      ["wss://demo.test/socket" "wss://demo.test/socket"]
      ["hello-1" "hello-2"]
      [[1000 "done"] [1001 "done-sync"]]
      true true])
