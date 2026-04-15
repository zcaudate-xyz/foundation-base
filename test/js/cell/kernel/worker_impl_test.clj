(ns js.cell.kernel.worker-impl-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-spec :as xt]
              [xt.lang.common-data :as xtd]
               [xt.lang.common-repl :as repl]
               [js.core :as j]
               [js.cell.kernel.worker-local :as worker-local]
               [js.cell.kernel.worker-impl :as worker-impl]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.worker-impl/worker-handle-async :added "4.0" :unchecked true}
(fact "handles async tasks"
  ^:hidden
  
  (notify/wait-on :js
    (var worker {:postMessage (fn [msg] (repl/notify msg))})
    (worker-impl/worker-handle-async
     worker
     (fn [ms]
       (return (j/future-delayed [ms]
                 (return ["pong" 1]))))
     "call"
     "async-1"
     [10]))
  => (contains {"op" "call"
                "id" "async-1"
                "status" "ok"
                "body" ["pong" 1]}))

^{:refer js.cell.kernel.worker-impl/worker-process :added "4.0" :unchecked true}
(fact "processes various types of actions"
  ^:hidden
  
  ;; Test eval operation
  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-impl/worker-process
    worker
    {:op "eval" :id "test-1" :body "1 + 1"})
   (xtd/first messages))
  => (contains {"op" "eval"
                "status" "ok"})

  ;; Test call operation (returns ok because it calls worker-process-eval)
  (!.js
   (var messages [])
    (var worker {:postMessage (fn [msg] (messages.push msg))})
    (worker-local/actions-init {} worker)
   (worker-impl/worker-process
     worker
     {:op "call" :id "test-2" :action "@worker/ping"})
    (xtd/first messages))
  => (contains {"op" "call"
                "status" "ok"})

  ;; Test unknown op returns error
  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-impl/worker-process
    worker
    {:op "unknown" :id "test-3"})
   (xtd/first messages))
  => (contains {"op" "unknown"
                "status" "error"}))

^{:refer js.cell.kernel.worker-impl/worker-init :added "4.0" :unchecked true}
(fact "initiates the worker actions"
  ^:hidden
  
  (!.js
   (var worker {:listeners []
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (worker-impl/worker-init worker k/identity))
  => true
  
  ;; Check that listener was added
  (!.js
   (var worker {:listeners []
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (worker-impl/worker-init worker k/identity)
   (xt/x:len worker.listeners))
  => 1)

^{:refer js.cell.kernel.worker-impl/worker-process-eval :added "4.0" :unchecked true}
(fact "processes eval requests"
  ^:hidden
  
  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-impl/worker-process-eval
    worker
    {:op "eval" :id "test-1" :body "1 + 1"}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "eval"
                "id" "test-1"
                "status" "ok"}))

^{:refer js.cell.kernel.worker-impl/worker-process-action :added "4.0" :unchecked true}
(fact "processes action requests"
  ^:hidden
  
  ;; Test with a registered action
  (!.js
   (var messages [])
    (var worker {:postMessage (fn [msg] (messages.push msg))
                 :actions {"@test/action" {:handler (fn [x] (return (+ x 10)))
                                           :is-async false
                                            :args ["x"]}}})
   (worker-impl/worker-process-action
    worker
    {:op "call" :id "test-2" :action "@test/action" :body [5]}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "call"
                "id" "test-2"
                "status" "ok"})
  
  ;; Test with missing action
  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))
                :actions {}})
   (worker-impl/worker-process-action
    worker
    {:op "call" :id "test-3" :action "@missing/action"}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "call"
                "id" "test-3"
                "status" "error"}))

^{:refer js.cell.kernel.worker-impl/worker-init-signal :added "4.0" :unchecked true}
(fact "posts an init message"
  ^:hidden
  
  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (worker-impl/worker-init-signal worker {:done true})
   (xtd/first messages))
  => (contains {"op" "stream"
                "signal" "@worker/::INIT"
                "body" {"done" true}}))
