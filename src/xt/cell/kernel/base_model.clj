(ns xt.cell.kernel.base-model
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.base-task :as task]
             [xt.lang.util-throttle :as th]
             [xt.lang.event-view :as event-view]
             [xt.cell.kernel.base-link :as link]
             [xt.cell.kernel.base-util :as util]
             [xt.cell.kernel.base-impl :as impl]]})


(defspec.xt async-fn :xt/any)

(defspec.xt task-all
  [:fn [[:xt/array :xt/any]] :xt/any])

(defspec.xt wrap-cell-args
  [:fn [[:fn [xt.cell.kernel.spec/LinkRecord xt.cell.kernel.spec/AnyList] :xt/any]]
   :xt/any])

(defspec.xt prep-view
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str xt.cell.kernel.spec/AnyMap]
   [:tuple xt.cell.kernel.spec/Path xt.cell.kernel.spec/AnyMap :xt/any]])

(defspec.xt get-view-dependents
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str]
   xt.cell.kernel.spec/ViewDependents])

(defspec.xt get-model-dependents
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str]
   [:xt/dict :xt/str :xt/bool]])

(defspec.xt run-tail-call
  [:fn [xt.cell.kernel.spec/AnyMap [:xt/maybe :xt/any]] :xt/any])

(defspec.xt run-remote
  [:fn [xt.cell.kernel.spec/AnyMap :xt/bool xt.cell.kernel.spec/Path [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt remote-call
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str xt.cell.kernel.spec/AnyList :xt/bool]
   :xt/any])

(defspec.xt run-refresh
  [:fn [xt.cell.kernel.spec/AnyMap :xt/any xt.cell.kernel.spec/Path [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt refresh-view-dependents
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str]
   xt.cell.kernel.spec/ViewDependents])

(defspec.xt refresh-view
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str xt.cell.kernel.spec/AnyMap [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt refresh-view-remote
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt refresh-view-dependents-unthrottled
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt refresh-model
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str xt.cell.kernel.spec/AnyMap [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt get-model-deps
  [:fn [:xt/str xt.cell.kernel.spec/ViewMap]
   xt.cell.kernel.spec/ModelDeps])

(defspec.xt get-unknown-deps
  [:fn [:xt/str xt.cell.kernel.spec/ViewMap xt.cell.kernel.spec/ModelDeps xt.cell.kernel.spec/CellRecord]
   [:xt/array xt.cell.kernel.spec/Path]])

(defspec.xt create-throttle
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str [:xt/maybe :xt/any]]
   :xt/any])

(defspec.xt create-view
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str xt.cell.kernel.spec/ViewSpec]
   xt.cell.kernel.spec/ViewRecord])

(defspec.xt add-model-attach
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str xt.cell.kernel.spec/ViewMap]
   xt.cell.kernel.spec/ModelRecord])

(defspec.xt add-model
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str xt.cell.kernel.spec/ViewMap]
   xt.cell.kernel.spec/ModelRecord])

(defspec.xt remove-model
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str]
   [:xt/maybe xt.cell.kernel.spec/ModelRecord]])

(defspec.xt remove-view
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str]
   [:xt/maybe xt.cell.kernel.spec/ViewRecord]])

(defspec.xt model-update
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str [:xt/maybe xt.cell.kernel.spec/AnyMap]]
   :xt/any])

(defspec.xt view-update
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str [:xt/maybe xt.cell.kernel.spec/AnyMap]]
   :xt/any])

(defspec.xt view-set-input
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str :xt/any [:xt/maybe xt.cell.kernel.spec/AnyMap]]
   :xt/any])

(defspec.xt trigger-model-raw
  [:fn [xt.cell.kernel.spec/CellRecord xt.cell.kernel.spec/ModelRecord :xt/str xt.cell.kernel.spec/AnyMap]
   xt.cell.kernel.spec/StringList])

(defspec.xt trigger-model
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str xt.cell.kernel.spec/AnyMap]
   xt.cell.kernel.spec/StringList])

(defspec.xt trigger-view
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str :xt/str :xt/str xt.cell.kernel.spec/AnyMap]
   [:xt/maybe :xt/any]])

(defspec.xt trigger-all
  [:fn [xt.cell.kernel.spec/CellRecord :xt/str xt.cell.kernel.spec/AnyMap]
   xt.cell.kernel.spec/AnyMap])

(defspec.xt add-raw-callback
  [:fn [xt.cell.kernel.spec/CellRecord] [:xt/array :xt/any]])

