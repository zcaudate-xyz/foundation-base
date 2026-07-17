(ns xt.ui.widgets.core
  "Semantic widget contracts and constructors shared by portable frames."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt register
  [registry component-id props events slots]
  (return
   (ui/registry-register-contract
    registry
    (ui/component-contract component-id ui/PORTABLE props events slots nil))))

(defn.xt semantic-registry
  "contracts for reusable semantic widgets"
  []
  (var registry (ui/registry-create "xt.ui/widgets"))
  (-/register registry "ui/card" ["class" "hidden" "key"] [] ["header" "footer"])
  (-/register registry "ui/card-header" ["class" "hidden" "key"] [] [])
  (-/register registry "ui/card-content" ["class" "hidden" "key"] [] [])
  (-/register registry "ui/title" ["value" "class" "hidden" "key"] [] [])
  (-/register registry "ui/description" ["value" "class" "hidden" "key"] [] [])
  (-/register registry "ui/label" ["value" "for" "class" "hidden" "key"] [] [])
  (-/register registry "ui/input"
               ["id" "value" "placeholder" "type" "class" "disabled"
                "read_only" "aria_label" "key"]
               ["on_change" "on_submit"] [])
  (-/register registry "ui/textarea"
               ["id" "value" "placeholder" "class" "disabled" "read_only"
                "rows" "aria_label" "key"]
               ["on_change"] [])
  (-/register registry "ui/button"
               ["variant" "size" "class" "disabled" "pending" "aria_label" "key"]
               ["on_press"] [])
  (-/register registry "ui/alert" ["tone" "class" "hidden" "key"] [] [])
  (-/register registry "ui/spinner" ["class" "label" "hidden" "key"] [] [])
  (-/register registry "ui/table" ["class" "hidden" "key"] [] ["header" "body"])
  (-/register registry "ui/table-header" ["class" "hidden" "key"] [] [])
  (-/register registry "ui/table-body" ["class" "hidden" "key"] [] [])
  (-/register registry "ui/table-row" ["class" "hidden" "key" "selected"] ["on_press"] [])
  (-/register registry "ui/table-cell" ["class" "hidden" "key" "value"] [] [])
  (return registry))

(defn.xt registry []
  (return (ui/registry-compose [(ui/base-registry) (-/semantic-registry)])))

(defn.xt widget [component props children]
  (return (ui/node component (or props {}) (or children []))))

(defn.xt field [component id label value props on-change]
  (return
   (ui/node "ui/column" {"class" "gap-2"}
            [(ui/node "ui/label" {"value" label "for" id} [])
             (ui/node component
                      (xt/x:obj-assign
                       {"id" id "value" (or value "") "on_change" on-change}
                       (or props {}))
                      [])])))
