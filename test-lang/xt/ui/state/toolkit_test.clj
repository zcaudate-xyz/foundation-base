(ns xt.ui.state.toolkit-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.ui.state.form :as form]
             [xt.ui.state.collection :as collection]
             [xt.ui.state.crud :as crud]
             [xt.ui.state.session :as session]
             [xt.ui.state.feedback :as feedback]
             [xt.ui.state.dev :as dev]]})

^{:refer xt.ui.state.form/set-field! :added "4.1"}
(fact "form state owns drafts, validation and reset without UI nodes"
  (!.js
   (var current
        (form/create
         {"name" ""}
         {"name" [(fn [value _draft]
                     (return (:? (== "" value) "Required" nil)))]}))
   (form/validate! current)
   (var invalid [(xt/x:get-key current "valid")
                 (xt/x:get-path current ["errors" "name"])])
   (form/set-field! current ["name"] "Ada")
   (var valid [(xt/x:get-key current "valid")
               (xt/x:get-key current "dirty")
               (xt/x:get-path current ["draft" "name"])])
   (form/reset! current)
   [invalid valid (xt/x:get-key current "dirty")])
  => [[false "Required"] [true true "Ada"] false])

^{:refer xt.ui.state.collection/select! :added "4.1"}
(fact "collection state tracks query, paging and selection"
  (!.js
   (var current (collection/create {"page" 3 "page_size" 10}))
   (collection/set-query! current ["search"] "ada")
   (collection/select! current "u-1" true)
   [(xt/x:get-key current "page")
    (xt/x:get-path current ["query" "search"])
    (collection/selected-ids current)])
  => [0 "ada" ["u-1"]])

^{:refer xt.ui.state.crud/spec :added "4.1"}
(fact "CRUD specifications explicitly declare the page-controller strategy"
  (!.js
   (var spec (crud/spec "users" {} [] [] [] {}))
   (var state (crud/create-state spec {}))
   [(xt/x:get-key spec "strategy")
    (xt/x:get-key state "mode")
    (xt/x:get-path state ["collection" "page_size"])])
  => ["page_controller" "list" 25])

^{:refer xt.ui.state.session/project :added "4.1"}
(fact "session projection exposes access data without selecting a route"
  (!.js
   (var current
        (session/project {"authenticated" true "user_id" "u-1"}
                         {"handle" "ada"}
                         {"camera" false "admin" true}))
   [(xt/x:get-key current "authenticated")
    (xt/x:get-key current "user_id")
    (session/capable? current "admin")
    (xt/x:has-key? current "route")])
  => [true "u-1" true false])

^{:refer xt.ui.state.feedback/fail! :added "4.1"}
(fact "feedback and diagnostics remain serializable and capability gated"
  (!.js
   (var status (feedback/create))
   (feedback/fail! status "offline" true)
   (var diagnostics (dev/create true {"inspect_store" true}))
   [(xt/x:get-key status "error")
    (xt/x:get-key status "retryable")
    (dev/available? diagnostics "inspect_store")
    (xt/x:has-key? diagnostics "password")])
  => ["offline" true true false])
