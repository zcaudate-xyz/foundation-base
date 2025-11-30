(ns std.dom.signal-test
  (:use code.test)
  (:require [std.dom.signal :refer :all]
            [std.dom.sync :as sync]))

^{:refer std.dom.signal/process-signal :added "4.0"}
(fact "processes a signal (delta or event)"
  (with-redefs [sync/apply-patch (fn [c p] [:delta c p])
                sync/receive-event (fn [s p] [:event s p])]
    (process-signal :client {:type :delta :payload [[:set :a 1]]}) => [:delta :client [[:set :a 1]]]
    (process-signal :server {:type :event :payload {:id :click}}) => [:event :server {:id :click}]))

^{:refer std.dom.signal/create-delta-signal :added "4.0"}
(fact "creates a signal carrying dom deltas"
  (create-delta-signal [[:set :a 1]])
  => {:type :delta :payload [[:set :a 1]]})

^{:refer std.dom.signal/create-event-signal :added "4.0"}
(fact "creates a signal carrying an event"
  (create-event-signal {:id :click})
  => {:type :event :payload {:id :click}})
