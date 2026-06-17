(ns lean.core-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-lean]
            [lean.core :as y])
  (:use code.test))

(fact "emits basic lean syntax"
  (l/emit-as :lean '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :lean '[(List.map succ [1 2 3])])
  => "List.map succ [1,2,3]")

(fact "lean.core exposes builtin outlines"
  (count y/+all+) => y/+count+
  (boolean (some #(= "List.map" (:name %)) y/+all+)) => true
  (boolean (some #(= "String.length" (:name %)) y/+all+)) => true
  (boolean (some #(= "Nat.add" (:name %)) y/+all+)) => true)
