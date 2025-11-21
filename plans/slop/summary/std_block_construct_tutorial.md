### `std.block.construct` Tutorial

**Module:** `std.block.construct`
**Source File:** `src/std/block/construct.clj`
**Test File:** `test/std/block/construct_test.clj`

The `std.block.construct` module provides functions for programmatically creating `std.block` AST nodes. These functions are essential for building block structures from Clojure data, which can then be manipulated, transformed, and eventually rendered back into code strings. It offers constructors for various block types, including void blocks, tokens, comments, and containers.

#### Core Concepts

*   **`*tags*`:** A dynamic var consolidating tags for different block types (void, token, collection, comment, meta, cons, literal, macro, modifier).
*   **`+space+`, `+newline+`, `+return+`, `+formfeed+`:** Pre-defined `std.block` instances for common void characters, optimizing their creation.
*   **`void-lookup`:** A map for quickly retrieving pre-defined void blocks.

#### Functions

##### `void`

`^{:refer std.block.construct/void :added "3.0"}`

Creates a void block. Void blocks represent non-code elements like spaces, newlines, or comments.

```clojure
(str (void))
;; => "␣"

(str (void \newline))
;; => "\n"
```

##### `space`

`^{:refer std.block.construct/space :added "3.0"}`

Creates a single space block.

```clojure
(str (space))
;; => "␣"
```

##### `spaces`

`^{:refer std.block.construct/spaces :added "3.0"}`

Creates a sequence of `n` space blocks.

```clojure
(apply str (spaces 5))
;; => "␣␣␣␣␣"
```

##### `tab`

`^{:refer std.block.construct/tab :added "3.0"}`

Creates a single tab block.

```clojure
(str (tab))
;; => "\t"
```

##### `tabs`

`^{:refer std.block.construct/tabs :added "3.0"}`

Creates a sequence of `n` tab blocks.

```clojure
(apply str (tabs 5))
;; => "\t\t\t\t\t"
```

##### `newline`

`^{:refer std.block.construct/newline :added "3.0"}`

Creates a single newline block.

```clojure
(str (newline))
;; => "\n"
```

##### `newlines`

`^{:refer std.block.construct/newlines :added "3.0"}`

Creates a sequence of `n` newline blocks.

```clojure
(apply str (newlines 5))
;; => "\n\n\n\n\n"
```

##### `comment`

`^{:refer std.block.construct/comment :added "3.0"}`

Creates a comment block from a string. The string must start with `;`.

```clojure
(str (comment ";hello"))
;; => ";hello"

;; Throws exception if string is not a valid comment
;; (str (comment "hello"))
;; => ExceptionInfo: "Not a valid comment string."
```

##### `token-dimensions`

`^{:refer std.block.construct/token-dimensions :added "3.0"}`

Returns the `[width height]` of a token based on its tag and string representation.

```clojure
(token-dimensions :regexp "#\"hello\nworld\"")
;; => [6 1]

(token-dimensions :regexp "#\"hello\nworld\n\"")
;; => [15 0]
```

##### `string-token`

`^{:refer std.block.construct/string-token :added "3.0"}`

Constructs a token block specifically for Clojure string literals, including quotes and handling newlines within the string.

```clojure
(str (string-token "hello"))
;; => "\"hello\""

(str (string-token "hello\nworld"))
;; => "\"hello\\nworld\""
```

##### `token`

`^{:refer std.block.construct/token :added "3.0"}`

Creates a token block from a Clojure form (symbol, number, keyword, etc.). It automatically determines the correct tag and string representation.

```clojure
(str (token 'abc))
;; => "abc"

(str (token 123))
;; => "123"
```

##### `token-from-string`

`^{:refer std.block.construct/token-from-string :added "3.0"}`

Creates a token block by reading a string input. This is useful for creating tokens from raw text.

```clojure
(str (token-from-string "abc"))
;; => "abc"

(str (token-from-string "123"))
;; => "123"
```

##### `container-checks`

`^{:refer std.block.construct/container-checks :added "3.0"}`

Performs validation checks for a container block based on its tag, children, and properties. This is an internal helper.

```clojure
;; No direct test example, but it ensures validity of container construction.
;; (container-checks :list [(token 1)] {:cons 1})
;; => true
```

##### `container`

`^{:refer std.block.construct/container :added "3.0"}`

Creates a container block (e.g., list, vector, map, set). It takes a tag and a sequence of child blocks.

```clojure
(str (container :list [(void) (void)]))
;; => "(  )"

(str (container :vector [(token 1) (space) (token 2)]))
;; => "[1 2]"
```

##### `uneval`

`^{:refer std.block.construct/uneval :added "3.0"}`

Creates a hash-uneval block (`#_`), which is a modifier block used to comment out the next form.

```clojure
(str (uneval))
;; => "#_"
```

##### `cursor`

`^{:refer std.block.construct/cursor :added "3.0"}`

Creates a cursor block (`|`), used for navigation or indicating a position.

```clojure
(str (cursor))
;; => "|"
```

##### `construct-collection`

`^{:refer std.block.construct/construct-collection :added "3.0"}`

A multimethod for constructing collection blocks (`:list`, `:vector`, `:set`, `:map`) from Clojure data.

```clojure
(str (construct-collection [1 2 (void) (void) 3]))
;; => "[1 2  3]"

(str (construct-collection '(1 2 3)))
;; => "(1 2 3)"
```

##### `construct-children`

`^{:refer std.block.construct/construct-children :added "3.0"}`

Constructs a sequence of child blocks from a raw Clojure data structure, automatically inserting spaces and handling different element types.

```clojure
(mapv str (construct-children [1 (newline) (void) 2]))
;; => ["1" "\n" "␣" "2"]
```

##### `block`

`^{:refer std.block.construct/block :added "3.0"}`

The primary entry point for creating any type of `std.block` from a Clojure data element. It dispatches to `token`, `construct-collection`, or returns the element if it's already a block.

```clojure
(base/block-info (block 1))
;; => {:type :token, :tag :long, :string "1", :height 0, :width 1}

(str (block [1 (newline) (void) 2]))
;; => "[1\n 2]"
```

##### `add-child`

`^{:refer std.block.construct/add-child :added "3.0"}`

Adds a child element to an existing container block.

```clojure
(-> (block [])
    (add-child 1)
    (add-child 2)
    (str))
;; => "[1 2]"
```

##### `empty`

`^{:refer std.block.construct/empty :added "3.0"}`

Constructs an empty list block `()`.

```clojure
(str (empty))
;; => "()"
```

##### `root`

`^{:refer std.block.construct/root :added "3.0"}`

Constructs a root block, which is a special container that typically represents the top-level of a parsed file.

```clojure
(str (root '[a b]))
;; => "a b"
```

##### `contents`

`^{:refer std.block.construct/contents :added "3.0"}`

Reads out the contents of a container block, returning a Clojure data structure.

```clojure
(contents (block [1 (space) 2 (space) 3]))
;; => '[1 ␣ 2 ␣ 3]
```