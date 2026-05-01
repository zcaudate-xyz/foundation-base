(ns xt.isolate.client-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :xtalk
  {:require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.client :as client]
             [xt.isolate.mock :as mock]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.client :as client]
             [xt.isolate.mock :as mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

(defn.xt make-client
  []
  (var isolate (mock/create-endpoint nil {} true))
  (var transport (mock/make-transport isolate))
  (return (client/client-create transport)))

^{:refer xt.isolate.client/client-listener-call :added "4.0"}
(fact "resolves or rejects an active call from a response frame"

  (!.js
   (client/client-listener-call {:op "call"
                                  :id "hello"
                                  :status "ok"
                                  :body "result"}
                                 {:hello {:resolve k/identity}}))
  => "result"

  (!.js
   (client/client-listener-call {:op "call"
                                  :id "hello"
                                  :status "error"
                                  :message "Server Error"}
                                 {:hello {:resolve k/identity
                                          :reject  k/identity}}))
  => (contains {"message" "Server Error"
                "id" "hello"
                "status" "error"
                "op" "call"}))

^{:refer xt.isolate.client/client-listener-event :added "4.0"}
(fact "dispatches stream events to matching subscriptions"

  (!.js
   (client/client-listener-event {:op "stream"
                                   :topic "hello"}
                                  {:hello {:pred true
                                           :handler (fn:> true)}
                                   :world {:handler (fn:> true)}
                                   :again {:pred (fn:> [t e] (not= t "hello"))
                                           :handler (fn:> true)}}))
  => ["hello" "world"])

^{:refer xt.isolate.client/client-listener :added "4.0"}
(fact "routes an incoming response frame to the correct handler"

  (!.js
   (client/client-listener {:op "call"
                             :id "hello"
                             :status "ok"
                             :body "result"}
                            {:hello {:resolve k/identity}}
                            {}))
  => "result")

^{:refer xt.isolate.client/client-create :added "4.0"}
(fact "creates a client from a transport map"

  (!.js
   (xt/x:get-key (-/make-client) "::"))
  => "isolate.client"

  (notify/wait-on :js
    (var cl (-/make-client))
    (. (client/client-call cl {:op    "call"
                               :route "@isolate/ping"
                               :body  []})
       (then (repl/>notify))))
  => (contains ["pong" integer?]))

^{:refer xt.isolate.client/client-active :added "4.0"}
(fact "tracks active calls on the client"

  (vals
   (!.js
    (var cl (-/make-client))
    (client/client-call cl {:op    "call"
                            :route "@isolate/ping.async"
                            :body  [100]})
    (client/client-active cl)))
  => (contains-in
      [{"input" {"body" [100] "route" "@isolate/ping.async" "op" "call"}}]))

^{:refer xt.isolate.client/client-subscribe :added "4.0"}
(fact "adds a topic subscription to the client"

  (!.js
   (var cl (-/make-client))
   (client/client-subscribe cl "hello" true (fn:> true))
   (. cl ["subscriptions"] ["hello"]))
  => (contains {"key" "hello"
                "pred" true}))

^{:refer xt.isolate.client/client-subscriptions :added "4.0"}
(fact "lists subscription keys on the client"

  (!.js
   (var cl (-/make-client))
   (client/client-subscribe cl "a" true (fn:> true))
   (client/client-subscribe cl "b" true (fn:> true))
   (client/client-subscriptions cl))
  => ["a" "b"])

^{:refer xt.isolate.client/client-unsubscribe :added "4.0"}
(fact "removes a topic subscription from the client"

  (!.js
   (var cl (-/make-client))
   (client/client-subscribe cl "a" true (fn:> true))
   (client/client-unsubscribe cl "a"))
  => (contains-in [{"key" "a" "pred" true}]))

^{:refer xt.isolate.client/client-call-id :added "4.0"}
(fact "generates a fresh correlation id"

  (!.js
   (client/client-call-id (-/make-client)))
  => string?)

^{:refer xt.isolate.client/client-call :added "4.0"}
(fact "sends a request frame and returns a Promise for the response"

  (notify/wait-on :js
    (var cl (-/make-client))
    (. (client/client-call cl {:op    "call"
                               :route "@isolate/ping"
                               :body  []})
       (then (repl/>notify))))
  => (contains ["pong" integer?])

  (notify/wait-on :js
    (var cl (-/make-client))
    (. (client/client-call cl {:op    "call"
                               :route "@isolate/error.async"
                               :body  [100]})
       (catch (repl/>notify))))
  => (contains-in
      {"body" ["error" integer?]
       "route" "@isolate/error.async"
       "status" "error"
       "op" "call"}))

^{:refer xt.isolate.client/client-notify :added "4.0"}
(fact "sends a fire-and-forget notification frame"

  (!.js
   (var cl (-/make-client))
   (client/client-notify cl {:op    "notify"
                             :route "@isolate/ping"
                             :body  []}))
  => nil?)
