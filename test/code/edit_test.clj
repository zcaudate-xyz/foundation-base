(ns code.edit-test
  (:use code.test)
  (:require [code.edit :refer :all]
            [std.block.type :as type]
            [std.block.construct :as construct]
            [std.block.base :as base]
            [std.block.parse :as parse]
            [std.lib.zip :as zip])
  (:refer-clojure :exclude [next replace type]))

^{:refer code.edit/nav-template :added "3.0"}
(fact "generates a navigation function definition from a given symbol and a block tag function"
  ^:hidden

  (nav-template '-tag- #'std.block.base/block-tag)
  => '(clojure.core/defn -tag-
        ([zip] (-tag- zip :right))
        ([zip step]
         (clojure.core/if-let [elem (std.lib.zip/get zip)]
           (std.block.base/block-tag elem)))))

^{:refer code.edit/left-anchor :added "3.0" :class [:nav/primitive]}
(fact "calculates the length to the last newline"
  ^:hidden
  
  (left-anchor (-> (navigator nil)
                   (zip/step-right)))
  => 3)

^{:refer code.edit/update-step-left :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to the left"
  ^:hidden
  
  (-> {:position [0 7]}
      (update-step-left (construct/block [1 2 3])))
  => {:position [0 0]})

^{:refer code.edit/update-step-right :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to the right"
  ^:hidden
  
  (-> {:position [0 0]}
      (update-step-right (construct/block [1 2 3])))
  => {:position [0 7]})

