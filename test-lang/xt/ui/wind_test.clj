(ns xt.ui.wind-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [xt.ui.wind :as wind-ui]]})

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
                            {"value" "Ada"
                             "on_change" (fn [value] (:= changed value))}
                            [])])))
   (var input (xt/x:get-key (xt/x:get-key (xt/x:get-key bundle "json") "children") 0))
   (var action-id (xt/x:get-path input ["props" "onChange" "action"]))
   ((xt/x:get-key (xt/x:get-key bundle "actions") action-id) {"_value" "Grace"})
   [(xt/x:get-path bundle ["json" "type"])
    (xt/x:get-path bundle ["json" "props" "className"])
    (xt/x:get-key input "type")
    changed])
  => ["WDiv" "flex flex-col gap-4" "WInput" "Grace"])


^{:refer xt.ui.wind/flutter-registry :added "4.1"}
(fact "TODO")

^{:refer xt.ui.wind/action-add! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.wind/normalize-props :added "4.1"}
(fact "TODO")

^{:refer xt.ui.wind/prepare-node :added "4.1"}
(fact "TODO")