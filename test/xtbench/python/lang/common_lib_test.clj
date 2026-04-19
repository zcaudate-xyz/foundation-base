(ns xtbench.python.lang.common-lib-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-lib/type-native :added "4.1"}
(fact "gets the native type"
  (!.py
   [(k/type-native "hello")
    (k/type-native 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/type-class :added "4.1"}
(fact "compiles the type-class helper"
  (!.py
   true)
  => true)

^{:refer xt.lang.common-lib/to-string :added "4.1"}
(fact "converts a value to a string"
  (!.py
   (k/to-string 42))
  => "42")

^{:refer xt.lang.common-lib/to-number :added "4.1"}
(fact "converts a string to a number"
  (!.py
   (k/to-number "42.5"))
  => 42.5)

^{:refer xt.lang.common-lib/nil? :added "4.1"}
(fact "checks whether a value is nil"
  (!.py
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false])

^{:refer xt.lang.common-lib/not-nil? :added "4.1"}
(fact "checks whether a value is not nil"
  (!.py
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true])

^{:refer xt.lang.common-lib/is-boolean? :added "4.1"}
(fact "checks if a value is boolean"
  (!.py
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-integer? :added "4.1"}
(fact "checks if a value is an integer"
  (!.py
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false])

^{:refer xt.lang.common-lib/is-number? :added "4.1"}
(fact "checks if a value is numeric"
  (!.py
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false])

^{:refer xt.lang.common-lib/is-string? :added "4.1"}
(fact "checks if a value is a string"
  (!.py
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-function? :added "4.1"}
(fact "checks if a value is a function"
  (!.py
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-array? :added "4.1"}
(fact "checks if a value is an array"
  (!.py
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false])

^{:refer xt.lang.common-lib/is-object? :added "4.1"}
(fact "checks if a value is an object"
  (!.py
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false])

^{:refer xt.lang.common-lib/noop :added "4.1"}
(fact "returns nil"
  (!.py
   (k/noop))
  => nil)

^{:refer xt.lang.common-lib/identity :added "4.1"}
(fact "returns the input value"
  (!.py
   (k/identity 1))
  => 1)

^{:refer xt.lang.common-lib/T :added "4.1"}
(fact "always returns true"
  (!.py
   (k/T "anything"))
  => true)

^{:refer xt.lang.common-lib/F :added "4.1"}
(fact "always returns false"
  (!.py
   (k/F "anything"))
  => false)

^{:refer xt.lang.common-lib/add :added "4.1"}
(fact "adds two numbers"
  (!.py
   (k/add 1 2))
  => 3)

^{:refer xt.lang.common-lib/sub :added "4.1"}
(fact "subtracts two numbers"
  (!.py
   (k/sub 5 3))
  => 2)

^{:refer xt.lang.common-lib/mul :added "4.1"}
(fact "multiplies two numbers"
  (!.py
   (k/mul 3 4))
  => 12)

^{:refer xt.lang.common-lib/div :added "4.1"}
(fact "divides two numbers"
  (!.py
   (k/div 10 4))
  => 2.5)

^{:refer xt.lang.common-lib/gt :added "4.1"}
(fact "checks whether the first number is greater"
  (!.py
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false])

^{:refer xt.lang.common-lib/lt :added "4.1"}
(fact "checks whether the first number is smaller"
  (!.py
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/gte :added "4.1"}
(fact "checks whether the first number is greater than or equal"
  (!.py
   [(k/gte 3 2)
    (k/gte 3 3)
    (k/gte 2 3)])
  => [true true false])

^{:refer xt.lang.common-lib/lte :added "4.1"}
(fact "checks whether the first number is less than or equal"
  (!.py
   [(k/lte 2 3)
    (k/lte 3 3)
    (k/lte 3 2)])
  => [true true false])

^{:refer xt.lang.common-lib/eq :added "4.1"}
(fact "checks numeric equality"
  (!.py
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/neq :added "4.1"}
(fact "checks numeric inequality"
  (!.py
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true])

^{:refer xt.lang.common-lib/neg :added "4.1"}
(fact "returns the negated number"
  (!.py
   (k/neg 3))
  => -3)

^{:refer xt.lang.common-lib/inc :added "4.1"}
(fact "increments a number"
  (!.py
   (k/inc 1))
  => 2)

^{:refer xt.lang.common-lib/dec :added "4.1"}
(fact "decrements a number"
  (!.py
   (k/dec 1))
  => 0)

^{:refer xt.lang.common-lib/zero? :added "4.1"}
(fact "checks whether a number is zero"
  (!.py
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false])

^{:refer xt.lang.common-lib/pos? :added "4.1"}
(fact "checks whether a number is positive"
  (!.py
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false])

^{:refer xt.lang.common-lib/neg? :added "4.1"}
(fact "checks whether a number is negative"
  (!.py
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false])

^{:refer xt.lang.common-lib/even? :added "4.1"}
(fact "checks whether a number is even"
  (!.py
   [(k/even? 2)
    (k/even? 3)])
  => [true false])

^{:refer xt.lang.common-lib/odd? :added "4.1"}
(fact "checks whether a number is odd"
  (!.py
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true])

^{:refer xt.lang.common-lib/wrap-callback :added "4.1"}
(fact "returns a wrapped callback given a map"
  (!.py
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3])
