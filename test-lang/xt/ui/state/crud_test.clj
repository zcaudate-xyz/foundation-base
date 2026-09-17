(ns xt.ui.state.crud-test
  (:use code.test)
  (:require [lang.core :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.crud :as crud]
             [xt.ui.state.form :as form]]})

^{:refer xt.ui.state.crud/spec :added "4.1"}
(fact "declares the controller strategy and preserves supplied schema data"
  (!.js (crud/spec "users" nil nil nil nil nil))
  => {"id" "users" "strategy" "page_controller" "models" {} "fields" [] "columns" [] "actions" [] "opts" {}}
  (!.js (crud/spec "users" {"list" "users/list"} [{"id" "name"}] [{"id" "name"}] ["save"] {"editable" true}))
  => {"id" "users" "strategy" "page_controller" "models" {"list" "users/list"}
      "fields" [{"id" "name"}] "columns" [{"id" "name"}] "actions" ["save"] "opts" {"editable" true}})

^{:refer xt.ui.state.crud/create-state :added "4.1"}
(fact "initializes independent collection and form state for each controller"
  (!.js
   (var spec (crud/spec "users" nil nil nil nil nil))
   (var first (crud/create-state spec {"name" "Ada"}))
   (var second (crud/create-state spec {"name" "Grace"}))
   (form/set-field! (. first ["form"]) ["name"] "Changed")
   [(. first ["status"]) (. first ["mode"]) (. first ["record"]) (. first ["errors"])
    (xt/x:get-path first ["collection" "page_size"])
    (xt/x:get-path first ["form" "initial"])
    (xt/x:get-path second ["form" "draft"])])
  => ["idle" "list" nil {} 25 {"name" "Ada"} {"name" "Grace"}])

^{:refer xt.ui.state.crud/set-mode! :added "4.1"}
(fact "mode changes select and clear records without resetting the form"
  (!.js
   (var current (crud/create-state {} {"name" "Ada"}))
   (crud/set-mode! current "edit" {"id" "u-1"})
   (var edit [(. current ["mode"]) (. current ["record"])])
   (crud/set-mode! current "list" nil)
   [edit (. current ["mode"]) (. current ["record"]) (xt/x:get-path current ["form" "draft"])])
  => [["edit" {"id" "u-1"}] "list" nil {"name" "Ada"}])
