(ns xt.isolate.mock-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.mock :as mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.isolate.mock/mock-endpoint-send :added "4.0"}
(fact "sends a frame directly to the mock endpoint's processor"

  (!.js
   (var isolate (mock/mock-endpoint (fn [msg])))
   (mock/mock-endpoint-send isolate {"op" "call" "route" "@isolate/ping" "body" []}))
  => nil?)

^{:refer xt.isolate.mock/mock-endpoint :added "4.0"}
(fact "creates a bare mock endpoint that forwards events to a listener"

  (!.js
   (var messages [])
   (var isolate (mock/mock-endpoint
                 (fn [msg] (messages.push msg))))
   ((. isolate ["emit"]) {"test" 1})
   messages)
  => [{"test" 1}])

^{:refer xt.isolate.mock/create-endpoint :added "4.0"}
(fact "creates and initialises a fully configured mock endpoint"

  (!.js
   (var isolate (mock/create-endpoint
                 (fn [msg])
                 {}
                 true))
   (xt/x:get-key isolate "::"))
  => "isolate.mock"

  (!.js
   (var isolate (mock/create-endpoint k/identity {} true))
   (k/is-function? (xt/x:get-key isolate "emit")))
  => true

  (notify/wait-on :js
    (mock/create-endpoint (repl/>notify) {} false))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "topic" "@isolate/::INIT"})

^{:refer xt.isolate.mock/make-transport :added "4.0"}
(fact "builds a transport capability map backed by the given mock endpoint"

  (!.js
   (var isolate (mock/create-endpoint (fn [msg]) {} true))
   (var transport (mock/make-transport isolate))
   [(k/is-function? (. transport ["send"]))
    (k/is-function? (. transport ["listen"]))])
  => [true true])
