(ns xt.db.poc.s09-kernel-parity-sql-test
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
             [xt.db.node.client-base :as client-base]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.runtime :as runtime]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(defn.js tree-view-model-dataview
  []
  (return {"table" "Log"
           "select_entry" {"input" []
                           "view" {"table" "Log"
                                   "type" "select"
                                   "query" {}}}
           "return_entry" {"input" []
                           "view" {"table" "Log"
                                   "type" "return"
                                   "query" ["id" "message"]}}}))

(defn.js tree-view-model
  []
  (return {"pipeline" {}
           "options" {}
           "defaults" {"select_args" []
                       "return_args" []}}))

(def +server-only-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "kernel-parity-sql-server-only"
                                           "spaces" {"room/a" {"state" {}}}}))
      (xt.db.node.runtime/sharedworker-init-kernel node "browser" "kernel-parity-sql-server-only")
      (xt.substrate/register-handler
       node "@xt.db/kernel-init"
       (fn [space args request node]
         (return
          (. (xt.db.node.kernel-base/kernel-init-main
              node
              (. args [0])
              (. args [1])
              (. args [2]))
             (then
              (fn [_]
                (xt.substrate.page-core/add-group-attach
                 node
                 "room/a"
                 "demo"
                 {"tree-view" (xt.db.node.kernel-base/dataview-create-model
                               "db/primary"
                               ((xt.db.poc.s09-kernel-parity-sql-test/tree-view-model-dataview))
                               ((xt.db.poc.s09-kernel-parity-sql-test/tree-view-model)))})
                (return {"status" "ok"})))
             (catch
              (fn [err]
                (return {"status" "error"
                         "message" (. err ["message"])
                         "stack" (. err ["stack"])}))))))
       nil))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(fact:global
 {
  :setup [(l/rt:restart :js)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log")
          (l/rt:scaffold-imports :js)
          (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                             4000)]
  :teardown [(l/rt:stop)]})

(defn.js connect-kernel-worker
  "connects a client to the shared worker and initialises the db adaptor on the client"
  {:added "4.1"}
  [client source]
  (return
   (runtime/sharedworker-connect client
                                 {"primary" {"id" "db/primary"
                                             "type" "supabase"
                                             "defaults" (@! local-min/+config-supabase-anon+)}
                                  "caching" {"id" "db/caching"
                                             "type" "sqlite"
                                             "defaults" {}}}
                                 -/Schema
                                 -/SchemaLookup
                                 source
                                 "transport-browser")))

(defn.js with-shared-worker
  "connects a client to a shared worker running the given server script"
  {:added "4.1"}
  [source callback]
  (var client (substrate/node-create {"id" "kernel-parity-sql-client"
                                      "spaces" {"room/a" {"state" {}}}}))
  (return
   (promise/x:promise-then
    (-/connect-kernel-worker client source)
    (fn [_]
      (return (callback client))))))

(defn.js read-tree-view-output
  "opens the proxy group and reads the current output of the tree-view model"
  {:added "4.1"}
  [client]
  (return
   (-> (page-proxy/open-proxy-group
        client
        "room/a"
        "demo"
        {})
       (promise/x:promise-then
        (fn [_]
          (return (base-page/remote-call client "room/a" "demo" "tree-view" [[] []] true))))
       (promise/x:promise-then
        (fn [_]
          (var group (base-page/group-get client "room/a" "demo"))
          (var model (xtd/get-in group ["models" "tree-view"]))
          (return {"has_group" (xt/x:not-nil? group)
                   "model_type" (xt/x:get-key model "::")
                   "output" (event-model/get-current model nil)}))))))

^{:refer xt.db.poc.s09-kernel-parity-sql-test/server-config-tree-view
  :added "4.1"
  :setup [(scratch-v0/log-append-public "parity-sql-server")]}
(fact "server config with sqlite caching: model attached directly on server reads postgres data"

  (notify/wait-on [:js 30000]
    (-/with-shared-worker
     (@! +server-only-script+)
     (fn [client]
       (return
        (-> (-/read-tree-view-output client)
            (promise/x:promise-then
             (fn [out]
               (repl/notify out)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                {"has_group" false
                 "error" (. err ["message"])
                 "stack" (. err ["stack"])}))))))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [{"message" "parity-sql-server"}]}))

^{:refer xt.db.poc.s09-kernel-parity-sql-test/client-server-config-tree-view
  :added "4.1"
  :setup [(scratch-v0/log-append-public "parity-sql-client")]}
(fact "client/server config with sqlite caching: model attached remotely reads postgres data"

  (notify/wait-on [:js 30000]
    (-/with-shared-worker
     nil
     (fn [client]
       (return
        (-> (client-base/dataview-attach-model
             client
             "db/primary"
             {"space_id" "room/a"
              "group_id" "demo"
              "model_id" "tree-view"}
             (-/tree-view-model-dataview)
             (-/tree-view-model)
             {})
            (promise/x:promise-then
             (fn [_]
               (return (-/read-tree-view-output client))))
            (promise/x:promise-then
             (fn [out]
               (repl/notify out)))
            (promise/x:promise-catch
             (fn [err]
               (repl/notify
                {"has_group" false
                 "error" (. err ["message"])
                 "stack" (. err ["stack"])}))))))))
  => (contains-in
      {"has_group" true
       "model_type" "event.model"
       "output" [{"message" "parity-sql-client"}]}))
