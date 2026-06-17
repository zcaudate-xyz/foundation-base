(ns haskell.core
  (:require [haskell.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :haskell {})

(f/intern-all haskell.core.builtin)

(defn.hs add
  "adds two numbers"
  {:added "4.1"}
  [a b]
  (+ a b))

(defn.hs twice
  "doubles a number"
  {:added "4.1"}
  [x]
  (* x 2))

(defn.hs square
  "returns x squared"
  {:added "4.1"}
  [x]
  (* x x))

(defn.hs sum-of
  "sums a list of numbers"
  {:added "4.1"}
  [xs]
  (sum xs))

(defn.hs length-of
  "returns the length of a list"
  {:added "4.1"}
  [xs]
  (length xs))
