(ns std.block.base-test
  (:use code.test)
  (:require [std.block.base :refer :all]
            [std.block.construct :as construct]
            [std.block.parse :as parse]))

^{:refer std.block.base/block? :added "3.0"}
(fact "check whether an object is a block"

  (block? (construct/void nil))
  => true

  (block? (construct/token "hello"))
  => true)

^{:refer std.block.base/block-type :added "3.0"}
(fact "returns block type as keyword"

  (block-type (construct/void nil))
  => :void

  (block-type (construct/token "hello"))
  => :token)

^{:refer std.block.base/block-tag :added "3.0"}
(fact "returns block tag as keyword"

  (block-tag (construct/void nil))
  => :eof

  (block-tag (construct/void \space))
  => :linespace)

^{:refer std.block.base/block-string :added "3.0"}
(fact "returns block string as representated in the file"

  (block-string (construct/token 3/4))
  => "3/4"

  (block-string (construct/void \space))
  => " ")

^{:refer std.block.base/block-length :added "3.0"}
(fact "return the length of the string"

  (block-length (construct/void))
  => 1

  (block-length (construct/block [1 2 3 4]))
  => 9)

^{:refer std.block.base/block-width :added "3.0"}
(fact "returns the width of a block"

  (block-width (construct/token 'hello))
  => 5)

^{:refer std.block.base/block-height :added "3.0"}
(fact "returns the height of a block"

  (block-height (construct/block
                 ^:list [(construct/newline)
                         (construct/newline)]))
  => 2)

^{:refer std.block.base/block-prefixed :added "3.0"}
(fact "returns the length of the starting characters"

  (block-prefixed (construct/block #{}))
  => 2)

^{:refer std.block.base/block-suffixed :added "3.0"}
(fact "returns the length of the ending characters"

  (block-suffixed (construct/block #{}))
  => 1)

^{:refer std.block.base/block-verify :added "3.0"}
(fact "checks that the block has correct data"

  (block-verify (construct/token "hello"))
  => true)

^{:refer std.block.base/expression? :added "3.0"}
(fact "checks if the block has a value associated with it"

  (expression? (construct/token 1.2))
  => true)

^{:refer std.block.base/block-value :added "3.0"}
(fact "returns the value of the block"

  (block-value (construct/token 1.2))
  => 1.2)

^{:refer std.block.base/block-value-string :added "3.0"}
(fact "returns the string for which a value will be generated"

  (block-value-string (parse/parse-string "#(+ 1 ::2)"))
  => "#(+ 1 (keyword \":2\"))")

^{:refer std.block.base/modifier? :added "3.0"}
(fact "checks if the block is of type INodeModifier"

  (modifier? (construct/uneval))
  => true)

^{:refer std.block.base/block-modify :added "3.0"}
(fact "allows the block to modify an accumulator"

  (block-modify (construct/uneval) [1 2] 'ANYTHING)
  => [1 2])

^{:refer std.block.base/container? :added "3.0"}
(fact "determines whether a block has children"

  (container? (parse/parse-string "[1 2 3]"))
  => true

  (container? (parse/parse-string " "))
  => false)

^{:refer std.block.base/block-children :added "3.0"}
(fact "returns the children in the block"

  (->> (block-children (parse/parse-string "[1   2]"))
       (apply str))
  => "1␣␣␣2")

^{:refer std.block.base/replace-children :added "3.0"}
(fact "replace childer"

  (->> (replace-children (construct/block [])
                         (conj (vec (block-children (construct/block [1 2])))
                               (construct/void \space)
                               (construct/block [3 4])))
       str)
  => "[1 2 [3 4]]")

^{:refer std.block.base/block-info :added "3.0"}
(fact "returns the data associated with the block"

  (block-info (construct/token true))
  => {:type :token, :tag :boolean, :string "true", :height 0, :width 4}
  => {:type :token, :tag :boolean, :string "true"}

  (block-info (construct/void \tab))
  => {:type :void, :tag :linetab, :string "\t", :height 0, :width 4})
