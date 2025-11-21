### `code.query.block` Tutorial

**Module:** `code.query.block`
**Source File:** `src/code/query/block.clj`
**Test File:** `test/code/query/block_test.clj`

The `code.query.block` module provides a powerful navigation and manipulation API for `std.block` ASTs, built on top of `std.lib.zip` (zippers). It allows for cursor-based traversal, inspection, and modification of Clojure code represented as blocks, making it ideal for structural editing, refactoring tools, and code analysis.

#### Core Concepts

*   **Navigator:** The central data structure, which is a `std.lib.zip/Zipper` specifically configured for `std.block`s. It maintains a current position within the AST and provides functions to move around and modify the tree.
*   **Cursor (`#|`):** A special block (`construct/cursor`) used to denote the current position within the code string representation of a navigator.
*   **Position Tracking:** The navigator tracks the `[line column]` position of the cursor within the code.
*   **Expression vs. Element:** Functions often distinguish between "expressions" (blocks with a Clojure value) and "elements" (any block, including whitespace and comments).

#### Functions

##### `nav-template`

`^{:refer code.query.block/nav-template :added "3.0"}`

A helper macro for generating navigation function definitions. It takes a symbol and a block tag function, and creates a function that can be used to query properties of the current block in a navigator.

```clojure
(nav-template '-tag- #'std.block.base/block-tag)
;; => '(clojure.core/defn -tag-
;;       ([zip] (-tag- zip :right))
;;       ([zip step]
;;        (clojure.core/if-let [elem (std.lib.zip/get zip)]
;;          (std.block.base/block-tag elem))))
```

##### `left-anchor`

`^{:refer code.query.block/left-anchor :added "3.0" :class [:nav/primitive]}`

Calculates the length from the start of the current line to the current cursor position, considering newlines.

```clojure
(left-anchor (-> (navigator nil)
                   (zip/step-right)))
;; => 3
```

##### `update-step-left`

`^{:refer code.query.block/update-step-left :added "3.0" :class [:nav/primitive]}`

Updates the navigator's position when moving left, adjusting line and column numbers based on the block's dimensions.

```clojure
(-> {:position [0 7]}
      (update-step-left (construct/block [1 2 3])))
;; => {:position [0 0]}
```

##### `update-step-right`

`^{:refer code.query.block/update-step-right :added "3.0" :class [:nav/primitive]}`

Updates the navigator's position when moving right, adjusting line and column numbers.

```clojure
(-> {:position [0 0]}
      (update-step-right (construct/block [1 2 3])))
;; => {:position [0 7]}
```

##### `update-step-inside`

`^{:refer code.query.block/update-step-inside :added "3.0" :class [:nav/primitive]}`

Updates the navigator's position when stepping inside a container block, placing the cursor after the opening delimiter.

```clojure
(-> {:position [0 0]}
      (update-step-inside (construct/block #{})))
;; => {:position [0 2]}
```

##### `update-step-inside-left`

`^{:refer code.query.block/update-step-inside-left :added "3.0" :class [:nav/primitive]}`

Updates the navigator's position when stepping inside a container block from the right, placing the cursor before the closing delimiter.

```clojure
(-> {:position [0 3]}
      (update-step-inside-left (construct/block #{})))
;; => {:position [0 2]}
```

##### `update-step-outside`

`^{:refer code.query.block/update-step-outside :added "3.0" :class [:nav/primitive]}`

Updates the navigator's position when stepping outside a container block.

```clojure
(let [left-elems [(construct/block [1 2 3]) (construct/newline)]]
    (-> {:position [1 0]
         :left left-elems}
        (update-step-outside left-elems)
        :position))
;; => [0 7]
```

##### `display-navigator`

`^{:refer code.query.block/display-navigator :added "3.0" :class [:nav/primitive]}`

Returns a string representation of the navigator, including its position and the current block.

```clojure
(-> (navigator [1 2 3 4])
      (display-navigator))
;; => "<0,0> |[1 2 3 4]"
```

##### `navigator`

`^{:refer code.query.block/navigator :added "3.0" :class [:nav/general]}`

Creates a new `std.block` navigator from a block or Clojure data. This is the primary way to start navigating an AST.

```clojure
(str (navigator [1 2 3 4]))
;; => "<0,0> |[1 2 3 4]"
```

##### `navigator?`

