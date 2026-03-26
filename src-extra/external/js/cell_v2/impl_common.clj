(ns js.cell-v2.impl-common
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]
             [js.cell-v2.event :as event]
             [js.cell-v2.link :as link]]})

(defn.js new-cell-init
  "creates a record for asynchronous resolve"
  {:added "4.0"}
  []
  (var init {})
  (var init-state (new Promise
                     (fn [resolve reject]
                       (k/obj-assign init {:resolve resolve
                                           :reject reject}))))
  (k/set-key init "current" init-state)
  (return init))

(defn.js create-link
  "creates or normalizes a cell-v2 link source"
  {:added "4.0"}
  [link-source opts]
  (:= opts (or opts {}))
  (when (== "cell-v2.link" (k/get-key link-source "::"))
    (return link-source))
  (when (. opts ["legacy"])
    (return (link/make-legacy-worker-link link-source opts)))
  (return (link/make-worker-link link-source opts)))

(defn.js new-cell
  "makes the core link-backed runtime container"
  {:added "4.0" :adopt true}
  [link-source opts]
  (var link (-/create-link link-source opts))
  (var init (-/new-cell-init))
  (link/add-callback
   link
   event/EV_INIT
   event/EV_INIT
   (fn [data signal inner-link]
     (link/remove-callback inner-link event/EV_INIT)
     (. init (resolve true))))
  (return
   (event-common/blank-container
    "cell-v2.cell"
    {:id (. link ["id"])
     :link link
     :models {}
     :init (k/get-key init "current")})))

(defn.js list-models
  "lists all models"
  {:added "4.0"}
  [cell]
  (var models (. cell ["models"]))
  (return (k/obj-keys models)))

(defn.js call
  "conducts a call, either for a link or cell"
  {:added "4.0"}
  [client event]
  (var t (k/get-key client "::"))
  (when (== t "cell-v2.link")
    (return (link/call client event)))
  (when (== t "cell-v2.cell")
    (return (link/call (k/get-key client "link") event))))

(defn.js model-get
  "gets a model"
  {:added "4.0"}
  [cell model-id]
  (var models (. cell ["models"]))
  (return (k/get-key models model-id)))

(defn.js model-ensure
  "throws an error if model is not present"
  {:added "4.0"}
  [cell model-id]
  (var model (-/model-get cell model-id))
  (when (k/nil? model)
    (k/err (k/cat "ERR - Model not found - " model-id)))
  (return model))

(defn.js list-views
  "lists views in the model"
  {:added "4.0"}
  [cell model-id]
  (var model (-/model-ensure cell model-id))
  (var views (. model ["views"]))
  (return (k/obj-keys views)))

(defn.js view-ensure
  "gets the view"
  {:added "4.0"}
  [cell model-id view-id]
  (var model (-/model-ensure cell model-id))
  (var views (. model ["views"]))
  (var view (k/get-key views view-id))
  (when (k/nil? view)
    (k/err (k/cat "ERR - View not found - " view-id)))
  (return [model view]))

(defn.js view-access
  "acts as the view access function"
  {:added "4.0"}
  [cell model-id view-id f args]
  (var model (-/model-get cell model-id))
  (when (k/nil? model)
    (return nil))
  (var views (. model ["views"]))
  (var view (k/get-key views view-id))
  (when (k/nil? view)
    (return nil))
  (return (f view (k/unpack args))))

(defn.js clear-listeners
  "clears listeners from the cell container"
  {:added "4.0"}
  [cell]
  (return (event-common/clear-listeners cell)))

(defn.js add-listener
  "add listener to cell"
  {:added "4.0"}
  [cell path listener-id f meta pred]
  (var view-key (k/json-encode path))
  (return
   (event-common/add-keyed-listener
    cell view-key listener-id "cell" f meta pred)))

(defn.js remove-listener
  "remove listeners from cell"
  {:added "4.0"}
  [cell path listener-id]
  (var view-key (k/json-encode path))
  (return
   (event-common/remove-keyed-listener
    cell view-key listener-id)))

(defn.js list-listeners
  "lists listeners in a cell path"
  {:added "4.0"}
  [cell path]
  (var view-key (k/json-encode path))
  (return
   (event-common/list-keyed-listeners cell view-key)))

(defn.js list-all-listeners
  "lists all listeners in cell"
  {:added "4.0"}
  [cell]
  (var listeners (. cell ["listeners"]))
  (var out {})
  (k/for:object [[view-key callbacks] listeners]
    (k/set-in out
              (k/json-decode view-key)
              (k/obj-keys callbacks)))
  (return out))

(defn.js trigger-listeners
  "triggers listeners"
  {:added "4.0"}
  [cell path event]
  (var view-key (k/json-encode path))
  (return
   (event-common/trigger-keyed-listeners
    cell view-key (k/obj-assign {:path path} event))))
