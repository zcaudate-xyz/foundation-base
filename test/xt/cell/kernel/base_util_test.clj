(ns xt.cell.kernel.base-util-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.cell.kernel.base-util :as base-util]
             [xt.lang.common-lib :as k]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.base-util/rand-id :added "4.1"}
(fact "builds a random id with the requested prefix and size"
  ^:hidden

  (!.js
   (var out (base-util/rand-id "id-" 4))
   [(. out ["startsWith"] "id-")
    (k/len out)])
  => [true 7])

^{:refer xt.cell.kernel.base-util/check-event :added "4.1"}
(fact "supports nil, literal, boolean, and functional event predicates"
  ^:hidden

  (!.js
   [(base-util/check-event nil "refresh" {} {})
    (base-util/check-event "refresh" "refresh" {} {})
    (base-util/check-event false "refresh" {} {})
    (base-util/check-event
     (fn [signal ctx]
       (return (fn [event inner-ctx]
                 (return (== (. event ["id"]) (. inner-ctx ["id"]))))))
     "refresh"
     {"id" "evt-1"}
     {"id" "evt-1"})])
  => [true true false true])

^{:refer xt.cell.kernel.base-util/arg-encode :added "4.1"}
(fact "encodes functions inside payload trees"
  ^:hidden

  (!.js
   (base-util/arg-encode
    {"f" (fn [x] (return (+ x 1)))}))
  => {"f" ["fn" string?]})

^{:refer xt.cell.kernel.base-util/arg-decode :added "4.1"}
(fact "decodes encoded functions back into callable values"
  ^:hidden

  (!.js
   (var encoded
        (base-util/arg-encode
         {"f" (fn [x] (return (+ x 1)))}))
   ((. (base-util/arg-decode encoded) ["f"]) 2))
  => 3)

^{:refer xt.cell.kernel.base-util/req-frame :added "4.1"}
(fact "builds protocol frames with merged metadata and extras"
  ^:hidden

  (!.js
   (base-util/req-frame
    "call"
    "id-1"
    {"hello" true}
    {"source" "test"}
    {"action" "@worker/echo"}))
  => {"op" "call"
      "id" "id-1"
      "body" {"hello" true}
      "meta" {"source" "test"}
      "action" "@worker/echo"})

^{:refer xt.cell.kernel.base-util/req-call :added "4.1"}
(fact "builds raw call requests"
  ^:hidden

  (!.js
   (base-util/req-call "@worker/echo" ["hello"]))
  => {"op" "call"
      "action" "@worker/echo"
      "body" ["hello"]})

^{:refer xt.cell.kernel.base-util/req-eval :added "4.1"}
(fact "builds raw eval requests"
  ^:hidden

  (!.js
   (base-util/req-eval "1 + 1" true))
  => {"op" "eval"
      "body" "1 + 1"
      "async" true})

^{:refer xt.cell.kernel.base-util/resp-ok :added "4.1"}
(fact "builds ok response frames"
  ^:hidden

  (!.js
   (base-util/resp-ok "call" "id-1" ["pong" 1]))
  => {"op" "call"
      "id" "id-1"
      "status" "ok"
      "body" ["pong" 1]})

^{:refer xt.cell.kernel.base-util/resp-error :added "4.1"}
(fact "builds error response frames"
  ^:hidden

  (!.js
   (base-util/resp-error "call" "id-2" {"message" "boom"}))
  => {"op" "call"
      "id" "id-2"
      "status" "error"
      "body" {"message" "boom"}})

^{:refer xt.cell.kernel.base-util/resp-stream :added "4.1"}
(fact "builds stream response frames"
  ^:hidden

  (!.js
   (base-util/resp-stream "orders/updated" {"id" 1}))
  => {"op" "stream"
      "status" "ok"
      "signal" "orders/updated"
      "body" {"id" 1}})
