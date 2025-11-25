(ns std.config-test
  (:use code.test)
  (:require [std.config :refer :all])
  (:refer-clojure :exclude [load resolve]))

^{:refer std.config/get-session :added "3.0"}
(fact "retrieves the global session"
  (clear-session)
  (get-session) => {})

^{:refer std.config/swap-session :added "3.0"}
(fact "updates the global session"
  (clear-session)
  (swap-session assoc :a 1)
  (get-session) => {:a 1})

^{:refer std.config/clear-session :added "3.0"}
(fact "clears the global session"
  (swap-session assoc :a 1)
  (clear-session)
  (get-session) => {})
