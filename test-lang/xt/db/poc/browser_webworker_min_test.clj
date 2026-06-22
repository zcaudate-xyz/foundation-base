(ns xt.db.poc.browser-webworker-min-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [xt.substrate]
            [xt.substrate.transport-browser]
            [xt.substrate.page-remote]
            [xt.substrate.page-core]
            [xt.event.base-model]
            [xt.lang.common-repl]
            [xt.lang.common-data :as xtd]))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [js.worker.link :as worker-link]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-remote :as page-remote]
             [xt.db.node.adaptor-base]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(def +worker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "db-model-server"}))
      (xt.substrate.page-remote/install node)
      (xt.substrate/register-handler
       node "@/echo"
       (fn [space args request node]
         (return {"echo" args}))
       nil)
      (. (xt.db.node.adaptor-base/init-db
          node
          {"primary" {"type" "memory" "defaults" {}}
           "caching" {"type" "memory" "defaults" {}}}
          {} {})
         (then
          (fn [node]
            (xt.substrate.page-core/add-group
             node
             "room/a"
             "demo"
             {"main" {"defaults" ["hello"]
                      "handler" (fn [ctx]
                                  (var data (xt.lang.common-data/get-in ctx ["input" "data"]))
                                  (return {"value" (xt.lang.spec-base/x:first data)}))
                      "options" {}}})
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" self
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "db-model-server"}}))))
         (catch
             (fn [err]
               (return err)))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(fact "webworker page-remote baseline"
  (notify/wait-on [:js 10000]
                  (var client (substrate/node-create {"id" "db-model-client"}))
                  (page-remote/install client)
                  (promise/x:promise-catch
                   (promise/x:promise-then
                    (browser-transport/connect-worker
                     client
                     {"transport_id" "worker"
                      "source" (worker-link/make-webworker-link (@! +worker-script+))})
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
                                     (repl/notify
                                      {"groups" groups
                                       "output" (event-model/get-current model nil)}))))))))))
                   (fn [err]
                       (repl/notify {"error" err
                                     "message" (xt/x:ex-message err)}))))
  => (contains-in
      [{"groups" {"demo" {"models" ["main"]}}
        "output" {"value" "hello"}}]))
