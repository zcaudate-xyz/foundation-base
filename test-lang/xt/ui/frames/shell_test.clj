(ns xt.ui.frames.shell-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]
             [xt.ui.frames.core :as frame]
             [xt.ui.frames.shell :as shell]]})

^{:refer xt.ui.frames.shell/view :added "4.1"}
(fact "shells supply headings and a namespaced content slot with portable fallback"
  (!.js
   (shell/view (frame/spec "users" "shell" nil {"title" "Users" "description" "Manage users" "class" "custom"}) ["body"]))
  => {"component" "ui/column" "props" {"class" "custom"}
      "children" [{"component" "ui/column" "props" {"class" "gap-1"}
                   "children" [{"component" "ui/title" "props" {"value" "Users"} "children" []}
                               {"component" "ui/description" "props" {"value" "Manage users"} "children" []}]}
                  {"component" "ui/slot" "props" {"slot_id" "users/content"} "children" ["body"]}]}
  (!.js
   (var tree (shell/view (frame/spec "empty" "shell" nil nil) nil))
   [(ui/validate-node (widgets/registry) tree)
    (xt/x:get-path tree ["props" "class"])
    (xt/x:get-path tree ["children" 0 "children" 0 "props" "value"])
    (xt/x:get-path tree ["children" 1 "children"])])
  => [true "mx-auto w-full gap-6 p-4 md:p-8" "" []])
