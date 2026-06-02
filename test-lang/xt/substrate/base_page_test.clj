(ns xt.substrate.base-page-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.substrate :as substrate]
             [xt.substrate.base-page :as base-page]
             [xt.substrate.base-space :as node-space]]})

(defn.js create-node
  []
  (return
   {"id" "node-a"
    "spaces" {"space/a" {"state" {"count" 1
                                  "label" "A"}}
              "space/b" {"state" {"count" 10
                                  "label" "B"}}}}))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.substrate.base-page/async-fn :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/wrap-space-args :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/check-event :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/runtime-page :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-space-page :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/ensure-space-page :added "4.1"}
(fact "stores the runtime in a nested per-space slot"

  (!.js
    (var node (substrate/node-create (-/create-node)))
    (base-page/ensure-space-page node "space/a")
    (node-space/get-space-state node "space/a"))
  => {"count" 1
      "label" "A"
      "page" {"::" "substrate.page"
               "groups" {}
               "meta" {}
               "opts" {}}})

^{:refer xt.substrate.base-page/set-space-page :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/group-get :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/group-ensure :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/model-ensure :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-listeners :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/prep-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-model-dependents :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-group-dependents :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/run-tail-call :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/run-remote :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remote-call :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/run-refresh :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-dependents :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-remote :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-model-dependents-unthrottled :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/refresh-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/get-group-deps :added "4.1"}
(fact "compiles local and cross-model dependencies"
  (!.js
   (base-page/get-group-deps
    "hello"
    {"source" {}
     "detail" {"deps" ["source" ["other" "remote"]]}}))
  => {"hello" {"source" {"detail" true}}
      "other" {"remote" {"detail" true}}})

^{:refer xt.substrate.base-page/get-unknown-deps :added "4.1"}
(fact "reports missing dependent views in the current space runtime"
  (!.js
   (var node (substrate/node-create (-/create-node)))
   (base-page/add-group-attach
    node
    "space/a"
    "other"
    {"remote" {"handler" (fn [space]
                           (return (. space ["state"] ["label"])))
               "defaultArgs" []}})
   (base-page/get-unknown-deps
    node
    "space/a"
    "hello"
    {"detail" {"deps" ["missing" ["other" "unknown"]]}}
    (base-page/get-group-deps
     "hello"
     {"detail" {"deps" ["missing" ["other" "unknown"]]}})))
  => [["hello" "missing"]
      ["other" "unknown"]])

^{:refer xt.substrate.base-page/create-throttle :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/create-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/add-group-attach :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/add-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remove-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remove-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/group-update :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/model-update :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/model-set-input :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-group-raw :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-group :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-model :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/trigger-all :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/raw-callback-id :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/register-page-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/unregister-page-trigger :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/add-raw-callback :added "4.1"}
(fact "TODO")

^{:refer xt.substrate.base-page/remove-raw-callback :added "4.1"}
(fact "TODO")
