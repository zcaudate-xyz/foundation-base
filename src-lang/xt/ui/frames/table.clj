(ns xt.ui.frames.table
  "Schema-driven operational table frame."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [frame state actions]
  (var collection (or (. state ["collection"]) state))
  (var columns (or (. frame ["opts"] ["columns"]) []))
  (var header [])
  (xt/for:array [column columns]
    (xt/x:arr-push header
                   (ui/node "ui/table-cell"
                            {"value" (or (. column ["label"])
                                         (. column ["id"]))} [])))
  (var rows [])
  (xt/for:array [item (or (. collection ["items"]) [])]
    (var cells [])
    (xt/for:array [column columns]
      (xt/x:arr-push cells
                     (ui/node "ui/table-cell"
                              {"value" (xt/x:get-key item (. column ["id"]))}
                              [])))
    (xt/x:arr-push rows
                   (ui/node "ui/table-row"
                            {"key" (. item ["id"])}
                            cells)))
  (return
   (ui/node "ui/column" {"class" "gap-4"}
            [(ui/slot (xt/x:cat (. frame ["id"]) "/toolbar") [] {})
             (ui/node "ui/table" {"class" "w-full"}
                      [(ui/node "ui/table-header" {} header)
                       (ui/node "ui/table-body" {} rows)])])))
