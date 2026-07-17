(ns xt.ui.core
  "Portable UI nodes, component registries, slots and native capabilities.

   UI functions return these data nodes in both JavaScript and Dart. Platform
   renderers resolve component ids through layered registries; business state
   remains in xt.substrate models."
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]]})

(defspec.xt UiNode :xt/any)
(defspec.xt UiComponentContract :xt/any)
(defspec.xt UiRegistry :xt/any)
(defspec.xt UiRuntime :xt/any)

(def$.xt PORTABLE "portable")
(def$.xt EXTENSION "extension")
(def$.xt NATIVE "native")

(defn.xt node
  "constructs a portable UI node"
  [component props children]
  (return {"component" component
           "props" (or props {})
           "children" (or children [])}))

(defn.xt text
  "constructs a portable text node"
  [value props]
  (return (-/node "ui/text"
                  (xt/x:obj-assign {"value" value} (or props {}))
                  [])))

(defn.xt slot
  "constructs a named slot with portable fallback children"
  [slot-id fallback props]
  (return (-/node "ui/slot"
                  (xt/x:obj-assign {"slot_id" slot-id} (or props {}))
                  (or fallback []))))

(defn.xt extension
  "constructs an optional native extension with a portable fallback"
  [extension-id props fallback]
  (return (-/node extension-id
                  (xt/x:obj-assign {"extension" true} (or props {}))
                  (or fallback []))))

(defn.xt component-contract
  "constructs a component contract independent of a renderer"
  [component-id tier props events slots fallback]
  (return {"id" component-id
           "tier" (or tier -/PORTABLE)
           "props" (or props [])
           "events" (or events [])
           "slots" (or slots [])
           "fallback" fallback}))

(defn.xt registry-create
  "creates a registry layer"
  [id]
  (return {"id" id
           "contracts" {}
           "renderers" {}}))

(defn.xt registry-register-contract
  "registers a contract and rejects incompatible duplicate definitions"
  [registry contract]
  (var component-id (xt/x:get-key contract "id"))
  (var contracts (xt/x:get-key registry "contracts"))
  (var existing (xt/x:get-key contracts component-id))
  (when (and existing
             (not= (xt/x:json-encode existing)
                   (xt/x:json-encode contract)))
    (xt/x:err (xt/x:cat "ERR - incompatible UI contract - " component-id)))
  (xt/x:set-key contracts component-id contract)
  (return registry))

(defn.xt registry-register-renderer
  "registers the renderer for a component in this layer"
  [registry component-id renderer]
  (xt/x:set-key (xt/x:get-key registry "renderers") component-id renderer)
  (return registry))

(defn.xt registry-compose
  "composes base, platform and application layers in that order"
  [layers]
  (var out (-/registry-create "composed"))
  (xt/for:array [layer (or layers [])]
    (xt/for:object [[component-id contract]
                    (or (xt/x:get-key layer "contracts") {})]
      (-/registry-register-contract out contract))
    (xt/for:object [[component-id renderer]
                    (or (xt/x:get-key layer "renderers") {})]
      (xt/x:set-key (xt/x:get-key out "renderers") component-id renderer)))
  (return out))

(defn.xt registry-contract
  [registry component-id]
  (return (xt/x:get-key (xt/x:get-key registry "contracts") component-id)))

(defn.xt registry-renderer
  [registry component-id]
  (return (xt/x:get-key (xt/x:get-key registry "renderers") component-id)))

(defn.xt validate-props
  [contract props]
  (var allowed (or (xt/x:get-key contract "props") []))
  (var events (or (xt/x:get-key contract "events") []))
  (xt/for:object [[prop-id _] (or props {})]
    (when (and (not (xtd/arr-some allowed
                                  (fn [allowed-id]
                                    (return (== allowed-id prop-id)))))
               (not (xtd/arr-some events
                                  (fn [event-id]
                                    (return (== event-id prop-id))))))
      (xt/x:err (xt/x:cat "ERR - unsupported UI prop - "
                          (xt/x:get-key contract "id") "." prop-id))))
  (return true))

(defn.xt validate-node
  "validates a tree against the portable component contract"
  [registry ui-node]
  (when (xt/x:nil? ui-node)
    (return true))
  (when (or (xt/x:is-string? ui-node)
            (xt/x:is-number? ui-node))
    (return true))
  (when (xt/x:is-array? ui-node)
    (xt/for:array [child ui-node]
      (-/validate-node registry child))
    (return true))
  (var component-id (xt/x:get-key ui-node "component"))
  (var contracts (xt/x:get-key registry "contracts"))
  (when (not (xt/x:has-key? contracts component-id))
    (xt/x:err (xt/x:cat "ERR - unregistered UI component - " component-id)))
  (var contract (xt/x:get-key contracts component-id))
  (-/validate-props contract (xt/x:get-key ui-node "props"))
  (xt/for:array [child (or (xt/x:get-key ui-node "children") [])]
    (-/validate-node registry child))
  (return true))

(defn.xt runtime-create
  "creates the runtime passed to portable UI functions"
  [store registry capabilities services slots]
  (return {"store" store
           "registry" registry
           "capabilities" (or capabilities {})
           "services" (or services {})
           "slots" (or slots {})}))

(defn.xt capability?
  [runtime capability-id]
  (return (== true
              (xt/x:get-key (xt/x:get-key runtime "capabilities")
                            capability-id))))

(defn.xt service
  [runtime service-id]
  (return (xt/x:get-key (xt/x:get-key runtime "services") service-id)))

(defn.xt effect!
  "invokes a typed native service and normalizes unavailable services"
  [runtime service-id args]
  (var handler (-/service runtime service-id))
  (when (not (xt/x:is-function? handler))
    (return (promise/x:promise-run
             {"status" "unavailable"
              "service" service-id})))
  (return
   (promise/x:promise-catch
    (promise/x:promise-run (handler (or args {})))
    (fn [err]
      (return {"status" "error"
               "service" service-id
               "message" (xt/x:ex-message err)
               "data" (xt/x:ex-data err)})))))

(defn.xt resolve-slot
  "returns a supplied slot value or the node's portable fallback"
  [runtime slot-node]
  (var slot-id (xtd/get-in slot-node ["props" "slot_id"]))
  (var supplied (xt/x:get-key (xt/x:get-key runtime "slots") slot-id))
  (return (:? supplied
              supplied
              (xt/x:get-key slot-node "children"))))

(defn.xt base-registry
  "portable structural component contracts"
  []
  (var registry (-/registry-create "xt.ui/base"))
  (var structural-props ["class" "style" "hidden" "key" "aria_label"])
  (xt/for:array [component-id ["ui/fragment"
                               "ui/row"
                               "ui/column"
                               "ui/text"
                               "ui/icon"
                               "ui/image"
                               "ui/slot"]]
    (-/registry-register-contract
     registry
     (-/component-contract component-id
                           -/PORTABLE
                           (:? (== component-id "ui/text")
                               ["value" "class" "style" "hidden" "key" "aria_label"]
                               (:? (== component-id "ui/slot")
                                   ["slot_id" "class" "style" "hidden" "key"]
                                   structural-props))
                           []
                           []
                           nil)))
  (return registry))
