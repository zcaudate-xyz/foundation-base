^{:seedgen/skip true}
(ns xt.substrate.walkthrough-js.s31-proxy-test
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

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
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
                      "trigger" true
                      "options" {}}})
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

(defn- ci?
  []
  (boolean (System/getenv "CI")))

(fact:global
 {:skip (ci?)
  :setup [(l/rt:restart)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-proxy/group-open-proxy
  :added "4.1"}
(fact "page-proxy can list, open, and read a remote group over a SharedWorker"
  
  (notify/wait-on [:js 5000]
    (var client (substrate/node-create
                 {"id" "page-proxy-browser-client"
                  "spaces" {"room/a" {"state" {}}}}))
    (page-proxy/install client)
    (var transport-id nil)
    (-> (browser-transport/connect-sharedworker
         client
         {"transport_id" "worker"
          "source" (browser-transport/sharedworker-source (@! +sharedworker-script+) {})})
        (promise/x:promise-then
         (fn [conn]
           (:= transport-id (. conn ["transport_id"]))
           (return
            (page-proxy/group-list-proxy client "room/a" {"transport_id" transport-id}))))
        (promise/x:promise-then
         (fn [groups]
           (return
            (page-proxy/group-open-proxy
             client
             "room/a"
             "demo"
             {"transport_id" transport-id}))))
        (promise/x:promise-then
         (fn [group]
           (var model (xtd/get-in group ["models" "main"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})
           (browser-transport/disconnect conn)))        
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" {"value" "hello"}}))
