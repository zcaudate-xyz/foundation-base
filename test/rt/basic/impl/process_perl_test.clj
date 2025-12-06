(ns rt.basic.impl.process-perl-test
  (:use code.test)
  (:require [rt.basic.impl.process-perl :refer :all]
            [std.lang :as l]
            [std.concurrent :as cc]))

(l/script- :perl
  {:runtime :oneshot})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer rt.basic.impl.process-perl/CANARY :adopt true :added "4.0"}
(fact "EVALUATE perl code"
  ^:unchecked

  (!.pl (+ 1 2 3 4))
  => 10)
