### `code.query.match` Tutorial

**Module:** `code.query.match`
**Source File:** `src/code/query/match.clj`
**Test File:** `test/code/query/match_test.clj`

The `code.query.match` module provides a powerful and flexible system for pattern matching against Clojure code represented as `std.block` navigators. It allows you to define complex matching rules using a declarative syntax, enabling tasks like code analysis, refactoring, and linting. The module is built around the concept of "matchers" – functions that take a navigator and return `true` if the current position matches a given pattern.

#### Core Concepts

*   **Matcher:** A function (or an instance of `code.query.match/Matcher` record) that takes a `code.query.block` navigator as input and returns a boolean indicating whether the current block matches a defined pattern.
*   **Predicate Functions (`p-*`):** A rich set of functions (prefixed with `p-`) that create matchers for various conditions, such as checking the value, type, metadata, or structural relationships (parent, child, sibling) of a block.
*   **Query Language:** Matchers can be composed using logical operators (`p-and`, `p-or`, `p-not`) and can be built from a declarative data structure using `compile-matcher`.
*   **Navigator (`nav/`):** Functions from `code.query.block` (aliased as `nav`) are used extensively to create and manipulate the AST context for matching.

#### Functions

##### `matcher`

`^{:refer code.query.match/matcher :added "3.0"}`

Creates a `Matcher` record from a predicate function. This allows any function that takes a navigator and returns a boolean to be used as a matcher.

```clojure
((matcher string?) "hello")
;; => true
```

##### `matcher?`

`^{:refer code.query.match/matcher? :added "3.0"}`

Checks if an object is a `Matcher` instance.

```clojure
(matcher? (matcher string?))
;; => true
```

##### `p-fn`

`^{:refer code.query.match/p-fn :added "3.0"}`

Creates a matcher that applies a given predicate function directly to the navigator.

```clojure
((p-fn (fn [nav]
           (-> nav (nav/tag) (= :symbol))))
 (nav/parse-string "defn"))
;; => true
```

##### `p-not`

`^{:refer code.query.match/p-not :added "3.0"}`

Creates a matcher that negates the result of another matcher.

```clojure
((p-not (p-is 'if)) (nav/parse-string "defn"))
;; => true

((p-not (p-is 'if)) (nav/parse-string "if"))
;; => false
```

##### `p-is`

`^{:refer code.query.match/p-is :added "3.0"}`

Creates a matcher that checks if the current block's value is equivalent to a given template, ignoring metadata.

```clojure
((p-is 'defn) (nav/parse-string "defn"))
;; => true

((p-is '^{:a 1} defn) (nav/parse-string "defn"))
;; => true

((p-is 'defn) (nav/parse-string "is"))
;; => false

((p-is '(defn & _)) (nav/parse-string "(defn x [])"))
;; => false
```

##### `p-equal-loop`

`^{:refer code.query.match/p-equal-loop :added "3.0"}`

A helper function for `p-equal` that recursively compares two Clojure forms for deep equality, including collection contents.

```clojure
((p-equal [1 2 3]) (nav/parse-string "[1 2 3]"))
;; => true

((p-equal (list 'defn)) (nav/parse-string "(defn)"))
;; => true

((p-equal '(defn)) (nav/parse-string "(defn)"))
;; => true
```

##### `p-equal`

`^{:refer code.query.match/p-equal :added "3.0"}`

Creates a matcher that checks for deep equality between the current block's value and a template, including metadata.

```clojure
((p-equal '^{:a 1} defn) (nav/parse-string "defn"))
;; => false

((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 1} defn"))
;; => true

((p-equal '^{:a 1} defn) (nav/parse-string "^{:a 2} defn"))
;; => false
```

##### `p-meta`

`^{:refer code.query.match/p-meta :added "3.0"}`

Creates a matcher that checks if the metadata of the current block's parent (if it's a meta form) matches a given template.

```clojure
((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 1} defn")))
;; => true

((p-meta {:a 1}) (nav/down (nav/parse-string "^{:a 2} defn")))
;; => false
```

##### `p-type`

`^{:refer code.query.match/p-type :added "3.0"}`

Creates a matcher that checks if the `block-tag` of the current block matches a given type keyword (e.g., `:symbol`, `:list`).

```clojure
((p-type :symbol) (nav/parse-string "defn"))
;; => true

((p-type :symbol) (-> (nav/parse-string "^{:a 1} defn") nav/down nav/right))
;; => true
```

##### `p-form`

