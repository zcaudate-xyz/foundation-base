(ns math.parse-test
  (:require [math.parse :refer :all])
  (:use code.test))

^{:refer math.parse/parse-expr :added "3.0"}
(fact "parses string into SyntaxTree"

  (parse-expr "1 + 1")
  => org.scijava.parsington.SyntaxTree)

^{:refer math.parse/to-clj :added "3.0"}
(fact "converts syntax tree into clojure"

  (to-clj (parse-expr "1+1"))
  => '({:op "+"} 1 1)

  (to-clj (parse-expr "sin((1+1))"))
  => '(sin (:infix/splice ({:op "+"} 1 1))))

^{:refer math.parse/walk-postfix :added "3.0"}
(fact "fixes splice forms occuring in parsing"

  (walk-postfix '(sin (:infix/splice ({:op "+"} 1 1))))
  => '(sin ({:op "+"} 1 1)))

^{:refer math.parse/parse :added "3.0"}
(fact "parses string into clojure data structure"

  (parse "sin((1+1))")
  => '(sin ({:op "+"} 1 1))

  (parse "a+b*c^f(1,2)")
  => '({:op "+"} a ({:op "*"} b ({:op "^"} c (f 1 2))))

  (parse "a+b+c+d")
  => '({:op "+"} ({:op "+"} ({:op "+"} a b) c) d))
