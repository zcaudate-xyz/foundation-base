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
      (:= (. globalThis ["__js_worker_tabs_counter__"]) 0)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var node (xt.substrate/node-create {"id" "sharedworker-tabs-demo"}))
            (var port (. e ["ports"] [0]))
            (. port (start))
            (var counter (+ 1 (. globalThis ["__js_worker_tabs_counter__"])))
            (:= (. globalThis ["__js_worker_tabs_counter__"]) counter)
            (xt.substrate/register-handler
             node
             "demo/counter"
             (fn [space args request worker-node]
               (return {"counter" (. globalThis ["__js_worker_tabs_counter__"])
                        "worker" (. worker-node ["id"])}))
             nil)
            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" "host"
              "target" port
              "ready" {"signal" "ready"
                       "worker" "sharedworker-tabs-demo"}})
            (return node))))
   {:lang :js
    :layout :full}))

(def ^:private +notify-url+
  (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/"))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto +notify-url+ 4000)
          (!.js
            (var blob (new Blob [(@! +sharedworker-script+)]
                               {"type" "text/javascript"}))
            (var url (. (!:G URL) (createObjectURL blob)))
            (. (!:G localStorage) (setItem "__js_worker_tabs_url__" url))
            (return url))]
  :teardown [(l/rt:stop)]})

(defn tab-connect
  "Connects `tab` to the sharedworker and stores the connection on globalThis."
  {:added "4.1"}
  [tab]
  (chromedriver/with-tab (l/rt :js) tab
    (notify/wait-on [:js 5000]
      (var browser-node (event-node/node-create {"id" "browser-node"}))
      (var worker-url (. (!:G localStorage) (getItem "__js_worker_tabs_url__")))
      (var shared (new SharedWorker worker-url))
      (promise/x:promise-catch
       (promise/x:promise-then
        (browser-transport/connect-sharedworker
         browser-node
         {"transport_id" "worker"
          "sharedworker" shared})
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

(defn tab-disconnect
  "Disconnects the stored sharedworker connection on `tab`."
  {:added "4.1"}
  [tab]
  (chromedriver/with-tab (l/rt :js) tab
    (notify/wait-on [:js 5000]
      (var conn (. globalThis ["__conn__"]))
      (promise/x:promise-catch
       (promise/x:promise-then
        (browser-transport/disconnect conn)
        (fn [_]
          (repl/notify {"status" "disconnected"})))
       (fn [err]
         (repl/notify {"error" (xt/x:ex-message err)}))))))

^{:refer js.worker.e2e-sharedworker-tabs-test/debug-sharedworker-connect
  :added "4.1"}
(fact "single-tab sharedworker connection using make-sharedworker-link works"
  (notify/wait-on [:js 5000]
    (var browser-node (event-node/node-create {"id" "browser-node"}))
    (promise/x:promise-catch
     (promise/x:promise-then
      (browser-transport/connect-sharedworker
       browser-node
       {"transport_id" "worker"
        "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
      (fn [conn]
        (repl/notify {"ready" (. conn ["ready"])})))
     (fn [err]
       (repl/notify {"error" (xt/x:ex-message err)}))))
  => {"ready" {"signal" "ready"
               "worker" "sharedworker-tabs-demo"}})

^{:refer js.worker.e2e-sharedworker-tabs-test/sharedworker-is-shared-across-tabs
  :added "4.1"}
(fact "sharedworker instance is shared across two chromedriver tabs"
  (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))

  (def connect-a (tab-connect +tab-a+))
  connect-a => {"ready" {"signal" "ready"
                          "worker" "sharedworker-tabs-demo"}}
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

^{:refer js.worker.e2e-sharedworker-tabs-test/sharedworker-survives-tab-close
  :added "4.1"}
(fact "sharedworker keeps serving after one connecting tab closes"
  (def +tab-a+ (chromedriver/current-tab (l/rt :js)))
  (def +tab-b+ (chromedriver/tab-create (l/rt :js) +notify-url+))

  (tab-connect +tab-a+)
  (tab-connect +tab-b+)

  ;; disconnect tab a, but leave it open so the blob url stays valid
  (tab-disconnect +tab-a+)

  (def result-b (tab-request-counter +tab-b+))

  (tab-disconnect +tab-b+)

  result-b => {"counter" 2
               "worker" "sharedworker-tabs-demo"})