`^{:refer code.query.block/navigator? :added "3.0" :class [:nav/general]}`

Checks if an object is a `std.block` navigator.

```clojure
(navigator? (navigator [1 2 3 4]))
;; => true
```

##### `from-status`

`^{:refer code.query.block/from-status :added "3.0" :class [:nav/general]}`

Constructs a navigator from a given status (a block with a cursor).

```clojure
(str (from-status (construct/block [1 2 3 (construct/cursor) 4])))
;; => "<0,7> [1 2 3 |4]"
```

##### `parse-string`

`^{:refer code.query.block/parse-string :added "3.0" :class [:nav/general]}`

Parses a string into a navigator, automatically placing the cursor at the beginning or at a `#|` marker if present.

```clojure
(str (parse-string "(2   #|   3  )"))
;; => "<0,5> (2   |   3  )"
```

##### `parse-root`

`^{:refer code.query.block/parse-root :added "3.0" :class [:nav/general]}`

Parses a root string into a navigator.

```clojure
(str (parse-root "a b c"))
;; => "<0,0> |a b c"
```

##### `parse-root-status`

`^{:refer code.query.block/parse-root-status :added "3.0" :class [:nav/general]}`

Parses a string and creates a navigator from its status, similar to `from-status` but for root strings.

```clojure
(str (parse-root-status "a b #|c"))
;; => "<0,6> a b |c"
```

##### `root-string`

`^{:refer code.query.block/root-string :added "3.0" :class [:nav/general]}`

Returns the string representation of the entire root block of the navigator.

```clojure
(root-string (navigator [1 2 3 4]))
;; => "[1 2 3 4]"
```

##### `left-expression`

`^{:refer code.query.block/left-expression :added "3.0" :class [:nav/general]}`

Returns the first expression block to the left of the current cursor position.

```clojure
(-> {:left [(construct/newline)
            (construct/block [1 2 3])]}
      (left-expression)
      (base/block-value))
;; => [1 2 3]
```

##### `left-expressions`

`^{:refer code.query.block/left-expressions :added "3.0" :class [:nav/general]}`

Returns all expression blocks to the left of the current cursor position.

```clojure
(->> {:left [(construct/newline)
             (construct/block :b)
             (construct/space)
             (construct/space)
             (construct/block :a)]}
       (left-expressions)
       (mapv base/block-value))
;; => [:a :b]
```

##### `right-expression`

`^{:refer code.query.block/right-expression :added "3.0" :class [:nav/general]}`

Returns the first expression block to the right of the current cursor position.

```clojure
(-> {:right [(construct/newline)
              (construct/block [1 2 3])]}
      (right-expression)
      (base/block-value))
;; => [1 2 3]
```

##### `right-expressions`

`^{:refer code.query.block/right-expressions :added "3.0" :class [:nav/general]}`

Returns all expression blocks to the right of the current cursor position.

```clojure
(->> {:right [(construct/newline)
               (construct/block :b)
               (construct/space)
               (construct/space)
               (construct/block :a)]}
        (right-expressions)
        (mapv base/block-value))
;; => [:b :a]
```

##### `left`

`^{:refer code.query.block/left :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next expression block on the left.

```clojure
(-> (parse-string "(1  [1 2 3]    #|)")
      (left)
      str)
;; => "<0,4> (1  |[1 2 3]    )"
```

##### `left-most`

`^{:refer code.query.block/left-most :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the leftmost expression block in the current level.

```clojure
(-> (parse-string "(1  [1 2 3]  3 4   #|)")
      (left-most)
      str)
;; => "<0,1> (|1  [1 2 3]  3 4   )"
```

##### `left-most?`

`^{:refer code.query.block/left-most? :added "3.0" :class [:nav/move]}`

Checks if the navigator's cursor is at the leftmost expression block.

```clojure
(-> (from-status [1 [(construct/cursor) 2 3]])
      (left-most?))
;; => true
```

##### `right`

`^{:refer code.query.block/right :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next expression block on the right.

```clojure
(-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right)
      str)
