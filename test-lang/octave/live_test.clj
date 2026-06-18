matlab/live_test.clj:1:(ns matlab.live-test
  (:use code.test)
  (:require [hara.lang :as l]
            [std.lib.env :as env]))

(fact:global
matlab/live_test.clj:7: {:skip (not (or (env/program-exists? "matlab-cli")
matlab/live_test.clj:8:                 (env/program-exists? "matlab")))})

matlab/live_test.clj:10:(l/script- :matlab
  {:runtime :oneshot
matlab/live_test.clj:12:   :require [[matlab.core :as y]] :test-mode true})

matlab/live_test.clj:14:(fact "live matlab core examples"
matlab/live_test.clj:15:  (!.matlab (y/add 1 2))
  => 3

matlab/live_test.clj:18:  (!.matlab (y/scale 5 3))
  => 15

matlab/live_test.clj:21:  (!.matlab (y/square 4))
  => 16

matlab/live_test.clj:24:  (!.matlab (y/sum-of [1 2 3 4]))
  => 10

matlab/live_test.clj:27:  (!.matlab (y/mean-of [1 2 3 4]))
  => 2.5

matlab/live_test.clj:30:  (!.matlab (y/linspace-5))
  => [0 0.25 0.5 0.75 1.0]

matlab/live_test.clj:33:  (!.matlab (y/zeros-3x3))
  => [[0 0 0] [0 0 0] [0 0 0]])

(fact "live builtin calls work"
matlab/live_test.clj:37:  (!.matlab (abs -3))
  => 3

matlab/live_test.clj:40:  (!.matlab (sqrt 16))
  => 4

matlab/live_test.clj:43:  (!.matlab (mean [1 2 3 4]))
  => 2.5

matlab/live_test.clj:46:  (!.matlab (sum [1 2 3 4]))
  => 10)
