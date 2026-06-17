(ns lean.core
  (:require [lean.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :lean {})

(f/intern-all lean.core.builtin)

(defn.lean add
  "adds two natural numbers"
  {:added "4.1"}
  [a b]
  (Nat.add a b))

(defn.lean twice
  "doubles a natural number"
  {:added "4.1"}
  [x]
  (Nat.mul x 2))

(defn.lean square
  "returns x squared"
  {:added "4.1"}
  [x]
  (Nat.mul x x))

(defn.lean length-of
  "returns the length of a list"
  {:added "4.1"}
  [xs]
  (List.length xs))
