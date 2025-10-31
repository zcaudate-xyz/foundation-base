### `std.block.value` Tutorial

**Module:** `std.block.value`
**Source File:** `src/std/block/value.clj`
**Test File:** `test/std/block/value_test.clj`

The `std.block.value` module is responsible for extracting the actual Clojure values from `std.block` AST nodes. It provides functions to convert various block types (tokens, collections, special forms) back into their corresponding Clojure data structures, handling modifiers like `#_` (uneval) in the process. This module is crucial for bridging the gap between the AST representation and executable Clojure code.

#### Core Concepts

*   **Value Extraction:** The primary goal is to get the Clojure data represented by a block.
*   **Modifier Application:** Correctly applies the logic of modifier blocks (like `#_`) during value extraction.
*   **`*container-values*`:** A dynamic var mapping container tags to functions that extract their Clojure value.

#### Functions

##### `apply-modifiers`

`^{:refer std.block.value/apply-modifiers :added "3.0"}`

Applies modifier blocks within a sequence of blocks to an accumulator. For example, `#_` modifiers will remove the subsequent block from the sequence.

```clojure
(apply-modifiers [(construct/uneval)
                  (construct/uneval)
                  1 2 3])
;; => [3]
```

##### `child-values`

`^{:refer std.block.value/child-values :added "3.0"}`

Returns the Clojure values of the children within a container block, applying any modifiers present.

```clojure
(child-values (parse/parse-string "[1 #_2 3]"))
;; => [1 3]
```

##### `root-value`

`^{:refer std.block.value/root-value :added "3.0"}`

Returns the Clojure value of a `:root` block, typically as a `(do ...)` form if it contains multiple top-level expressions.

```clojure
(root-value (parse/parse-string "#[1 2 3]"))
;; => '(do 1 2 3)
```

##### `from-value-string`

`^{:refer std.block.value/from-value-string :added "3.0"}`

Reads a Clojure value from the `block-value-string` of a block. This is useful for blocks where the string representation for value generation differs from the raw string.

```clojure
(from-value-string (parse/parse-string "(+ 1 1)"))
;; => '(+ 1 1)
```

##### `list-value`

`^{:refer std.block.value/list-value :added "3.0"}`

Returns the Clojure `list` value of an `:list` block.

```clojure
(list-value (parse/parse-string "(+ 1 1)"))
;; => '(+ 1 1)
```

##### `map-value`

`^{:refer std.block.value/map-value :added "3.0"}`

Returns the Clojure `map` value of an `:map` block.

```clojure
(map-value (parse/parse-string "{1 2 3 4}"))
;; => {1 2, 3 4}

(map-value (parse/parse-string "{1 2 3}"))
;; => (throws)
```

##### `set-value`

`^{:refer std.block.value/set-value :added "3.0"}`

Returns the Clojure `set` value of an `:set` block.

```clojure
(set-value (parse/parse-string "#{1 2 3 4}"))
;; => #{1 4 3 2}
```

##### `vector-value`

`^{:refer std.block.value/vector-value :added "3.0"}`

Returns the Clojure `vector` value of an `:vector` block.

```clojure
(vector-value (parse/parse-string "[1 2 3 4]"))
;; => [1 2 3 4]
```

##### `deref-value`

`^{:refer std.block.value/deref-value :added "3.0"}`

Returns the Clojure value of a `:deref` block (e.g., `@atom`).

```clojure
(deref-value (parse/parse-string "@hello"))
;; => '(deref hello)
```

##### `meta-value`

`^{:refer std.block.value/meta-value :added "3.0"}`

Returns the Clojure value of a `:meta` block (e.g., `^:dynamic x`).

```clojure
((juxt meta identity)
 (meta-value (parse/parse-string "^:dynamic {:a 1}")))
;; => [{:dynamic true} {:a 1}]

((juxt meta identity)
 (meta-value (parse/parse-string "^String {:a 1}")))
;; => [{:tag 'String} {:a 1}]
```

##### `quote-value`

`^{:refer std.block.value/quote-value :added "3.0"}`

Returns the Clojure value of a `:quote` block (e.g., `'symbol`).

```clojure
(quote-value (parse/parse-string "'hello"))
;; => '(quote hello)
```

##### `var-value`

`^{:refer std.block.value/var-value :added "3.0"}`

Returns the Clojure value of a `:var` block (e.g., `#'symbol`).

```clojure
(var-value (parse/parse-string "#'hello"))
;; => '(var hello)
```

##### `hash-keyword-value`

`^{:refer std.block.value/hash-keyword-value :added "3.0"}`

Returns the Clojure value of a `:hash-keyword` block (e.g., `#:prefix{:key value}`).

```clojure
(hash-keyword-value (parse/parse-string "#:hello{:a 1 :b 2}"))
;; => #:hello{:b 2, :a 1}
```

##### `select-value`

`^{:refer std.block.value/select-value :added "3.0"}`

Returns the Clojure value of a `:select` block (reader conditional, e.g., `#?(:clj x)`).

```clojure
(select-value (parse/parse-string "#?(:clj hello)"))
;; => '(? {:clj hello})
```

##### `select-splice-value`

`^{:refer std.block.value/select-splice-value :added "3.0"}`

Returns the Clojure value of a `:select-splice` block (reader conditional splicing, e.g., `#?@(:clj x)`).

```clojure
(select-splice-value (parse/parse-string "#?@(:clj hello)"))
;; => '(?-splicing {:clj hello})
```

##### `unquote-value`

`^{:refer std.block.value/unquote-value :added "3.0"}`

Returns the Clojure value of an `:unquote` block (e.g., `~x`).

```clojure
(unquote-value (parse/parse-string "~hello"))
;; => '(unquote hello)
```

##### `unquote-splice-value`

`^{:refer std.block.value/unquote-splice-value :added "3.0"}`

Returns the Clojure value of an `:unquote-splice` block (e.g., `~@x`).

```clojure
(unquote-splice-value (parse/parse-string "~@hello"))
;; => '(unquote-splicing hello)
```