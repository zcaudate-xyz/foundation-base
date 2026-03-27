(ns js.cell.kernel.worker-fn-mock-test
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
             [js.cell.kernel.worker-fn :as base-fn]
             [js.cell.kernel.worker-impl :as base-internal]
             [js.cell.link-fn :as link-fn]
             [js.cell.link-raw :as link-raw]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.worker-fn/WORKER_STATE :adopt true :added "4.0" :unchecked true
  :setup [(base-fn/WORKER_STATE-reset {:eval true})]}
(fact "gets worker state"

  (base-fn/WORKER_STATE)
  => map?)

^{:refer js.cell.kernel.worker-fn/WORKER_ACTIONS :adopt true :added "4.0" :unchecked true}
(fact "gets worker actions"

  (base-fn/WORKER_ACTIONS)
  => map?)

^{:refer js.cell.kernel.worker-fn/fn-trigger :adopt true :added "4.0" :unchecked true}
(fact "triggers an event"
  ^:hidden
  
  (notify/wait-on :js
    (base-fn/fn-trigger
     (base-internal/new-mock (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}))
  => {"body" {"a" 1},
      "status" "ok",
      "op" "stream",
      "signal" "hello"})

^{:refer js.cell.kernel.worker-fn/fn-trigger-async :adopt true :added "4.0" :unchecked true}
(fact "triggers an event after a delay"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-trigger-async
     (base-internal/new-mock (repl/>notify))
     "stream"
     "hello"
     "ok"
     {:a 1}
     100))
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "signal" "hello"})

^{:refer js.cell.kernel.worker-fn/fn-set-state :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/WORKER_STATE)
                     {:final false}))]}
(fact "helper to set the state and emit event"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-set-state
     (base-internal/new-mock (repl/>notify))
     (base-fn/WORKER_STATE)
     (fn [])
     false))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@/::STATE"})

^{:refer js.cell.kernel.worker-fn/fn-final-set :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/WORKER_STATE)
                     {:final false}))]}
(fact "sets the worker state to final"
  ^:hidden
  
  (notify/wait-on :js
    (base-fn/fn-final-set
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" true, "final" true},
      "status" "ok",
      "op" "stream",
      "signal" "@/::STATE"})

^{:refer js.cell.kernel.worker-fn/fn-final-status :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/WORKER_STATE)
                     {:final false}))]}
(fact "gets the final status"
  ^:hidden

  (!.js (base-fn/fn-final-status))
  => false)

^{:refer js.cell.kernel.worker-fn/fn-eval-enable :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/WORKER_STATE)
                     {:eval true}))]}
(fact "enables eval"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-eval-enable
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" true, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@/::STATE"})

^{:refer js.cell.kernel.worker-fn/fn-eval-disable :adopt true :added "4.0" :unchecked true
  :setup [(!.js
           (j/assign (base-fn/WORKER_STATE)
                     {:eval true}))]}
(fact "disables eval"
  ^:hidden

  (notify/wait-on :js
    (base-fn/fn-eval-disable
     (base-internal/new-mock (repl/>notify))))
  => {"body" {"eval" false, "final" false},
      "status" "ok",
      "op" "stream",
      "signal" "@/::STATE"})

^{:refer js.cell.kernel.worker-fn/fn-eval-status :adopt true :added "4.0" :unchecked true}
(fact "gets the eval status"
  ^:hidden
  
  (base-fn/fn-eval-status)
  => boolean?)

^{:refer js.cell.kernel.worker-fn/fn-action-list :adopt true :added "4.0" :unchecked true}
(fact "gets the actions list"
  ^:hidden

  (base-fn/fn-action-list)
  => vector?)

^{:refer js.cell.kernel.worker-fn/fn-action-entry :adopt true :added "4.0" :unchecked true}
(fact  "gets a action entry"
  ^:hidden

  (base-fn/fn-action-entry "hello")
  => nil)

