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

;; BEGIN merged documentation: plans/slop/summary/std_lib_zip_summary.md
;; sha256: 52d3472899a73e9b55157bc66f3d1d990573019cd2a7b04d73491c2446b6ece9
[[:chapter {:title "std.lib.zip: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-zip-summary-md"}]]

"The `std.lib.zip` namespace provides a powerful and flexible implementation of a zipper data structure, enabling efficient traversal, inspection, and modification of hierarchical data. It extends the core zipper concept with custom context functions, allowing it to operate on various data structures (sequences, vectors, etc.) and offering a rich set of navigation and editing operations. This module is crucial for tasks involving tree-like data manipulation, such as AST transformations, XML/HTML processing, or UI component tree management."

"**Key Features and Concepts:**"

"1.  **Zipper Structure and Context:**\n    *   **`*handler*`**: A dynamic var that can be bound to a map of custom handler functions for specific zipper operations (e.g., `:step :at-left-most`).\n    *   **`+nil-handler+`**: A map of `h/NIL` functions for various handler points, serving as a default.\n    *   **`Zipper` Record**: The core data structure for the zipper. It holds:\n        *   `context`: A map of functions defining how the zipper interacts with the underlying data structure (e.g., `is-container?`, `list-elements`).\n        *   `left`: A sequence of elements to the left of the current position.\n        *   `right`: A sequence of elements to the right of the current position.\n        *   `parent`: The parent zipper node, forming the hierarchical context.\n        *   `prefix`, `display`: Optional fields for custom string representation.\n    *   **`check-context [context]`**: Validates that a zipper `context` map contains all required functions.\n    *   **`check-optional [context]`**: Validates that a zipper `context` map contains all optional functions.\n    *   **`zipper? [x]`**: A predicate to check if an object is a `Zipper`.\n    *   **`zipper [root context & [opts]]`**: Constructs a new `Zipper` instance from a `root` data structure and a `context` map.\n\n2.  **Element Access and Inspection:**\n    *   **`left-element [zip]`**: Returns the element immediately to the left of the current position.\n    *   **`right-element [zip]`**: Returns the element immediately to the right of the current position.\n    *   **`left-elements [zip]`**: Returns all elements to the left of the current position.\n    *   **`right-elements [zip]`**: Returns all elements to the right of the current position.\n    *   **`current-elements [zip]`**: Returns all elements at the current level (left and right).\n    *   **`is [zip pred & [step]]`**: Checks if the element at the current position (or a specified `step`) satisfies a `pred`icate.\n    *   **`get [zip & [func step]]`**: Retrieves the element at the current position (or a specified `step`), optionally applying a `func`.\n    *   **`is-container? [zip & [step]]`**: Checks if the element at the current position (or a specified `step`) is a container.\n    *   **`is-empty-container? [zip & [step]]`**: Checks if the element at the current position (or a specified `step`) is an empty container.\n\n3.  **Navigation (Step Operations):**\n    *   **`at-left-most? [zip]`**: Checks if the cursor is at the left-most position in the current container.\n    *   **`at-right-most? [zip]`**: Checks if the cursor is at the right-most position in the current container.\n    *   **`at-inside-most? [zip]`**: Checks if the cursor is at the deepest possible position to the right within the current container.\n    *   **`at-inside-most-left? [zip]`**: Checks if the cursor is at the deepest possible position to the left within the current container.\n    *   **`at-outside-most? [zip]`**: Checks if the cursor is at the top-most level of the entire tree.\n    *   **`can-step-left? [zip]`**: Checks if it's possible to move left.\n    *   **`can-step-right? [zip]`**: Checks if it's possible to move right.\n    *   **`can-step-inside? [zip]`**: Checks if it's possible to move down into a child.\n    *   **`can-step-inside-left? [zip]`**: Checks if it's possible to move down into a child on the left.\n    *   **`can-step-outside? [zip]`**: Checks if it's possible to move up to the parent.\n    *   **`step-left [zip & [n]]`**: Moves the cursor `n` steps to the left.\n    *   **`step-right [zip & [n]]`**: Moves the cursor `n` steps to the right.\n    *   **`step-inside [zip & [n]]`**: Moves the cursor `n` steps down into the right-most child.\n    *   **`step-inside-left [zip & [n]]`**: Moves the cursor `n` steps down into the left-most child.\n    *   **`step-outside [zip & [n]]`**: Moves the cursor `n` steps up to the parent.\n    *   **`step-outside-right [zip & [n]]`**: Moves the cursor `n` steps up and then right.\n    *   **`step-left-most [zip]`**: Moves the cursor to the left-most position in the current container.\n    *   **`step-right-most [zip]`**: Moves the cursor to the right-most position in the current container.\n    *   **`step-inside-most [zip]`**: Moves the cursor to the deepest possible position to the right within the current container.\n    *   **`step-inside-most-left [zip]`**: Moves the cursor to the deepest possible position to the left within the current container.\n    *   **`step-outside-most [zip]`**: Moves the cursor to the top-most level of the entire tree.\n    *   **`step-outside-most-right [zip]`**: Moves the cursor to the top-most level and then to the right-most position.\n    *   **`step-end [zip]`**: Moves the cursor to the end of the tree (deepest right-most element).\n\n4.  **Editing Operations:**\n    *   **`insert-left [zip data & more]`**: Inserts one or more elements to the left of the current position.\n    *   **`insert-token-to-right [zip data & more]`**: Inserts one or more elements to the right of the current position.\n    *   **`delete-left [zip & [n]]`**: Deletes `n` elements to the left of the current position.\n    *   **`delete-right [zip & [n]]`**: Deletes `n` elements to the right of the current position.\n    *   **`replace-left [zip data]`**: Replaces the element to the left of the current position.\n    *   **`replace-right [zip data]`**: Replaces the element to the right of the current position.\n    *   **`surround [zip]`**: Nests the current elements within a new container.\n\n5.  **Tree Traversal and Search:**\n    *   **`hierarchy [zip]`**: Returns a sequence of zipper nodes from the current position up to the root.\n    *   **`at-end? [zip]`**: Checks if the cursor is at the end of the tree (deepest right-most element).\n    *   **`root-element [zip]`**: Returns the root element of the tree.\n    *   **`status [zip]`**: Returns the data structure with a marker (`|`) indicating the current cursor position.\n    *   **`status-string [zip]`**: Returns a string representation of the data structure with the cursor marker.\n    *   **`step-next [zip]`**: Moves the cursor to the next element in depth-first order.\n    *   **`step-prev [zip]`**: Moves the cursor to the previous element in reverse depth-first order.\n    *   **`find [zip move pred]`**: Finds the first element satisfying a `pred`icate by repeatedly applying a `move` function.\n    *   **`find-left [zip pred]`**: Finds the first element satisfying `pred` by moving left.\n    *   **`find-right [zip pred]`**: Finds the first element satisfying `pred` by moving right.\n    *   **`find-next [zip pred]`**: Finds the first element satisfying `pred` by moving in depth-first order.\n    *   **`find-prev [zip pred]`**: Finds the first element satisfying `pred` by moving in reverse depth-first order.\n    *   **`from-status [data & [zipper-fn]]`**: Constructs a zipper from a data structure that includes a `|` marker for the initial cursor position.\n    *   **`prewalk [zip f]`**: Emulates `std.lib.walk/prewalk` behavior using the zipper.\n    *   **`postwalk [zip f]`**: Emulates `std.lib.walk/postwalk` behavior using the zipper.\n    *   **`matchwalk [zip matchers f & [matchwalk opts]]`**: Performs a walk, applying `f` when a matcher is found, and can recurse into sub-trees.\n    *   **`levelwalk [zip [pred] f & [levelwalk opts]]`**: Performs a walk at the current level, applying `f` when a predicate is met.\n\n6.  **Zipper Types:**\n    *   **`seq-zip [root & [opts]]`**: Constructs a zipper for sequence-like data structures.\n    *   **`vector-zip [root & [opts]]`**: Constructs a zipper for vector-like data structures."

"**Overall Importance:**"

"The `std.lib.zip` module is a powerful tool for manipulating hierarchical data within the `foundation-base` project. Its key contributions include:"

"*   **Efficient Tree Manipulation**: Provides a functional and efficient way to navigate and modify tree-like data structures without explicit recursion.\n*   **Abstract Data Structure Operations**: The customizable `context` allows the zipper to operate on various underlying data representations (sequences, vectors, custom trees).\n*   **Precise Cursor Control**: Offers fine-grained control over the cursor's position within the data structure, enabling targeted modifications.\n*   **Simplified Transformations**: Functions like `prewalk` and `postwalk` (implemented with the zipper) provide familiar traversal patterns for complex transformations.\n*   **Debugging and Visualization**: The `status` and `status-string` functions are invaluable for visualizing the zipper's current position and the state of the data."

"By offering these advanced tree manipulation capabilities, `std.lib.zip` significantly enhances the `foundation-base` project's ability to process and transform complex data models, which is vital for its multi-language development ecosystem."
;; END merged documentation: plans/slop/summary/std_lib_zip_summary.md
