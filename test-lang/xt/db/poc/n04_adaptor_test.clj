(ns xt.db.poc.n04-kernel-test
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [xt.lang.common-notify :as notify]
            [scaffold.supabase.local-min :as local-min]
            [xt.substrate]
            [xt.substrate.page-core]
            [xt.substrate.page-proxy]
            [xt.substrate.transport-browser]
            [postgres.core :as pg]
            [xt.db.node.kernel-base]
            [xt.db.node.runtime]))

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
                                  "caching" {"type" "sqlite"
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

^{:refer xt.db.node.kernel-base/kernel-init-main
  :added "4.1"
  :setup [(scratch-v0/log-append-public "remote")]}
(fact "SharedWorker sqlite init exposes a remote pull model"

  (notify/wait-on [:js 15000]
    (-/with-kernel-worker
     (fn [client]
       (return
        (-> (client-base/pull-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "entry"}
             ["Log"]
             {"pipeline" {}
              "options" {}
              "defaults" {"args" [["Log"]]}}
             {})
            (promise/x:promise-then
             (fn [_]
               (return
                (page-proxy/group-open-proxy client "room/a" "demo" {}))))
            (promise/x:promise-then
             (fn [_]
               (return
                (base-page/model-remote-call client "room/a" "demo" "entry" [["Log"]] true))))
            (promise/x:promise-then
             (fn [_]
               (var group (base-page/group-get client "room/a" "demo"))
               (var model (xtd/get-in group ["models" "entry"]))
               (repl/notify
                {"has_group" (xt/x:not-nil? group)
                 "model_type" (xt/x:get-key model "::")
                 "output" (event-model/get-current model nil)})))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                {"has_group" false
                 "error" (. err ["message"])
                 "stack" (. err ["stack"])}))))))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [{"message" "remote"}]}))
