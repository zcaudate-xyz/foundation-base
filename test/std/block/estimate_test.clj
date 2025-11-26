(ns std.block.layout.estimate-test
  (:use code.test)
  (:require [std.block.layout.estimate :as est]))

^{:refer std.block.layout.estimate/get-max-width :added "4.0"}
(fact "gets the max width of whole form"
  ^:hidden
  
  (est/get-max-width [:a :b :c])
  => 10

  (est/get-max-width [:a :b :c :d])
  => 13)

^{:refer std.block.layout.estimate/get-max-width-children :added "4.0"}
(fact "gets the max with of the children"
  ^:hidden

  (est/get-max-width-children [:a :b :c])
  => 8

  (est/get-max-width-children [:a :b :c :d])
  => 11)

^{:refer std.block.layout.estimate/estimate-multiline-basic :added "4.0"}
(fact "does basic estimation"

  (est/estimate-multiline-basic [1 2 3] {:readable-len 10})
  => false

  (est/estimate-multiline-basic [1 2 3 4 5 6 7 8 9 10 11] {:readable-len 10})
  => true)

^{:refer std.block.layout.estimate/estimate-multiline-data :added "4.0"}
(fact "estimation for maps and sets"

  (est/estimate-multiline-data {:a 1} {:readable-len 10})
  => false

  (est/estimate-multiline-data #{1 2 3} {:readable-len 10})
  => false)

^{:refer std.block.layout.estimate/estimate-multiline-vector :added "4.0"}
(fact "estimation for vectors"

  (est/estimate-multiline-vector [1 2 3] {:readable-len 10})
  => false)

^{:refer std.block.layout.estimate/estimate-multiline-list :added "4.0"}
(fact "estimates if special forms are multilined"
  ^:hidden
  
  (est/estimate-multiline-list '(let [] a)
                               {:readable-len 30})
  => false
  
  (est/estimate-multiline-list '(let [a 1] a)
                               {:readable-len 10})
  => true)

^{:refer std.block.layout.estimate/estimate-multiline :added "4.0"}
(fact "creates multiline function"
  ^:hidden

  ;; check for functions
  (est/estimate-multiline '(a-function that does this)
                          {:readable-len 30})
  => false
  
  (est/estimate-multiline '(a-really-long funtion with lots of parameters)
                          {:readable-len 30})
  => true


  ;; check for let
  (est/estimate-multiline '(let [a 1] a)
                          {:readable-len 30})
  => false)