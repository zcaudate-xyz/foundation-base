(ns xt.cell.kernel.worker-state-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [xt.cell.kernel.worker-state :as worker-state]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer xt.cell.kernel.worker-state/WORKER_STATE :added "4.1"}
(fact "gets worker state"

  (!.js (worker-state/WORKER_STATE))
  => map?)

^{:refer xt.cell.kernel.worker-state/WORKER_ACTIONS :added "4.1"}
(fact "gets worker actions"

  (!.js (worker-state/WORKER_ACTIONS))
  => map?)

^{:refer xt.cell.kernel.worker-state/get-state :added "4.1"}
(fact "gets cell state"
  ^:hidden

  (!.js (worker-state/get-state nil))
  => {"eval" true}

  (!.js (worker-state/get-state {}))
  => {"eval" true})

^{:refer xt.cell.kernel.worker-state/get-actions :added "4.1"}
(fact "gets cell actions"
  ^:hidden

  (!.js (worker-state/get-actions nil))
  => map?

  (!.js (worker-state/get-actions {"actions" {"@test/action" {}}}))
  => {"@test/action" {}})

^{:refer xt.cell.kernel.worker-state/set-actions :added "4.1"}
(fact "initiates the base actions"
  ^:hidden

  (!.js
   (var worker {})
   (worker-state/set-actions {"@test/action" {}} worker)
   (xt/x:get-key worker "actions"))
  => {"@test/action" {}}

  (!.js
   (worker-state/set-actions {"@global/action" {}} nil)
   (xt/x:has-key? (worker-state/WORKER_ACTIONS) "@global/action"))
  => true)

^{:refer xt.cell.kernel.worker-state/post-message :added "4.1"}
(fact "posts a message through a worker-like transport"
  ^:hidden

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-state/post-message worker {:op "stream" :signal "test"})
   (k/first messages))
  => {"op" "stream", "signal" "test"})

^{:refer xt.cell.kernel.worker-state/fn-self :added "4.1"}
(fact "applies arguments along with `self`"
  ^:hidden

  (!.js
   (k/is-function? (worker-state/fn-self k/identity)))
  => true)

^{:refer xt.cell.kernel.worker-state/fn-trigger :added "4.1"}
(fact "triggers an event"
  ^:hidden

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-state/fn-trigger worker "stream" "hello" "ok" {:a 1})
   (k/first messages))
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "signal" "hello"})

^{:refer xt.cell.kernel.worker-state/fn-trigger-async :added "4.1"}
(fact "triggers an event after a delay"
  ^:hidden

  (!.js
   (var worker {:postMessage (fn [msg] msg)})
   (k/is-object? (worker-state/fn-trigger-async worker "stream" "hello" "ok" {:a 1} 50)))
  => true)

^{:refer xt.cell.kernel.worker-state/fn-set-state :added "4.1"}
(fact "helper to set the state and emit event"
  ^:hidden

  (!.js
   (worker-state/fn-set-state {} (worker-state/WORKER_STATE) (fn []) true))
  => {"eval" true}

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-state/fn-set-state worker (worker-state/WORKER_STATE) (fn []))
   (k/first messages))
  => {"body" {"eval" true},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer xt.cell.kernel.worker-state/fn-set-final-status :added "4.1"
  :setup [(fact:global :setup)]}
(fact "sets the worker state to final"
  ^:hidden

  (!.js
   (worker-state/fn-set-final-status {} true))
  => (contains {"eval" true, "final" true}))

^{:refer xt.cell.kernel.worker-state/fn-get-final-status :added "4.1"
  :setup [(fact:global :setup)]}
(fact "gets the final status"
  ^:hidden

  (!.js
   (worker-state/fn-get-final-status {}))
  => nil

  (!.js
   (worker-state/fn-set-final-status {} true)
   (worker-state/fn-get-final-status (worker-state/WORKER_STATE)))
  => true)

^{:refer xt.cell.kernel.worker-state/fn-set-eval-status :added "4.1"
  :setup [(fact:global :setup)]}
(fact "enables eval"
  ^:hidden

  (!.js
   (worker-state/fn-set-eval-status {} true true))
  => {"eval" true}

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-state/fn-set-eval-status worker false)
   (k/first messages))
  => {"body" {"eval" false},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer xt.cell.kernel.worker-state/fn-get-eval-status :added "4.1"
  :setup [(fact:global :setup)]}
(fact "gets the eval status"
  ^:hidden

  (!.js
   (worker-state/fn-get-eval-status))
  => true

  (!.js
   (var worker {})
   (worker-state/fn-set-eval-status worker false true)
   (worker-state/fn-get-eval-status))
  => false)

^{:refer xt.cell.kernel.worker-state/fn-get-action-list :added "4.1"}
(fact "gets the actions list"
  ^:hidden

  (!.js
   (worker-state/fn-get-action-list))
  => vector?)

^{:refer xt.cell.kernel.worker-state/fn-get-action-entry :added "4.1"}
(fact "gets a action entry"
  ^:hidden

  (!.js
   (worker-state/fn-get-action-entry "hello"))
  => nil)

^{:refer xt.cell.kernel.worker-state/fn-ping :added "4.1"}
(fact "pings the worker"
  ^:hidden

  (!.js
   (worker-state/fn-ping))
  => (contains ["pong" integer?]))

^{:refer xt.cell.kernel.worker-state/fn-ping-async :added "4.1"}
(fact "pings after a delay"
  ^:hidden

  (!.js
   (k/is-object? (worker-state/fn-ping-async 50)))
  => true)

^{:refer xt.cell.kernel.worker-state/fn-echo :added "4.1"}
(fact "echos the first arg"
  ^:hidden

  (!.js
   (worker-state/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer xt.cell.kernel.worker-state/fn-echo-async :added "4.1"}
(fact "echos the first arg after delay"
  ^:hidden

  (!.js
   (k/is-object? (worker-state/fn-echo-async "hello" 50)))
  => true)

^{:refer xt.cell.kernel.worker-state/fn-error :added "4.1"}
(fact "throws an error"
  ^:hidden

  (!.js
   (worker-state/fn-error))
  => (throws))

^{:refer xt.cell.kernel.worker-state/fn-error-async :added "4.1"}
(fact "throws an error after delay"
  ^:hidden

  (!.js
   (k/is-object? (worker-state/fn-error-async 50)))
  => true)