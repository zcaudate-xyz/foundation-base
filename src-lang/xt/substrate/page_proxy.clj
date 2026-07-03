(ns xt.substrate.page-proxy
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-common]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-util :as page-util]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-space :as node-space]]})

(def$.xt ACTION_GROUP_LIST   "page.group/list")
(def$.xt ACTION_GROUP_OPEN   "page.group/open")
(def$.xt ACTION_GROUP_CLOSE  "page.group/close")
(def$.xt ACTION_GROUP_UPDATE "page.group/update")
(def$.xt ACTION_MODEL_UPDATE "page.model/update")
(def$.xt ACTION_MODEL_SET_INPUT "page.model/set-input")
(def$.xt ACTION_MODEL_TRIGGER "page.model/trigger")
(def$.xt ACTION_MODEL_PROXY_CALL "page.model/proxy-call")
(def$.xt ACTION_GROUP_TRIGGER "page.group/trigger")

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
  (return {"current" (xt/x:get-key input "current")
           "updated" (xt/x:get-key input "updated")}))

(defn.xt model-serialize-output
  "serializes an output record for transport"
  {:added "4.1"}
  [output]
  (return {"type"      (xt/x:get-key output "type")
           "current"   (xt/x:get-key output "current")
           "updated"   (xt/x:get-key output "updated")
           "elapsed"   (xt/x:get-key output "elapsed")
           "pending"   (xt/x:get-key output "pending")
           "disabled"  (xt/x:get-key output "disabled")
           "errored"   (xt/x:get-key output "errored")
           "tag"       (xt/x:get-key output "tag")}))

(defn.xt model-serialize
  "captures a serializable snapshot of model state"
  {:added "4.1"}
  [model]
  (var input  (event-model/get-input model))
  (var output (event-model/get-output model nil))
  (var remote (xt/x:get-key model "remote"))
  (var sync   (xt/x:get-key model "sync"))
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
  (var models (xt/x:get-key group "models"))
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
   (substrate/publish node
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
   (substrate/publish node
                      space-id
                      -/SIGNAL_INPUT
                      {"path" path
                       "input" (-/model-serialize-input input)}
                      {})))

(defn.xt ensure-model-listeners
  "adds proxy-publish listeners to a model if not present"
  {:added "4.1"}
  [node space-id group-id model-id model]
  (var listeners-map (xt/x:get-key model "listeners"))
  (when (xt/x:nil? (xt/x:get-key listeners-map -/LISTENER_OUTPUT))
    (event-model/add-listener
     model
     -/LISTENER_OUTPUT
     (fn [_id data _t meta]
       (return (-/publish-model-output node space-id [group-id model-id] (xt/x:get-key data "data"))))
     nil
     (fn [event]
       (return (== "model.output" (xt/x:get-key event "type"))))))
  (when (xt/x:nil? (xt/x:get-key listeners-map -/LISTENER_INPUT))
    (event-model/add-listener
     model
     -/LISTENER_INPUT
     (fn [_id data _t meta]
       (return (-/publish-model-input node space-id [group-id model-id] (xt/x:get-key data "data"))))
     nil
     (fn [event]
       (return (== "model.input" (xt/x:get-key event "type"))))))
  (return model))

