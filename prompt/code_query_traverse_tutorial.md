### `code.query.traverse` Tutorial

**Module:** `code.query.traverse`
**Source File:** `src/code/query/traverse.clj`
**Test File:** `test/code/query/traverse_test.clj`

The `code.query.traverse` module provides a powerful mechanism for traversing and transforming Clojure code ASTs (represented as `std.block` navigators) based on declarative patterns. It enables complex operations like inserting, deleting, and modifying code elements while maintaining structural integrity. This module is fundamental for building advanced code manipulation tools.

#### Core Concepts

*   **`Position` Record:** A record that encapsulates the current state of a traversal, including the `source` navigator, the `pattern` navigator, and the `op` (operation) map.
*   **Pattern-driven Traversal:** The traversal is guided by a `pattern` (a Clojure form with special metadata and symbols) that dictates how to navigate and what actions to perform.
*   **Operations (`op` map):** A map of functions (`:delete-form`, `:insert-level`, `:cursor-node`, etc.) that define how to handle different parts of the pattern during traversal.
*   **Metadata Flags:** `^:+` (insert), `^:-` (delete), `^:?` (optional) on forms within the pattern control the transformation behavior.
*   **Cursor (`|`):** Marks a specific point of interest in the pattern, allowing for precise targeting of operations.

#### Functions

##### `pattern-zip`

`^{:refer code.query.traverse/pattern-zip :added "3.0"}`

Creates a `clojure.zip` zipper specifically configured for traversing Clojure forms (lists and vectors) as patterns.

```clojure
;; No direct test example, but it's used internally to create pattern navigators.
```

##### `wrap-meta`

`^{:refer code.query.traverse/wrap-meta :added "3.0"}`

A higher-order function that wraps a traversal function to handle metadata tags. It ensures that metadata forms are correctly processed during traversal.

```clojure
;; No direct test example, but used internally by traversal functions.
```

##### `wrap-delete-next`

`^{:refer code.query.traverse/wrap-delete-next :added "3.0"}`

A wrapper function for deleting the element immediately following the current position in a zipper. Used internally by deletion traversal.

```clojure
;; No direct test example, but used internally by deletion traversal functions.
```

##### `traverse-delete-form`

`^{:refer code.query.traverse/traverse-delete-form :added "3.0"}`

Traverses a form marked for deletion, recursively applying deletion logic to its children.

```clojure
;; No direct test example, but used internally by deletion traversal functions.
```

##### `traverse-delete-node`

`^{:refer code.query.traverse/traverse-delete-node :added "3.0"}`

Handles the deletion of a single node during traversal.

```clojure
;; No direct test example, but used internally by deletion traversal functions.
```

##### `traverse-delete-level`

`^{:refer code.query.traverse/traverse-delete-level :added "3.0"}`

Traverses a level within the AST, applying deletion logic based on the pattern.

```clojure
;; No direct test example, but used internally by deletion traversal functions.
```

##### `prep-insert-pattern`

`^{:refer code.query.traverse/prep-insert-pattern :added "3.0"}`

Prepares a pattern element for insertion by removing its metadata and evaluating it if marked with `:%`.

```clojure
;; No direct test example, but used internally by insertion traversal functions.
```

##### `wrap-insert-next`

`^{:refer code.query.traverse/wrap-insert-next :added "3.0"}`

A wrapper function for inserting an element immediately following the current position in a zipper. Used internally by insertion traversal.

```clojure
;; No direct test example, but used internally by insertion traversal functions.
```

##### `traverse-insert-form`

`^{:refer code.query.traverse/traverse-insert-form :added "3.0"}`

Traverses a form marked for insertion, recursively applying insertion logic to its children.

```clojure
;; No direct test example, but used internally by insertion traversal functions.
```

##### `traverse-insert-node`

`^{:refer code.query.traverse/traverse-insert-node :added "3.0"}`

Handles the insertion of a single node during traversal.

```clojure
;; No direct test example, but used internally by insertion traversal functions.
```

##### `traverse-insert-level`

`^{:refer code.query.traverse/traverse-insert-level :added "3.0"}`

Traverses a level within the AST, applying insertion logic based on the pattern.

```clojure
;; No direct test example, but used internally by insertion traversal functions.
```

##### `wrap-cursor-next`

`^{:refer code.query.traverse/wrap-cursor-next :added "3.0"}`

A wrapper function for locating the cursor at the next element during code traversal.

```clojure
;; No direct test example, but used internally by cursor traversal functions.
```

##### `traverse-cursor-form`

`^{:refer code.query.traverse/traverse-cursor-form :added "3.0"}`

Traverses a form to locate the cursor within it.

```clojure
;; No direct test example, but used internally by cursor traversal functions.
```

##### `traverse-cursor-level`

`^{:refer code.query.traverse/traverse-cursor-level :added "3.0"}`

Traverses a level within the AST to locate the cursor.

```clojure
;; No direct test example, but used internally by cursor traversal functions.
```

##### `count-elements`

`^{:refer code.query.traverse/count-elements :added "3.0"}`

Counts the number of elements in a given code structure. Used internally for pattern matching.

```clojure
;; No direct test example, but used internally.
```

##### `traverse`

`^{:refer code.query.traverse/traverse :added "3.0"}`

The main traversal function. It takes a source navigator and a pattern, and applies the transformations (insertions, deletions) defined by the pattern to the source.

```clojure
(source
 (traverse (nav/parse-string "^:a (+ () 2 3)")
           '(+ () 2 3)))
;; => '(+ () 2 3)

(source
 (traverse (nav/parse-string "^:a (hello)")
           '(hello)))
;; => '(hello)

;; Deletions
(source
 (traverse (nav/parse-string "^:a (hello)")
           '(^:- hello)))
;; => ()

(source
 (traverse (nav/parse-string "(hello)")
           '(^:- hello)))
;; => ()

(source
 (traverse (nav/parse-string "((hello))")
           '((^:- hello))))
;; => '(()) 

;; Insertions
(source
 (traverse (nav/parse-string "()")
           '(^:+ hello)))
;; => '(hello)

(source
 (traverse (nav/parse-string "(())")
           '((^:+ hello))))
;; => '((hello))

;; More advanced transformations
(source
 (traverse (nav/parse-string "(defn hello)")
           '(defn ^{:? true :% true} symbol? ^:+ [])))
;; => '(defn hello [])

(source
 (traverse (nav/parse-string "(defn hello)")
           '(defn ^{:? true :% true :- true} symbol? ^:+ [])))
;; => '(defn [])

(source
 (traverse (nav/parse-string "(defn hello)")
           '(defn ^{:? true :% true :- true} symbol? | ^:+ [])))
;; => []

(source
 (traverse (nav/parse-string "(defn hello "world" {:a 1} [])")
           '(defn ^:% symbol?
              ^{:? true :% true :- true} string?
              ^{:? true :% true :- true} map?
              ^:% vector? & _)))
;; => '(defn hello [])

(source
 (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
           '(defn _ _ (+ | 1 & _))))
;; => 1

(source
 (traverse (nav/parse-string "(defn hello [] (+ 1 1))")
           '(#{defn} | & _)))
;; => 'hello

(source
 (traverse (nav/parse-string "(fact "hello world")")
           '(fact | & _)))
;; => "hello world"
```

##### `source`

`^{:refer code.query.traverse/source :added "3.0"}`

Retrieves the final source code (Clojure form) from a traversed `Position` record.

```clojure
(source
 (traverse (nav/parse-string "()")
           '(^:+ hello)))
;; => '(hello)
```