(ns gdscript.core
  (:require [hara.lang :as l]))

(l/script :gdscript
  {:require [[xt.lang.common-lib :as lib]]})

(defn.gd hello
  "returns a greeting string"
  {:added "4.1"}
  []
  (return "hello from gdscript"))

(defn.gd add
  "adds two numbers"
  {:added "4.1"}
  [a b]
  (return (+ a b)))

(defn.gd sum-array
  "sums an array of numbers"
  {:added "4.1"}
  [arr]
  (var total 0)
  (for [x :in arr]
    (:= total (+ total x)))
  (return total))

(defn.gd factorial
  "computes n!"
  {:added "4.1"}
  [n]
  (if (<= n 1)
    (return 1)
    (return (* n (factorial (- n 1))))))
