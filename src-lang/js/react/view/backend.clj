(ns js.react.view.backend
  "Native React implementations for substrate view component ids.

   Portable `ui/*` entries lower to @xtalk/figma-ui components; `fg/*` escape
   ids resolve directly against the figma-ui package. Styling is Tailwind
   class strings only (`class` -> `className`) - no style objects are built
   here."
  (:require [hara.lang :as l]))

(l/script :js
  {:import [["@xtalk/figma-ui" :as [* FigmaUi]]]
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as xts]
             [xt.substrate.view-catalog :as catalog]
             [js.react :as r]
             [js.lib.figma :as figma]]})

(defn.js native-registry
  []
  (return
   {"ui/fragment"         {"tag" r/Fragment}
    "ui/row"              {"tag" "div" "row" true}
    "ui/column"           {"tag" "div" "column" true}
    "ui/scroll"           {"tag" "div"}
    "ui/separator"        {"tag" figma/Separator}
    "ui/text"             {"tag" "span" "value_prop" "value"}
    "ui/title"            {"tag" "h2" "value_prop" "value"}
    "ui/label"            {"tag" figma/Label "value_prop" "value"}
    "ui/description"      {"tag" "p" "value_prop" "value"}
    "ui/icon"             {"tag" "span" "value_prop" "value"}
    "ui/input"            {"tag" figma/Input}
    "ui/textarea"         {"tag" figma/Textarea}
    "ui/button"           {"tag" figma/Button}
    "ui/alert"            {"tag" figma/Alert}
    "ui/spinner"          {"tag" figma/Skeleton}
    "ui/badge"            {"tag" figma/Badge "value_prop" "value"}
    "ui/image"            {"tag" "img"}
    "ui/card"             {"tag" figma/Card}
    "ui/card-header"      {"tag" figma/CardHeader}
    "ui/card-content"     {"tag" figma/CardContent}
    "ui/card-title"       {"tag" figma/CardTitle "value_prop" "value"}
    "ui/card-description" {"tag" figma/CardDescription "value_prop" "value"}
    "ui/card-footer"      {"tag" figma/CardFooter}
    "ui/table"            {"tag" figma/Table}
    "ui/table-header"     {"tag" figma/TableHeader}
    "ui/table-body"       {"tag" figma/TableBody}
    "ui/table-row"        {"tag" figma/TableRow}
    "ui/table-head"       {"tag" figma/TableHead "value_prop" "value"}
    "ui/table-cell"       {"tag" figma/TableCell "value_prop" "value"}}))

(defn.js pascal-case
  "converts a kebab-case figma id to its PascalCase export name"
  [s]
  (var out "")
  (xt/for:array [part (xts/split s "-")]
    (:= out (xt/x:cat out (xts/capitalize part))))
  (return out))

(defn.js native-entry
  "resolves a component id to a native entry, including `fg/` escape ids"
  [registry component-id]
  (var entry (xt/x:get-key (xt/x:get-key registry "native") component-id))
  (when (xt/x:not-nil? entry)
    (return entry))
  (when (catalog/platform-id? component-id)
    (var name (-/pascal-case
               (xts/substring component-id 3 (xt/x:str-len component-id))))
    (var tag (xt/x:get-key FigmaUi name))
    (when (xt/x:nil? tag)
      (xt/x:err (xt/x:cat "figma component missing [react] - " component-id)))
    (return {"tag" tag}))
  (return nil))

(defn.js registry
  [overrides polyfills]
  (return {"backend" "react"
           "native" (-/native-registry)
           "polyfills" (or polyfills {})
           "overrides" (or overrides {})}))

(defn.js dom-props
  "normalizes portable props to React DOM/figma props; styling is classes"
  [runtime props entry]
  (var out {})
  (xt/for:array [key ["id" "value" "placeholder" "type" "disabled"
                      "rows" "src" "alt" "variant" "size" "href"]]
    (when (xt/x:has-key? props key)
      (xt/x:set-key out key (xt/x:get-key props key))))
  (when (xt/x:has-key? props "for")
    (xt/x:set-key out "htmlFor" (xt/x:get-key props "for"))
    (xt/x:del-key out "for"))
  (var class-name (xt/x:get-key props "class"))
  (when (xt/x:get-key entry "row")
    (:= class-name (xt/x:cat "flex flex-row " (or class-name ""))))
  (when (xt/x:get-key entry "column")
    (:= class-name (xt/x:cat "flex flex-col " (or class-name ""))))
  (when (xt/x:get-key props "pending")
    (xt/x:set-key out "disabled" true)
    (:= class-name (xt/x:cat (or class-name "") " opacity-60 pointer-events-none")))
  (when class-name
    (xt/x:set-key out "className" class-name))
  (var aria-label (xt/x:get-key props "aria_label"))
  (when aria-label
    (xt/x:set-key out "aria-label" aria-label))
  (when (xt/x:has-key? props "read_only")
    (xt/x:set-key out "readOnly" (xt/x:get-key props "read_only")))
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
  (var value (:? value-prop
                 (xt/x:get-key props value-prop)
                 nil))
  (return (r/createElement tag dom-props (or value children))))
