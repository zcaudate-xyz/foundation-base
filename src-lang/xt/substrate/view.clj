(ns xt.substrate.view
  "Serializable view specifications backed by substrate state and handlers.

   A view specification declares the substrate values it observes. Portable
   render functions consume `snapshot` and return JSON-safe view nodes; target
   adapters own rendering and explicitly local presentation state."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.event.base-listener :as event-common]
             [xt.event.base-model :as event-model]
             [xt.substrate.base-space :as node-space]
             [xt.substrate.base-util :as base-util]
             [xt.substrate.page-core :as page-core]
             [xt.substrate.view-catalog :as catalog]]})

(def$.xt VERSION 1)
(def$.xt STATE_SLOT "view")

(defspec.xt ViewSpec :xt/any)
(defspec.xt ViewNode :xt/any)
(defspec.xt ViewSnapshot :xt/any)
(defspec.xt ViewSubscription :xt/any)

(defn.xt view-spec
  "constructs a versioned, serializable view specification"
  [view-id bindings root]
  (return {"version" -/VERSION
           "id" view-id
           "bindings" (or bindings {})
           "root" root}))

(defn.xt node
  "constructs a serializable semantic view node"
  [component props children]
  (return {"component" component
           "props" (or props {})
           "children" (or children [])}))

(defn.xt action
  "constructs a substrate handler action descriptor"
  [action-id payload]
  (return {"action" action-id
           "payload" payload}))

(defn.xt event-value
  "constructs a backend-neutral event value projection"
  [path]
  (return {"$" "event"
           "path" (or path [])}))

(defn.xt json-safe?
  "checks that a value can cross the shared JS/Dart IR boundary"
  [value]
  (cond (xt/x:nil? value) (return true)
        (xt/x:is-string? value) (return true)
        (xt/x:is-number? value) (return true)
        (xt/x:is-boolean? value) (return true)
        (xt/x:is-function? value) (return false)
        (xt/x:is-array? value)
        (do (xt/for:array [item value]
              (when (not (-/json-safe? item))
                (return false)))
            (return true))
        (xt/x:is-object? value)
        (do (xt/for:object [[key item] value]
              (when (or (not (xt/x:is-string? key))
                        (not (-/json-safe? item)))
                (return false)))
            (return true))
        :else (return false)))

(defn.xt validate-binding
  [binding-id binding]
  (when (not (xt/x:is-object? binding))
    (xt/x:err (xt/x:cat "invalid view binding - " binding-id)))
  (var source (or (xt/x:get-key binding "source") "state"))
  (when (not (or (== source "state")
                 (== source "model-output")
                 (== source "model-input")
                 (== source "local")))
    (xt/x:err (xt/x:cat "invalid view binding source - " source)))
  (when (and (or (== source "model-output")
                 (== source "model-input"))
             (or (not (xt/x:is-string? (xt/x:get-key binding "group_id")))
                 (not (xt/x:is-string? (xt/x:get-key binding "model_id")))))
    (xt/x:err (xt/x:cat "model binding requires group_id/model_id - " binding-id)))
  (return true))

(defn.xt validate-node
  [value opts]
  (when (xt/x:nil? value)
    (return true))
  (when (or (xt/x:is-string? value)
            (xt/x:is-number? value))
    (return true))
  (when (xt/x:is-array? value)
    (xt/for:array [child value]
      (-/validate-node child opts))
    (return true))
  (when (not (xt/x:is-object? value))
    (xt/x:err "invalid view node"))
  (var component-id (xt/x:get-key value "component"))
  (when (not (xt/x:is-string? component-id))
    (xt/x:err "view node requires component"))
  (cond (catalog/has-component? component-id)
        (catalog/validate-props component-id (xt/x:get-key value "props"))

        (catalog/platform-id? component-id)
        (when (xt/x:get-key (or opts {}) "portable")
          (xt/x:err (xt/x:cat "platform view component not portable - "
                              component-id)))

        :else
        (xt/x:err (xt/x:cat "unknown view component - " component-id)))
  (when (not (-/json-safe? (xt/x:get-key value "props")))
    (xt/x:err (xt/x:cat "view props are not serializable - "
                        component-id)))
  (xt/for:array [child (or (xt/x:get-key value "children") [])]
    (-/validate-node child opts))
  (return true))

(defn.xt validate-with
  "validates a view specification and its current concrete root"
  [spec opts]
  (when (not (xt/x:is-object? spec))
    (xt/x:err "view spec must be an object"))
  (when (not= -/VERSION (xt/x:get-key spec "version"))
    (xt/x:err (xt/x:cat "unsupported view spec version - "
                        (xt/x:to-string (xt/x:get-key spec "version")))))
  (when (not (xt/x:is-string? (xt/x:get-key spec "id")))
    (xt/x:err "view spec requires id"))
  (xt/for:object [[binding-id binding] (or (xt/x:get-key spec "bindings") {})]
    (-/validate-binding binding-id binding))
  (-/validate-node (xt/x:get-key spec "root") opts)
  (return true))

(defn.xt validate
  "validates a view specification, allowing platform (`fg/`) components"
  [spec]
  (return (-/validate-with spec nil)))

(defn.xt validate-portable
  "validates a view specification, rejecting platform (`fg/`) components"
  [spec]
  (return (-/validate-with spec {"portable" true})))

