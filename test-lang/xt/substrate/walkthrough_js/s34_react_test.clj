^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s34-react-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]
             [js.react.ext-page :as ext-page]]})

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      ;; single shared node and page group across all tab connections
      (var node (xt.substrate/node-create
                 {"id" "page-proxy-shared-server"
                  "spaces" {"room/a" {"state" {}}}}))
      (xt.substrate.page-proxy/install node)
      (xt.substrate.page-core/group-add
       node
       "room/a"
       "demo"
       {"main" {"defaults" {"args" ["hello"]
                            "output" {}
                            "process" (fn [x] (return x))
                            "init" (fn [] (return nil))}
                "handler" (fn [ctx]
                            (var data (xt.lang.common-data/get-in ctx ["input" "data"]))
                            (return {"value" (xt.lang.spec-base/x:first data)}))
                "options" {"trigger" true}}})

      ;; each tab connection needs a unique transport id on the shared node,
      ;; otherwise later connections overwrite earlier ones and server pushes
      ;; can only reach the most recently connected tab.
      (var connection-counter 0)

      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (:= connection-counter (+ connection-counter 1))
            (var transport-id (xt.lang.spec-base/x:cat "host-" connection-counter))
            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" transport-id
              "target" port
              "ready" {"signal" "ready"
                       "transport" "browser"
                       "worker" "page-proxy-shared"}})
            (return node))))
   {:lang :js
    :layout :full}))

(defn.js store-worker-url
  []
  (var url (browser-transport/blob-url (@! +sharedworker-script+)))
  (. (!:G localStorage) (setItem "__s34_worker_url__" url))
  (return url))

(defn.js make-worker-link
  []
  (var url (. (!:G localStorage) (getItem "__s34_worker_url__")))
  (return (browser-transport/sharedworker-url-source url)))

(defn.js group-output-value
  [group]
  (var model (xtd/get-in group ["models" "main"]))
  (var output (event-model/get-current model nil))
  (return (. output ["value"])))

(defn.js tab1-state
  []
  (return (. (!:G globalThis) ["__s34_tab1_state__"])))

(defn.js tab1-output-value
  []
  (var output (. (!:G globalThis) ["__s34_tab1_output__"]))
  (return (. output ["value"])))

(defn.js connect-tab
  [client-id]
  (var client (substrate/node-create
               {"id" client-id
                "spaces" {"room/a" {"state" {}}}}))
  (page-proxy/install client)
  (return
   (-> (browser-transport/connect-sharedworker
        client
        {"transport_id" "worker"
         "source" (-/make-worker-link)})
       (promise/x:promise-then
        (fn [conn]
          (return (page-proxy/group-open-proxy
                   client
                   "room/a"
                   "demo"
                   {"transport_id" (. conn ["transport_id"])}))))
       (promise/x:promise-then
        (fn [group]
          (return {"client" client
                   "group" group}))))))

(defn.js boot-tab1
  []
  (-/store-worker-url)
  (return (-/connect-tab "react-client-tab1")))

(defn.js setup-tab1
  []
  (return
   (-> (-/boot-tab1)
       (promise/x:promise-then
        (fn [state]
          (:= (. (!:G globalThis) ["__s34_tab1_state__"]) state)
          (var model (xtd/get-in (. state ["group"]) ["models" "main"]))
          (event-model/add-listener
           model
           "@/test/watch-output"
           (fn [_id data _t meta]
             (when (== "model.output" (xt/x:get-key data "type"))
               (:= (. (!:G globalThis) ["__s34_tab1_output__"])
                   (event-model/get-current model nil)))))
          (-> (page-core/model-set-input
               (. state ["client"])
               "room/a"
               "demo"
               "main"
               {"data" ["hello"]}
               {})
              (promise/x:promise-then
               (fn [_]
                 (:= (. (!:G globalThis) ["__s34_tab1_output__"])
                     (event-model/get-current model nil))
                 (return state)))))))))

(defn.js update-from-tab2
  []
  (return
   (-> (-/connect-tab "react-client-tab2")
       (promise/x:promise-then
        (fn [state]
          (page-core/model-set-input
           (. state ["client"])
           "room/a"
           "demo"
           "main"
           {"data" ["world"]}
           {}))))))

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.walkthrough-js.s34-react-test/stage-1-worker-and-proxy
  :added "4.1"}
(fact "stage 1: the sharedworker boots and the proxy group exposes the model output"
  (let [result (notify/wait-on :js
                (-> (-/boot-tab1)
                    (promise/x:promise-then
                     (fn [state]
                       (promise/x:with-delay 100
                         (fn []
                           (repl/notify (-/group-output-value (. state ["group"])))))))
                    (promise/x:promise-catch
                     (fn [err]
                       (repl/notify {"error" (xt/x:ex-message err)})))))]
    (assert (= "hello" result))))

^{:refer xt.substrate.walkthrough-js.s34-react-test/stage-2-proxy-client-view
  :added "4.1"}
(fact "stage 2: the first tab opens the proxy client without errors"
  (!.js
    (-/setup-tab1))
  => {})

^{:refer xt.substrate.walkthrough-js.s34-react-test/stage-3-second-tab-update
  :added "4.1"}
(fact "stage 3: tab1 sees the update that tab2 writes to the sharedworker-backed model"
  (let [notify-server (l/default-notify)
        browser (l/rt :js)
        tab2 (chromedriver/tab-create browser
                                      (str "http://127.0.0.1:" (:http-port notify-server) "/"))]
    (notify/wait-on :js
      (-> (-/setup-tab1)
          (promise/x:promise-then
           (fn [_]
             (repl/notify true)))
          (promise/x:promise-catch
           (fn [err]
             (repl/notify {"error" (xt/x:ex-message err)})))))
    (chromedriver/with-tab browser tab2
      (l/rt:scaffold-imports :js)
      (notify/wait-on :js
        (-/update-from-tab2)))
    (let [final (notify/wait-on [:js 5000]
                 (promise/x:with-delay
                  1000
                  (fn []
                    (repl/notify {"final" (-/tab1-output-value)}))))]
      (assert (= {"final" "world"} final)))))
