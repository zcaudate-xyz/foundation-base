(ns xt.cell.kernel.inner-impl-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-impl :as inner-impl]
             [xt.lang.common-repl :as repl]
             [js.core :as j]]})

(fact:global
 {:setup [(l/rt:restart)
                 (l/rt:scaffold-imports :js)]
 :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.inner-impl/worker-handle-async :added "4.0"}
(fact "handles async tasks"

  (notify/wait-on :js
    (var worker {:postMessage (fn [msg] (repl/notify msg))})
    (inner-impl/worker-handle-async
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

^{:refer xt.cell.kernel.inner-impl/worker-process-eval :added "4.0"}
(fact "processes eval requests"

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (inner-impl/worker-process-eval
    worker
    {:op "eval" :id "test-1" :body "1 + 1"}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "eval"
                "id" "test-1"
                "status" "ok"}))

^{:refer xt.cell.kernel.inner-impl/worker-process-action :added "4.0"}
(fact "processes action requests"

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))
                :actions {"@test/action" {:handler (fn [x] (return (+ x 10)))
                                          :is-async false
                                          :args ["x"]}}})
   (inner-impl/worker-process-action
    worker
    {:op "call" :id "test-2" :action "@test/action" :body [5]}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "call"
                "id" "test-2"
                "status" "ok"})

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))
                :actions {}})
   (inner-impl/worker-process-action
    worker
    {:op "call" :id "test-3" :action "@missing/action"}
    (fn [x] (return (worker.postMessage x))))
   (xtd/first messages))
  => (contains {"op" "call"
                "id" "test-3"
                "status" "error"}))

^{:refer xt.cell.kernel.inner-impl/worker-process :added "4.0"}
(fact "processes eval, call, and unknown operations"

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (inner-impl/worker-process
    worker
    {:op "eval" :id "test-1" :body "1 + 1"})
   (xtd/first messages))
  => (contains {"op" "eval"
                "status" "ok"})

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (inner-local/actions-init {} worker)
   (inner-impl/worker-process
    worker
    {:op "call" :id "test-2" :action "@cell/ping"})
   (xtd/first messages))
  => (contains {"op" "call"
                "status" "ok"})

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (inner-impl/worker-process
    worker
    {:op "unknown" :id "test-3"})
   (xtd/first messages))
  => (contains {"op" "unknown"
                "status" "error"}))

^{:refer xt.cell.kernel.inner-impl/worker-init :added "4.0"}
(fact "registers a worker message listener"

  (!.js
   (var worker {:listeners []
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (inner-impl/worker-init worker k/identity))
  => true

  (!.js
   (var worker {:listeners []
                :addEventListener (fn [event listener capture]
                                    (worker.listeners.push listener))})
   (inner-impl/worker-init worker k/identity)
   (xt/x:len worker.listeners))
  => 1)

^{:refer xt.cell.kernel.inner-impl/worker-init-signal :added "4.0"}
(fact "posts an init message"

  (!.js
   (var messages [])
   (var worker {:postMessage (fn [msg] (messages.push msg))})
   (inner-impl/worker-init-signal worker {:done true})
   (xtd/first messages))
  => (contains {"op" "stream"
                "signal" "@cell/::INIT"
                "body" {"done" true}}))