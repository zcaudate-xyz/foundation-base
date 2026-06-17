(ns octave.core-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-octave]
            [octave.core :as y])
  (:use code.test))

(fact "emits basic octave syntax"
  (l/emit-as :octave '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :octave '[(mod 10 3)])
  => "mod(10,3)"

  (l/emit-as :octave '[{:a 1 :b 2}])
  => "struct(\"a\", 1, \"b\", 2)"

  (l/emit-as :octave '[[1 2 3]])
  => "[1, 2, 3]")

(fact "octave.core exposes builtin outlines with signatures"
  y/+count+ => 1654
  (count y/+all+) => 1654
  (boolean (some #(= "abs" (:name %)) y/+all+)) => true
  (boolean (some #(= "plot" (:name %)) y/+plot+)) => true
  (boolean (some #(= "mean" (:name %)) y/+statistics+)) => true
  (boolean (some #(= "convhull" (:name %)) y/+geometry+)) => true
  (boolean (seq (:signatures (first (filter #(= "abs" (:name %)) y/+core+))))) => true
  (boolean (seq (:signatures (first (filter #(= "sum" (:name %)) y/+core+))))) => true)

(fact "emits octave function definitions"
  (l/emit-as :octave '[(defn add [a b] (+ a b))])
  => "function add = add(a, b)\nadd = a + b;\nend"

  (l/emit-as :octave '[(defn scale [x s] (* x s))])
  => "function scale = scale(x, s)\nscale = x * s;\nend")
