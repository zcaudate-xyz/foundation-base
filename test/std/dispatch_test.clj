(ns std.dispatch-test
  (:use code.test)
  (:require [std.dispatch :refer :all]
            [std.lib.component :as component]))

^{:refer std.dispatch/dispatch? :added "3.0"}
(fact "checks if object is an dispatch"
  (dispatch? (create {:type :core
                      :options {:pool {:size 1}}
                      :handler (fn [_ _])})) => true)

^{:refer std.dispatch/submit :added "3.0"}
(fact "submits entry to an dispatch"
  (let [d (dispatch {:type :core
                     :options {:pool {:size 1}}
                     :handler (fn [_ _])})]
    (submit d :entry)
    (component/stop d))
  => anything)

^{:refer std.dispatch/create :added "3.0"}
(fact "creates a component compatible dispatch"
  (create {:type :core
           :options {:pool {:size 1}}
           :handler (fn [_ _])})
  => dispatch?)

^{:refer std.dispatch/dispatch :added "3.0"}
(fact "creates and starts an dispatch"
  (let [d (dispatch {:type :core
                     :options {:pool {:size 1}}
                     :handler (fn [_ _])})]
    d => dispatch?
    (component/stop d)))

(comment
  (require '[std.dispatch.debounce-test :as debounce.test])
  (meta #'create)

  (create {})

  (create debounce.test/+test-config+))
