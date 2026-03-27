(ns js.cell.kernel.test-mock-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as repl]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.core :as j]
             [js.cell.kernel.worker-mock :as worker-mock]
             [js.cell.kernel.worker-local :as worker-local]
             [js.cell.kernel.base-link :as base-link]
             [js.cell.kernel.base-link-local :as base-link-local]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

;; Test 1: Direct mock worker with actions
^{:refer js.cell.kernel.test-mock-test/mock-worker-action :added "4.0"}
(fact "test mock worker with actions"
  (notify/wait-on :js
    ;; Initialize actions when creating worker
    (var mock (worker-mock/create-worker (repl/>notify)
                                       {}  ; Use default baseline actions
                                       true))
    (worker-mock/mock-worker-send mock {:op "call"
                                        :id "id-action"
                                        :action "@worker/ping"
                                        :body []}))
  => (contains-in
      {"body" ["pong" integer?],
       "id" "id-action",
       "status" "ok",
       "op" "call"}))

;; Test 2: Link with mock worker  
^{:refer js.cell.kernel.test-mock-test/link-mock :added "4.0"}
(fact "creates a mock link for testing"
  (notify/wait-on :js
    (var link (base-link/link-create
              {:create-fn
               (fn:> [listener]
                 (worker-mock/create-worker
                  listener
                  {}  ; Use default baseline actions
                  true))}))
    (. (base-link-local/ping link)
       (then (repl/>notify))))
  => (contains ["pong" integer?]))
