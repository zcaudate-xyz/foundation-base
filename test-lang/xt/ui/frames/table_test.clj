(ns xt.ui.frames.table-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]
             [xt.ui.frames.core :as frame]
             [xt.ui.frames.table :as table]]})

^{:refer xt.ui.frames.table/view :added "4.1"}
(fact "tables retain column order, row keys and false or zero cell values"
  (!.js
   (var spec (frame/spec "users" "table" nil {"columns" [{"id" "count" "label" "Count"} {"id" "active"}]}))
   (var data {"items" [{"id" "u-1" "count" 0 "active" false} {"id" "u-2" "count" 3 "active" true}]})
   (var direct (table/view spec data {}))
   (var wrapped (table/view spec {"collection" data} {}))
   [(ui/validate-node (widgets/registry) direct)
    (== (xt/x:json-encode direct) (xt/x:json-encode wrapped))
    (xt/x:get-path direct ["children" 0])
    (xt/x:get-path direct ["children" 1 "children" 0])
    (xt/x:get-path direct ["children" 1 "children" 1])])
  => [true true
      {"component" "ui/slot" "props" {"slot_id" "users/toolbar"} "children" []}
      {"component" "ui/table-header" "props" {}
       "children" [{"component" "ui/table-cell" "props" {"value" "Count"} "children" []}
                   {"component" "ui/table-cell" "props" {"value" "active"} "children" []}]}
      {"component" "ui/table-body" "props" {}
       "children" [{"component" "ui/table-row" "props" {"key" "u-1"}
                    "children" [{"component" "ui/table-cell" "props" {"value" 0} "children" []}
                                {"component" "ui/table-cell" "props" {"value" false} "children" []}]}
                   {"component" "ui/table-row" "props" {"key" "u-2"}
                    "children" [{"component" "ui/table-cell" "props" {"value" 3} "children" []}
                                {"component" "ui/table-cell" "props" {"value" true} "children" []}]}]}]
  (!.js
   (var empty (table/view (frame/spec "empty" "table" nil nil) {} nil))
   [(xt/x:get-path empty ["children" 1 "children" 0 "children"])
    (xt/x:get-path empty ["children" 1 "children" 1 "children"])])
  => [[] []])
