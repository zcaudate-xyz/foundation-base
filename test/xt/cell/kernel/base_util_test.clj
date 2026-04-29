(ns xt.cell.kernel.base-util-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt] [xt.cell.kernel.base-util :as base-util]] :runtime :basic})

(l/script- :lua
  {:require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt] [xt.cell.kernel.base-util :as base-util]] :runtime :basic})

(l/script- :python
  {:require [[xt.lang.common-lib :as k] [xt.lang.spec-base :as xt] [xt.cell.kernel.base-util :as base-util]] :runtime :basic})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.base-util/EV_INIT :added "4.0"}
(fact "event constant for init"

  (!.js base-util/EV_INIT)
  => "@worker/::INIT"

  (!.lua base-util/EV_INIT)
  => "@worker/::INIT"

  (!.py base-util/EV_INIT)
  => "@worker/::INIT")

^{:refer xt.cell.kernel.base-util/EV_STATE :added "4.0"}
(fact "event constant for state"

  (!.js base-util/EV_STATE)
  => "@worker/::STATE"

  (!.lua base-util/EV_STATE)
  => "@worker/::STATE"

  (!.py base-util/EV_STATE)
  => "@worker/::STATE")

^{:refer xt.cell.kernel.base-util/rand-id :added "4.0"}
(fact "prepares a random id"

  (!.js (base-util/rand-id "test-" 8))
  => string?

  (!.js (xt/x:len (base-util/rand-id "" 10)))
  => 10

  (!.js (k/is-string? (base-util/rand-id "prefix-" 6)))
  => true

  (!.lua (base-util/rand-id "test-" 8))
  => string?

  (!.lua (xt/x:len (base-util/rand-id "" 10)))
  => 10

  (!.lua (k/is-string? (base-util/rand-id "prefix-" 6)))
  => true

  (!.py (base-util/rand-id "test-" 8))
  => string?

  (!.py (xt/x:len (base-util/rand-id "" 10)))
  => 10

  (!.py (k/is-string? (base-util/rand-id "prefix-" 6)))
  => true)

^{:refer xt.cell.kernel.base-util/check-event :added "4.0"}
(fact "checks that trigger matches signal and event"

  (!.js (base-util/check-event true "signal" {} {}))
  => true

  (!.js (base-util/check-event false "signal" {} {}))
  => false

  (!.js (base-util/check-event "test-signal" "test-signal" {} {}))
  => true

  (!.js (base-util/check-event "other" "test-signal" {} {}))
  => false

  (!.js (base-util/check-event (fn [s c] (return (== s "match"))) "match" {} {}))
  => true

  (!.lua (base-util/check-event true "signal" {} {}))
  => true

  (!.lua (base-util/check-event false "signal" {} {}))
  => false

  (!.lua (base-util/check-event "test-signal" "test-signal" {} {}))
  => true

  (!.lua (base-util/check-event "other" "test-signal" {} {}))
  => false

  (!.lua (base-util/check-event (fn [s c] (return (== s "match"))) "match" {} {}))
  => true

  (!.py (base-util/check-event true "signal" {} {}))
  => true

  (!.py (base-util/check-event false "signal" {} {}))
  => false

  (!.py (base-util/check-event "test-signal" "test-signal" {} {}))
  => true

  (!.py (base-util/check-event "other" "test-signal" {} {}))
  => false

  (!.py (base-util/check-event (fn [s c] (return (== s "match"))) "match" {} {}))
  => true)

^{:refer xt.cell.kernel.base-util/arg-encode :added "4.0"}
(fact "encodes functions in a data tree"

  (!.js (base-util/arg-encode [1 2 3]))
  => [1 2 3]

  (!.js (base-util/arg-encode {"data" [1 2 3]}))
  => {"data" [1 2 3]}

  (!.lua (base-util/arg-encode [1 2 3]))
  => [1 2 3]

  (!.lua (base-util/arg-encode {"data" [1 2 3]}))
  => {"data" [1 2 3]}

  (!.py (base-util/arg-encode [1 2 3]))
  => [1 2 3]

  (!.py (base-util/arg-encode {"data" [1 2 3]}))
  => {"data" [1 2 3]})

