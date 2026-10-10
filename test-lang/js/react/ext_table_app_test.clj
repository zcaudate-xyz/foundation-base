(ns js.react.ext-table-app-test
  (:require [lang.core :as l]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.client-supabase :as client-supabase]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.main :as db-main]
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]]})

(do
  (l/script- :postgres
    {:runtime :jdbc.client
     :require [[postgres.sample.scratch-v0 :as scratch-v0]
               [postgres.core :as pg]
               [postgres.core.supabase :as s]]
     :config {:host (-> local-min/+config+ :db :host)
              :port (-> local-min/+config+ :db :port)
              :user (-> local-min/+config+ :db :user)
              :pass (-> local-min/+config+ :db :password)
              :dbname (-> local-min/+config+ :db :database)
              :startup local-min/start-supabase
              :shutdown local-min/stop-supabase}
     :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})
  (defrun.pg __init__
    (s/grant-usage #{"scratch_v0"})))

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js SupabaseDefaults
  (xt/x:obj-assign
   (@! local-min/+config-supabase-anon+)
   {"token" (@! (-> local-min/+config+ :api :anon-key))}))

(def.js RuntimeConfig
  {"primary" {"type" "supabase"
              "defaults" -/SupabaseDefaults}
   "caching" {"type" "memory"
              "defaults" {}}})

(defn.js install-rpc-service
  [node]
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" -/SupabaseDefaults nil nil))
  (db-runtime/init-server node)
  (return node))

(defn.js live-context
  [node]
  (return {"node" node
           "runtime" {"config" -/RuntimeConfig
                      "schema" -/Schema
                      "lookup" -/SchemaLookup}}))

(defn.js table-impl
  []
  (return
   {"base"
    {"list" {"spec" ["Log" {"data" ["id" "message"]}]}
     "data" {"spec" ["Log" {"data" ["id" "message"]}]}}
    "call" {}
    "cached" {}}))

(defn.js log-query
  [message]
  (return
   {"defaultArgs" ["Log"
                   {"where" [{"message" message}]
                    "data" ["id" "message"]}]}))

(defn.js log-app
  [props]
  (var message (. props ["message"]))
  (var node
       (-/install-rpc-service
        (substrate/node-create {"id" "log-app-node"})))
  (var context (-/live-context node))
  (var view
       (ext-table/useRemoteView
        (-/table-impl)
        "data"
        (-/log-query message)
        context
        {}))
  (xt/x:set-key (. props ["state"]) "node" node)
  (xt/x:set-key (. props ["state"]) "view" view)
  (return (r/createElement "span" nil "log-app")))

^{:refer js.react.ext-table-app-test/log-query :added "4.1"}
(fact "filters the Log view to the appended message"
  (!.js
    (return (-/log-query "message")))
  => {"defaultArgs" ["Log"
                      {"where" [{"message" "message"}]
                       "data" ["id" "message"]}]})

^{:refer js.react.ext-table-app-test/log-app :added "4.1"
  :setup [(pg/t:delete scratch-v0/Log
            {:where {:message "ext-table-app/log-append-refresh"}})]
  :teardown [(pg/t:delete scratch-v0/Log
               {:where {:message "ext-table-app/log-append-refresh"}})]}
(fact "appends through the anonymous RPC and refreshes the mounted Log view"
  (helper-source/test
   (fn [props]
     (return (-/log-app props)))
   {"message" "ext-table-app/log-append-refresh"
    "state" {}}
   (fn [props document env]
     (var node (. props ["state"] ["node"]))
     (var view (. props ["state"] ["view"]))
     (var message (. props ["message"]))
     (return
      (-> (client-supabase/rpc-call
           node
           "auth/supabase"
           "log_append_public"
           {"i_message" message}
           {"headers" {"Content-Profile" "scratch_v0"
                       "Accept-Profile" "scratch_v0"}})
          (promise/x:promise-then
           (fn [rpc-row]
             (return
              (-> (ext-model/refresh-model view {})
                  (promise/x:promise-then
                   (fn [_]
                     (var rows (event-model/get-current view nil))
                     (return
                      {"root-id" (. env ["root"] ["id"])
                       "rpc" rpc-row
                       "rows" rows
                       "same-id" (== (. rpc-row ["id"])
                                     (. rows [0] ["id"]))})))))))
          (promise/x:promise-finally
           (fn []
             (return
              (client-base/kernel-teardown
               node
               "db/primary"
               {}))))))))
  => (contains-in
      {"root-id" "root"
       "rpc" {"id" string? "message" "ext-table-app/log-append-refresh"}
       "rows" [{"id" string? "message" "ext-table-app/log-append-refresh"}]
       "same-id" true}))
