### `std.block.grid` Tutorial

**Module:** `std.block.grid`
**Source File:** `src/std/block/grid.clj`
**Test File:** `test/std/block/grid_test.clj`

The `std.block.grid` module provides advanced functionality for formatting and indenting `std.block` AST nodes. It's designed to take a raw block structure and apply a set of rules to produce a "gridded" or well-formatted code string, handling line breaks, indentation, and comment placement. This module is crucial for pretty-printing and code generation where consistent formatting is required.

#### Core Concepts

*   **`*bind-length*`:** A dynamic var controlling the length of binding indentation.
*   **`*indent-length*`:** A dynamic var controlling the base indentation length.
*   **Indentation Rules:** The `grid` function takes a map of rules that define how different forms (e.g., `if-let`, `do`, `let`) should be indented. These rules can specify `indent`, `bind`, and `scope`.
    *   `indent`: The base indentation level.
    *   `bind`: The number of binding forms to consider for special indentation.
    *   `scope`: A vector or map defining how child scopes should be indented.

#### Functions

##### `trim-left`

`^{:refer std.block.grid/trim-left :added "3.0"}`

Removes leading whitespace nodes from a sequence of blocks.

```clojure
(->> (trim-left [(construct/space)
                 :a
                 (construct/space)])
     (mapv str))
;; => [":a" "␣"]
```

##### `trim-right`

`^{:refer std.block.grid/trim-right :added "3.0"}`

Removes trailing whitespace nodes from a sequence of blocks.

```clojure
(->> (trim-right [(construct/space)
                  :a
                  (construct/space)])
     (mapv str))
;; => ["␣" ":a"]
```

##### `split-lines`

`^{:refer std.block.grid/split-lines :added "3.0"}`

Splits a sequence of blocks into sub-sequences, where each sub-sequence represents a line, retaining linebreak nodes.

```clojure
(split-lines [:a :b (construct/newline) :c :d])
;; => [[:a :b]
;;     [(construct/newline) :c :d]]
```

##### `remove-starting-spaces`

`^{:refer std.block.grid/remove-starting-spaces :added "3.0"}`

Removes redundant spaces at the beginning of lines, especially after linebreaks.

```clojure
(remove-starting-spaces [[(construct/newline)
                          (construct/space)
                          (construct/space) :a :b]
                           [(construct/newline) (construct/space) :c :d]])
;; => [[(construct/newline) :a :b]
;;     [(construct/newline) :c :d]]
```

##### `adjust-comments`

`^{:refer std.block.grid/adjust-comments :added "3.0"}`

Adds additional newlines after comments to ensure proper formatting and readability.

```clojure
(->> (adjust-comments [(construct/comment ";hello") :a])
     (mapv str))
;; => [";hello" "\n" ":a"]
```

##### `remove-extra-linebreaks`

`^{:refer std.block.grid/remove-extra-linebreaks :added "3.0"}`

Removes redundant or excessive linebreak nodes from a sequence of lines.

```clojure
(remove-extra-linebreaks [[:a]
                            [(construct/newline)]
                            [(construct/newline)]
                            [(construct/newline)]
                            [:b]])
;; => [[:a]
;;     [(construct/newline)]
;;     [:b]]
```

##### `grid-scope`

`^{:refer std.block.grid/grid-scope :added "3.0"}`

Calculates the grid scope for child nodes based on the parent scope. This is an internal helper for indentation logic.

```clojure
(grid-scope [{0 1} 1])
;; => [{0 1} 0]
```

##### `grid-rules`

`^{:refer std.block.grid/grid-rules :added "3.0"}`

Creates indentation rules for the current block based on its tag, symbol, parent scope, and a global rules map.

```clojure
(grid-rules :list nil nil nil)
;; => {:indent 0, :bind 0, :scope []}

(grid-rules :vector nil nil nil)
;; => {:indent 0, :bind 0, :scope [0]}

(grid-rules :list 'add [1] nil)
;; => {:indent 1, :bind 0, :scope [0]}

(grid-rules :list 'if nil '{if {:indent 1}})
;; => {:indent 1, :bind 0, :scope []}
```

##### `indent-bind`

`^{:refer std.block.grid/indent-bind :added "3.0"}`

Returns the number of lines to indent for binding forms within a block, based on the `bind` rule.

```clojure
(indent-bind [[(construct/token 'if-let)]
              [(construct/newline)]
              [(construct/newline) (construct/block '[i (pos? 0)])]
              [(construct/newline) (construct/block '(+ i 1))]]
             1)
;; => 2

(indent-bind [[(construct/token 'if-let)]
              [(construct/newline)]
              [(construct/newline) (construct/block '[i (pos? 0)])]
              [(construct/newline) (construct/block '(+ i 1))]]
             0)
;; => 0
```

##### `indent-lines`

`^{:refer std.block.grid/indent-lines :added "3.0"}`

Indents a sequence of lines based on a given anchor and indentation rule.

```clojure
(-> (indent-lines [[(construct/token 'if-let)]
                   [(construct/newline)]
                   [(construct/newline) (construct/block '[i (pos? 0)])]
                   [(construct/newline) (construct/block '(+ i 1))]]
                  1
                  {:indent 1
                   :bind 1})
    (construct/contents))
;; => '([if-let]
;;      (\n ␣ ␣ ␣ ␣)
;;      (\n ␣ ␣ ␣ ␣ [i (pos? 0)])
;;      (\n ␣ ␣ (+ i 1)))
```

##### `grid`

`^{:refer std.block.grid/grid :added "3.0"}`

The main function for formatting a container block. It applies indentation rules and scope to produce a well-formatted block structure.

```clojure
(-> (construct/block ^:list ['if-let
                             (construct/newline)
                             (construct/newline) (construct/block '[i (pos? 0)])
                             (construct/newline) (construct/block '(+ i 1))])
    (grid 1 {:rules {'if-let {:indent 1
                              :bind 1}}})
    (construct/contents))
;; => '(if-let
;;      \n ␣ ␣ ␣ ␣ ␣
;;      \n ␣ ␣ ␣ ␣ ␣ [i (pos? 0)]
;;      \n ␣ ␣ (+ i 1)))
```