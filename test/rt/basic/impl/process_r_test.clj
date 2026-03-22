(ns rt.basic.impl.process-r-test
  (:require [rt.basic.impl.process-r :refer :all]
            [std.lang :as l])
  (:use code.test))

(l/script- :r
  {:runtime :oneshot})

^{:refer rt.basic.impl.process-r/CANARY :adopt true :added "4.0"}
(fact "EVALUATE r code"
  
  (!.R (+ 1 2 3 4))
  => 10)

^{:refer rt.basic.impl.process-r/default-oneshot-wrap  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-oneshot-wrap 1)
  => string?)

^{:refer rt.basic.impl.process-r/default-basic-client  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-basic-client 19000)
  => string?)

^{:refer rt.basic.impl.process-r/default-oneshot-trim :added "4.0"}
(fact "trim for oneshot"

  (default-oneshot-trim "[1] \"1\"")
  => "1")
