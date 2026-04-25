(ns xt.lang.common-lib-test
  (:require [std.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-lib :as k]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-lib/type-native :added "4.1"}
(fact "gets the native type"

  (!.js
    [(k/type-native "hello")
     (k/type-native 1)])
  => ["string" "number"]

  (!.lua
    [(k/type-native "hello")
     (k/type-native 1)])
  => ["string" "number"]

  (!.py
    [(k/type-native "hello")
     (k/type-native 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/type-class :added "4.1"}
(fact "gets the class type"

  (!.js
   [(k/type-class "hello")
    (k/type-class 1)])
  => ["string" "number"]

  (!.lua
   [(k/type-class "hello")
    (k/type-class 1)])
  => ["string" "number"]

  (!.py
   [(k/type-class "hello")
    (k/type-class 1)])
  => ["string" "number"])

^{:refer xt.lang.common-lib/to-string :added "4.1"}
(fact "converts a value to a string"

  (!.js
   (k/to-string 42))
  => "42"

  (!.lua
   (k/to-string 42))
  => "42"

  (!.py
   (k/to-string 42))
  => "42")

^{:refer xt.lang.common-lib/to-number :added "4.1"}
(fact "converts a string to a number"

  (!.js
   (k/to-number "42.5"))
  => 42.5

  (!.lua
   (k/to-number "42.5"))
  => 42.5

  (!.py
   (k/to-number "42.5"))
  => 42.5)

^{:refer xt.lang.common-lib/nil? :added "4.1"}
(fact "checks whether a value is nil"

  (!.js
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false]

  (!.lua
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false]

  (!.py
   [(k/nil? nil)
    (k/nil? 0)])
  => [true false])

^{:refer xt.lang.common-lib/not-nil? :added "4.1"}
(fact "checks whether a value is not nil"

  (!.js
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true]

  (!.lua
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true]

  (!.py
   [(k/not-nil? nil)
    (k/not-nil? 0)])
  => [false true])

^{:refer xt.lang.common-lib/is-boolean? :added "4.1"}
(fact "checks if a value is boolean"

  (!.js
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false]

  (!.lua
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false]

  (!.py
   [(k/is-boolean? true)
    (k/is-boolean? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-integer? :added "4.1"}
(fact "checks if a value is an integer"

  (!.js
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false]

  (!.lua
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false]

  (!.py
   [(k/is-integer? 4)
    (k/is-integer? 4.5)])
  => [true false])

^{:refer xt.lang.common-lib/is-number? :added "4.1"}
(fact "checks if a value is numeric"

  (!.js
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false]

  (!.lua
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false]

  (!.py
   [(k/is-number? 4)
    (k/is-number? "4")])
  => [true false])

^{:refer xt.lang.common-lib/is-string? :added "4.1"}
(fact "checks if a value is a string"

  (!.js
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false]

  (!.lua
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false]

  (!.py
   [(k/is-string? "hello")
    (k/is-string? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-function? :added "4.1"}
(fact "checks if a value is a function"

  (!.js
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false]

  (!.lua
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false]

  (!.py
   [(k/is-function? (fn:> [x] x))
    (k/is-function? 1)])
  => [true false])

^{:refer xt.lang.common-lib/is-array? :added "4.1"}
(fact "checks if a value is an array"

  (!.js
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false]

  (!.lua
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false]

  (!.py
   [(k/is-array? [1 2 3])
    (k/is-array? {:a 1})])
  => [true false])

^{:refer xt.lang.common-lib/is-object? :added "4.1"}
(fact "checks if a value is an object"

  (!.js
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false]

  (!.lua
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false]

  (!.py
   [(k/is-object? {:a 1})
    (k/is-object? [1 2 3])])
  => [true false])

^{:refer xt.lang.common-lib/noop :added "4.1"}
(fact "returns nil"

  (!.js
   (k/noop))
  => nil

  (!.lua
   (k/noop))
  => nil

  (!.py
   (k/noop))
  => nil)

^{:refer xt.lang.common-lib/identity :added "4.1"}
(fact "returns the input value"

  (!.js
   (k/identity 1))
  => 1

  (!.lua
   (k/identity 1))
  => 1

  (!.py
   (k/identity 1))
  => 1)

^{:refer xt.lang.common-lib/T :added "4.1"}
(fact "always returns true"

  (!.js
   (k/T "anything"))
  => true

  (!.lua
   (k/T "anything"))
  => true

  (!.py
   (k/T "anything"))
  => true)

^{:refer xt.lang.common-lib/F :added "4.1"}
(fact "always returns false"

  (!.js
   (k/F "anything"))
  => false

  (!.lua
   (k/F "anything"))
  => false

  (!.py
   (k/F "anything"))
  => false)

^{:refer xt.lang.common-lib/add :added "4.1"}
(fact "adds two numbers"

  (!.js
   (k/add 1 2))
  => 3

  (!.lua
   (k/add 1 2))
  => 3

  (!.py
   (k/add 1 2))
  => 3)

^{:refer xt.lang.common-lib/sub :added "4.1"}
(fact "subtracts two numbers"

  (!.js
   (k/sub 5 3))
  => 2

  (!.lua
   (k/sub 5 3))
  => 2

  (!.py
   (k/sub 5 3))
  => 2)

^{:refer xt.lang.common-lib/mul :added "4.1"}
(fact "multiplies two numbers"

  (!.js
   (k/mul 3 4))
  => 12

  (!.lua
   (k/mul 3 4))
  => 12

  (!.py
   (k/mul 3 4))
  => 12)

^{:refer xt.lang.common-lib/div :added "4.1"}
(fact "divides two numbers"

  (!.js
   (k/div 10 4))
  => 2.5

  (!.lua
   (k/div 10 4))
  => 2.5

  (!.py
   (k/div 10 4))
  => 2.5)

^{:refer xt.lang.common-lib/gt :added "4.1"}
(fact "checks whether the first number is greater"

  (!.js
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false]

  (!.lua
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false]

  (!.py
   [(k/gt 3 2)
    (k/gt 2 3)])
  => [true false])

^{:refer xt.lang.common-lib/lt :added "4.1"}
(fact "checks whether the first number is smaller"

  (!.js
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false]

  (!.lua
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false]

  (!.py
   [(k/lt 2 3)
    (k/lt 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/gte :added "4.1"}
(fact "checks whether the first number is greater than or equal"

  (!.js
   [(k/gte 3 2)
    (k/gte 3 3)
    (k/gte 2 3)])
  => [true true false]

  (!.lua
   [(k/gte 3 2)
    (k/gte 3 3)
    (k/gte 2 3)])
  => [true true false]

  (!.py
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
  => [true true false]

  (!.lua
   [(k/lte 2 3)
    (k/lte 3 3)
    (k/lte 3 2)])
  => [true true false]

  (!.py
   [(k/lte 2 3)
    (k/lte 3 3)
    (k/lte 3 2)])
  => [true true false])

^{:refer xt.lang.common-lib/eq :added "4.1"}
(fact "checks numeric equality"

  (!.js
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false]

  (!.lua
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false]

  (!.py
   [(k/eq 3 3)
    (k/eq 3 2)])
  => [true false])

^{:refer xt.lang.common-lib/neq :added "4.1"}
(fact "checks numeric inequality"

  (!.js
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true]

  (!.lua
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true]

  (!.py
   [(k/neq 3 3)
    (k/neq 3 2)])
  => [false true])

^{:refer xt.lang.common-lib/neg :added "4.1"}
(fact "returns the negated number"

  (!.js
   (k/neg 3))
  => -3

  (!.lua
   (k/neg 3))
  => -3

  (!.py
   (k/neg 3))
  => -3)

^{:refer xt.lang.common-lib/inc :added "4.1"}
(fact "increments a number"

  (!.js
   (k/inc 1))
  => 2

  (!.lua
   (k/inc 1))
  => 2

  (!.py
   (k/inc 1))
  => 2)

^{:refer xt.lang.common-lib/dec :added "4.1"}
(fact "decrements a number"

  (!.js
   (k/dec 1))
  => 0

  (!.lua
   (k/dec 1))
  => 0

  (!.py
   (k/dec 1))
  => 0)

^{:refer xt.lang.common-lib/zero? :added "4.1"}
(fact "checks whether a number is zero"

  (!.js
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false]

  (!.lua
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false]

  (!.py
   [(k/zero? 0)
    (k/zero? 1)])
  => [true false])

^{:refer xt.lang.common-lib/pos? :added "4.1"}
(fact "checks whether a number is positive"

  (!.js
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false]

  (!.lua
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false]

  (!.py
   [(k/pos? 1)
    (k/pos? -1)])
  => [true false])

^{:refer xt.lang.common-lib/neg? :added "4.1"}
(fact "checks whether a number is negative"

  (!.js
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false]

  (!.lua
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false]

  (!.py
   [(k/neg? -1)
    (k/neg? 1)])
  => [true false])

^{:refer xt.lang.common-lib/even? :added "4.1"}
(fact "checks whether a number is even"

  (!.js
   [(k/even? 2)
    (k/even? 3)])
  => [true false]

  (!.lua
   [(k/even? 2)
    (k/even? 3)])
  => [true false]

  (!.py
   [(k/even? 2)
    (k/even? 3)])
  => [true false])

^{:refer xt.lang.common-lib/odd? :added "4.1"}
(fact "checks whether a number is odd"

  (!.js
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true]

  (!.lua
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true]

  (!.py
   [(k/odd? 2)
    (k/odd? 3)])
  => [false true])

^{:refer xt.lang.common-lib/wrap-callback :added "4.1"}
(fact "returns a wrapped callback given a map"

  (!.js
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3]

  (!.lua
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3]

  (!.py
   [((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1)
    ((k/wrap-callback {} "missing") 3)])
  => [2 3])

^{:refer xt.lang.common-lib/return-encode :added "4.1"}
(fact "encode result for publish"

  (!.js
    (xt/x:json-decode
     (k/return-encode 1 "id-A" "key-A")))
  => {"return" "number", "key" "key-A", "id" "id-A", "value" 1, "type" "data"}

  (!.lua
    (xt/x:json-decode
     (k/return-encode 1 "id-A" "key-A")))
  => {"return" "number", "key" "key-A", "id" "id-A", "value" 1, "type" "data"}

  (!.py
    (xt/x:json-decode
     (k/return-encode 1 "id-A" "key-A")))
  => {"return" "number", "key" "key-A", "id" "id-A", "value" 1, "type" "data"})

^{:refer xt.lang.common-lib/return-wrap :added "4.1"}
(fact "wraps a function for encode"

  (!.js
    (xt/x:json-decode
     (k/return-wrap (fn []
                      (return 3)))))
  => (contains {"return" "number", "value" 3, "type" "data"})

  (!.lua
    (xt/x:json-decode
     (k/return-wrap (fn []
                      (return 3)))))
  => (contains {"return" "number", "value" 3, "type" "data"})

  (!.py
    (xt/x:json-decode
     (k/return-wrap (fn []
                      (return 3)))))
  => (contains {"return" "number", "value" 3, "type" "data"}))

^{:refer xt.lang.common-lib/return-eval :added "4.1"}
(fact "returns evaluation"

  ^{:seedgen/base   {:lua    {:transform {"1+1" "return 1+1"}}
                     :python {:suppress true}}}
  (!.js
    (xt/x:json-decode
     (k/return-eval "1+1")))
  => {"return" "number", "value" 2, "type" "data"}

  (!.lua
    (xt/x:json-decode
     (k/return-eval "return 1+1")))
  => {"return" "number", "value" 2, "type" "data"})

(comment

  (s/seedgen-benchadd '[xt.lang.common] {:lang [:r] :write true})
  
  (s/seedgen-langadd 'xt.lang.common-lib {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-lib {:lang [:lua :python] :write true}))
