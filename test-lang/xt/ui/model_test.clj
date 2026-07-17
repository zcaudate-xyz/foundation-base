(ns xt.ui.model-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.ui.model :as ui-model]]})

(defn.js create-store
  []
  (var node (substrate/node-create {"id" "ui-model-test"}))
  (page-core/group-add-attach
   node "app/account" "account/settings"
   {"draft" {"handler" (fn [_context value] (return value))
              "pipeline" {"check_args" (fn [context]
                                           (return [(xt/x:get-key context "input")]))
                          "check_disabled" (fn [_context] (return false))}
              "options" {}
              "defaults" {"args" [] "output" {}}}
    "save" {"handler" (fn [_context value]
                          (return {"saved" value}))
            "pipeline" {}
            "options" {}
            "defaults" {"args" [] "output" {}}}})
  (return (ui-model/store-create node "app/account" "account/settings" "local" {})))

^{:refer xt.ui.model/patch-input! :added "4.1"}
(fact "patches drafts and reads standardized model slots"
  (notify/wait-on :js
    (var store (-/create-store))
    (promise/x:promise-then
     (ui-model/patch-input! store "draft" ["first_name"] "Ada" {})
     (fn [_]
       (repl/notify [(ui-model/model-input store "draft" ["first_name"] nil)
                     (ui-model/model-pending? store "draft")
                     (ui-model/model-error store "draft")]))))
  => ["Ada" false nil])

^{:refer xt.ui.model/subscribe! :added "4.1"}
(fact "subscribes to page model changes and removes listeners on close"
  (notify/wait-on :js
    (var store (-/create-store))
    (var captured [])
    (ui-model/subscribe!
     store "screen"
     (fn [_id data _t meta]
       (xt/x:arr-push captured [(xt/x:get-key meta "model_id")
                                (xt/x:get-key data "type")])
       (return nil)))
    (promise/x:promise-then
     (ui-model/patch-input! store "draft" ["language"] "EN" {})
     (fn [_]
       (return
        (promise/x:promise-then
         (ui-model/store-close store)
         (fn [_]
           (repl/notify {"captured" captured
                         "listeners" (xt/x:obj-keys (xt/x:get-key store "listeners"))})))))))
  => (contains-in {"captured" vector?
                   "listeners" []}))


^{:refer xt.ui.model/store-create :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/store-version :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/store-open :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-slot :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-input :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-output :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-pending? :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-disabled? :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-error :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-remote :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/model-sync :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/set-input! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/invoke! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/refresh! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/unsubscribe! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.model/store-close :added "4.1"}
(fact "TODO")