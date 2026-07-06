(ns documentation.std-lib-zip
  (:require [std.lib.zip :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [find get]))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.zip` is part of the standard foundation library set. This page collects the public API reference for the namespace."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Creating zippers"}]]

"`vector-zip` and `seq-zip` construct zippers over vectors and sequences. `from-status` parses a form that already contains a `|` cursor marker."

(fact "create zippers from collections"
  (vector-zip [1 2 3 4 5])
  => (contains {:left ()
                :right '([1 2 3 4 5])})

  (seq-zip '(1 2 3 4 5))
  => (contains {:left ()
                :right '((1 2 3 4 5))}))

(fact "parse a form with a cursor"
  (from-status '[1 2 3 | 4])
  => (contains {:left '(3 2 1)
                :right '(4)}))

[[:section {:title "Moving around"}]]

"Step into, out of, left, and right within the tree. `status` renders the zipper back into a readable form with the cursor visible."

(fact "step inside and right"
  (-> (from-status '[1 2 | [3 4]])
      (step-inside)
      (status))
  => '([1 2 [| 3 4]]))

(fact "step left and right"
  (-> (from-status '[1 2 [3 4 |]])
      (step-left)
      (status))
  => '([1 2 [3 | 4]]))

(fact "walk to the outside-most point"
  (-> (from-status '[1 2 [| 3 4]])
      (step-outside-most)
      (status))
  => '(| [1 2 [3 4]]))

[[:section {:title "Editing"}]]

"Insert, delete, and replace elements around the cursor, then use `root-element` to read the modified tree."

(fact "insert elements left of the cursor"
  (-> (from-status '[1 2  [[| 3] 4]])
      (insert-left 1 2 3)
      (status))
  => '([1 2 [[1 2 3 | 3] 4]]))

(fact "delete the element right of the cursor"
  (-> (from-status '[1 2 | 3])
      (delete-right)
      (status))
  => '([1 2 |]))

(fact "replace the element right of the cursor"
  (-> (from-status '[1 2 | 3])
      (replace-right "10")
      (status))
  => '([1 2 | "10"]))

[[:section {:title "Searching"}]]

"`find-next`, `find-prev`, `find-right`, and `find-left` move the cursor to the first element that satisfies a predicate."

(fact "find a nested value"
  (-> (vector-zip [1 [2 [6 7] 3] [4 5]])
      (find-next #{7})
      (status))
  => '([1 [2 [6 | 7] 3] [4 5]]))

(fact "find right for an even number"
  (-> (from-status '[0 | 1 [2 3] [4 5] 6])
      (find-right even?)
      (status))
  => '([0 1 [2 3] [4 5] | 6]))

[[:section {:title "Walking"}]]

"`prewalk` and `postwalk` apply a function to every node of the zipper, similar to `std.lib.walk` but with explicit navigation."

(fact "prewalk over a vector zipper"
  (-> (vector-zip [[1 2] [3 4]])
      (prewalk (fn [v] (if (vector? v)
                         (conj v 100)
                         (+ v 100))))
      (root-element))
  => [[101 102 200] [103 104 200] 200])

(fact "postwalk over a vector zipper"
  (-> (vector-zip [[1 2] [3 4]])
      (postwalk (fn [v] (if (vector? v)
                          (conj v 100)
                          (+ v 100))))
      (root-element))
  => [[101 102 100] [103 104 100] 100])

[[:section {:title "End-to-end: navigate, edit, and read back"}]]

"A common workflow: create a zipper, navigate to a specific node, replace it, and extract the modified root."

(fact "replace the first odd number in a nested vector"
  (-> (vector-zip [2 [4 [5 6]] 8])
      (find-next odd?)
      (replace-right 99)
      (root-element))
  => [2 [4 [99 6]] 8])

[[:chapter {:title "API" :link "std.lib.zip"}]]

[[:api {:namespace "std.lib.zip"}]]
