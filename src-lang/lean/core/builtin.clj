(ns lean.core.builtin
  "Curated outline of common Lean 4 Init/Std builtins."
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :lean
  lean.core
  {:macro-only true})

(def +nat+
  "Natural number functions from Init.Nat."
  [
    {:name "Nat.add" :signature "Nat.add : Nat -> Nat -> Nat"}
    {:name "Nat.sub" :signature "Nat.sub : Nat -> Nat -> Nat"}
    {:name "Nat.mul" :signature "Nat.mul : Nat -> Nat -> Nat"}
    {:name "Nat.div" :signature "Nat.div : Nat -> Nat -> Nat"}
    {:name "Nat.mod" :signature "Nat.mod : Nat -> Nat -> Nat"}
    {:name "Nat.pow" :signature "Nat.pow : Nat -> Nat -> Nat"}
    {:name "Nat.gcd" :signature "Nat.gcd : Nat -> Nat -> Nat"}
    {:name "Nat.sqrt" :signature "Nat.sqrt : Nat -> Nat"}
    {:name "Nat.log" :signature "Nat.log : Nat -> Nat -> Nat"}])

(def +list+
  "List functions from Init.List."
  [
    {:name "List.length" :signature "List.length : List a -> Nat"}
    {:name "List.head" :signature "List.head : List a -> a"}
    {:name "List.tail" :signature "List.tail : List a -> List a"}
    {:name "List.map" :signature "List.map : (a -> b) -> List a -> List b"}
    {:name "List.filter" :signature "List.filter : (a -> Bool) -> List a -> List a"}
    {:name "List.foldl" :signature "List.foldl : (b -> a -> b) -> b -> List a -> b"}
    {:name "List.foldr" :signature "List.foldr : (a -> b -> b) -> b -> List a -> b"}
    {:name "List.reverse" :signature "List.reverse : List a -> List a"}
    {:name "List.append" :signature "List.append : List a -> List a -> List a"}
    {:name "List.concat" :signature "List.concat : List (List a) -> List a"}
    {:name "List.zip" :signature "List.zip : List a -> List b -> List (Prod a b)"}
    {:name "List.zipWith" :signature "List.zipWith : (a -> b -> c) -> List a -> List b -> List c"}
    {:name "List.take" :signature "List.take : Nat -> List a -> List a"}
    {:name "List.drop" :signature "List.drop : Nat -> List a -> List a"}
    {:name "List.range" :signature "List.range : Nat -> List Nat"}])

(def +string+
  "String functions from Init.String."
  [
    {:name "String.length" :signature "String.length : String -> Nat"}
    {:name "String.append" :signature "String.append : String -> String -> String"}
    {:name "String.toList" :signature "String.toList : String -> List Char"}
    {:name "String.join" :signature "String.join : String -> List String -> String"}
    {:name "String.contains" :signature "String.contains : String -> String -> Bool"}
    {:name "String.take" :signature "String.take : Nat -> String -> String"}
    {:name "String.drop" :signature "String.drop : Nat -> String -> String"}
    {:name "String.trim" :signature "String.trim : String -> String"}])

(def +array+
  "Array functions from Init.Data.Array."
  [
    {:name "Array.size" :signature "Array.size : Array a -> Nat"}
    {:name "Array.push" :signature "Array.push : Array a -> a -> Array a"}
    {:name "Array.get" :signature "Array.get : Array a -> Nat -> a"}
    {:name "Array.set" :signature "Array.set : Array a -> Nat -> a -> Array a"}
    {:name "Array.map" :signature "Array.map : (a -> b) -> Array a -> Array b"}
    {:name "Array.foldl" :signature "Array.foldl : (b -> a -> b) -> b -> Array a -> b"}])

(def +bool+
  "Boolean functions from Init.Core."
  [
    {:name "not" :signature "not : Bool -> Bool"}
    {:name "and" :signature "and : Bool -> Bool -> Bool"}
    {:name "or" :signature "or : Bool -> Bool -> Bool"}
    {:name "xor" :signature "xor : Bool -> Bool -> Bool"}])

(def +all+
  "All curated Lean builtins."
  (vec (concat +nat+ +list+ +string+ +array+ +bool+)))

(def +count+
  "Number of curated Lean builtins."
  (count +all+))
