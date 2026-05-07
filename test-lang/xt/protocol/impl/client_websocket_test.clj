(ns xt.protocol.impl.client-websocket-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.client-websocket/driver-create :added "4.1.3"}
(fact "wraps websocket drivers and connections"

  (!.js
   (var handlers {})
   (var sent [])
   (var closed [])
   (var driver
        (ws/driver-create
         {"connect"
          (fn [url]
            (return {"url" url
                     "send" (fn [payload]
                              (x:arr-push sent payload)
                              (return true))
                     "close" (fn [code reason]
                               (x:arr-push closed [code reason])
                               (return true))
                     "addEventListener" (fn [event handler]
                                          (x:set-key handlers event handler)
                                          (return true))}))}))
   (var client (ws/connect driver "wss://demo.test/socket"))
   (ws/add-listener client "message" (fn [payload] (return payload)))
   (ws/send client "hello")
   (ws/disconnect client 1000 "done")
   [(ws/driver? driver)
    (ws/client? client)
    sent
    closed
    (x:has-key? handlers "message")])
  => [true true ["hello"] [[1000 "done"]] true])
