(ns xt.substrate.page-proxy
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.event.base-model :as event-model]
             [xt.substrate.base-util :as base-util]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-util :as page-util]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]]})

(def$.xt ACTION_GROUP_LIST   "@page/group-list")
(def$.xt ACTION_GROUP_OPEN   "@page/group-open")
(def$.xt ACTION_GROUP_CLOSE  "@page/group-close")
(def$.xt ACTION_GROUP_UPDATE "@page/group-update")
(def$.xt ACTION_MODEL_UPDATE "@page/model-update")
(def$.xt ACTION_MODEL_SET_INPUT "@page/model-set-input")
(def$.xt ACTION_MODEL_TRIGGER "@page/model-trigger")
(def$.xt ACTION_MODEL_PROXY_CALL "@page/model-proxy-call")
(def$.xt ACTION_GROUP_TRIGGER "@page/group-trigger")

(def$.xt SIGNAL_OUTPUT "page.model/output")
(def$.xt SIGNAL_INPUT  "page.model/input")

(def$.xt LISTENER_OUTPUT "@/page-proxy/output")
(def$.xt LISTENER_INPUT  "@/page-proxy/input")

;;;
;;; SERIALIZATION
;;;

(defn.xt model-serialize-input
  "serializes the input record for transport"
  {:added "4.1"}
  [input]
  (return {"current" (. input ["current"])
           "updated" (. input ["updated"])}))

(defn.xt model-serialize-output
  "serializes an output record for transport"
  {:added "4.1"}
  [output]
  (return {"type"      (. output ["type"])
           "current"   (. output ["current"])
           "updated"   (. output ["updated"])
           "elapsed"   (. output ["elapsed"])
           "pending"   (. output ["pending"])
           "disabled"  (. output ["disabled"])
           "errored"   (. output ["errored"])
           "tag"       (. output ["tag"])}))

