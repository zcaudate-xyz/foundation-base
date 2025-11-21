### `code.query.walk` Tutorial

**Module:** `code.query.walk`
**Source File:** `src/code/query/walk.clj`
**Test File:** `test/code/query/walk_test.clj`

The `code.query.walk` module provides functions for traversing and transforming `std.block` navigators (ASTs) based on patterns. It offers two primary walking strategies: `matchwalk` for deep, recursive traversal and `levelwalk` for top-level matching. These functions are essential for implementing automated code transformations and refactorings.

#### Core Concepts

*   **Navigator (`nav/`):** Functions from `code.query.block` (aliased as `nav`) are used to create and manipulate the AST context.
*   **Matchers:** Predicates (created using `code.query.match` functions) that determine whether a block matches a specific pattern.
*   **Transformation Function (`f`):** A function that takes a matching navigator and returns a transformed navigator.
*   **Wrappers:** Functions like `wrap-meta` and `wrap-suppress` enhance the walking behavior by handling metadata and exceptions.

#### Functions

##### `wrap-meta`

`^{:refer code.query.walk/wrap-meta :added "3.0"}`

A higher-order function that wraps a walk function to correctly handle metadata tags. It ensures that metadata forms are properly traversed and processed.

```clojure
;; No direct test example, but used internally by matchwalk and levelwalk.
```

##### `wrap-suppress`

`^{:refer code.query.walk/wrap-suppress :added "3.0"}`

A higher-order function that wraps a walk function to suppress exceptions during traversal, returning the original navigator in case of an error.

```clojure
;; No direct test example, but used internally by matchwalk and levelwalk.
```

##### `matchwalk`

`^{:refer code.query.walk/matchwalk :added "3.0"}`

Performs a deep, recursive traversal of the AST. It applies a transformation function `f` to every block that matches any of the provided `matchers`.

```clojure
(-> (matchwalk (nav/parse-string "(+ (+ (+ 8 9)))")
               [(match/compile-matcher '+)]
               (fn [nav]
                 (-> nav nav/down (nav/replace '-) nav/up)))
    nav/value)
;; => '(- (- (- 8 9)))
```

##### `levelwalk`

`^{:refer code.query.walk/levelwalk :added "3.0"}`

Performs a top-level traversal of the AST. It applies a transformation function `f` only to blocks at the current level that match the provided matcher.

```clojure
(-> (levelwalk (nav/parse-string "(+ (+ (+ 8 9)))")
               [(match/compile-matcher '+)]
               (fn [nav]
                 (-> nav nav/down (nav/replace '-) nav/up)))
    nav/value)
;; => '(- (+ (+ 8 9)))
```