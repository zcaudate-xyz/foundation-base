matlab/core.clj:1:(ns matlab.core
matlab/core.clj:2:  (:require [matlab.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

matlab/core.clj:6:(l/script :matlab
  {:require [[xt.lang.common-lib :as lib]]})

matlab/core.clj:9:(f/intern-all matlab.core.builtin)

matlab/core.clj:11:(defn.matlab add
  "adds two numbers or arrays"
  {:added "4.0"}
  [a b]
  (+ a b))

matlab/core.clj:17:(defn.matlab subtract
  "subtracts b from a"
  {:added "4.0"}
  [a b]
  (- a b))

matlab/core.clj:23:(defn.matlab scale
  "scales a value by a factor"
  {:added "4.0"}
  [x s]
  (* x s))

matlab/core.clj:29:(defn.matlab square
  "returns x squared"
  {:added "4.0"}
  [x]
  (* x x))

matlab/core.clj:35:(defn.matlab sum-of
  "sums the elements of a vector"
  {:added "4.0"}
  [v]
  (sum v))

matlab/core.clj:41:(defn.matlab mean-of
  "computes the mean of a vector"
  {:added "4.0"}
  [v]
  (mean v))

matlab/core.clj:47:(defn.matlab linspace-5
  "returns 5 linearly spaced points between 0 and 1"
  {:added "4.0"}
  []
  (linspace 0 1 5))

matlab/core.clj:53:(defn.matlab zeros-3x3
  "returns a 3x3 zero matrix"
  {:added "4.0"}
  []
  (zeros 3 3))
