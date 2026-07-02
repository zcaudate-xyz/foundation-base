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
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.event.base-model :as event-model]
             [xt.db.node.kernel-client :as kernel-client]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def +server-only-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "kernel-parity-sql-server-only"
                                           "spaces" {"room/a" {"state" {}}}}))
      (xt.db.node.kernel-base/init-handlers node)
      (xt.substrate.page-proxy/install node)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (return
             (. (xt.db.node.kernel-base/init-base-main
                 node
                 {"primary" {"id" "db/primary"
                             "type" "supabase"
                             "defaults" (@! local-min/+config-supabase-anon+)}
                  "caching" {"id" "db/caching"
                             "type" "sqlite"
                             "defaults" {}}}
                 xt.db.poc.s09-kernel-parity-sql-test/Schema
                 xt.db.poc.s09-kernel-parity-sql-test/SchemaLookup)
                (then
                 (fn [_]
                   (xt.substrate.page-core/add-group-attach
                    node
                    "room/a"
                    "demo"
                    {"tree-view" (xt.db.node.kernel-base/create-tree-view-model
                                  {"caching_id" "db/caching"
                                   "primary_id" "db/primary"}
                                  {"table" "Log"
                                   "select_entry" {"input" []
                                                   "view" {"table" "Log"
                                                           "type" "select"
                                                           "query" {}}}
                                   "return_entry" {"input" []
                                                   "view" {"table" "Log"
                                                           "type" "return"
                                                           "query" ["id" "message"]}}
                                   "pipeline" {}
                                   "options" {}
                                   "defaults" {"select_args" []
                                               "return_args" []}})})
                   (return
                    (xt.substrate.transport-browser/boot-self
                     node
                     {"transport_id" "host"
                      "target" port
                      "ready" {"signal" "ready"
                               "transport" "browser"
                               "worker" "kernel-parity-sql-server-only"}}))))
                (catch
                    (fn [err]
                      (. port (postMessage {"type" "error"
                                            "stage" "init"
                                            "message" (. err ["message"])
                                            "stack" (. err ["stack"])})))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(def +client-server-script+
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "kernel-parity-sql-client-server"
                                           "spaces" {"room/a" {"state" {}}}}))
      (xt.db.node.kernel-base/init-handlers node)
      (xt.substrate.page-proxy/install node)
      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (return
             (. (xt.db.node.kernel-base/init-base-main
                 node
                 {"primary" {"id" "db/primary"
                             "type" "supabase"
                             "defaults" (@! local-min/+config-supabase-anon+)}
                  "caching" {"id" "db/caching"
                             "type" "sqlite"
                             "defaults" {}}}
                 xt.db.poc.s09-kernel-parity-sql-test/Schema
                 xt.db.poc.s09-kernel-parity-sql-test/SchemaLookup)
                (then
                 (fn [_]
                   (return
                    (xt.substrate.transport-browser/boot-self
                     node
                     {"transport_id" "host"
                      "target" port
                      "ready" {"signal" "ready"
                               "transport" "browser"
                               "worker" "kernel-parity-sql-client-server"}}))))
                (catch
                    (fn [err]
                      (. port (postMessage {"type" "error"
                                            "stage" "init"
                                            "message" (. err ["message"])
                                            "stack" (. err ["stack"])})))))))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                      "pg"
                      "data:text/javascript,export default {Client: function() {}}"}}}))

(defn.js tree-view-model-args
  []
  (return {"table" "Log"
           "select_entry" {"input" []
                           "view" {"table" "Log"
                                   "type" "select"
                                   "query" {}}}
           "return_entry" {"input" []
                           "view" {"table" "Log"
                                   "type" "return"
                                   "query" ["id" "message"]}}
           "pipeline" {}
           "options" {}
           "defaults" {"select_args" []
                       "return_args" []}}))

(defn.js with-shared-worker
  "connects a client to a shared worker running the given server script"
  {:added "4.1"}
  [script callback]
  (var client (substrate/node-create {"id" "kernel-parity-sql-client"
                                      "spaces" {"room/a" {"state" {}}}}))
  (page-proxy/install client)
  (return
   (promise/x:promise-then
    (browser-transport/connect-sharedworker
     client
     {"transport_id" "worker"
      "source" (browser-transport/sharedworker-source script {"type" "module"})})
    (fn [conn]
      (var transport-id (. conn ["transport_id"]))
      (return (callback client transport-id))))))

(defn.js read-tree-view-output
  "opens the proxy group and reads the current output of the tree-view model"
  {:added "4.1"}
  [client transport-id]
  (return
   (-> (page-proxy/open-proxy-group
        client
        "room/a"
        "demo"
        {"transport_id" transport-id})
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

^{:refer xt.db.poc.s09-kernel-parity-sql-test/server-config-tree-view
  :added "4.1"
  :setup [(scratch-v0/log-append-public "parity-sql-server")]}
(fact "server config with sqlite caching: model attached directly on server reads postgres data"

  (notify/wait-on [:js 30000]
    (-/with-shared-worker
     (@! +server-only-script+)
     (fn [client transport-id]
       (return
        (-> (-/read-tree-view-output client transport-id)
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
     (@! +client-server-script+)
     (fn [client transport-id]
       (return
        (-> (kernel-client/attach-tree-view-model
             client
             "room/a"
             "demo"
             "tree-view"
             "db/primary"
             (-/tree-view-model-args)
             {"transport_id" transport-id})
            (promise/x:promise-then
             (fn [_]
               (return (-/read-tree-view-output client transport-id))))
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
