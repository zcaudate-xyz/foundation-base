tahto/runtime/basic/impl/process_go_test.clj:1:(ns tahto.runtime.basic.impl.process-go-test
  (:use code.test)
tahto/runtime/basic/impl/process_go_test.clj:3:  (:require [tahto.runtime.basic.impl.process-go :refer :all]))

tahto/runtime/basic/impl/process_go_test.clj:5:^{:refer tahto.runtime.basic.impl.process-go/default-twostep-wrap :added "4.1"}
(fact "prepends standalone go wrapper"
  (default-twostep-wrap "func main() {}")
  => #"package main")

tahto/runtime/basic/impl/process_go_test.clj:10:^{:refer tahto.runtime.basic.impl.process-go/transform-form :added "4.1"}
(fact "transforms forms into go main function"
  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"func main"

  (-> (transform-form ['(+ 1 2)] {}) pr-str)
  => #"fmt.Println")