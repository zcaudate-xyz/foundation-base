(ns rt.basic.impl.process-go-test
  (:use code.test)
  (:require [rt.basic.impl.process-go :refer :all]))

^{:refer rt.basic.impl.process-go/default-twostep-wrap :added "4.1"}
(fact "prepends standalone go wrapper"
  (default-twostep-wrap "func main() {}")
  => #"package main")

^{:refer rt.basic.impl.process-go/transform-form :added "4.1"}
(fact "transforms forms into go main function"
  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"func main"

  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"fmt.Println")