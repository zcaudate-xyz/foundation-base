(ns code.layout.estimate-test
  (:use code.test)
  (:require [code.layout.estimate :as est]))

^{:refer code.layout.estimate/get-max-width :added "4.0"}
(fact "gets the max width of whole form"
  ^:hidden
  
  (est/get-max-width [:a :b :c])
  => 10

  (est/get-max-width [:a :b :c :d])
  => 13)

^{:refer code.layout.estimate/get-max-width-children :added "4.0"}
(fact "gets the max with of the children"
  ^:hidden

  (est/get-max-width-children [:a :b :c])
  => 8

  (est/get-max-width-children [:a :b :c :d])
  => 11)

^{:refer code.layout.estimate/estimate-multiline-basic :added "4.0"}
(fact "TODO")

^{:refer code.layout.estimate/estimate-multiline-data :added "4.0"}
(fact "TODO")

^{:refer code.layout.estimate/estimate-multiline-vector :added "4.0"}
(fact "TODO")

^{:refer code.layout.estimate/estimate-multiline-list :added "4.0"}
(fact "estimates if special forms are multilined"
  ^:hidden
  
  (est/estimate-multiline-special '(let [] a)
                                  {:readable-len 30})
  => false
  
  (est/estimate-multiline-special '(let [a 1] a)
                                     {:readable-len 30})
  => true)

^{:refer code.layout.estimate/estimate-multiline :added "4.0"}
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
  => true)



