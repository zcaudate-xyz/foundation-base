(ns xt.ui.state.model
  "UI-facing facade over local and proxy xt.substrate models."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-listener :as event-listener]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.page-proxy :as page-proxy]]})

(defspec.xt UiModelStore :xt/any)

(defn.xt listener-key
  "matches page-core's JSON key for controlled space/group/model identifiers"
  [space-id group-id model-id]
  (return (xt/x:cat "[\"" space-id "\",[\"" group-id "\",\"" model-id "\"]]")))

(defn.xt store-create [node space-id group-id mode opts]
  (return {"node" node
           "space_id" space-id
           "group_id" group-id
           "mode" (or mode "local")
           "opts" (or opts {})
           "control" nil
           "revision" 0
           "listeners" {}}))

(defn.xt store-version [store]
  (return (. store ["revision"])))

(defn.xt store-open [store]
  (when (not= "proxy" (. store ["mode"]))
    (page-core/group-ensure (. store ["node"])
                            (. store ["space_id"])
                            (. store ["group_id"]))
    (return (promise/x:promise-run store)))
  (return
   (promise/x:promise-then
    (page-proxy/group-sync-proxy
     (. store ["node"])
     (. store ["space_id"])
     (. store ["group_id"])
     (. store ["opts"]))
    (fn [control]
      (xt/x:set-key store "control" control)
      (return store)))))

(defn.xt model [store model-id]
  (var current-value (page-core/model-ensure (. store ["node"])
                               (. store ["space_id"])
                               (. store ["group_id"])
                               model-id))
  (var [_group current] current-value)
  (return current))

(defn.xt model-slot [store model-id slot path fallback]
  (var current (-/model store model-id))
  (var value (xtd/get-in current (or slot [])))
  (when (xt/x:not-nil? path)
    (:= value (xtd/get-in value path)))
  (return (:? (xt/x:nil? value) fallback value)))

(defn.xt model-input [store model-id path fallback]
  (return (-/model-slot store model-id ["input" "current"] path fallback)))

(defn.xt model-output [store model-id path fallback]
  (return (-/model-slot store model-id ["output" "current"] path fallback)))

(defn.xt model-pending? [store model-id]
  (return (== true (-/model-slot store model-id ["output" "pending"] nil false))))

(defn.xt model-disabled? [store model-id]
  (return (== true (-/model-slot store model-id ["output" "disabled"] nil false))))

(defn.xt model-error [store model-id]
  (when (not (== true (-/model-slot store model-id ["output" "errored"] nil false)))
    (return nil))
  (return (-/model-slot store model-id ["output" "current"] nil nil)))

(defn.xt model-remote [store model-id path fallback]
  (return (-/model-slot store model-id ["remote" "current"] path fallback)))

(defn.xt model-sync [store model-id path fallback]
  (return (-/model-slot store model-id ["sync" "current"] path fallback)))

(defn.xt set-input! [store model-id value event]
  (return (page-core/model-set-input
           (. store ["node"])
           (. store ["space_id"])
           (. store ["group_id"])
           model-id value (or event {}))))

(defn.xt patch-input! [store model-id path value event]
  (var current (-/model-input store model-id nil {}))
  (when (and (xt/x:is-object? current)
             (xt/x:has-key? current "data")
             (== 1 (xt/x:len (xt/x:obj-keys current))))
    (var initial-data (. current ["data"]))
    (:= current (:? (and (xt/x:is-object? initial-data)
                         (not (xt/x:is-array? initial-data)))
                    initial-data
                    {})))
  (var next (xtd/clone-nested (or current {})))
  (xtd/set-in next path value)
  (return (-/set-input! store model-id next event)))

(defn.xt invoke! [store model-id args]
  (return (page-core/model-remote-call
           (. store ["node"])
           (. store ["space_id"])
           (. store ["group_id"])
           model-id (or args []) true)))

(defn.xt refresh! [store model-id event]
  (return (page-core/model-refresh
           (. store ["node"])
           (. store ["space_id"])
           (. store ["group_id"])
           model-id (or event {}) nil)))

(defn.xt subscribe! [store subscription-id callback]
  (var node (. store ["node"]))
  (var space-id (. store ["space_id"]))
  (var group-id (. store ["group_id"]))
  (var group (page-core/group-ensure node space-id group-id))
  (xt/for:object [[model-id _] (. group ["models"])]
    (var key (-/listener-key space-id group-id model-id))
    (event-listener/add-keyed-listener
     node key (xt/x:cat subscription-id "/" model-id) "ui.model"
     (fn [listener-id data t meta]
       (xt/x:set-key store "revision" (+ 1 (. store ["revision"])))
       (return (callback listener-id data t meta)))
     {"model_id" model-id} nil))
  (xt/x:set-key (. store ["listeners"]) subscription-id true)
  (return subscription-id))

(defn.xt unsubscribe! [store subscription-id]
  (var node (. store ["node"]))
  (var space-id (. store ["space_id"]))
  (var group-id (. store ["group_id"]))
  (var group (page-core/group-ensure node space-id group-id))
  (xt/for:object [[model-id _] (. group ["models"])]
    (var key (-/listener-key space-id group-id model-id))
    (event-listener/remove-keyed-listener
     node key (xt/x:cat subscription-id "/" model-id)))
  (xt/x:del-key (. store ["listeners"]) subscription-id)
  (return true))

(defn.xt store-close [store]
  (var listener-ids (xt/x:obj-keys (. store ["listeners"])))
  (xt/for:array [listener-id listener-ids]
    (-/unsubscribe! store listener-id))
  (var control (. store ["control"]))
  (when (and (xt/x:not-nil? control)
             (xt/x:is-function? (. control ["close"])))
    (return ((. control ["close"]))))
  (return (promise/x:promise-run true)))
