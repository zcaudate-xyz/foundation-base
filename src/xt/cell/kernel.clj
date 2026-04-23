(ns xt.cell.kernel
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.common-runtime :as rt :with [defvar.xt]]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-task :as task]
             [xt.lang.event-view :as event-view]
             [xt.cell.kernel.base-link :as raw]
             [xt.cell.kernel.base-impl :as impl-common]
             [xt.cell.kernel.base-model :as impl-model]]})

(defn.xt task-all
  "waits for a collection of tasks in order"
  {:added "4.0"}
  [tasks]
  (return (impl-model/task-all tasks)))

(defn.xt make-cell
  "makes a current cell"
  {:added "4.0"}
  [worker-url]
  (var cell  (impl-common/new-cell worker-url))
  (impl-model/add-raw-callback cell)
  (return cell))


;;
;; STATE
;;

(defvar.xt
  GD
  "gets the current cell"
  {:added "4.0"}
  [] (return nil))

(defvar.xt
  GX
  "gets the current annex"
  {:added "4.0"}
  [] (return {}))

(defn.xt GX-val
  "gets the current annex key"
  {:added "4.0"}
  [key]
  (return (xt/x:get-key (-/GX) key)))

(defn.xt GX-set
  "set the current annex key"
  {:added "4.0"}
  [key val]
  (xt/x:set-key (-/GX) key val)
  (return val))

;;
;; METHOD
;;

(defn.xt get-cell
  "gets the current cell"
  {:added "4.0"}
  [ctx]
  (cond (xt/x:nil? ctx)
        (return (-/GD))
        
        (xt/x:is-string? ctx)
        (return (-/GX-val ctx))

        (xt/x:is-object? ctx)
        (if (== (. ctx ["::"]) "cell")
          (return ctx)
          (return (. ctx ["cell"])))

        :else
        (throw "Type not Correct")))

(defn.xt call
  "conducts a raw call against a cell or link"
  {:added "4.0"}
  [client event]
  (return (impl-common/call client event)))

;;
;; WRAPPERS
;;

(defn.xt fn-call-cell
  "calls the cell in context"
  {:added "4.0"}
  [f args ctx]
  (var cell (-/get-cell ctx))
  (return (f cell (xt/x:unpack args))))

(defn.xt fn-call-model
  "calls the model in context"
  {:added "4.0"}
  [f model-id args ctx]
  (var cell (-/get-cell ctx))
  (return (f cell model-id (xt/x:unpack args))))

(defn.xt fn-call-view
  "calls the view in context"
  {:added "4.0"}
  [f path args ctx]
    (var cell (-/get-cell ctx))
    (var [model-id view-id] path)
    (return (f cell model-id view-id (xt/x:unpack args))))

(defn.xt fn-access-cell
  "calls access function on the current cell"
  {:added "4.0"}
  [f ctx]
  (var cell (-/get-cell ctx))
  (var #{models} cell)
  (return (xtd/obj-map models
                     (fn [model]
                       (var #{views} model)
                       (return (xtd/obj-map views f))))))

(defn.xt fn-access-model
  "calls access function on the current model"
  {:added "4.0"}
  [f model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (xtd/obj-map views f))))

(defn.xt fn-access-view
  "calls access function on the current view"
  {:added "4.0"}
  [f path args ctx]
  (var cell (-/get-cell ctx))
  (var [model-id view-id] path)
  (return (impl-common/view-access cell model-id view-id f args)))

(defn.xt list-models
  "lists all models"
  {:added "4.0"}
  [ctx]
  (return (-/fn-call-cell impl-common/list-models [] ctx)))

(defn.xt list-views
  "lists all views"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-cell impl-common/list-views [model-id] ctx)))

(defn.xt get-model
  "gets the model in context"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-cell impl-common/model-get [model-id] ctx)))

(defn.xt get-view
  "gets the view in context"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view (fn [x] (return x)) path [] ctx)))

;;
;; FNS
;;

(defn.xt cell-vals
  "gets all vals in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-current ctx)))

(defn.xt cell-outputs
  "gets all output data in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-output ctx)))

(defn.xt cell-inputs
  "gets all output data in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-input ctx)))

(defn.xt cell-trigger
  "triggers a view given event"
  {:added "4.0"}
  [topic event ctx]
  (return (-/fn-call-cell impl-model/trigger-all [topic event] ctx)))

;;
;;
;;

(defn.xt model-outputs
  "gets the model outputs"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-access-model event-view/get-output model-id ctx)))

(defn.xt model-vals
  "gets model vals"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-access-model event-view/get-current model-id ctx)))

(defn.xt model-is-errored
  "checks if model has errored"
  {:added "4.0"}
  [model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (xt/x:arr-some (xtd/obj-vals views)
                         event-view/is-errored)))
  (return false))

(defn.xt model-is-pending
  "checks if model is pending"
  {:added "4.0"}
  [model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var #{views} model)
    (return (xt/x:arr-some (xtd/obj-vals views)
                         event-view/is-pending)))
  (return false))

(defn.xt add-model-attach
  "adds a model"
  {:added "4.0"}
  [model-id model-input ctx]
  (return (-/fn-call-model impl-model/add-model-attach model-id [model-input] ctx)))

(defn.xt add-model
  "attaches a model"
  {:added "4.0"}
  [model-id model-input ctx]
  (return (-/fn-call-model impl-model/add-model model-id [model-input] ctx)))

(defn.xt remove-model
  "removes a model from cell"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-model impl-model/remove-model model-id [] ctx)))

(defn.xt model-update
  "calls update on a model"
  {:added "4.0"}
  [model-id ctx]
  (return (-/fn-call-model impl-model/model-update model-id [] ctx)))

