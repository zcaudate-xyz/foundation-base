^{:seedgen/skip true}
(ns xt.substrate.browser.e2e-page-remote-sharedworker-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [js.worker.link]
            [xt.lang.common-notify :as notify]
            [xt.substrate.page-remote]
            [xt.substrate.page-core]
            [xt.event.base-model]
            [xt.substrate.transport-browser]))

(def ^:private +sharedworker-script+
  (l/emit-script
   '(do
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (var node (xt.substrate/node-create
                       {"id" "page-remote-shared-server"
                        "spaces" {"room/a" {"state" {}}}}))
            (xt.substrate.page-remote/install node)
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
                      "trigger" true
                      "options" {}}})
            (xt.substrate.transport-browser/boot-self
             node
             {"transport_id" "host"
              "target" port
              "ready" {"signal" "ready"
                       "transport" "browser"
                       "worker" "page-remote-shared"}})
            (return node))))
   {:lang :js
    :layout :full}))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[js.worker.link :as worker-link]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-remote :as page-remote]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.page-remote/open-remote-group
  :added "4.1"}
(fact "page-remote can list, open, and read a remote group over a SharedWorker"

  (notify/wait-on [:js 5000]
    (var client (substrate/node-create
                 {"id" "page-remote-browser-client"
                  "spaces" {"room/a" {"state" {}}}}))
    (page-remote/install client)
    (promise/x:promise-catch
     (promise/x:promise-then
      (browser-transport/connect-sharedworker
       client
       {"transport_id" "worker"
        "source" (worker-link/make-sharedworker-link (@! +sharedworker-script+))})
      (fn [conn]
        (var transport-id (. conn ["transport_id"]))
        (return
         (promise/x:promise-then
          (page-remote/list-remote-groups client "room/a" {"transport_id" transport-id})
          (fn [groups]
            (return
             (promise/x:promise-then
              (page-remote/open-remote-group
               client
               "room/a"
               "demo"
               {"transport_id" transport-id})
              (fn [group]
                (var model (xtd/get-in group ["models" "main"]))
                (return
                 (promise/x:promise-then
                  (browser-transport/disconnect conn)
                  (fn [_]
                    (repl/notify
                     {"groups" groups
                      "has_group" (xt/x:not-nil? group)
                      "model_type" (xt/x:get-key model "::")
                      "output" (event-model/get-current model nil)}))))))))))))
     (fn [err]
       (repl/notify {"error" err
                     "message" (xt/x:ex-message err)}))))
  => (contains-in
      {"groups" {"demo" {"models" ["main"]}}
       "has_group" true
       "model_type" "event.model"
       "output" {"value" "hello"}}))
