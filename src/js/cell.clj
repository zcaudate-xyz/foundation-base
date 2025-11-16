(ns js.cell
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]
             [xt.lang.event-view :as event-view]
             [js.cell.link-raw :as raw]
             [js.cell.impl-common :as impl-common]
             [js.cell.impl-model :as impl-model]
             [js.core :as j]]})

(defn.js make-cell
  "makes a current cell"
  {:added "4.0"}
  [worker-url]
  (var cell  (impl-common/new-cell worker-url))
  (impl-model/add-raw-callback cell)
  (return cell))


;;
;; STATE
;;

(defvar.js
  GD
  "gets the current cell"
  {:added "4.0"}
  [] (return nil))

(defvar.js
  GX
  "gets the current annex"
  {:added "4.0"}
  [] (return {}))

(defn.js GX-val
  "gets the current annex key"
  {:added "4.0"}
  [key]
  (return (k/get-key (-/GX) key)))

(defn.js GX-set
  "set the current annex key"
  {:added "4.0"}
  [key val]
  (k/set-key (-/GX) key val)
  (return val))

;;
;; METHOD
;;

(defn.js get-cell
  "gets the current cell"
  {:added "4.0"}
  [ctx]
  (cond (k/nil? ctx)
        (return (-/GD))
        
        (k/is-string? ctx)
        (return (-/GX-val ctx))

        (k/obj? ctx)
        (if (== (. ctx ["::"]) "cell")
          (return ctx)
          (return (. ctx ["cell"])))

        :else
        (throw "Type not Correct")))

;;
;; WRAPPERS
;;

(defn.js fn-call-cell
  "calls the cell in context"
  {:added "4.0"}
  [f args ctx]
  (var cell (-/get-cell ctx))
  (return (f cell (k/unpack args))))

(defn.js fn-call-model
  "calls the model in context"
  {:added "4.0"}
  [f model-id args ctx]
  (var cell (-/get-cell ctx))
  (return (f cell model-id (k/unpack args))))

(defn.js fn-call-view
  "calls the view in context"
  {:added "4.0"}
  [f path args ctx]
    (var cell (-/get-cell ctx))
    (var [model-id view-id] path)
    (return (f cell model-id view-id (k/unpack args))))

(defn.js fn-access-cell
  "calls access function on the current cell"
  {:added "4.0"}
  [f ctx]
  (var cell (-/get-cell ctx))
  (var #{models} cell)
  (return (k/obj-map models
                     (fn [model]
                       (var #{views} model)
                       (return (k/obj-map views f))))))

(defn.js fn-access-model
  "calls access function on the current model"
  {:added "4.0"}
  [f model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (k/obj-map views f))))

(defn.js fn-access-view
  "calls access function on the current view"
  {:added "4.0"}
  [f path args ctx]
  (var cell (-/get-cell ctx))
  (var [model-id view-id] path)
  (return (impl-common/view-access cell model-id view-id f args)))

(defn.js list-models
  "lists all models"
  {:added "4.0"}
  [ctx]
  (return (-/fn-call-cell impl-common/list-models [] ctx)))

(defn.js list-views
  "lists all views"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-cell impl-common/list-views [model-id] ctx)))

(defn.js get-model
  "gets the model in context"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-cell impl-common/model-get [model-id] ctx)))

(defn.js get-view
  "gets the view in context"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view k/identity path [] ctx)))

;;
;; FNS
;;

(defn.js cell-vals
  "gets all vals in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-current ctx)))

(defn.js cell-outputs
  "gets all output data in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-output ctx)))

(defn.js cell-inputs
  "gets all output data in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-input ctx)))

(defn.js cell-trigger
  "triggers a view given event"
  {:added "4.0"}
  [topic event ctx]
  (return (-/fn-call-cell impl-model/trigger-all [topic event] ctx)))

;;
;;
;;

(defn.js model-outputs
  "gets the model outputs"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-access-model event-view/get-output model-id ctx)))

(defn.js model-vals
  "gets model vals"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-access-model event-view/get-current model-id ctx)))

(defn.js model-is-errored
  "checks if model has errored"
  {:added "4.0"}
  [model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (k/arr-some (k/obj-vals views)
                        event-view/is-errored)))
  (return false))

(defn.js model-is-pending
  "checks if model is pending"
  {:added "4.0"}
  [model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (k/arr-some (k/obj-vals views)
                        event-view/is-pending)))
  (return false))

(defn.js add-model-attach
  "adds a model"
  {:added "4.0"}
  [model-id model-input ctx]
  (return (-/fn-call-model impl-model/add-model-attach model-id [model-input] ctx)))

(defn.js add-model
  "attaches a model"
  {:added "4.0"}
  [model-id model-input ctx]
  (return (-/fn-call-model impl-model/add-model model-id [model-input] ctx)))

(defn.js remove-model
  "removes a model from cell"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-model impl-model/remove-model model-id [] ctx)))

(defn.js model-update
  "calls update on a model"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-model impl-model/model-update model-id [] ctx)))

