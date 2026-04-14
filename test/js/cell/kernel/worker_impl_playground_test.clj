(ns js.cell.kernel.worker-impl-playground-test
  (:require [js.cell.playground :as browser]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(def ^:private +tiny-worker-path+
  (str (System/getProperty "user.dir") "/node_modules/tiny-worker/lib/index.js"))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
              [xt.lang.common-repl :as repl]
              [xt.lang.common-runtime :as rt]
              [js.cell.kernel.worker-impl :as worker-impl]
              [js.cell.kernel.worker-local :as worker-local]
              [js.cell.kernel.worker-mock :as worker-mock]
              [js.cell.kernel.base-link :as base-link]
              [js.cell.kernel.base-link-local :as base-link-local]
              [js.core :as j]]})

(fact:global
 {:setup     [(l/rt:restart)
              (l/rt:scaffold-imports :js)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell.kernel.worker-impl/CANARY :adopt true :added "4.0"}
(fact "preliminary check"
  ^:hidden
  
  (notify/wait-on :js
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (var l (base-link/link-create
            {:create-fn
             (fn [listener]
               (var Worker (require (@! +tiny-worker-path+)))
               (var worker (new Worker
                                (fn []
                                  (eval (@! (browser/play-worker true))))))
               (. worker (addEventListener
                          "message"
                          (fn [e]
                            (listener e.data))
                          false))
               (return worker))}))
    (j/notify (base-link-local/ping l)))
  => (contains ["pong"])
  
  (comment ;; FOR BROWSER
    (notify/wait-on :js
      (var l (base-link/link-create
              
              (+ "data:text/javascript;base64,"
                 (btoa (@! (browser/play-worker true))))))
      (j/notify (worker/ping l)))
    => (contains ["pong"])))

^{:refer js.cell.kernel.worker-impl/worker-handle-async :added "4.0"}
(fact "worker function for handling async tasks"
  ^:hidden
  
  (notify/wait-on :js
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (worker-impl/worker-handle-async
     worker (fn:> [] (j/future-delayed [100]
                   (return "hello")))
     "action"
     "id-hello"
     []))
  => {"body" "hello", "id" "id-hello", "status" "ok", "op" "action"})

^{:refer js.cell.kernel.worker-impl/worker-process :added "4.0"}
(fact "processes various types of actions"
  ^:hidden
  
  (notify/wait-on :js
    (worker-local/actions-init {})
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (worker-impl/worker-process worker {:op "eval"
                                   :id "id-eval"
                                   :body "1+1"}))
  => {"op" "eval"
      "id" "id-eval",
      "status" "ok"
      "body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",}  

  (notify/wait-on :js
    (worker-local/actions-init {})
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (worker-impl/worker-process worker {:op "call"
                                     :id "id-action"
                                     :action "@worker/ping"
                                     :body []}))
  => (contains-in {"body" ["pong" integer?], "id" "id-action", "status" "ok", "op" "call"})

  (notify/wait-on :js
    (worker-local/actions-init {})
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (worker-impl/worker-process worker {:op "call"
                                      :id "id-action"
                                      :action "@worker/ping.async"
                                      :body [100]}))
  => (contains-in {"body" ["pong" integer?], "id" "id-action", "status" "ok", "op" "call"}))

^{:refer js.cell.kernel.worker-impl/worker-init :added "4.0"}
(fact "initiates the worker actions"
  ^:hidden
  
  (!.js
   (var Worker (require (@! +tiny-worker-path+)))
   (var worker
        (new Worker
             (fn []
               (eval (@! (browser/play-script
                          '[(addEventListener
                             "message"
                             (fn [e]
                               (postMessage e.data))
                             false)]
                          true))))))
   (worker-impl/worker-init worker))
  => true)

^{:refer js.cell.kernel.worker-impl/worker-init-signal :added "4.0"}
(fact "posts an init message"
  ^:hidden

  (notify/wait-on :js
    (var Worker (require (@! +tiny-worker-path+)))
    (var worker
         (new Worker
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
    (worker-impl/worker-init-signal worker {:done true}))
  => {"op" "stream",
      "signal" "@worker/::INIT",
      "status" "ok",
      "body" {"done" true}})

^{:refer js.cell.kernel.worker-mock/mock-worker-send :added "4.0"}
(fact "sends a request to the mock worker"
  ^:hidden
  
  (notify/wait-on :js
    (var mock (worker-mock/create-worker (repl/>notify)
                                  {}
                                  true))
    (worker-mock/mock-worker-send mock "1+1"))
  => {"body" "{\"type\":\"data\",\"return\":\"number\",\"value\":2}",
      "id" nil,
      "status" "ok",
      "op" "eval"})

^{:refer js.cell.kernel.worker-mock/mock-worker :added "4.0"}
(fact "creates a new mock worker"

  (!.js
   (worker-mock/mock-worker k/identity))
  => {"::" "worker.mock", "listeners" [nil]})

^{:refer js.cell.kernel.worker-mock/create-worker :added "4.0"}
(fact "initialises the mock worker")


^{:refer js.cell.kernel.worker-impl/worker-process-eval :added "4.1"}
(fact "TODO")

^{:refer js.cell.kernel.worker-impl/worker-process-action :added "4.1"}
(fact "TODO")

^{:refer js.cell.kernel.worker-impl/worker-init-signal :added "4.1"}
(fact "TODO")
