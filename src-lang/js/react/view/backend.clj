(ns js.react.view.backend
  "Native React implementations for substrate view component ids."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [js.react :as r]]})

(defn.js native-registry
  []
  (return
   {"ui/fragment" {"tag" r/Fragment}
    "ui/row" {"tag" "div" "layout" "row"}
    "ui/column" {"tag" "div" "layout" "column"}
    "ui/text" {"tag" "span" "value_prop" "value"}
    "ui/title" {"tag" "h2" "value_prop" "value"}
    "ui/label" {"tag" "label" "value_prop" "value"}
    "ui/input" {"tag" "input" "input" true}
    "ui/button" {"tag" "button" "press" true}
    "ui/alert" {"tag" "div" "layout" "column"}
    "ui/spinner" {"tag" "span" "value" "Loading…"}}))

(defn.js registry
  [overrides polyfills]
  (return {"backend" "react"
           "native" (-/native-registry)
           "polyfills" (or polyfills {})
           "overrides" (or overrides {})}))

(defn.js style
  "maps portable layout and token props to React style"
  [props entry]
  (var out (xt/x:obj-clone (or (xt/x:get-key props "style") {})))
  (var layout (xt/x:get-key entry "layout"))
  (when layout
    (xt/x:set-key out "display" "flex")
    (xt/x:set-key out "flexDirection" layout))
  (xt/for:array [pair [["gap" "gap"]
                       ["padding" "padding"]
                       ["align" "alignItems"]
                       ["justify" "justifyContent"]]]
    (var source (xt/x:get-idx pair 0))
    (var target (xt/x:get-idx pair 1))
    (when (xt/x:not-nil? (xt/x:get-key props source))
      (xt/x:set-key out target (xt/x:get-key props source))))
  (var tone (xt/x:get-key props "tone"))
  (when (== tone "danger")
    (xt/x:set-key out "color" "#b91c1c")
    (xt/x:set-key out "backgroundColor" "#fef2f2"))
  (return out))

(defn.js dom-props
  [runtime props entry]
  (var out {})
  (xt/for:array [key ["id" "value" "placeholder" "type" "disabled"
                      "readOnly" "rows" "title" "className"]]
    (when (xt/x:has-key? props key)
      (xt/x:set-key out key (xt/x:get-key props key))))
  (var class-name (or (xt/x:get-key props "class_name")
                      (xt/x:get-key props "class")))
  (when class-name
    (xt/x:set-key out "className" class-name))
  (var aria-label (xt/x:get-key props "aria_label"))
  (when aria-label
    (xt/x:set-key out "aria-label" aria-label))
  (var style (-/style props entry))
  (when (> (xt/x:len (xt/x:obj-keys style)) 0)
    (xt/x:set-key out "style" style))
  (var on-change (xt/x:get-key props "on_change"))
  (when on-change
    (xt/x:set-key out "onChange"
                  (fn [event]
                    (return ((xt/x:get-key runtime "dispatch")
                             on-change
                             {"value" (. event target value)})))))
  (var on-press (xt/x:get-key props "on_press"))
  (when on-press
    (xt/x:set-key out "onClick"
                  (fn [event]
                    (return ((xt/x:get-key runtime "dispatch")
                             on-press event)))))
  (return out))

(defn.js render-native
  [runtime entry props children]
  (var tag (xt/x:get-key entry "tag"))
  (var dom-props (-/dom-props runtime props entry))
  (var value-prop (xt/x:get-key entry "value_prop"))
  (var value (:? value-prop (xt/x:get-key props value-prop)
                 (xt/x:get-key entry "value")))
  (return (r/createElement tag dom-props (or value children))))
