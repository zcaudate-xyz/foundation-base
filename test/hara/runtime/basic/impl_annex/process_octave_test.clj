(ns hara.runtime.basic.impl-annex.process-octave-test
  (:require [hara.runtime.basic.impl-annex.process-octave :refer :all]
            [std.lib.env :as env]
            [hara.lang :as l])
  (:use code.test))

(fact:global
 {:skip (not (or (env/program-exists? "octave-cli")
                 (env/program-exists? "octave")))})

(l/script- :octave
  {:runtime :oneshot})

^{:refer hara.runtime.basic.impl-annex.process-octave/CANARY :adopt true :added "4.0"}
(fact "EVALUATE octave code"

  (!.octave (+ 1 2 3 4))
  => 10

  (!.octave (* 2 3 4))
  => 24

  (!.octave (mod 17 5))
  => 2

  (!.octave (not= 1 2))
  => true

  (!.octave [1 2 3])
  => [1 2 3]

  (!.octave (sum [1 2 3 4]))
  => 10

  (!.octave (mean [1 2 3 4]))
  => 2.5)

^{:refer hara.runtime.basic.impl-annex.process-octave/default-oneshot-wrap  :adopt true :added "4.0"}
(fact "creates the oneshot form"

  (default-oneshot-wrap "1 + 2")
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-octave/default-basic-client  :adopt true :added "4.0"}
(fact "creates the basic client form"

  (default-basic-client 19000)
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-octave/default-oneshot-trim :added "4.0"}
(fact "trim for oneshot"

  (default-oneshot-trim "{\"type\":\"data\",\"return\":\"number\",\"value\":10}")
  => {"type" "data" "return" "number" "value" 10})
