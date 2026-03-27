(ns js.cell.kernel.worker-state-test
  (:use code.test)
  (:require [js.cell.playground :as browser]
            [std.lang :as l]
            [std.lib.template :as template]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [xt.lang.util-throttle :as th]
             [js.core :as j]
             [js.cell.kernel.worker-state :as worker-state]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

(defmacro eval-worker
  [body & [timeout no-post]]
  (template/$ (notify/wait-on [:js ~(or timeout 1000)]
         (var worker (new Worker
                          (fn []
                            (eval (@! (browser/play-script
                                       '[~(if no-post
                                            body
                                            (list 'js.core/settle 'postMessage body))]
                                       true))))))
         (. worker (addEventListener
                    "message"
                    (fn [e]
                      (repl/notify e.data))
                    false)))))

^{:refer js.cell.kernel.worker-state/WORKER_STATE :added "4.0"}
(fact "gets worker state"

  (worker-state/WORKER_STATE)
  => map?)

^{:refer js.cell.kernel.worker-state/WORKER_ACTIONS :added "4.0"}
(fact "gets worker actions"

  (worker-state/WORKER_ACTIONS)
  => map?)

^{:refer js.cell.kernel.worker-state/get-state :added "4.0"}
(fact "gets cell state")

^{:refer js.cell.kernel.worker-state/get-actions :added "4.0"}
(fact "gets cell actions")

^{:refer js.cell.kernel.worker-state/set-actions :added "4.0"}
(fact "initiates the base actions"
  ^:hidden
  
  ;; Test setting actions on a worker
  (!.js
   (var worker {})
   (worker-state/set-actions {"@test/action" {}} worker)
   (k/get-key worker "actions"))
  => {"@test/action" {}}
  
  ;; Test resetting global actions
  (!.js
   (worker-state/set-actions {"@global/action" {}} nil)
   (k/has-key? (worker-state/WORKER_ACTIONS) "@global/action"))
  => true)

^{:refer js.cell.kernel.worker-state/fn-self :added "4.0"}
(fact "applies arguments along with `self`"
  ^:hidden
  
  (set 
   (eval-worker ((js.cell.kernel.worker-state/fn-self
                  xt.lang.base-lib/obj-keys))))
  => #{"onerror" "close" "postMessage" "addEventListener" "onmessage"})

^{:refer js.cell.kernel.worker-state/fn-trigger :added "4.0"}
(fact "triggers an event"
  ^:hidden
  
  (eval-worker (js.cell.kernel.worker-state/fn-trigger
                self
                "stream"
                "hello"
                "ok"
                {:a 1})
               nil true)
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "signal" "hello"})

^{:refer js.cell.kernel.worker-state/fn-trigger-async :added "4.0"}
(fact "triggers an event after a delay"
  ^:hidden
  
  (eval-worker (js.cell.kernel.worker-state/fn-trigger-async
                self
                "stream"
                "hello"
                "ok"
                {:a 1}
                100)
               nil true)
  => {"body" {"a" 1}, "status" "ok", "op" "stream", "signal" "hello"})

