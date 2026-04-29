(ns xt.cell.kernel.inner-state-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [xt.cell.kernel.inner-state :as inner-state]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-mock :as inner-mock]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defn.js reset-inner-state
  []
  (inner-state/WORKER_STATE-reset {"eval" true})
  (inner-state/WORKER_ACTIONS-reset {})
  (return true))

^{:refer xt.cell.kernel.inner-state/WORKER_STATE :added "4.0"}
(fact "gets worker state"

  (!.js
   (-/reset-inner-state)
   (inner-state/WORKER_STATE))
  => {"eval" true})

^{:refer xt.cell.kernel.inner-state/WORKER_ACTIONS :added "4.0"}
(fact "gets worker actions"

  (!.js
   (-/reset-inner-state)
   (inner-state/WORKER_ACTIONS))
  => {})

^{:refer xt.cell.kernel.inner-state/get-state :added "4.0"}
(fact "gets cell state"

  (!.js
   (-/reset-inner-state)
   (inner-state/get-state nil))
  => {"eval" true}

  (!.js
   (-/reset-inner-state)
   (inner-state/get-state {}))
  => {"eval" true})

^{:refer xt.cell.kernel.inner-state/get-actions :added "4.0"}
(fact "gets worker actions from the worker or singleton"

  (!.js
   (-/reset-inner-state)
   (inner-state/get-actions nil))
  => {}

  (!.js
   (inner-state/get-actions {"actions" {"@test/action" {}}}))
  => {"@test/action" {}})

^{:refer xt.cell.kernel.inner-state/set-actions :added "4.0"}
(fact "sets actions on a worker or in global state"

  (!.js
   (-/reset-inner-state)
   (var worker {})
   (inner-state/set-actions {"@test/action" {}} worker)
   (xt/x:get-key worker "actions"))
  => {"@test/action" {}}

  (!.js
   (-/reset-inner-state)
   (inner-state/set-actions {"@global/action" {}} nil)
   (xt/x:has-key? (inner-state/WORKER_ACTIONS) "@global/action"))
  => true)

^{:refer xt.cell.kernel.inner-state/fn-bind :added "4.0"}
(fact "binds a worker instance into a handler"

  (!.js
   ((inner-state/fn-bind {"id" "w1"}
                          (fn [worker x]
                            (return [(. worker ["id"]) x])))
    "hello"))
  => ["w1" "hello"])

^{:refer xt.cell.kernel.inner-state/fn-trigger :added "4.0"}
(fact "triggers an event on a worker"

  (notify/wait-on :js
    (inner-state/fn-trigger
     (inner-mock/mock-worker (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}))
  => {"body" {"a" 1}
      "status" "ok"
      "op" "stream"
      "signal" "hello"})

^{:refer xt.cell.kernel.inner-state/fn-trigger-async :added "4.0"}
(fact "triggers an event after a delay"

  (notify/wait-on :js
    (inner-state/fn-trigger-async
     (inner-mock/mock-worker (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}
     50))
  => {"body" {"a" 1}
      "status" "ok"
      "op" "stream"
      "signal" "hello"})

^{:refer xt.cell.kernel.inner-state/fn-set-state :added "4.0"}
(fact "updates worker state and can emit state events"

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-set-state
    (inner-mock/mock-worker (fn [msg]))
    (inner-state/WORKER_STATE)
    (fn [state]
      (xt/x:set-key state "final" false))
    true))
  => {"eval" true "final" false}

  (!.js
   (-/reset-inner-state)
   (var messages [])
   (inner-state/fn-set-state
    (inner-mock/mock-worker (fn [msg] (messages.push msg)))
    (inner-state/WORKER_STATE)
    (fn [state]
      (xt/x:set-key state "final" false))
    false)
   (xtd/first messages))
  => {"body" {"eval" true "final" false}
      "status" "ok"
      "op" "stream"
      "signal" "@worker/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-set-final-status :added "4.0"}
(fact "sets the worker state to final"

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-set-final-status
    (inner-mock/mock-worker (fn [msg]))
    true))
  => {"eval" true "final" true}

  (!.js
   (-/reset-inner-state)
   (var messages [])
   (inner-state/fn-set-final-status
    (inner-mock/mock-worker (fn [msg] (messages.push msg)))
    false)
   (xtd/first messages))
  => {"body" {"eval" true "final" true}
      "status" "ok"
      "op" "stream"
      "signal" "@worker/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-get-final-status :added "4.0"}
(fact "gets the final status"

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-get-final-status nil))
  => nil

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-set-final-status
    (inner-mock/mock-worker (fn [msg]))
    true)
   (inner-state/fn-get-final-status nil))
  => true)

^{:refer xt.cell.kernel.inner-state/fn-set-eval-status :added "4.0"}
(fact "sets the eval status"

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-set-eval-status
    (inner-mock/mock-worker (fn [msg]))
    false
    true))
  => {"eval" false}

  (!.js
   (-/reset-inner-state)
   (var messages [])
   (inner-state/fn-set-eval-status
    (inner-mock/mock-worker (fn [msg] (messages.push msg)))
    false
    false)
   (xtd/first messages))
  => {"body" {"eval" false}
      "status" "ok"
      "op" "stream"
      "signal" "@worker/::STATE"})

^{:refer xt.cell.kernel.inner-state/fn-get-eval-status :added "4.0"}
(fact "gets the eval status"

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-get-eval-status))
  => true

  (!.js
   (-/reset-inner-state)
   (inner-state/fn-set-eval-status
    (inner-mock/mock-worker (fn [msg]))
    false
    true)
   (inner-state/fn-get-eval-status))
  => false)

^{:refer xt.cell.kernel.inner-state/fn-get-action-list :added "4.0"}
(fact "lists registered actions"

  (!.js
   (-/reset-inner-state)
   (inner-local/actions-init {} nil)
   (xt/x:len (inner-state/fn-get-action-list)))
  => integer?)

^{:refer xt.cell.kernel.inner-state/fn-get-action-entry :added "4.0"}
(fact "gets a registered action entry"

  (!.js
   (-/reset-inner-state)
   (inner-local/actions-init {} nil)
   (inner-state/fn-get-action-entry "@worker/ping"))
  => (contains {"is_async" false "args" []}))

^{:refer xt.cell.kernel.inner-state/fn-ping :added "4.0"}
(fact "pings the worker"

  (!.js
   (inner-state/fn-ping))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-ping-async :added "4.0"}
(fact "pings after a delay"

  (j/<! (inner-state/fn-ping-async 10))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-echo :added "4.0"}
(fact "echos the first arg"

  (!.js
   (inner-state/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-echo-async :added "4.0"}
(fact "echos after a delay"

  (j/<! (inner-state/fn-echo-async "hello" 10))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.inner-state/fn-error :added "4.0"}
(fact "throws an error"

  (!.js
   (inner-state/fn-error))
  => (throws))

^{:refer xt.cell.kernel.inner-state/fn-error-async :added "4.0"}
(fact "throws an async error"

  (j/<! (. (inner-state/fn-error-async 10)
           (catch j/identity)))
  => (contains ["error"]))