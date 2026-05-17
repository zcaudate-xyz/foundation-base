(ns xt.event.node-transport-browser-e2e-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [js.worker.link]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
             [xt.lang.spec-base :as xt]
             [xt.event.node :as event-node]
             [xt.event.node-frame :as event-frame]
             [xt.event.node-transport-browser :as browser-transport]
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
                                  "transport" "browser"
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
                                  "transport" "browser"
                                  "worker" "worker-shared"}))
            (return node))))
   {:lang :js
    :layout :full}))

(fact:global
  {:setup [(l/rt:restart :js)
           (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                              4000)]
   :teardown [(l/rt:stop)]})

(fact "worker-endpoint talks to a live WebWorker through xt.event.node-transport-browser"
  (notify/wait-on [:js 4000]
    (var link (worker-link/make-webworker-link (@! +webworker-script+)))
    (var endpoint (browser-transport/worker-endpoint link))
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
             (repl/notify {"ready" {"transport" "browser"
                                    "worker" "worker-web"}
                           "response" (. frame ["data"])}))))
    true)
  => {"ready" {"transport" "browser"
               "worker" "worker-web"}
      "response" {"space" "room/a"
                  "action" "demo/echo"
                  "args" ["single"]
                  "worker" "worker-web"}})

(fact "a browser-side node can talk to a live worker-side node through an attached browser transport"

  (notify/wait-on [:js 4000]
    (var state {"ready" nil})
    (var browser-node (event-node/node-create {"id" "browser-node"}))
    (var base-link (worker-link/make-webworker-link (@! +webworker-script+)))
    (var source
         {"create_fn"
          (fn [listener]
            (return
             ((. base-link ["create_fn"])
              (fn [event]
                (if (== (. event ["signal"]) "ready")
                  (xt/x:set-key state "ready" event)
                  (listener event nil))))))})
    (promise/x:promise-catch
     (promise/x:promise-then
      (event-node/attach-transport
       browser-node
       "worker"
       (browser-transport/worker-endpoint source))
      (fn [_]
        (return
         (promise/x:promise-then
          (promise/x:with-delay 50
                                (fn []
                                  (return nil)))
          (fn [_]
            (return
             (promise/x:promise-then
              (event-node/request
               browser-node
               "room/a"
               "demo/echo"
               ["browser-node"]
               {"transport_id" "worker"})
              (fn [response]
                (return
                 (promise/x:promise-then
                  (event-node/detach-transport browser-node "worker")
                  (fn [_]
                    (repl/notify {"ready" (. state ["ready"])
                                  "response" response}))))))))))))
     (fn [err]
       (repl/notify {"error" err}))))
  => {"ready" {"signal" "ready"
               "transport" "browser"
               "worker" "worker-web"}
      "response" {"space" "room/a"
                  "action" "demo/echo"
                  "args" ["browser-node"]
                  "worker" "worker-web"}})

(fact "sharedworker-endpoint talks to a live SharedWorker through xt.event.node-transport-browser"
  (notify/wait-on [:js 4000]
    (var link (worker-link/make-sharedworker-link (@! +sharedworker-script+)))
    (var port ((. link ["create_fn"]) (fn [data] nil)))
    (var endpoint (browser-transport/sharedworker-endpoint port))
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
             (repl/notify {"ready" {"transport" "browser"
                                    "worker" "worker-shared"}
                           "response" (. frame ["data"])}))))
    true)
  => {"ready" {"transport" "browser"
               "worker" "worker-shared"}
      "response" {"space" "room/a"
                  "action" "demo/echo"
                  "args" ["single"]
                  "worker" "worker-shared"}})
