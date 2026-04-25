(ns xtbench.r.lang.common-lib-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-lib/type-native :added "4.1"}
(fact "gets the native type"

  (!.R
    [(k/type-native "hello")
     (k/type-native 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/type-class :added "4.1"}
(fact "gets the class type"

  (!.R
   [(k/type-class "hello")
    (k/type-class 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/to-string :added "4.1"}
(fact "converts a value to a string"

  (!.R
   (k/to-string 42))
  => "42")

^{:refer xt.lang.common-lib/to-number :added "4.1"}
(fact "converts a string to a number"

  (!.R
   (k/to-number "42.5"))
  => 42.5)

^{:refer xt.lang.common-lib/nil? :added "4.1"}
(fact "checks whether a value is nil"

  (!.R
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false])

^{:refer xt.lang.common-lib/not-nil? :added "4.1"}
(fact "checks whether a value is not nil"

  (!.R
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true])

^{:refer xt.lang.common-lib/is-boolean? :added "4.1"}
(fact "checks if a value is boolean"

  (!.R
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-integer? :added "4.1"}
(fact "checks if a value is an integer"

  (!.R
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false])

^{:refer xt.lang.common-lib/is-number? :added "4.1"}
(fact "checks if a value is numeric"

  (!.R
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false])

^{:refer xt.lang.common-lib/is-string? :added "4.1"}
(fact "checks if a value is a string"

  (!.R
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-function? :added "4.1"}
(fact "checks if a value is a function"

  (!.R
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-array? :added "4.1"}
(fact "checks if a value is an array"

  (!.R
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false])

^{:refer xt.lang.common-lib/is-object? :added "4.1"}
(fact "checks if a value is an object"

  (!.R
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false])

^{:refer xt.lang.common-lib/noop :added "4.1"}
(fact "returns nil"

  (!.R
   (k/noop))
  => nil)

^{:refer xt.lang.common-lib/identity :added "4.1"}
(fact "returns the input value"

  (!.R
   (k/identity 1))
  => 1)

^{:refer xt.lang.common-lib/T :added "4.1"}
(fact "always returns true"

  (!.R
   (k/T "anything"))
  => true)

^{:refer xt.lang.common-lib/F :added "4.1"}
(fact "always returns false"

  (!.R
   (k/F "anything"))
  => false)

^{:refer xt.lang.common-lib/add :added "4.1"}
(fact "adds two numbers"

  (!.R
   (k/add 1 2))
  => 3)

^{:refer xt.lang.common-lib/sub :added "4.1"}
(fact "subtracts two numbers"

  (!.R
   (k/sub 5 3))
  => 2)

^{:refer xt.lang.common-lib/mul :added "4.1"}
(fact "multiplies two numbers"

  (!.R
   (k/mul 3 4))
  => 12)

^{:refer xt.lang.common-lib/div :added "4.1"}
(fact "divides two numbers"

  (!.R
   (k/div 10 4))
  => 2.5)

^{:refer xt.lang.common-lib/gt :added "4.1"}
(fact "checks whether the first number is greater"

  (!.R
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false])

^{:refer xt.lang.common-lib/lt :added "4.1"}
(fact "checks whether the first number is smaller"

  (!.R
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/gte :added "4.1"}
(fact "checks whether the first number is greater than or equal"

  (!.R
   [(k/gte 3 2)
    (k/gte 3 3)
    (k/gte 2 3)])
  => [true true false])

^{:refer xt.lang.common-lib/lte :added "4.1"}
(fact "checks whether the first number is less than or equal"

  (!.R
   [(k/lte 2 3)
    (k/lte 3 3)
    (k/lte 3 2)])
  => [true true false])

^{:refer xt.lang.common-lib/eq :added "4.1"}
(fact "checks numeric equality"

  (!.R
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/neq :added "4.1"}
(fact "checks numeric inequality"

  (!.R
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true])

^{:refer xt.lang.common-lib/neg :added "4.1"}
(fact "returns the negated number"

  (!.R
   (k/neg 3))
  => -3)

^{:refer xt.lang.common-lib/inc :added "4.1"}
(fact "increments a number"

  (!.R
   (k/inc 1))
  => 2)

^{:refer xt.lang.common-lib/dec :added "4.1"}
(fact "decrements a number"

  (!.R
   (k/dec 1))
  => 0)

^{:refer xt.lang.common-lib/zero? :added "4.1"}
(fact "checks whether a number is zero"

  (!.R
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false])

^{:refer xt.lang.common-lib/pos? :added "4.1"}
(fact "checks whether a number is positive"

  (!.R
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false])

^{:refer xt.lang.common-lib/neg? :added "4.1"}
(fact "checks whether a number is negative"

  (!.R
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false])

^{:refer xt.lang.common-lib/even? :added "4.1"}
(fact "checks whether a number is even"

  (!.R
   [(k/even? 2)
    (k/even? 3)])
  => [true false])

^{:refer xt.lang.common-lib/odd? :added "4.1"}
(fact "checks whether a number is odd"

  (!.R
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true])

^{:refer xt.lang.common-lib/wrap-callback :added "4.1"}
(fact "returns a wrapped callback given a map"

  (!.R
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3])

^{:refer xt.lang.common-lib/return-encode :added "4.1"}
(fact "encode result for publish"

  (!.R
    (xt/x:json-decode
     (k/return-encode 1 "id-A" "key-A")))
  => {"return" "number", "key" "key-A", "id" "id-A", "value" 1, "type" "data"})

^{:refer xt.lang.common-lib/return-wrap :added "4.1"}
(fact "wraps a function for encode"

  (!.R
    (xt/x:json-decode
     (k/return-wrap (fn []
                      (return 3)))))
  => (contains {"return" "number", "value" 3, "type" "data"}))

^{:refer xt.lang.common-lib/return-eval :added "4.1"}
(fact "returns evaluation"

  (!.R
    (xt/x:json-decode
     (k/return-eval "1+1")))
  => {"return" "number", "value" 2, "type" "data"})

(comment

  (s/seedgen-benchadd '[xt.lang.common] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.common-lib {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-lib {:lang [:lua :python] :write true}))
