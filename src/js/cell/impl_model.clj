(ns js.cell.impl-model
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.util-throttle :as th]
             [xt.lang.event-view :as event-view]
             [js.cell.link-raw :as raw]
             [js.cell.base-util :as util]
             [js.cell.impl-common :as impl-common]
             [js.core :as j]]})

(defn.js wrap-cell-args
  "puts the cell as first argument"
  {:added "4.0"}
  [handler]
  (return (fn [context]
            (return (handler (. context ["cell"] ["link"])
                             (k/unpack (. context ["args"])))))))

(def.js async-fn (j/asyncFn))

(defn.js prep-view
  "prepares params of views"
  {:added "4.0"}
  [cell model-id view-id opts]
  (var path [model-id view-id])
  (var [model view] (impl-common/view-ensure cell model-id view-id))
  (var [context disabled] (event-view/pipeline-prep view
                                                   (j/assign {:path  path
                                                              :cell  cell
                                                              :model  model}
                                                             opts)))
  (return [path context disabled]))

(defn.js get-view-dependents
  "gets all dependents for a view"
  {:added "4.0"}
  [cell model-id view-id]
  (var out {})
  (var #{models} cell)
  (k/for:object [[dmodel-id dmodel] models]
    (var #{deps} dmodel)
    (var view-lu (k/get-in deps [model-id view-id]))
    (when (k/not-nil? view-lu)
      (k/set-key out dmodel-id (k/obj-keys view-lu))))
  (return out))

(defn.js get-model-dependents
  "gets all dependents for a model"
  {:added "4.0"}
  [cell model-id]
  (var out {})
  (var #{models} cell)
  (k/for:object [[dmodel-id dmodel] models]
    (var #{deps} dmodel)
    (var model-lu (. deps [model-id]))
    (when (k/not-nil? model-lu)
      (k/set-key out dmodel-id true)))
  (return out))

(defn.js run-tail-call
  "helper function for tail calls on `run` commands"
  {:added "4.0"}
  [context refresh-deps-fn]
  (var #{acc cell path} context)
  (var [model-id view-id] path)
  (when (and acc (not (. acc ["error"])))
    (when refresh-deps-fn
      (refresh-deps-fn cell model-id view-id refresh-deps-fn)))
  (return acc))

(defn.js run-remote
  "runs the remote function"
  {:added "4.0"}
  [context save-output path refresh-deps-fn]
  (k/set-key (. context ["acc"]) "path" path)
  (return
   (. (event-view/pipeline-run-remote
       context
       save-output
       -/async-fn
       nil
       k/identity)
      (then (fn []
              (return (-/run-tail-call context refresh-deps-fn)))))))

(defn.js remote-call
  "runs tthe remote call"
  {:added "4.0"}
  [cell model-id view-id args save-output]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {:args args}))
  (return (-/run-remote context save-output path)))

(defn.js run-refresh
  "helper function for refresh"
  {:added "4.0"}
  [context disabled path refresh-deps-fn]
  (k/set-key (. context ["acc"]) "path" path)
  (return
   (. (event-view/pipeline-run
       context
       disabled
       -/async-fn
       nil
       k/identity)
      (then (fn:> (-/run-tail-call context refresh-deps-fn))))))

(defn.js refresh-view-dependents
  "refreshes view dependents"
  {:added "4.0"}
  [cell model-id view-id]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (k/for:object [[dmodel-id dview-ids] dependents]
    (var #{throttle} (. models [dmodel-id]))
    (k/for:array [dview-id dview-ids]
      (th/throttle-run throttle dview-id [])))
  (return dependents))

(defn.js refresh-view
  "calls update on the view"
  {:added "4.0"}
  [cell model-id view-id event refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {:event event}))
  (return (-/run-refresh context disabled path refresh-deps-fn)))

(defn.js refresh-view-remote
  "calls update on remote function"
  {:added "4.0"}
  [cell model-id view-id refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view cell model-id view-id {}))
  (return (-/run-remote context true path refresh-deps-fn)))

(defn.js refresh-view-dependents-unthrottled
  "refreshes dependents without throttle"
  {:added "4.0"}
  [cell model-id view-id refresh-deps-fn]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (var out [])
  (k/for:object [[dmodel-id dview-ids] dependents]
    (k/for:array [dview-id dview-ids]
      (x:arr-push out (-/refresh-view cell dmodel-id dview-id {} refresh-deps-fn))))
  (return (j/onAll out)))

(defn.js refresh-model
  "refreshes the model"
  {:added "4.0"}
  [cell model-id event refresh-deps-fn]
  (var model (impl-common/model-ensure cell model-id))
  (var running [])
  (k/for:object [[view-id view] (. model ["views"])]
    (var [path context disabled]
         (-/prep-view cell model-id view-id {:event event}))
    (x:arr-push running (-/run-refresh context disabled path refresh-deps-fn)))
  (return (j/onAll running)))


(defn.js get-model-deps
  "gets model deps"
  {:added "4.0"}
  [model-id views]
  (var all-deps {})
  (k/for:object [[view-id view-entry] views]
    (var #{deps} view-entry)
    (k/for:array [path (or deps [])]
      (:= path (:? (k/arr? path) path [model-id path]))
      (k/set-in all-deps
                [(k/first path)
                 (k/second path)
                 view-id]
                true)))
  (return all-deps))

(defn.js get-unknown-deps
  "gets unknown deps"
  {:added "4.0"}
  [model-id views model-deps cell]
  (var out [])
  (k/for:object [[linked-model-id linked-views] model-deps]
    (cond (== model-id linked-model-id)
          (k/for:object [[linked-view-id _] linked-views]
            (when (k/nil? (. views [linked-view-id]))
              (x:arr-push out [linked-model-id linked-view-id])))

          :else
          (do (var linked-model (impl-common/model-get cell linked-model-id))
              (k/for:object [[linked-view-id _] linked-views]
                (when (or (k/nil? linked-model)
                          (k/nil? (. linked-model ["views"] [linked-view-id])))
                  (x:arr-push out [linked-model-id linked-view-id]))))))
  (return out))

(defn.js create-throttle
  "creates the throttle"
  {:added "4.0"}
  [cell model-id refresh-deps-fn]
  (return
   (th/throttle-create
    (fn [view-id event]
      (return (. (-/refresh-view cell model-id view-id event refresh-deps-fn)
                 (catch (fn [err]
                          (k/LOG! {:stack   (. err ["stack"])
                                   :message (. err ["message"])})
                          (return err))))))
    k/now-ms)))

(defn.js create-view
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
             (k/obj-assign-nested
              {:main   {:handler handler
                        :wrapper -/wrap-cell-args}
               :remote {:handler remoteHandler
                        :wrapper -/wrap-cell-args}}
              pipeline)
             defaultArgs
             defaultOutput
             defaultProcess
             (j/assign {:trigger trigger
                        :init defaultInit}
                       options)))
  (event-view/init-view view)
  (event-view/add-listener
   view
   "@/cell"
   (fn [event]
     (return (impl-common/trigger-listeners cell [model-id view-id] event))))
  (return view))

(defn.js add-model-attach
  "adds model statically"
  {:added "4.0"}
  [cell model-id views]
  (var #{models} cell)
  (var model-throttle (-/create-throttle cell model-id -/refresh-view-dependents))
  (var model-deps (-/get-model-deps model-id views))
  (var unknown-deps (-/get-unknown-deps model-id views model-deps cell))
  (when (k/not-empty? unknown-deps)
    (console.log (k/cat "ERR - deps not found - " (k/json-encode unknown-deps))
                 model-deps))
  (var model-views {})
  (k/for:object [[view-id view] views]
    (k/set-key model-views view-id (-/create-view cell model-id view-id view)))
  (var model {:name     model-id
              :views    model-views
              :throttle model-throttle
              :deps     model-deps})
  (k/set-key models model-id model)
  (return model))

(defn.js add-model
  "calls update on the view"
  {:added "4.0"}
  [cell model-id views]
  (var #{models} cell)
  (var model (-/add-model-attach cell model-id views))
  (k/set-key model "init" (-/refresh-model cell model-id {}))
  (return model))

(defn.js remove-model
  "removes the model"
  {:added "4.0"}
  [cell model-id]
  (var #{models} cell)
  (var dependents (-/get-model-dependents cell models))
  (when (k/not-empty? dependents)
    (k/err (k/cat "ERR - existing model dependents - " (k/json-encode dependents))))
  (var curr (k/get-key models model-id))
  (k/del-key models model-id)
  (return curr))

(defn.js remove-view
  "removes the view"
  {:added "4.0"}
  [cell model-id view-id]
  (var #{models} cell)
  (var dependents (-/get-view-dependents cell model-id view-id))
  (when (k/not-empty? dependents)
    (k/err (k/cat "ERR - existing view dependents - " (k/json-encode dependents))))
  (var model (k/get-key models model-id))
  (when model
    (var #{views} model)
    (var curr (k/get-key views view-id))
    (k/del-key views view-id)
    (return curr)))

(defn.js model-update
  "updates a model"
  {:added "4.0"}
  [cell model-id ?event]
  (var model (impl-common/model-ensure cell model-id))
  (var #{throttle views} model)
  (var out [])
  (k/for:object [[view-id _] views]
    (x:arr-push out [view-id (k/first (th/throttle-run throttle view-id [(or ?event {})]))]))
  (return (. (j/onAll (k/arr-map out k/second))
             (then (fn [arr]
                     (return (k/arr-zip (k/arr-map out k/first)
                                        arr)))))))

(defn.js view-update
  "updates a view"
  {:added "4.0"}
  [cell model-id view-id ?event]
  (var [model view] (impl-common/view-ensure cell model-id view-id))
  (var #{throttle} model)
  (return (th/throttle-run throttle view-id [(or ?event {})])))

(defn.js view-set-input
  "sets the view input"
  {:added "4.0"}
  [cell model-id view-id current ?event]
  (var [model view] (impl-common/view-ensure cell model-id view-id))
  (event-view/set-input view current)
  (return(-/view-update cell model-id view-id (or ?event {}))))

;;
;;
;;

(defn.js trigger-model-raw
  "triggers a model"
  {:added "4.0"}
  [cell model topic event]
  (var #{views throttle} model)
  (var out [])
  (k/for:object [[view-id view] views]
    (var #{options} view)
    (var #{trigger} options)
    (var check (util/check-event trigger topic event {:view view
                                                      :model model
                                                      :cell cell}))
    (when check
      (th/throttle-run (k/get-key model "throttle")
                       view-id
                       [event])
      (x:arr-push out view-id)))
  (return out))

(defn.js trigger-model
  "triggers a model"
  {:added "4.0"}
  [cell model-id topic event]
  (var model (impl-common/model-ensure cell model-id))
  (return (-/trigger-model-raw cell model topic event)))

(defn.js trigger-view
  "triggers a view"
  {:added "4.0"}
  [cell model-id view-id topic event]
  (var #{link} cell)
  (var [model view] (impl-common/view-ensure cell model-id view-id))
  (var #{options} view)
  (var #{trigger} options)
  (when (util/check-event trigger topic event {:view view
                                               :model model
                                               :cell cell})
    (return (th/throttle-run (k/get-key model "throttle")
                             view-id
                             [event])))
  (return nil))

(defn.js trigger-all
  "triggers all models in cell"
  {:added "0.1"}
  [cell topic event]
  (var #{models} cell)
  (var out {})
  (k/for:object [[model-id model] models]
    (var model-out (-/trigger-model-raw cell model topic event))
    (k/set-key out model-id model-out))
  (return out))

(defn.js add-raw-callback
  "adds the callback on events"
  {:added "4.0"}
  [cell]
  (var #{link} cell)
  (return (raw/add-callback
           link
           "@/raw"
           (fn:> true)
           (fn [event topic]
             (return (-/trigger-all cell topic event))))))

(defn.js remove-raw-callback
  "removes the cell callback"
  {:added "4.0"}
  [cell]
  (var #{link} cell)
  (return (raw/remove-callback link "@/raw")))

