(ns xt.substrate.base-page
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.event.base-view :as event-view]
             [xt.event.util-throttle :as th]
             [xt.substrate.base-space :as node-space]]})

(def$.xt STATE_TAG "substrate.page")
(def$.xt STATE_SLOT "page")

(defn.xt async-fn
  "normalises view stage execution across plain values and xt.promise results"
  {:added "4.1"}
  [handler context callbacks]
  (var success (xt/x:get-key callbacks "success"))
  (var error (xt/x:get-key callbacks "error"))
  (try
    (var output (handler context))
    (if (promise/x:promise-native? output)
      (return
       (promise/x:promise-catch
        (promise/x:promise-then output success)
        error))
      (return (success output)))
    (catch err
      (return (error err)))))

(defn.xt wrap-space-args
  "puts the view context as first argument"
  {:added "4.1"}
  [handler]
  (return
   (fn [context]
     (var args (or (. context ["args"]) []))
     (var params [context])
     (xt/x:arr-assign params args)
     (return (xt/x:apply handler params)))))

(defn.xt check-event
  "checks that a trigger matches signal and event"
  {:added "4.1"}
  [pred signal event ctx]
  (var check false)
  (try
    (var t (:? (xt/x:nil? pred)
               true

               (xt/x:is-boolean? pred)
               pred

               (xt/x:is-function? pred)
               (pred signal ctx)

               (xt/x:is-object? pred)
               (xt/x:get-key pred signal)

               :else
               (== signal pred)))
    (:= check (or (== true t)
                  (and (xt/x:is-function? t) (t event ctx))
                  false))
    (catch err
      (:= check false)))
  (return check))

(defn.xt runtime-page
  "creates the page runtime container"
  {:added "4.1"}
  [opts]
  (:= opts (or opts {}))
  (return {"::" -/STATE_TAG
           "models" {}
           "meta" (or (xt/x:get-key opts "meta") {})
           "opts" opts}))

(defn.xt get-space-page
  "gets the page runtime from a node space"
  {:added "4.1"}
  [node space-id]
  (var state (node-space/get-space-state node space-id))
  (when (xt/x:is-object? state)
    (return (xt/x:get-key state -/STATE_SLOT))))

(defn.xt ensure-space-page
  "ensures the node space has a page runtime slot"
  {:added "4.1"}
  [node space-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (or (xt/x:nil? state)
            (not (xt/x:is-object? state)))
    (:= state {})
    (xt/x:set-key space "state" state))
  (var runtime (xt/x:get-key state -/STATE_SLOT))
  (when (not (and (xt/x:not-nil? runtime)
                  (== (xt/x:get-key runtime "::") -/STATE_TAG)))
    (:= runtime (-/runtime-page nil))
    (xt/x:set-key state -/STATE_SLOT runtime))
  (return runtime))

(defn.xt set-space-page
  "replaces the nested page runtime for a node space"
  {:added "4.1"}
  [node space-id runtime]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (or (xt/x:nil? state)
            (not (xt/x:is-object? state)))
    (:= state {})
    (xt/x:set-key space "state" state))
  (xt/x:set-key state -/STATE_SLOT runtime)
  (return runtime))

(defn.xt model-get
  "gets a page model record from a node space"
  {:added "4.1"}
  [node space-id model-id]
  (return (xtd/get-in (-/ensure-space-page node space-id)
                      ["models" model-id])))

(defn.xt model-ensure
  "gets a registered page model or throws"
  {:added "4.1"}
  [node space-id model-id]
  (var model (-/model-get node space-id model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "ERR - Model not found - " model-id)))
  (return model))

(defn.xt view-ensure
  "gets a registered page view or throws"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var model (-/model-ensure node space-id model-id))
  (var view (xt/x:get-key (xt/x:get-key model "views") view-id))
  (when (xt/x:nil? view)
    (xt/x:err (xt/x:cat "ERR - View not found - "
                        (xt/x:json-encode [model-id view-id]))))
  (return [model view]))

(defn.xt trigger-listeners
  "triggers keyed listeners on the node for a page path"
  {:added "4.1"}
  [node space-id path event]
  (var view-key (xt/x:json-encode [space-id path]))
  (return
   (event-common/trigger-keyed-listeners
    node view-key
    (xt/x:obj-assign {"space_id" space-id
                      "path" path}
                     event))))

(defn.xt prep-view
  "prepares params of views"
  {:added "4.1"}
  [node space-id model-id view-id opts]
  (var path [model-id view-id])
  (var space (node-space/ensure-space node space-id nil))
  (var [model view] (-/view-ensure node space-id model-id view-id))
  (var [context disabled]
       (event-view/pipeline-prep
        view
        (xt/x:obj-assign {"path" path
                          "node" node
                          "space" space
                          "model" model}
                         (or opts {}))))
  (return [path context disabled]))

