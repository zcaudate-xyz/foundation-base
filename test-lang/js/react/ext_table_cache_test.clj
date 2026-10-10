^{:seedgen/skip true}
(ns js.react.ext-table-cache-test
  (:use code.test)
  (:require [lang.core :as l]
            [js.react.helper-jsdom :as helper-source]
            [scaffold.supabase.local-min :as local-min]
            [postgres.core :as pg]
            [postgres.core.supabase :as s]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.db.node.client-base :as client-base]
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

(def.js Schema
  (@! (pg/bind-schema (:schema (pg/app "scratch_v0")))))

(def.js SchemaLookup
  (@! (pg/bind-app (pg/app "scratch_v0"))))

(def.js RuntimeConfig
  {"primary" {"type" "supabase"
              "defaults" (xt/x:obj-assign
                          (@! local-min/+config-supabase-service+)
                          {"token" (@! (-> local-min/+config+ :api :service-key))})}
   "caching" {"type" "memory"
              "defaults" {}}})

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

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)
          (l/rt:teardown :postgres)
          (l/rt:setup :postgres)
          (local-min/restart-postgrest)
          (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.ext-table/makeRemoteView
  :added "4.1"
  :eval false
  :setup [(pg/t:delete scratch-v0/Log
                       {:where {:message "ext-table-cache-jsdom"}})
          (pg/t:insert scratch-v0/Log
                       {:message "ext-table-cache-jsdom"}
                       {:track {}})]
  :teardown [(pg/t:delete scratch-v0/Log
                          {:where {:message "ext-table-cache-jsdom"}})]}
(fact "warms the same-node cache with a live pull inside jsdom"
  (let [message "ext-table-cache-jsdom"]
    (try
      (helper-source/test
       (fn [props]
         (var view
              (ext-table/makeRemoteView
               (. props ["impl"])
               "data"
               {"defaultArgs"
                ["Log"
                 {"where" [{"message" "ext-table-cache-jsdom"}]
                  "data" ["id" "message"]}]}
               (. props ["context"])))
         (xt/x:set-key (. props ["state"]) "view" view)
         (return (r/createElement "span" nil "ready")))
       (fn [_]
         (var node (substrate/node-create {"id" "ext-table-cache-jsdom"}))
         (return {"impl" (-/table-impl)
                  "context" (-/live-context node)
                  "state" {}}))
       (fn [props document env]
         (var node (. props ["context"] ["node"]))
         (var view (. props ["state"] ["view"]))
         (return
          (. (ext-model/refresh-model view {})
             (then
              (fn [_]
                (var remote-output (event-model/get-current view nil))
                (return
                 (. (client-base/pull-cached node "db/primary" ["Log"] {})
                    (then
                     (fn [cached-output]
                       (return {"root-id" (. env ["root"] ["id"])
                                "remote" remote-output
                                "cached" cached-output})))))))
             (finally
              (fn []
                (return
                 (client-base/kernel-teardown node "db/primary" {}))))))))
  => (contains-in
      {"root-id" "root"
       "remote" [{"id" string? "message" message}]
       "cached" [{"id" string? "message" message}]})
      (finally
        (pg/t:delete scratch-v0/Log {:where {:message message}})))))