`^{:refer code.query.match/p-form :added "3.0"}`

Creates a matcher that checks if the current block is a list form whose first element (the function/macro name) matches a given symbol.

```clojure
((p-form 'defn) (nav/parse-string "(defn x [])"))
;; => true
((p-form 'let) (nav/parse-string "(let [])"))
;; => true
```

##### `p-pattern`

`^{:refer code.query.match/p-pattern :added "3.0"}`

Creates a matcher that checks if the current block's value matches a complex pattern defined using Clojure forms and special query symbols (like `_`, `&`). This leverages `code.query.match.pattern`.

```clojure
((p-pattern '(defn ^:% symbol? & _)) (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true

((p-pattern '(defn ^:% symbol? ^{:% true :? true} string? []))
 (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true
```

##### `p-code`

`^{:refer code.query.match/p-code :added "3.0"}`

Creates a matcher that checks if the string representation of the current block matches a given regular expression.

```clojure
((p-code #"defn") (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true
```

##### `p-and`

`^{:refer code.query.match/p-and :added "3.0"}`

Combines multiple matchers, returning `true` only if all of them match.

```clojure
((p-and (p-code #"defn")
          (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
;; => false

((p-and (p-code #"defn")
          (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true
```

##### `p-or`

`^{:refer code.query.match/p-or :added "3.0"}`

Combines multiple matchers, returning `true` if at least one of them matches.

```clojure
((p-or (p-code #"defn")
          (p-type :token)) (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true

((p-or (p-code #"defn")
          (p-type :list)) (nav/parse-string "(defn ^{:a 1} x [])"))
;; => true
```

##### `compile-matcher`

`^{:refer code.query.match/compile-matcher :added "3.0"}`

The main entry point for creating complex matchers from a declarative data structure (a map, vector, symbol, or function). It recursively compiles the structure into a composite matcher.

```clojure
((compile-matcher {:is 'hello}) (nav/parse-string "hello"))
;; => true
```

##### `p-parent`

`^{:refer code.query.match/p-parent :added "3.0"}`

Creates a matcher that checks if the parent of the current block matches a given template.

```clojure
((p-parent 'defn) (-> (nav/parse-string "(defn x [])") nav/next nav/next))
;; => true

((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => true

((p-parent {:parent 'if}) (-> (nav/parse-string "(if (= x y))") nav/down))
;; => false
```

##### `p-child`

`^{:refer code.query.match/p-child :added "3.0"}`

Creates a matcher that checks if any child of the current container block matches a given template.

```clojure
((p-child {:form '=}) (nav/parse-string "(if (= x y))"))
;; => true

((p-child '=) (nav/parse-string "(if (= x y))"))
;; => false
```

##### `p-first`

`^{:refer code.query.match/p-first :added "3.0"}`

Creates a matcher that checks if the first element of the current container block matches a given template.

```clojure
((p-first 'defn) (-> (nav/parse-string "(defn x [])")))
;; => true

((p-first 'x) (-> (nav/parse-string "[x y z]")))
;; => true

((p-first 'x) (-> (nav/parse-string "[y z]")))
;; => false
```

##### `p-last`

`^{:refer code.query.match/p-last :added "3.0"}`

Creates a matcher that checks if the last element of the current container block matches a given template.

```clojure
((p-last 1) (-> (nav/parse-string "(defn [] 1)")))
;; => true

((p-last 'z) (-> (nav/parse-string "[x y z]")))
;; => true

((p-last 'x) (-> (nav/parse-string "[y z]")))
;; => false
```

##### `p-nth`

`^{:refer code.query.match/p-nth :added "3.0"}`

Creates a matcher that checks if the element at a specific Nth index within the current container block matches a given template.

```clojure
((p-nth [0 'defn]) (-> (nav/parse-string "(defn [] 1)")))
;; => true

((p-nth [2 'z]) (-> (nav/parse-string "[x y z]")))
;; => true

((p-nth [2 'x]) (-> (nav/parse-string "[y z]")))
;; => false
```

##### `p-nth-left`

`^{:refer code.query.match/p-nth-left :added "3.0"}`

Creates a matcher that checks if the element at a specific Nth index to the left of the current position has a certain characteristic.

```clojure
((p-nth-left [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
;; => true

((p-nth-left [1 ^:& vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down nav/right-most))
;; => true
```

##### `p-nth-right`

`^{:refer code.query.match/p-nth-right :added "3.0"}`

