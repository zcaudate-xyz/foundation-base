(ns xt.ui.widgets.core-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.widgets.core :as widgets]]})

^{:refer xt.ui.widgets.core/register :added "4.1"}
(fact "registers portable semantic contracts with props, events and slots"
  (!.js
   (var registry (ui/registry-create "test"))
   (var result (widgets/register registry "ui/custom" ["value"] ["on_change"] ["body"]))
   [(== result registry) (ui/registry-contract registry "ui/custom")])
  => [true {"id" "ui/custom" "tier" "portable" "props" ["value"]
            "events" ["on_change"] "slots" ["body"] "fallback" nil}])

^{:refer xt.ui.widgets.core/semantic-registry :added "4.1"}
(fact "declares semantic widgets and their event contracts"
  (!.js
   (var registry (widgets/semantic-registry))
   [(xt/x:get-key registry "id")
    (xt/x:len (xt/x:obj-keys (. registry ["contracts"])))
    (xt/x:get-path registry ["contracts" "ui/input" "events"])
    (xt/x:get-path registry ["contracts" "ui/button" "events"])
    (xt/x:get-path registry ["contracts" "ui/table" "slots"])])
  => ["xt.ui/widgets" 16 ["on_change" "on_submit"] ["on_press"] ["header" "body"]])

^{:refer xt.ui.widgets.core/registry :added "4.1"}
(fact "composes structural and semantic contracts into a valid widget tree registry"
  (!.js
   (var registry (widgets/registry))
   [(xt/x:len (xt/x:obj-keys (. registry ["contracts"])))
    (ui/validate-node registry
     (widgets/widget "ui/card" {} [(ui/text "Hello" {})
                                   (widgets/widget "ui/button" {"on_press" (fn [_] (return true))} [])]))])
  => [23 true])

^{:refer xt.ui.widgets.core/widget :added "4.1"}
(fact "constructs widgets with empty defaults and supplied children"
  (!.js [(widgets/widget "ui/card" nil nil)
         (widgets/widget "ui/alert" {"tone" "error"} ["offline"])])
  => [{"component" "ui/card" "props" {} "children" []}
      {"component" "ui/alert" "props" {"tone" "error"} "children" ["offline"]}])

^{:refer xt.ui.widgets.core/field :added "4.1"}
(fact "fields link labels to input ids, merge props and forward change values"
  (!.js
   (var seen [])
   (var field (widgets/field "ui/input" "name" "Name" nil {"placeholder" "Full name"}
                             (fn [value] (xt/x:arr-push seen value) (return value))))
   (var input (xt/x:get-path field ["children" 1]))
   (var result ((xt/x:get-path input ["props" "on_change"]) "Ada"))
   [(ui/validate-node (widgets/registry) field)
    (xt/x:get-path field ["children" 0])
    (xt/x:get-path input ["props" "id"])
    (xt/x:get-path input ["props" "value"])
    (xt/x:get-path input ["props" "placeholder"])
    result seen])
  => [true {"component" "ui/label" "props" {"value" "Name" "for" "name"} "children" []}
      "name" "" "Full name" "Ada" ["Ada"]])
