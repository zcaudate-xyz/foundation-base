(ns dart.ui.view.backend
  "Native Wind widget mappings for substrate view component ids."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]]})

(defn.dt native-registry
  []
  (return
   {"ui/fragment" {"type" "WDiv" "layout" "column"}
    "ui/row" {"type" "WDiv" "layout" "row"}
    "ui/column" {"type" "WDiv" "layout" "column"}
    "ui/text" {"type" "WText" "value_prop" "value"}
    "ui/title" {"type" "WText" "value_prop" "value" "role" "title"}
    "ui/label" {"type" "WText" "value_prop" "value"}
    "ui/input" {"type" "WInput" "input" true}
    "ui/button" {"type" "WButton" "press" true}
    "ui/alert" {"type" "WDiv" "layout" "column"}
    "ui/spinner" {"type" "WText" "value" "Loading…"}}))

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
  [runtime component-id input state entry]
  (var out {})
  (xt/for:object [[key value] (or input {})]
    (cond (== key "class") (xt/x:set-key out "className" value)
          (== key "class_name") (xt/x:set-key out "className" value)
          (== key "aria_label") (xt/x:set-key out "semanticLabel" value)
          (== key "read_only") (xt/x:set-key out "readOnly" value)
          (== key "pending") (xt/x:set-key out "isLoading" value)
          (== key "on_press")
          (xt/x:obj-assign out (-/action-add runtime state "onTap" value false))
          (== key "on_change")
          (xt/x:obj-assign out (-/action-add runtime state "onChange" value true))
          (or (== key "variant")
              (== key "tone")
              (== key "style")) nil
          :else (xt/x:set-key out key value)))
  (var layout (xt/x:get-key entry "layout"))
  (when layout
    (var base (:? (== layout "row") "flex flex-row" "flex flex-col"))
    (xt/x:set-key out "className"
                  (xt/x:cat base " " (or (xt/x:get-key out "className") ""))))
  (var value-prop (xt/x:get-key entry "value_prop"))
  (when value-prop
    (xt/x:set-key out "text" (or (xt/x:get-key input value-prop) ""))
    (xt/x:del-key out value-prop))
  (when (xt/x:get-key entry "value")
    (xt/x:set-key out "text" (xt/x:get-key entry "value")))
  (return out))

(defn.dt prepare-native
  [runtime component-id entry props children state]
  (return {"type" (xt/x:get-key entry "type")
           "props" (-/props runtime component-id props state entry)
           "children" children}))
