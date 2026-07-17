(ns xt.ui.model
  "A UI-facing facade over local and proxy xt.substrate page models."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-listener]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]]})

(defspec.xt UiModelStore :xt/any)

(defn.xt store-create
  "creates a store over a local group or a remote proxy group"
  [node space-id group-id mode opts]
  (return {"node" node
           "space_id" space-id
           "group_id" group-id
           "mode" (or mode "local")
           "opts" (or opts {})
           "control" nil
           "revision" 0
           "listeners" {}}))

(defn.xt store-version
  [store]
  (return (xt/x:get-key store "revision")))

(defn.xt store-open
  [store]
  (when (not= "proxy" (xt/x:get-key store "mode"))
    (page-core/group-ensure (xt/x:get-key store "node")
                            (xt/x:get-key store "space_id")
                            (xt/x:get-key store "group_id"))
    (return (promise/x:promise-run store)))
  (return
   (promise/x:promise-then
    (page-proxy/group-sync-proxy
     (xt/x:get-key store "node")
     (xt/x:get-key store "space_id")
     (xt/x:get-key store "group_id")
     (xt/x:get-key store "opts"))
    (fn [control]
      (xt/x:set-key store "control" control)
      (return store)))))

(defn.xt model
  [store model-id]
  (var [_group current]
       (page-core/model-ensure (xt/x:get-key store "node")
                               (xt/x:get-key store "space_id")
                               (xt/x:get-key store "group_id")
                               model-id))
  (return current))

(defn.xt model-slot
  [store model-id slot path fallback]
  (var current (-/model store model-id))
  (var value (xtd/get-in current (or slot [])))
  (when (xt/x:not-nil? path)
    (:= value (xtd/get-in value path)))
  (return (:? (xt/x:nil? value) fallback value)))

(defn.xt model-input
  [store model-id path fallback]
  (return (-/model-slot store model-id ["input" "current"] path fallback)))

(defn.xt model-output
  [store model-id path fallback]
  (return (-/model-slot store model-id ["output" "current"] path fallback)))

(defn.xt model-pending?
  [store model-id]
  (return (== true (-/model-slot store model-id ["output" "pending"] nil false))))

(defn.xt model-disabled?
  [store model-id]
  (return (== true (-/model-slot store model-id ["output" "disabled"] nil false))))

(defn.xt model-error
  [store model-id]
  (when (not (== true (-/model-slot store model-id ["output" "errored"] nil false)))
    (return nil))
  (return (-/model-slot store model-id ["output" "current"] nil nil)))

(defn.xt model-remote
  [store model-id path fallback]
  (return (-/model-slot store model-id ["remote" "current"] path fallback)))

(defn.xt model-sync
  [store model-id path fallback]
  (return (-/model-slot store model-id ["sync" "current"] path fallback)))

(defn.xt set-input!
  [store model-id value event]
  (return (page-core/model-set-input
           (xt/x:get-key store "node")
           (xt/x:get-key store "space_id")
           (xt/x:get-key store "group_id")
           model-id value (or event {}))))

(defn.xt patch-input!
  "immutably patches a nested draft value before setting model input"
  [store model-id path value event]
  (var current (-/model-input store model-id nil {}))
  (when (and (xt/x:is-object? current)
             (xt/x:has-key? current "data")
             (== 1 (xt/x:len (xt/x:obj-keys current))))
    (var initial-data (xt/x:get-key current "data"))
    (:= current (:? (and (xt/x:is-object? initial-data)
                         (not (xt/x:is-array? initial-data)))
                    initial-data
                    {})))
  (var next (xt/x:json-decode (xt/x:json-encode (or current {}))))
  (xtd/set-in next path value)
  (return (-/set-input! store model-id next event)))

(defn.xt invoke!
  "invokes a command/view model; proxy dispatch is selected by page-core"
  [store model-id args]
  (return (page-core/model-remote-call
           (xt/x:get-key store "node")
           (xt/x:get-key store "space_id")
           (xt/x:get-key store "group_id")
           model-id (or args []) true)))

(defn.xt refresh!
  [store model-id event]
  (return (page-core/model-refresh
           (xt/x:get-key store "node")
           (xt/x:get-key store "space_id")
           (xt/x:get-key store "group_id")
           model-id (or event {}) nil)))

(defn.xt subscribe!
  "subscribes to every model in a group and returns a subscription id"
  [store subscription-id callback]
  (var node (xt/x:get-key store "node"))
  (var space-id (xt/x:get-key store "space_id"))
  (var group-id (xt/x:get-key store "group_id"))
  (var group (page-core/group-ensure node space-id group-id))
  (xt/for:object [[model-id _] (xt/x:get-key group "models")]
    (var key (xt/x:json-encode [space-id [group-id model-id]]))
    (event-listener/add-keyed-listener
     node key (xt/x:cat subscription-id "/" model-id) "ui.model"
     (fn [listener-id data t meta]
       (xt/x:set-key store "revision" (+ 1 (xt/x:get-key store "revision")))
       (return (callback listener-id data t meta)))
     {"model_id" model-id} nil))
  (xt/x:set-key (xt/x:get-key store "listeners") subscription-id true)
  (return subscription-id))

(defn.xt unsubscribe!
  [store subscription-id]
  (var node (xt/x:get-key store "node"))
  (var space-id (xt/x:get-key store "space_id"))
  (var group-id (xt/x:get-key store "group_id"))
  (var group (page-core/group-ensure node space-id group-id))
  (xt/for:object [[model-id _] (xt/x:get-key group "models")]
    (var key (xt/x:json-encode [space-id [group-id model-id]]))
    (event-listener/remove-keyed-listener
     node key (xt/x:cat subscription-id "/" model-id)))
  (xt/x:del-key (xt/x:get-key store "listeners") subscription-id)
  (return true))

(defn.xt store-close
  [store]
  (var listener-ids (xt/x:obj-keys (xt/x:get-key store "listeners")))
  (xt/for:array [listener-id listener-ids]
    (-/unsubscribe! store listener-id))
  (var control (xt/x:get-key store "control"))
  (when (and (xt/x:not-nil? control)
             (xt/x:is-function? (xt/x:get-key control "close")))
    (return ((xt/x:get-key control "close"))))
  (return (promise/x:promise-run true)))
