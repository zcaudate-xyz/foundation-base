(ns xt.lang.common-interval-test
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-interval :as interval]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-interval/start-interval :added "4.0"}
(fact "starts an interval"

  (notify/wait-on :js
    (interval/start-interval
     (fn []
       (repl/notify "hello"))
     500))
  => "hello")

^{:refer xt.lang.common-interval/stop-interval :added "4.0"
  :setup [(l/rt:restart)]}
(fact "stops the interval from happening"

  (!.js
   (var it (interval/start-interval
            (fn []
              )
            500))
   (interval/stop-interval))
  => nil)
