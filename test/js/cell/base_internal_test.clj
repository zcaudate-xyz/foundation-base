(ns js.cell.base-internal-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [js.cell.playground :as browser]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-runtime :as rt]
             [js.cell.base-internal :as internal]
             [js.cell.base-fn :as base-fn]
             [js.cell.link-raw :as link-raw]
             [js.cell.link-fn :as link-fn]
             [js.core :as j]]
   :import [["tiny-worker" :as Worker]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.base-internal/CANARY :adopt true :added "4.0" :unchecked true}
(fact "preliminary check"
  ^:hidden
  
  (notify/wait-on :js
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (. self (postMessage e.data)))
                                     false)]
                                  true))))))
    (. worker (addEventListener
               "message"
               (fn [e]
                 (repl/notify e.data))
               false))
    (. worker (postMessage "hello")))
  => "hello"

  (notify/wait-on :js
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (. self (postMessage ((eval e.data) "hello"))))
                                     false)]
                                  true))))))
    (. worker (addEventListener
               "message"
               (fn [e]
                 (repl/notify e.data))
               false))
    (. worker (postMessage (+ "(" k/identity ")"))))
  => "hello"

  (notify/wait-on :js
    (var l (link-raw/link-create
            (fn []
              (eval (@! (browser/play-worker true))))))
    (j/notify (link-fn/ping l)))
  => (contains ["pong"])
  
  (comment ;; FOR BROWSER
    (notify/wait-on :js
      (var l (link-raw/link-create
              
              (+ "data:text/javascript;base64,"
                 (btoa (@! (browser/play-worker true))))))
      (j/notify (worker/ping l)))
    => (contains ["pong"])))

^{:refer js.cell.base-internal/worker-handle-async :added "4.0" :unchecked true}
(fact "worker function for handling async tasks"
  ^:hidden
  
  (notify/wait-on :js
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (postMessage e.data))
                                     false)]
                                  true))))))
    (. worker (addEventListener
          "message"
          (fn [e]
            (repl/notify e.data))
          false))
    (internal/worker-handle-async
     worker (fn:> [] (j/future-delayed [100]
                  (return "hello")))
     "route"
     "id-hello"
     []))
  => {"body" "hello", "id" "id-hello", "status" "ok", "op" "route"})

^{:refer js.cell.base-internal/worker-process :added "4.0" :unchecked true}
(fact "processes various types of routes"
  ^:hidden
  
  (notify/wait-on :js
    (base-fn/routes-init {})
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (postMessage e.data))
                                     false)]
                                  true))))))
    (. worker (addEventListener
          "message"
          (fn [e]
            (repl/notify e.data))
          false))
    (internal/worker-process worker {:op "eval"
                                   :id "id-eval"
                                   :body "1+1"}))
  => {"op" "eval"
      "id" "id-eval",
      "status" "ok"
      "body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",}  

  (notify/wait-on :js
    (base-fn/routes-init {})
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (postMessage e.data))
                                     false)]
                                  true))))))
    (. worker (addEventListener
          "message"
          (fn [e]
            (repl/notify e.data))
          false))
    (internal/worker-process worker {:op "route"
                                     :id "id-route"
                                     :route "@/ping"
                                     :body []}))
  => (contains-in {"body" ["pong" integer?], "id" "id-route", "status" "ok", "op" "route"})

  (notify/wait-on :js
    (base-fn/routes-init {})
    (var worker (new Worker
                     (fn []
                       (eval (@! (browser/play-script
                                  '[(addEventListener
                                     "message"
                                     (fn [e]
                                       (postMessage e.data))
                                     false)]
                                  true))))))
    (. worker (addEventListener
               "message"
               (fn [e]
                 (j/notify e.data))
               false))
    (internal/worker-process worker {:op "route"
                                     :id "id-route"
                                     :route "@/ping-async"
                                     :body [100]}))
  => (contains-in {"body" ["pong" integer?], "id" "id-route", "status" "ok", "op" "route"}))

^{:refer js.cell.base-internal/worker-init :added "4.0" :unchecked true}
(fact "initiates the worker routes"
  ^:hidden
  
  (!.js
   (var worker (new Worker
                    (fn []
                      (eval (@! (browser/play-script
                                 '[(addEventListener
                                    "message"
                                    (fn [e]
                                      (postMessage e.data))
                                    false)]
                                 true))))))
   (internal/worker-init worker))
  => true)

^{:refer js.cell.base-internal/worker-init-post :added "4.0" :unchecked true}
(fact "posts an init message")

^{:refer js.cell.base-internal/mock-send :added "4.0" :unchecked true}
(fact "sends a request to the mock worker"
  ^:hidden
  
  (notify/wait-on :js
    (var mock (internal/mock-init (repl/>notify)
                                  {}
                                  true))
    (internal/mock-send mock "1+1"))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" nil,
      "status" "ok",
      "op" "eval"})

^{:refer js.cell.base-internal/new-mock :added "4.0" :unchecked true}
(fact "creates a new mock worker"

  (!.js
   (internal/new-mock k/identity))
  => {"::" "worker.mock", "listeners" [nil]})

^{:refer js.cell.base-internal/mock-init :added "4.0" :unchecked true}
(fact "initialises the mock worker")
