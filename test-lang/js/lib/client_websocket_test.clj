(ns js.lib.client-websocket-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [js.lib.client-websocket :as js-ws]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.client-websocket/client :added "4.1.3"}
(fact "wraps raw js websocket clients with sync and async client methods"

  (!.js
   (var handlers {})
   (var sent [])
   (var closed [])
   (var client
        (js-ws/client
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
    (promise/x:promise-native? (ws/send client "hello"))
    (ws/send-sync client "world")
    (promise/x:promise-native? (ws/disconnect client 1000 "done"))
    (ws/disconnect-sync client 1001 "done-sync")
    (promise/x:promise-native? (ws/add-listener client "message" (fn [payload] (return payload))))
    sent
    closed
    (xt/x:has-key? handlers "message")])
  => [true true true true true true
      ["hello" "world"]
      [[1000 "done"] [1001 "done-sync"]]
      true])

^{:refer js.lib.client-websocket/driver :added "4.1.3"}
(fact "connects through the js websocket driver with a promise-returning connect"

  (!.js
   (var driver
        (js-ws/driver
         (fn [url]
           (return {"url" url
                    "send" (fn [_payload] (return true))
                    "close" (fn [_code _reason] (return true))
                    "addEventListener" (fn [_event _handler] (return true))}))))
   (promise/x:promise-native? (ws/connect driver "wss://demo.test/socket")))
  => true)
