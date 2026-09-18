(ns xt.ui.state.core-test
  (:use code.test)
  (:require [lang.core :as l]
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
  (!.js (page/controller-create nil nil nil nil))
  => {"state" {} "revision" 0 "handlers" {} "lifecycle" {} "deps" {} "listeners" {} "opened" false}

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
                      (xt/x:arr-push seen [(xt/x:get-path state ["data" "count"]) revision])))
   (-> ((xt/x:get-key (page/actions-create controller ["increment"]) "increment") 2)
       (promise/x:promise-then
        (fn [_] (repl/notify [(page/snapshot controller) seen])))))
  => [{"status" "ready" "data" {"count" 2}} [[2 1]]])

^{:refer xt.ui.state.core/snapshot :added "4.1"}
(fact "returns the current state without advancing its revision"
  (!.js
   (var controller (page/controller-create {"value" "initial"} nil nil nil))
   [(page/snapshot controller) (page/snapshot controller) (page/revision controller)])
  => [{"value" "initial"} {"value" "initial"} 0])

^{:refer xt.ui.state.core/revision :added "4.1"}
(fact "each explicit state replacement advances the revision once"
  (!.js
   (var controller (page/controller-create {} nil nil nil))
   (var revisions [(page/revision controller)])
   (page/set-state! controller {"count" 1})
   (xt/x:arr-push revisions (page/revision controller))
   (page/set-state! controller {"count" 1})
   (xt/x:arr-push revisions (page/revision controller))
   revisions)
  => [0 1 2])

^{:refer xt.ui.state.core/notify! :added "4.1"}
(fact "notifies each subscriber with the same state and revision without mutation"
  (!.js
   (var controller (page/controller-create {"count" 3} nil nil nil))
   (var seen {})
   (page/subscribe! controller "a" (fn [state rev] (xt/x:set-key seen "a" [state rev])))
   (page/subscribe! controller "b" (fn [state rev] (xt/x:set-key seen "b" [state rev])))
   (var result (page/notify! controller))
   [(== result controller) (page/revision controller) seen])
  => [true 0 {"a" [{"count" 3} 0] "b" [{"count" 3} 0]}])

^{:refer xt.ui.state.core/set-state! :added "4.1"}
(fact "replaces rather than merges state and normalizes nil snapshots"
  (!.js
   (var controller (page/controller-create {"old" true} nil nil nil))
   (var result (page/set-state! controller {"new" true}))
   (var snapshot (page/snapshot controller))
   (page/set-state! controller nil)
   [result snapshot (page/snapshot controller) (page/revision controller)])
  => [{"new" true} {"new" true} {} 2])

^{:refer xt.ui.state.core/update-state! :added "4.1"}
(fact "passes the latest state to the updater and publishes its result"
  (!.js
   (var controller (page/controller-create {"count" 2} nil nil nil))
   (var seen [])
   (page/subscribe! controller "test" (fn [state rev] (xt/x:arr-push seen [state rev])))
   (var result (page/update-state! controller
                                  (fn [state] (return {"count" (+ 4 (. state ["count"]))}))))
   [result (page/snapshot controller) seen])
  => [{"count" 6} {"count" 6} [[{"count" 6} 1]]])

^{:refer xt.ui.state.core/subscribe! :added "4.1"}
(fact "reusing a listener id replaces the callback rather than adding a duplicate"
  (!.js
   (var controller (page/controller-create {} nil nil nil))
   (var seen [])
   (page/subscribe! controller "screen" (fn [_state _rev] (xt/x:arr-push seen "old")))
   (var result (page/subscribe! controller "screen" (fn [_state _rev] (xt/x:arr-push seen "new"))))
   (page/set-state! controller {"count" 1})
   [result seen (xt/x:obj-keys (. controller ["listeners"]))])
  => ["screen" ["new"] ["screen"]])

^{:refer xt.ui.state.core/unsubscribe! :added "4.1"}
(fact "unsubscribing is idempotent and prevents subsequent notifications"
  (!.js
   (var controller (page/controller-create {} nil nil nil))
   (var seen [])
   (page/subscribe! controller "screen" (fn [_state rev] (xt/x:arr-push seen rev)))
   (page/set-state! controller {})
   (var removed (page/unsubscribe! controller "screen"))
   (var repeated (page/unsubscribe! controller "screen"))
   (page/set-state! controller {})
   [removed repeated seen (. controller ["listeners"])])
  => [true true [1] {}])

