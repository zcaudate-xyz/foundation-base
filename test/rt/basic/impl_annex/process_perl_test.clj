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


^{:refer rt.basic.impl-annex.process-perl/default-body-transform :added "4.1"}
(fact "transforms oneshot forms for return-eval"
  (default-body-transform '[1 2 3] {})
  => '[1 2 3]

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 3))