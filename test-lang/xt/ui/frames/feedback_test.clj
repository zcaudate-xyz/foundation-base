(ns xt.ui.frames.feedback-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.ui.frames.feedback :as feedback]]})

^{:refer xt.ui.frames.feedback/view :added "4.1"}
(fact "pending takes precedence over errors and idle state returns the exact fallback"
  (!.js [(feedback/view {"pending" true "error" "offline"} "content")
         (feedback/view {"pending" false "error" "offline"} "content")
         (feedback/view {"pending" false "error" nil} ["content"])
         (feedback/view {} nil)])
  => [{"component" "ui/spinner" "props" {"label" "Loading"} "children" []}
      {"component" "ui/alert" "props" {"tone" "error"}
       "children" [{"component" "ui/text" "props" {"value" "offline"} "children" []}]}
      ["content"] nil])
