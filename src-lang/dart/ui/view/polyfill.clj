(ns dart.ui.view.polyfill
  "Wind-specific substrate view polyfills expressed as lower-level IR.

   Components without a Wind widget lower to layout/text primitives here;
   the shared catalog remains the grammar both backends validate against."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.substrate.view :as view]
             [xt.substrate.view-catalog :as catalog]]})

(defn.dt lower-to
  "lowers a node to another component id, keeping props and children"
  [component-id]
  (var lowering
       (fn [node]
         (return (view/node component-id
                            (xt/x:get-key node "props")
                            (xt/x:get-key node "children")))))
  (return lowering))

(defn.dt lower-variant
  "lowers a variant-bearing node to another id, folding the variant's
   shared class bundle into the node's class"
  [source-id target-id]
  (var lowering
       (fn [node]
         (var props (xtd/obj-clone (or (xt/x:get-key node "props") {})))
         (var classes (catalog/variant-classes source-id
                                               (xt/x:get-key props "variant")))
         (when (xt/x:not-nil? classes)
           (var class-name (or (xt/x:get-key props "class") ""))
           (when (< 0 (xt/x:str-len class-name))
             (:= class-name (xt/x:cat class-name " ")))
           (xt/x:set-key props "class" (xt/x:cat class-name classes)))
         (xt/x:del-key props "variant")
         (return (view/node target-id props (xt/x:get-key node "children")))))
  (return lowering))

(defn.dt registry
  []
  (return
   {"ui/card"             (-/lower-to "ui/column")
    "ui/card-header"      (-/lower-to "ui/column")
    "ui/card-content"     (-/lower-to "ui/column")
    "ui/card-title"       (-/lower-to "ui/text")
    "ui/card-description" (-/lower-to "ui/text")
    "ui/card-footer"      (-/lower-to "ui/column")
    "ui/separator"        (-/lower-to "ui/row")
    "ui/badge"            (-/lower-variant "ui/badge" "ui/text")
    "ui/table"            (-/lower-to "ui/column")
    "ui/table-header"     (-/lower-to "ui/column")
    "ui/table-body"       (-/lower-to "ui/column")
    "ui/table-row"        (-/lower-to "ui/row")
    "ui/table-head"       (-/lower-to "ui/text")
    "ui/table-cell"       (-/lower-to "ui/text")
    "ui/textarea"
    (fn [node]
      (var props (xtd/obj-clone (xt/x:get-key node "props")))
      (xt/x:set-key props "maxLines" (or (xt/x:get-key props "rows") 4))
      (xt/x:del-key props "rows")
      (return (view/node "ui/input" props [])))}))
