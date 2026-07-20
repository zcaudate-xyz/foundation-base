(ns xt.substrate.view-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:langs [:dart]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]]})

^{:refer xt.substrate.view/validate :added "4.1"}
(fact "validates JSON-safe view IR and rejects closures in props"
  (!.js
   (var valid (view/view-spec "sample"
                              {"name" {"source" "state"
                                       "space_id" "app"
                                       "path" ["name"]}}
                              (view/node "ui/text" {"value" "hello"} [])))
   (var rejected false)
   (try
     (view/validate
      (view/view-spec "invalid" {}
                      (view/node "ui/button" {"on_press" (fn [] nil)} [])))
     (catch err
       (:= rejected true)))
   [(view/validate valid) rejected])
  => [true true])

^{:refer xt.substrate.view/subscribe :added "4.1"}
(fact "deduplicates dependencies and emits monotonic substrate snapshots"
  (!.js
   (var node (substrate/node-create {}))
   (var spec
        (view/view-spec
         "sample"
         {"name" {"source" "state" "space_id" "app" "path" ["name"]}
          "copy" {"source" "state" "space_id" "app" "path" ["name"]}}
         nil))
   (var events [])
   (var subscription
        (view/subscribe node spec "listener"
                        (fn [snapshot revision _event]
                          (xt/x:arr-push events [revision
                                                 (xt/x:get-key snapshot "name")])
                          (return nil))))
   (view/state-set node "app" "sample" ["name"] "Ada")
   (view/state-set node "app" "sample" ["name"] "Grace")
   (view/unsubscribe subscription)
   (view/state-set node "app" "sample" ["name"] "Lin")
   [(xt/x:len (xt/x:get-key subscription "keys")) events
    (xt/x:get-key (view/snapshot node spec) "name")])
  => [0 [[1 "Ada"] [2 "Grace"]] "Lin"])

^{:refer xt.substrate.view/dispatch :added "4.1"}
(fact "dispatches serializable event projections through substrate handlers"
  (notify/wait-on :js
    (var node (substrate/node-create {}))
    (substrate/register-handler
     node "profile/set-name"
     (fn [_space args _frame _node]
       (return (xt/x:first args)))
     {})
    (promise/x:promise-then
     (view/dispatch node "app"
                    (view/action "profile/set-name"
                                 (view/event-value ["target" "value"]))
                    {"target" {"value" "Ada"}}
                    {})
     (fn [output]
       (repl/notify output))))
  => "Ada")
