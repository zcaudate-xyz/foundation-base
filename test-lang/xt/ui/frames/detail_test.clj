(ns xt.ui.frames.detail-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]
             [xt.ui.frames.core :as frame]
             [xt.ui.frames.detail :as detail]]})

^{:refer xt.ui.frames.detail/view :added "4.1"}
(fact "detail fields preserve schema order, labels, keys and missing-value fallbacks"
  (!.js
   (detail/view (frame/spec "user" "detail" nil {"fields" [{"id" "name" "label" "Full name"} {"id" "city"}]})
                {"name" "Ada"}))
  => {"component" "ui/card-content" "props" {"class" "flex flex-col gap-3"}
      "children" [{"component" "ui/row" "props" {"class" "justify-between gap-4" "key" "name"}
                   "children" [{"component" "ui/label" "props" {"value" "Full name"} "children" []}
                               {"component" "ui/text" "props" {"value" "Ada"} "children" []}]}
                  {"component" "ui/row" "props" {"class" "justify-between gap-4" "key" "city"}
                   "children" [{"component" "ui/label" "props" {"value" "city"} "children" []}
                               {"component" "ui/text" "props" {"value" ""} "children" []}]}]}
  (!.js
   (ui/validate-node (widgets/registry) (detail/view (frame/spec "empty" "detail" nil nil) {})))
  => true)
