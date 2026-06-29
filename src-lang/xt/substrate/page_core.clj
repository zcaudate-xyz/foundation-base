(ns xt.substrate.page-core
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.event.base-model :as event-model]
             [xt.event.util-throttle :as th]
             [xt.substrate.base-space :as node-space]]})

(def$.xt STATE_TAG "substrate.page")
(def$.xt STATE_SLOT "page")

(def.xt PROXY_DISPATCHER nil)

(defn.xt set-proxy-dispatcher
  "sets the proxy operation dispatcher"
  {:added "4.1"}
  [dispatcher]
  (:= -/PROXY_DISPATCHER dispatcher)
  (return dispatcher))

(defn.xt get-proxy-dispatcher
  "gets the proxy operation dispatcher"
  {:added "4.1"}
  []
  (return -/PROXY_DISPATCHER))

(defn.xt proxy-group?
  "checks if a group is a proxy for a remote page group"
  {:added "4.1"}
  [group]
  (return (xt/x:get-key group "remote")))

(defn.xt wrap-space-args
  "puts the model context as first argument"
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
           "groups" {}
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

(defn.xt group-get
  "gets a page model record from a node space"
  {:added "4.1"}
  [node space-id group-id]
  (return (xtd/get-in (-/ensure-space-page node space-id)
                      ["groups" group-id])))

(defn.xt group-ensure
  "gets a registered page model or throws"
  {:added "4.1"}
  [node space-id group-id]
  (var group (-/group-get node space-id group-id))
  (when (xt/x:nil? group)
    (xt/x:err (xt/x:cat "ERR - Group not found - " group-id)))
  (return group))

(defn.xt model-ensure
  "gets a registered page model or throws"
  {:added "4.1"}
  [node space-id group-id model-id]
  (var group (-/group-ensure node space-id group-id))
  (var model (xt/x:get-key (xt/x:get-key group "models") model-id))
  (when (xt/x:nil? model)
    (xt/x:err (xt/x:cat "ERR - Model not found - "
                        (xt/x:json-encode [group-id model-id]))))
  (return [group model]))

(defn.xt get-current-output
  "returns the current output value of a page model"
  {:added "4.1"}
  [node space-id group-id model-id]
  (var [_group model] (-/model-ensure node space-id group-id model-id))
  (return (xtd/get-in model ["output" "current"])))

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

(defn.xt prep-model
  "prepares params of models"
  {:added "4.1"}
  [node space-id group-id model-id opts]
  (var path [group-id model-id])
  (var space (node-space/ensure-space node space-id nil))
  (var [group model] (-/model-ensure node space-id group-id model-id))
  (var [context disabled]
       (event-model/pipeline-prep
        model
        (xt/x:obj-assign {"path" path
                          "node" node
                          "space" space
                          "group" group}
                         (or opts {}))))
  (return [path context disabled]))

(defn.xt get-model-dependents
  "gets all dependents for a model"
  {:added "4.1"}
  [node space-id group-id model-id]
  (var out {})
  (var groups (xt/x:get-key (-/ensure-space-page node space-id) "groups"))
  (xt/for:object [[dgroup-id dgroup] groups]
    (var deps (xt/x:get-key dgroup "deps"))
    (var model-lu (xtd/get-in deps [group-id model-id]))
    (when (xt/x:not-nil? model-lu)
      (xt/x:set-key out dgroup-id (xt/x:obj-keys model-lu))))
  (return out))

(defn.xt get-group-dependents
  "gets all dependents for a group"
  {:added "4.1"}
  [node space-id group-id]
  (var out {})
  (var groups (xt/x:get-key (-/ensure-space-page node space-id) "groups"))
  (xt/for:object [[dgroup-id dgroup] groups]
    (var deps (xt/x:get-key dgroup "deps"))
    (var group-lu (xt/x:get-key deps group-id))
    (when (xt/x:not-nil? group-lu)
      (xt/x:set-key out dgroup-id true)))
  (return out))

(defn.xt run-tail-call
  "helper function for tail calls on run commands"
  {:added "4.1"}
  [context refresh-deps-fn]
  (var acc (xt/x:get-key context "acc"))
  (var path (xt/x:get-key context "path"))
  (var node (xt/x:get-key context "node"))
  (var space-id (xt/x:get-key (xt/x:get-key context "space") "id"))
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (when (and acc (not (xt/x:get-key acc "error")))
    (when refresh-deps-fn
      (refresh-deps-fn node space-id group-id model-id refresh-deps-fn)))
  (return acc))

(defn.xt run-remote
  "runs the remote function"
  {:added "4.1"}
  [context save-output path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-model/pipeline-run-remote context save-output event-model/async-fn-promise nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt remote-call
  "runs the remote call"
  {:added "4.1"}
  [node space-id group-id model-id args save-output]
  (var [group model] (-/model-ensure node space-id group-id model-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "proxy-call" node space-id group-id [model-id args save-output])))
  (var [path context disabled]
       (-/prep-model node space-id group-id model-id {"args" args}))
  (return (-/run-remote context save-output path nil)))

(defn.xt run-refresh
  "helper function for refresh"
  {:added "4.1"}
  [context disabled path refresh-deps-fn]
  (xt/x:set-key (xt/x:get-key context "acc") "path" path)
  (return
   (promise/x:promise-then
    (event-model/pipeline-run context disabled event-model/async-fn-promise nil nil)
    (fn []
      (return (-/run-tail-call context refresh-deps-fn))))))

(defn.xt refresh-model-dependents
  "refreshes model dependents"
  {:added "4.1"}
  [node space-id group-id model-id]
  (var dependents (-/get-model-dependents node space-id group-id model-id))
  (xt/for:object [[dgroup-id dmodel-ids] dependents]
    (var throttle (xt/x:get-key (-/group-ensure node space-id dgroup-id) "throttle"))
    (xt/for:array [dmodel-id dmodel-ids]
      (th/throttle-run throttle dmodel-id [])))
  (return dependents))

(defn.xt refresh-model
  "calls update on the model"
  {:added "4.1"}
  [node space-id group-id model-id event refresh-deps-fn]
  (var [path context disabled]
       (-/prep-model node space-id group-id model-id {"event" event}))
  (return (-/run-refresh context disabled path refresh-deps-fn)))

(defn.xt refresh-model-remote
  "calls update on remote function"
  {:added "4.1"}
  [node space-id group-id model-id refresh-deps-fn]
  (var [path context disabled]
       (-/prep-model node space-id group-id model-id {}))
  (return (-/run-remote context true path refresh-deps-fn)))

(defn.xt refresh-model-dependents-unthrottled
  "refreshes dependents without throttle"
  {:added "4.1"}
  [node space-id group-id model-id refresh-deps-fn]
  (var dependents (-/get-model-dependents node space-id group-id model-id))
  (var out [])
  (xt/for:object [[dgroup-id dmodel-ids] dependents]
    (xt/for:array [dmodel-id dmodel-ids]
      (xt/x:arr-push out
                     (-/refresh-model node
                                     space-id
                                     dgroup-id
                                     dmodel-id
                                     {}
                                     refresh-deps-fn))))
  (return (promise/x:promise-all out)))

(defn.xt refresh-group
  "refreshes the group"
  {:added "4.1"}
  [node space-id group-id event refresh-deps-fn]
  (var group (-/group-ensure node space-id group-id))
  (var running [])
  (xt/for:object [[model-id model] (xt/x:get-key group "models")]
    (var [path context disabled]
         (-/prep-model node space-id group-id model-id {"event" event}))
    (xt/x:arr-push running (-/run-refresh context disabled path refresh-deps-fn)))
  (return (promise/x:promise-all running)))

(defn.xt get-group-deps
  "gets group deps"
  {:added "4.1"}
  [group-id models]
  (var all-deps {})
  (xt/for:object [[model-id model-entry] models]
    (var deps (xt/x:get-key model-entry "deps"))
    (xt/for:array [path (or deps [])]
      (:= path (:? (xt/x:is-array? path) path [group-id path]))
      (xtd/set-in all-deps
                  [(xt/x:first path)
                   (xt/x:second path)
                   model-id]
                  true)))
  (return all-deps))

(defn.xt get-unknown-deps
  "gets unknown deps"
  {:added "4.1"}
  [node space-id group-id models group-deps]
  (var out [])
  (xt/for:object [[linked-group-id linked-models] group-deps]
    (cond (== group-id linked-group-id)
          (xt/for:object [[linked-model-id _] linked-models]
            (when (xt/x:nil? (xt/x:get-key models linked-model-id))
              (xt/x:arr-push out [linked-group-id linked-model-id])))

          :else
          (do (var linked-group (-/group-get node space-id linked-group-id))
              (xt/for:object [[linked-model-id _] linked-models]
                (when (or (xt/x:nil? linked-group)
                          (xt/x:nil? (xt/x:get-key (xt/x:get-key linked-group "models")
                                                   linked-model-id)))
                  (xt/x:arr-push out [linked-group-id linked-model-id]))))))
  (return out))

(defn.xt create-throttle
  "creates the throttle"
  {:added "4.1"}
  [node space-id group-id refresh-deps-fn]
  (return
   (th/throttle-create
    (fn [model-id event]
      (return
       (promise/x:promise-catch
        (-/refresh-model node space-id group-id model-id event refresh-deps-fn)
        (fn [err]
          (return err)))))
    xt/x:now-ms)))

(defn.xt create-model
  "creates a model"
  {:added "4.1"}
  [node space-id group-id model-id
   opts]
  (var handler  (xt/x:get-key opts "handler"))
  (var pipeline (xt/x:get-key opts "pipeline"))
  (var defaults (xt/x:get-key opts "defaults"))
  (var options  (xt/x:get-key opts "options"))
  (var model
       (event-model/create-model
        nil
        (xtd/obj-assign-nested
         {"main"   {"handler" handler
                    "wrapper" -/wrap-space-args}
          "remote" {"wrapper" -/wrap-space-args}
          "sync"   {"wrapper" -/wrap-space-args}}
         pipeline)
        (xt/x:get-key defaults "args")
        (xt/x:get-key defaults "output")
        (xt/x:get-key defaults "process")
        options))
  (event-model/init-model model)
  (event-model/add-listener
   model
   "@/page"
   (fn [_id data _t meta]
     (var emitted (xt/x:obj-assign {} data))
     (xt/x:set-key emitted "meta" meta)
     (return
      (-/trigger-listeners
       node
       space-id
       [group-id model-id]
       emitted)))
   nil
   nil)
  (return model))

(defn.xt add-group-attach
  "adds group statically, merging models into an existing group when present"
  {:added "4.1"}
  [node space-id group-id models]
  (var runtime (-/ensure-space-page node space-id))
  (var groups (xt/x:get-key runtime "groups"))
  (var group (xt/x:get-key groups group-id))
  (when (xt/x:nil? group)
    (:= group {"name" group-id
               "models" {}
               "specs" {}
               "throttle" (-/create-throttle node space-id group-id -/refresh-model-dependents)
               "deps" {}})
    (xt/x:set-key groups group-id group))
  (var group-models (xt/x:get-key group "models"))
  (var group-specs (xt/x:get-key group "specs"))
  (when (xt/x:nil? group-models)
    (:= group-models {})
    (xt/x:set-key group "models" group-models))
  (when (xt/x:nil? group-specs)
    (:= group-specs {})
    (xt/x:set-key group "specs" group-specs))
  (xtd/obj-assign group-specs models)
  (xt/for:object [[model-id model] models]
    (xt/x:set-key group-models
                  model-id
                  (-/create-model node space-id group-id model-id model)))
  (xt/x:set-key group "deps" (-/get-group-deps group-id group-specs))
  (return group))

(defn.xt add-group
  "adds a group and runs its initial refresh"
  {:added "4.1"}
  [node space-id group-id models]
  (var group (-/add-group-attach node space-id group-id models))
  (xt/x:set-key group "init" (-/refresh-group node space-id group-id {} nil))
  (return group))

(defn.xt remove-group
  "removes the group"
  {:added "4.1"}
  [node space-id group-id]
  (var runtime (-/ensure-space-page node space-id))
  (var groups (xt/x:get-key runtime "groups"))
  (var dependents (-/get-group-dependents node space-id group-id))
  (when (> (xt/x:len (xt/x:obj-keys dependents)) 0)
    (xt/x:err (xt/x:cat "ERR - existing group dependents - "
                        (xt/x:json-encode dependents))))
  (var curr (xt/x:get-key groups group-id))
  (xt/x:del-key groups group-id)
  (return curr))

(defn.xt remove-model
  "removes the model"
  {:added "4.1"}
  [node space-id group-id model-id]
  (var dependents (-/get-model-dependents node space-id group-id model-id))
  (when (> (xt/x:len (xt/x:obj-keys dependents)) 0)
    (xt/x:err (xt/x:cat "ERR - existing model dependents - "
                        (xt/x:json-encode dependents))))
  (var group (-/group-get node space-id group-id))
  (when group
    (var models (xt/x:get-key group "models"))
    (var curr (xt/x:get-key models model-id))
    (xt/x:del-key models model-id)
    (return curr)))

(defn.xt group-update
  "updates a group"
  {:added "4.1"}
  [node space-id group-id event]
  (var group (-/group-ensure node space-id group-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "group-update" node space-id group-id [event])))
  (var throttle (xt/x:get-key group "throttle"))
  (var models (xt/x:get-key group "models"))
  (var out [])
  (xt/for:object [[model-id _] models]
    (var entry (th/throttle-run throttle model-id [(or event {})]))
    (xt/x:arr-push out [model-id (xt/x:get-key entry "promise")]))
  (return
   (promise/x:promise-then
    (promise/x:promise-all (xt/x:arr-map out xt/x:second))
    (fn [arr]
      (return (xtd/arr-zip (xt/x:arr-map out xt/x:first)
                           arr))))))

