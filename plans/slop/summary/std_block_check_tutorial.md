### `std.block.check` Tutorial

**Module:** `std.block.check`
**Source File:** `src/std/block/check.clj`
**Test File:** `test/std/block/check_test.clj`

The `std.block.check` module provides utility functions for classifying characters and Clojure forms based on their syntactic role within `std.block`'s parsing and construction logic. It defines predicates for various types of characters (whitespace, delimiters, linebreaks) and forms (tokens, collections, void elements).

#### Core Concepts

*   **`*boundaries*`:** A dynamic var containing a set of characters that act as boundaries in Clojure syntax (e.g., space, colon, semicolon, parentheses, brackets, braces).
*   **`*linebreaks*`:** A dynamic var containing a set of characters that represent line breaks (`\newline`, `\return`, `\formfeed`).
*   **`*delimiters*`:** A dynamic var containing a set of characters that act as delimiters for collections (`}`, `]`, `)`, `(`, `[`, `{`).
*   **`*void-checks*`:** A dynamic var mapping void block tags (e.g., `:eof`, `:linetab`) to their respective predicate functions.
*   **`*token-checks*`:** A dynamic var mapping token block tags (e.g., `:nil`, `:boolean`, `:symbol`) to their respective predicate functions.
*   **`*collection-checks*`:** A dynamic var mapping collection block tags (e.g., `:list`, `:map`, `:set`, `:vector`) to their respective predicate functions.

#### Functions

##### `boundary?`

`^{:refer std.block.check/boundary? :added "3.0"}`

Returns `true` if a character is considered a boundary character in Clojure syntax.

```clojure
(boundary? (first "["))
;; => true

(boundary? (first """))
;; => true
```

##### `whitespace?`

`^{:refer std.block.check/whitespace? :added "3.0"}`

Returns `true` if a character is a whitespace character (including spaces, tabs, newlines).

```clojure
(whitespace? \space)
;; => true
```

##### `comma?`

`^{:refer std.block.check/comma? :added "3.0"}`

Returns `true` if a character is a comma.

```clojure
(comma? (first ","))
;; => true
```

##### `linebreak?`

`^{:refer std.block.check/linebreak? :added "3.0"}`

Returns `true` if a character is a linebreak character.

```clojure
(linebreak? \newline)
;; => true
```

##### `delimiter?`

`^{:refer std.block.check/delimiter? :added "3.0"}`

Returns `true` if a character is a collection delimiter (e.g., `(`, `)`, `[`, `]`, `{`, `}`).

```clojure
(delimiter? (first ")"))
;; => true
```

##### `voidspace?`

`^{:refer std.block.check/voidspace? :added "3.0"}`

Determines if an input character represents a "void space" (whitespace or comma).

```clojure
(voidspace? \newline)
;; => true
```

##### `linetab?`

`^{:refer std.block.check/linetab? :added "3.0"}`

Checks if a character is a tab character.

```clojure
(linetab? (first "\t"))
;; => true
```

##### `linespace?`

`^{:refer std.block.check/linespace? :added "3.0"}`

Returns `true` if a character is a whitespace character that is *not* a linebreak or a tab.

```clojure
(linespace? \space)
;; => true
```

##### `voidspace-or-boundary?`

`^{:refer std.block.check/voidspace-or-boundary? :added "3.0"}`

Checks if a character is either a void space or a boundary character.

```clojure
(->> (map voidspace-or-boundary? (concat *boundaries*
                                         *linebreaks*))
     (every? true?))
;; => true
```

##### `tag`

`^{:refer std.block.check/tag :added "3.0"}`

Takes a map of checks (predicate functions) and an input, returning the tag (key) of the first predicate that returns `true`.

```clojure
(tag *void-checks* \space)
;; => :linespace

(tag *collection-checks* [])
;; => :vector
```

##### `void-tag`

`^{:refer std.block.check/void-tag :added "3.0"}`

Returns the void tag associated with a character (e.g., `:linebreak` for `\newline`).

```clojure
(void-tag \newline)
;; => :linebreak
```

##### `void?`

`^{:refer std.block.check/void? :added "3.0"}`

Determines if a character corresponds to a void block type.

```clojure
(void? \newline)
;; => true
```

##### `token-tag`

`^{:refer std.block.check/token-tag :added "3.0"}`

Returns the token tag associated with a Clojure form (e.g., `:symbol` for a symbol, `:boolean` for `true`).

```clojure
(token-tag 'hello)
;; => :symbol
```

##### `token?`

`^{:refer std.block.check/token? :added "3.0"}`

Determines if a Clojure form is a token type.

```clojure
(token? 3/4)
;; => true
```

##### `collection-tag`

`^{:refer std.block.check/collection-tag :added "3.0"}`

Returns the collection tag associated with a Clojure form (e.g., `:vector` for `[]`, `:map` for `{}`).

```clojure
(collection-tag [])
;; => :vector
```

##### `collection?`

`^{:refer std.block.check/collection? :added "3.0"}`

Determines if a Clojure form is a collection type.

```clojure
(collection? {})
;; => true
```

##### `comment?`

`^{:refer std.block.check/comment? :added "3.0"}`

Determines if a string is a comment (starts with `;`).

```clojure
(comment? "hello")
;; => false

(comment? ";hello")
;; => true
```