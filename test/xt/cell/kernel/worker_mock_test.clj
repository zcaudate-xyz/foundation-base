(ns xt.cell.kernel.worker-mock-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]
             [js.core :as j]
             [xt.cell.kernel.worker-mock :as worker-mock]]})

(fact:global
  {:setup    [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.cell.kernel.worker-mock/mock-worker-send :added "4.1"}
(fact "processes eval strings and request frames through the mock worker"
  ^:hidden

  (notify/wait-on :js
    (var worker
         (worker-mock/mock-worker
          (fn [event]
            (when (== (. event ["op"]) "eval")
              (repl/notify event)))))
    (worker-mock/mock-worker-send worker "1 + 1"))
  => (contains {"op" "eval"
                "status" "ok"})

  (notify/wait-on :js
    (var worker
          (worker-mock/create-worker
           (fn [event]
             (when (== (. event ["op"]) "call")
               (repl/notify event)))
           {} true))
    (worker-mock/mock-worker-send
     worker
     {"op" "call"
      "id" "ping-1"
      "action" "@worker/ping"
      "body" []}))
  => (contains {"op" "call"
                "id" "ping-1"
                "status" "ok"}))

^{:refer xt.cell.kernel.worker-mock/mock-worker :added "4.1"}
(fact "creates a worker-like transport with listener and request APIs"
  ^:hidden

  (!.js
   (var worker (worker-mock/mock-worker (fn:> [event] event)))
   [(k/get-key worker "::")
    (k/len (k/get-key worker "listeners"))
    (== nil (k/get-key worker "postMessage"))
    (== nil (k/get-key worker "postRequest"))])
  => ["worker.mock" 1 false false]

  (notify/wait-on :js
    (var worker (worker-mock/mock-worker (repl/>notify)))
    (. worker
       (postMessage {"op" "stream"
                     "signal" "hello"
                     "status" "ok"
                     "body" {"id" 1}})))
  => {"op" "stream"
      "signal" "hello"
      "status" "ok"
      "body" {"id" 1}})

^{:refer xt.cell.kernel.worker-mock/create-worker :added "4.1"}
(fact "initializes actions and optionally emits the init signal"
  ^:hidden

  (!.js
   (var worker
        (worker-mock/create-worker
         nil
         {"@custom/action" {:handler (fn [x] (return x))
                            :is_async false
                            :args ["x"]}}
         true))
   (xt/x:has-key? (. worker ["actions"]) "@custom/action"))
  => true

  (notify/wait-on :js
    (worker-mock/create-worker (repl/>notify) {} false))
  => {"op" "stream"
      "signal" "@worker/::INIT"
      "status" "ok"
      "body" {"done" true}})
