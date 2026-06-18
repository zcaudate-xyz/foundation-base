(ns hara.runtime.basic.impl-annex.process-matlab-test
  (:require [hara.runtime.basic.impl-annex.process-matlab :refer :all]
            [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "octave-cli")
                 (env/program-exists? "octave")))
  :setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(l/script- :matlab
  {:runtime :oneshot :test-mode true})

^{:refer hara.runtime.basic.impl-annex.process-matlab/CANARY :adopt true :added "4.0"}
(fact "EVALUATE matlab code"

  (!.mat (+ 1 2 3 4))
  => 10

  (!.mat (* 2 3 4))
  => 24

  (!.mat (mod 17 5))
  => 2

  (!.mat (not= 1 2))
  => true

  (!.mat [1 2 3])
  => [1 2 3]

  (!.mat (sum [1 2 3 4]))
  => 10

  (!.mat (mean [1 2 3 4]))
  => 2.5)

^{:refer hara.runtime.basic.impl-annex.process-matlab/default-oneshot-wrap  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-oneshot-wrap "1 + 2")
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-matlab/default-basic-client  :adopt true :added "4.0"}
(fact "creates the basic client form"

  (default-basic-client 19000)
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-matlab/default-oneshot-trim :added "4.0"}
(fact "trim for oneshot"

  (default-oneshot-trim "{\"type\":\"data\",\"return\":\"number\",\"value\":10}")
  => "{\"type\":\"data\",\"return\":\"number\",\"value\":10}")
