### `std.block.type` Tutorial

**Module:** `std.block.type`
**Source File:** `src/std/block/type.clj`
**Test File:** `test/std/block/type_test.clj`

The `std.block.type` module defines the various concrete implementations of `std.block` AST nodes (VoidBlock, CommentBlock, TokenBlock, ContainerBlock, ModifierBlock). It also provides predicate functions to check the type of a block and functions to construct new instances of these block types directly. This module is crucial for understanding the internal representation of blocks and for low-level block manipulation.

#### Core Concepts

*   **`*tab-width*`:** A dynamic var controlling the assumed width of a tab character for layout calculations.
*   **`VoidBlock`:** Represents non-code elements like spaces, newlines, tabs, commas, or EOF signals.
*   **`CommentBlock`:** Represents single-line comments starting with `;`.
*   **`TokenBlock`:** Represents literal values, symbols, keywords, numbers, strings, etc.
*   **`ContainerBlock`:** Represents collections like lists, vectors, maps, and sets, holding other blocks as children.
*   **`ModifierBlock`:** Represents special forms that modify subsequent forms, like `#_` (uneval) or `|` (cursor).

#### Functions

##### `block-compare`

`^{:refer std.block.type/block-compare :added "3.0"}`

Compares two blocks for equality based on their tag and string representation. Returns 0 if equal, a negative number if the first is "less" than the second, and a positive number otherwise. This is used for `Comparable` implementation.

```clojure
(block-compare (construct/void \space)
                 (construct/void \space))
;; => 0
```

##### `void-block?`

`^{:refer std.block.type/void-block? :added "3.0"}`

Checks if a block is a `VoidBlock` instance.

```clojure
(void-block? (construct/void))
;; => true
```

##### `void-block`

`^{:refer std.block.type/void-block :added "3.0"}`

Constructs a new `VoidBlock` instance directly.

```clojure
(-> (void-block :linespace \tab 1 0)
    (base/block-info))
;; => {:type :void, :tag :linespace, :string "\t", :height 0, :width 1}
```

##### `space-block?`

`^{:refer std.block.type/space-block? :added "3.0"}`

Checks if a block represents a space character.

```clojure
(space-block? (construct/space))
;; => true
```

##### `linebreak-block?`

`^{:refer std.block.type/linebreak-block? :added "3.0"}`

Checks if a block represents a linebreak character.

```clojure
(linebreak-block? (construct/newline))
;; => true
```

##### `linespace-block?`

`^{:refer std.block.type/linespace-block? :added "3.0"}`

Checks if a block represents a non-linebreak whitespace character (e.g., `\space`, `\tab`).

```clojure
(linespace-block? (construct/space))
;; => true
```

##### `eof-block?`

`^{:refer std.block.type/eof-block? :added "3.0"}`

Checks if a block represents the end-of-file signal.

```clojure
(eof-block? (construct/void nil))
;; => true
```

##### `nil-void?`

`^{:refer std.block.type/nil-void? :added "3.0"}`

Checks if a block is `nil` or a `VoidBlock`.

```clojure
(nil-void? nil)
;; => true

(nil-void? (construct/block nil))
;; => false

(nil-void? (construct/space))
;; => true
```

##### `comment-block?`

`^{:refer std.block.type/comment-block? :added "3.0"}`

Checks if a block is a `CommentBlock` instance.

```clojure
(comment-block? (construct/comment ";;hello"))
;; => true
```

##### `comment-block`

`^{:refer std.block.type/comment-block :added "3.0"}`

Constructs a new `CommentBlock` instance directly.

```clojure
(-> (comment-block ";hello")
    (base/block-info))
;; => {:type :comment, :tag :comment, :string ";hello", :height 0, :width 6}
```

##### `token-block?`

`^{:refer std.block.type/token-block? :added "3.0"}`

Checks if a block is a `TokenBlock` instance.

```clojure
(token-block? (construct/token "hello"))
;; => true
```

##### `token-block`

`^{:refer std.block.type/token-block :added "3.0"}`

Constructs a new `TokenBlock` instance directly.

```clojure
(base/block-info (token-block :symbol "abc" 'abc "abc" 3 0))
;; => {:type :token, :tag :symbol, :string "abc", :height 0, :width 3}
```

##### `container-width`

`^{:refer std.block.type/container-width :added "3.0"}`

Calculates the visual width of a container block, considering its children and delimiters.

```clojure
(container-width (construct/block [1 2 3 4]))
;; => 9
```

##### `container-height`

`^{:refer std.block.type/container-height :added "3.0"}`

Calculates the height (number of lines) of a container block.

```clojure
(container-height (construct/block [(construct/newline)
                                      (construct/newline)]))
;; => 2
```

##### `container-string`

`^{:refer std.block.type/container-string :added "3.0"}`

Returns the string representation of a container block, including its delimiters and children's string representations. This is a multimethod extending `base/block-tag`.

```clojure
(container-string (construct/block [1 2 3]))
;; => "[1 2 3]"
```

##### `container-value-string`

`^{:refer std.block.type/container-value-string :added "3.0"}`

Returns the string representation used to generate the *value* of a container block, often used for debugging or internal representation.

```clojure
(container-value-string (construct/block [::a :b :c]))
;; => "[:std.block.type-test/a :b :c]"

(container-value-string (parse/parse-string "[::a :b :c]"))
;; => "[(keyword ":a") (keyword "b") (keyword "c")]"
```

##### `container-block?`

`^{:refer std.block.type/container-block? :added "3.0"}`

Checks if a block is a `ContainerBlock` instance.

```clojure
(container-block? (construct/block []))
;; => true
```

##### `container-block`

`^{:refer std.block.type/container-block :added "3.0"}`

Constructs a new `ContainerBlock` instance directly.

```clojure
(-> (container-block :fn [(construct/token '+)
                            (construct/void)
                            (construct/token '1)]
                       (construct/*container-props* :fn))
    (base/block-value))
;; => '(fn* [] (+ 1))
```

##### `modifier-block?`

`^{:refer std.block.type/modifier-block? :added "3.0"}`

Checks if a block is a `ModifierBlock` instance.

```clojure
(modifier-block? (construct/uneval))
;; => true
```

##### `modifier-block`

`^{:refer std.block.type/modifier-block :added "3.0"}`

Constructs a new `ModifierBlock` instance directly.

```clojure
(modifier-block :hash-uneval "#_" (fn [acc _] acc))
;; => #std.block.type.ModifierBlock{:tag :hash-uneval, :string "#_", :command #function[...]}
```