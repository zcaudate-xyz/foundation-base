^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s30-workers-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.substrate.transport-browser]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.substrate :as event-node]
             [xt.substrate.base-frame :as event-frame]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(def +webworker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create
                 {"id" "worker-web"
                  "handlers"
                  {"demo/echo" {"fn" (fn [space args request worker-node]
         (return {"space" (. space ["id"])
                  "action" (. request ["action"])
                  "args" args
                  "worker" (. worker-node ["id"])}))}}}))
      (-> (xt.substrate.transport-browser/boot-self
           node
           {"transport_id" "host"
            "target" self
            "ready" {"signal" "ready"
                     "transport" "browser"
                     "worker" "worker-web"}})
          (xt.lang.spec-promise/x:promise-then
           (fn [_]
             (return node)))))
   {:lang :js
    :layout :full}))

(def +sharedworker-script+
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

(defn.js make-webworker
  "Creates a WebWorker from the test script."
  {:added "4.1"}
  []
  (var blob (new Blob [(@! +webworker-script+)]
                     {"type" "text/javascript"}))
  (var url (. (!:G URL) (createObjectURL blob)))
  (try
    (var worker (new Worker url))
    (. (!:G URL) (revokeObjectURL url))
    (return worker)
    (catch err
      (. (!:G URL) (revokeObjectURL url))
      (throw err))))

(defn.js make-sharedworker
  "Creates a SharedWorker from the test script."
  {:added "4.1"}
  []
  (var blob (new Blob [(@! +sharedworker-script+)]
                     {"type" "text/javascript"}))
  (var url (. (!:G URL) (createObjectURL blob)))
  (try
    (var shared (new SharedWorker url))
    (. (!:G URL) (revokeObjectURL url))
    (return shared)
    (catch err
      (. (!:G URL) (revokeObjectURL url))
      (throw err))))

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
        "worker" (-/make-webworker)})
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
       "sharedworker" (-/make-sharedworker)})
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
