(ns code.test.compile-nested-test
  (:use code.test))

(fact "nested assertions in let bindings work correctly"
  ^:hidden
  
  ;; Simple let with nested assertion
  (let [a 1]
    a => 1
    (+ a 1) => 2)
  
  ;; Nested lets
  (let [x 2]
    (let [y 3]
      (+ x y) => 5))
  
  ;; Multiple bindings
  (let [a 1
        b 2
        c 3]
    (+ a b) => 3
    (+ b c) => 5
    (+ a b c) => 6))

(fact "nested assertions in do blocks work correctly"
  ^:hidden
  
  ;; Simple do with assertion
  (do
    (+ 1 1) => 2)
  
  ;; Multiple assertions in do
  (do
    1 => 1
    2 => 2
    (+ 1 2) => 3))

(fact "nested assertions in complex structures"
  ^:hidden
  
  ;; Inside if
  (if true
    (do
      (+ 1 1) => 2)
    false)
  
  ;; Inside when
  (when true
    (+ 2 2) => 4))

(fact "assertions work at different nesting levels"
  ^:hidden
  
  ;; Top level
  1 => 1
  
  ;; One level deep
  (let [a 1]
    a => 1)
  
  ;; Two levels deep
  (let [a 1]
    (let [b 2]
      (+ a b) => 3)))
