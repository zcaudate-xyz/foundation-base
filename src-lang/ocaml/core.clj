(ns ocaml.core
  (:require [ocaml.core.builtin :as builtin]
            [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :ocaml {})

(f/intern-all ocaml.core.builtin)

(defn.ml add
  "adds two integers"
  {:added "4.1"}
  [a b]
  (+ a b))

(defn.ml twice
  "doubles an integer"
  {:added "4.1"}
  [x]
  (* x 2))

(defn.ml square
  "returns x squared"
  {:added "4.1"}
  [x]
  (* x x))

(defn.ml length-of
  "returns the length of a list"
  {:added "4.1"}
  [xs]
  (List.length xs))
