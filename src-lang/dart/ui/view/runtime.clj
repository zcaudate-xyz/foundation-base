(ns dart.ui.view.runtime
  "Dart/Wind runtime adapter for serializable xt.substrate.view IR."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate.view :as view]
             [dart.ui.view.backend :as backend]
             [dart.ui.view.polyfill :as polyfill]]})

(defn.dt runtime-create
  [node spec render-fn opts]
  (:= opts (or opts {}))
  (var registry (backend/registry (xt/x:get-key opts "overrides")
                                  (or (xt/x:get-key opts "polyfills")
                                      (polyfill/registry))))
  (var local {})
  (xt/for:object [[binding-id binding] (or (xt/x:get-key spec "bindings") {})]
    (when (== "local" (xt/x:get-key binding "source"))
      (xt/x:set-key local binding-id (xt/x:get-key binding "initial"))))
  (var runtime {"backend" "wind"
                "node" node
                "spec" spec
                "render_fn" render-fn
                "registry" registry
                "space_id" (xt/x:get-key opts "space_id")
                "local" local
                "local_revision" 0
                "invalidate" nil
                "listener_id" (or (xt/x:get-key opts "listener_id")
                                  (xt/x:cat "wind/" (xt/x:get-key spec "id") "/"
                                             (xt/x:to-string (xt/x:now-ms))))
                "subscription" nil})
  (xt/x:set-key runtime "dispatch"
                (fn [action-desc event]
                  (return (view/dispatch node
                                         (xt/x:get-key runtime "space_id")
                                         action-desc event nil))))
  (return runtime))

(defn.dt snapshot
  [runtime]
  (var out (view/snapshot (xt/x:get-key runtime "node")
                          (xt/x:get-key runtime "spec")))
  (xt/x:obj-assign out (xt/x:get-key runtime "local"))
  (return out))

(defn.dt local-set
  "sets an explicitly local binding and asks the Flutter host to rebuild"
  [runtime binding-id value]
  (xt/x:set-key (xt/x:get-key runtime "local") binding-id value)
  (var revision (+ 1 (xt/x:get-key runtime "local_revision")))
  (xt/x:set-key runtime "local_revision" revision)
  (var invalidate (xt/x:get-key runtime "invalidate"))
  (when (xt/x:is-function? invalidate)
    (invalidate (-/snapshot runtime) revision
                {"source" "local" "binding_id" binding-id}))
  (return value))

(defn.dt resolve-node
  [runtime node seen]
  (when (or (xt/x:nil? node)
            (xt/x:is-string? node)
            (xt/x:is-number? node))
    (return node))
  (when (xt/x:is-array? node)
    (return (xtd/arr-map node (fn [child]
                                (return (-/resolve-node runtime child {}))))))
  (var component-id (xt/x:get-key node "component"))
  (var registry (xt/x:get-key runtime "registry"))
  (var override (xt/x:get-key (xt/x:get-key registry "overrides") component-id))
  (var native (backend/native-entry registry component-id))
  (when (or override native)
    (return node))
  (when (xt/x:has-key? seen component-id)
    (xt/x:err (xt/x:cat "view polyfill cycle [wind] - " component-id)))
  (var lowering (xt/x:get-key (xt/x:get-key registry "polyfills") component-id))
  (when (not (xt/x:is-function? lowering))
    (xt/x:err (xt/x:cat "view implementation missing [wind] - " component-id)))
  (xt/x:set-key seen component-id true)
  (return (-/resolve-node runtime (lowering node) seen)))

(defn.dt prepare-node
  [runtime node state]
  (when (xt/x:nil? node) (return nil))
  (when (or (xt/x:is-string? node) (xt/x:is-number? node))
    (return {"type" "WText"
             "props" {"text" (xt/x:to-string node)}
             "children" []}))
  (when (xt/x:is-array? node)
    (return (xtd/arr-map node (fn [child]
                                (return (-/prepare-node runtime child state))))))
  (when (== true (xt/x:get-key (or (xt/x:get-key node "props") {}) "hidden"))
    (return {"type" "WText"
             "props" {"text" ""}
             "children" []}))
  (:= node (-/resolve-node runtime node {}))
  (var component-id (xt/x:get-key node "component"))
  (var registry (xt/x:get-key runtime "registry"))
  (var children (-/prepare-node runtime
                                (or (xt/x:get-key node "children") [])
                                state))
  (var override (xt/x:get-key (xt/x:get-key registry "overrides") component-id))
  (when (xt/x:is-function? override)
    (return (override runtime node children state)))
  (var entry (backend/native-entry registry component-id))
  (return (backend/prepare-native runtime component-id entry
                                  (or (xt/x:get-key node "props") {})
                                  children state)))

(defn.dt prepare
  "returns the WDynamic json/action bundle for the current substrate snapshot"
  [runtime]
  (var snapshot (-/snapshot runtime))
  (var concrete ((xt/x:get-key runtime "render_fn") snapshot))
  (view/validate (view/view-spec (xt/x:get-key (xt/x:get-key runtime "spec") "id")
                                (xt/x:get-key (xt/x:get-key runtime "spec") "bindings")
                                concrete))
  (var state {"next" 0 "actions" {}})
  (var json (-/prepare-node runtime concrete state))
  (return {"json" json
           "actions" (xt/x:get-key state "actions")}))

(defn.dt open
  "opens the unified substrate subscription; notify asks Flutter to rebuild"
  [runtime notify]
  (var spec (xt/x:get-key runtime "spec"))
  (xt/x:set-key runtime "invalidate" notify)
  (var subscription
       (view/subscribe (xt/x:get-key runtime "node") spec
                       (xt/x:get-key runtime "listener_id")
                       (fn [snapshot revision event]
                         (return (notify snapshot revision event)))))
  (xt/x:set-key runtime "subscription" subscription)
  (return runtime))

(defn.dt close
  [runtime]
  (var subscription (xt/x:get-key runtime "subscription"))
  (when subscription
    (view/unsubscribe subscription)
    (xt/x:set-key runtime "subscription" nil))
  (xt/x:set-key runtime "invalidate" nil)
  (return true))
