(ns
 xtbench.python.lang.common-interval-test
 (:require [std.lang :as l] [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require
  [[xt.lang.common-interval :as interval]
   [xt.lang.common-repl :as repl]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-interval/start-interval, :added "4.0"}
(fact
 "starts an interval"
 ^{:hidden true}
 (notify/wait-on
  :python
  (interval/start-interval (fn [] (repl/notify "hello")) 500))
 =>
 "hello")

^{:refer xt.lang.common-interval/stop-interval,
  :added "4.0",
  :setup [(l/rt:restart)]}
(fact
 "stops the interval from happening"
 ^{:hidden true}
 (!.py
  (var it (interval/start-interval (fn []) 500))
  (interval/stop-interval))
 =>
 nil)
