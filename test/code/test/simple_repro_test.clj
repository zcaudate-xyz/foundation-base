(ns code.test.simple-repro-test
  (:use code.test)
  (:require [code.test.base.print :as print]))

^{:refer code.test/any.checker :added "3.0"
  :adopt true}
(fact "Test Checker"
  ^:hidden
  
  (with-new-context {}
    (fact "Test Checker"
      {:a 1}
      => (contains {:a 1})))
  => true

  (binding [print/*options* #{}]
    (with-new-context {}
      (fact "contains fail"
        ^:hidden
        
        {:a 1}
        => (contains {:a 2}))))
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

  (binding [print/*options* #{}]
    (with-new-context {}

      (fact "simple fail"
        {:a 1}
        => {:a 2})))
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

  (binding [print/*options* #{}]
    (with-new-context {}
      (fact "nested check should fail"
        
        (let [a 1]
          a => 2)
        
        (do
          1 => 2))))
  => false

  (with-new-context {}
    (fact "nested checks should pass"

      (let [a 1]
        a => 1)

      (do
        1 => 1)))
  => true)
