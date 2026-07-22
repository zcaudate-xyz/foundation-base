(ns xt.ui.frames.detail
  "Schema-driven record detail frame."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [frame record]
  (var fields (or (. frame ["opts"] ["fields"]) []))
  (var children [])
  (xt/for:array [field fields]
    (var id (. field ["id"]))
    (xt/x:arr-push
     children
     (ui/node "ui/row" {"class" "justify-between gap-4" "key" id}
              [(ui/node "ui/label" {"value" (or (. field ["label"]) id)} [])
               (ui/text (or (xt/x:get-key record id) "") {})])))
  (return (ui/node "ui/card-content" {"class" "flex flex-col gap-3"} children)))