(defn.xt model-update
  "updates a model"
  {:added "4.1"}
  [node space-id group-id model-id event]
  (var [group model] (-/model-ensure node space-id group-id model-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "model-update" node space-id group-id [model-id event])))
  (var throttle (xt/x:get-key group "throttle"))
  (var entry (th/throttle-run throttle model-id [(or event {})]))
  (return (xt/x:get-key entry "promise")))

(defn.xt model-set-input
  "sets the model input"
  {:added "4.1"}
  [node space-id group-id model-id current event]
  (var [group model] (-/model-ensure node space-id group-id model-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "model-set-input" node space-id group-id [model-id current event])))
  (event-model/set-input model current)
  (return (-/model-update node space-id group-id model-id (or event {}))))

(defn.xt trigger-group-raw
  "triggers a group"
  {:added "4.1"}
  [node space-id group signal event]
  (var models (xt/x:get-key group "models"))
  (var out [])
  (xt/for:object [[model-id model] models]
    (var options (xt/x:get-key model "options"))
    (var trigger (xt/x:get-key options "trigger"))
    (var check (-/check-event trigger signal event {"model" model
                                                    "group" group
                                                    "node" node
                                                    "space_id" space-id}))
    (when check
      (th/throttle-run (xt/x:get-key group "throttle")
                       model-id
                       [event])
      (xt/x:arr-push out model-id)))
  (return out))

