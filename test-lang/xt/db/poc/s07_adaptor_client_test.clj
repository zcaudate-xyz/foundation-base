(ns xt.db.poc.s07-kernel-client-test
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
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as runtime]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js LogModelBasic
  {"table" "Log"
   "select_entry" {"input" []
                   "view" {"table" "Log"
                           "type" "select"
                           "query" {}}}
   "return_entry" {"input" []
                   "view" {"table" "Log"
                           "type" "return"
                           "query" ["id" "message"]}}})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(defn.js connect-kernel-worker
  "connects to the shared worker, initialises the db adaptor, and invokes callback"
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

^{:refer xt.db.poc.s07-kernel-client-test/dataview-attach-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "kernel-client-tree")]}
(fact "client can init adaptor, attach a remote tree-view model, and read postgres data"
  
  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (-> (client-base/dataview-attach-model
                 client
                 "db/primary"
                 {"space_id" "room/a"
                  "group_id" "demo"
                  "model_id" "tree-view"}
                 -/LogModelBasic
                 {"pipeline" {}
                  "options" {}
                  "defaults" {"select_args" []
                              "return_args" []}}
                 {})))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (base-page/model-remote-call client "room/a" "demo" "tree-view" [[] []] true))))
        (promise/x:promise-then
         (fn [_]
           (var group (base-page/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "tree-view"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [{"message" "kernel-client-tree"}]}))

^{:refer xt.db.poc.s07-kernel-client-test/attach-pull-model
  :added "4.1"
  :setup [(scratch-v0/log-append-public "kernel-client-pull")]}
(fact "client can init adaptor, attach a remote pull-view model, and read postgres data"


  (notify/wait-on [:js 5000]
    (var client (substrate/node-create {}))
    (-> (-/connect-kernel-worker client)
        (promise/x:promise-then
         (fn [_]
           (return
            (-> (client-base/pull-attach-model
                 client
                 "db/primary"
                 {"space_id" "room/a"
                  "group_id" "demo"
                  "model_id" "pull"}
                 ["Log"]
                 {"pipeline" {}
                  "options" {}
                  "defaults" {"args" []
                              "output" {}}}
                 {})))))
        (promise/x:promise-then
         (fn [_]
           (return
            (page-proxy/group-open-proxy client "room/a" "demo" {}))))
        (promise/x:promise-then
         (fn [_]
           (return
            (base-page/model-remote-call client "room/a" "demo" "pull" [["Log"]] true))))
        (promise/x:promise-then
         (fn [_]
           (var group (base-page/group-get client "room/a" "demo"))
           (var model (xtd/get-in group ["models" "pull"]))
           (repl/notify
            {"has_group" (xt/x:not-nil? group)
             "model_type" (xt/x:get-key model "::")
             "output" (event-model/get-current model nil)})))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify
            {"has_group" false
             "error" (. err ["message"])
             "stack" (. err ["stack"])})))))
  => (contains-in
      {"has_group" true,
       "output" [{"message" "tree", "author_id" nil, "id" string?}],
       "model_type" "event.model"}))