(defspec.xt remove-raw-callback
  [:fn [xt.cell.kernel.spec/CellRecord] [:xt/array :xt/any]])

(defn.xt wrap-cell-args
  "puts the cell as first argument"
  {:added "4.0"}
  [handler]
  (return (fn [context]
            (return (handler (. context ["cell"] ["link"])
                             (xt/x:unpack (. context ["args"])))))))

(def.xt async-fn
  (fn [handler-fn context #{success error}]
    (return
     (task/task-catch
      (task/task-then
       (task/task-run
        (fn []
          (return (handler-fn context))))
       success)
      error))))

(defn.xt task-all
  "waits for a collection of tasks in order"
  {:added "4.0"}
  [tasks]
  (var out [])
  (var total (xt/x:len tasks))
  (var step nil)
  (:= step
      (fn [i]
        (if (< i (xt/x:offset total))
          (return
           (task/task-then
            (xt/x:get-idx tasks i)
            (fn [res]
              (xt/x:arr-push out res)
              (return (step (xt/x:inc i))))))
          (return (task/task-run (fn [] (return out)))))))
  (return (step (xt/x:offset 0))))

(defn.xt prep-view
  "prepares params of views"
  {:added "4.0"}
  [cell model-id view-id opts]
  (var path [model-id view-id])
  (var [model view] (impl/view-ensure cell model-id view-id))
  (var [context disabled] (event-view/pipeline-prep view
                                                   (xt/x:obj-assign {:path  path
                                                                  :cell  cell
                                                                  :model  model}
                                                                 opts)))
  (return [path context disabled]))

(defn.xt get-view-dependents
  "gets all dependents for a view"
  {:added "4.0"}
  [cell model-id view-id]
  (var out {})
  (var #{models} cell)
  (xt/for:object [[dmodel-id dmodel] models]
    (var #{deps} dmodel)
    (var view-lu (xtd/get-in deps [model-id view-id]))
    (when (xt/x:not-nil? view-lu)
      (xt/x:set-key out dmodel-id (xt/x:obj-keys view-lu))))
  (return out))

(defn.xt get-model-dependents
  "gets all dependents for a model"
  {:added "4.0"}
  [cell model-id]
  (var out {})
  (var #{models} cell)
  (xt/for:object [[dmodel-id dmodel] models]
    (var #{deps} dmodel)
    (var model-lu (. deps [model-id]))
    (when (xt/x:not-nil? model-lu)
      (xt/x:set-key out dmodel-id true)))
  (return out))

(defn.xt run-tail-call
  "helper function for tail calls on `run` commands"
  {:added "4.0"}
  [context refresh-deps-fn]
  (var #{acc cell path} context)
  (var [model-id view-id] path)
  (when (and acc (not (. acc ["error"])))
    (when refresh-deps-fn
      (refresh-deps-fn cell model-id view-id refresh-deps-fn)))
  (return acc))

(defn.xt run-remote
  "runs the remote function"
  {:added "4.0"}
  [context save-output path refresh-deps-fn]
  (xt/x:set-key (. context ["acc"]) "path" path)
  (return
   (task/task-then
    (event-view/pipeline-run-remote
     context
     save-output
     -/async-fn
     nil
     (fn [x] (return x)))
    (fn [_]
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt remote-call
  "runs tthe remote call"
  {:added "4.0"}
  [cell model-id view-id args save-output]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {:args args}))
  (return (-/run-remote context save-output path)))

(defn.xt run-refresh
  "helper function for refresh"
  {:added "4.0"}
  [context disabled path refresh-deps-fn]
  (xt/x:set-key (. context ["acc"]) "path" path)
  (return
   (task/task-then
    (event-view/pipeline-run
     context
     disabled
     -/async-fn
     nil
     (fn [x] (return x)))
    (fn [_]
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt refresh-view-dependents
  "refreshes view dependents"
  {:added "4.0"}
  [cell model-id view-id]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (var #{throttle} (. models [dmodel-id]))
    (xt/for:array [dview-id dview-ids]
      (th/throttle-run throttle dview-id [])))
  (return dependents))

(defn.xt refresh-view
  "calls update on the view"
  {:added "4.0"}
  [cell model-id view-id event refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {:event event}))
  (return (-/run-refresh context disabled path refresh-deps-fn)))

(defn.xt refresh-view-remote
  "calls update on remote function"
  {:added "4.0"}
  [cell model-id view-id refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {}))
  (return (-/run-remote context true path refresh-deps-fn)))

(defn.xt refresh-view-dependents-unthrottled
  "refreshes dependents without throttle"
  {:added "4.0"}
  [cell model-id view-id refresh-deps-fn]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (var out [])
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (xt/for:array [dview-id dview-ids]
      (xt/x:arr-push out (-/refresh-view cell dmodel-id dview-id {} refresh-deps-fn))))
  (return (-/task-all out)))

(defn.xt refresh-model
  "refreshes the model"
  {:added "4.0"}
  [cell model-id event refresh-deps-fn]
  (var model (impl/model-ensure cell model-id))
  (var running [])
  (xt/for:object [[view-id view] (. model ["views"])]
    (var [path context disabled]
         (-/prep-view cell model-id view-id {:event event}))
    (xt/x:arr-push running (-/run-refresh context disabled path refresh-deps-fn)))
  (return (-/task-all running)))


(defn.xt get-model-deps
  "gets model deps"
  {:added "4.0"}
  [model-id views]
  (var all-deps {})
  (xt/for:object [[view-id view-entry] views]
    (var #{deps} view-entry)
    (xt/for:array [path (or deps [])]
      (:= path (:? (xt/x:is-array? path) path [model-id path]))
      (xtd/set-in all-deps
                [(xt/x:first path)
                 (xt/x:second path)
                 view-id]
                true)))
  (return all-deps))

(defn.xt get-unknown-deps
  "gets unknown deps"
  {:added "4.0"}
  [model-id views model-deps cell]
  (var out [])
  (xt/for:object [[linked-model-id linked-views] model-deps]
    (cond (== model-id linked-model-id)
          (xt/for:object [[linked-view-id _] linked-views]
            (when (xt/x:nil? (. views [linked-view-id]))
              (xt/x:arr-push out [linked-model-id linked-view-id])))

          :else
          (do (var linked-model (impl/model-get cell linked-model-id))
              (xt/for:object [[linked-view-id _] linked-views]
                (when (or (xt/x:nil? linked-model)
                          (xt/x:nil? (. linked-model ["views"] [linked-view-id])))
                  (xt/x:arr-push out [linked-model-id linked-view-id]))))))
  (return out))

(defn.xt create-throttle
  "creates the throttle"
  {:added "4.0"}
  [cell model-id refresh-deps-fn]
  (return
   (th/throttle-create
    (fn [view-id event]
      (return
       (task/task-catch
        (-/refresh-view cell model-id view-id event refresh-deps-fn)
        (fn [err]
          (xt/x:LOG! {:stack   (. err ["stack"])
                   :message (. err ["message"])})
          (return err)))))
    k/now-ms)))

(defn.xt create-view
  "creates a view"
  {:added "4.0"}
  [cell model-id view-id
   #{handler
     remoteHandler
     pipeline
     defaultArgs
     defaultOutput
     defaultProcess
     defaultInit
     trigger
     options}]
  (var view (event-view/create-view
             nil
             (xtd/obj-assign-nested
              {:main   {:handler handler
                        :wrapper -/wrap-cell-args}
               :remote {:handler remoteHandler
                        :wrapper -/wrap-cell-args}}
              pipeline)
             defaultArgs
              defaultOutput
              defaultProcess
              (xt/x:obj-assign {:trigger trigger
                             :init defaultInit}
                            options)))
  (event-view/init-view view)
  (event-view/add-listener
   view
   "@/cell"
   (fn [event]
     (return (impl/trigger-listeners cell [model-id view-id] event))))
  (return view))

(defn.xt add-model-attach
  "adds model statically"
  {:added "4.0"}
  [cell model-id views]
  (var #{models} cell)
  (var model-throttle (-/create-throttle cell model-id -/refresh-view-dependents))
  (var model-deps (-/get-model-deps model-id views))
  (var unknown-deps (-/get-unknown-deps model-id views model-deps cell))
  (when (xt/x:not-empty? unknown-deps)
    (xt/x:LOG! {:message (xt/x:cat "ERR - deps not found - " (xt/x:json-encode unknown-deps))
             :deps model-deps}))
  (var model-views {})
  (xt/for:object [[view-id view] views]
    (xt/x:set-key model-views view-id (-/create-view cell model-id view-id view)))
  (var model {:name     model-id
              :views    model-views
              :throttle model-throttle
              :deps     model-deps})
  (xt/x:set-key models model-id model)
  (return model))

(defn.xt add-model
  "calls update on the view"
  {:added "4.0"}
  [cell model-id views]
  (var #{models} cell)
  (var model (-/add-model-attach cell model-id views))
  (xt/x:set-key model "init" (-/refresh-model cell model-id {}))
  (return model))

(defn.xt remove-model
  "removes the model"
  {:added "4.0"}
  [cell model-id]
  (var #{models} cell)
  (var dependents (-/get-model-dependents cell models))
  (when (xt/x:not-empty? dependents)
    (xt/x:err (xt/x:cat "ERR - existing model dependents - " (xt/x:json-encode dependents))))
  (var curr (xt/x:get-key models model-id))
  (xt/x:del-key models model-id)
  (return curr))

(defn.xt remove-view
  "removes the view"
  {:added "4.0"}
  [cell model-id view-id]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (when (xt/x:not-empty? dependents)
    (xt/x:err (xt/x:cat "ERR - existing view dependents - " (xt/x:json-encode dependents))))
  (var model (xt/x:get-key models model-id))
  (when model
    (var #{views} model)
    (var curr (xt/x:get-key views view-id))
    (xt/x:del-key views view-id)
    (return curr)))

(defn.xt model-update
  "updates a model"
  {:added "4.0"}
  [cell model-id ?event]
  (var model (impl/model-ensure cell model-id))
  (var #{throttle views} model)
  (var out [])
  (xt/for:object [[view-id _] views]
    (xt/x:arr-push out [view-id (xt/x:first (th/throttle-run throttle view-id [(or ?event {})]))]))
  (return
   (task/task-then
    (-/task-all (xt/x:arr-map out k/second))
    (fn [arr]
      (return (xt/x:arr-zip (xt/x:arr-map out k/first)
                         arr))))))

(defn.xt view-update
  "updates a view"
  {:added "4.0"}
  [cell model-id view-id ?event]
  (var [model view] (impl/view-ensure cell model-id view-id))
  (var #{throttle} model)
  (return (th/throttle-run throttle view-id [(or ?event {})])))

(defn.xt view-set-input
  "sets the view input"
  {:added "4.0"}
  [cell model-id view-id current ?event]
  (var [model view] (impl/view-ensure cell model-id view-id))
  (event-view/set-input view current)
  (return(-/view-update cell model-id view-id (or ?event {}))))

;;
;;
;;

(defn.xt trigger-model-raw
  "triggers a model"
  {:added "4.0"}
  [cell model signal event]
  (var #{views throttle} model)
  (var out [])
  (xt/for:object [[view-id view] views]
    (var #{options} view)
    (var #{trigger} options)
    (var check (util/check-event trigger signal event {:view view
                                                      :model model
                                                      :cell cell}))
    (when check
      (th/throttle-run (xt/x:get-key model "throttle")
                       view-id
                       [event])
      (xt/x:arr-push out view-id)))
  (return out))

(defn.xt trigger-model
  "triggers a model"
  {:added "4.0"}
  [cell model-id signal event]
  (var model (impl/model-ensure cell model-id))
  (return (-/trigger-model-raw cell model signal event)))

(defn.xt trigger-view
  "triggers a view"
  {:added "4.0"}
  [cell model-id view-id signal event]
  (var #{link} cell)
  (var [model view] (impl/view-ensure cell model-id view-id))
  (var #{options} view)
  (var #{trigger} options)
  (when (util/check-event trigger signal event {:view view
                                               :model model
                                               :cell cell})
    (return (th/throttle-run (xt/x:get-key model "throttle")
                             view-id
                             [event])))
  (return nil))

(defn.xt trigger-all
  "triggers all models in cell"
  {:added "0.1"}
  [cell signal event]
  (var #{models} cell)
  (var out {})
  (xt/for:object [[model-id model] models]
    (var model-out (-/trigger-model-raw cell model signal event))
    (xt/x:set-key out model-id model-out))
  (return out))

(defn.xt add-raw-callback
  "adds the callback on events"
  {:added "4.0"}
  [cell]
  (var #{link} cell)
  (return (link/add-callback
           link
           "@/raw"
           (fn:> true)
           (fn [event signal]
             (return (-/trigger-all cell signal event))))))

(defn.xt remove-raw-callback
  "removes the cell callback"
  {:added "4.0"}
  [cell]
  (var #{link} cell)
  (return (link/remove-callback link "@/raw")))
