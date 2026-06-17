(ns ocaml.core-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-ocaml]
            [ocaml.core :as y])
  (:use code.test))

(fact "emits basic ocaml syntax"
  (l/emit-as :ocaml '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :ocaml '[(List.map succ [1 2 3])])
  => "List.map succ [1,2,3]")

(fact "ocaml.core exposes builtin outlines"
  (count y/+all+) => y/+count+
  (boolean (some #(= "List.map" (:name %)) y/+all+)) => true
  (boolean (some #(= "String.length" (:name %)) y/+all+)) => true
  (boolean (some #(= "Array.length" (:name %)) y/+all+)) => true)