(defn.xt model-trigger
  "triggers an event on the model"
  {:added "4.0"}
  [model-id topic event ctx]
  (return (-/fn-call-model impl-model/trigger-model model-id [topic event] ctx)))


;;
;;
;;

(defn.xt view-success
  "gets the success value"
  {:added "4.0"}
  [path  ctx]
  (return (-/fn-access-view event-view/get-success
                            path
                            []
                            ctx)))

(defn.xt view-val
  "gets the view val"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-current
                             path
                             []
                             ctx)))

(defn.xt view-get-input
  "gets the view input"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-input
                             path
                             []
                             ctx)))

(defn.xt view-get-output
  "gets the view output"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-output
                            path
                            []
                            ctx)))

(defn.xt view-set-val
  "sets the view val"
  {:added "4.0"}
  [path val errored ctx]
  (return (-/fn-access-view event-view/set-output
                            path
                            [val errored]
                            ctx)))

(defn.xt view-get-time-updated
  "gets updated"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-time-updated
                             path
                             []
                             ctx)))

(defn.xt view-is-errored
  "gets the errored flag for view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/is-errored
                             path
                             []
                             ctx)))

(defn.xt view-is-pending
  "gets pending"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/is-pending
                             path
                             []
                             ctx)))

(defn.xt view-get-time-elapsed
  "gets the elapsed time"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-time-elapsed
                             path
                             []
                             ctx)))

(defn.xt view-set-input
  "sets the view input"
  {:added "4.0"}
  [path current ctx]
  (return (-/fn-call-view impl-model/view-set-input
                          path
                           [current]
                           ctx)))

(defn.xt view-refresh
  "refreshes the view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/refresh-view
                                     cell model-id view-id
                                     {}
                                     impl-model/refresh-view-dependents)))
                          path [] ctx)))

(defn.xt view-update
  "updates the view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view impl-model/view-update path [] ctx)))

(defn.xt view-ensure
  "ensures view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view impl-common/view-ensure path [] ctx)))

(defn.xt view-call-remote
  "calls the remote function"
  {:added "4.0"}
  [path args save-output ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/remote-call
                                     cell model-id view-id
                                     args save-output
                                     impl-model/refresh-view-dependents)))
                          path [args save-output] ctx)))

(defn.xt view-refresh-remote
  "refreshes the remote function"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view (fn [cell model-id view-id]
                            (return (impl-model/refresh-view-remote
                                     cell model-id view-id
                                     impl-model/refresh-view-dependents)))
                          path [] ctx)))

(defn.xt view-trigger
  "triggers the view with an event"
  {:added "4.0"}
  [path topic event ctx]
  (return (-/fn-call-view impl-model/trigger-view path [topic event] ctx)))

;;
;;
;;

(defn.xt view-for
  "gets the view after update"
  {:added "4.0"}
  [path ctx]
  (return
   (task/task-then
    (xt/x:first (-/view-update path ctx))
    (fn [_]
      (return (-/view-val path ctx))))))

(defn.xt view-for-input
  "gets the view after setting input"
  {:added "4.0"}
  [path input ctx]
  (return
   (task/task-then
    (xt/x:first (-/view-set-input path input ctx))
    (fn [_]
      (return (-/view-val path ctx))))))

(defn.xt get-val
  "gets the subview"
  {:added "4.0"}
  [path subpath ctx]
  (var out (-/view-val path ctx))
  (when (or (xt/x:nil? out) (xtd/is-empty? subpath))
    (return out))
  (return (xtd/get-in out subpath)))

(defn.xt get-for
  "gets the subview after update"
  {:added "4.0"}
  [path subpath ctx]
  (return
   (task/task-then
    (xt/x:first (-/view-update path ctx))
    (fn [_]
      (return (-/get-val path subpath ctx))))))

(defn.xt nil-view
  "sets view input to nil"
  {:added "4.0"}
  [path ctx]
  (return (-/view-for-input path nil ctx)))

(defn.xt nil-model
  "sets all model inputs to nil"
  {:added "4.0"}
  [model-id ctx]
  (return (-/task-all
           (xt/x:arr-map (-/list-views model-id ctx)
                      (fn [k]
                        (return (-/nil-view [model-id k] ctx)))))))

;;
;; LISTENERS
;;

(defn.xt clear-listeners
  "clears all listeners"
  {:added "4.0"}
  [ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/clear-listeners cell)))

(defn.xt add-listener
  "adds a cell listener"
  {:added "4.0"}
  [path listener-id f meta pred ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/add-listener cell path listener-id f meta pred)))

(defn.xt remove-listener
  "removes a listener"
  {:added "4.0"}
  [path listener-id ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/remove-listener cell path listener-id)))

(defn.xt list-listeners
  "lists view listeners"
  {:added "4.0"}
  [path ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/list-listeners cell path)))

(defn.xt list-all-listeners
  "lists all listeners"
  {:added "4.0"}
  [path ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/list-all-listeners cell)))

;;
;; RAW CALLBACKS
;;

(defn.xt add-raw-callback
  "adds a raw callback (for all events)"
  {:added "4.0"}
  [key pred handler ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/add-callback
           link
           key
           pred
           (fn [event signal]
             (var out (xtd/obj-assign {} event))
             (xt/x:del-key out "signal")
             (xt/x:set-key out "topic" signal)
             (return
              (handler
               out
               signal))))))

(defn.xt remove-raw-callback
  "removes a raw callback"
  {:added "4.0"}
  [key ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/remove-callback link key)))

(defn.xt list-raw-callbacks
  "lists all raw calllbacks"
  {:added "4.0"}
  [ctx]
  (var cell (-/get-cell ctx))
  (var #{link} cell)
  (return (raw/list-callbacks link)))
