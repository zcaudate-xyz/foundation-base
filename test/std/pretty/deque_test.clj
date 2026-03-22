(ns std.pretty.deque-test
  (:require [std.pretty.deque :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [concat]))

^{:refer std.pretty.deque/pop-left :added "3.0"}
(fact "pops an element from the left"

  (pop-left [1 2 3 4])
  => [2 3 4])

^{:refer std.pretty.deque/peek-left :added "3.0"}
(fact "peeks at the first element on the left"

  (peek-left [1 2 3 4])
  => 1)

^{:refer std.pretty.deque/conj-right :added "3.0"}
(fact "appends elements on the right"

  (conj-right [1] 2 3 4)
  => [1 2 3 4])

^{:refer std.pretty.deque/conj-left :added "3.0"}
(fact "appends elements on the left"

  (conj-left [4] 3 2 1)
  => [1 2 3 4])

^{:refer std.pretty.deque/conj-both :added "3.0"}
(fact "appends elements on either side"

  (conj-both 1 [2] 3)
  => [1 2 3])

^{:refer std.pretty.deque/update-left :added "3.0"}
(fact "updates the leftmost element"

  (update-left [1 2 3] dec)
  => [0 2 3])

^{:refer std.pretty.deque/update-right :added "3.0"}
(fact "updates the rightmost element"

  (update-right [1 2 3] inc)
  => [1 2 4])

