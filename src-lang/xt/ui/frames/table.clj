(ns xt.ui.frames.table
  "Schema-driven operational table frame."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [frame state actions]
  (var collection (or (xt/x:get-key state "collection") state))
  (var columns (or (xt/x:get-key (xt/x:get-key frame "opts") "columns") []))
  (var header [])
  (xt/for:array [column columns]
    (xt/x:arr-push header
                   (ui/node "ui/table-cell"
                            {"value" (or (xt/x:get-key column "label")
                                         (xt/x:get-key column "id"))} [])))
  (var rows [])
  (xt/for:array [item (or (xt/x:get-key collection "items") [])]
    (var cells [])
    (xt/for:array [column columns]
      (xt/x:arr-push cells
                     (ui/node "ui/table-cell"
                              {"value" (xt/x:get-key item (xt/x:get-key column "id"))}
                              [])))
    (xt/x:arr-push rows
                   (ui/node "ui/table-row"
                            {"key" (xt/x:get-key item "id")}
                            cells)))
  (return
   (ui/node "ui/column" {"class" "gap-4"}
            [(ui/slot (xt/x:cat (xt/x:get-key frame "id") "/toolbar") [] {})
             (ui/node "ui/table" {"class" "w-full"}
                      [(ui/node "ui/table-header" {} header)
                       (ui/node "ui/table-body" {} rows)])])))
