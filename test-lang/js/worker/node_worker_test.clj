(ns js.worker.node-worker-test
  (:use code.test)
  (:require [hara.lang :as l]
            [js.worker.env-node]
            [js.worker.link]
            [xt.substrate]
            [xt.substrate.transport-browser]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.worker.env-node :as env-node]
             [js.worker.link :as worker-link]
             [xt.substrate :as event-node]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(def ^:private +nodeworker-script+
  (l/emit-script
   '(do
      (var worker (js.worker.env-node/make-node-worker))
      (var node (xt.substrate/node-create {"id" "worker-node"}))
      (xt.substrate/register-handler
       node
       "demo/echo"
       (fn [space args request worker-node]
         (return {"space" (. space ["id"])
                  "action" (. request ["action"])
                  "args" args
                  "worker" (. worker-node ["id"])}))
       nil)
      (. (xt.substrate/attach-transport
          node
          "host"
          (xt.substrate.transport-browser/self-endpoint worker))
         (then
          (fn [_]
            (. worker (postMessage {"signal" "ready"
                                    "worker" (. node ["id"])}))
            (return node))))
      node)
   {:lang :js
    :layout :flat}))

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:scaffold-imports :js)]
   :teardown [(l/rt:stop)]})

(fact "creates a live Node worker and talks to it through xt.substrate"
  (notify/wait-on [:js 10000]
    (var host-node (event-node/node-create {"id" "host-node"}))
    (var state {"ready" nil})
    (var current-worker nil)
    (var await-ready
         (fn []
           (if (xt/x:not-nil? (. state ["ready"]))
             (return (promise/x:promise-run (. state ["ready"])))
             (return
              (promise/x:promise-then
               (promise/x:with-delay 10
                 (fn []
                   (return nil)))
               (fn [_]
                 (return (await-ready))))))))
    (var link (worker-link/make-node-link (@! +nodeworker-script+) {}))
    (var transport
         {"send_fn"
          (fn [frame]
            (return (. current-worker (postMessage frame))))

          "start_fn"
          (fn [listener]
            (:= current-worker
                ((. link ["create_fn"])
                 (fn [event]
                   (if (== (. event ["signal"]) "ready")
                     (xt/x:set-key state "ready" event)
                     (listener event nil)))))
            (return current-worker))

          "stop_fn"
          (fn [_]
            (when (and (xt/x:not-nil? current-worker)
                       (xt/x:is-function? (. current-worker ["terminate"])))
              (. current-worker (terminate)))
            (:= current-worker nil)
            (return true))})
    (promise/x:promise-catch
     (promise/x:promise-then
      (event-node/attach-transport
       host-node
       "worker"
       transport)
      (fn [_]
        (promise/x:promise-then
         (await-ready)
         (fn [ready]
           (promise/x:promise-then
            (event-node/request
             host-node
             "room/a"
             "demo/echo"
             ["alpha"]
             {"transport_id" "worker"})
            (fn [response]
              (promise/x:promise-then
               (event-node/detach-transport host-node "worker")
               (fn [_]
                 (repl/notify {"ready" ready
                               "response" response})))))))))
     (fn [err]
       (repl/notify {"error" err}))))
  => {"ready" {"signal" "ready"
               "worker" "worker-node"}
      "response" {"space" "room/a"
                  "action" "demo/echo"
                  "args" ["alpha"]
                  "worker" "worker-node"}})