(defn.js model-trigger
  "triggers an event on the model"
  {:added "4.0"}
  [model-id topic event ctx]
  (return (-/fn-call-model impl-model/trigger-model model-id [topic event] ctx)))


;;
;;
;;

(defn.js view-success
  "gets the success value"
  {:added "4.0"}
  [path  ctx]
  (return (-/fn-access-view event-view/get-success
                            path
                            []
                            ctx)))

(defn.js view-val
  "gets the view val"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-current
                             path
                             []
                             ctx)))

(defn.js view-get-input
  "gets the view input"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-input
                             path
                             []
                             ctx)))

(defn.js view-get-output
  "gets the view output"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-output
                            path
                            []
                            ctx)))

(defn.js view-set-val
  "sets the view val"
  {:added "4.0"}
  [path val errored ctx]
  (return (-/fn-access-view event-view/set-output
                            path
                            [val errored]
                            ctx)))

(defn.js view-get-time-updated
  "gets updated"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-time-updated
                             path
                             []
                             ctx)))

(defn.js view-is-errored
  "gets the errored flag for view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/is-errored
                             path
                             []
                             ctx)))

(defn.js view-is-pending
  "gets pending"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/is-pending
                             path
                             []
                             ctx)))

(defn.js view-get-time-elapsed
  "gets the elapsed time"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-time-elapsed
                             path
                             []
                             ctx)))

(defn.js view-set-input
  "sets the view input"
  {:added "4.0"}
  [path current ctx]
  (return (-/fn-call-view impl-model/view-set-input
                          path
                           [current]
                           ctx)))

(defn.js view-refresh
  "refreshes the view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/refresh-view
                                     cell model-id view-id
                                     {}
                                     impl-model/refresh-view-dependents)))
                          path [] ctx)))

(defn.js view-update
  "updates the view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view impl-model/view-update path [] ctx)))

(defn.js view-ensure
  "ensures view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view impl-common/view-ensure path [] ctx)))

(defn.js view-call-remote
  "calls the remote function"
  {:added "4.0"}
  [path args save-output ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/remote-call
                                     cell model-id view-id
                                     args save-output
                                     impl-model/refresh-view-dependents)))
                          path [args save-output] ctx)))

(defn.js view-refresh-remote
  "refreshes the remote function"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/refresh-view-remote
                                     cell model-id view-id
                                     impl-model/refresh-view-dependents)))
                          path [] ctx)))

(defn.js view-trigger
  "triggers the view with an event"
  {:added "4.0"}
  [path topic event ctx]
  (return (-/fn-call-view impl-model/trigger-view path [topic event] ctx)))

;;
;;
;;

(defn.js view-for
  "gets the view after update"
  {:added "4.0"}
  [path ctx]
  (return (. (k/first (-/view-update path ctx))
             (then (fn []
                     (return (-/view-val path ctx)))))))

(defn.js view-for-input
  "gets the view after setting input"
  {:added "4.0"}
  [path input ctx]
  (return (. (k/first (-/view-set-input path input ctx))
             (then (fn []
                     (return (-/view-val path ctx)))))))

(defn.js get-val
  "gets the subview"
  {:added "4.0"}
  [path subpath ctx]
  (var out (-/view-val path ctx))
  (when (or (k/nil? out) (k/is-empty? subpath))
    (return out))
  (return (k/get-in out subpath)))

(defn.js get-for
  "gets the subview after update"
  {:added "4.0"}
  [path subpath ctx]
  (return (. (k/first (-/view-update path ctx))
             (then (fn []
                     (return (-/get-val path subpath ctx)))))))

(defn.js nil-view
  "sets view input to nil"
  {:added "4.0"}
  [path ctx]
  (return (-/view-for-input path nil ctx)))

(defn.js nil-model
  "sets all model inputs to nil"
  {:added "4.0"}
  [model-id ctx]
  (return (j/onAll (k/arr-map (-/list-views model-id ctx)
                              (fn [k]
                                (return (-/nil-view [model-id k] ctx)))))))

;;
;; LISTENERS
;;

(defn.js clear-listeners
  "clears all listeners"
  {:added "4.0"}
  [ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/clear-listeners cell)))

(defn.js add-listener
  "adds a cell listener"
  {:added "4.0"}
  [path listener-id f meta pred ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/add-listener cell path listener-id f meta pred)))

(defn.js remove-listener
  "removes a listener"
  {:added "4.0"}
  [path listener-id ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/remove-listener cell path listener-id)))

(defn.js list-listeners
  "lists view listeners"
  {:added "4.0"}
  [path ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/list-listeners cell path)))

(defn.js list-all-listeners
  "lists all listeners"
  {:added "4.0"}
  [path ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/list-all-listeners cell)))

;;
;; RAW CALLBACKS
;;

(defn.js add-raw-callback
  "adds a raw callback (for all events)"
  {:added "4.0"}
  [key pred handler ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/add-callback link key pred handler)))

(defn.js remove-raw-callback
  "removes a raw callback"
  {:added "4.0"}
  [key ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/remove-callback link key)))

(defn.js list-raw-callbacks
  "lists all raw calllbacks"
  {:added "4.0"}
  [ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/list-callbacks link)))

