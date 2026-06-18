(ns js.worker.e2e-sharedworker-tabs-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as event-node]
             [xt.substrate.transport-browser :as browser-transport]
             [js.worker.link :as worker-link]]})

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      ;; single node shared across all tab connections
      (var node (xt.substrate/node-create {"id" "sharedworker-tabs-demo"}))

      ;; shared counter lives in a node space instead of globalThis
      (xt.substrate/create-space node "shared" {:state {"counter" 0}})

      ;; register the handlers once on the global node
      (xt.substrate/register-handler
       node
       "demo/counter"
       (fn [space args request worker-node]
         (var state (xt.substrate/get-space-state worker-node "shared"))
         (return {"counter" (xt.lang.spec-base/x:get-key state "counter")
                  "worker" (. worker-node ["id"])}))
       nil)

      (xt.substrate/register-handler
       node
       "demo/disconnect"
       (fn [space args request worker-node]
         (xt.substrate/update-space-state
          worker-node
          "shared"
          (fn [state]
            (return {"counter" (- (or (xt.lang.spec-base/x:get-key state "counter") 0) 1)})))
         (return {"status" "disconnected"}))
       nil)

      ;; onconnect fires for every tab and attaches a new transport
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))

            ;; increment the connection counter inside the node space
            (var new-state
                 (xt.substrate/update-space-state
                  node
                  "shared"
                  (fn [state]
                    (return {"counter" (+ 1 (or (xt.lang.spec-base/x:get-key state "counter") 0))}))))

            ;; each connection needs a unique transport id on the same node
            (var transport-id
                 (xt.lang.spec-base/x:cat
                  "host-"
                  (xt.lang.spec-base/x:get-key new-state "counter")))

            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" transport-id
              "target" port
              "ready" {"signal" "ready"
                       "worker" "sharedworker-tabs-demo"}}))))
   {:lang :js
    :layout :full}))

(def ^:private +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(defn.js store-worker-url
  "Creates a fresh blob URL for the sharedworker script and stores it in localStorage."
  {:added "4.1"}
  []
  (var url (worker-link/make-blob-url (@! +sharedworker-script+)))
  (. (!:G localStorage) (setItem "__js_worker_tabs_url__" url))
  (return url))

(fact:global
 {:setup [(l/rt:restart :js)
          (chromedriver/goto +notify-url+ 4000)]
  :teardown [(l/rt:stop)]})

(defn tab-connect
  "Connects `tab` to the sharedworker and stores the connection on globalThis."
  {:added "4.1"}
  [tab]
  (chromedriver/with-tab (l/rt :js) tab
    (notify/wait-on [:js 5000]
      (var browser-node (event-node/node-create {"id" "browser-node"}))
      (var worker-url (. (!:G localStorage) (getItem "__js_worker_tabs_url__")))
      (promise/x:promise-catch
       (promise/x:promise-then
        (browser-transport/connect-sharedworker
         browser-node
         {"transport_id" "worker"
          "source" (worker-link/make-sharedworker-link-from-url worker-url)})
        (fn [conn]
          (:= (. globalThis ["__conn__"]) conn)
          (repl/notify {"ready" (. conn ["ready"])})))
       (fn [err]
         (repl/notify {"error" (xt/x:ex-message err)}))))))

(defn tab-request-counter
  "Requests the demo/counter value from the sharedworker on `tab`."
  {:added "4.1"}
  [tab]
  (chromedriver/with-tab (l/rt :js) tab
    (notify/wait-on [:js 5000]
      (var conn (. globalThis ["__conn__"]))
      (var browser-node (. conn ["node"]))
      (promise/x:promise-catch
       (promise/x:promise-then
        (event-node/request
         browser-node
         "room/a"
         "demo/counter"
         []
         {"transport_id" (. conn ["transport_id"])})
        (fn [response]
          (repl/notify {"counter" (xt/x:get-key response "counter")
                        "worker" (xt/x:get-key response "worker")})))
       (fn [err]
         (repl/notify {"error" (xt/x:ex-message err)}))))))


(defn.js node-disconnect
  [node])

(defn tab-disconnect
  "Disconnects the stored sharedworker connection on `tab`."
  {:added "4.1"}
  [tab]
  (chromedriver/with-tab (l/rt :js) tab
    (notify/wait-on [:js 5000]
      (var conn (. globalThis ["__conn__"]))
      (var browser-node (. conn ["node"]))
      (promise/x:promise-catch
       (promise/x:promise-then
        (event-node/request
         browser-node
         "room/a"
         "demo/disconnect"
         []
         {"transport_id" (. conn ["transport_id"])})
        (fn [_]
          (promise/x:promise-then
           (browser-transport/disconnect conn)
           (fn [_]
             (repl/notify {"status" "disconnected"})))))
       (fn [err]
         (repl/notify {"error" (xt/x:ex-message err)}))))))

^{:refer js.worker.e2e-sharedworker-tabs-test/sharedworker-is-shared-across-tabs
  :added "4.1"
  :setup [(store-worker-url)]}
(fact "sharedworker instance is shared across two chromedriver tabs"
  (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))

  (tab-connect +tab-a+)
  (def result-a (tab-request-counter +tab-a+))

  (tab-connect +tab-b+)
  (def result-b (tab-request-counter +tab-b+))

  (tab-disconnect +tab-a+)
  (tab-disconnect +tab-b+)
  (chromedriver/tab-close (l/rt :js) +tab-b+)
  (chromedriver/tab-switch (l/rt :js) +tab-a+ {:bootstrap false})

  result-a => {"counter" 1
               "worker" "sharedworker-tabs-demo"}
  result-b => {"counter" 2
               "worker" "sharedworker-tabs-demo"})

^{:refer js.worker.e2e-sharedworker-tabs-test/sharedworker-survives-tab-disconnect
  :added "4.1"
  :setup [(store-worker-url)]}
(fact "sharedworker keeps serving after one connecting tab disconnects"
  (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))
  
  (tab-connect +tab-a+)
  (tab-connect +tab-b+)
  
  ;; disconnect tab a, but leave it open so the blob url stays valid
  (tab-disconnect +tab-a+)
  
  (def result-b (tab-request-counter +tab-b+))
  
  (tab-disconnect +tab-b+)

  result-b => {"counter" 1
               "worker" "sharedworker-tabs-demo"})
