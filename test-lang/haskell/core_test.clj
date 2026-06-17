(ns haskell.core-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-haskell]
            [haskell.core :as h])
  (:use code.test))

(fact "emits basic haskell syntax"
  (l/emit-as :haskell '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :haskell '[(map inc [1 2 3])])
  => "map inc [1,2,3]")

(fact "haskell.core exposes prelude builtin outlines"
  h/+count+ => 223
  (count h/+prelude+) => 223
  (boolean (some #(= "map" (:name %)) h/+prelude+)) => true
  (boolean (some #(= "filter" (:name %)) h/+prelude+)) => true
  (boolean (some #(= "sum" (:name %)) h/+prelude+)) => true
  (boolean (some #(= "++" (:name %)) h/+prelude+)) => true
  (boolean (seq (:signature (first (filter #(= "map" (:name %)) h/+prelude+))))) => true)
