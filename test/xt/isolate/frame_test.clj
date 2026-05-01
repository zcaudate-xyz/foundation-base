(ns xt.isolate.frame-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.isolate.frame :as frame]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.isolate.frame/EV_INIT :added "4.0"}
(fact "event constant for isolate init"

  (!.js frame/EV_INIT)
  => "@isolate/::INIT")

^{:refer xt.isolate.frame/EV_STATE :added "4.0"}
(fact "event constant for isolate state"

  (!.js frame/EV_STATE)
  => "@isolate/::STATE")

^{:refer xt.isolate.frame/rand-id :added "4.0"}
(fact "prepares a random id"

  (!.js (frame/rand-id "test-" 8))
  => string?

  (!.js (xt/x:len (frame/rand-id "" 10)))
  => 10

  (!.js (k/is-string? (frame/rand-id "prefix-" 6)))
  => true)

^{:refer xt.isolate.frame/check-topic :added "4.0"}
(fact "checks that a pred matches a topic and event"

  (!.js (frame/check-topic true "topic" {}))
  => true

  (!.js (frame/check-topic false "topic" {}))
  => false

  (!.js (frame/check-topic "my-topic" "my-topic" {}))
  => true

  (!.js (frame/check-topic "other" "my-topic" {}))
  => false

  (!.js (frame/check-topic (fn [s e] (return (== s "match"))) "match" {}))
  => true)

^{:refer xt.isolate.frame/req-frame :added "4.0"}
(fact "constructs a generic request frame"

  (!.js (frame/req-frame "call" "id-1" [1 2] {} {}))
  => (contains {"op" "call" "id" "id-1" "body" [1 2]}))

^{:refer xt.isolate.frame/req-call :added "4.0"}
(fact "constructs a call request frame"

  (!.js (frame/req-call "@isolate/ping" []))
  => {"op" "call" "route" "@isolate/ping" "body" []})

^{:refer xt.isolate.frame/req-notify :added "4.0"}
(fact "constructs a notify request frame"

  (!.js (frame/req-notify "@isolate/ping" []))
  => {"op" "notify" "route" "@isolate/ping" "body" []})

^{:refer xt.isolate.frame/resp-ok :added "4.0"}
(fact "constructs an ok response frame"

  (!.js (frame/resp-ok "call" "id-1" "result"))
  => {"op" "call" "id" "id-1" "status" "ok" "body" "result"})

^{:refer xt.isolate.frame/resp-error :added "4.0"}
(fact "constructs an error response frame"

  (!.js (frame/resp-error "call" "id-1" "error-msg"))
  => {"op" "call" "id" "id-1" "status" "error" "body" "error-msg"})

^{:refer xt.isolate.frame/resp-event :added "4.0"}
(fact "constructs a broadcast event frame"

  (!.js (frame/resp-event "@isolate/::STATE" {"eval" true}))
  => {"op" "stream" "status" "ok" "topic" "@isolate/::STATE" "body" {"eval" true}})