(defn.xt state-container
  "ensures substrate-owned state for one view in a node space"
  [node space-id view-id]
  (var space (node-space/ensure-space node space-id nil))
  (var state (xt/x:get-key space "state"))
  (when (or (xt/x:nil? state)
            (not (xt/x:is-object? state)))
    (:= state {})
    (xt/x:set-key space "state" state))
  (var views (xt/x:get-key state -/STATE_SLOT))
  (when (not (xt/x:is-object? views))
    (:= views {})
    (xt/x:set-key state -/STATE_SLOT views))
  (var container (xt/x:get-key views view-id))
  (when (not (xt/x:is-object? container))
    (:= container {"values" {} "revision" 0})
    (xt/x:set-key views view-id container))
  (return container))

(defn.xt state-listener-key
  [space-id view-id]
  (return (xt/x:json-encode [space-id ["view" view-id]])))

(defn.xt model-listener-key
  [space-id group-id model-id]
  (return (xt/x:json-encode [space-id [group-id model-id]])))

(defn.xt state-get
  [node space-id view-id path default-value]
  (var values (xt/x:get-key (-/state-container node space-id view-id) "values"))
  (var value (xtd/get-in values (or path [])))
  (return (:? (xt/x:nil? value) default-value value)))

(defn.xt state-set
  "sets substrate-owned view state and notifies view subscribers"
  [node space-id view-id path value]
  (var container (-/state-container node space-id view-id))
  (var values (xt/x:get-key container "values"))
  (when (== 0 (xt/x:len (or path [])))
    (:= values value)
    (xt/x:set-key container "values" values))
  (when (> (xt/x:len (or path [])) 0)
    (xtd/set-in values path value))
  (var revision (+ 1 (or (xt/x:get-key container "revision") 0)))
  (xt/x:set-key container "revision" revision)
  (event-common/trigger-keyed-listeners
   node
   (-/state-listener-key space-id view-id)
   {"space_id" space-id
    "view_id" view-id
    "revision" revision
    "path" (or path [])
    "value" value})
  (return value))

(defn.xt binding-read
  [node view-id binding]
  (var source (or (xt/x:get-key binding "source") "state"))
  (var space-id (xt/x:get-key binding "space_id"))
  (var path (or (xt/x:get-key binding "path") []))
  (cond (== source "local")
        (return (xt/x:get-key binding "initial"))

        (== source "state")
        (return (-/state-get node space-id view-id path
                            (xt/x:get-key binding "default")))

        (== source "model-output")
        (return (xtd/get-in
                 (page-core/model-get-output node space-id
                                             (xt/x:get-key binding "group_id")
                                             (xt/x:get-key binding "model_id"))
                 path))

        (== source "model-input")
        (do (var [_group model]
                 (page-core/model-ensure node space-id
                                         (xt/x:get-key binding "group_id")
                                         (xt/x:get-key binding "model_id")))
            (return (xtd/get-in (xt/x:get-key (event-model/get-input model) "current")
                                path)))

        :else (xt/x:err (xt/x:cat "unsupported binding source - " source))))

(defn.xt snapshot
  "reads every declared binding from substrate"
  [node spec]
  (var out {})
  (var view-id (xt/x:get-key spec "id"))
  (xt/for:object [[binding-id binding] (or (xt/x:get-key spec "bindings") {})]
    (xt/x:set-key out binding-id (-/binding-read node view-id binding)))
  (return out))

(defn.xt subscription-notify
  [subscription event]
  (var revision (+ 1 (xt/x:get-key subscription "revision")))
  (xt/x:set-key subscription "revision" revision)
  (var next (-/snapshot (xt/x:get-key subscription "node")
                        (xt/x:get-key subscription "spec")))
  (xt/x:set-key subscription "snapshot" next)
  ((xt/x:get-key subscription "callback") next revision event)
  (return next))

(defn.xt subscribe
  "subscribes once to each declared substrate dependency"
  [node spec listener-id callback]
  (-/validate spec)
  (var subscription {"id" listener-id
                     "node" node
                     "spec" spec
                     "callback" callback
                     "revision" 0
                     "snapshot" (-/snapshot node spec)
                     "keys" []})
  (var seen {})
  (var view-id (xt/x:get-key spec "id"))
  (xt/for:object [[_ binding] (or (xt/x:get-key spec "bindings") {})]
    (var source (or (xt/x:get-key binding "source") "state"))
    (when (not= source "local")
      (var space-id (xt/x:get-key binding "space_id"))
      (var key (:? (== source "state")
                   (-/state-listener-key space-id view-id)
                   (-/model-listener-key space-id
                                          (xt/x:get-key binding "group_id")
                                          (xt/x:get-key binding "model_id"))))
      (when (not (xt/x:has-key? seen key))
        (xt/x:set-key seen key true)
        (xt/x:arr-push (xt/x:get-key subscription "keys") key)
        (event-common/add-keyed-listener
         node key listener-id "view"
         (fn [_id event _time _meta]
           (return (-/subscription-notify subscription event)))
         {"view_id" view-id}
         nil))))
  (return subscription))

(defn.xt unsubscribe
  [subscription]
  (var node (xt/x:get-key subscription "node"))
  (var listener-id (xt/x:get-key subscription "id"))
  (xt/for:array [key (xt/x:get-key subscription "keys")]
    (event-common/remove-keyed-listener node key listener-id))
  (xt/x:set-key subscription "keys" [])
  (return true))

(defn.xt dispatch
  "dispatches a view action through the existing substrate handler surface"
  [node space-id action-desc event meta]
  (var action-id (xt/x:get-key action-desc "action"))
  (when (not (xt/x:is-string? action-id))
    (xt/x:err "view action requires a substrate handler id"))
  (var payload (xt/x:get-key action-desc "payload"))
  (when (and (xt/x:is-object? payload)
             (== "event" (xt/x:get-key payload "$")))
    (:= payload (xtd/get-in event (or (xt/x:get-key payload "path") []))))
  (return (base-util/request node space-id action-id [payload] (or meta {}))))
