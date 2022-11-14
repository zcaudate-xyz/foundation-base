(ns std.pretty.color-test
  (:use code.test)
  (:require [std.pretty.color :refer :all]
            [std.pretty :as printer]))

^{:refer std.pretty.color/-document :added "3.0"}
(fact "Constructs a pretty print document, which may be colored if `:print-color` is true."

  (-document printer/+defaults+ :string "hello there")
  => [:span [:pass "[36m"] "hello there" [:pass "[0m"]]

  (-document printer/+defaults+ :keyword :something)
  => [:span [:pass "[34m"] :something [:pass "[0m"]])

^{:refer std.pretty.color/-text :added "3.0"}
(fact "Produces text colored according to the active color scheme. This is mostly
  useful to clients which want to produce output which matches data printed by
  Puget, but which is not directly printed by the library. Note that this
  function still obeys the `:print-color` option."

  (-text printer/+defaults+ :string "hello there")
  => "[36mhello there[0m"

  (-text printer/+defaults+ :keyword :hello)
  => "[34m:hello[0m")
