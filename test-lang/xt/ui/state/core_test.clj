(ns xt.ui.state.core-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-repl :as repl]
             [xt.ui.state.core :as page]]})

^{:refer xt.ui.state.core/controller-create :added "4.1"}
(fact "runs state transitions without a UI runtime"
  (notify/wait-on :js
   (var controller
        (page/controller-create
         {"status" "idle" "data" {"count" 0}}
         {"increment"
          (fn [self payload _deps]
            (var state (page/snapshot self))
            (return
             (page/set-state!
              self
              {"status" "ready"
               "data" {"count" (+ (xt/x:get-path state ["data" "count"])
                                      (or payload 1))}})))}
         {} {}))
   (var seen [])
   (page/subscribe! controller "test"
                    (fn [state revision]
                      (xt/x:arr-push seen
                                     [(xt/x:get-path state ["data" "count"])
                                      revision])))
   (-> ((xt/x:get-key (page/actions-create controller ["increment"])
                      "increment") 2)
       (promise/x:promise-then
        (fn [_]
          (repl/notify [(page/snapshot controller) seen])))))
  => [{"status" "ready" "data" {"count" 2}} [[2 1]]])

^{:refer xt.ui.state.core/open! :added "4.1"}
(fact "owns lifecycle separately from view rendering"
  (notify/wait-on :js
    (var events [])
    (var controller
         (page/controller-create
          {}
          {}
          {"open" (fn [_controller _deps]
                     (xt/x:arr-push events "open"))
           "close" (fn [_controller _deps]
                      (xt/x:arr-push events "close"))}
          {}))
    (-> (page/open! controller)
        (promise/x:promise-then (fn [_] (return (page/close! controller))))
        (promise/x:promise-then (fn [_] (repl/notify events)))))
  => ["open" "close"])


^{:refer xt.ui.state.core/snapshot :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/revision :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/notify! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/set-state! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/update-state! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/subscribe! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/unsubscribe! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/dispatch! :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/actions-create :added "4.1"}
(fact "TODO")

^{:refer xt.ui.state.core/close! :added "4.1"}
(fact "TODO")