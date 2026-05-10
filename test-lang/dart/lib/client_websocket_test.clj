(ns dart.lib.client-websocket-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [dart.lib.client-websocket :as dart-ws]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(def CANARY-DART
  (common/program-exists? "dart"))

^{:refer dart.lib.client-websocket/client :added "4.1.3"}
(fact "wraps raw dart websocket clients with sync and async client methods"

  (if CANARY-DART
    (!.dt
     (var handlers {})
     (var sent [])
     (var closed [])
     (var async-client
          (dart-ws/client
           {"send" (fn [_payload]
                     (return true))
            "close" (fn [_code _reason]
                      (return true))
            "addEventListener" (fn [_event _handler]
                                 (return true))}))
     (var client
           (dart-ws/client
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
     :dart-unavailable)
  => (any [true true true true true true true
           ["world"]
           [[1001 "done-sync"]]
           true]
          :dart-unavailable))

^{:refer dart.lib.client-websocket/driver :added "4.1.3"}
(fact "connects through the dart websocket driver with promise and sync variants"

  (if CANARY-DART
    [(!.dt
      (var driver
           (dart-ws/driver
            (fn [url]
              (return {"url" url
                       "send" (fn [_payload] (return true))
                       "close" (fn [_code _reason] (return true))
                       "addEventListener" (fn [_event _handler] (return true))}))))
      (promise/x:promise-native? (ws/connect driver "wss://demo.test/socket")))
     (!.dt
      (var driver
           (dart-ws/driver
            (fn [url]
              (return {"url" url
                       "send" (fn [_payload] (return true))
                       "close" (fn [_code _reason] (return true))
                       "addEventListener" (fn [_event _handler] (return true))}))))
       (var client (ws/connect-sync driver "wss://demo.test/socket"))
       [(ws/client? client)
        (. (. client ["_raw"]) ["url"])])]
    :dart-unavailable)
  => (any [true
           [true "wss://demo.test/socket"]]
          :dart-unavailable))
