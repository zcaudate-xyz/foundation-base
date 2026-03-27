(ns js.cell.kernel.worker-state-mock-test
  (:require [std.lang :as l]
            [std.lib.foundation :as f]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [xt.lang.util-throttle :as th]
             [js.core :as j]
             [js.cell.kernel.worker-state :as worker-state]
             [js.cell.kernel.worker-local :as worker-local]
             [js.cell.kernel.worker-mock :as worker-mock]
             [js.cell.kernel.base-link-local :as base-link-local]
             [js.cell.kernel.base-link :as base-link]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.worker-state/WORKER_STATE :adopt true :added "4.0"
  :setup [(worker-state/WORKER_STATE-reset {:eval true})]}
(fact "gets worker state"

  (worker-state/WORKER_STATE)
  => map?)

^{:refer js.cell.kernel.worker-state/WORKER_ACTIONS :adopt true :added "4.0"}
(fact "gets worker actions"

  (worker-state/WORKER_ACTIONS)
  => map?)

^{:refer js.cell.kernel.worker-state/fn-trigger :adopt true :added "4.0"}
(fact "triggers an event"
  ^:hidden
  
  (notify/wait-on :js
    (worker-state/fn-trigger
     (worker-mock/mock-worker (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}))
  => {"body" {"a" 1},
      "status" "ok",
      "op" "stream",
      "signal" "hello"})

^{:refer js.cell.kernel.worker-state/fn-trigger-async :adopt true :added "4.0"}
(fact "triggers an event after a delay"
  ^:hidden

  (notify/wait-on :js
    (worker-state/fn-trigger-async
     (worker-mock/mock-worker (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}
     100))
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "signal" "hello"})

^{:refer js.cell.kernel.worker-state/fn-set-state :adopt true :added "4.0"
  :setup [(!.js
           (j/assign (worker-state/WORKER_STATE)
                     {:final false}))]}
(fact "helper to set the state and emit event"
  ^:hidden

  (notify/wait-on :js
    (worker-state/fn-set-state
     (worker-mock/mock-worker (repl/>notify))
     (worker-state/WORKER_STATE)
     (fn [])
     false))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-final-set :adopt true :added "4.0"
  :setup [(!.js
           (j/assign (worker-state/WORKER_STATE)
                     {:final false}))]}
(fact "sets the worker state to final"
  ^:hidden
  
  (notify/wait-on :js
    (worker-state/fn-set-final-status
     (worker-mock/mock-worker (repl/>notify))))
  => {"body" {"eval" true, "final" true},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-final-status :adopt true :added "4.0"
  :setup [(!.js
           (j/assign (worker-state/WORKER_STATE)
                     {:final false}))]}
(fact "gets the final status"
  ^:hidden

  (!.js (worker-state/fn-get-final-status))
  => false)

^{:refer js.cell.kernel.worker-state/fn-eval-enable :adopt true :added "4.0"
  :setup [(!.js
           (j/assign (worker-state/WORKER_STATE)
                     {:eval true}))]}
(fact "enables eval"
  ^:hidden

  (notify/wait-on :js
    (worker-state/fn-set-eval-status
     (worker-mock/mock-worker (repl/>notify))
     true))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-eval-disable :adopt true :added "4.0"
  :setup [(!.js
           (j/assign (worker-state/WORKER_STATE)
                     {:eval true}))]}
(fact "disables eval"
  ^:hidden

  (notify/wait-on :js
    (worker-state/fn-set-eval-status
     (worker-mock/mock-worker (repl/>notify))
     false))
  => {"body" {"eval" false, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-eval-status :adopt true :added "4.0"}
(fact "gets the eval status"
  ^:hidden
  
  (worker-state/fn-get-eval-status)
  => boolean?)

^{:refer js.cell.kernel.worker-state/fn-action-list :adopt true :added "4.0"}
(fact "gets the actions list"
  ^:hidden

  (worker-state/fn-get-action-list)
  => vector?)

^{:refer js.cell.kernel.worker-state/fn-action-entry :adopt true :added "4.0"}
(fact  "gets a action entry"
  ^:hidden

  (worker-state/fn-get-action-entry "hello")
  => nil)

^{:refer js.cell.kernel.worker-state/fn-ping :adopt true :added "4.0"}
(fact "pings the worker"
  ^:hidden
  
  (worker-state/fn-ping)
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-state/fn-ping-async :adopt true :added "4.0"}
(fact "pings after a delay"
  ^:hidden

  (j/<! (worker-state/fn-ping-async 100))
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-state/fn-echo :adopt true :added "4.0"}
(fact  "echos the first arg"
  ^:hidden
  
  (worker-state/fn-echo "hello")
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-state/fn-echo-async :adopt true :added "4.0"}
(fact "echos the first arg after delay"
  ^:hidden

  (j/<! (worker-state/fn-echo-async "hello" 100))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-state/fn-error :adopt true :added "4.0"}
(fact "throws an error"
  ^:hidden
  
  (worker-state/fn-error)
  => (throws))

^{:refer js.cell.kernel.worker-state/fn-error-async :adopt true :added "4.0"}
(fact  "throws an error after delay"
  ^:hidden
  
  (j/<! (. (worker-state/fn-error-async)
           (catch k/identity)))
  => (contains ["error"]))


^{:refer js.cell.kernel.worker-state/actions-base :adopt true :added "4.0"}
(fact "returns the base actions"
  ^:hidden
  
  (worker-local/actions-baseline)
  => map?)

^{:refer js.cell.kernel.worker-local/actions-init :adopt true :added "4.0"}
(fact "initiates the base actions"
  ^:hidden
  
  (worker-local/actions-init {})
  => (contains [true]))


^{:refer js.cell.kernel.worker-mock/create-mock :adopt true :added "4.0"}
(fact "initiates the base actions"
  ^:hidden

  (notify/wait-on :js
    (worker-mock/create-worker (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "signal" "@worker/::INIT"})

^{:refer js.cell.kernel.worker-mock/worker-process :adopt true :added "4.0"
  :setup [(l/rt:restart)]}
(fact "initiates the base actions"
  ^:hidden
  
  (notify/wait-on :js
    (worker-mock/create-worker (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "signal" "@worker/::INIT"}

  (notify/wait-on :js
    (var mock (worker-mock/create-worker (repl/>notify)
                                       {}
                                       true))
    (worker-mock/mock-worker-send mock "1+1"))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" nil,
      "status" "ok",
      "op" "eval"}
  
  (notify/wait-on :js
    (var mock (worker-mock/create-worker (repl/>notify)
                                       {}
                                       true))
    (worker-mock/mock-worker-send mock {:op "eval"
                                   :id "A"
                                   :body "1+1"}))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" "A",
      "status" "ok",
      "op" "eval"}
  
  (notify/wait-on :js
    (var mock (worker-mock/create-worker (repl/>notify)
                                       {}
                                       true))
    (worker-mock/mock-worker-send mock {:op "call"
                                        :id "id-action"
                                        :action "@worker/ping.async"
                                        :body [100]}))
  
  => (contains-in
      {"body" ["pong" integer?],
       "id" "id-action",
       "status" "ok",
       "op" "call"}))


^{:refer js.cell.base-link/link-create-mock :adopt true :added "4.0"
  :setup [(l/rt:restart)]}
(fact "creates a mock link for testing purposes"

  (notify/wait-on :js
    (var link (base-link/link-create
              {:create-fn
               (fn:> [listener]
                 (worker-mock/create-worker
                  listener
                  {} true))}))
    (j/notify (base-link-local/ping-async link 300)))
  => (contains ["pong" integer?]) )