(defn.xt model-serialize
  "captures a serializable snapshot of model state"
  {:added "4.1"}
  [model]
  (var input  (event-model/get-input model))
  (var output (event-model/get-output model nil))
  (var #{remote sync} model)
  (var out {"input"  (-/model-serialize-input input)
            "output" (-/model-serialize-output output)})
  (when (xt/x:not-nil? remote)
    (xt/x:set-key out "remote" (-/model-serialize-output remote)))
  (when (xt/x:not-nil? sync)
    (xt/x:set-key out "sync" (-/model-serialize-output sync)))
  (return out))

(defn.xt group-snapshot
  "captures a serializable snapshot of all models in a group"
  {:added "4.1"}
  [node space-id group-id]
  (var group (page-core/group-get node space-id group-id))
  (when (xt/x:nil? group)
    (return {}))
  (var #{models} group)
  (var out {})
  (xt/for:object [[model-id model] models]
    (xt/x:set-key out model-id (-/model-serialize model)))
  (return out))

(defn.xt model-get-output
  "returns the current output value of a page model"
  {:added "4.1"}
  [node space-id group-id model-id]
  (return (page-core/model-get-output node space-id group-id model-id)))

;;;
;;; SERVER HANDLERS
;;;

(defn.xt publish-model-output
  "publishes a model output change to subscribed transports"
  {:added "4.1"}
  [node space-id path output]
  (var target-ids (router/target-ids node space-id -/SIGNAL_OUTPUT))
  (when (== 0 (xt/x:len target-ids))
    (return nil))
  (return
   (base-util/publish node
                      space-id
                      -/SIGNAL_OUTPUT
                      {"path" path
                       "output" (-/model-serialize-output output)}
                      {})))

(defn.xt publish-model-input
  "publishes a model input change to subscribed transports"
  {:added "4.1"}
  [node space-id path input]
  (var target-ids (router/target-ids node space-id -/SIGNAL_INPUT))
  (when (== 0 (xt/x:len target-ids))
    (return nil))
  (return
   (base-util/publish node
                      space-id
                      -/SIGNAL_INPUT
                      {"path" path
                       "input" (-/model-serialize-input input)}
                      {})))

(defn.xt ensure-model-listeners
  "adds proxy-publish listeners to a model if not present"
  {:added "4.1"}
  [node space-id group-id model-id model]
  (var listeners-map (. model ["listeners"]))
  (when (xt/x:nil? (xt/x:get-key listeners-map -/LISTENER_OUTPUT))
    (event-model/add-listener
     model
     -/LISTENER_OUTPUT
     (fn [_id data _t meta]
       (return (-/publish-model-output node space-id [group-id model-id] (. data ["data"]))))
     nil
     (fn [event]
       (return (== "model.output" (. event ["type"]))))))
  (when (xt/x:nil? (xt/x:get-key listeners-map -/LISTENER_INPUT))
    (event-model/add-listener
     model
     -/LISTENER_INPUT
     (fn [_id data _t meta]
       (return (-/publish-model-input node space-id [group-id model-id] (. data ["data"]))))
     nil
     (fn [event]
       (return (== "model.input" (. event ["type"]))))))
  (return model))

(defn.xt group-handle-list
  "lists page groups and model ids available on the server"
  {:added "4.1"}
  [space args request node]
  (var space-id (xt/x:first args))
  (var runtime (page-core/space-ensure-page node space-id))
  (var #{groups} runtime)
  (var out {})
  (xt/for:object [[group-id group] groups]
    (var #{models} group)
    (var model-ids [])
    (xt/for:object [[model-id _] models]
      (xt/x:arr-push model-ids model-id))
    (xt/x:set-key out group-id {"models" model-ids}))
  (return out))

(defn.xt group-handle-open
  "opens a group to a proxy client and returns a snapshot"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var space-id (. payload ["space"]))
  (var group-id (. payload ["group"]))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (var group (page-core/group-get node space-id group-id))
  (when (xt/x:nil? group)
    (return {"error" "group not found"
             "space" space-id
             "group" group-id}))
  (var #{models} group)
  (xt/for:object [[model-id model] models]
    (-/ensure-model-listeners node space-id group-id model-id model))
  (when (xt/x:not-nil? transport-id)
    (router/add-subscription node transport-id space-id -/SIGNAL_OUTPUT nil {})
    (router/add-subscription node transport-id space-id -/SIGNAL_INPUT nil {}))
  (var #{init} group)
  (if (xt/x:not-nil? init)
    (return
     (promise/x:promise-then
      init
      (fn [_]
        (return {"space"  space-id
                 "group"  group-id
                 "models" (-/group-snapshot node space-id group-id)}))))
    (return {"space"  space-id
             "group"  group-id
             "models" (-/group-snapshot node space-id group-id)})))

(defn.xt group-handle-close
  "closes a proxy group subscription"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var space-id (. payload ["space"]))
  (var group-id (. payload ["group"]))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (when (xt/x:not-nil? transport-id)
    (router/remove-subscription node transport-id space-id -/SIGNAL_OUTPUT)
    (router/remove-subscription node transport-id space-id -/SIGNAL_INPUT))
  (return {"status" "closed"
           "space"  space-id
           "group"  group-id}))

(defn.xt group-handle-update
  "handles a proxy group update request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (return
   (-> (page-core/group-update node
                               (. payload ["space"])
                               (. payload ["group"])
                               (or (. payload ["event"]) {}))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"}))))))

(defn.xt model-handle-update
  "handles a proxy model update request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (return
   (page-core/model-update node
                           (. payload ["space"])
                           (. payload ["group"])
                           (. payload ["model"])
                           (or (. payload ["event"]) {}))))

(defn.xt model-handle-set-input
  "handles a proxy model set-input request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (return
   (-> (page-core/model-set-input node
                                 (. payload ["space"])
                                 (. payload ["group"])
                                 (. payload ["model"])
                                 (. payload ["current"])
                                 (or (. payload ["event"]) {}))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"}))))))

(defn.xt model-handle-trigger
  "handles a proxy model trigger request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var out (page-core/model-trigger node
                                    (. payload ["space"])
                                    (. payload ["group"])
                                    (. payload ["model"])
                                    (. payload ["signal"])
                                    (or (. payload ["event"]) {})))
  (return {"status"    "ok"
           "triggered" (xt/x:not-nil? out)}))

(defn.xt group-handle-trigger
  "handles a proxy group trigger request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var out (page-core/group-trigger node
                                    (. payload ["space"])
                                    (. payload ["group"])
                                    (. payload ["signal"])
                                    (or (. payload ["event"]) {})))
  (return {"status" "ok"
           "models" out}))

(defn.xt model-handle-proxy-call
  "handles a proxy model proxy-call request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var space-id (. payload ["space"]))
  (var group-id (. payload ["group"]))
  (var model-id (. payload ["model"]))
  (return
   (-> (page-core/model-remote-call node
                                    space-id
                                    group-id
                                    model-id
                                    (or (. payload ["args"]) [])
                                    (. payload ["save_output"]))
       (promise/x:promise-then
        (fn [_]
          (var model-value (page-core/model-ensure node space-id group-id model-id))
          (var [_group model] model-value)
          (return {"status" "ok"
                   "output" (-/model-serialize-output
                             (. model ["output"]))})))
       (promise/x:promise-catch
        (fn [err]
          (return {"status" "error"
                   "message" (xt/x:ex-message err)
                   "stack" (. err ["stack"])
                   "data" (xt/x:ex-data err)}))))))

(defn.xt install-handlers
  "installs page-proxy request handlers on a node"
  {:added "4.1"}
  [node]
  (base-util/register-handler node -/ACTION_GROUP_LIST -/group-handle-list nil)
  (base-util/register-handler node -/ACTION_GROUP_OPEN -/group-handle-open nil)
  (base-util/register-handler node -/ACTION_GROUP_CLOSE -/group-handle-close nil)
  (base-util/register-handler node -/ACTION_GROUP_UPDATE -/group-handle-update nil)
  (base-util/register-handler node -/ACTION_MODEL_UPDATE -/model-handle-update nil)
  (base-util/register-handler node -/ACTION_MODEL_SET_INPUT -/model-handle-set-input nil)
  (base-util/register-handler node -/ACTION_MODEL_TRIGGER -/model-handle-trigger nil)
  (base-util/register-handler node -/ACTION_MODEL_PROXY_CALL -/model-handle-proxy-call nil)
  (base-util/register-handler node -/ACTION_GROUP_TRIGGER -/group-handle-trigger nil)
  (return node))

;;;
;;; CLIENT PROXY MODELS
;;;

(defn.xt model-create-proxy
  "creates a lightweight proxy model from a server snapshot"
  {:added "4.1"}
  [node space-id group-id model-id snapshot]
  (var identity-fn (fn [x] (return x)))
  (var nil-fn      (fn [] (return nil)))
  (var input-snapshot  (. snapshot ["input"]))
  (var output-snapshot (. snapshot ["output"]))
  (var model
       (event-common/blank-container
        "event.model"
        {"pipeline" {}
         "options"  {}
         "input"    {"current" (. input-snapshot ["current"])
                     "updated" (. input-snapshot ["updated"])
                     "default" nil-fn}
         "output"   {"type"      (. output-snapshot ["type"])
                     "current"   (. output-snapshot ["current"])
                     "updated"   (. output-snapshot ["updated"])
                     "elapsed"   (. output-snapshot ["elapsed"])
                     "pending"   (. output-snapshot ["pending"])
                     "disabled"  (. output-snapshot ["disabled"])
                     "errored"   (. output-snapshot ["errored"])
                     "tag"       (. output-snapshot ["tag"])
                     "process"   identity-fn
                     "default"   nil-fn}}))
  (var remote-snapshot (. snapshot ["remote"]))
  (when (xt/x:not-nil? remote-snapshot)
    (xt/x:set-key model "remote" {"type"      (. remote-snapshot ["type"])
                                  "current"   (. remote-snapshot ["current"])
                                  "updated"   (. remote-snapshot ["updated"])
                                  "elapsed"   (. remote-snapshot ["elapsed"])
                                  "pending"   (. remote-snapshot ["pending"])
                                  "disabled"  (. remote-snapshot ["disabled"])
                                  "errored"   (. remote-snapshot ["errored"])
                                  "tag"       (. remote-snapshot ["tag"])
                                  "process"   identity-fn
                                  "default"   nil-fn}))
  (var sync-snapshot (. snapshot ["sync"]))
  (when (xt/x:not-nil? sync-snapshot)
    (xt/x:set-key model "sync" {"type"      (. sync-snapshot ["type"])
                                "current"   (. sync-snapshot ["current"])
                                "updated"   (. sync-snapshot ["updated"])
                                "elapsed"   (. sync-snapshot ["elapsed"])
                                "pending"   (. sync-snapshot ["pending"])
                                "disabled"  (. sync-snapshot ["disabled"])
                                "errored"   (. sync-snapshot ["errored"])
                                "tag"       (. sync-snapshot ["tag"])
                                "process"   identity-fn
                                "default"   nil-fn}))
  (event-model/add-listener
   model
   "@/page"
   (fn [_id data _t meta]
     (var emitted (xt/x:obj-assign {} data))
     (xt/x:set-key emitted "meta" meta)
     (return
      (page-core/trigger-listeners
       node
       space-id
       [group-id model-id]
       emitted)))
   nil
   nil)
  (return model))


;;;
;;; PROXY DISPATCHER
;;;

(defn.xt proxy-dispatch-op
  "forwards a single proxy page operation to the server over a given transport"
  {:added "4.1"}
  [node transport-id op space-id group-id args]
  (cond (== op "group-update")
        (do (var event (xtd/nth args 0))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_GROUP_UPDATE
                                       [{"space" space-id
                                         "group" group-id
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "model-update")
        (do (var model-id (xtd/nth args 0))
            (var event    (xtd/nth args 1))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_MODEL_UPDATE
                                       [{"space" space-id
                                         "group" group-id
                                         "model" model-id
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "model-set-input")
        (do (var model-id (xtd/nth args 0))
            (var current  (xtd/nth args 1))
            (var event    (xtd/nth args 2))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_MODEL_SET_INPUT
                                       [{"space" space-id
                                         "group" group-id
                                         "model" model-id
                                         "current" current
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "trigger-model")
        (do (var model-id (xtd/nth args 0))
            (var signal   (xtd/nth args 1))
            (var event    (xtd/nth args 2))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_MODEL_TRIGGER
                                       [{"space" space-id
                                         "group" group-id
                                         "model" model-id
                                         "signal" signal
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "trigger-group")
        (do (var signal (xtd/nth args 0))
            (var event  (xtd/nth args 1))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_GROUP_TRIGGER
                                       [{"space" space-id
                                         "group" group-id
                                         "signal" signal
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "proxy-call")
        (do (var model-id   (xtd/nth args 0))
            (var call-args  (xtd/nth args 1))
            (var save-output (xtd/nth args 2))
            (return (base-util/request node
                                       space-id
                                       -/ACTION_MODEL_PROXY_CALL
                                       [{"space" space-id
                                         "group" group-id
                                         "model" model-id
                                         "args" call-args
                                         "save_output" save-output}]
                                       {"transport_id" transport-id})))

        :else
        (return nil)))

(defn.xt proxy-dispatcher
  "forwards local page operations to the server owning the proxy group"
  {:added "4.1"}
  [op node space-id group-id args]
  (var group (page-core/group-get node space-id group-id))
  (var dispatch-fn (. group ["proxy_dispatch"]))
  (return (dispatch-fn op node space-id group-id args)))

(defn.xt group-create-proxy
  "creates a proxy group on the client from a server snapshot"
  {:added "4.1"}
  [node space-id group-id snapshot remote-spec]
  (var runtime (page-core/space-ensure-page node space-id))
  (var #{groups} runtime)
  (var group-models {})
  (xt/for:object [[model-id model-snapshot] snapshot]
    (xt/x:set-key group-models
                  model-id
                  (-/model-create-proxy node space-id group-id model-id model-snapshot)))
  (var #{transport-id} remote-spec)
  (var dispatch-fn (fn [op node space-id group-id args]
                     (return (-/proxy-dispatch-op node transport-id op space-id group-id args))))
  (var group {"name"    group-id
              "models"  group-models
              "remote"  remote-spec
              "proxy_dispatch" dispatch-fn
              "deps"    {}
              "throttle" nil})
  (xt/x:set-key groups group-id group)
  (return group))

(defn.xt model-apply-output
  "applies an inbound output delta to a proxy model"
  {:added "4.1"}
  [space stream node]
  (var #{data} stream)
  (var space-id (. stream ["space"]))
  (var #{path} data)
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (var #{output} data)
  (var group (page-core/group-get node space-id group-id))
  (when (or (xt/x:nil? group)
            (not (page-core/proxy-group? group)))
    (return nil))
  (var model (xtd/get-in group ["models" model-id]))
  (when (xt/x:nil? model)
    (return nil))
  (xt/x:obj-assign (. model ["output"]) output)
  (return (event-model/trigger-listeners model "model.output" (. model ["output"]))))

(defn.xt model-apply-input
  "applies an inbound input delta to a proxy model"
  {:added "4.1"}
  [space stream node]
  (var #{data} stream)
  (var space-id (. stream ["space"]))
  (var #{path} data)
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (var #{input} data)
  (var group (page-core/group-get node space-id group-id))
  (when (or (xt/x:nil? group)
            (not (page-core/proxy-group? group)))
    (return nil))
  (var model (xtd/get-in group ["models" model-id]))
  (when (xt/x:nil? model)
    (return nil))
  (xt/x:obj-assign (. model ["input"]) input)
  (return (event-model/trigger-listeners model "model.input" (. model ["input"]))))

(defn.xt install-triggers
  "installs client stream triggers for proxy page deltas"
  {:added "4.1"}
  [node]
  (base-util/register-trigger node -/SIGNAL_OUTPUT -/model-apply-output nil)
  (base-util/register-trigger node -/SIGNAL_INPUT -/model-apply-input nil)
  (return node))

;;;
;;; PUBLIC API
;;;

(defn.xt install
  "installs page-proxy protocol on a node (both client and server)"
  {:added "4.1"}
  [node]
  (-/install-handlers node)
  (-/install-triggers node)
  (return node))

(defn.xt group-list-proxy
  "queries a server for available page groups"
  {:added "4.1"}
  [node space-id opts]
  (var #{transport-id} opts)
  (return (base-util/request node
                             space-id
                             -/ACTION_GROUP_LIST
                             [space-id]
                             {"transport_id" transport-id})))

(defn.xt group-open-proxy
  "opens a proxy page group on a client and creates proxy models"
  {:added "4.1"}
  [node space-id group-id opts]
  (var #{transport-id} opts)
  (var existing-group (page-core/group-get node space-id group-id))
  (var remote-spec (or (and existing-group (. existing-group ["remote"]))
                       opts))
  (return
   (-> (base-util/request node
                          space-id
                          -/ACTION_GROUP_OPEN
                          [{"space" space-id
                            "group" group-id}]
                          {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [response]
          (var #{error} response)
          (when (xt/x:not-nil? error)
            (xt/x:err (xt/x:cat "ERR - " error)))
          (var snapshot (. response ["models"]))
          (-/group-create-proxy node space-id group-id snapshot remote-spec)
          (return (page-core/group-get node space-id group-id)))))))

(defn.xt group-close-proxy
  "closes a proxy page group and removes proxy models"
  {:added "4.1"}
  [node space-id group-id opts]
  (var #{transport-id} opts)
  (return
   (-> (base-util/request node
                          space-id
                          -/ACTION_GROUP_CLOSE
                          [{"space" space-id
                            "group" group-id}]
                          {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [_]
          (var runtime (page-core/space-ensure-page node space-id))
          (var #{groups} runtime)
          (xt/x:del-key groups group-id)
          (return nil))))))

(defn.xt model-proxy-call
  "invokes the proxy-call path on a proxy page model"
  {:added "4.1"}
  [node space-id group-id model-id args save-output opts]
  (var group (page-core/group-get node space-id group-id))
  (var dispatch-fn (. group ["proxy_dispatch"]))
  (return
   (-> (dispatch-fn "proxy-call" node space-id group-id [model-id args save-output])
       (promise/x:promise-then
        (fn [response]
          (var #{output} response)
          (when (xt/x:not-nil? output)
            (-/model-apply-output
             space-id
             {"space" space-id
              "data" {"path" [group-id model-id]
                      "output" output}}
             node))
          (return response))))))

(defn.xt group-sync-proxy
  "opens a proxy group and returns a bidirectional sync control handle"
  {:added "4.1"}
  [node space-id group-id opts]
  (-/install node)
  (var #{transport-id} opts)
  (return
   (-> (-/group-open-proxy node space-id group-id opts)
       (promise/x:promise-then
        (fn [group]
          (return {"space"        space-id
                   "group"        group-id
                   "group-obj"    group
                   "transport_id" transport-id
                   "close"        (fn []
                                   (return (-/group-close-proxy
                                            node space-id group-id opts)))}))))))
