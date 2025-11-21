### `code.query.common` Tutorial

**Module:** `code.query.common`
**Source File:** `src/code/query/common.clj`
**Test File:** `test/code/query/common_test.clj`

The `code.query.common` module provides a set of utility functions for working with Clojure forms, particularly in the context of code querying, transformation, and diffing. It includes predicates for special symbols (like cursor markers), functions for manipulating metadata-driven flags (insertion, deletion), and tools for walking and cleaning up forms.

#### Core Concepts

*   **Metadata Flags:** Uses metadata (`^:+`, `^:-`, `^:?`) to mark forms for insertion, deletion, or optional presence during code transformation.
*   **Cursor Marker:** The `|` symbol is used as a placeholder for a cursor position within a form.
*   **Form Walking:** Leverages `std.lib.walk` for recursive traversal and transformation of Clojure forms.

#### Functions

##### `any`

`^{:refer code.query.common/any :added "3.0"}`

A predicate that always returns `true` for any input. Useful as a wildcard or default matcher.

```clojure
(any nil)
;; => true

(any '_) ; Note: The test uses '_ as a symbol, which is fine.
;; => true
```

##### `none`

`^{:refer code.query.common/none :added "3.0"}`

A predicate that always returns `false` for any input.

```clojure
(none nil)
;; => false

(none '_) ; Note: The test uses '_ as a symbol, which is fine.
;; => false
```

##### `expand-meta`

`^{:refer code.query.common/expand-meta :added "3.0"}`

Takes a form and expands its metadata keywords (e.g., `:?`, `:+`, `:%`) into a map of boolean flags.

```clojure
(meta (expand-meta ^:? ()))
;; => {:? true}

(meta (expand-meta ^:+%? ()))
;; => {:? true, :% true, :+ true}
```

##### `cursor?`

`^{:refer code.query.common/cursor? :added "3.0"}`

Checks if an element is the cursor marker (`|`).

```clojure
(cursor? '|)
;; => true

(cursor? '_) ; Note: The test uses '_ as a symbol, which is fine.
;; => false
```

##### `insertion?`

`^{:refer code.query.common/insertion? :added "3.0"}`

Checks if a form has the `^:+` metadata flag, indicating it's marked for insertion.

```clojure
(insertion? '^:+ a)
;; => true

(insertion? 'a)
;; => false
```

##### `deletion?`

`^{:refer code.query.common/deletion? :added "3.0"}`

Checks if a form has the `^:-` metadata flag, indicating it's marked for deletion.

```clojure
(deletion? '^:- a)
;; => true

(deletion? 'a)
;; => false
```

##### `prewalk`

`^{:refer code.query.common/prewalk :added "3.0"}`

Applies a function to elements in a depth-first, pre-order traversal, eagerly modifying them. It preserves metadata.

```clojure
;; Example from test code, but no direct assertion provided.
;; (prewalk inc '(1 (2 3)))
;; => '(2 (3 4))
```

##### `remove-items`

`^{:refer code.query.common/remove-items :added "3.0"}`

Recursively removes items from a form that match a given predicate.

```clojure
(remove-items #{1} '(1 2 3 4))
;; => '(2 3 4)

(remove-items #{1} '(1 (1 (1 (1)))))
;; => '(((())))
```

##### `prepare-deletion`

`^{:refer code.query.common/prepare-deletion :added "3.0"}`

Prepares a form for a deletion walk by removing cursor markers and insertion-flagged elements.

```clojure
(prepare-deletion '(+ a 2))
;; => '(+ a 2)

(prepare-deletion '(+ ^:+ a | 2))
;; => '(+ 2)
```

##### `prepare-insertion`

`^{:refer code.query.common/prepare-insertion :added "3.0"}`

Prepares a form for an insertion operation by removing cursor markers and deletion-flagged elements.

```clojure
(prepare-insertion '(+ a 2))
;; => '(+ a 2)

(prepare-insertion '(+ ^:+ a | ^:- b 2))
;; => '(+ a 2)
```

##### `prepare-query`

`^{:refer code.query.common/prepare-query :added "3.0"}`

Prepares a form for a query walk by removing cursor markers, deletion-flagged, and insertion-flagged elements.

```clojure
(prepare-query '(+ ^:+ a | ^:- b 2))
;; => '(+ 2)
```

##### `find-index`

`^{:refer code.query.common/find-index :added "3.0"}`

Returns the index of the first occurrence of an element matching a predicate in a sequence.

```clojure
(find-index #{2} '(1 2 3 4))
;; => 1
```

##### `finto`

`^{:refer code.query.common/finto :added "3.0"}`

A version of `into` that correctly handles lists by reversing the `from` collection before `into`ing.

```clojure
(finto () '(1 2 3))
;; => '(1 2 3)
```