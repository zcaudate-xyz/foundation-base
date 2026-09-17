(ns xt.ui.wind-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.wind :as wind-ui]]})

^{:refer xt.ui.wind/flutter-registry :added "4.1"}
(fact "registers the complete Wind whitelist, including every table renderer"
  (!.js
   (var registry (wind-ui/flutter-registry))
   [(xt/x:len (xt/x:obj-keys (. registry ["renderers"])))
    (ui/registry-renderer registry "ui/text")
    (ui/registry-renderer registry "ui/input")
    (ui/registry-renderer registry "ui/table")
    (ui/registry-renderer registry "ui/table-header")
    (ui/registry-renderer registry "ui/table-body")
    (ui/registry-renderer registry "ui/table-row")
    (ui/registry-renderer registry "ui/table-cell")
    (xt/x:get-key (ui/registry-contract registry "ui/table") "tier")])
  => [22 "WText" "WInput" "WDiv" "WDiv" "WDiv" "WDiv" "WText" "portable"])

^{:refer xt.ui.wind/action-add! :added "4.1"}
(fact "allocates distinct action ids and distinguishes value events from payload events"
  (!.js
   (var state {"next" 5 "actions" {}})
   (var seen [])
   (var change (wind-ui/action-add! state "onChange"
                 (fn [value] (xt/x:arr-push seen value) (return value)) true))
   (var tap (wind-ui/action-add! state "onTap"
              (fn [args] (xt/x:arr-push seen args) (return (. args ["id"]))) false))
   (var change-result ((xt/x:get-path state ["actions" "xt_ui_5"]) {"_value" "Ada" "ignored" true}))
   (var tap-result ((xt/x:get-path state ["actions" "xt_ui_6"]) {"id" 7}))
   [change tap (. state ["next"]) change-result tap-result seen])
  => [{"onChange" {"action" "xt_ui_5"}} {"onTap" {"action" "xt_ui_6"}}
      7 "Ada" 7 ["Ada" {"id" 7}]])

^{:refer xt.ui.wind/normalize-props :added "4.1"}
(fact "translates portable props, drops presentation-only flags and normalizes layouts"
  (!.js
   (var state {"next" 0 "actions" {}})
   [(wind-ui/normalize-props "ui/textarea"
     {"class" "wide" "aria_label" "Biography" "read_only" true "pending" false
      "rows" 6 "value" "Ada" "hidden" false "variant" "small" "size" "sm" "tone" "info"
      "on_submit" (fn [_] (return nil))} state)
    (wind-ui/normalize-props "ui/textarea" {} state)
    (wind-ui/normalize-props "ui/row" {"class" "gap-2"} state)
    (wind-ui/normalize-props "ui/column" {} state)
    (wind-ui/normalize-props "ui/fragment" nil state)])
  => [{"className" "wide" "semanticLabel" "Biography" "readOnly" true "isLoading" false "maxLines" 6 "value" "Ada"}
      {"maxLines" 4} {"className" "flex flex-row gap-2"}
      {"className" "flex flex-col "} {"className" "flex flex-col "}])

^{:refer xt.ui.wind/prepare-node :added "4.1"}
(fact "prepares scalars, hidden nodes, slots and unknown-renderer fallbacks recursively"
  (!.js
   (var runtime (ui/runtime-create nil (wind-ui/flutter-registry) nil nil {"body" ["native"]}))
   (var state {"next" 0 "actions" {}})
   [(wind-ui/prepare-node runtime [nil "hello" 0] state)
    (wind-ui/prepare-node runtime (ui/text "hidden" {"hidden" true}) state)
    (wind-ui/prepare-node runtime (ui/slot "body" ["fallback"] nil) state)
    (wind-ui/prepare-node runtime (ui/slot "missing" ["fallback"] nil) state)
    (wind-ui/prepare-node runtime (ui/extension "native/unknown" nil ["fallback"]) state)
    (wind-ui/prepare-node runtime (ui/node "ui/label" {"value" "Name" "for" "name"} []) state)])
  => [[nil {"type" "WText" "props" {"text" "hello"}} {"type" "WText" "props" {"text" "0"}}]
      nil
      [{"type" "WText" "props" {"text" "native"}}]
      [{"type" "WText" "props" {"text" "fallback"}}]
      {"type" "WDiv" "props" {"className" "flex flex-col"}
       "children" [{"type" "WText" "props" {"text" "fallback"}}]}
      {"type" "WText" "props" {"text" "Name"} "children" []}])

^{:refer xt.ui.wind/prepare :added "4.1"}
(fact "transforms portable functions into Wind WDynamic json and actions"
  (!.js
   (var changed nil)
   (var runtime (ui/runtime-create nil (wind-ui/flutter-registry) {} {} {}))
   (var bundle
        (wind-ui/prepare
         runtime
         (ui/node "ui/column" {"class" "gap-4"}
                  [(ui/node "ui/input"
                            {"value" "Ada" "on_change" (fn [value] (:= changed value))}
                            [])])))
   (var input (xt/x:get-path bundle ["json" "children" 0]))
   (var action-id (xt/x:get-path input ["props" "onChange" "action"]))
   ((xt/x:get-path bundle ["actions" action-id]) {"_value" "Grace"})
   [(xt/x:get-path bundle ["json" "type"])
    (xt/x:get-path bundle ["json" "props" "className"])
    (xt/x:get-key input "type") changed])
  => ["WDiv" "flex flex-col gap-4" "WInput" "Grace"]
  (!.js
   (var runtime (ui/runtime-create nil (wind-ui/flutter-registry) nil nil nil))
   (var tree (ui/node "ui/button" {"on_press" (fn [args] (return args))} []))
   (var first (wind-ui/prepare runtime tree))
   (var second (wind-ui/prepare runtime tree))
   [(xt/x:get-path first ["json" "props" "onTap"])
    (xt/x:get-path second ["json" "props" "onTap"])
    (xt/x:len (xt/x:obj-keys (. second ["actions"])))
    (wind-ui/prepare runtime (ui/node "ui/table-cell" {"value" "Ada"} []))])
  => [{"action" "xt_ui_0"} {"action" "xt_ui_0"} 1
      {"json" {"type" "WText" "props" {"text" "Ada"} "children" []} "actions" {}}])
