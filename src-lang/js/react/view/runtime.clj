(ns js.react.view.runtime
  "React runtime adapter for serializable xt.substrate.view IR."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]
             [js.react :as r]
             [js.react.view.backend :as backend]
             [js.react.view.polyfill :as polyfill]]})

(defn.js runtime-create
  [node spec render-fn opts]
  (:= opts (or opts {}))
  (var registry (backend/registry (xt/x:get-key opts "overrides")
                                  (or (xt/x:get-key opts "polyfills")
                                      (polyfill/registry))))
  (var local {})
  (xt/for:object [[binding-id binding] (or (xt/x:get-key spec "bindings") {})]
    (when (== "local" (xt/x:get-key binding "source"))
      (xt/x:set-key local binding-id (xt/x:get-key binding "initial"))))
  (var runtime {"backend" "react"
                "node" node
                "spec" spec
                "render_fn" render-fn
                "registry" registry
                "space_id" (xt/x:get-key opts "space_id")
                "local" local
                "invalidate" nil
                "listener_id" (or (xt/x:get-key opts "listener_id")
                                  (xt/x:cat "react/" (xt/x:get-key spec "id") "/"
                                             (xt/x:to-string (xt/x:now-ms))))})
  (xt/x:set-key runtime "dispatch"
                (fn [action-desc event]
                  (return (view/dispatch node
                                         (xt/x:get-key runtime "space_id")
                                         action-desc event nil))))
  (return runtime))

(defn.js snapshot
  [runtime]
  (var out (view/snapshot (xt/x:get-key runtime "node")
                          (xt/x:get-key runtime "spec")))
  (xt/x:obj-assign out (xt/x:get-key runtime "local"))
  (return out))

(defn.js local-set
  "sets an explicitly local binding and asks React to refresh"
  [runtime binding-id value]
  (xt/x:set-key (xt/x:get-key runtime "local") binding-id value)
  (var invalidate (xt/x:get-key runtime "invalidate"))
  (when (xt/x:is-function? invalidate)
    (invalidate))
  (return value))

(defn.js resolve-node
  "resolves overrides/native entries/polyfills and rejects missing or cyclic ids"
  [runtime node seen]
  (when (or (xt/x:nil? node)
            (xt/x:is-string? node)
            (xt/x:is-number? node))
    (return node))
  (when (xt/x:is-array? node)
    (return (. node (map (fn [child]
                           (return (-/resolve-node runtime child {})))))))
  (var component-id (xt/x:get-key node "component"))
  (var registry (xt/x:get-key runtime "registry"))
  (var override (xt/x:get-key (xt/x:get-key registry "overrides") component-id))
  (var native (xt/x:get-key (xt/x:get-key registry "native") component-id))
  (when (or override native)
    (return node))
  (when (xt/x:has-key? seen component-id)
    (xt/x:err (xt/x:cat "view polyfill cycle [react] - " component-id)))
  (var lowering (xt/x:get-key (xt/x:get-key registry "polyfills") component-id))
  (when (not (xt/x:is-function? lowering))
    (xt/x:err (xt/x:cat "view implementation missing [react] - " component-id)))
  (xt/x:set-key seen component-id true)
  (return (-/resolve-node runtime (lowering node) seen)))

(defn.js render-node
  [runtime node]
  (when (xt/x:nil? node) (return nil))
  (when (xt/x:is-string? node) (return node))
  (when (xt/x:is-number? node) (return (String node)))
  (when (xt/x:is-array? node)
    (return (. node (map (fn [child]
                           (return (-/render-node runtime child)))))))
  (:= node (-/resolve-node runtime node {}))
  (var component-id (xt/x:get-key node "component"))
  (var registry (xt/x:get-key runtime "registry"))
  (var props (or (xt/x:get-key node "props") {}))
  (var children (-/render-node runtime (or (xt/x:get-key node "children") [])))
  (var override (xt/x:get-key (xt/x:get-key registry "overrides") component-id))
  (when (xt/x:is-function? override)
    (return (override runtime props children)))
  (var entry (xt/x:get-key (xt/x:get-key registry "native") component-id))
  (return (backend/render-native runtime entry props children)))

(defn.js render
  [runtime snapshot]
  (var concrete ((xt/x:get-key runtime "render_fn") snapshot))
  (view/validate (view/view-spec (xt/x:get-key (xt/x:get-key runtime "spec") "id")
                                (xt/x:get-key (xt/x:get-key runtime "spec") "bindings")
                                concrete))
  (return (-/render-node runtime concrete)))

(defn.js View
  "React component owning only subscription lifecycle and refresh"
  [#{node spec render-fn options}]
  (var refresh (r/useRefresh))
  (var runtime-ref (r/ref (-/runtime-create node spec render-fn options)))
  (var subscription-ref (r/ref nil))
  (xt/x:set-key (r/curr runtime-ref) "invalidate" refresh)
  (r/init []
    (var subscription
         (view/subscribe node spec
                         (xt/x:get-key (r/curr runtime-ref) "listener_id")
                         (fn [_snapshot _revision _event]
                           (return (refresh)))))
    (r/curr:set subscription-ref subscription)
    (return (fn []
              (var current (r/curr subscription-ref))
              (when current (view/unsubscribe current)))))
  (var runtime (r/curr runtime-ref))
  (return (-/render runtime (-/snapshot runtime))))
