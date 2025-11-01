(ns std.block.check-test
  (:use code.test)
  (:require [std.block.check :refer :all]))

^{:refer std.block.check/boundary? :added "3.0"}
(fact "returns whether a char is of a boundary type"

  (boundary? (first "[")) => true

  (boundary? (first "\"")) => true)

^{:refer std.block.check/whitespace? :added "3.0"}
(fact "returns whether a char is of a whitespace type"

  (whitespace? \space) => true)

^{:refer std.block.check/comma? :added "3.0"}
(fact "returns whether a char is a comma"

  (comma? (first ",")) => true)

^{:refer std.block.check/linebreak? :added "3.0"}
(fact "returns whether a char is a linebreak type"

  (linebreak? \newline) => true)

^{:refer std.block.check/delimiter? :added "3.0"}
(fact "check for delimiter char"

  (delimiter? (first ")")) => true)

^{:refer std.block.check/voidspace? :added "3.0"}
(fact "determines if an input is a void input"

  (voidspace? \newline)
  => true)

^{:refer std.block.check/linetab? :added "3.0"}
(fact "checs if character is a tab"

  (linetab? (first "\t"))
  => true)

^{:refer std.block.check/linespace? :added "3.0"}
(fact "returs whether a char is a linespace type"

  (linespace? \space) => true)

^{:refer std.block.check/voidspace-or-boundary? :added "3.0"}
(fact "check for void or boundary type"

  (->> (map voidspace-or-boundary? (concat *boundaries*
                                           *linebreaks*))
       (every? true?))
  => true)

^{:refer std.block.check/tag :added "3.0"}
(fact "takes a set of checks and returns the key"

  (tag *void-checks* \space)
  => :linespace

  (tag *collection-checks* [])
  => :vector)

^{:refer std.block.check/void-tag :added "3.0"}
(fact "returns the tag associated with input"

  (void-tag \newline)
  => :linebreak)

^{:refer std.block.check/void? :added "3.0"}
(fact "determines if an input is a void input"

  (void? \newline)
  => true)

^{:refer std.block.check/token-tag :added "3.0"}
(fact "returns the tag associated with the input"

  (token-tag 'hello)
  => :symbol)

^{:refer std.block.check/token? :added "3.0"}
(fact "determines if an input is a token"

  (token? 3/4)
  => true)

^{:refer std.block.check/collection-tag :added "3.0"}
(fact "returns th tag associated with the input"

  (collection-tag [])
  => :vector)

^{:refer std.block.check/collection? :added "3.0"}
(fact "determines if an input is a token"

  (collection? {})
  => true)

^{:refer std.block.check/comment? :added "3.0"}
(fact "determines if an input is a comment string"

  (comment? "hello")
  => false

  (comment? ";hello")
  => true)