^{:refer code.edit/update-step-inside :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to within a block"
  ^:hidden
  
  (-> {:position [0 0]}
      (update-step-inside (construct/block #{})))
  => {:position [0 2]})

^{:refer code.edit/update-step-inside-left :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to within a block"
  (-> {:position [0 3]}
      (update-step-inside-left (construct/block #{})))
  => {:position [0 2]})

^{:refer code.edit/update-step-outside :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to be outside a block"
  ^:hidden
  
  (let [left-elems [(construct/block [1 2 3]) (construct/newline)]]
    (-> {:position [1 0]
         :left left-elems}
        (update-step-outside left-elems)
        :position))
  => [0 7])

^{:refer code.edit/display-navigator :added "3.0" :class [:nav/primitive]}
(fact "displays a string representing the navigator"
  ^:hidden
  
  (-> (navigator [1 2 3 4])
      (display-navigator))
  => "<0,0> |[1 2 3 4]")

^{:refer code.edit/navigator :added "3.0" :class [:nav/general]}
(fact "creates a navigator for the block"
  ^:hidden
  
  (str (navigator [1 2 3 4]))
  => "<0,0> |[1 2 3 4]")

^{:refer code.edit/navigator? :added "3.0" :class [:nav/general]}
(fact "checks if object is navigator"
  ^:hidden
  
  (navigator? (navigator [1 2 3 4]))
  => true)

^{:refer code.edit/from-status :added "3.0" :class [:nav/general]}
(fact "constructs a navigator from a given status"
  ^:hidden
  
  (str (from-status (construct/block [1 2 3 (construct/cursor) 4])))
  => "<0,7> [1 2 3 |4]")

^{:refer code.edit/parse-string :added "3.0" :class [:nav/general]}
(fact "creates a navigator from string"
  ^:hidden
  
  (str (parse-string "(2   #|   3  )"))
  => "<0,5> (2   |   3  )")

^{:refer code.edit/parse-root :added "3.0" :class [:nav/general]}
(fact "parses the navigator from root string"
  ^:hidden
  
  (str (parse-root "a b c"))
  => "<0,0> |a b c")

^{:refer code.edit/parse-root-status :added "3.0" :class [:nav/general]}
(fact "parses string and creates a navigator from status"
  ^:hidden
  
  (str (parse-root-status "a b #|c"))
  => "<0,6> a b |c")

^{:refer code.edit/root-string :added "3.0" :class [:nav/general]}
(fact "returns the top level string"
  ^:hidden
  
  (root-string (navigator [1 2 3 4]))
  => "[1 2 3 4]")

^{:refer code.edit/left-expression :added "3.0" :class [:nav/general]}
(fact "returns the expression on the left"
  ^:hidden
  
  (-> {:left [(construct/newline)
              (construct/block [1 2 3])]}
      (left-expression)
      (base/block-value))
  => [1 2 3])

^{:refer code.edit/left-expressions :added "3.0" :class [:nav/general]}
(fact "returns all expressions on the left"
  ^:hidden
  
  (->> {:left [(construct/newline)
               (construct/block :b)
               (construct/space)
               (construct/space)
               (construct/block :a)]}
       (left-expressions)
       (mapv base/block-value))
  => [:a :b])

^{:refer code.edit/right-expression :added "3.0" :class [:nav/general]}
(fact "returns the expression on the right"
  ^:hidden
  
  (-> {:right [(construct/newline)
               (construct/block [1 2 3])]}
      (right-expression)
      (base/block-value))
  => [1 2 3])

^{:refer code.edit/right-expressions :added "3.0" :class [:nav/general]}
(fact "returns all expressions on the right"
  ^:hidden
  
  (->> {:right [(construct/newline)
                (construct/block :b)
                (construct/space)
                (construct/space)
                (construct/block :a)]}
       (right-expressions)
       (mapv base/block-value))
  => [:b :a])

^{:refer code.edit/left :added "3.0" :class [:nav/move]}
(fact "moves to the left expression"
  ^:hidden
  
  (-> (parse-string "(1  [1 2 3]    #|)")
      (left)
      str)
  => "<0,4> (1  |[1 2 3]    )")

^{:refer code.edit/left-most :added "3.0" :class [:nav/move]}
(fact "moves to the left-most expression"
  ^:hidden
  
  (-> (parse-string "(1  [1 2 3]  3 4   #|)")
      (left-most)
      str)
  => "<0,1> (|1  [1 2 3]  3 4   )")

^{:refer code.edit/left-most? :added "3.0" :class [:nav/move]}
(fact "checks if navigator is at left-most"
  ^:hidden
  
  (-> (from-status [1 [(construct/cursor) 2 3]])
      (left-most?))
  => true)

^{:refer code.edit/right :added "3.0" :class [:nav/move]}
(fact "moves to the expression on the right"
  ^:hidden
  
  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right)
      str)
  => "<0,10> ([1 2 3]  |3 4  )")

^{:refer code.edit/right-most :added "3.0" :class [:nav/move]}
(fact "moves to the right-most expression"
   ^:hidden
 
  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right-most)
      str)
  => "<0,12> ([1 2 3]  3 |4  )")

^{:refer code.edit/right-most? :added "3.0" :class [:nav/move]}
(fact "checks if navigator is at right-most"
  ^:hidden
  
  (-> (from-status [1 [2 3 (construct/cursor)]])
      (right-most?))
  => true)

^{:refer code.edit/up :added "3.0" :class [:nav/move]}
(fact "navigates outside of the form"
  ^:hidden
  
  (str (up (from-status [1 [2 (construct/cursor) 3]])))
  => "<0,3> [1 |[2 3]]")

^{:refer code.edit/down :added "3.0" :class [:nav/move]}
(fact "navigates into the form"
  ^:hidden
  
  (str (down (from-status [1 (construct/cursor) [2 3]])))
  => "<0,4> [1 [|2 3]]")

^{:refer code.edit/right* :added "3.0" :class [:nav/move]}
(fact "navigates to right element, including whitespace"
  ^:hidden
  
  (str (right* (from-status [(construct/cursor) 1 2])))
  => "<0,2> [1| 2]")

^{:refer code.edit/left* :added "3.0" :class [:nav/move]}
(fact "navigates to left element, including whitespace"
  ^:hidden
  
  (str (left* (from-status [1 (construct/cursor) 2])))
  => "<0,2> [1| 2]")

^{:refer code.edit/block :added "3.0" :class [:nav/general]}
(fact "returns the current block"
  ^:hidden
  
  (block (from-status [1 [2 (construct/cursor) 3]]))
  => (construct/block 3))

^{:refer code.edit/prev :added "3.0" :class [:nav/move]}
(fact "moves to the previous expression"
  ^:hidden
  
  (-> (parse-string "([1 2 [3]] #|)")
      (prev)
      str)
  => "<0,7> ([1 2 [|3]] )")

^{:refer code.edit/next :added "3.0" :class [:nav/move]}
(fact "moves to the next expression"
  ^:hidden
  
  (-> (parse-string "(#|  [[3]]  )")
      (next)
      (next)
      (next)
      str)
  => "<0,5> (  [[|3]]  )")

^{:refer code.edit/find-next-token :added "3.0" :class [:nav/move]}
(fact "moves to the next token"
  ^:hidden
  
  (-> (parse-string "(#|  [[3 2]]  )")
      (find-next-token 2)
      str)
  => "<0,7> (  [[3 |2]]  )")

^{:refer code.edit/prev-anchor :added "3.0" :class [:nav/move]}
(fact "moves to the previous newline"
  ^:hidden
  
  (-> (parse-string "( \n \n [[3 \n]] #|  )")
      (prev-anchor)
      (:position))
  => [3 0]

  (-> (parse-string "( #| )")
      (prev-anchor)
      (:position))
  => [0 0])

^{:refer code.edit/next-anchor :added "3.0" :class [:nav/move]}
(fact "moves to the next newline"
  ^:hidden
  
  (-> (parse-string "( \n \n#| [[3 \n]]  )")
      (next-anchor)
      (:position))
  => [3 0])

^{:refer code.edit/left-token :added "3.0" :class [:nav/move]}
(fact "moves to the left token"
  ^:hidden
  
  (-> (parse-string "(1  {}  #|2 3 4)")
      (left-token)
      str)
  => "<0,1> (|1  {}  2 3 4)")

^{:refer code.edit/left-most-token :added "3.0" :class [:nav/move]}
(fact "moves to the left-most token"
  ^:hidden
  
  (-> (parse-string "(1  {}  2 3 #|4)")
      (left-most-token)
      str)
  "<0,10> (1  {}  2 |3 4)")

^{:refer code.edit/right-token :added "3.0" :class [:nav/move]}
(fact "moves to the right token"
  ^:hidden
  
  (-> (parse-string "(#|1  {}  2 3 4)")
      (right-token)
      str)
  => "<0,8> (1  {}  |2 3 4)")

^{:refer code.edit/right-most-token :added "3.0" :class [:nav/move]}
(fact "moves to the right-most token"
  ^:hidden
  
  (-> (parse-string "(#|1  {}  2 3 [4])")
      (right-most-token)
      str)
  => "<0,10> (1  {}  2 |3 [4])")

^{:refer code.edit/prev-token :added "3.0" :class [:nav/move]}
(fact "moves to the previous token"
  ^:hidden
  
  (-> (parse-string "(1 (2 3 [4])#|)")
      (prev-token)
      str)
  => "<0,9> (1 (2 3 [|4]))")

^{:refer code.edit/next-token :added "3.0" :class [:nav/move]}
(fact "moves to the next token"
  ^:hidden
  
  (-> (parse-string "(#|[[1 2 3 4]])")
      (next-token)
      str)
  => "<0,3> ([[|1 2 3 4]])")

^{:refer code.edit/position-left :added "3.0" :class [:nav/move]}
(fact "moves the cursor to left expression"
  ^:hidden
  
  (-> (parse-string "( 2   #|   3  )")
      (position-left)
      str)
  => "<0,2> ( |2      3  )"

  (-> (parse-string "(   #|   3  )")
      (position-left)
      str)
  => "<0,1> (|      3  )")

^{:refer code.edit/position-right :added "3.0" :class [:nav/move]}
(fact "moves the cursor the right expression"
  ^:hidden
  
  (-> (parse-string "(2   #|    3  )")
      (position-right)
      str)
  => "<0,9> (2       |3  )"

  (-> (parse-string "(2   #|     )")
      (position-right)
      str)
  => "<0,10> (2        |)")

^{:refer code.edit/tighten-left :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on the left"
  ^:hidden
  
  (-> (parse-string "(1 2 3   #|4)")
      (tighten-left)
      str)
  => "<0,7> (1 2 3 |4)"

  (-> (parse-string "(1 2 3   #|    4)")
      (tighten-left)
      str)
  => "<0,7> (1 2 3 |4)"

  (-> (parse-string "(    #|     )")
      (tighten-left)
      str)
  => "<0,1> (|)")

^{:refer code.edit/tighten-right :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on the right"
  ^:hidden
  
  (-> (parse-string "(1 2 #|3       4)")
      (tighten-right)
      str)
  => "<0,5> (1 2 |3 4)"

  (-> (parse-string "(1 2 3   #|    4)")
      (tighten-right)
      str)
  => "<0,5> (1 2 |3 4)"

  (-> (parse-string "(    #|     )")
      (tighten-right)
      str)
  => "<0,1> (|)")

^{:refer code.edit/tighten :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on both the left and right"
  ^:hidden
  
  (-> (parse-string "(1 2      #|3       4)")
      (tighten)
      str)
  => "<0,5> (1 2 |3 4)")

^{:refer code.edit/level-empty? :added "3.0" :class [:nav/edit]}
(fact "checks if current container has expressions"
  ^:hidden
  
  (-> (parse-string "( #| )")
      (level-empty?))
  => true)

^{:refer code.edit/insert-empty :added "3.0" :class [:nav/edit]}
(fact "inserts an element into an empty container"
  ^:hidden
  
  (-> (parse-string "( #| )")
      (insert-empty 1)
      str)
  => "<0,1> (|1  )")

^{:refer code.edit/insert-token-to-right :added "3.0" :class [:nav/edit]}
(fact "inserts an element to the right"
  ^:hidden
  
  (-> (parse-string "(#|0)")
      (insert-token-to-right 1)
      str)
  => "<0,1> (|0 1)"

  (-> (parse-string "(#|)")
      (insert-token-to-right 1)
      str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
      (insert-token-to-right 1)
      str)
  => "<0,1> (|1  )")

^{:refer code.edit/insert-token-to-left :added "3.0" :class [:nav/edit]}
(fact "inserts an element to the left"
  ^:hidden
  
  (-> (parse-string "(#|0)")
      (insert-token-to-left 1)
      str)
  => "<0,3> (1 |0)"

  (-> (parse-string "(#|)")
      (insert-token-to-left 1)
      str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
      (insert-token-to-left 1)
      str)
  => "<0,1> (|1  )")

^{:refer code.edit/insert :added "3.0" :class [:nav/edit]}
(fact "inserts an element at the current cursor position and moves the cursor past the inserted element"
  ^:hidden
  
  (-> (parse-string "(#|0)")
      (insert 1)
      str)
  => "<0,3> (0 |1)"

  (-> (parse-string "(#|)")
      (insert-token-to-right 1)
      str)
  => "<0,1> (|1)"

  (-> (parse-string "( #| )")
      (insert-token-to-right 1)
      str)
  => "<0,1> (|1  )")

^{:refer code.edit/insert-all :added "3.0"}
(fact "inserts all expressions into the block"
  ^:hidden
  
  (-> (parse-string "")
      (insert-all [1 2 3 4 5 6])))

^{:refer code.edit/insert-newline :added "3.0"}
(fact "insert newline/s into the block")

^{:refer code.edit/insert-space :added "3.0"}
(fact "insert space/s into the block")

^{:refer code.edit/delete-left :added "3.0" :class [:nav/edit]}
(fact "deletes left of the current expression"
  ^:hidden
  
  (-> (parse-string "(1 2   #|3)")
      (delete-left)
      str)
  => "<0,3> (1 |3)"

  (-> (parse-string "(  #|1 2 3)")
      (delete-left)
      str)
  => "<0,1> (|1 2 3)"

  (-> (parse-string "(#|1   )")
      (delete-left)
      str)

  (-> (parse-string "( 1 #|  )")
      (delete-left)
      str)

  (-> (parse-string "( #| )")
      (delete-left)
      str)
  => "<0,1> (|)")

^{:refer code.edit/delete-right :added "3.0" :class [:nav/edit]}
(fact "deletes right of the current expression"
  ^:hidden
  
  (-> (parse-string "(  #|1 2 3)")
      (delete-right)
      str)
  => "<0,3> (  |1 3)"

  (-> (parse-string "(1 2   #|3)")
      (delete-right)
      str)
  => "<0,7> (1 2   |3)"

  (-> (parse-string "(  #|   1  )")
      (delete-right)
      str)
  => "<0,1> (|  )"

  (-> (parse-string "( #| )")
      (delete-right)
      str)
  => "<0,1> (|)")

^{:refer code.edit/delete :added "3.0" :class [:nav/edit]}
(fact "deletes the current element"
  ^:hidden
  
  (-> (parse-string "(  #|1   2 3)")
      (delete)
      str)
  => "<0,3> (  |2 3)"

  (-> (parse-string "(1 2   #|3)")
      (delete)
      str)
  => "<0,7> (1 2   |)"

  (-> (parse-string "(  #|    )")
      (delete)
      str)
  => "<0,1> (|)")

^{:refer code.edit/backspace :added "3.0" :class [:nav/edit]}
(fact "the reverse of insert"
  ^:hidden
  
  (-> (parse-string "(0  #|1   2 3)")
      (backspace)
      str)
  => "<0,1> (|0 2 3)" ^:hidden

  (-> (parse-string "(  #|1   2 3)")
      (backspace)
      str)
  => "<0,1> (|2 3)")

^{:refer code.edit/replace :added "3.0" :class [:nav/edit]}
(fact "replaces an element at the cursor"
  ^:hidden
  
  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (replace :a)
      str)
  => "<0,4> (0  |:a   2 3)")

^{:refer code.edit/replace-splice :added "3.0" :class [:nav/edit]}
(fact "replaces an element at the cursor"
  ^:hidden
  
  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (replace-splice [:a :b :c])
      str)
  => "<0,4> (0  |:a :b :c   2 3)")

^{:refer code.edit/swap :added "3.0" :class [:nav/edit]}
(fact "applies a function to the element at the current cursor position, replacing it with the result"
  ^:hidden
  
  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (swap inc)
      str)
  => "<0,4> (0  |2   2 3)")

^{:refer code.edit/update-children :added "3.0" :class [:nav/edit]}
(fact "replaces the current children"
  ^:hidden
  
  (-> (update-children (parse-string "[1 2 3]")
                       [(construct/block 4)
                        (construct/space)
                        (construct/block 5)])
      str)
  => "<0,0> |[4 5]")

^{:refer code.edit/line-info :added "3.0" :class [:nav/general]}
(fact "returns the line info for the current block"
  ^:hidden
  
  (line-info (parse-string "[1 \n  2 3]"))
  => {:row 1, :col 1, :end-row 2, :end-col 7})

(comment

  (zip/from-status
   (parse/parse-string "(1  [1 2 3] #|)")
   navigator)

  (get (code.project/all-files ["src"])
       'std.block.type)
  (get (code.project/all-files ["test"])
       'std.block.type-test)

  (./import)
  (./incomplete '[code.manage])
  (./reset '[std.block])
  (./scaffold '[std.block])
  (./arrange))
