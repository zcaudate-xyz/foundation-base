(ns gdscript.tutorial.example-core
  (:require [hara.lang :as l]))

(l/script :gdscript)

(defn.gd add
  "Adds two numbers and returns the result."
  {:added "4.1"}
  [a b]
  (return (+ a b)))

(defn.gd factorial
  "Computes n! recursively."
  {:added "4.1"}
  [n]
  (if (<= n 1)
    (return 1)
    (return (* n (factorial (- n 1))))))

(defn.gd sum-array
  "Sums the elements of an array."
  {:added "4.1"}
  [arr]
  (var total 0)
  (for [x :in arr]
    (:= total (+ total x)))
  (return total))

(defn.gd build-vector
  "Builds a 3D vector from components."
  {:added "4.1"}
  [x y z]
  (return [x y z]))

(comment
  (l/rt:restart)

  
  (c/hello)
  
  ;; Eval these forms in a REPL with the :godot runtime active.
  (!.gd (gdscript.tutorial.example-core/add 2 3))
  (!.gd (gdscript.tutorial.example-core/factorial 5))
  (!.gd (gdscript.tutorial.example-core/sum-array [1 2 3 4 5]))
  (!.gd (gdscript.tutorial.example-core/build-vector 1 2 3)))
