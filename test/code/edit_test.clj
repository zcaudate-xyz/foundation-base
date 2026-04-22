(ns std.block.navigate-test
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.navigate :refer :all]
            [std.block.parse :as parse]
            [std.block.type :as type]
            [std.lib.zip :as zip])
  (:use code.test)
  (:refer-clojure :exclude [next replace type]))

^{:refer std.block.navigate/nav-template :added "3.0"}
(fact "generates a navigation function definition from a given symbol and a block tag function"

  (nav-template '-tag- #'std.block.base/block-tag)
  => '(clojure.core/defn -tag-
        ([zip] (-tag- zip :right))
        ([zip step]
         (clojure.core/if-let [elem (std.lib.zip/get zip)]
           (std.block.base/block-tag elem)))))

^{:refer std.block.navigate/left-anchor :added "3.0" :class [:nav/primitive]}
(fact "calculates the length to the last newline"

  (left-anchor (-> (navigator nil)
                   (zip/step-right)))
  => 3)

^{:refer std.block.navigate/update-step-left :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to the left"

  (-> {:position [0 7]}
      (update-step-left (construct/block [1 2 3])))
  => {:position [0 0]})

^{:refer std.block.navigate/update-step-right :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to the right"

  (-> {:position [0 0]}
      (update-step-right (construct/block [1 2 3])))
  => {:position [0 7]})