^{:refer js.cell.kernel.worker-fn/fn-ping :adopt true :added "4.0" :unchecked true}
(fact "pings the worker"
  ^:hidden
  
  (base-fn/fn-ping)
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-fn/fn-ping-async :adopt true :added "4.0" :unchecked true}
(fact "pings after a delay"
  ^:hidden

  (j/<! (base-fn/fn-ping-async 100))
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-fn/fn-echo :adopt true :added "4.0" :unchecked true}
(fact  "echos the first arg"
  ^:hidden
  
  (base-fn/fn-echo "hello")
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-fn/fn-echo-async :adopt true :added "4.0" :unchecked true}
(fact "echos the first arg after delay"
  ^:hidden

  (j/<! (base-fn/fn-echo-async "hello" 100))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-fn/fn-error :adopt true :added "4.0" :unchecked true}
(fact "throws an error"
  ^:hidden
  
  (base-fn/fn-error)
  => (throws))

^{:refer js.cell.kernel.worker-fn/fn-error-async :adopt true :added "4.0" :unchecked true}
(fact  "throws an error after delay"
  ^:hidden
  
  (j/<! (. (base-fn/fn-error-async)
           (catch k/identity)))
  => (contains ["error"]))


^{:refer js.cell.kernel.worker-fn/actions-base :adopt true :added "4.0" :unchecked true}
(fact "returns the base actions"
  ^:hidden
  
  (base-fn/actions-base)
  => map?)

^{:refer js.cell.kernel.worker-fn/actions-init :adopt true :added "4.0" :unchecked true}
(fact "initiates the base actions"
  ^:hidden
  
  (base-fn/actions-init {})
  => (contains [true]))


^{:refer js.cell.kernel.worker-impl/create-mock :adopt true :added "4.0" :unchecked true}
(fact "initiates the base actions"
  ^:hidden

  (notify/wait-on :js
    (base-internal/mock-init (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "signal" "@/::INIT"})

^{:refer js.cell.kernel.worker-impl/worker-process :adopt true :added "4.0" :unchecked true
  :setup [(l/rt:restart)]}
(fact "initiates the base actions"
  ^:hidden
  
  (notify/wait-on :js
    (base-internal/mock-init (repl/>notify)
                             {}))
  => {"body" {"done" true}, "status" "ok", "op" "stream", "signal" "@/::INIT"}

  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock "1+1"))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" nil,
      "status" "ok",
      "op" "eval"}
  
  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock {:op "eval"
                                   :id "A"
                                   :body "1+1"}))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" "A",
      "status" "ok",
      "op" "eval"}

  (notify/wait-on :js
    (var mock (base-internal/mock-init (repl/>notify)
                                       {}
                                       true))
    (base-internal/mock-send mock {:op "action"
                                   :id "id-action"
                                   :action "@/ping-async"
                                   :body [100]}))
  => (contains-in
      {"body" ["pong" integer?],
       "id" "id-action",
       "status" "ok",
       "op" "action"}))


^{:refer js.cell.link-raw/link-create-mock :adopt true :added "4.0" :unchecked true
  :setup [(l/rt:restart)]}
(fact "creates a mock link for testing purposes"

  (notify/wait-on :js
    (var link (link-raw/link-create
              {:create-fn
               (fn:> [listener]
                 (base-internal/mock-init
                  listener
                  {} true))}))
   (j/notify (link-fn/ping-async link 300)))
  => (contains ["pong" integer?]) )




(comment
  ^*(!.js
 (link-raw/link-create
  {:create-fn (fn:> [listener] (base-internal/mock-init listener {}))}))
  
  (l/rt:restart)
  
  (!.js
   (k/trace-log-clear)
   (:= (!:G LK)
       ))
  
  (j/<! (link-fn/echo LK "hello"))
  
  (j/<! )
  (!.js
   (. (link-fn/echo LK "hello")
      (catch k/identity)))
  
  (!.js
   LK)
  (k/trace-log)
  
  (do 
    (k/trace-log-clear)
    (f/suppress
     (notify/wait-on :js
       (var link )
       (j/notify (link-fn/ping link))))
    (k/trace-log))
  
  [{"tag" "s10l40aeuvky",
    "time" 1646974384441,
    "line" 78,
    "column" 3,
    "data"
    [{"body" [], "action" "@/error", "id" "8g6-oof", "op" "action"}
     {"8g6-oof"
      {"time" 1646974384440,
       "input"
       {"body" [],
        "action" "@/error",
        "id" "8g6-oof",
        "op" "action"}}}],
    "ns" "js.cell.link-raw"}]
  
  
  (link-fn/echo)

  )
