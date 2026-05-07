(ns js.worker.e2e-event-node-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [js.worker.link]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
              [js.worker.transport :as worker-transport]
              [xt.event.node :as event-node]
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
          (js.worker.transport/self-endpoint self))
         (then
          (fn [_]
            (. self (postMessage {"signal" "ready"
                                  "runtime" "xt.event.node"
                                  "worker" "worker-web"}))
            (return node))))
      node)
   {:lang :js
    :layout :flat}))

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
             (js.worker.transport/self-endpoint port))
            (. port (postMessage {"signal" "ready"
                                  "runtime" "xt.event.node"
                                  "worker" "worker-shared"}))
            (return node))))
   {:lang :js
    :layout :flat}))

(fact:global
  {:setup [(l/rt:restart :js)
           (l/rt:scaffold-imports :js)
           (chromedriver/goto "https://example.com/" 4000)]
   :teardown [(l/rt:stop)]})

(fact "make-webworker-link creates a live xt.event.node runtime and adapter"
  (!.js
   (:= (. globalThis ["__worker_test"]) {"stage" "init"})
   (var link (worker-link/make-webworker-link (@! +webworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (:= (. globalThis ["__worker_test"]) frame)))
   true)
  => true

  (do (Thread/sleep 1000) true)
  => true

  (!.js
   (return (. globalThis ["__worker_test"])))
  => {"signal" "ready"
      "runtime" "xt.event.node"
      "worker" "worker-web"})

(fact "make-sharedworker-link creates a live xt.event.node runtime and adapter"
  (!.js
   (:= (. globalThis ["__worker_test"]) {"stage" "init"})
   (var link (worker-link/make-sharedworker-link (@! +sharedworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (:= (. globalThis ["__worker_test"]) frame)))
   true)
  => true

  (do (Thread/sleep 1000) true)
  => true

  (!.js
   (return (. globalThis ["__worker_test"])))
  => {"signal" "ready"
      "runtime" "xt.event.node"
      "worker" "worker-shared"})

(fact "demo/echo can be called from outside a live WebWorker"
  (!.js
   (:= (. globalThis ["__worker_test"]) {"stage" "init"})
   (var link (worker-link/make-webworker-link (@! +webworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (cond (== (. frame ["signal"]) "ready")
            (do (promise/x:with-delay
                 50
                 (fn []
                   ((. endpoint ["send_fn"])
                    (event-node/request-frame
                     "room/a"
                     "demo/echo"
                     ["single"]
                     nil))
                   (:= (. globalThis ["__worker_test"])
                       {"stage" "requesting"})))
                nil)

            (event-node/response-frame? frame)
            (:= (. globalThis ["__worker_test"])
                (. frame ["data"]))

            :else
            (:= (. globalThis ["__worker_test"])
                frame))))
   true)
  => true

  (do (Thread/sleep 1500) true)
  => true

  (!.js
   (return (. globalThis ["__worker_test"])))
  => {"space" "room/a"
      "action" "demo/echo"
      "args" ["single"]
      "worker" "worker-web"})

(fact "demo/echo can be called from outside a live SharedWorker"
  (!.js
   (:= (. globalThis ["__worker_test"]) {"stage" "init"})
   (var link (worker-link/make-sharedworker-link (@! +sharedworker-script+)))
   (var endpoint (worker-transport/worker-endpoint link))
   ((. endpoint ["start_fn"])
    (fn [frame ctx]
      (cond (== (. frame ["signal"]) "ready")
            (do (promise/x:with-delay
                 50
                 (fn []
                   ((. endpoint ["send_fn"])
                    (event-node/request-frame
                     "room/a"
                     "demo/echo"
                     ["single"]
                     nil))
                   (:= (. globalThis ["__worker_test"])
                       {"stage" "requesting"})))
                nil)

            (event-node/response-frame? frame)
            (:= (. globalThis ["__worker_test"])
                (. frame ["data"]))

            :else
            (:= (. globalThis ["__worker_test"])
                frame))))
   true)
  => true

  (do (Thread/sleep 1500) true)
  => true

  (!.js
   (return (. globalThis ["__worker_test"])))
  => {"space" "room/a"
      "action" "demo/echo"
      "args" ["single"]
      "worker" "worker-shared"})
