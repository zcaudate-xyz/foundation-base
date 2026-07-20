(ns js.react.view.polyfill
  "React-specific substrate view polyfills expressed as lower-level IR."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate.view :as view]]})

(defn.js registry
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
      (xt/x:set-key props "rows" (or (xt/x:get-key props "rows") 4))
      (return (view/node "ui/input" props [])))}))
