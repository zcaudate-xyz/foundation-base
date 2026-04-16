(ns xtbench.js.lang.common-lib-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-lib/type-native :added "4.1"}
(fact "gets the native type"
  (!.js
   [(k/type-native "hello")
    (k/type-native 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/type-class :added "4.1"}
(fact "compiles the type-class helper"
  (!.js
   true)
  => true)

^{:refer xt.lang.common-lib/to-string :added "4.1"}
(fact "converts a value to a string"
  (!.js
   (k/to-string 42))
  => "42")

^{:refer xt.lang.common-lib/to-number :added "4.1"}
(fact "converts a string to a number"
  (!.js
   (k/to-number "42.5"))
  => 42.5)

^{:refer xt.lang.common-lib/nil? :added "4.1"}
(fact "checks whether a value is nil"
  (!.js
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false])

^{:refer xt.lang.common-lib/not-nil? :added "4.1"}
(fact "checks whether a value is not nil"
  (!.js
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true])

^{:refer xt.lang.common-lib/is-boolean? :added "4.1"}
(fact "checks if a value is boolean"
  (!.js
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-integer? :added "4.1"}
(fact "checks if a value is an integer"
  (!.js
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false])

^{:refer xt.lang.common-lib/is-number? :added "4.1"}
(fact "checks if a value is numeric"
  (!.js
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false])

^{:refer xt.lang.common-lib/is-string? :added "4.1"}
(fact "checks if a value is a string"
  (!.js
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-function? :added "4.1"}
(fact "checks if a value is a function"
  (!.js
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-array? :added "4.1"}
(fact "checks if a value is an array"
  (!.js
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false])

^{:refer xt.lang.common-lib/is-object? :added "4.1"}
(fact "checks if a value is an object"
  (!.js
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false])

^{:refer xt.lang.common-lib/proto-create :added "4.1"}
(fact "creates the prototype map"
  (!.js
   (var mt (k/proto-create {:hello (fn:> [v] (. v world))
                            :world "hello"}))
   (var a {})
   (setmetatable a mt)
   (. a (hello)))
  => "hello")

^{:refer xt.lang.common-lib/proto-get :added "4.1"}
(fact "gets the prototype map from an object"
  (!.js
   (var mt (k/proto-create {:world "hello"}))
   (var a {})
   (k/proto-set a mt nil)
   (== (k/proto-get a nil) mt))
  => true)

^{:refer xt.lang.common-lib/proto-set :added "4.1"}
(fact "sets the prototype map onto an object"
  (!.js
   (var mt (k/proto-create {:hello (fn:> [v] (. v world))
                            :world "hello"}))
   (var a {})
   (k/proto-set a mt nil)
   (. a (hello)))
  => "hello")

^{:refer xt.lang.common-lib/proto-tostring :added "4.1"}
(fact "compiles the runtime tostring helper"
  (!.js
   (var _ (fn [obj]
            (k/proto-tostring obj)))
   true)
  => true)

^{:refer xt.lang.common-lib/noop :added "4.1"}
(fact "returns nil"
  (!.js
   (k/noop))
  => nil)

^{:refer xt.lang.common-lib/identity :added "4.1"}
(fact "returns the input value"
  (!.js
   (k/identity 1))
  => 1)

^{:refer xt.lang.common-lib/T :added "4.1"}
(fact "always returns true"
  (!.js
   (k/T "anything"))
  => true)

^{:refer xt.lang.common-lib/F :added "4.1"}
(fact "always returns false"
  (!.js
   (k/F "anything"))
  => false)

^{:refer xt.lang.common-lib/add :added "4.1"}
(fact "adds two numbers"
  (!.js
   (k/add 1 2))
  => 3)

^{:refer xt.lang.common-lib/sub :added "4.1"}
(fact "subtracts two numbers"
  (!.js
   (k/sub 5 3))
  => 2)

^{:refer xt.lang.common-lib/mul :added "4.1"}
(fact "multiplies two numbers"
  (!.js
   (k/mul 3 4))
  => 12)

^{:refer xt.lang.common-lib/div :added "4.1"}
(fact "divides two numbers"
  (!.js
   (k/div 10 4))
  => 2.5)

^{:refer xt.lang.common-lib/gt :added "4.1"}
(fact "checks whether the first number is greater"
  (!.js
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false])

^{:refer xt.lang.common-lib/lt :added "4.1"}
(fact "checks whether the first number is smaller"
  (!.js
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/gte :added "4.1"}
(fact "checks whether the first number is greater than or equal"
  (!.js
   [(k/gte 3 2)
    (k/gte 3 3)
    (k/gte 2 3)])
  => [true true false])

^{:refer xt.lang.common-lib/lte :added "4.1"}
(fact "checks whether the first number is less than or equal"
  (!.js
   [(k/lte 2 3)
    (k/lte 3 3)
    (k/lte 3 2)])
  => [true true false])

^{:refer xt.lang.common-lib/eq :added "4.1"}
(fact "checks numeric equality"
  (!.js
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/neq :added "4.1"}
(fact "checks numeric inequality"
  (!.js
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true])

^{:refer xt.lang.common-lib/neg :added "4.1"}
(fact "returns the negated number"
  (!.js
   (k/neg 3))
  => -3)

^{:refer xt.lang.common-lib/inc :added "4.1"}
(fact "increments a number"
  (!.js
   (k/inc 1))
  => 2)

^{:refer xt.lang.common-lib/dec :added "4.1"}
(fact "decrements a number"
  (!.js
   (k/dec 1))
  => 0)

^{:refer xt.lang.common-lib/zero? :added "4.1"}
(fact "checks whether a number is zero"
  (!.js
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false])

^{:refer xt.lang.common-lib/pos? :added "4.1"}
(fact "checks whether a number is positive"
  (!.js
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false])

^{:refer xt.lang.common-lib/neg? :added "4.1"}
(fact "checks whether a number is negative"
  (!.js
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false])

^{:refer xt.lang.common-lib/even? :added "4.1"}
(fact "checks whether a number is even"
  (!.js
   [(k/even? 2)
    (k/even? 3)])
  => [true false])

^{:refer xt.lang.common-lib/odd? :added "4.1"}
(fact "checks whether a number is odd"
  (!.js
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true])

^{:refer xt.lang.common-lib/wrap-callback :added "4.1"}
(fact "returns a wrapped callback given a map"
  (!.js
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3])