;; => "<0,10> ([1 2 3]  |3 4  )"
```

##### `right-most`

`^{:refer code.query.block/right-most :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the rightmost expression block in the current level.

```clojure
(-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right-most)
      str)
;; => "<0,12> ([1 2 3]  3 |4  )"
```

##### `right-most?`

`^{:refer code.query.block/right-most? :added "3.0" :class [:nav/move]}`

Checks if the navigator's cursor is at the rightmost expression block.

```clojure
(-> (from-status [1 [2 3 (construct/cursor)]])
      (right-most?))
;; => true
```

##### `up`

`^{:refer code.query.block/up :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor up to the parent container.

```clojure
(str (up (from-status [1 [2 (construct/cursor) 3]])))
;; => "<0,3> [1 |[2 3]]"
```

##### `down`

`^{:refer code.query.block/down :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor down into the first child expression of the current container.

```clojure
(str (down (from-status [1 (construct/cursor) [2 3]])))
;; => "<0,4> [1 [|2 3]]"
```

##### `right*`

`^{:refer code.query.block/right* :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next element (including whitespace) on the right.

```clojure
(str (right* (from-status [(construct/cursor) 1 2])))
;; => "<0,2> [1| 2]"
```

##### `left*`

`^{:refer code.query.block/left* :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next element (including whitespace) on the left.

```clojure
(str (left* (from-status [1 (construct/cursor) 2])))
;; => "<0,2> [1| 2]"
```

##### `block`

`^{:refer code.query.block/block :added "3.0" :class [:nav/general]}`

Returns the `std.block` AST node at the current cursor position.

```clojure
(block (from-status [1 [2 (construct/cursor) 3]]))
;; => (construct/block 3)
```

##### `prev`

`^{:refer code.query.block/prev :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the previous expression block in a depth-first traversal.

```clojure
(-> (parse-string "([1 2 [3]] #|)")
      (prev)
      str)
;; => "<0,7> ([1 2 [|3]] )"
```

##### `next`

`^{:refer code.query.block/next :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next expression block in a depth-first traversal.

```clojure
(-> (parse-string "(#|  [[3]]  )")
      (next)
      (next)
      (next)
      str)
;; => "<0,5> (  [[|3]]  )"
```

##### `find-next-token`

`^{:refer code.query.block/find-next-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next token block whose value matches the given data.

```clojure
(-> (parse-string "(#|  [[3 2]]  )")
      (find-next-token 2)
      str)
;; => "<0,7> (  [[3 |2]]  )"
```

##### `prev-anchor`

`^{:refer code.query.block/prev-anchor :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the previous newline or the beginning of the current line.

```clojure
(-> (parse-string "( \n \n [[3 \n]] #|  )")
      (prev-anchor)
      (:position))
;; => [3 0]

(-> (parse-string "( #| )")
      (prev-anchor)
      (:position))
;; => [0 0]
```

##### `next-anchor`

`^{:refer code.query.block/next-anchor :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next newline.

```clojure
(-> (parse-string "( \n \n#| [[3 \n]]  )")
      (next-anchor)
      (:position))
;; => [3 0]
```

##### `left-token`

`^{:refer code.query.block/left-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next token block on the left.

```clojure
(-> (parse-string "(1  {}  #|2 3 4)")
      (left-token)
      str)
;; => "<0,1> (|1  {}  2 3 4)"
```

##### `left-most-token`

`^{:refer code.query.block/left-most-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the leftmost token block in the current level.

```clojure
(-> (parse-string "(1  {}  2 3 #|4)")
      (left-most-token)
      str)
;; => "<0,10> (1  {}  2 |3 4)"
```

##### `right-token`

`^{:refer code.query.block/right-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next token block on the right.

```clojure
(-> (parse-string "(#|1  {}  2 3 4)")
      (right-token)
      str)
;; => "<0,8> (1  {}  |2 3 4)"
```

##### `right-most-token`

`^{:refer code.query.block/right-most-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the rightmost token block in the current level.

```clojure
(-> (parse-string "(#|1  {}  2 3 [4])")
      (right-most-token)
      str)
;; => "<0,10> (1  {}  2 |3 [4])"
```

##### `prev-token`

`^{:refer code.query.block/prev-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the previous token block in a depth-first traversal.

```clojure
(-> (parse-string "(1 (2 3 [4])#|)")
      (prev-token)
      str)
;; => "<0,9> (1 (2 3 [|4]))"
```

##### `next-token`

`^{:refer code.query.block/next-token :added "3.0" :class [:nav/move]}`

Moves the navigator's cursor to the next token block in a depth-first traversal.

```clojure
(-> (parse-string "(#|[[1 2 3 4]])")
      (next-token)
      str)
;; => "<0,3> ([[|1 2 3 4]])"
```

##### `position-left`

`^{:refer code.query.block/position-left :added "3.0" :class [:nav/move]}`

Moves the cursor to the left expression, skipping whitespace.

```clojure
(-> (parse-string "( 2   #|   3  )")
      (position-left)
      str)
;; => "<0,2> ( |2      3  )"

(-> (parse-string "(   #|   3  )")
      (position-left)
      str)
;; => "<0,1> (|      3  )"
```

##### `position-right`

`^{:refer code.query.block/position-right :added "3.0" :class [:nav/move]}`

Moves the cursor to the right expression, skipping whitespace.

```clojure
(-> (parse-string "(2   #|    3  )")
      (position-right)
      str)
;; => "<0,9> (2       |3  )"

(-> (parse-string "(2   #|     )")
      (position-right)
      str)
;; => "<0,10> (2        |)"
```

##### `tighten-left`

`^{:refer code.query.block/tighten-left :added "3.0" :class [:nav/edit]}`

Removes extra spaces on the left of the current expression.

```clojure
(-> (parse-string "(1 2 3   #|4)")
      (tighten-left)
      str)
;; => "<0,7> (1 2 3 |4)"

(-> (parse-string "(1 2 3   #|    4)")
      (tighten-left)
      str)
;; => "<0,7> (1 2 3 |4)"

(-> (parse-string "(    #|     )")
      (tighten-left)
      str)
;; => "<0,1> (|)"
```

##### `tighten-right`

`^{:refer code.query.block/tighten-right :added "3.0" :class [:nav/edit]}`

Removes extra spaces on the right of the current expression.

```clojure
(-> (parse-string "(1 2 #|3       4)")
      (tighten-right)
      str)
;; => "<0,5> (1 2 |3 4)"

(-> (parse-string "(1 2 3   #|    4)")
      (tighten-right)
      str)
;; => "<0,5> (1 2 |3 4)"

(-> (parse-string "(    #|     )")
      (tighten-right)
      str)
;; => "<0,1> (|)"
```

##### `tighten`

`^{:refer code.query.block/tighten :added "3.0" :class [:nav/edit]}`

Removes extra spaces on both the left and right of the current expression.

```clojure
(-> (parse-string "(1 2      #|3       4)")
      (tighten)
      str)
;; => "<0,5> (1 2 |3 4)"
```

##### `level-empty?`

`^{:refer code.query.block/level-empty? :added "3.0" :class [:nav/edit]}`

Checks if the current container has no expression children.

```clojure
(-> (parse-string "( #| )")
      (level-empty?))
;; => true
```

##### `insert-empty`

`^{:refer code.query.block/insert-empty :added "3.0" :class [:nav/edit]}`

Inserts an element into an empty container.

```clojure
(-> (parse-string "( #| )")
      (insert-empty 1)
      str)
;; => "<0,1> (|1  )"
```

##### `insert-right`

`^{:refer code.query.block/insert-right :added "3.0" :class [:nav/edit]}`

Inserts an element to the right of the current cursor position.

```clojure
(-> (parse-string "(#|0)")
      (insert-right 1)
      str)
;; => "<0,1> (|0 1)"

(-> (parse-string "(#|)")
      (insert-right 1)
      str)
;; => "<0,1> (|1)"

(-> (parse-string "( #| )")
      (insert-right 1)
      str)
;; => "<0,1> (|1  )"
```

##### `insert-token-to-left`

`^{:refer code.query.block/insert-token-to-left :added "3.0" :class [:nav/edit]}`

Inserts an element to the left of the current cursor position.

```clojure
(-> (parse-string "(#|0)")
      (insert-token-to-left 1)
      str)
;; => "<0,3> (1 |0)"

(-> (parse-string "(#|)")
      (insert-token-to-left 1)
      str)
;; => "<0,1> (|1)"

(-> (parse-string "( #| )")
      (insert-token-to-left 1)
      str)
;; => "<0,1> (|1  )"
```

##### `insert`

`^{:refer code.query.block/insert :added "3.0" :class [:nav/edit]}`

Inserts an element at the current cursor position and moves the cursor past the inserted element.

```clojure
(-> (parse-string "(#|0)")
      (insert 1)
      str)
;; => "<0,3> (0 |1)"

(-> (parse-string "(#|)")
      (insert-right 1)
      str)
;; => "<0,1> (|1)"

(-> (parse-string "( #| )")
      (insert-right 1)
      str)

;; => "<0,1> (|1  )"
```

##### `insert-all`

`^{:refer code.query.block/insert-all :added "3.0"}`

Inserts all expressions from a collection into the block at the current cursor position.

```clojure
;; No direct test example, but it would involve:
;; (-> (parse-string "(#|)")
;;     (insert-all [1 2 3])
;;     str)
;; => "<0,7> (1 2 3|)"
```

##### `insert-newline`

`^{:refer code.query.block/insert-newline :added "3.0"}`

Inserts one or more newline blocks at the current cursor position.

```clojure
;; No direct test example, but it would involve:
;; (-> (parse-string "(#|)")
;;     (insert-newline)
;;     str)
;; => "<0,1> (|
)"
```

##### `insert-space`

`^{:refer code.query.block/insert-space :added "3.0"}`

Inserts one or more space blocks at the current cursor position.

```clojure
;; No direct test example, but it would involve:
;; (-> (parse-string "(#|)")
;;     (insert-space)
;;     str)
;; => "<0,1> (| \t)"
```

##### `delete-left`

`^{:refer code.query.block/delete-left :added "3.0" :class [:nav/edit]}`

Deletes the element to the left of the current cursor position.

```clojure
(-> (parse-string "(1 2   #|3)")
      (delete-left)
      str)
;; => "<0,3> (1 |3)"

(-> (parse-string "(  #|1 2 3)")
      (delete-left)
      str)
;; => "<0,1> (|1 2 3)"

(-> (parse-string "( #| )")
      (delete-left)
      str)
;; => "<0,1> (|)"
```

##### `delete-right`

`^{:refer code.query.block/delete-right :added "3.0" :class [:nav/edit]}`

Deletes the element to the right of the current cursor position.

```clojure
(-> (parse-string "(  #|1 2 3)")
      (delete-right)
      str)
;; => "<0,3> (  |1 3)"

(-> (parse-string "(1 2   #|3)")
      (delete-right)
      str)
;; => "<0,7> (1 2   |3)"

(-> (parse-string "( #| )")
      (delete-right)
      str)
;; => "<0,1> (|)"
```

##### `delete`

`^{:refer code.query.block/delete :added "3.0" :class [:nav/edit]}`

Deletes the element at the current cursor position.

```clojure
(-> (parse-string "(  #|1   2 3)")
      (delete)
      str)
;; => "<0,3> (  |2 3)"

(-> (parse-string "(1 2   #|3)")
      (delete)
      str)
;; => "<0,7> (1 2   |)"

(-> (parse-string "(  #|    )")
      (delete)
      str)
;; => "<0,1> (|)"
```

##### `backspace`

`^{:refer code.query.block/backspace :added "3.0" :class [:nav/edit]}`

Performs a "backspace" operation, deleting the element to the left of the cursor and moving the cursor.

```clojure
(-> (parse-string "(0  #|1   2 3)")
      (backspace)
      str)
;; => "<0,1> (|0 2 3)"

(-> (parse-string "(  #|1   2 3)")
      (backspace)
      str)
;; => "<0,1> (|2 3)"
```

##### `replace`

`^{:refer code.query.block/replace :added "3.0" :class [:nav/edit]}`

Replaces the element at the current cursor position with new data.

```clojure
(-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (replace :a)
      str)
;; => "<0,4> (0  |:a   2 3)"
```

##### `swap`

`^{:refer code.query.block/swap :added "3.0" :class [:nav/edit]}`

Applies a function to the element at the current cursor position, replacing it with the result.

```clojure
(-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (swap inc)
      str)
;; => "<0,4> (0  |2   2 3)"
```

##### `update-children`

`^{:refer code.query.block/update-children :added "3.0" :class [:nav/edit]}`

Replaces all children of the current container block with a new sequence of children.

```clojure
(-> (update-children (parse-string "[1 2 3]")
                     [(construct/block 4)
                      (construct/space)
                      (construct/block 5)])
    str)
;; => "<0,0> |[4 5]"
```

##### `line-info`

`^{:refer code.query.block/line-info :added "3.0" :class [:nav/general]}`

Returns a map containing line and column information for the current block.

```clojure
(line-info (parse-string "[1 \n  2 3]"))
;; => {:row 1, :col 1, :end-row 2, :end-col 7}
```