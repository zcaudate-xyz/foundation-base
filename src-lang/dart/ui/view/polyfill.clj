(ns dart.ui.view.polyfill
  "Wind-specific substrate view polyfills expressed as lower-level IR."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]]})

(defn.dt registry
  []
  (return
   {"ui/card"
    (fn [node]
      (return (view/node "ui/column"
                         (xt/x:get-key node "props")
                         (xt/x:get-key node "children"))))
    "ui/card-content"
    (fn [node]
      (return (view/node "ui/column"
                         (xt/x:get-key node "props")
                         (xt/x:get-key node "children"))))
    "ui/description"
    (fn [node]
      (return (view/node "ui/text"
                         (xt/x:get-key node "props")
                         (xt/x:get-key node "children"))))
    "ui/textarea"
    (fn [node]
      (var props (xt/x:obj-clone (xt/x:get-key node "props")))
      (xt/x:set-key props "maxLines" (or (xt/x:get-key props "rows") 4))
      (xt/x:del-key props "rows")
      (return (view/node "ui/input" props [])))}))
