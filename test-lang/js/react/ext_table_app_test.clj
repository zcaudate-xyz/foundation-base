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
             [js.react.helper-jsdom :as helper]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]
             [lang-demos.js-007-ext-page.generated.runtime :as app-runtime]]})

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

(defn.js log-app
  [props]
  (var node (app-runtime/install-rpc-service
             (substrate/node-create {"id" "log-app-node"})))
  (var context (app-runtime/live-context node))
  (var view
       (ext-table/useRemoteView
        (app-runtime/table-impl)
        "data"
        {"defaultArgs" []}
        context
        {}))
  (xt/x:set-key (. props ["state"]) "node" node)
  (xt/x:set-key (. props ["state"]) "view" view)
  (return (r/createElement "span" nil "log-app")))

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
                       "same-id" (and rows
                                      (> rows.length 0)
                                      (== (. rpc-row ["id"])
                                          (. rows [0] ["id"])))})))))))
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
