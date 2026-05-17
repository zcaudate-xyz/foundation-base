(ns xt.substrate.transport-browser-e2e-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [js.worker.link]
            [xt.substrate.transport-browser]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
             [xt.lang.spec-base :as xt]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as event-frame]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(def ^:private +webworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "worker-web"}))
      (xt.substrate/register-handler
       node
       "demo/echo"
       (fn [space args request worker-node]
         (return {"space" (. space ["id"])
                  "action" (. request ["action"])
                  "args" args
                  "worker" (. worker-node ["id"])}))
       nil)
      (. (xt.substrate.transport-browser/boot-self
          node
         {"transport_id" "host"
          "target" self
          "ready" {"signal" "ready"
                   "transport" "browser"
                   "worker" "worker-web"}})
        (then
         (fn [_]
            (return node))))
      node)
   {:lang :js
    :layout :full}))

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var node (xt.substrate/node-create {"id" "worker-shared"}))
            (var port (. e ["ports"] [0]))
            (. port (start))
            (xt.substrate/register-handler
             node
             "demo/echo"
             (fn [space args request worker-node]
               (return {"space" (. space ["id"])
                        "action" (. request ["action"])
                        "args" args
                        "worker" (. worker-node ["id"])}))
             nil)
            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" "host"
              "target" port
              "ready" {"signal" "ready"
                       "transport" "browser"
                       "worker" "worker-shared"}})
            (return node))))
   {:lang :js
    :layout :full}))

(fact:global
  {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
   :teardown [(l/rt:stop)]})

(fact "connect-worker creates a browser-side node connection to a live WebWorker node"
  (notify/wait-on [:js 4000]
    (var browser-node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-catch
     (promise/x:promise-then
      (browser-transport/connect-worker
       browser-node
       {"transport_id" "worker"
        "source" (worker-link/make-webworker-link (@! +webworker-script+))})
      (fn [conn]
        (return
         (promise/x:promise-then
          (event-node/request
           browser-node
           "room/a"
           "demo/echo"
           ["browser-node"]
           {"transport_id" (. conn ["transport_id"])})
          (fn [response]
            (return
             (promise/x:promise-then
              (browser-transport/disconnect conn)
              (fn [_]
                (repl/notify {"ready" (. conn ["ready"])
                              "response" response})))))))))
     (fn [err]
       (repl/notify {"error" err}))))
  => {"ready" {"signal" "ready"
              "transport" "browser"
              "worker" "worker-web"}
     "response" {"space" "room/a"
                 "action" "demo/echo"
                 "args" ["browser-node"]
                 "worker" "worker-web"}})

(fact "connect-sharedworker creates a browser-side node connection to a live SharedWorker node"
  (notify/wait-on [:js 4000]
    (var browser-node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-catch
     (promise/x:promise-then
     (browser-transport/connect-sharedworker
      browser-node
      {"transport_id" "worker"
       "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
     (fn [conn]
       (return
        (promise/x:promise-then
         (event-node/request
          browser-node
          "room/a"
          "demo/echo"
          ["browser-node"]
          {"transport_id" (. conn ["transport_id"])})
         (fn [response]
           (return
            (promise/x:promise-then
             (browser-transport/disconnect conn)
             (fn [_]
               (repl/notify {"ready" (. conn ["ready"])
                             "response" response})))))))))
     (fn [err]
      (repl/notify {"error" err}))))
  => {"ready" {"transport" "browser"
              "signal" "ready"
              "worker" "worker-shared"}
     "response" {"space" "room/a"
                 "action" "demo/echo"
                 "args" ["browser-node"]
                 "worker" "worker-shared"}})