(defn.xt group-handle-list
  "lists page groups and model ids available on the server"
  {:added "4.1"}
  [space args request node]
  (var space-id (xt/x:first args))
  (var runtime (page-core/space-ensure-page node space-id))
  (var groups (xt/x:get-key runtime "groups"))
  (var out {})
  (xt/for:object [[group-id group] groups]
    (var models (xt/x:get-key group "models"))
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
  (var space-id (xt/x:get-key payload "space"))
  (var group-id (xt/x:get-key payload "group"))
  (var transport-id (xtd/get-in request ["meta" "transport_id"]))
  (var group (page-core/group-get node space-id group-id))
  (when (xt/x:nil? group)
    (return {"error" "group not found"
             "space" space-id
             "group" group-id}))
  (var models (xt/x:get-key group "models"))
  (xt/for:object [[model-id model] models]
    (-/ensure-model-listeners node space-id group-id model-id model))
  (when (xt/x:not-nil? transport-id)
    (router/add-subscription node transport-id space-id -/SIGNAL_OUTPUT nil {})
    (router/add-subscription node transport-id space-id -/SIGNAL_INPUT nil {}))
  (var init (xt/x:get-key group "init"))
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
  (var space-id (xt/x:get-key payload "space"))
  (var group-id (xt/x:get-key payload "group"))
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
                               (xt/x:get-key payload "space")
                               (xt/x:get-key payload "group")
                               (or (xt/x:get-key payload "event") {}))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"}))))))

(defn.xt model-handle-update
  "handles a proxy model update request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (return
   (-> (page-core/model-update node
                               (xt/x:get-key payload "space")
                               (xt/x:get-key payload "group")
                               (xt/x:get-key payload "model")
                               (or (xt/x:get-key payload "event") {}))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"}))))))

(defn.xt model-handle-set-input
  "handles a proxy model set-input request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (return
   (-> (page-core/model-set-input node
                                 (xt/x:get-key payload "space")
                                 (xt/x:get-key payload "group")
                                 (xt/x:get-key payload "model")
                                 (xt/x:get-key payload "current")
                                 (or (xt/x:get-key payload "event") {}))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"}))))))

(defn.xt model-handle-trigger
  "handles a proxy model trigger request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var out (page-core/model-trigger node
                                    (xt/x:get-key payload "space")
                                    (xt/x:get-key payload "group")
                                    (xt/x:get-key payload "model")
                                    (xt/x:get-key payload "signal")
                                    (or (xt/x:get-key payload "event") {})))
  (return {"status"    "ok"
           "triggered" (xt/x:not-nil? out)}))

(defn.xt group-handle-trigger
  "handles a proxy group trigger request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var out (page-core/group-trigger node
                                    (xt/x:get-key payload "space")
                                    (xt/x:get-key payload "group")
                                    (xt/x:get-key payload "signal")
                                    (or (xt/x:get-key payload "event") {})))
  (return {"status" "ok"
           "models" out}))

(defn.xt model-handle-proxy-call
  "handles a proxy model proxy-call request"
  {:added "4.1"}
  [space args request node]
  (var payload (xt/x:first args))
  (var space-id (xt/x:get-key payload "space"))
  (var group-id (xt/x:get-key payload "group"))
  (var model-id (xt/x:get-key payload "model"))
  (return
   (-> (page-core/model-remote-call node
                                    space-id
                                    group-id
                                    model-id
                                    (or (xt/x:get-key payload "args") [])
                                    (xt/x:get-key payload "save_output"))
       (promise/x:promise-then
        (fn [_]
          (return {"status" "ok"})))
       (promise/x:promise-catch
        (fn [err]
          (return {"status" "error"
                   "message" (xt/x:ex-message err)
                   "stack" (xt/x:get-key err "stack")
                   "data" (xt/x:ex-data err)}))))))

(defn.xt install-handlers
  "installs page-proxy request handlers on a node"
  {:added "4.1"}
  [node]
  (substrate/register-handler node -/ACTION_GROUP_LIST -/group-handle-list nil)
  (substrate/register-handler node -/ACTION_GROUP_OPEN -/group-handle-open nil)
  (substrate/register-handler node -/ACTION_GROUP_CLOSE -/group-handle-close nil)
  (substrate/register-handler node -/ACTION_GROUP_UPDATE -/group-handle-update nil)
  (substrate/register-handler node -/ACTION_MODEL_UPDATE -/model-handle-update nil)
  (substrate/register-handler node -/ACTION_MODEL_SET_INPUT -/model-handle-set-input nil)
  (substrate/register-handler node -/ACTION_MODEL_TRIGGER -/model-handle-trigger nil)
  (substrate/register-handler node -/ACTION_MODEL_PROXY_CALL -/model-handle-proxy-call nil)
  (substrate/register-handler node -/ACTION_GROUP_TRIGGER -/group-handle-trigger nil)
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
  (var input-snapshot  (xt/x:get-key snapshot "input"))
  (var output-snapshot (xt/x:get-key snapshot "output"))
  (var model
       (event-common/blank-container
        "event.model"
        {"pipeline" {}
         "options"  {}
         "input"    {"current" (xt/x:get-key input-snapshot "current")
                     "updated" (xt/x:get-key input-snapshot "updated")
                     "default" nil-fn}
         "output"   {"type"      (xt/x:get-key output-snapshot "type")
                     "current"   (xt/x:get-key output-snapshot "current")
                     "updated"   (xt/x:get-key output-snapshot "updated")
                     "elapsed"   (xt/x:get-key output-snapshot "elapsed")
                     "pending"   (xt/x:get-key output-snapshot "pending")
                     "disabled"  (xt/x:get-key output-snapshot "disabled")
                     "errored"   (xt/x:get-key output-snapshot "errored")
                     "tag"       (xt/x:get-key output-snapshot "tag")
                     "process"   identity-fn
                     "default"   nil-fn}}))
  (var remote-snapshot (xt/x:get-key snapshot "remote"))
  (when (xt/x:not-nil? remote-snapshot)
    (xt/x:set-key model "remote" {"type"      (xt/x:get-key remote-snapshot "type")
                                  "current"   (xt/x:get-key remote-snapshot "current")
                                  "updated"   (xt/x:get-key remote-snapshot "updated")
                                  "elapsed"   (xt/x:get-key remote-snapshot "elapsed")
                                  "pending"   (xt/x:get-key remote-snapshot "pending")
                                  "disabled"  (xt/x:get-key remote-snapshot "disabled")
                                  "errored"   (xt/x:get-key remote-snapshot "errored")
                                  "tag"       (xt/x:get-key remote-snapshot "tag")
                                  "process"   identity-fn
                                  "default"   nil-fn}))
  (var sync-snapshot (xt/x:get-key snapshot "sync"))
  (when (xt/x:not-nil? sync-snapshot)
    (xt/x:set-key model "sync" {"type"      (xt/x:get-key sync-snapshot "type")
                                "current"   (xt/x:get-key sync-snapshot "current")
                                "updated"   (xt/x:get-key sync-snapshot "updated")
                                "elapsed"   (xt/x:get-key sync-snapshot "elapsed")
                                "pending"   (xt/x:get-key sync-snapshot "pending")
                                "disabled"  (xt/x:get-key sync-snapshot "disabled")
                                "errored"   (xt/x:get-key sync-snapshot "errored")
                                "tag"       (xt/x:get-key sync-snapshot "tag")
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

(defn.xt group-create-proxy
  "creates a proxy group on the client from a server snapshot"
  {:added "4.1"}
  [node space-id group-id snapshot remote-spec]
  (var runtime (page-core/space-ensure-page node space-id))
  (var groups (xt/x:get-key runtime "groups"))
  (var group-models {})
  (xt/for:object [[model-id model-snapshot] snapshot]
    (xt/x:set-key group-models
                  model-id
                  (-/model-create-proxy node space-id group-id model-id model-snapshot)))
  (var group {"name"    group-id
              "models"  group-models
              "remote"  remote-spec
              "deps"    {}
              "throttle" nil})
  (xt/x:set-key groups group-id group)
  (return group))

(defn.xt model-apply-output
  "applies an inbound output delta to a proxy model"
  {:added "4.1"}
  [space stream node]
  (var data (xt/x:get-key stream "data"))
  (var space-id (xt/x:get-key stream "space"))
  (var path (xt/x:get-key data "path"))
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (var output (xt/x:get-key data "output"))
  (var group (page-core/group-get node space-id group-id))
  (when (or (xt/x:nil? group)
            (not (page-core/proxy-group? group)))
    (return nil))
  (var model (xtd/get-in group ["models" model-id]))
  (when (xt/x:nil? model)
    (return nil))
  (xt/x:obj-assign (xt/x:get-key model "output") output)
  (return (event-model/trigger-listeners model "model.output" (xt/x:get-key model "output"))))

(defn.xt model-apply-input
  "applies an inbound input delta to a proxy model"
  {:added "4.1"}
  [space stream node]
  (var data (xt/x:get-key stream "data"))
  (var space-id (xt/x:get-key stream "space"))
  (var path (xt/x:get-key data "path"))
  (var group-id (xt/x:first path))
  (var model-id (xt/x:second path))
  (var input (xt/x:get-key data "input"))
  (var group (page-core/group-get node space-id group-id))
  (when (or (xt/x:nil? group)
            (not (page-core/proxy-group? group)))
    (return nil))
  (var model (xtd/get-in group ["models" model-id]))
  (when (xt/x:nil? model)
    (return nil))
  (xt/x:obj-assign (xt/x:get-key model "input") input)
  (return (event-model/trigger-listeners model "model.input" (xt/x:get-key model "input"))))

(defn.xt install-triggers
  "installs client stream triggers for proxy page deltas"
  {:added "4.1"}
  [node]
  (substrate/register-trigger node -/SIGNAL_OUTPUT -/model-apply-output nil)
  (substrate/register-trigger node -/SIGNAL_INPUT -/model-apply-input nil)
  (return node))

;;;
;;; PROXY DISPATCHER
;;;

(defn.xt proxy-dispatcher
  "forwards local page operations to the server owning the proxy group"
  {:added "4.1"}
  [op node space-id group-id args]
  (var group (page-core/group-get node space-id group-id))
  (var remote-spec (xt/x:get-key group "remote"))
  (var transport-id (xt/x:get-key remote-spec "transport_id"))
  (cond (== op "group-update")
        (do (var event (xtd/nth args 0))
            (return (substrate/request node
                                       space-id
                                       -/ACTION_GROUP_UPDATE
                                       [{"space" space-id
                                         "group" group-id
                                         "event" event}]
                                       {"transport_id" transport-id})))

        (== op "model-update")
        (do (var model-id (xtd/nth args 0))
            (var event    (xtd/nth args 1))
            (return (substrate/request node
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
            (return (substrate/request node
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
            (return (substrate/request node
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
            (return (substrate/request node
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
            (return (substrate/request node
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

;;;
;;; PUBLIC API
;;;

(defn.xt install
  "installs page-proxy protocol on a node (both client and server)"
  {:added "4.1"}
  [node]
  (-/install-handlers node)
  (-/install-triggers node)
  (page-core/proxy-dispatcher-set -/proxy-dispatcher)
  (return node))

(defn.xt group-list-proxy
  "queries a server for available page groups"
  {:added "4.1"}
  [node space-id opts]
  (var transport-id (xt/x:get-key opts "transport_id"))
  (return (substrate/request node
                             space-id
                             -/ACTION_GROUP_LIST
                             [space-id]
                             {"transport_id" transport-id})))

(defn.xt group-open-proxy
  "opens a proxy page group on a client and creates proxy models"
  {:added "4.1"}
  [node space-id group-id opts]
  (var transport-id (xt/x:get-key opts "transport_id"))
  (return
   (-> (substrate/request node
                          space-id
                          -/ACTION_GROUP_OPEN
                          [{"space" space-id
                            "group" group-id}]
                          {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [response]
          (var error (xt/x:get-key response "error"))
          (when (xt/x:not-nil? error)
            (xt/x:err (xt/x:cat "ERR - " error)))
          (var snapshot (xt/x:get-key response "models"))
          (-/group-create-proxy node space-id group-id snapshot opts)
          (return (page-core/group-get node space-id group-id)))))))

(defn.xt group-close-proxy
  "closes a proxy page group and removes proxy models"
  {:added "4.1"}
  [node space-id group-id opts]
  (var transport-id (xt/x:get-key opts "transport_id"))
  (return
   (-> (substrate/request node
                          space-id
                          -/ACTION_GROUP_CLOSE
                          [{"space" space-id
                            "group" group-id}]
                          {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [_]
          (var runtime (page-core/space-ensure-page node space-id))
          (var groups (xt/x:get-key runtime "groups"))
          (xt/x:del-key groups group-id)
          (return nil))))))

(defn.xt model-proxy-call
  "invokes the proxy-call path on a proxy page model"
  {:added "4.1"}
  [node space-id group-id model-id args save-output opts]
  (var group (page-core/group-get node space-id group-id))
  (var remote-spec (xt/x:get-key group "remote"))
  (var transport-id (xt/x:get-key remote-spec "transport_id"))
  (return (substrate/request node
                             space-id
                             -/ACTION_MODEL_PROXY_CALL
                             [{"space" space-id
                               "group" group-id
                               "model" model-id
                               "args" args
                               "save_output" save-output}]
                             {"transport_id" transport-id})))

(defn.xt group-sync-proxy
  "opens a proxy group and returns a bidirectional sync control handle"
  {:added "4.1"}
  [node space-id group-id opts]
  (-/install node)
  (var transport-id (xt/x:get-key opts "transport_id"))
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

