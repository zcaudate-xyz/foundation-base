(ns js.cell-v3
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]
             [xt.lang.event-view :as event-view]
             [js.cell-v3.kernel.impl-link :as link]
             [js.cell-v3.kernel.impl-common :as impl-common]
             [js.cell-v3.kernel.impl-model :as impl-model]
             [js.core :as j]]})

(defn.js make-cell
  "makes a current cell-v3 runtime"
  {:added "4.0"}
  [link-source opts]
  (var cell (impl-common/new-cell link-source opts))
  (impl-model/add-raw-callback cell)
  (return cell))

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

(defn.js get-cell
  "gets the current cell"
  {:added "4.0"}
  [ctx]
  (cond (k/nil? ctx)
        (return (-/GD))

        (k/is-string? ctx)
        (return (-/GX-val ctx))

        (k/obj? ctx)
        (if (== (. ctx ["::"]) "cell-v3.cell")
          (return ctx)
          (return (. ctx ["cell"])))

        :else
        (throw "Type not Correct")))

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
  (var models (. cell ["models"]))
  (return (k/obj-map models
                     (fn [model]
                       (var views (. model ["views"]))
                       (return (k/obj-map views f))))))

(defn.js fn-access-model
  "calls access function on the current model"
  {:added "4.0"}
  [f model-id ctx]
  (var cell (-/get-cell ctx))
  (var model (impl-common/model-get cell model-id))
  (when model
    (var views (. model ["views"]))
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
  "gets all input data in the context"
  {:added "4.0"}
  [ctx]
  (return (-/fn-access-cell event-view/get-input ctx)))

(defn.js cell-trigger
  "triggers a view given event"
  {:added "4.0"}
  [topic event ctx]
  (return (-/fn-call-cell impl-model/trigger-all [topic event] ctx)))

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

(defn.js view-val
  "gets the view val"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-current path [] ctx)))

(defn.js view-get-input
  "gets the view input"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-input path [] ctx)))

(defn.js view-get-output
  "gets the view output"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-access-view event-view/get-output path [] ctx)))

(defn.js view-set-input
  "sets the view input"
  {:added "4.0"}
  [path current ctx]
  (return (-/fn-call-view impl-model/view-set-input path [current] ctx)))

(defn.js view-update
  "updates the view"
  {:added "4.0"}
  [path ctx]
  (return (-/fn-call-view impl-model/view-update path [] ctx)))

(defn.js view-trigger
  "triggers the view with an event"
  {:added "4.0"}
  [path topic event ctx]
  (return (-/fn-call-view impl-model/trigger-view path [topic event] ctx)))

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
  [ctx]
  (var cell (-/get-cell ctx))
  (return (impl-common/list-all-listeners cell)))

(defn.js add-raw-callback
  "adds a raw callback (for all events)"
  {:added "4.0"}
  [key pred handler ctx]
  (var cell (-/get-cell ctx))
  (var raw-link (. cell ["link"]))
  (return (link/add-callback raw-link key pred handler)))

(defn.js remove-raw-callback
  "removes a raw callback"
  {:added "4.0"}
  [key ctx]
  (var cell (-/get-cell ctx))
  (var raw-link (. cell ["link"]))
  (return (link/remove-callback raw-link key)))

(defn.js list-raw-callbacks
  "lists all raw callbacks"
  {:added "4.0"}
  [ctx]
  (var cell (-/get-cell ctx))
  (var raw-link (. cell ["link"]))
  (return (link/list-callbacks raw-link)))