(defn.xt trigger-group
  "triggers a group"
  {:added "4.1"}
  [node space-id group-id signal event]
  (var group (-/group-ensure node space-id group-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "trigger-group" node space-id group-id [signal event])))
  (return (-/trigger-group-raw node space-id group signal event)))

(defn.xt trigger-model
  "triggers a model"
  {:added "4.1"}
  [node space-id group-id model-id signal event]
  (var [group model] (-/model-ensure node space-id group-id model-id))
  (when (and (-/proxy-group? group) -/PROXY_DISPATCHER)
    (return (-/PROXY_DISPATCHER "trigger-model" node space-id group-id [model-id signal event])))
  (var options (xt/x:get-key model "options"))
  (var trigger (xt/x:get-key options "trigger"))
  (when (-/check-event trigger signal event {"model" model
                      "group" group
                                             "node" node
                                             "space_id" space-id})
    (var entry (th/throttle-run (xt/x:get-key group "throttle")
                                model-id
                                [event]))
    (return (xt/x:get-key entry "promise")))
  (return nil))

(defn.xt trigger-all
  "triggers all groups in a space"
  {:added "4.1"}
  [node space-id signal event]
  (var groups (xt/x:get-key (-/ensure-space-page node space-id) "groups"))
  (var out {})
  (xt/for:object [[group-id group] groups]
    (xt/x:set-key out
                  group-id
                  (:? (and (-/proxy-group? group) -/PROXY_DISPATCHER)
                      (-/PROXY_DISPATCHER "trigger-group" node space-id group-id [signal event])
                      (-/trigger-group-raw node space-id group signal event))))
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