(defn.xt get-view-dependents
  "gets all dependents for a view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var out {})
  (var models (xt/x:get-key (-/ensure-space-page node space-id) "models"))
  (xt/for:object [[dmodel-id dmodel] models]
    (var deps (xt/x:get-key dmodel "deps"))
    (var view-lu (xtd/get-in deps [model-id view-id]))
    (when (xt/x:not-nil? view-lu)
      (xt/x:set-key out dmodel-id (xt/x:obj-keys view-lu))))
  (return out))

(defn.xt get-model-dependents
  "gets all dependents for a model"
  {:added "4.1"}
  [node space-id model-id]
  (var out {})
  (var models (xt/x:get-key (-/ensure-space-page node space-id) "models"))
  (xt/for:object [[dmodel-id dmodel] models]
    (var deps (xt/x:get-key dmodel "deps"))
    (var model-lu (xt/x:get-key deps model-id))
    (when (xt/x:not-nil? model-lu)
      (xt/x:set-key out dmodel-id true)))
  (return out))

(defn.xt run-tail-call
  "helper function for tail calls on run commands"
  {:added "4.1"}
  [context refresh-deps-fn]
  (var acc (xt/x:get-key context "acc"))
  (var path (xt/x:get-key context "path"))
  (var node (xt/x:get-key context "node"))
  (var space-id (xt/x:get-key (xt/x:get-key context "space") "id"))
  (var model-id (xt/x:first path))
  (var view-id (xt/x:second path))
  (when (and acc (not (xt/x:get-key acc "error")))
    (when refresh-deps-fn
      (refresh-deps-fn node space-id model-id view-id refresh-deps-fn)))
  (return acc))

(defn.xt run-remote
  "runs the remote function"
  {:added "4.1"}
  [context save-output path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-view/pipeline-run-remote context save-output -/async-fn nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt remote-call
  "runs the remote call"
  {:added "4.1"}
  [node space-id model-id view-id args save-output]
  (var [path context disabled]
       (-/prep-view node space-id model-id view-id {"args" args}))
  (return (-/run-remote context save-output path nil)))

(defn.xt run-refresh
  "helper function for refresh"
  {:added "4.1"}
  [context disabled path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-view/pipeline-run context disabled -/async-fn nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt refresh-view-dependents
  "refreshes view dependents"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var dependents (-/get-view-dependents node space-id model-id view-id))
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (var throttle (xt/x:get-key (-/model-ensure node space-id dmodel-id) "throttle"))
    (xt/for:array [dview-id dview-ids]
      (th/throttle-run throttle dview-id [])))
  (return dependents))

(defn.xt refresh-view
  "calls update on the view"
  {:added "4.1"}
  [node space-id model-id view-id event refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view node space-id model-id view-id {"event" event}))
  (return (-/run-refresh context disabled path refresh-deps-fn)))

(defn.xt refresh-view-remote
  "calls update on remote function"
  {:added "4.1"}
  [node space-id model-id view-id refresh-deps-fn]
  (var [path context disabled]
       (-/prep-view node space-id model-id view-id {}))
  (return (-/run-remote context true path refresh-deps-fn)))

(defn.xt refresh-view-dependents-unthrottled
  "refreshes dependents without throttle"
  {:added "4.1"}
  [node space-id model-id view-id refresh-deps-fn]
  (var dependents (-/get-view-dependents node space-id model-id view-id))
  (var out [])
  (xt/for:object [[dmodel-id dview-ids] dependents]
    (xt/for:array [dview-id dview-ids]
      (xt/x:arr-push out
                     (-/refresh-view node
                                     space-id
                                     dmodel-id
                                     dview-id
                                     {}
                                     refresh-deps-fn))))
  (return (promise/x:promise-all out)))

(defn.xt refresh-model
  "refreshes the model"
  {:added "4.1"}
  [node space-id model-id event refresh-deps-fn]
  (var model (-/model-ensure node space-id model-id))
  (var running [])
  (xt/for:object [[view-id view] (xt/x:get-key model "views")]
    (var [path context disabled]
         (-/prep-view node space-id model-id view-id {"event" event}))
    (xt/x:arr-push running (-/run-refresh context disabled path refresh-deps-fn)))
  (return (promise/x:promise-all running)))