^{:refer xt.cell.kernel.base-util/arg-decode :added "4.0"}
(fact "decodes functions in a data tree"

  (!.js (base-util/arg-decode [1 2 3]))
  => [1 2 3]

  (!.js (base-util/arg-decode {"key" "value"}))
  => {"key" "value"}

  (!.lua (base-util/arg-decode [1 2 3]))
  => [1 2 3]

  (!.lua (base-util/arg-decode {"key" "value"}))
  => {"key" "value"}

  (!.py (base-util/arg-decode [1 2 3]))
  => [1 2 3]

  (!.py (base-util/arg-decode {"key" "value"}))
  => {"key" "value"})

^{:refer xt.cell.kernel.base-util/req-frame :added "4.0"}
(fact "constructs a protocol frame"

  (!.js (base-util/req-frame "call" "id-1" [1 2] {} {}))
  => (contains {"op" "call" "id" "id-1" "body" [1 2]})

  (!.lua (base-util/req-frame "call" "id-1" [1 2] {} {}))
  => (contains {"op" "call" "id" "id-1" "body" [1 2]})

  (!.py (base-util/req-frame "call" "id-1" [1 2] {} {}))
  => (contains {"op" "call" "id" "id-1" "body" [1 2]}))

^{:refer xt.cell.kernel.base-util/req-call :added "4.0"}
(fact "constructs a call request"

  (!.js (base-util/req-call "@worker/ping" []))
  => {"op" "call" "action" "@worker/ping" "body" []}

  (!.lua (base-util/req-call "@worker/ping" []))
  => {"op" "call" "action" "@worker/ping" "body" []}

  (!.py (base-util/req-call "@worker/ping" []))
  => {"op" "call" "action" "@worker/ping" "body" []})

^{:refer xt.cell.kernel.base-util/req-eval :added "4.0"}
(fact "constructs an eval request"

  (!.js (base-util/req-eval "1 + 1"))
  => {"op" "eval" "body" "1 + 1"}

  (!.js (base-util/req-eval "1 + 1" true))
  => {"op" "eval" "body" "1 + 1" "async" true}

  (!.lua (base-util/req-eval "1 + 1"))
  => {"op" "eval" "body" "1 + 1"}

  (!.lua (base-util/req-eval "1 + 1" true))
  => {"op" "eval" "body" "1 + 1" "async" true}

  (!.py (base-util/req-eval "1 + 1"))
  => {"op" "eval" "body" "1 + 1"}

  (!.py (base-util/req-eval "1 + 1" true))
  => {"op" "eval" "body" "1 + 1" "async" true})

^{:refer xt.cell.kernel.base-util/resp-ok :added "4.0"}
(fact "constructs an ok response"

  (!.js (base-util/resp-ok "call" "id-1" "result"))
  => {"op" "call" "id" "id-1" "status" "ok" "body" "result"}

  (!.lua (base-util/resp-ok "call" "id-1" "result"))
  => {"op" "call" "id" "id-1" "status" "ok" "body" "result"}

  (!.py (base-util/resp-ok "call" "id-1" "result"))
  => {"op" "call" "id" "id-1" "status" "ok" "body" "result"})

^{:refer xt.cell.kernel.base-util/resp-error :added "4.0"}
(fact "constructs an error response"

  (!.js (base-util/resp-error "call" "id-1" "error-msg"))
  => {"op" "call" "id" "id-1" "status" "error" "body" "error-msg"}

  (!.lua (base-util/resp-error "call" "id-1" "error-msg"))
  => {"op" "call" "id" "id-1" "status" "error" "body" "error-msg"}

  (!.py (base-util/resp-error "call" "id-1" "error-msg"))
  => {"op" "call" "id" "id-1" "status" "error" "body" "error-msg"})

^{:refer xt.cell.kernel.base-util/resp-stream :added "4.0"}
(fact "constructs a stream response"

  (!.js (base-util/resp-stream "@worker/::STATE" {"eval" true}))
  => {"op" "stream" "status" "ok" "signal" "@worker/::STATE" "body" {"eval" true}}

  (!.lua (base-util/resp-stream "@worker/::STATE" {"eval" true}))
  => {"op" "stream" "status" "ok" "signal" "@worker/::STATE" "body" {"eval" true}}

  (!.py (base-util/resp-stream "@worker/::STATE" {"eval" true}))
  => {"op" "stream" "status" "ok" "signal" "@worker/::STATE" "body" {"eval" true}})
