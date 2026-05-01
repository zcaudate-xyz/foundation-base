(ns xt.cell.kernel.inner-mock-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.cell.kernel.inner-mock :as inner-mock]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-mock/mock-worker-send :added "4.0"}
(fact "sends a request to the mock worker"

  (!.js
   (var worker (inner-mock/mock-worker (fn [msg])))
   (inner-mock/mock-worker-send worker {"op" "eval" "body" "1 + 1"}))
  => nil?)

^{:refer xt.cell.kernel.inner-mock/mock-worker :added "4.0"}
(fact "creates a mock worker"

  (!.js
   (var messages [])
   (var worker (inner-mock/mock-worker
                (fn [msg] (messages.push msg))))
   (worker.postMessage {"test" 1})
   messages)
  => [{"test" 1}])

^{:refer xt.cell.kernel.inner-mock/create-worker :added "4.0"}
(fact "initialises the mock worker"

  (!.js
   (var worker (inner-mock/create-worker
                (fn [msg])
                {}
                true))
   (xt/x:get-key worker "::"))
  => "worker.mock"

  (!.js
   (var worker (inner-mock/create-worker k/identity {} true))
   (k/is-function? (xt/x:get-key worker "postMessage")))
  => true

  (notify/wait-on :js
    (inner-mock/create-worker (repl/>notify) {} false))
  => {"body" {"done" true}
      "status" "ok"
      "op" "stream"
      "signal" "@cell/::INIT"})
