(ns xt.ui.catalog
  "Portable semantic component contracts shared by React and Flutter."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.ui.core :as ui]]})

(defn.xt register
  [registry component-id props events slots]
  (return
   (ui/registry-register-contract
    registry
    (ui/component-contract component-id ui/PORTABLE props events slots nil))))

(defn.xt semantic-registry
  "contracts for the small presentation vocabulary used by page functions"
  []
  (var registry (ui/registry-create "xt.ui/semantic"))
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
  (return registry))

(defn.xt registry
  []
  (return (ui/registry-compose [(ui/base-registry) (-/semantic-registry)])))
