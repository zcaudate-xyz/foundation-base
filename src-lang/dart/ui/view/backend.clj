(ns dart.ui.view.backend
  "Native Wind widget mappings for substrate view component ids.

   Styling is Tailwind class strings only (`class` -> `className`); `variant`
   props lower to the shared class bundles in xt.substrate.view-catalog.
   Platform (`fg/`) ids are figma-only and rejected here."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as xts]
             [xt.substrate.view-catalog :as catalog]]})

(defn.dt native-registry
  []
  (return
   {"ui/fragment"    {"type" "WDiv" "layout" "column"}
    "ui/row"         {"type" "WDiv" "layout" "row"}
    "ui/column"      {"type" "WDiv" "layout" "column"}
    "ui/scroll"      {"type" "WDiv" "layout" "column"}
    "ui/text"        {"type" "WText" "value_prop" "value"}
    "ui/title"       {"type" "WText" "value_prop" "value" "role" "title"}
    "ui/label"       {"type" "WText" "value_prop" "value"}
    "ui/description" {"type" "WText" "value_prop" "value"}
    "ui/icon"        {"type" "WIcon" "value_prop" "value"}
    "ui/input"       {"type" "WInput" "input" true}
    "ui/textarea"    {"type" "WInput" "input" true}
    "ui/button"      {"type" "WButton" "press" true}
    "ui/alert"       {"type" "WDiv" "layout" "column"}
    "ui/spinner"     {"type" "WText" "value_prop" "value"}
    "ui/image"       {"type" "WImage"}}))

(defn.dt native-entry
  "resolves a component id to a native entry, rejecting `fg/` platform ids"
  [registry component-id]
  (when (catalog/platform-id? component-id)
    (xt/x:err (xt/x:cat "platform view component not portable [wind] - "
                        component-id)))
  (return (xt/x:get-key (xt/x:get-key registry "native") component-id)))

(defn.dt registry
  [overrides polyfills]
  (return {"backend" "wind"
           "native" (-/native-registry)
           "polyfills" (or polyfills {})
           "overrides" (or overrides {})}))

(defn.dt action-add
  [runtime state event-id action-desc value-event]
  (var action-id (xt/x:cat "substrate_view_"
                           (xt/x:to-string (xt/x:get-key state "next"))))
  (xt/x:set-key state "next" (+ 1 (xt/x:get-key state "next")))
  (xt/x:set-key
   (xt/x:get-key state "actions") action-id
   (fn [args]
     (var event (:? value-event
                    {"value" (xt/x:get-key args "_value")}
                    (or args {})))
     (return ((xt/x:get-key runtime "dispatch") action-desc event))))
  (return {event-id {"action" action-id}}))

(defn.dt props
  "normalizes portable props to Wind props; styling is classes plus the
   shared variant class bundles"
  [runtime component-id input state entry]
  (var out {})
  (xt/for:object [[key value] (or input {})]
    (cond (== key "class") (xt/x:set-key out "className" value)
          (== key "aria_label") (xt/x:set-key out "semanticLabel" value)
          (== key "read_only") (xt/x:set-key out "readOnly" value)
          (== key "pending") (xt/x:set-key out "isLoading" value)
          (== key "hidden") nil
          (== key "variant") nil
          (== key "on_press")
          (xtd/obj-assign out (-/action-add runtime state "onTap" value false))
          (== key "on_change")
          (xtd/obj-assign out (-/action-add runtime state "onChange" value true))
          :else (xt/x:set-key out key value)))
  (var variant (xt/x:get-key input "variant"))
  (when (xt/x:not-nil? variant)
    (var classes (catalog/variant-classes component-id variant))
    (when (xt/x:not-nil? classes)
      (var current (or (xt/x:get-key out "className") ""))
      (when (< 0 (xt/x:str-len current))
        (:= current (xt/x:cat current " ")))
      (xt/x:set-key out "className" (xt/x:cat current classes))))
  (var layout (xt/x:get-key entry "layout"))
  (when layout
    (var base (:? (== layout "row") "flex flex-row" "flex flex-col"))
    (xt/x:set-key out "className"
                  (xt/x:cat base " " (or (xt/x:get-key out "className") ""))))
  (var value-prop (xt/x:get-key entry "value_prop"))
  (when value-prop
    (xt/x:set-key out "text" (or (xt/x:get-key input value-prop) ""))
    (xt/x:del-key out value-prop))
  (return out))

(defn.dt prepare-native
  [runtime component-id entry input children state]
  (return {"type" (xt/x:get-key entry "type")
           "props" (-/props runtime component-id input state entry)
           "children" children}))
