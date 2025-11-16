(ns js.cell.impl-common
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]
             [js.cell.link-raw :as raw]
             [js.cell.base-util :as util]
             [js.core :as j]]})

(defn.js new-cell-init
  "creates a record for asynchronous resolve"
  {:added "4.0"}
  []
  (var init  {})
  (var init-state (new Promise
                     (fn [resolve reject]
                       (k/obj-assign init {:resolve resolve
                                           :reject reject}))))
  (k/set-key init "current" init-state)
  (return init))

(defn.js new-cell
  "makes the core link"
  {:added "0.1" :adopt true}
  [worker-url]
  (var link    (:? (and (k/obj? worker-url)
                        (not (. worker-url ["create_fn"])))
                   worker-url
                   (raw/link-create worker-url)))
  (var init    (-/new-cell-init))
  (var models  {})
  (raw/add-callback link
                    util/EV_INIT
                    (fn:> [topic] (== util/EV_INIT topic))
                    (fn [data]
                      (raw/remove-callback link util/EV_INIT)
                      (. init (resolve true))))
  (return
   (event-common/blank-container
    "cell"
    {:id        (. link ["id"])
     :link      link
     :models    {}
     :init      (k/get-key init "current")})))

(defn.js list-models
  "lists all models"
  {:added "0.1"}
  [cell]
    (var #{models} cell)
    (return (k/obj-keys models)))

(defn.js call
  "conducts a call, either for a link or cell"
  {:added "4.0"}
  [client event]
  (var t (k/get-key client "::"))
  (cond (== t "cell.link")
        (return (raw/call client event))
        
        (== t "cell")
        (return (raw/call (k/get-key client "link") event))))

;;
;; ACCESS
;;

(defn.js model-get
  "gets a model"
  {:added "4.0"}
  [cell model-id]
  (var #{models} cell)
  (return (k/get-key models model-id)))

(defn.js model-ensure
  "throws an error if model is not present"
  {:added "4.0"}
  [cell model-id]
  (var model (-/model-get cell model-id))
  (when (k/nil? model)
    (k/err (k/cat "ERR - Page not found - " model-id)))
  (return model))

(defn.js list-views
  "lists views in the model"
  {:added "0.1"}
  [cell model-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (return (k/obj-keys views)))

(defn.js view-ensure
  "gets the view"
  {:added "0.1"}
  [cell model-id view-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (var view  (k/get-key views view-id))
  (when (k/nil? view)
    (k/err (k/cat "ERR - Model not found - " view-id)))
  (return [model view]))

(defn.js view-access
  "acts as the view access function"
  {:added "4.0"}
  [cell model-id view-id f args]
  (var model (-/model-get cell model-id))
  (when (k/nil? model)
    (return nil))
  
  (var #{views} model)
  (var view  (k/get-key views view-id))
  (when (k/nil? view)
    (return nil))
  (return (f view (k/unpack args))))

;;
;; LISTENER
;;

(def.js ^{:arglists '([cell])}
  clear-listeners
  event-common/clear-listeners)

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
  (var #{listeners} cell)
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
    cell view-key (j/assign {:path path} event))))

