(ns js.worker.e2e-event-node-test
  (:use code.test)
  (:require [hara.lang :as l]
             [hara.runtime.chromedriver :as chromedriver]
             [js.worker.link]
             [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
              [xt.event.node-transport-browser :as worker-transport]
              [xt.event.node :as event-node]
              [xt.event.node-frame :as event-frame]
              [xt.lang.common-repl :as repl]
              [xt.lang.spec-promise :as promise]]})

(def ^:private +webworker-script+
  (l/emit-script
   '(do
      (var node (xt.event.node/node-create {"id" "worker-web"}))
      (xt.event.node/register-handler
       node
       "demo/echo"
       (fn [space args request worker-node]
         (return {"space" (. space ["id"])
                  "action" (. request ["action"])
                  "args" args
                  "worker" (. worker-node ["id"])}))
       nil)
      (. (xt.event.node/attach-transport
          node
          "host"
          (xt.event.node-transport-browser/self-endpoint self))
         (then
          (fn [_]
            (. self (postMessage {"signal" "ready"
                                  "runtime" "xt.event.node"
                                  "worker" "worker-web"}))
            (return node))))
      node)
   {:lang :js
    :layout :full}))

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var node (xt.event.node/node-create {"id" "worker-shared"}))
            (var port (. e ["ports"] [0]))
            (. port (start))
            (xt.event.node/register-handler
             node
             "demo/echo"
             (fn [space args request worker-node]
               (return {"space" (. space ["id"])
                        "action" (. request ["action"])
                        "args" args
                        "worker" (. worker-node ["id"])}))
             nil)
            (xt.event.node/attach-transport
             node
             "host"
             (xt.event.node-transport-browser/self-endpoint port))
            (. port (postMessage {"signal" "ready"
                                  "runtime" "xt.event.node"
                                  "worker" "worker-shared"}))
            (return node))))
   {:lang :js
    :layout :full}))

(fact:global
  {:setup [(l/rt:restart :js)
           (l/rt:scaffold-imports :js)
           (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                              4000)]
   :teardown [(l/rt:stop)]})

(fact "make-webworker-link creates a live xt.event.node runtime and adapter"
  (notify/wait-on [:js 4000]
    (var link (worker-link/make-webworker-link (@! +webworker-script+)))
    (var endpoint (worker-transport/worker-endpoint link))
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (repl/notify frame)))
    true)
  => {"signal" "ready"
      "runtime" "xt.event.node"
      "worker" "worker-web"})

(fact "make-sharedworker-link creates a live xt.event.node runtime and adapter"
  (notify/wait-on [:js 4000]
    (var link (worker-link/make-sharedworker-link (@! +sharedworker-script+)))
    (var endpoint (worker-transport/worker-endpoint link))
    ((. endpoint ["start_fn"])
     (fn [frame ctx]
       (repl/notify frame)))
    true)
  => {"signal" "ready"
      "runtime" "xt.event.node"
      "worker" "worker-shared"})

(fact "demo/echo can be called from outside a live WebWorker"
  (notify/wait-on [:js 4000]
   (var link (worker-link/make-webworker-link (@! +webworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (cond (== (. frame ["signal"]) "ready")
            (do (promise/x:with-delay
                 50
                 (fn []
                   ((. endpoint ["send_fn"])
                    (event-frame/request-frame
                     "room/a"
                     "demo/echo"
                     ["single"]
                     nil))))
                nil)

            (event-frame/response-frame? frame)
            (repl/notify (. frame ["data"])))))
   true)
  => {"space" "room/a"
      "action" "demo/echo"
      "args" ["single"]
      "worker" "worker-web"})

(fact "demo/echo can be called from outside a live SharedWorker"
  (notify/wait-on [:js 4000]
   (var link (worker-link/make-sharedworker-link (@! +sharedworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (cond (== (. frame ["signal"]) "ready")
            (do (promise/x:with-delay
                 50
                 (fn []
                   ((. endpoint ["send_fn"])
                    (event-frame/request-frame
                     "room/a"
                     "demo/echo"
                     ["single"]
                     nil))))
                nil)

            (event-frame/response-frame? frame)
            (repl/notify (. frame ["data"])))))
   true)
  => {"space" "room/a"
      "action" "demo/echo"
      "args" ["single"]
      "worker" "worker-shared"})
