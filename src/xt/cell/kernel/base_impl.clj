(ns xt.cell.kernel.base-impl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.base-task :as task]
             [xt.lang.event-common :as event-common]
             [xt.cell.kernel.base-link :as link]
             [xt.cell.kernel.base-util :as util]]})

(defspec.xt new-cell-init
  [:fn [] xt.cell.kernel.spec/CellInit])

(defspec.xt new-cell
  [:fn [:xt/any] xt.cell.kernel.spec/CellRecord])

(defspec.xt list-models
  [:fn [xt.cell.kernel.spec/CellRecord]
   xt.cell.kernel.spec/StringList])

(defspec.xt call
  [:fn [[:or xt.cell.kernel.spec/CellRecord xt.cell.kernel.spec/LinkRecord]
        xt.cell.kernel.spec/RequestFrame]
   :xt/any])

(defspec.xt model-get
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str]
   [:xt/maybe xt.cell.kernel.spec/ModelRecord]])

(defspec.xt model-ensure
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str]
   xt.cell.kernel.spec/ModelRecord])

(defspec.xt list-views
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str]
   xt.cell.kernel.spec/StringList])

(defspec.xt view-ensure
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str]
   [:tuple xt.cell.kernel.spec/ModelRecord xt.cell.kernel.spec/ViewRecord]])

(defspec.xt view-access
  [:fn [xt.cell.kernel.spec/CellRecord
        :xt/str
        :xt/str
        [:fn [xt.cell.kernel.spec/ViewRecord xt.cell.kernel.spec/AnyList] :xt/any]
        xt.cell.kernel.spec/AnyList]
   [:xt/maybe :xt/any]])

(defspec.xt clear-listeners
  [:fn [xt.cell.kernel.spec/CellRecord] :xt/any])

(defspec.xt add-listener
  [:fn [xt.cell.kernel.spec/CellRecord
        xt.cell.kernel.spec/Path
        :xt/str
        [:fn [:xt/any] :xt/any]
        [:xt/maybe :xt/any]
        [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt remove-listener
  [:fn [xt.cell.kernel.spec/CellRecord
        xt.cell.kernel.spec/Path
        :xt/str]
   :xt/any])

(defspec.xt list-listeners
  [:fn [xt.cell.kernel.spec/CellRecord xt.cell.kernel.spec/Path]
   xt.cell.kernel.spec/StringList])

(defspec.xt list-all-listeners
  [:fn [xt.cell.kernel.spec/CellRecord]
   xt.cell.kernel.spec/AnyMap])

(defspec.xt trigger-listeners
  [:fn [xt.cell.kernel.spec/CellRecord
        xt.cell.kernel.spec/Path
        xt.cell.kernel.spec/AnyMap]
   :xt/any])

(defn.xt new-cell-init
  "creates a record for asynchronous resolve"
  {:added "4.0"}
  []
  (var init  {})
  (var init-state
       (task/task-from-async
        (fn [resolve reject]
          (xt/x:obj-assign init {:resolve resolve
                              :reject reject}))))
  (xt/x:set-key init "current" init-state)
  (return init))

(defn.xt new-cell
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

(defn.xt list-models
  "lists all models"
  {:added "0.1"}
  [cell]
    (var #{models} cell)
    (return (xt/x:obj-keys models)))

(defn.xt call
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

(defn.xt model-get
  "gets a model"
  {:added "4.0"}
  [cell model-id]
  (var #{models} cell)
  (return (xt/x:get-key models model-id)))

(defn.xt model-ensure
  "throws an error if model is not present"
  {:added "4.0"}
  [cell model-id]
  (var model (-/model-get cell model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "ERR - Page not found - " model-id)))
  (return model))

(defn.xt list-views
  "lists views in the model"
  {:added "0.1"}
  [cell model-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (return (xt/x:obj-keys views)))

(defn.xt view-ensure
  "gets the view"
  {:added "0.1"}
  [cell model-id view-id]
  (var model (-/model-ensure cell model-id))
  (var #{views} model)
  (var view  (xt/x:get-key views view-id))
  (when (xt/x:nil? view)
    (xt/x:err (xt/x:cat "ERR - Model not found - " view-id)))
  (return [model view]))

(defn.xt view-access
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

(def.xt ^{:arglists '([cell])}
  clear-listeners
  event-common/clear-listeners)

(defn.xt add-listener
  "add listener to cell"
  {:added "4.0"}
  [cell path listener-id f meta pred]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/add-keyed-listener
    cell view-key listener-id "cell" f meta pred)))

(defn.xt remove-listener
  "remove listeners from cell"
  {:added "4.0"}
  [cell path listener-id]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/remove-keyed-listener
    cell view-key listener-id)))

(defn.xt list-listeners
  "lists listeners in a cell path"
  {:added "4.0"}
  [cell path]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/list-keyed-listeners cell view-key)))

(defn.xt list-all-listeners
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

(defn.xt trigger-listeners
  "triggers listeners"
  {:added "4.0"}
  [cell path event]
  (var view-key (xt/x:json-encode path))
  (return
   (event-common/trigger-keyed-listeners
    cell view-key (xt/x:obj-assign {:path path} event))))
