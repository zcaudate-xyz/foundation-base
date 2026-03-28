(ns rt.basic.impl-annex.process-perl-test
  (:require [rt.basic.impl-annex.process-perl :refer :all]
            [std.concurrent :as cc]
            [std.lang :as l])
  (:use code.test))

(l/script- :perl
  {:runtime :oneshot})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer rt.basic.impl-annex.process-perl/CANARY :adopt true :added "4.0"}
(fact "EVALUATE perl code"
  ^:unchecked

  (!.pl (+ 1 2 3 4))
  => 10)
