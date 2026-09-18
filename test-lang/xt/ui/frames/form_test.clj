(ns xt.ui.frames.form-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]
             [xt.ui.frames.core :as frame]
             [xt.ui.frames.form :as form]]})

^{:refer xt.ui.frames.form/invoke :added "4.1"}
(fact "invocation forwards payloads and returns nil for missing or non-function actions"
  (!.js
   [(form/invoke {"save" (fn [payload] (return {"saved" payload}))} "save" {"name" "Ada"})
    (form/invoke nil "save" {})
    (form/invoke {"save" "invalid"} "save" {})])
  => [{"saved" {"name" "Ada"}} nil nil])

^{:refer xt.ui.frames.form/view :added "4.1"}
(fact "form fields retain their own callback ids and submit the current draft"
  (!.js
   (var seen [])
   (var spec (frame/spec "user" "form" nil
                        {"fields" [{"id" "name" "label" "Name"} {"id" "bio" "component" "ui/textarea"}]}))
   (var state {"draft" {"name" "Ada" "bio" ""} "errors" {"name" "Required"} "valid" false "pending" false})
   (var actions {"set_field" (fn [payload] (xt/x:arr-push seen ["set" payload]) (return "changed"))
                "submit" (fn [payload] (xt/x:arr-push seen ["submit" payload]) (return "submitted"))})
   (var tree (form/view spec {"form" state} actions))
   (var first-result ((xt/x:get-path tree ["children" 0 "children" 1 "props" "on_change"]) "Grace"))
   (var second-result ((xt/x:get-path tree ["children" 1 "children" 1 "props" "on_change"]) "Writer"))
   (var submit-result ((xt/x:get-path tree ["children" 2 "props" "on_press"]) nil))
   [(ui/validate-node (widgets/registry) tree)
    (xt/x:get-path tree ["children" 0 "children" 0 "props"])
    (xt/x:get-path tree ["children" 1 "children" 1 "component"])
    (xt/x:get-path tree ["children" 0 "children" 1 "props" "value"])
    (xt/x:get-path tree ["children" 0 "children" 2 "props" "hidden"])
    (xt/x:get-path tree ["children" 1 "children" 2 "props" "hidden"])
    (xt/x:get-path tree ["children" 0 "children" 2 "children" 0 "props" "value"])
    [first-result second-result submit-result] seen])
  => [true {"value" "Name" "for" "name"} "ui/textarea" "Ada" false true "Required"
      ["changed" "changed" "submitted"]
      [["set" {"field" "name" "value" "Grace"}]
       ["set" {"field" "bio" "value" "Writer"}]
       ["submit" {"name" "Ada" "bio" ""}]]]
  (!.js
   (var spec (frame/spec "user" "form" nil {"fields" [{"id" "name"}]}))
   (var flags [])
   (xt/for:array [state [{"valid" true "pending" false}
                         {"valid" false "pending" false}
                         {"valid" true "pending" true}]]
     (var tree (form/view spec state nil))
     (xt/x:arr-push flags
                    [(xt/x:get-path tree ["children" 0 "children" 1 "props" "disabled"])
                     (xt/x:get-path tree ["children" 1 "props" "disabled"])
                     (xt/x:get-path tree ["children" 1 "props" "pending"])]))
   flags)
  => [[false false false] [false true false] [true true true]])
