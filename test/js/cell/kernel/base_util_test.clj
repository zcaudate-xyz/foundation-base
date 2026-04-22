(ns js.cell.kernel.base-util-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [js.core :as j]
             [js.cell.kernel.base-util :as base-util]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.base-util/EV_INIT :added "4.0" :unchecked true}
(fact "event constant for init"

  (!.js base-util/EV_INIT)
  => "@worker/::INIT")

^{:refer js.cell.kernel.base-util/EV_STATE :added "4.0" :unchecked true}
(fact "event constant for state"

  (!.js base-util/EV_STATE)
  => "@worker/::STATE")

^{:refer js.cell.kernel.base-util/rand-id :added "4.0" :unchecked true}
(fact "prepares a random id"

  (!.js (base-util/rand-id "test-" 8))
  => string?

  (!.js (xt/x:len (base-util/rand-id "" 10)))
  => 10

  (!.js (k/is-string? (base-util/rand-id "prefix-" 6)))
  => true)

^{:refer js.cell.kernel.base-util/check-event :added "4.0" :unchecked true}
(fact "checks that trigger matches signal and event"

  (!.js (base-util/check-event true "signal" {} {}))
  => true

  (!.js (base-util/check-event false "signal" {} {}))
  => false

  (!.js (base-util/check-event "test-signal" "test-signal" {} {}))
  => true

  (!.js (base-util/check-event "other" "test-signal" {} {}))
  => false

  ;; Test with predicate function
  (!.js (base-util/check-event (fn [s c] (return (== s "match"))) "match" {} {}))
  => true)

^{:refer js.cell.kernel.base-util/arg-encode :added "4.0" :unchecked true}
(fact "encodes functions in data tree"

  (!.js (base-util/arg-encode [1 2 3]))
  => [1 2 3]

  (!.js (base-util/arg-encode {"data" [1 2 3]}))
  => {"data" [1 2 3]})

^{:refer js.cell.kernel.base-util/arg-decode :added "4.0" :unchecked true}
(fact "decodes function in data tree"

  (!.js (base-util/arg-decode [1 2 3]))
  => [1 2 3]

  (!.js (base-util/arg-decode {"key" "value"}))
  => {"key" "value"})

^{:refer js.cell.kernel.base-util/req-frame :added "4.0" :unchecked true}
(fact "constructs a protocol frame"

  (!.js (base-util/req-frame "call" "id-1" [1 2] {} {}))
  => (contains {"op" "call" "id" "id-1" "body" [1 2]}))

^{:refer js.cell.kernel.base-util/req-call :added "4.0" :unchecked true}
(fact "constructs a call request"

  (!.js (base-util/req-call "@worker/ping" []))
  => {"op" "call" "action" "@worker/ping" "body" []})

^{:refer js.cell.kernel.base-util/req-eval :added "4.0" :unchecked true}
(fact "constructs an eval request"

  (!.js (base-util/req-eval "1 + 1"))
  => {"op" "eval" "body" "1 + 1"}

  (!.js (base-util/req-eval "1 + 1" true))
  => {"op" "eval" "body" "1 + 1" "async" true})

^{:refer js.cell.kernel.base-util/resp-ok :added "4.0" :unchecked true}
(fact "constructs an ok response"

  (!.js (base-util/resp-ok "call" "id-1" "result"))
  => {"op" "call" "id" "id-1" "status" "ok" "body" "result"})

^{:refer js.cell.kernel.base-util/resp-error :added "4.0" :unchecked true}
(fact "constructs an error response"

  (!.js (base-util/resp-error "call" "id-1" "error-msg"))
  => {"op" "call" "id" "id-1" "status" "error" "body" "error-msg"})

^{:refer js.cell.kernel.base-util/resp-stream :added "4.0" :unchecked true}
(fact "constructs a stream response"

  (!.js (base-util/resp-stream "@worker/::STATE" {"eval" true}))
  => {"op" "stream" "status" "ok" "signal" "@worker/::STATE" "body" {"eval" true}})
