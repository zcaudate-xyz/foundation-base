(ns xt.ui.frames.form
  "Schema-driven form frame over serializable form state and actions."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.ui.core :as ui]]})

(defn.xt invoke [actions action-id payload]
  (var handler (xt/x:get-key (or actions {}) action-id))
  (when (xt/x:is-function? handler) (return (handler payload)))
  (return nil))

(defn.xt view [frame state actions]
  (var form (or (. state ["form"]) state))
  (var draft (or (. form ["draft"]) {}))
  (var errors (or (. form ["errors"]) {}))
  (var fields (or (. frame ["opts"] ["fields"]) []))
  (var children [])
  (xt/for:array [field fields]
    (var id (. field ["id"]))
    (var component (or (. field ["component"]) "ui/input"))
    (xt/x:arr-push
     children
     (ui/node "ui/column" {"class" "gap-2" "key" id}
              [(ui/node "ui/label" {"value" (or (. field ["label"]) id)
                                     "for" id} [])
               (ui/node component
                        {"id" id
                         "value" (or (xt/x:get-key draft id) "")
                         "disabled" (== true (. form ["pending"]))
                         "on_change" (fn [value]
                                       (return (-/invoke actions "set_field"
                                                          {"field" id "value" value})))} [])
               (ui/node "ui/alert" {"tone" "error"
                                     "hidden" (xt/x:nil? (xt/x:get-key errors id))}
                        [(ui/text (or (xt/x:get-key errors id) "") {})])])))
  (xt/x:arr-push
   children
   (ui/node "ui/button"
            {"pending" (== true (. form ["pending"]))
             "disabled" (or (not= true (. form ["valid"]))
                            (== true (. form ["pending"])))
             "on_press" (fn [_] (return (-/invoke actions "submit" draft)))}
            [(ui/text "Save" {})]))
  (return (ui/node "ui/column" {"class" "gap-4"} children)))
