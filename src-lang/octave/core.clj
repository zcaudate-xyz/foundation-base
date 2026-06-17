(ns octave.core
  (:require [octave.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :octave
  {:require [[xt.lang.common-lib :as lib]]})

(f/intern-all octave.core.builtin)

(defn.octave add
  "adds two numbers or arrays"
  {:added "4.0"}
  [a b]
  (+ a b))

(defn.octave subtract
  "subtracts b from a"
  {:added "4.0"}
  [a b]
  (- a b))

(defn.octave scale
  "scales a value by a factor"
  {:added "4.0"}
  [x s]
  (* x s))

(defn.octave square
  "returns x squared"
  {:added "4.0"}
  [x]
  (* x x))

(defn.octave sum-of
  "sums the elements of a vector"
  {:added "4.0"}
  [v]
  (sum v))

(defn.octave mean-of
  "computes the mean of a vector"
  {:added "4.0"}
  [v]
  (mean v))

(defn.octave linspace-5
  "returns 5 linearly spaced points between 0 and 1"
  {:added "4.0"}
  []
  (linspace 0 1 5))

(defn.octave zeros-3x3
  "returns a 3x3 zero matrix"
  {:added "4.0"}
  []
  (zeros 3 3))
