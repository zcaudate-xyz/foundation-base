(ns lang-demos.js-009-log-sharedworker.generated.main
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]
             [lang-demos.shared-log.actions :as log-actions]
             [lang-demos.shared-log.page :as log-page]
             [lang-demos.js-009-log-sharedworker.generated.runtime :as app-runtime]]})

(defn.js LogContent
  [props]
  (var node (. props node))
  (var context (app-runtime/live-context node))
  (var table (app-runtime/table-impl))
  (var view (r/const
             (ext-table/makeRemoteView
              table
              "data"
              {"defaultArgs" []}
              context)))
  (var initial-fetch-ref (r/ref nil))
  (var refresh-timeout-ref (r/ref nil))
  (var [realtime-status set-realtime-status] (r/local "CONNECTING"))
  (var [realtime-error set-realtime-error] (r/local ""))
  (r/init []
    (var active true)
    (when (xt/x:nil? (. initial-fetch-ref current))
      (:= (. initial-fetch-ref current)
          (ext-model/refresh-model view {})))
    (var subscription-id "js-009-log-page")
    (var schedule-refresh
         (fn []
           (when (xt/x:nil? (. refresh-timeout-ref current))
             (:= (. refresh-timeout-ref current)
                 (setTimeout
                  (fn []
                    (:= (. refresh-timeout-ref current) nil)
                    (when active
                      (ext-model/refresh-model view {})))
                  100)))))
    (substrate/register-trigger
     node
     "log.changed"
     (fn [_space _stream _trigger-node]
       (when active
         (schedule-refresh)))
     nil)
    (-> (substrate/subscribe node "log" "log.changed" subscription-id {})
        (promise/x:promise-then
         (fn [_]
           (return
            (substrate/request node nil "@js009/log-subscribe" [] {}))))
        (promise/x:promise-then
         (fn [_]
           (when active
             (set-realtime-error "")
             (set-realtime-status "LIVE"))
           (return true)))
        (promise/x:promise-catch
         (fn [error]
           (when active
             (set-realtime-status "OFFLINE")
             (set-realtime-error
              (or (. error message)
                  "Unable to subscribe to shared log updates.")))
           (return nil))))
    (return
     (fn []
       (:= active false)
       (when (xt/x:not-nil? (. refresh-timeout-ref current))
         (clearTimeout (. refresh-timeout-ref current))
         (:= (. refresh-timeout-ref current) nil))
       (substrate/unregister-trigger node "log.changed")
       (-> (substrate/unsubscribe node "log" "log.changed" subscription-id {})
           (promise/x:promise-catch
            (fn [_] (return nil)))))))
  (var append
       (fn [message]
         (return (log-actions/append-log node message))))
  (var remove
       (fn [entry-id]
         (return (log-actions/delete-log node entry-id))))
  (return
   [:% log-page/LogPage {"view" view
                         "append" append
                         "remove" remove
                         "realtimeStatus" realtime-status
                         "realtimeError" realtime-error}]))

(defn.js App
  []
  (var node (r/const
             (substrate/node-create {"id" "log-sharedworker-client"})))
  (var [worker-state set-worker-state] (r/local nil))
  (var [worker-error set-worker-error] (r/local ""))
  (var startup-ref (r/ref nil))
  (var connection-ref (r/ref nil))
  (r/useEffect
   (fn []
     (var active true)
     (var pending (. startup-ref current))
     (when (xt/x:nil? pending)
       (:= pending (app-runtime/connect-client node))
       (:= (. startup-ref current) pending))
     (var timeout-id
          (setTimeout
           (fn []
             (when active
               (set-worker-error
                "SharedWorker startup is taking longer than expected. Check the worker and Supabase connection.")))
           15000))
     (. pending
        (then
         (fn [state]
           (clearTimeout timeout-id)
           (when active
             (:= (. connection-ref current) state)
             (set-worker-error "")
             (set-worker-state state))))
        (catch
         (fn [error]
           (clearTimeout timeout-id)
           (when active
             (set-worker-error
              (or (. error message)
                  "Unable to connect to the shared worker."))))))
     (return
      (fn []
        (:= active false)
        (clearTimeout timeout-id)
        (var state (. connection-ref current))
        (when state
          (:= (. connection-ref current) nil)
          (:= (. startup-ref current) nil)
          (. (app-runtime/disconnect-client state)
             (catch (fn [_] nil)))))))
   [])
  (when worker-state
    (return [:% -/LogContent {"node" node}]))
  (return
   [:% log-page/LogPageStatus
    {"status" (:? worker-error "OFFLINE" "CONNECTING")
     "error" worker-error
     "message" (:? worker-error
                 (+ "Unable to connect to the SharedWorker: " worker-error)
                 "The shared log is starting.")}]))