Creates a matcher that checks if the element at a specific Nth index to the right of the current position has a certain characteristic.

```clojure
((p-nth-right [0 'defn]) (-> (nav/parse-string "(defn [] 1)") nav/down))
;; => true

((p-nth-right [1 vector?]) (-> (nav/parse-string "(defn [] 1)") nav/down))
;; => true
```

##### `p-nth-ancestor`

`^{:refer code.query.match/p-nth-ancestor :added "3.0"}`

Creates a matcher that searches for a match `n` levels up in the ancestor chain.

```clojure
((p-nth-ancestor [2 {:contains 3}])
 (-> (nav/parse-string "(* (- (+ 1 2) 3) 4)")
     nav/down nav/right nav/down nav/right nav/down))
;; => true
```

##### `tree-search`

`^{:refer code.query.match/tree-search :added "3.0"}`

A helper function for `p-contains` that recursively searches a tree structure for elements matching a predicate.

```clojure
;; No direct test example, but used internally by p-contains.
```

##### `p-contains`

`^{:refer code.query.match/p-contains :added "3.0"}`

Creates a matcher that checks if any element (deeply nested) within the current container matches a given template.

```clojure
((p-contains '=) (nav/parse-string "(if (= x y))"))
;; => true

((p-contains 'x) (nav/parse-string "(if (= x y))"))
;; => true
```

##### `tree-depth-search`

`^{:refer code.query.match/tree-depth-search :added "3.0"}`

A helper function for `p-nth-contains` that performs a depth-first search for a match `n` levels down in a tree structure.

```clojure
;; No direct test example, but used internally by p-nth-contains.
```

##### `p-nth-contains`

`^{:refer code.query.match/p-nth-contains :added "3.0"}`

Creates a matcher that searches for a match `n` levels down in the tree.

```clojure
((p-nth-contains [2 {:contains 1}])
 (nav/parse-string "(* (- (+ 1 2) 3) 4)"))
;; => true
```

##### `p-ancestor`

`^{:refer code.query.match/p-ancestor :added "3.0"}`

Creates a matcher that checks if any ancestor of the current block matches a given template.

```clojure
((p-ancestor {:form 'if}) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => true

((p-ancestor 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => true
```

##### `p-sibling`

`^{:refer code.query.match/p-sibling :added "3.0"}`

Creates a matcher that checks if any element on the same level (sibling) as the current block matches a given template.

```clojure
((p-sibling '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => false

((p-sibling 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => true
```

##### `p-left`

`^{:refer code.query.match/p-left :added "3.0"}`

Creates a matcher that checks if the immediate left sibling of the current block matches a given template.

```clojure
((p-left '=) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next nav/next))
;; => true

((p-left 'if) (-> (nav/parse-string "(if (= x y))") nav/down nav/next))
;; => true
```

##### `p-right`

`^{:refer code.query.match/p-right :added "3.0"}`

Creates a matcher that checks if the immediate right sibling of the current block matches a given template.

```clojure
((p-right 'x) (-> (nav/parse-string "(if (= x y))") nav/down nav/next nav/next))
;; => true

((p-right {:form '=}) (-> (nav/parse-string "(if (= x y))") nav/down))
;; => true
```

##### `p-left-of`

`^{:refer code.query.match/p-left-of :added "3.0"}`

Creates a matcher that checks if any element to the left of the current block (on the same level) matches a given template.

```clojure
((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next))
;; => true

((p-left-of '=) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
;; => true
```

##### `p-right-of`

`^{:refer code.query.match/p-right-of :added "3.0"}`

Creates a matcher that checks if any element to the right of the current block (on the same level) matches a given template.

```clojure
((p-right-of 'x) (-> (nav/parse-string "(= x y)") nav/down))
;; => true

((p-right-of 'y) (-> (nav/parse-string "(= x y)") nav/down))
;; => true

((p-right-of 'z) (-> (nav/parse-string "(= x y)") nav/down))
;; => false
```

##### `p-left-most`

`^{:refer code.query.match/p-left-most :added "3.0"}`

Creates a matcher that checks if the current block is the leftmost expression within its current level.

```clojure
((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down))
;; => true

((p-left-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
;; => false
```

##### `p-right-most`

`^{:refer code.query.match/p-right-most :added "3.0"}`

Creates a matcher that checks if the current block is the rightmost expression within its current level.

```clojure
((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next))
;; => false

((p-right-most true) (-> (nav/parse-string "(= x y)") nav/down nav/next nav/next))
;; => true
```