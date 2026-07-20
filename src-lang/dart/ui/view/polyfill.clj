(ns dart.ui.view.polyfill
  "Wind-specific substrate view polyfills expressed as lower-level IR.

   Components without a Wind widget lower to layout/text primitives here;
   the shared catalog remains the grammar both backends validate against."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]]})

(defn.dt lower-to
  "lowers a node to another component id, keeping props and children"
  [component-id]
  (var lowering
       (fn [node]
         (return (view/node component-id
                            (xt/x:get-key node "props")
                            (xt/x:get-key node "children")))))
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
    "ui/badge"            (-/lower-to "ui/text")
    "ui/table"            (-/lower-to "ui/column")
    "ui/table-header"     (-/lower-to "ui/column")
    "ui/table-body"       (-/lower-to "ui/column")
    "ui/table-row"        (-/lower-to "ui/row")
    "ui/table-head"       (-/lower-to "ui/text")
    "ui/table-cell"       (-/lower-to "ui/text")
    "ui/textarea"
    (fn [node]
      (var props (xt/x:obj-clone (xt/x:get-key node "props")))
      (xt/x:set-key props "maxLines" (or (xt/x:get-key props "rows") 4))
      (xt/x:del-key props "rows")
      (return (view/node "ui/input" props [])))}))
