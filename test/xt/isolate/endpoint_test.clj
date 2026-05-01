(ns xt.isolate.endpoint-test
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

^{:refer xt.isolate.endpoint/get-state :added "4.0"}
(fact "returns the current endpoint state"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (endpoint/get-state isolate))
  => (contains {"eval" true}))

^{:refer xt.isolate.endpoint/get-routes :added "4.0"}
(fact "returns the current route table"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (xt/x:has-key? (endpoint/get-routes isolate) "@isolate/ping"))
  => true)

^{:refer xt.isolate.endpoint/set-routes :added "4.0"}
(fact "installs routes into the route table"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (endpoint/set-routes {"@custom/action" {:handler (fn [x] (return x))
                                           :is-async false
                                           :args ["x"]}}
                        isolate)
   (xt/x:has-key? (endpoint/get-routes isolate) "@custom/action"))
  => true)

^{:refer xt.isolate.endpoint/routes-baseline :added "4.0"}
(fact "returns the default baseline routes"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   [(xt/x:has-key? (endpoint/routes-baseline isolate) "@isolate/ping")
    (xt/x:has-key? (endpoint/routes-baseline isolate) "@isolate/echo")])
  => [true true])

^{:refer xt.isolate.endpoint/routes-init :added "4.0"}
(fact "installs baseline routes merged with extra routes"

  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (endpoint/routes-init {"@custom/check" {:handler (fn [] (return true))
                                           :is-async false
                                           :args []}}
                         isolate)
   [(xt/x:has-key? (endpoint/get-routes isolate) "@isolate/ping")
    (xt/x:has-key? (endpoint/get-routes isolate) "@custom/check")])
  => [true true])

^{:refer xt.isolate.endpoint/endpoint-process :added "4.0"}
(fact "processes an incoming request frame dispatching by op"

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (endpoint/endpoint-process isolate {:op "call"
                                       :id "x1"
                                       :route "@isolate/ping"
                                       :body []})
   (k/select-keys (xt/x:first events) ["op" "id" "status"]))
  => {"op" "call" "id" "x1" "status" "ok"}

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (endpoint/endpoint-process isolate {:op "call"
                                       :id "x2"
                                       :route "@isolate/unknown"
                                       :body []})
   (k/select-keys (xt/x:first events) ["op" "id" "status"]))
  => {"op" "call" "id" "x2" "status" "error"})

^{:refer xt.isolate.endpoint/endpoint-init-signal :added "4.0"}
(fact "emits the init event to signal that the endpoint is ready"

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (endpoint/endpoint-init-signal isolate {:done true})
   (xt/x:first events))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "topic" "@isolate/::INIT"})

^{:refer xt.isolate.endpoint/fn-ping :added "4.0"}
(fact "pings the endpoint"

  (!.js (endpoint/fn-ping))
  => (contains ["pong" integer?]))

^{:refer xt.isolate.endpoint/fn-echo :added "4.0"}
(fact "echoes the first argument"

  (!.js (endpoint/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer xt.isolate.endpoint/fn-error :added "4.0"}
(fact "throws an error"

  (!.js
   (try (endpoint/fn-error)
        (catch e (return (xt/x:first e)))))
  => "error")

^{:refer xt.isolate.endpoint/fn-get-eval :added "4.0"}
(fact "returns the eval-enabled flag"

  (!.js (endpoint/fn-get-eval))
  => true)

^{:refer xt.isolate.endpoint/fn-get-route-list :added "4.0"}
(fact "lists available route names"

  (!.js
   (k/in-array? "@isolate/ping"
                (endpoint/fn-get-route-list)))
  => true)

^{:refer xt.isolate.endpoint/fn-set-eval :added "4.0"}
(fact "enables or disables eval in the isolate"

  (!.js
   (var events [])
   (var isolate (mock/create-endpoint
                 (fn [e] (events.push e))
                 {}
                 true))
   (endpoint/fn-set-eval isolate false false)
   (endpoint/fn-get-eval))
  => false

  ;; reset for subsequent tests
  (!.js
   (var isolate (mock/create-endpoint nil {} true))
   (endpoint/fn-set-eval isolate true false)
   (endpoint/fn-get-eval))
  => true)
