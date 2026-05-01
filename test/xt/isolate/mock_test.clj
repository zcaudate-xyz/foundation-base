(ns xt.isolate.mock-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.endpoint :as endpoint]
             [xt.isolate.mock :as mock]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.isolate.mock/mock-endpoint-send :added "4.0"}
(fact "sends a frame directly to the mock endpoint's processor"

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (mock/mock-endpoint-send isolate {:op "call"
                                     :id "test-1"
                                     :route "@isolate/ping"
                                     :body []})
   (xt/x:get-key (xt/x:first events) "status"))
  => "ok")

^{:refer xt.isolate.mock/mock-endpoint :added "4.0"}
(fact "creates a bare mock endpoint that forwards events to a listener"

  (!.js
   (var messages [])
   (var isolate (mock/mock-endpoint
                 (fn [msg] (messages.push msg))))
   ((. isolate ["emit"]) {"test" 1})
   messages)
  => [{"test" 1}]

  (!.js
   (var isolate (mock/mock-endpoint nil))
   (xt/x:get-key isolate "::"))
  => "isolate.mock"

  (!.js
   (var isolate (mock/mock-endpoint nil))
   (k/is-function? (xt/x:get-key isolate "emit")))
  => true)

^{:refer xt.isolate.mock/create-endpoint :added "4.0"}
(fact "creates and initialises a fully configured mock endpoint"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (xt/x:get-key isolate "::"))
  => "isolate.mock"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (k/is-function? (xt/x:get-key isolate "emit")))
  => true

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (xt/x:has-key? (endpoint/get-routes isolate) "@isolate/ping"))
  => true

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   ;; emit the init signal directly and check it
   (endpoint/endpoint-init-signal isolate {:done true})
   (xt/x:get-key (xt/x:first events) "topic"))
  => "@isolate/::INIT")

^{:refer xt.isolate.mock/make-transport :added "4.0"}
(fact "builds a transport capability map backed by the given mock endpoint"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (var transport (mock/make-transport isolate))
   [(k/is-function? (. transport ["send"]))
    (k/is-function? (. transport ["listen"]))])
  => [true true]

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (var transport (mock/make-transport isolate))
   ;; transport.send drives the endpoint and emits a response to listeners
   ((. transport ["send"]) {:op "call"
                            :id "t1"
                            :route "@isolate/ping"
                            :body []})
   (xt/x:get-key (xt/x:first events) "status"))
  => "ok")