(defn.xt get-model-deps
  "gets model deps"
  {:added "4.1"}
  [model-id views]
  (var all-deps {})
  (xt/for:object [[view-id view-entry] views]
    (var deps (xt/x:get-key view-entry "deps"))
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
  {:added "4.1"}
  [node space-id model-id views model-deps]
  (var out [])
  (xt/for:object [[linked-model-id linked-views] model-deps]
    (cond (== model-id linked-model-id)
          (xt/for:object [[linked-view-id _] linked-views]
            (when (xt/x:nil? (xt/x:get-key views linked-view-id))
              (xt/x:arr-push out [linked-model-id linked-view-id])))

          :else
          (do (var linked-model (-/model-get node space-id linked-model-id))
              (xt/for:object [[linked-view-id _] linked-views]
                (when (or (xt/x:nil? linked-model)
                          (xt/x:nil? (xt/x:get-key (xt/x:get-key linked-model "views")
                                                   linked-view-id)))
                  (xt/x:arr-push out [linked-model-id linked-view-id]))))))
  (return out))

(defn.xt create-throttle
  "creates the throttle"
  {:added "4.1"}
  [node space-id model-id refresh-deps-fn]
  (return
   (th/throttle-create
    (fn [view-id event]
      (return
       (promise/x:promise-catch
        (-/refresh-view node space-id model-id view-id event refresh-deps-fn)
        (fn [err]
          (return err)))))
    xt/x:now-ms)))

(defn.xt create-view
  "creates a view"
  {:added "4.1"}
  [node space-id model-id view-id
   opts]
  (var handler (xt/x:get-key opts "handler"))
  (var remote-handler (xt/x:get-key opts "remoteHandler"))
  (var pipeline (xt/x:get-key opts "pipeline"))
  (var default-args (xt/x:get-key opts "defaultArgs"))
  (var default-output (xt/x:get-key opts "defaultOutput"))
  (var default-process (xt/x:get-key opts "defaultProcess"))
  (var default-init (xt/x:get-key opts "defaultInit"))
  (var trigger (xt/x:get-key opts "trigger"))
  (var options (xt/x:get-key opts "options"))
  (var view
       (event-view/create-view
        nil
        (xtd/obj-assign-nested
         {"main" {"handler" handler
                  "wrapper" -/wrap-space-args}
          "remote" {"handler" remote-handler
                    "wrapper" -/wrap-space-args}}
         pipeline)
        default-args
        default-output
        default-process
        (xt/x:obj-assign {"trigger" trigger
                          "init" default-init}
                         options)))
  (event-view/init-view view)
  (event-view/add-listener
   view
   "@/page"
   (fn [_id data _t meta]
     (var emitted (xt/x:obj-assign {} data))
     (xt/x:set-key emitted "meta" meta)
     (return
      (-/trigger-listeners
       node
       space-id
       [model-id view-id]
       emitted)))
   nil
   nil)
  (return view))

(defn.xt add-model-attach
  "adds model statically"
  {:added "4.1"}
  [node space-id model-id views]
  (var runtime (-/ensure-space-page node space-id))
  (var models (xt/x:get-key runtime "models"))
  (var model-throttle (-/create-throttle node space-id model-id -/refresh-view-dependents))
  (var model-deps (-/get-model-deps model-id views))
  (var model-views {})
  (xt/for:object [[view-id view] views]
    (xt/x:set-key model-views
                  view-id
                  (-/create-view node space-id model-id view-id view)))
  (var model {"name" model-id
              "views" model-views
              "throttle" model-throttle
              "deps" model-deps})
  (xt/x:set-key models model-id model)
  (return model))

(defn.xt add-model
  "adds a model and runs its initial refresh"
  {:added "4.1"}
  [node space-id model-id views]
  (var model (-/add-model-attach node space-id model-id views))
  (xt/x:set-key model "init" (-/refresh-model node space-id model-id {} nil))
  (return model))

(defn.xt remove-model
  "removes the model"
  {:added "4.1"}
  [node space-id model-id]
  (var runtime (-/ensure-space-page node space-id))
  (var models (xt/x:get-key runtime "models"))
  (var dependents (-/get-model-dependents node space-id model-id))
  (when (> (xt/x:len (xt/x:obj-keys dependents)) 0)
    (xt/x:err (xt/x:cat "ERR - existing model dependents - "
                        (xt/x:json-encode dependents))))
  (var curr (xt/x:get-key models model-id))
  (xt/x:del-key models model-id)
  (return curr))

(defn.xt remove-view
  "removes the view"
  {:added "4.1"}
  [node space-id model-id view-id]
  (var dependents (-/get-view-dependents node space-id model-id view-id))
  (when (> (xt/x:len (xt/x:obj-keys dependents)) 0)
    (xt/x:err (xt/x:cat "ERR - existing view dependents - "
                        (xt/x:json-encode dependents))))
  (var model (-/model-get node space-id model-id))
  (when model
    (var views (xt/x:get-key model "views"))
    (var curr (xt/x:get-key views view-id))
    (xt/x:del-key views view-id)
    (return curr)))

(defn.xt model-update
  "updates a model"
  {:added "4.1"}
  [node space-id model-id event]
  (var model (-/model-ensure node space-id model-id))
  (var throttle (xt/x:get-key model "throttle"))
  (var views (xt/x:get-key model "views"))
  (var out [])
  (xt/for:object [[view-id _] views]
    (var entry (th/throttle-run throttle view-id [(or event {})]))
    (xt/x:arr-push out [view-id (xt/x:get-key entry "promise")]))
  (return
   (promise/x:promise-then
    (promise/x:promise-all (xt/x:arr-map out xt/x:second))
    (fn [arr]
      (return (xtd/arr-zip (xt/x:arr-map out xt/x:first)
                           arr))))))

(defn.xt view-update
  "updates a view"
  {:added "4.1"}
  [node space-id model-id view-id event]
  (var [model view] (-/view-ensure node space-id model-id view-id))
  (var throttle (xt/x:get-key model "throttle"))
  (var entry (th/throttle-run throttle view-id [(or event {})]))
  (return (xt/x:get-key entry "promise")))

(defn.xt view-set-input
  "sets the view input"
  {:added "4.1"}
  [node space-id model-id view-id current event]
  (var [model view] (-/view-ensure node space-id model-id view-id))
  (event-view/set-input view current)
  (return (-/view-update node space-id model-id view-id (or event {}))))

(defn.xt trigger-model-raw
  "triggers a model"
  {:added "4.1"}
  [node space-id model signal event]
  (var views (xt/x:get-key model "views"))
  (var out [])
  (xt/for:object [[view-id view] views]
    (var options (xt/x:get-key view "options"))
    (var trigger (xt/x:get-key options "trigger"))
    (var check (-/check-event trigger signal event {"view" view
                                                    "model" model
                                                    "node" node
                                                    "space_id" space-id}))
    (when check
      (th/throttle-run (xt/x:get-key model "throttle")
                       view-id
                       [event])
      (xt/x:arr-push out view-id)))
  (return out))

(defn.xt trigger-model
  "triggers a model"
  {:added "4.1"}
  [node space-id model-id signal event]
  (var model (-/model-ensure node space-id model-id))
  (return (-/trigger-model-raw node space-id model signal event)))

(defn.xt trigger-view
  "triggers a view"
  {:added "4.1"}
  [node space-id model-id view-id signal event]
  (var [model view] (-/view-ensure node space-id model-id view-id))
  (var options (xt/x:get-key view "options"))
  (var trigger (xt/x:get-key options "trigger"))
  (when (-/check-event trigger signal event {"view" view
                                             "model" model
                                             "node" node
                                             "space_id" space-id})
    (var entry (th/throttle-run (xt/x:get-key model "throttle")
                                view-id
                                [event]))
    (return (xt/x:get-key entry "promise")))
  (return nil))

(defn.xt trigger-all
  "triggers all models in a space"
  {:added "4.1"}
  [node space-id signal event]
  (var models (xt/x:get-key (-/ensure-space-page node space-id) "models"))
  (var out {})
  (xt/for:object [[model-id model] models]
    (xt/x:set-key out
                  model-id
                  (-/trigger-model-raw node space-id model signal event)))
  (return out))

(defn.xt raw-callback-id
  "creates a stable node trigger id for one page space"
  {:added "4.1"}
  [space-id]
  (return (xt/x:cat "@/raw/page/" (or space-id ""))))

(defn.xt register-page-trigger
  "registers a raw page trigger directly on the node"
  {:added "4.1"}
  [node signal trigger-fn meta]
  (var entry {"id" signal
             "fn" trigger-fn
             "meta" (or meta {})})
  (xt/x:set-key (xt/x:get-key node "triggers")
               signal
               entry)
  (return entry))

(defn.xt unregister-page-trigger
  "removes a raw page trigger directly from the node"
  {:added "4.1"}
  [node signal]
  (var triggers (xt/x:get-key node "triggers"))
  (var prev (xt/x:get-key triggers signal))
  (xt/x:del-key triggers signal)
  (return prev))

(defn.xt add-raw-callback
  "adds a raw substrate trigger callback for one space"
  {:added "4.1"}
  [node space-id]
  (var trigger-id (-/raw-callback-id space-id))
  (return
   (-/register-page-trigger
    node
    trigger-id
    (fn [_space frame local-node]
      (return (-/trigger-all local-node
                             space-id
                             (xt/x:get-key frame "signal")
                             frame)))
    {"space_id" space-id})))

(defn.xt remove-raw-callback
  "removes the raw substrate trigger callback for one space"
  {:added "4.1"}
  [node space-id]
  (return (-/unregister-page-trigger node (-/raw-callback-id space-id))))
