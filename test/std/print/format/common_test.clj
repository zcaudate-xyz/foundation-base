(ns std.print.format.common-test
  (:require [std.concurrent.print :as print]
            [std.print.format.common :refer :all])
  (:use code.test))

^{:refer std.print.format.common/pad :added "3.0"}
(fact "creates `n` number of spaces"

  (pad 1) => " "
  (pad 5) => "     ")

^{:refer std.print.format.common/pad:right :added "3.0"}
(fact "puts the content to the left, padding missing spaces"

  (pad:right "hello" 10)
  => "hello     ")

^{:refer std.print.format.common/pad:center :added "3.0"}
(fact "puts the content at the center, padding missing spacing"

  (pad:center "hello" 10)
  => "  hello   ")

^{:refer std.print.format.common/pad:left :added "3.0"}
(fact "puts the content to the right, padding missing spaces"

  (pad:left "hello" 10)
  => "     hello")

^{:refer std.print.format.common/justify :added "3.0"}
(fact "justifies the content to a given alignment"

  (justify :right "hello" 10)
  => "     hello"

  (justify :left "hello" 10)
  => "hello     ")

^{:refer std.print.format.common/indent :added "3.0"}
(fact "format lines with indent"

  (indent ["hello"] 3)
  => ["   hello"])

^{:refer std.print.format.common/pad:lines :added "3.0"}
(fact "creates new lines of n spaces"
  (pad:lines [] 10 2)
  => ["          " "          "])

^{:refer std.print.format.common/border :added "3.0"}
(fact "formats a border around given lines"

  (border ["HELLO"])
  => ["┌-----┐"
      "│HELLO│"
      "└-----┘"]

  (-> ["HELLO"]
      (border  nil +style:pad+)
      (border  nil +style:pad+))
  => ["         "
      "         "
      "  HELLO  "
      "         "
      "         "])
