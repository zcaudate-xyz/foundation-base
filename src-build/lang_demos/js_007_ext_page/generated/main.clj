(ns lang-demos.js-007-ext-page.generated.main
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [js.react :as r]
             [js.react.ext-model :as ext-model]
             [js.react.ext-table :as ext-table]
             [lang-demos.shared-log.actions :as log-actions]
             [lang-demos.shared-log.page :as log-page]
             [lang-demos.js-007-ext-page.generated.runtime :as app-runtime]]})

(defn.js App
  []
  (var node (r/const
             (app-runtime/install-rpc-service
              (substrate/node-create {"id" "ext-page-log-node"}))))
  (var context (r/const (app-runtime/live-context node)))
  (var table (r/const (app-runtime/table-impl)))
  (var [realtime-status set-realtime-status] (r/local "CONNECTING"))
  (var [realtime-error set-realtime-error] (r/local ""))
  (var view (ext-table/useRemoteView
             table
             "data"
             {"defaultArgs" []}
             context
             {}))
  (r/init []
    (var disposed false)
    (var cleanup nil)
    (var refresh
         (fn []
           (when (not disposed)
             (return
              (-> (ext-model/refresh-model view {})
                  (promise/x:promise-catch
                   (fn [_] (return nil))))))))
    (var on-focus (fn [] (refresh)))
    (var on-visibility-change
         (fn []
           (when (== "visible" (. document visibilityState))
             (refresh))))
    (. window (addEventListener "focus" on-focus))
    (. document (addEventListener "visibilitychange" on-visibility-change))
    (var connection-id
         (+ "js-007-log/"
            (. (Math.random)
               (toString 36)
               (substr 2 10))))
    (-> (app-runtime/subscribe-log
         node context connection-id
         (fn [_] (refresh)))
        (promise/x:promise-then
         (fn [stop]
           (if disposed
             (stop)
             (do
               (:= cleanup stop)
               (set-realtime-error "")
               (set-realtime-status "LIVE")))))
        (promise/x:promise-catch
         (fn [error]
           (when (not disposed)
             (set-realtime-status "OFFLINE")
             (set-realtime-error
              (or (. error message)
                  "Unable to subscribe to shared log updates.")))
           (return nil))))
    (return
     (fn []
       (:= disposed true)
       (. window (removeEventListener "focus" on-focus))
       (. document (removeEventListener "visibilitychange" on-visibility-change))
       (when cleanup
         (cleanup)))))
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
