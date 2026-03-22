(ns xt.lang.base-interval-test
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-interval :as interval]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-interval/start-interval :added "4.0"}
(fact "starts an interval"
  ^:hidden
  
  (notify/wait-on :js
    (interval/start-interval
     (fn []
       (repl/notify "hello"))
     500))
  => "hello")

^{:refer xt.lang.base-interval/stop-interval :added "4.0"
  :setup [(l/rt:restart)]}
(fact "stops the interval from happening"
  ^:hidden
  
  (!.js
   (var it (interval/start-interval
            (fn []
              )
            500))
   (interval/stop-interval))
  => nil)
