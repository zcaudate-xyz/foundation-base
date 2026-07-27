(ns matlab.core
  (:require [matlab.core.builtin :as builtin]
            [tahto.core :as l]
            [std.lib.foundation :as f]))

(l/script :matlab
  {:require [[xt.lang.common-lib :as lib]]})

(f/intern-all matlab.core.builtin)

(defn.matlab add
  "adds two numbers or arrays"
  {:added "4.0"}
  [a b]
  (+ a b))

(defn.matlab subtract
  "subtracts b from a"
  {:added "4.0"}
  [a b]
  (- a b))

(defn.matlab scale
  "scales a value by a factor"
  {:added "4.0"}
  [x s]
  (* x s))

(defn.matlab square
  "returns x squared"
  {:added "4.0"}
  [x]
  (* x x))

(defn.matlab sum-of
  "sums the elements of a vector"
  {:added "4.0"}
  [v]
  (sum v))

(defn.matlab mean-of
  "computes the mean of a vector"
  {:added "4.0"}
  [v]
  (mean v))

(defn.matlab linspace-5
  "returns 5 linearly spaced points between 0 and 1"
  {:added "4.0"}
  []
  (linspace 0 1 5))

(defn.matlab zeros-3x3
  "returns a 3x3 zero matrix"
  {:added "4.0"}
  []
  (zeros 3 3))
