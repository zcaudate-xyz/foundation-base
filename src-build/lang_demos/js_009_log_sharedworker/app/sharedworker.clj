(ns lang-demos.js-009-log-sharedworker.app.sharedworker
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.db.node.client-base :as client-base]
             [xt.db.node.runtime :as db-runtime]
             [xt.db.system.impl-supabase-realtime :as impl-realtime]
             [xt.db.system.main :as db-main]
             [lang-demos.js-009-log-sharedworker.generated.runtime :as app-runtime]]})

(defn.js ensure-log-realtime
  [node]
  (var pending (substrate/get-service node "log/realtime-init"))
  (when pending
    (return pending))
  (var primary (substrate/get-service node "db/primary"))
  (when (not primary)
    (xt/x:err "The shared log database did not initialise."))
  (var connection-id "log-sharedworker-realtime")
  (var callback-id "log-sharedworker-realtime/notify")
  (impl-realtime/add-realtime-callback
   primary
   connection-id
   callback-id
   (fn [event]
     (when (and (== "realtime:scratch_v0:Log" (. event ["topic"]))
                (or (xt/x:has-key? event "db/sync")
                    (xt/x:has-key? event "db/remove")))
       (substrate/publish node
                          "log"
                          "log.changed"
                          {}
                          {}))))
  (var init
       (-> (client-base/subscribe-db
            node
            "db/primary"
            connection-id
            ["realtime:scratch_v0:Log"]
            {})
           (promise/x:promise-then
            (fn [results]
              (when (not (== true (. results [0])))
                (xt/x:err "Supabase Realtime could not subscribe to scratch_v0.Log."))
              (return true)))
           (promise/x:promise-catch
            (fn [error]
              (impl-realtime/remove-realtime-callback
               primary connection-id callback-id)
              (substrate/remove-service node "log/realtime-init")
              (xt/x:err (or (. error message)
                            "Supabase Realtime could not subscribe to scratch_v0.Log."))))))
  (substrate/set-service node "log/realtime-init" init)
  (return init))

(defn.js subscribe-log-handler
  [space args request node]
  (return (-/ensure-log-realtime node)))

(defn.js runtime-init
  []
  (var node (substrate/node-create {"id" "log-sharedworker-server"}))
  (substrate/set-service
   node
   "auth/supabase"
   (db-main/create-impl "supabase" app-runtime/SupabaseDefaults nil nil))
  (substrate/register-handler node "@js009/log-subscribe" -/subscribe-log-handler nil)
  (return (db-runtime/sharedworker-init-kernel node nil "log-sharedworker")))
