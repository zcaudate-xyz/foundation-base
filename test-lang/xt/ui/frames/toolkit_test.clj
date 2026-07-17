(ns xt.ui.frames.toolkit-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]
             [xt.ui.frames.core :as frame]
             [xt.ui.frames.shell :as shell]
             [xt.ui.frames.form :as form-frame]
             [xt.ui.frames.table :as table-frame]
             [xt.ui.frames.detail :as detail-frame]
             [xt.ui.frames.feedback :as feedback-frame]]})

^{:refer xt.ui.frames.core/override :added "4.1"}
(fact "frame regions are declarative and independently replaceable"
  (!.js
   (var current (frame/spec "admin/users" "table" {"toolbar" "default"} {}))
   (var next (frame/override current {"toolbar" "native"}))
   [(frame/region current "toolbar" nil)
    (frame/region next "toolbar" nil)])
  => ["default" "native"])

^{:refer xt.ui.frames.table/view :added "4.1"}
(fact "generic frames build valid widget trees without owning routing"
  (!.js
   (var registry (widgets/registry))
   (var table
        (table-frame/view
         (frame/spec "admin/users" "table" {}
                     {"columns" [{"id" "handle" "label" "Handle"}]})
         {"items" [{"id" "u-1" "handle" "ada"}]}
         {}))
   (var form
        (form-frame/view
         (frame/spec "admin/user" "form" {}
                     {"fields" [{"id" "handle" "label" "Handle"}]})
         {"draft" {"handle" "ada"}
          "errors" {}
          "valid" true
          "pending" false}
         {}))
   [(ui/validate-node registry table)
    (ui/validate-node registry form)
    (xt/x:get-key table "component")
    (xt/x:has-key? table "route")])
  => [true true "ui/column" false])
