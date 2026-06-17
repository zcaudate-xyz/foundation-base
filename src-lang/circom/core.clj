(ns circom.core
  (:require [hara.lang :as l]
            [hara.model.annex.spec-circom]))

(l/script :circom)

(defn.circom Multiplier
  "a simple multiplier circuit"
  {:added "4.1"}
  []
  (signal input a)
  (signal input b)
  (signal output c)
  (<== c (* a b)))

(defn.circom AND
  "bitwise AND gate"
  {:added "4.1"}
  []
  (signal input a)
  (signal input b)
  (signal output out)
  (<== out (* a b)))

(defn.circom IsZero
  "outputs 1 if in is 0, otherwise 0"
  {:added "4.1"}
  []
  (signal input in)
  (signal output out)
  (signal inv)
  (=== (* in out) 0)
  (=== (+ (* in inv) out) 1))

(defn.circom SumOfSquares
  "computes a^2 + b^2"
  {:added "4.1"}
  []
  (signal input a)
  (signal input b)
  (signal output c)
  (var aa (* a a))
  (var bb (* b b))
  (<== c (+ aa bb)))

(defn.circom Fibonacci
  "computes the nth fibonacci number iteratively"
  {:added "4.1"}
  [n]
  (signal input in)
  (signal output out)
  (var a 0)
  (var b 1)
  (var i 0)
  (for [i 0 n]
    (var t a)
    (:= a b)
    (:= b (+ t b)))
  (<== out a))
