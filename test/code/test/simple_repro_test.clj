(ns code.test.simple-repro-test
  (:use code.test)
  (:require [code.test.base.context :as ctx]))

^{:refer code.test/any.checker :added "3.0"
  :adopt true}
(fact "Test Checker"
  ^:hidden
  
  (with-new-context {}
    (fact "Test Checker"
      {:a 1}
      => (contains {:a 1})))
  => true

  (with-new-context {:print #{}}
    (fact "contains fail"
      ^:hidden
      
      {:a 1}
      => (contains {:a 2})))
  => false)

^{:refer code.test/any.simple :added "3.0"
  :adopt true}
(fact "Test simple"
  ^:hidden
  
  (with-new-context {}
    (fact "simple correct"
      ^:hidden
      
      {:a 1}
      => {:a 1}))
  => true

  (with-new-context {:print #{}}
    (fact "simple fail"
      {:a 1}
      => {:a 2}))
  => false)

^{:refer code.test/any.fn :added "3.0"
  :adopt true}
(fact "Test Fn"
  ^:hidden
  
  (with-new-context {}
    (fact "Test Fn"
      
      1
      => odd?))
  => true

  (with-new-context {}
    (fact "function forms should work"
      
      2
      => (fn [x]
           (= 2 x))))
  => true)

^{:refer code.test/any.nested :added "3.0"
  :adopt true}
(fact "Test Nesting"

  (with-new-context {:print #{}}
    (fact "nested check should fail"
      
      (let [a 1]
        a => 2)
      
      (do
        1 => 2)))
  => false

  (with-new-context {}
    (fact "nested checks should pass"

      (let [a 1]
        a => 1)

      (do
        1 => 1)))
  => true)
