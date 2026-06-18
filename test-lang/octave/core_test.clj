matlab/core_test.clj:1:(ns matlab.core-test
  (:require [hara.lang :as l]
matlab/core_test.clj:3:            [hara.model.annex.spec-matlab]
matlab/core_test.clj:4:            [matlab.core :as y])
  (:use code.test))

matlab/core_test.clj:7:(fact "emits basic matlab syntax"
matlab/core_test.clj:8:  (l/emit-as :matlab '[(+ 1 2 3)])
  => "1 + 2 + 3"

matlab/core_test.clj:11:  (l/emit-as :matlab '[(mod 10 3)])
  => "mod(10,3)"

matlab/core_test.clj:14:  (l/emit-as :matlab '[{:a 1 :b 2}])
  => "struct(\"a\", 1, \"b\", 2)"

matlab/core_test.clj:17:  (l/emit-as :matlab '[[1 2 3]])
  => "[1, 2, 3]")

matlab/core_test.clj:20:(fact "matlab.core exposes builtin outlines with signatures"
  y/+count+ => 1654
  (count y/+all+) => 1654
  (boolean (some #(= "abs" (:name %)) y/+all+)) => true
  (boolean (some #(= "plot" (:name %)) y/+plot+)) => true
  (boolean (some #(= "mean" (:name %)) y/+statistics+)) => true
  (boolean (some #(= "convhull" (:name %)) y/+geometry+)) => true
  (boolean (seq (:signatures (first (filter #(= "abs" (:name %)) y/+core+))))) => true
  (boolean (seq (:signatures (first (filter #(= "sum" (:name %)) y/+core+))))) => true)

matlab/core_test.clj:30:(fact "emits matlab function definitions"
matlab/core_test.clj:31:  (l/emit-as :matlab '[(defn add [a b] (+ a b))])
  => "function add = add(a, b)\nadd = a + b;\nend"

matlab/core_test.clj:34:  (l/emit-as :matlab '[(defn scale [x s] (* x s))])
  => "function scale = scale(x, s)\nscale = x * s;\nend")
