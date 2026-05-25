(ns xt.protocol.impl.client-websocket-test
  (:use code.test)
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :python :dart :lua.nginx]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(l/script- :lua.nginx
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(def CANARY-RESTY
  (common/program-exists? "resty"))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.client-websocket/ensure-promise :added "4.1.3"}
(fact "normalises raw values into native promises"

  (!.js
   (promise/x:promise-native? (ws/ensure-promise true)))
  => true)

^{:refer xt.protocol.impl.client-websocket/client-create :added "4.1.3"}
(fact "supports python and dart and lua.nginx raw websocket method conventions"

  (!.py
   (var handlers {})
   (var sent [])
   (var closed [])
   (var client
        (ws/client-create
         {"send" (fn [payload]
                   (xt/x:arr-push sent payload)
                   (return true))
          "close" (fn [code reason]
                    (xt/x:arr-push closed [code reason])
                    (return true))
          "add_listener" (fn [event handler]
                           (xt/x:set-key handlers event handler)
                           (return true))}
         {}))
   [(promise/x:promise-native? (ws/send client "hello-py"))
    (ws/send-sync client "world-py")
    (promise/x:promise-native? (ws/disconnect client 1000 "done-py"))
    (ws/disconnect-sync client 1001 "done-sync-py")
    (promise/x:promise-native? (ws/add-listener client "message" (fn [payload] (return payload))))
    (xt/x:is-object? (ws/add-listener-sync client "custom" (fn [payload] (return payload))))
    sent
    closed
    (xt/x:has-key? handlers "message")
    (xt/x:has-key? handlers "custom")])
  => [true true true true true true
      ["hello-py" "world-py"]
      [[1000 "done-py"] [1001 "done-sync-py"]]
      true true]

  (if CANARY-DART
    (!.dt
     (var listened [])
     (var sent [])
     (var closed [])
     (var client
          (ws/client-create
           {"add" (fn [payload]
                    (xt/x:arr-push sent payload)
                    (return true))
            "close" (fn [code reason]
                      (xt/x:arr-push closed [code reason])
                      (return true))
            "listen" (fn [handler]
                       (xt/x:arr-push listened handler)
                       (return {"cancel" (fn [] (return true))}))}
           {}))
     [(promise/x:promise-native? (ws/send client "hello-dt"))
      (ws/send-sync client "world-dt")
      (promise/x:promise-native? (ws/disconnect client 1000 "done-dt"))
      (ws/disconnect-sync client 1001 "done-sync-dt")
      (promise/x:promise-native? (ws/add-listener client "message" (fn [payload] (return payload))))
      (xt/x:is-object? (ws/add-listener-sync client "message" (fn [payload] (return payload))))
      (>= (xt/x:len listened) 2)
      (>= (xt/x:len sent) 2)
      (>= (xt/x:len closed) 2)])
    :dart-unavailable)
  => (any [true true true true true true
           true
           true
           true]
          :dart-unavailable)

  (if CANARY-RESTY
    (!.lua
     (local handlers {})
     (local sent [])
     (local closed [])
     (local client
            (ws/client-create
             {"send_text" (fn [payload]
                            (xt/x:arr-push sent payload)
                            (return true))
              "send_close" (fn [code reason]
                             (xt/x:arr-push closed [code reason])
                             (return true))
              "on" (fn [event handler]
                     (xt/x:set-key handlers event handler)
                     (return true))}
             {}))
      [(promise/x:promise-native? (ws/send client "hello-lua"))
       (ws/send-sync client "world-lua")
       (promise/x:promise-native? (ws/disconnect client 1000 "done-lua"))
       (ws/disconnect-sync client 1001 "done-sync-lua")
       (promise/x:promise-native? (ws/add-listener client "message" (fn [payload] (return payload))))
       (xt/x:is-object? (ws/add-listener-sync client "custom" (fn [payload] (return payload))))
       sent
       closed
       (xt/x:has-key? handlers "message")
       (xt/x:has-key? handlers "custom")])
    :resty-unavailable)
  => (any [true true true true true true
           ["hello-lua" "world-lua"]
           [[1000 "done-lua"] [1001 "done-sync-lua"]]
           true true]
          :resty-unavailable))

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
