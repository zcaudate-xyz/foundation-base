(ns js.cell.kernel.base-impl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[xt.lang.common-spec :as xt] [xt.lang.common-data :as xtd] [xt.lang.event-common :as event-common] [js.cell.kernel.base-link :as link] [js.cell.kernel.base-util :as util] [js.core :as j]]})

(defspec.xt new-cell-init
  [:fn [] js.cell.kernel.spec/CellInit])

(defspec.xt new-cell
  [:fn [:xt/any] js.cell.kernel.spec/CellRecord])

(defspec.xt list-models
  [:fn [js.cell.kernel.spec/CellRecord]
   js.cell.kernel.spec/StringList])

(defspec.xt call
  [:fn [[:or js.cell.kernel.spec/CellRecord js.cell.kernel.spec/LinkRecord]
        js.cell.kernel.spec/RequestFrame]
   :xt/any])

(defspec.xt model-get
  [:fn [js.cell.kernel.spec/CellRecord :xt/str]
   [:xt/maybe js.cell.kernel.spec/ModelRecord]])

(defspec.xt model-ensure
  [:fn [js.cell.kernel.spec/CellRecord :xt/str]
   js.cell.kernel.spec/ModelRecord])

(defspec.xt list-views
  [:fn [js.cell.kernel.spec/CellRecord :xt/str]
   js.cell.kernel.spec/StringList])

(defspec.xt view-ensure
  [:fn [js.cell.kernel.spec/CellRecord :xt/str :xt/str]
   [:tuple js.cell.kernel.spec/ModelRecord js.cell.kernel.spec/ViewRecord]])

(defspec.xt view-access
  [:fn [js.cell.kernel.spec/CellRecord
        :xt/str
        :xt/str
        [:fn [js.cell.kernel.spec/ViewRecord js.cell.kernel.spec/AnyList] :xt/any]
        js.cell.kernel.spec/AnyList]
   [:xt/maybe :xt/any]])

(defspec.xt clear-listeners
  [:fn [js.cell.kernel.spec/CellRecord] :xt/any])

(defspec.xt add-listener
  [:fn [js.cell.kernel.spec/CellRecord
        js.cell.kernel.spec/Path
        :xt/str
        [:fn [:xt/any] :xt/any]
        [:xt/maybe :xt/any]
        [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt remove-listener
  [:fn [js.cell.kernel.spec/CellRecord
        js.cell.kernel.spec/Path
        :xt/str]
   :xt/any])

(defspec.xt list-listeners
  [:fn [js.cell.kernel.spec/CellRecord js.cell.kernel.spec/Path]
   js.cell.kernel.spec/StringList])

(defspec.xt list-all-listeners
  [:fn [js.cell.kernel.spec/CellRecord]
   js.cell.kernel.spec/AnyMap])

(defspec.xt trigger-listeners
  [:fn [js.cell.kernel.spec/CellRecord
        js.cell.kernel.spec/Path
        js.cell.kernel.spec/AnyMap]
   :xt/any])

(defn.js new-cell-init
  "creates a record for asynchronous resolve"
  {:added "4.0"}
  []
  (var init  {})
  (var init-state (new Promise
                     (fn [resolve reject]
                       (xt/x:obj-assign init {:resolve resolve
                                              :reject reject}))))
  (xt/x:set-key init "current" init-state)
  (return init))

(defn.js new-cell
  "makes the core link"
  {:added "0.1" :adopt true}
  [worker-url]
   (var link    (:? (and (xt/x:is-object? worker-url)
                         (not (. worker-url ["create_fn"])))
                   worker-url
                   (link/link-create worker-url)))
  (var init    (-/new-cell-init))
  (var models  {})
  (link/add-callback link
                    util/EV_INIT
                    (fn:> [signal] (== util/EV_INIT signal))
                    (fn [data]
                      (link/remove-callback link util/EV_INIT)
                      (. init (resolve true))))
  (return
   (event-common/blank-container
    "cell"
     {:id        (. link ["id"])
      :link      link
      :models    {}
      :init      (xt/x:get-key init "current")})))

(defn.js list-models
  "lists all models"
  {:added "0.1"}
  [cell]
    (var #{models} cell)
    (return (xt/x:obj-keys models)))

(defn.js call
  "conducts a call, either for a link or cell"
  {:added "4.0"}
  [client event]
  (var t (xt/x:get-key client "::"))
  (cond (== t "cell.link")
        (return (link/call client event))
        
        (== t "cell")
        (return (link/call (xt/x:get-key client "link") event))))

;;
;; ACCESS
;;

(defn.js model-get
  "gets a model"
  {:added "4.0"}
  [cell model-id]
  (var #{models} cell)
  (return (xt/x:get-key models model-id)))

(defn.js model-ensure
  "throws an error if model is not present"
  {:added "4.0"}
  [cell model-id]
  (var model (-/model-get cell model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "ERR - Page not found - " model-id)))
  (return model))

(defn.js list-views
  "lists views in the model"
  {:added "0.1"}
  [cell model-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (return (xt/x:obj-keys views)))

(defn.js view-ensure
  "gets the view"
  {:added "0.1"}
  [cell model-id view-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (var view  (xt/x:get-key views view-id))
  (when (xt/x:nil? view)
    (xt/x:err (xt/x:cat "ERR - Model not found - " view-id)))
  (return [model view]))

(defn.js view-access
  "acts as the view access function"
  {:added "4.0"}
  [cell model-id view-id f args]
  (var model (-/model-get cell model-id))
  (when (xt/x:nil? model)
    (return nil))
  
  (var #{views} model)
  (var view  (xt/x:get-key views view-id))
  (when (xt/x:nil? view)
    (return nil))
  (return (f view (xt/x:unpack args))))

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
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/add-keyed-listener
    cell view-key listener-id "cell" f meta pred)))

(defn.js remove-listener
  "remove listeners from cell"
  {:added "4.0"}
  [cell path listener-id]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/remove-keyed-listener
    cell view-key listener-id)))

(defn.js list-listeners
  "lists listeners in a cell path"
  {:added "4.0"}
  [cell path]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/list-keyed-listeners cell view-key)))

(defn.js list-all-listeners
  "lists all listeners in cell"
  {:added "4.0"}
  [cell]
  (var #{listeners} cell)
  (var out {})
  (xt/for:object [[view-key callbacks] listeners]
    (xtd/set-in out
                (xt/x:json-decode view-key)
                (xt/x:obj-keys callbacks)))
  (return out))

(defn.js trigger-listeners
  "triggers listeners"
  {:added "4.0"}
  [cell path event]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/trigger-keyed-listeners
    cell view-key (j/assign {:path path} event))))
