(ns octave.core-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-octave])
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

(fact "emits octave function definitions"
  (l/emit-as :octave '[(defn add [a b] (+ a b))])
  => "function add(a, b)\nadd = a + b;\nend"

  (l/emit-as :octave '[(defn scale [x s] (* x s))])
  => "function scale(x, s)\nscale = x * s;\nend")
