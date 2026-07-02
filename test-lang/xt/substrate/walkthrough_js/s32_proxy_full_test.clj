;;
;; s32_proxy_full_test.clj
;;
;; Demonstrates the full server-to-client proxy delta flow using model listeners
;; instead of polling:
;;   1. A SharedWorker hosts a substrate node with a real page group.
;;   2. A browser client opens that group as a proxy.
;;   3. The client attaches a "model.output" listener to the proxy model.
;;   4. The client changes the proxy model's input.
;;   5. The request is forwarded to the server, which updates the real model.
;;   6. The server publishes a page.model/output delta back to the client.
;;   7. The client's proxy model updates, fires the listener, and the test
;;      harness is notified.
;;
^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s32-proxy-full-test
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
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]]})

;;
;; This script runs inside a browser SharedWorker. It creates the "server" node,
;; installs the page-proxy protocol, registers one demo page group, and exposes
;; itself as a substrate transport via boot-self.
;;
(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            ;; server-side substrate node
            (var node (xt.substrate/node-create
                       {"id" "page-proxy-shared-server"
                        "spaces" {"room/a" {"state" {}}}}))
            ;; install both handlers and stream triggers on the server
            (xt.substrate.page-proxy/install node)
            ;; a single page group with one model:
            ;;   input.data defaults to ["hello"]
            ;;   handler returns {:value (first input.data)}
            (xt.substrate.page-core/add-group
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
            ;; start listening on the SharedWorker port
            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" "host"
              "target" port
              "ready" {"signal" "ready"
                       "transport" "browser"
                       "worker" "page-proxy-shared"}})
            (return node))))
   {:lang :js
    :layout :full}))





(fact:global
 {:setup [(l/rt:restart)
          ;; navigate chromedriver to the test notify page
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-proxy/full-proxy-flow
  :added "4.1"}
(fact "server model change pushes output delta to client proxy"

  (notify/wait-on [:js 5000]
    ;; 1. create the browser client node and install page-proxy
    (var client (substrate/node-create
                 {"id" "page-proxy-browser-client"
                  "spaces" {"room/a" {"state" {}}}}))
    (page-proxy/install client)
    (var conn-ref nil)
    (->
     ;; 2. connect to the SharedWorker server transport
     (browser-transport/connect-sharedworker
      client
      {"transport_id" "worker"
       "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {})})
     (promise/x:promise-then
      (fn [conn]
        (:= conn-ref conn)
        ;; 3. open the remote group as a proxy on the client
        (return
         (page-proxy/open-proxy-group
          client
          "room/a"
          "demo"
          {"transport_id" (. conn ["transport_id"])}))))
     (promise/x:promise-then
      (fn [group]
        (var model (xtd/get-in group ["models" "main"]))
        ;; capture the initial output from the snapshot ("hello")
        (var initial (event-model/get-current model nil))
        ;; 4. attach a listener that reports the first output change.
        ;;    When the server pushes the page.model/output delta, the client
        ;;    proxy model emits "model.output" and this listener notifies the
        ;;    test harness with both the initial snapshot and the new value.
        (event-model/add-listener
         model
         "@/test/watch-output"
         (fn [_id data _t meta]
           (when (== "model.output" (xt/x:get-key data "type"))
             (repl/notify
              {"initial" initial
               "final" (event-model/get-current model nil)})))
         nil
         nil)
        ;; 5. change the proxy model's input; because the group is a proxy,
        ;;    page-core/model-set-input forwards the operation to the server.
        ;;    The server refreshes the real model and pushes the output delta.
        (return
         (base-page/model-set-input
          client
          "room/a"
          "demo"
          "main"
          {"data" ["world"]}
          {}))))
     (promise/x:promise-catch
      (fn [err]
        (when (xt/x:not-nil? conn-ref)
          (browser-transport/disconnect conn-ref))
        (repl/notify {"error" err
                      "message" (xt/x:ex-message err)})))))
  => {"initial" {"value" "hello"}
      "final"   {"value" "world"}})

^{:refer xt.substrate.page-proxy/full-proxy-listener-flow
  :added "4.1"}
(fact "proxy model listener fires when server pushes output delta"

  (notify/wait-on [:js 5000]
    ;; 1. create the browser client node and install page-proxy
    (var client (substrate/node-create
                 {"id" "page-proxy-browser-client-listener"
                  "spaces" {"room/a" {"state" {}}}}))
    (page-proxy/install client)
    (var conn-ref nil)
    (->
     ;; 2. connect to the SharedWorker server transport
     (browser-transport/connect-sharedworker
      client
      {"transport_id" "worker"
       "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {})})
     (promise/x:promise-then
      (fn [conn]
        (:= conn-ref conn)
        ;; 3. open the remote group as a proxy on the client
        (return
         (page-proxy/open-proxy-group
          client
          "room/a"
          "demo"
          {"transport_id" (. conn ["transport_id"])}))))
     (promise/x:promise-then
      (fn [group]
        (var model (xtd/get-in group ["models" "main"]))
        ;; 4. attach a listener directly to the proxy model.
        ;;    When the server publishes the output delta, the client applies it
        ;;    via apply-model-output and the model emits a "model.output" event.
        ;;    The listener catches that event and notifies the test harness.
        (event-model/add-listener
         model
         "@/test/watch-output"
         (fn [_id data _t meta]
           (when (== "model.output" (xt/x:get-key data "type"))
             (repl/notify
              {"captured" (event-model/get-current model nil)})))
         nil
         nil)
        ;; 5. change the proxy model's input; because the group is a proxy,
        ;;    page-core/model-set-input forwards the operation to the server.
        ;;    The server refreshes the real model and pushes the output delta.
        (return
         (base-page/model-set-input
          client
          "room/a"
          "demo"
          "main"
          {"data" ["world"]}
          {}))))
     (promise/x:promise-catch
      (fn [err]
        (when (xt/x:not-nil? conn-ref)
          (browser-transport/disconnect conn-ref))
        (repl/notify {"error" err
                      "message" (xt/x:ex-message err)})))))
  => {"captured" {"value" "world"}})