^{:refer js.cell.kernel.worker-state/fn-set-state :added "4.0"}
(fact "helper to set the state and emit event"
  ^:hidden

  (eval-worker (js.cell.kernel.worker-state/fn-set-state
                self
                (js.cell.kernel.worker-state/WORKER_STATE)
                (fn [])
                true))
  => {"eval" true}
  
  (eval-worker (js.cell.kernel.worker-state/fn-set-state
                self
                (js.cell.kernel.worker-state/WORKER_STATE)
                (fn []))
               nil true)
  => {"body" {"eval" true},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-set-final-status :added "4.0"}
(fact "sets the worker state to final"
  ^:hidden
  
  (eval-worker (js.cell.kernel.worker-state/fn-set-final-status
                self true))
  => {"eval" true, "final" true}
  
  (eval-worker (js.cell.kernel.worker-state/fn-set-final-status
                self)
               nil true)
  => {"body" {"eval" true, "final" true},
      "status" "ok",
      "op" "stream",
      "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-get-final-status :added "4.0"}
(fact "gets the final status"
  ^:hidden

  (eval-worker (js.cell.kernel.worker-state/fn-get-final-status
                self))
  => nil
  
  (eval-worker (do:> (js.cell.kernel.worker-state/fn-set-final-status
                      self true)
                     (return (js.cell.kernel.worker-state/fn-get-final-status
                              self))))
  => true)

^{:refer js.cell.kernel.worker-state/fn-set-eval-status :added "4.0"}
(fact "sets the eval status"
  ^:hidden
  
  (eval-worker (js.cell.kernel.worker-state/fn-set-eval-status
                self true true))
  => {"eval" true}
  
  (eval-worker (js.cell.kernel.worker-state/fn-set-eval-status
                self)
               nil true true)
  => {"body" {"eval" true}, "status" "ok", "op" "stream", "signal" "@worker/::STATE"})

^{:refer js.cell.kernel.worker-state/fn-get-eval-status :added "4.0"}
(fact "gets the eval status"
  ^:hidden
  
  (eval-worker (js.cell.kernel.worker-state/fn-get-eval-status
                self))
  => true
  
  (eval-worker (do:> (js.cell.kernel.worker-state/fn-set-eval-status
                      self false true)
                     (return (js.cell.kernel.worker-state/fn-get-eval-status
                              self))))
  => false)

^{:refer js.cell.kernel.worker-state/fn-get-action-list :added "4.0"}
(fact "gets the actions list"
  ^:hidden

  (worker-state/fn-get-action-list)
  => vector?
  
  (eval-worker (js.cell.kernel.worker-state/fn-get-action-list))
  => vector?)

^{:refer js.cell.kernel.worker-state/fn-get-action-entry :added "4.0"}
(fact  "gets a action entry"
  ^:hidden

  (worker-state/fn-get-action-entry "hello")
  => nil
  
  (eval-worker (js.cell.kernel.worker-state/fn-get-action-entry "hello"))
  => nil)

^{:refer js.cell.kernel.worker-state/fn-ping :added "4.0"}
(fact "pings the worker"
  ^:hidden
  
  (worker-state/fn-ping)
  => (contains ["pong" integer?])
  
  (eval-worker (js.cell.kernel.worker-state/fn-ping))
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-state/fn-ping-async :added "4.0"}
(fact "pings after a delay"
  ^:hidden

  (j/<! (worker-state/fn-ping-async 100))
  => (contains ["pong" integer?])
  
  (eval-worker (js.cell.kernel.worker-state/fn-ping-async 100))
  => (contains ["pong" integer?]))

^{:refer js.cell.kernel.worker-state/fn-echo :added "4.0"}
(fact  "echos the first arg"
  ^:hidden
  
  (worker-state/fn-echo "hello")
  => (contains ["hello" integer?])
  
  (eval-worker (js.cell.kernel.worker-state/fn-echo "hello"))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-state/fn-echo-async :added "4.0"}
(fact "echos the first arg after delay"
  ^:hidden

  (j/<! (worker-state/fn-echo-async "hello" 100))
  => (contains ["hello" integer?])
  
  (eval-worker (js.cell.kernel.worker-state/fn-echo-async "hello" 100))
  => (contains ["hello" integer?]))

^{:refer js.cell.kernel.worker-state/fn-error :added "4.0"}
(fact "throws an error"
  ^:hidden
  
  (worker-state/fn-error)
  => (throws)

  (eval-worker (js.cell.kernel.worker-state/fn-error))
  => :timeout)

^{:refer js.cell.kernel.worker-state/fn-error-async :added "4.0"}
(fact  "throws an error after delay"
  ^:hidden
  
  (j/<! (. (worker-state/fn-error-async)
           (catch k/identity)))
  => (contains ["error"])
  
  (eval-worker (js.cell.kernel.worker-state/fn-error)
               300)
  => :timeout)
