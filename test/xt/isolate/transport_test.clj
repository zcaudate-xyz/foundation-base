(ns xt.isolate.transport-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.transport :as transport]
             [xt.isolate.mock :as mock]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.isolate.transport/transport-make :added "4.0"}
(fact "creates a transport capability map from send/listen/close functions"

  (!.js
   (var t (transport/transport-make (fn [f]) (fn [h]) nil))
   [(k/is-function? (. t ["send"]))
    (k/is-function? (. t ["listen"]))])
  => [true true])

^{:refer xt.isolate.transport/transport-send :added "4.0"}
(fact "sends a frame through the transport"

  (!.js
   (var sent [])
   (var t (transport/transport-make
           (fn [f] (sent.push f))
           (fn [h])
           nil))
   (transport/transport-send t {:op "call" :body []})
   (xt/x:len sent))
  => 1)

^{:refer xt.isolate.transport/transport-listen :added "4.0"}
(fact "registers a handler for incoming frames on the transport"

  (!.js
   (var handlers [])
   (var t (transport/transport-make
           (fn [f])
           (fn [h] (handlers.push h))
           nil))
   (transport/transport-listen t (fn [d]))
   (xt/x:len handlers))
  => 1)

^{:refer xt.isolate.transport/transport-close :added "4.0"}
(fact "closes the transport if a close function is present"

  (!.js
   (var closed false)
   (var t (transport/transport-make
           (fn [f])
           (fn [h])
           (fn [] (:= closed true))))
   (transport/transport-close t)
   closed)
  => true

  (!.js
   (var t (transport/transport-make (fn [f]) (fn [h]) nil))
   (transport/transport-close t))
  => nil?)
