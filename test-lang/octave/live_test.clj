(ns octave.live-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-octave]
            [octave.core :as y]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (or (env/program-exists? "octave-cli")
                 (env/program-exists? "octave")))})

(l/script- :octave
  {:runtime :oneshot
   :require [[octave.core :as y]]})

(fact "live octave core examples"
  (!.octave (y/add 1 2))
  => 3

  (!.octave (y/scale 5 3))
  => 15

  (!.octave (y/square 4))
  => 16

  (!.octave (y/sum-of [1 2 3 4]))
  => 10

  (!.octave (y/mean-of [1 2 3 4]))
  => 2.5

  (!.octave (y/linspace-5))
  => [0 0.25 0.5 0.75 1.0]

  (!.octave (y/zeros-3x3))
  => [[0 0 0] [0 0 0] [0 0 0]])

(fact "live builtin calls work"
  (!.octave (abs -3))
  => 3

  (!.octave (sqrt 16))
  => 4

  (!.octave (mean [1 2 3 4]))
  => 2.5

  (!.octave (sum [1 2 3 4]))
  => 10)