^{:refer xt.ui.state.core/dispatch! :added "4.1"}
(fact "dispatch passes controller, payload and dependencies and reports unavailable actions"
  (notify/wait-on :js
    (var controller
         (page/controller-create {"count" 2}
          {"read" (fn [self payload deps]
                    (return [(page/snapshot self) payload deps]))
           "invalid" "not a function"}
          nil {"scope" "test"}))
    (promise/x:promise-then
     (promise/x:promise-all [(page/dispatch! controller "read" {"id" 7})
                            (page/dispatch! controller "missing" nil)
                            (page/dispatch! controller "invalid" nil)])
     (fn [results] (repl/notify results))))
  => [[{"count" 2} {"id" 7} {"scope" "test"}]
      {"status" "unavailable" "action" "missing"}
      {"status" "unavailable" "action" "invalid"}])

^{:refer xt.ui.state.core/dispatch! :id dispatch-rejection :added "4.1"}
(fact "handler failures remain rejected rather than becoming successful results"
  (notify/wait-on :js
    (var controller (page/controller-create {} {"fail" (fn [_self _payload _deps] (xt/x:err "denied"))} nil nil))
    (-> (page/dispatch! controller "fail" nil)
        (promise/x:promise-then (fn [value] (repl/notify ["resolved" value])))
        (promise/x:promise-catch (fn [err] (repl/notify ["rejected" (or (xt/x:ex-message err) (xt/x:to-string err))])))))
  => ["rejected" "denied"])

^{:refer xt.ui.state.core/actions-create :added "4.1"}
(fact "each action closure retains its own action id and forwards its payload"
  (notify/wait-on :js
    (var controller
         (page/controller-create {}
          {"left" (fn [_self payload _deps] (return ["left" payload]))
           "right" (fn [_self payload _deps] (return ["right" payload]))} nil nil))
    (var actions (page/actions-create controller ["left" "right"]))
    (promise/x:promise-then
     (promise/x:promise-all [((xt/x:get-key actions "left") 1)
                            ((xt/x:get-key actions "right") 2)])
     (fn [results] (repl/notify [results (page/actions-create controller nil)]))))
  => [[["left" 1] ["right" 2]] {}])

^{:refer xt.ui.state.core/open! :added "4.1"}
(fact "owns lifecycle separately from view rendering and opens and closes idempotently"
  (notify/wait-on :js
    (var events [])
    (var controller
         (page/controller-create {} {}
          {"open" (fn [_controller deps] (xt/x:arr-push events ["open" (. deps ["id"])]))
           "close" (fn [_controller deps] (xt/x:arr-push events ["close" (. deps ["id"])]))}
          {"id" "test"}))
    (-> (page/open! controller)
        (promise/x:promise-then (fn [opened]
                                 (xt/x:arr-push events ["same" (== opened controller)])
                                 (return (page/open! controller))))
        (promise/x:promise-then (fn [_] (return (page/close! controller))))
        (promise/x:promise-then (fn [_] (return (page/close! controller))))
        (promise/x:promise-then (fn [closed] (repl/notify [events closed (. controller ["opened"])])))))
  => [[["open" "test"] ["same" true] ["close" "test"]] true false])

^{:refer xt.ui.state.core/close! :added "4.1"}
(fact "closing without lifecycle handlers clears subscribers and permits reopening"
  (notify/wait-on :js
    (var controller (page/controller-create {} nil nil nil))
    (var seen [])
    (page/subscribe! controller "screen" (fn [_state rev] (xt/x:arr-push seen rev)))
    (-> (page/open! controller)
        (promise/x:promise-then (fn [_] (return (page/close! controller))))
        (promise/x:promise-then (fn [closed]
                                 (page/set-state! controller {"count" 1})
                                 (xt/x:arr-push seen closed)
                                 (return (page/open! controller))))
        (promise/x:promise-then (fn [opened]
                                 (repl/notify [seen (. opened ["opened"]) (. opened ["listeners"])])))))
  => [[true] true {}])
