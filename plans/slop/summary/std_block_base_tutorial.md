### `std.block.base` Tutorial

**Module:** `std.block.base`
**Source File:** `src/std/block/base.clj`
**Test File:** `test/std/block/base_test.clj`

The `std.block.base` module defines the fundamental protocols and core functions for interacting with `std.block` AST nodes. It establishes the basic building blocks and properties that all block types share, such as type, tag, string representation, and dimensions.

#### Core Concepts

*   **`IBlock` Protocol:** The foundational protocol that all `std.block` nodes implement. It defines the basic interface for querying block properties.
*   **`IBlockExpression` Protocol:** Extends `IBlock` for blocks that have an associated Clojure value.
*   **`IBlockModifier` Protocol:** Extends `IBlock` for blocks that modify an accumulator (e.g., unevaluated forms).
*   **`IBlockContainer` Protocol:** Extends `IBlock` for blocks that can contain other blocks (e.g., lists, vectors, maps).
*   **`*block-types*`:** A dynamic var containing a set of all recognized block types (`:void`, `:token`, `:comment`, `:container`, `:modifier`).
*   **`*container-tags*`:** A dynamic var mapping container types to their specific tags (e.g., `:collection` to `#{:list :map :set :vector}`).
*   **`*block-tags*`:** A dynamic var merging `*container-tags*` with tags for other block types.
*   **`*void-representations*`:** A dynamic var mapping special characters (like `\space`, `\newline`) to their void block tags.
*   **`*container-limits*`:** A dynamic var defining the start and end delimiters for various container types (e.g., `{:list {:start "(" :end ")"}}`).

#### Functions

##### `block?`

`^{:refer std.block.base/block? :added "3.0"}`

Checks whether an object is an `IBlock` instance.

```clojure
(block? (construct/void nil))
;; => true

(block? (construct/token "hello"))
;; => true
```

##### `block-type`

`^{:refer std.block.base/block-type :added "3.0"}`

Returns the block's type as a keyword (e.g., `:void`, `:token`, `:container`).

```clojure
(block-type (construct/void nil))
;; => :void

(block-type (construct/token "hello"))
;; => :token
```

##### `block-tag`

`^{:refer std.block.base/block-tag :added "3.0"}`

Returns the block's specific tag as a keyword (e.g., `:eof`, `:linespace`, `:symbol`).

```clojure
(block-tag (construct/void nil))
;; => :eof

(block-tag (construct/void \space))
;; => :linespace
```

##### `block-string`

`^{:refer std.block.base/block-string :added "3.0"}`

Returns the raw string representation of the block as it would appear in the source file.

```clojure
(block-string (construct/token 3/4))
;; => "3/4"

(block-string (construct/void \space))
;; => " "
```

##### `block-length`

`^{:refer std.block.base/block-length :added "3.0"}`

Returns the total character length of the block's string representation.

```clojure
(block-length (construct/void))
;; => 1

(block-length (construct/block [1 2 3 4]))
;; => 9
;; (e.g., "[1 2 3 4]")
```

##### `block-width`

`^{:refer std.block.base/block-width :added "3.0"}`

Returns the visual width of the block (number of characters on a single line).

```clojure
(block-width (construct/token 'hello))
;; => 5
```

##### `block-height`

`^{:refer std.block.base/block-height :added "3.0"}`

Returns the height of the block (number of lines it spans).

```clojure
(block-height (construct/block
               ^:list [(construct/newline)
                       (construct/newline)]))
;; => 2
```

##### `block-prefixed`

`^{:refer std.block.base/block-prefixed :added "3.0"}`

Returns the length of any starting characters (e.g., `(` for a list, `[` for a vector).

```clojure
(block-prefixed (construct/block #{}))
;; => 2
;; (e.g., for a set like #{})
```

##### `block-suffixed`

`^{:refer std.block.base/block-suffixed :added "3.0"}`

Returns the length of any ending characters (e.g., `)` for a list, `]` for a vector).

```clojure
(block-suffixed (construct/block #{}))
;; => 1
;; (e.g., for a set like #{})
```

##### `block-verify`

`^{:refer std.block.base/block-verify :added "3.0"}`

Checks that the block has correct internal data and structure.

```clojure
;; Example from test code, but no direct assertion provided.
;; This function likely returns true for valid blocks.
;; (block-verify (construct/token "valid"))
;; => true
```

##### `expression?`

`^{:refer std.block.base/expression? :added "3.0"}`

Checks if the block has a Clojure value associated with it (i.e., implements `IBlockExpression`).

```clojure
(expression? (construct/token 1.2))
;; => true
```

##### `block-value`

`^{:refer std.block.base/block-value :added "3.0"}`

Returns the actual Clojure value represented by an expression block.

```clojure
(block-value (construct/token 1.2))
;; => 1.2
```

##### `block-value-string`

`^{:refer std.block.base/block-value-string :added "3.0"}`

Returns the string representation from which the block's value was generated. This can differ from `block-string` for special forms.

```clojure
(block-value-string (parse/parse-string "#(+ 1 ::2)"))
;; => "#(+ 1 (keyword ":2"))"
```

##### `modifier?`

`^{:refer std.block.base/modifier? :added "3.0"}`

Checks if the block is of type `IBlockModifier`.

```clojure
(modifier? (construct/uneval))
;; => true
```

##### `block-modify`

`^{:refer std.block.base/block-modify :added "3.0"}`

Allows a modifier block to modify an accumulator. Used in parsing and transformation.

```clojure
(block-modify (construct/uneval) [1 2] 'ANYTHING)
;; => [1 2]
```

##### `container?`

`^{:refer std.block.base/container? :added "3.0"}`

Determines whether a block has children (i.e., implements `IBlockContainer`).

```clojure
(container? (parse/parse-string "[1 2 3]"))
;; => true

(container? (parse/parse-string " "))
;; => false
```

##### `block-children`

`^{:refer std.block.base/block-children :added "3.0"}`

Returns a sequence of child blocks within a container block.

```clojure
(->> (block-children (parse/parse-string "[1   2]"))
     (map block-string))
;; => ("1" "   " "2")
```

##### `replace-children`

`^{:refer std.block.base/replace-children :added "3.0"}`

Replaces the children of a container block with a new sequence of children.

```clojure
(->> (replace-children (construct/block [])
                       (conj (vec (block-children (construct/block [1 2]))) 
                             (construct/void \space)
                             (construct/block [3 4])))
     str)
;; => "[1 2 [3 4]]"
```

##### `block-info`

`^{:refer std.block.base/block-info :added "3.0"}`

Returns a map containing basic information about the block, including its type, tag, string, height, and width.

```clojure
(block-info (construct/token true))
;; => {:type :token, :tag :boolean, :string "true", :height 0, :width 4}

(block-info (construct/void \tab))
;; => {:type :void, :tag :linetab, :string "\t", :height 0, :width 4}
```
