(ns js.ui.figma-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]
             [js.ui.figma :as figma-ui]]})

^{:refer js.ui.figma/normalize-props :added "4.1"}
(fact "normalizes portable event and Tailwind property names"
  (!.js
   (var seen nil)
   (var props (figma-ui/normalize-props
               {"class" "p-4" "aria_label" "Name"
                "read_only" true
                "for" "name"
                "tone" "error"
                "on_change" (fn [value] (:= seen value))}))
   ((xt/x:get-key props "onChange") {"target" {"value" "Ada"}})
   [(xt/x:get-key props "className")
    (xt/x:get-key props "aria-label")
    (xt/x:get-key props "readOnly")
    (xt/x:get-key props "htmlFor")
    (xt/x:get-key props "variant")
    seen
    (xt/x:has-key? props "on_change")])
  => ["p-4" "Name" true "name" "destructive" "Ada" false])
