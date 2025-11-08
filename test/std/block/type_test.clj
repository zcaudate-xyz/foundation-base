(ns std.block.type-test
  (:use code.test)
  (:require [std.block.type :refer :all]
            [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.parse :as parse]))

^{:refer std.block.type/block-compare :added "3.0"}
(fact "compares equality of two blocks"

  (block-compare (construct/void \space)
                 (construct/void \space))
  => 0)

^{:refer std.block.type/void-block? :added "3.0"}
(fact "checks if the block is a void block"

  (void-block? (construct/void))
  => true)

^{:refer std.block.type/void-block :added "3.0"}
(fact "constructs a void block"

  (-> (void-block :linespace \tab 1 0)
      (base/block-info))
  => {:type :void, :tag :linespace, :string "\t", :height 0, :width 1})

^{:refer std.block.type/space-block? :added "3.0"}
(fact "checks if block is of type \\space"

  (space-block? (construct/space))
  => true)

^{:refer std.block.type/linebreak-block? :added "3.0"}
(fact "checks if block is of type :linebreak"

  (linebreak-block? (construct/newline))
  => true)

^{:refer std.block.type/linespace-block? :added "3.0"}
(fact "checks if block is of type :linespace"

  (linespace-block? (construct/space))
  => true)

^{:refer std.block.type/eof-block? :added "3.0"}
(fact "checks if input is an eof block"

  (eof-block? (construct/void nil))
  => true)

^{:refer std.block.type/nil-void? :added "3.0"}
(fact "checks if block is nil or type void block"

  (nil-void? nil) => true

  (nil-void? (construct/block nil)) => false

  (nil-void? (construct/space)) => true)

^{:refer std.block.type/comment-block? :added "3.0"}
(fact "checks if the block is a token block"

  (comment-block? (construct/comment ";;hello"))
  => true)

^{:refer std.block.type/comment-block :added "3.0"}
(fact "constructs a comment block"

  (-> (comment-block ";hello")
      (base/block-info))
  => {:type :comment, :tag :comment, :string ";hello", :height 0, :width 6})

^{:refer std.block.type/token-block? :added "3.0"}
(fact "checks if the block is a token block"

  (token-block? (construct/token "hello"))
  => true)

^{:refer std.block.type/token-block :added "3.0"}
(fact "creates a token block"

  (base/block-info (token-block :symbol "abc" 'abc "abc" 3 0))
  => {:type :token, :tag :symbol, :string "abc", :height 0, :width 3})

^{:refer std.block.type/container-width :added "3.0"}
(fact "calculates the width of a container"

  (container-width (construct/block [1 2 3 4]))
  => 9)

^{:refer std.block.type/container-height :added "3.0"}
(fact "calculates the height of a container"

  (container-height (construct/block []))
  => 0
  
  (container-height (construct/block [(construct/newline)]))
  => 1
  
  (container-height (construct/block [(construct/newline)
                                      (construct/newline)]))
  => 2)

^{:refer std.block.type/container-string :added "3.0"}
(fact "returns the string for the container"

  (container-string (construct/block [1 2 3]))
  => "[1 2 3]")

^{:refer std.block.type/container-value-string :added "3.0"}
(fact "returns the string for "

  (container-value-string (construct/block [::a :b :c]))
  => "[:std.block.type-test/a :b :c]"

  (container-value-string (parse/parse-string "[::a :b :c]"))
  => "[(keyword \":a\") (keyword \"b\") (keyword \"c\")]")

^{:refer std.block.type/container-block? :added "3.0"}
(fact "checks if block is a container block"

  (container-block? (construct/block []))
  => true)

^{:refer std.block.type/container-block :added "3.0"}
(fact "constructs a container block"

  (-> (container-block :fn [(construct/token '+)
                            (construct/void)
                            (construct/token '1)]
                       (construct/*container-props* :fn))
      (base/block-value))
  => '(fn* [] (+ 1)))

^{:refer std.block.type/modifier-block? :added "3.0"}
(fact "checks if block is a modifier block"

  (modifier-block? (construct/uneval))
  => true)

^{:refer std.block.type/modifier-block :added "3.0"}
(fact "creates a modifier block, specifically #_"

  (modifier-block :hash-uneval "#_" (fn [acc _] acc)))

^{:refer std.block.type/code-block? :added "4.0"}
(fact "TODO")
