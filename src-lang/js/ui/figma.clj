(ns js.ui.figma
  "React renderer for portable xt.ui nodes using @xtalk/figma-ui."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.ui.core :as ui]
             [xt.ui.catalog :as catalog]
             [xt.ui.model :as ui-model]
             [js.react :as r]
             [js.lib.figma :as figma]]})

(defn.js normalize-props
  "maps portable property/event names onto React conventions"
  [props]
  (var out (xt/x:obj-assign {} (or props {})))
  (when (xt/x:has-key? out "class")
    (xt/x:set-key out "className" (xt/x:get-key out "class"))
    (xt/x:del-key out "class"))
  (when (xt/x:has-key? out "aria_label")
    (xt/x:set-key out "aria-label" (xt/x:get-key out "aria_label"))
    (xt/x:del-key out "aria_label"))
  (when (xt/x:has-key? out "read_only")
    (xt/x:set-key out "readOnly" (xt/x:get-key out "read_only"))
    (xt/x:del-key out "read_only"))
  (when (xt/x:has-key? out "for")
    (xt/x:set-key out "htmlFor" (xt/x:get-key out "for"))
    (xt/x:del-key out "for"))
  (when (xt/x:has-key? out "tone")
    (xt/x:set-key out "variant"
                  (:? (== "error" (xt/x:get-key out "tone"))
                      "destructive"
                      "default"))
    (xt/x:del-key out "tone"))
  (when (xt/x:has-key? out "on_press")
    (xt/x:set-key out "onClick" (xt/x:get-key out "on_press"))
    (xt/x:del-key out "on_press"))
  (when (xt/x:has-key? out "on_submit")
    (xt/x:set-key out "onSubmit" (xt/x:get-key out "on_submit"))
    (xt/x:del-key out "on_submit"))
  (when (xt/x:has-key? out "on_change")
    (var callback (xt/x:get-key out "on_change"))
    (xt/x:set-key
     out "onChange"
     (fn [event]
       (return (callback (xtd/get-in event ["target" "value"])))))
    (xt/x:del-key out "on_change"))
  (xt/x:del-key out "pending")
  (xt/x:del-key out "hidden")
  (return out))

(defn.js web-registry
  "creates the default React registry; app layers can override any renderer"
  []
  (var platform (ui/registry-create "xt.ui/react-figma"))
  (ui/registry-register-renderer platform "ui/fragment" r/Fragment)
  (ui/registry-register-renderer platform "ui/row" "div")
  (ui/registry-register-renderer platform "ui/column" "div")
  (ui/registry-register-renderer platform "ui/text" "span")
  (ui/registry-register-renderer platform "ui/icon" "span")
  (ui/registry-register-renderer platform "ui/image" "img")
  (ui/registry-register-renderer platform "ui/card" figma/Card)
  (ui/registry-register-renderer platform "ui/card-header" figma/CardHeader)
  (ui/registry-register-renderer platform "ui/card-content" figma/CardContent)
  (ui/registry-register-renderer platform "ui/title" figma/CardTitle)
  (ui/registry-register-renderer platform "ui/description" figma/CardDescription)
  (ui/registry-register-renderer platform "ui/label" figma/Label)
  (ui/registry-register-renderer platform "ui/input" figma/Input)
  (ui/registry-register-renderer platform "ui/textarea" figma/Textarea)
  (ui/registry-register-renderer platform "ui/button" figma/Button)
  (ui/registry-register-renderer platform "ui/alert" figma/Alert)
  (ui/registry-register-renderer platform "ui/spinner" figma/Skeleton)
  (return (ui/registry-compose [(catalog/registry) platform])))

(defn.js render-node
  "renders a portable node recursively through the runtime registry"
  [runtime value]
  (when (xt/x:nil? value)
    (return nil))
  (when (or (xt/x:is-string? value) (xt/x:is-number? value))
    (return value))
  (when (xt/x:is-array? value)
    (return (xtd/arr-map value (fn [child]
                                (return (-/render-node runtime child))))))
  (var component-id (xt/x:get-key value "component"))
  (when (== component-id "ui/slot")
    (return (-/render-node runtime (ui/resolve-slot runtime value))))
  (var props (xt/x:get-key value "props"))
  (when (== true (xt/x:get-key props "hidden"))
    (return nil))
  (var renderer (ui/registry-renderer (xt/x:get-key runtime "registry") component-id))
  (when (not renderer)
    (xt/x:err (xt/x:cat "ERR - missing React UI renderer - " component-id)))
  (var children (-/render-node runtime (xt/x:get-key value "children")))
  (var rprops (-/normalize-props props))
  (when (== component-id "ui/row")
    (xt/x:set-key rprops "className"
                  (xt/x:cat "flex flex-row " (or (xt/x:get-key rprops "className") ""))))
  (when (== component-id "ui/column")
    (xt/x:set-key rprops "className"
                  (xt/x:cat "flex flex-col " (or (xt/x:get-key rprops "className") ""))))
  (when (or (== component-id "ui/text")
            (== component-id "ui/title")
            (== component-id "ui/description")
            (== component-id "ui/label"))
    (:= children [(xt/x:get-key props "value")])
    (xt/x:del-key rprops "value"))
  (return (r/createElement renderer rprops children)))

(defn.js use-model-store
  "binds React rendering to the same keyed model events used by proxy/local nodes"
  [store subscription-id]
  (r/useSyncExternalStore
   (fn [notify]
     (ui-model/subscribe! store subscription-id notify)
     (return (fn [] (ui-model/unsubscribe! store subscription-id))))
   (fn [] (return (ui-model/store-version store)))
   (fn [] (return (ui-model/store-version store))))
  (return store))

(defn.js PortableView
  [#{runtime view-fn subscription-id}]
  (-/use-model-store (xt/x:get-key runtime "store")
                     (or subscription-id "xt.ui/react-view"))
  (return (-/render-node runtime (view-fn runtime))))
