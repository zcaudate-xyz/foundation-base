# `code.query` Summary

`code.query` is a library for querying and manipulating code structures represented as `std.block` trees. It provides a powerful way to navigate, match, and transform code, making it a key component of the `foundation-base` transpiler and code analysis tools.

**Core Concepts:**

*   **Navigator:** A zipper-like data structure (`code.query.block/navigator`) that allows for efficient traversal and manipulation of the `std.block` tree.
*   **Matchers:** Predicate functions (`code.query.match`) that can be used to find specific nodes or patterns in the code tree.
*   **Traversal:** Functions for walking the code tree and applying transformations (`code.query.walk` and `code.query.traverse`).
*   **Compilation:** The `code.query.compile` namespace provides functions for compiling a query pattern into an efficient matcher.

**Key Areas of Functionality (with Examples):**

*   **Navigation (`code.query.block`):**
    *   **`navigator`**: Creates a navigator from a `std.block` tree.
    *   **`up`, `down`, `left`, `right`**: Functions for moving the navigator around the tree.
    *   **`node`**: Returns the current block at the navigator's focus.
    *   **`root-string`**: Returns the string representation of the entire code tree.
        ```clojure
        (require '[code.query.block :as nav])

        (def nav (nav/parse-string "(+ 1 2)"))
        (-> nav nav/down nav/right nav/node base/block-value)
        ;; => 1
        ```

*   **Matching (`code.query.match`):**
    *   **`p-is`**: Matches a specific value.
    *   **`p-form`**: Matches a form with a specific symbol as its first element.
    *   **`p-pattern`**: Matches a code pattern with wildcards and predicates.
    *   **`p-and`, `p-or`, `p-not`**: For combining matchers.
    *   **`p-parent`, `p-child`, `p-ancestor`, `p-contains`**: For matching based on relationships between nodes.
        ```clojure
        (require '[code.query.match :as match])

        (def nav (nav/parse-string "(defn my-fn [x] (+ x 1))"))
        ((match/p-form 'defn) nav)
        ;; => true
        ((match/p-pattern '(defn _ [_] _)) nav)
        ;; => true
        ```

*   **Walking and Traversal (`code.query.walk`, `code.query.traverse`):**
    *   **`matchwalk`**: Traverses the code tree and applies a function to all nodes that match a given pattern.
    *   **`levelwalk`**: Similar to `matchwalk`, but only applies the function to nodes at the top level of the tree.
    *   **`traverse`**: A more powerful traversal function that can be used to perform complex transformations, including insertions and deletions.
        ```clojure
        (require '[code.query.walk :as walk])
        (require '[code.query.match :as match])

        (-> (walk/matchwalk (nav/parse-string "(+ 1 (+ 2 3))")
                            [(match/p-is '+)]
                            (fn [nav] (nav/replace nav '-)))
            nav/root-string)
        ;; => "(- 1 (- 2 3))"
        ```

*   **Compilation (`code.query.compile`):**
    *   **`prepare`**: Compiles a query pattern into a more efficient internal representation that can be used by the matching and traversal functions. This is mostly used internally by the library.
