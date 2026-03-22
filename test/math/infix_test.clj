(ns math.infix-test
  (:require [math.infix :as in])
  (:use code.test))

^{:refer math.infix/infix-forms :added "3.0"}
(fact "helper function for infix macros")

^{:refer math.infix/= :added "3.0"}
(fact "evaluates the infix expression"

  (in/= 1 + 2 + 3 * 4)
  => 15

  (in/= 1 + (2 + 3) * 4)
  => 21)

^{:refer math.infix/> :added "3.0"}
(fact "creates the top level form"

  (in/> 1 + (2 + 3) * 4)
  => '(+ 1 (* (+ 2 3) 4)))
