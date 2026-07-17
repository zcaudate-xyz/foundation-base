^{:seedgen/skip true}
(ns xt.db.poc.s03-shared-rpc-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]))

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
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]]})

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

(defn.js connect-kernel-worker
  "connects to the shared worker and initialises the db adaptor on the client"
  {:added "4.1"}
  [client]
  (return
   (runtime/sharedworker-connect client
                                 {"primary" {"type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"type" "memory"
                                             "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup)))

(defn.js with-kernel-worker
  "connects a client to the shared worker and invokes callback"
  {:added "4.1"}
  [callback]
  (var client (substrate/node-create {"id" "db-model-client"}))
  (return
   (promise/x:promise-then
    (-/connect-kernel-worker client)
    (fn [_]
      (return (callback client))))))

^{:refer xt.db.poc.s03-shared-rpc-test/attach-rpc-model
  :added "4.1"}
(fact "client can open a remote rpc model and read the rpc result"

  (notify/wait-on [:js 20000]
    (-/with-kernel-worker
     (fn [client]
       (return
        (promise/x:promise-catch
         (-> (client-base/rpc-attach-model
              client
              "db/primary"
              {"space_id" "room/a"
               "group_id" "demo"
               "model_id" "rpc-view"}
              {"input" []
               "return" "text"
               "schema" "scratch_v0"
               "id" "ping"
               "flags" {}}
              {"pipeline" {}
               "options" {}
               "defaults" {"fn_args" []}}
              {})
             (promise/x:promise-then
              (fn [_]
                (return
                 (page-proxy/group-open-proxy client "room/a" "demo" {}))))
             (promise/x:promise-then
              (fn [group]
                (var model (xtd/get-in group ["models" "rpc-view"]))
                (return
                 (promise/x:promise-then
                  (base-page/model-remote-call client "room/a" "demo" "rpc-view" [[]] true)
                  (fn [res]
                    (return
                     (promise/x:with-delay
                      1000
                      (fn []
                        (repl/notify {"result" res
                                      "output" (event-model/get-current model nil)}))))))))))
         (fn [err]
           (repl/notify {"error" err
                         "message" (xt/x:ex-message err)})))))))
  => (contains-in
      {"output" "pong"}))
