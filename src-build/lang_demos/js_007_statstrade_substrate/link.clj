(ns lang-demos.js-007-statstrade-substrate.link
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.event.base-listener :as listener]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.spec-promise :as promise]]})

(defn.js call
  "the statslink request boundary, using a substrate context"
  [context route args]
  (return (. (substrate/request (. context node) (. context space) route args
                                {:transport_id (. context transport_id)})
             (catch (fn [err]
                      (when (. err error)
                        (throw (new Error (. err error message))))
                      (throw err))))))

(defn.js action-fn
  "preserves statslink's input -> wrapped function -> output contract"
  [action context]
  (var f ((or (. action wrap) k/identity) (. action fn)))
  (var input-fn (or (. action input) k/identity))
  (var output-fn (or (. action output) k/identity))
  (return
   (fn [...args]
     (return (. (promise/x:promise
                 (fn []
                   (return (xt/x:apply f (input-fn [...args] context)))))
                (then output-fn))))))

(defn.js make-actions
  [impl context]
  (return (Object.fromEntries
           (. (Object.entries (. impl actions))
              (map (fn [[key action]]
                     (return [key (-/action-fn action context)])))))))

(defn.js event-key
  [space signal]
  (return (JSON.stringify [space signal])))

(defn.js install-event-bridge
  "one substrate trigger fans out to any number of listeners in each space"
  [node signal]
  (return
   (substrate/register-trigger
    node signal
    (fn [space frame local-node]
      (return (listener/trigger-keyed-listeners
               local-node (-/event-key (. space id) signal) frame)))
    {})))

(defn.js addRawListener
  "returns a cleanup function; transport subscription belongs to bootstrap"
  [event-sync callback context listener-id]
  (var #{node space} context)
  (var key (-/event-key space (. event-sync signal)))
  (listener/add-keyed-listener
   node key listener-id "raw"
   (fn [_ frame]
     (return (callback frame)))
   {} (. event-sync check))
  (return (fn [] (listener/remove-keyed-listener node key listener-id))))