^{:refer std.block.navigate/update-step-inside :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to within a block"

  (-> {:position [0 0]}
      (update-step-inside (construct/block #{})))
  => {:position [0 2]})

^{:refer std.block.navigate/update-step-inside-left :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to within a block"
  (-> {:position [0 3]}
      (update-step-inside-left (construct/block #{})))
  => {:position [0 2]})

^{:refer std.block.navigate/update-step-outside :added "3.0" :class [:nav/primitive]}
(fact "updates the step position to be outside a block"

  (let [left-elems [(construct/block [1 2 3]) (construct/newline)]]
    (-> {:position [1 0]
         :left left-elems}
        (update-step-outside left-elems)
        :position))
  => [0 7])

^{:refer std.block.navigate/display-navigator :added "3.0" :class [:nav/primitive]}
(fact "displays a string representing the navigator"

  (-> (navigator [1 2 3 4])
      (display-navigator))
  => "<0,0> |[1 2 3 4]")

^{:refer std.block.navigate/navigator :added "3.0" :class [:nav/general]}
(fact "creates a navigator for the block"

  (str (navigator [1 2 3 4]))
  => "<0,0> |[1 2 3 4]")

^{:refer std.block.navigate/navigator? :added "3.0" :class [:nav/general]}
(fact "checks if object is navigator"

  (navigator? (navigator [1 2 3 4]))
  => true)

^{:refer std.block.navigate/from-status :added "3.0" :class [:nav/general]}
(fact "constructs a navigator from a given status"

  (str (from-status (construct/block [1 2 3 (construct/cursor) 4])))
  => "<0,7> [1 2 3 |4]")

^{:refer std.block.navigate/parse-string :added "3.0" :class [:nav/general]}
(fact "creates a navigator from string"

  (str (parse-string "(2   #|   3  )"))
  => "<0,5> (2   |   3  )")

^{:refer std.block.navigate/parse-root :added "3.0" :class [:nav/general]}
(fact "parses the navigator from root string"

  (str (parse-root "a b c"))
  => "<0,0> |a b c")

^{:refer std.block.navigate/parse-first :added "4.0"}
(fact "parses the navigator from the first form"

  (str (parse-first "a b c"))
  => "<0,0> |a"

  (str (parse-first "(+ 1 2 3) (+ 4 5)"))
  => "<0,0> |(+ 1 2 3)")

^{:refer std.block.navigate/parse-root-status :added "3.0" :class [:nav/general]}
(fact "parses string and creates a navigator from status"

  (str (parse-root-status "a b #|c"))
  => "<0,4> a b |c")

^{:refer std.block.navigate/root-string :added "3.0" :class [:nav/general]}
(fact "returns the top level string"

  (root-string (navigator [1 2 3 4]))
  => "[1 2 3 4]")

^{:refer std.block.navigate/left-expression :added "3.0" :class [:nav/general]}
(fact "returns the expression on the left"

  (-> {:left [(construct/newline)
              (construct/block [1 2 3])]}
      (left-expression)
      (base/block-value))
  => [1 2 3])

^{:refer std.block.navigate/left-expressions :added "3.0" :class [:nav/general]}
(fact "returns all expressions on the left"

  (->> {:left [(construct/newline)
               (construct/block :b)
               (construct/space)
               (construct/space)
               (construct/block :a)]}
       (left-expressions)
       (mapv base/block-value))
  => [:a :b])

^{:refer std.block.navigate/right-expression :added "3.0" :class [:nav/general]}
(fact "returns the expression on the right"

  (-> {:right [(construct/newline)
               (construct/block [1 2 3])]}
      (right-expression)
      (base/block-value))
  => [1 2 3])

^{:refer std.block.navigate/right-expressions :added "3.0" :class [:nav/general]}
(fact "returns all expressions on the right"

  (->> {:right [(construct/newline)
                (construct/block :b)
                (construct/space)
                (construct/space)
                (construct/block :a)]}
       (right-expressions)
       (mapv base/block-value))
  => [:b :a])

^{:refer std.block.navigate/top :added "4.0"}
(fact "moves cursor to the top"

  (-> (parse-string "(1  [1 2 3]    #|)")
      (top)
      str)
  => "<0,0> |(1  [1 2 3]    )")

^{:refer std.block.navigate/left :added "3.0" :class [:nav/move]}
(fact "moves to the left expression"

  (-> (parse-string "(1  [1 2 3]    #|)")
      (left)
      str)
  => "<0,4> (1  |[1 2 3]    )")

^{:refer std.block.navigate/left-most :added "3.0" :class [:nav/move]}
(fact "moves to the left-most expression"

  (-> (parse-string "(1  [1 2 3]  3 4   #|)")
      (left-most)
      str)
  => "<0,1> (|1  [1 2 3]  3 4   )")

^{:refer std.block.navigate/left-most? :added "3.0" :class [:nav/move]}
(fact "checks if navigator is at left-most"

  (-> (from-status [1 [(construct/cursor) 2 3]])
      (left-most?))
  => true)

^{:refer std.block.navigate/right :added "3.0" :class [:nav/move]}
(fact "moves to the expression on the right"

  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right)
      str)
  => "<0,10> ([1 2 3]  |3 4  )")

^{:refer std.block.navigate/right-most :added "3.0" :class [:nav/move]}
(fact "moves to the right-most expression"

  (-> (parse-string "(#|[1 2 3]  3 4  ) ")
      (right-most)
      str)
  => "<0,12> ([1 2 3]  3 |4  )")

^{:refer std.block.navigate/right-most? :added "3.0" :class [:nav/move]}
(fact "checks if navigator is at right-most"

  (-> (from-status [1 [2 3 (construct/cursor)]])
      (right-most?))
  => true)

^{:refer std.block.navigate/up :added "3.0" :class [:nav/move]}
(fact "navigates outside of the form"

  (str (up (from-status [1 [2 (construct/cursor) 3]])))
  => "<0,3> [1 |[2 3]]")

^{:refer std.block.navigate/down :added "3.0" :class [:nav/move]}
(fact "navigates into the form"

  (str (down (from-status [1 (construct/cursor) [2 3]])))
  => "<0,4> [1 [|2 3]]")

^{:refer std.block.navigate/right* :added "3.0" :class [:nav/move]}
(fact "navigates to right element, including whitespace"

  (str (right* (from-status [(construct/cursor) 1 2])))
  => "<0,2> [1| 2]")

^{:refer std.block.navigate/left* :added "3.0" :class [:nav/move]}
(fact "navigates to left element, including whitespace"

  (str (left* (from-status [1 (construct/cursor) 2])))
  => "<0,2> [1| 2]")

^{:refer std.block.navigate/block :added "3.0" :class [:nav/general]}
(fact "returns the current block"

  (block (from-status [1 [2 (construct/cursor) 3]]))
  => (construct/block 3))

^{:refer std.block.navigate/prev :added "3.0" :class [:nav/move]}
(fact "moves to the previous expression"

  (-> (parse-string "([1 2 [3]] #|)")
      (prev)
      str)
  => "<0,7> ([1 2 [|3]] )")

^{:refer std.block.navigate/find-prev :added "4.0"}
(fact "finds the previous token or whitespace"

  (-> (parse-string "( \n \n [[3 \n]] #|  )")
      (find-prev type/linebreak-block?)
      (str))
  => "<2,5> ( \n \n [[3 |\n]]   )")

^{:refer std.block.navigate/find-prev-token :added "4.0"}
(fact "finds the previous token"

  (-> (parse-string "( \n \n [[3 \n]] #|  )")
      (find-prev-token 3)
      (str))
  => "<2,3> ( \n \n [[|3 \n]]   )")

^{:refer std.block.navigate/next :added "3.0" :class [:nav/move]}
(fact "moves to the next expression"

  (-> (parse-string "(#|  [[3]]  )")
      (next)
      (next)
      (next)
      str)
  => "<0,5> (  [[|3]]  )")

^{:refer std.block.navigate/find-next :added "4.0"}
(fact "finds the next expression"

  (-> (parse-string "(#|  [[3] @5]  )")
      (find-next (fn [x]
                   (= :deref (base/block-tag x))))
      str)
  => "<0,8> (  [[3] |@5]  )")

^{:refer std.block.navigate/find-next-token :added "3.0" :class [:nav/move]}
(fact "moves to the next token"

  (-> (parse-string "(#|  [[3 2]]  )")
      (find-next-token 2)
      str)
  => "<0,7> (  [[3 |2]]  )")

^{:refer std.block.navigate/prev-anchor :added "3.0" :class [:nav/move]}
(fact "moves to the previous newline"

  (-> (parse-string "( \n \n [[3 \n]] #|  )")
      (prev-anchor)
      (:position))
  => [3 0]

  (-> (parse-string "( #| )")
      (prev-anchor)
      (:position))
  => [0 0])

^{:refer std.block.navigate/next-anchor :added "3.0" :class [:nav/move]}
(fact "moves to the next newline"

  (-> (parse-string "( \n \n#| [[3 \n]]  )")
      (next-anchor)
      (:position))
  => [3 0])

^{:refer std.block.navigate/left-token :added "3.0" :class [:nav/move]}
(fact "moves to the left token"

  (-> (parse-string "(1  {}  #|2 3 4)")
      (left-token)
      str)
  => "<0,1> (|1  {}  2 3 4)")

^{:refer std.block.navigate/left-most-token :added "3.0" :class [:nav/move]}
(fact "moves to the left-most token"

  (-> (parse-string "(1  {}  2 3 #|4)")
      (left-most-token)
      str)
  => "<0,1> (|1  {}  2 3 4)")

^{:refer std.block.navigate/right-token :added "3.0" :class [:nav/move]}
(fact "moves to the right token"

  (-> (parse-string "(#|1  {}  2 3 4)")
      (right-token)
      str)
  => "<0,8> (1  {}  |2 3 4)")

^{:refer std.block.navigate/right-most-token :added "3.0" :class [:nav/move]}
(fact "moves to the right-most token"

  (-> (parse-string "(#|1  {}  2 3 [4])")
      (right-most-token)
      str)
  => "<0,10> (1  {}  2 |3 [4])")

^{:refer std.block.navigate/prev-token :added "3.0" :class [:nav/move]}
(fact "moves to the previous token"

  (-> (parse-string "(1 (2 3 [4])#|)")
      (prev-token)
      str)
  => "<0,9> (1 (2 3 [|4]))")

^{:refer std.block.navigate/next-token :added "3.0" :class [:nav/move]}
(fact "moves to the next token"

  (-> (parse-string "(#|[[1 2 3 4]])")
      (next-token)
      str)
  => "<0,3> ([[|1 2 3 4]])")

^{:refer std.block.navigate/position-left :added "3.0" :class [:nav/move]}
(fact "moves the cursor to left expression"

  (-> (parse-string "( 2   #|   3  )")
      (position-left)
      str)
  => "<0,2> ( |2      3  )"

  (-> (parse-string "(   #|   3  )")
      (position-left)
      str)
  => "<0,1> (|      3  )")

^{:refer std.block.navigate/position-right :added "3.0" :class [:nav/move]}
(fact "moves the cursor the right expression"

  (-> (parse-string "(2   #|    3  )")
      (position-right)
      str)
  => "<0,9> (2       |3  )"

  (-> (parse-string "(2   #|     )")
      (position-right)
      str)
  => "<0,10> (2        |)")

^{:refer std.block.navigate/tighten-left :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on the left"

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

^{:refer std.block.navigate/tighten-right :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on the right"

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

^{:refer std.block.navigate/tighten :added "3.0" :class [:nav/edit]}
(fact "removes extra spaces on both the left and right"

  (-> (parse-string "(1 2      #|3       4)")
      (tighten)
      str)
  => "<0,5> (1 2 |3 4)")

^{:refer std.block.navigate/level-empty? :added "3.0" :class [:nav/edit]}
(fact "checks if current container has expressions"

  (-> (parse-string "( #| )")
      (level-empty?))
  => true)

^{:refer std.block.navigate/insert-raw :added "4.0"}
(fact "inserts a raw element"

  (-> (parse-string "( #| )")
      (insert-raw (construct/space))
      str)
  => "<0,3> (  | )"

  (-> (parse-string "( #| )")
      (insert-raw (construct/space))
      (insert-raw (construct/space))
      (insert-raw (construct/space))
      (insert-raw (construct/space))
      str)
  => "<0,6> (     | )")

^{:refer std.block.navigate/insert-empty :added "3.0" :class [:nav/edit]}
(fact "inserts an element into an empty container"

  (-> (parse-string "( #| )")
      (insert-empty 1)
      str)
  => "<0,1> (|1  )")

^{:refer std.block.navigate/insert-token-to-right :added "3.0" :class [:nav/edit]}
(fact "inserts an element to the right"

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

^{:refer std.block.navigate/insert-token-to-left :added "3.0" :class [:nav/edit]}
(fact "inserts an element to the left"

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

^{:refer std.block.navigate/insert-token :added "4.0"}
(fact "standard insert token"

  (-> (parse-string "(#|0)")
      (insert-token 1)
      str)
  => "<0,3> (0 |1)")

^{:refer std.block.navigate/insert-all :added "3.0"}
(fact "inserts all expressions into the block"

  (-> (parse-string "")
      (insert-all [1 2 3 4 5 6])
      str)
  => "<0,10> 1␣2␣3␣4␣5␣|6:eof")

^{:refer std.block.navigate/insert-newline :added "3.0"}
(fact "insert newline/s into the block"

  (-> (parse-string "(#|0)")
      (insert-newline 2)
      str)
  => "<2,0> (\n\n|0)")

^{:refer std.block.navigate/insert-space :added "3.0"}
(fact "insert space/s into the block"

  (-> (parse-string "(#|0)")
      (insert-space 2)
      str)
  => "<0,3> (  |0)")

^{:refer std.block.navigate/delete-spaces-left :added "4.0"}
(fact "deletes spaces to the left"

  (-> (parse-string "(1 2      #|3       4)")
      (delete-spaces-left)
      str)
  => "<0,4> (1 2|3       4)")

^{:refer std.block.navigate/delete-spaces-right :added "4.0"}
(fact "deletes spaces to the left"

  (-> (parse-string "(1 2      3 #|       4)")
      (delete-spaces-right)
      str)
  => "<0,12> (1 2      3 |4)")

^{:refer std.block.navigate/delete-left :added "3.0" :class [:nav/edit]}
(fact "deletes left of the current expression"

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

^{:refer std.block.navigate/delete-right :added "3.0" :class [:nav/edit]}
(fact "deletes right of the current expression"

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

^{:refer std.block.navigate/delete :added "3.0" :class [:nav/edit]}
(fact "deletes the current element"

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

^{:refer std.block.navigate/backspace :added "3.0" :class [:nav/edit]}
(fact "the reverse of insert"

  (-> (parse-string "(0  #|1   2 3)")
      (backspace)
      str)
  => "<0,1> (|0 2 3)"

  (-> (parse-string "(  #|1   2 3)")
      (backspace)
      str)
  => "<0,1> (|2 3)")

^{:refer std.block.navigate/replace :added "3.0" :class [:nav/edit]}
(fact "replaces an element at the cursor"

  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (replace :a)
      str)
  => "<0,4> (0  |:a   2 3)")

^{:refer std.block.navigate/replace-splice :added "3.0" :class [:nav/edit]}
(fact "replaces an element at the cursor"

  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (replace-splice [:a :b :c])
      str)
  => "<0,4> (0  |:a :b :c   2 3)")

^{:refer std.block.navigate/swap :added "3.0" :class [:nav/edit]}
(fact "applies a function to the element at the current cursor position, replacing it with the result"

  (-> (parse-string "(0  #|1   2 3)")
      (position-right)
      (swap inc)
      str)
  => "<0,4> (0  |2   2 3)")

^{:refer std.block.navigate/update-children :added "3.0" :class [:nav/edit]}
(fact "replaces the current children"

  (-> (update-children (parse-string "[1 2 3]")
                       [(construct/block 4)
                        (construct/space)
                        (construct/block 5)])
      str)
  => "<0,0> |[4 5]")

^{:refer std.block.navigate/line-info :added "3.0" :class [:nav/general]}
(fact "returns the line info for the current block"

  (line-info (parse-string "[1 \\n  2 3]"))
  => {:row 1, :col 1, :end-row 1, :end-col 12}

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
