(ns code.doc.link.number-test
  (:use code.test)
  (:require [code.doc.link.number :refer :all]))

^{:refer code.doc.link.number/increment :added "3.0"}
(fact "increment string representation"

  (increment 1)
  = "A"

  (increment "1")
  => "2")

^{:refer code.doc.link.number/link-numbers-loop :added "3.0"}
(fact "iterates and assigns sequential numbers to elements, used as a helper for `link-numbers`")

^{:refer code.doc.link.number/link-numbers :added "3.0"}
(fact "creates numbers for each of the elements in the list")
