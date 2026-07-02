(ns xt.db.poc.n03-webworker-custom-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]))

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host   (-> local-min/+config+ :db :host)
              :port   (-> local-min/+config+ :db :port)
              :user   (-> local-min/+config+ :db :user)
              :pass   (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup  local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.kernel-base]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(fact:global
 {
  :setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(def +worker-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "db-model-server"}))
      (xt.substrate.page-proxy/install node)
      (xt.substrate/register-handler
       node "@/echo"
       (fn [space args request node]
         (return {"echo" args}))
       nil)
      (var group (xt.substrate.page-core/add-group
                  node
                  "room/a"
                  "demo"
                  {"main" {"defaults" {"args" ["hello"]
                                       "output" {}
                                       "process" (fn [x] (return x))}
                           "handler" (fn [ctx]
                                       (var data (xt.lang.common-data/get-in ctx ["input" "data"]))
                                       (return {"value" (xt.lang.spec-base/x:first data)}))
                           "options" {"trigger" true}}}))
      (. (xt.lang.spec-base/x:get-key group "init")
         (then
          (fn [_]
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" self
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "db-model-server"}}))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

^{:refer xt.db.poc.n03-webworker-custom-test/attach-pull-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "client can open a remote pull-view model and read its output"

  (notify/wait-on [:js 20000]
                  (var client (substrate/node-create {"id" "db-model-client"}))
                  (page-proxy/install client)
                  (promise/x:promise-catch
                   (promise/x:promise-then
                    (browser-transport/connect-worker
                     client
                     {"transport_id" "worker"
                      "source" (browser-transport/webworker-source (@! +worker-script+))})
                    (fn [conn]
                        (var transport-id (. conn ["transport_id"]))
                        (return
                         (promise/x:promise-then
                          (substrate/request client "room/a" "@/echo" [[1 2 3]] {"transport_id" transport-id})
                          (fn [echo]
                              (return
                               (promise/x:promise-then
                                (page-proxy/list-proxy-groups client "room/a" {"transport_id" transport-id})
                                (fn [groups]
                                    (return
                                     (promise/x:promise-then
                                      (page-proxy/open-proxy-group
                                       client
                                       "room/a"
                                       "demo"
                                       {"transport_id" transport-id})
                                      (fn [group]
                                          (var model (xtd/get-in group ["models" "main"]))
                                          (return
                                           (repl/notify
                                            {"echo" echo
                                             "groups" groups
                                             "has_group" (xt/x:not-nil? group)
                                             "output" (event-model/get-current model nil)})))))))))))))
                   (fn [err]
                       (repl/notify {"error" err
                                     "message" (xt/x:ex-message err)}))))
  => (contains-in
      {"echo" {"echo" [[1 2 3]]}
       "groups" {"demo" {"models" ["main"]}}
       "has_group" true
       "output" {"value" "hello"}}))
