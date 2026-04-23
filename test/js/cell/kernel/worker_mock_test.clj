(ns js.cell.kernel.worker-mock-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [js.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.worker-mock/mock-worker :added "4.0" :unchecked true}
(fact "creates a new mock worker"

  (!.js
   (var messages [])
   (var worker (worker-mock/mock-worker
                (fn [msg] (messages.push msg))))
   ;; postMessage pushes to listeners but returns undefined
   (worker.postMessage {"test" 1})
   ;; Return the messages array to verify
   messages)
  => [{"test" 1}])

^{:refer js.cell.kernel.worker-mock/mock-worker-send :added "4.0" :unchecked true}
(fact "sends a request to the mock worker"

  (!.js
   (var worker (worker-mock/mock-worker (fn [msg])))
   ;; Returns undefined because no actions are registered
   (worker-mock/mock-worker-send worker {"op" "eval" "body" "1 + 1"}))
  => nil?)

^{:refer js.cell.kernel.worker-mock/create-worker :added "4.0" :unchecked true}
(fact "initialises the mock worker"

  (!.js
   (var messages [])
   (var worker (worker-mock/create-worker
                (fn [msg] (messages.push msg))
                {}
                true))
   (xt/x:get-key worker "::"))
  => "worker.mock"

  ;; Check that worker has postMessage function
  (!.js
   (var worker (worker-mock/create-worker k/identity {} true))
   (k/is-function? (xt/x:get-key worker "postMessage")))
  => true)
