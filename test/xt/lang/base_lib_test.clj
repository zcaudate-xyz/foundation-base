(ns xt.lang.base-lib-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(do 
  (l/script- :js
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-macro :as km]]})
  
  (l/script- :lua
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-macro :as km]]})
  
  (l/script- :python
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-macro :as km]]})
  
  (l/script- :r
    {:runtime :basic
     :require [[xt.lang.base-lib :as k]
               [xt.lang.base-macro :as km]]}))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-lib/proto-create :added "4.0"}
(fact "creates the prototype map"
  ^:hidden

  (!.lua
   (var mt (k/proto-create {:hello (fn:> [v] (. v world))
                            :world "hello"}))
   (var a  {})
   (k/set-proto a mt)
   (. a (hello)))
  => "hello"

  (!.js
   (var mt (k/proto-create {:hello (fn:> [v] (. v world))
                            :world "hello"}))
   (var a  {})
   (k/set-proto a mt)
   (. a (hello)))
  => "hello")

^{:refer xt.lang.base-lib/type-native :added "4.0"}
(fact "gets the native type"
  ^:hidden
  
  (!.js
   [(k/type-native {})
    (k/type-native [1])
    (k/type-native (fn []))
    (k/type-native 1)
    (k/type-native "")
    (k/type-native true)])
  => ["object" "array" "function" "number" "string" "boolean"]
  
  (!.lua
   [(k/type-native {})
    (k/type-native [1])
    (k/type-native (fn []))
    (k/type-native 1)
    (k/type-native "")
    (k/type-native true)])
  => ["object" "array" "function" "number" "string" "boolean"]

  (!.py
   [(k/type-native {})
    (k/type-native [1])
    (k/type-native (fn []))
    (k/type-native 1)
    (k/type-native "")
    (k/type-native true)])
  => ["object" "array" "function" "number" "string" "boolean"]
  
  (!.R
   [(k/type-native {:a 1 :b 2})
    (k/type-native [1])
    (k/type-native (fn []))
    (k/type-native 1)
    (k/type-native "")
    (k/type-native true)])
  => ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.base-lib/type-class :added "4.0"}
(fact "gets the type of an object"
  ^:hidden
  
  (!.js
   [(k/type-class {})
    (k/type-class [1])
    (k/type-class (fn []))
    (k/type-class 1)
    (k/type-class "")
    (k/type-class true)])
  => ["object" "array" "function" "number" "string" "boolean"]
  
  (!.lua
   [(k/type-class {})
    (k/type-class [1])
    (k/type-class (fn []))
    (k/type-class 1)
    (k/type-class "")
    (k/type-class true)])
  => ["object" "array" "function" "number" "string" "boolean"]

  (!.py
   [(k/type-class {})
    (k/type-class [1])
    (k/type-class (fn []))
    (k/type-class 1)
    (k/type-class "")
    (k/type-class true)])
  => ["object" "array" "function" "number" "string" "boolean"]

  (!.R
   [(k/type-class {:a 1 :b 2})
    (k/type-class [1])
    (k/type-class (fn []))
    (k/type-class 1)
    (k/type-class "")
    (k/type-class true)])
  => ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.base-lib/fn? :added "4.0"}
(fact "checks if object is a function type"
  ^:hidden
  
  (!.js
   [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
  => [true true false]
  
  (!.lua
   [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
  => [true true false]
  
  (!.py
   [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
  => [true true false]

  (!.R
   [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
  => [true true false])

^{:refer xt.lang.base-lib/arr? :added "4.0"}
(fact "checks if object is an array"
  ^:hidden
  
  (!.js
   [(k/arr? [1 2 3]) (k/arr? {:a 1})])
  => [true false]

  (!.lua
   [(k/arr? [1 2 3]) (k/arr? {:a 1})])
  => [true false]

  (!.py
   [(k/arr? [1 2 3]) (k/arr? {:a 1})])
  => [true false]

  (!.R
   [(k/arr? [1 2 3]) (k/arr? {:a 1})])
  => [true false])

^{:refer xt.lang.base-lib/obj? :added "4.0"}
(fact "checks if object is a map type"
  ^:hidden
  
  (!.js
   [(k/obj? {:a 1}) (k/obj? [1 2 3])])
  => [true false]

  (!.lua
   [(k/obj? {:a 1}) (k/obj? [1 2 3])])
  => [true false]

  (!.py
   [(k/obj? {:a 1}) (k/obj? [1 2 3])])
  => [true false]

  (!.R
   [(k/obj? {:a 1}) (k/obj? [1 2 3])])
  => [true false])

^{:refer xt.lang.base-lib/id-fn :added "4.0"}
(fact "gets the id for an object")

^{:refer xt.lang.base-lib/key-fn :added "4.0"}
(fact "creates a key access function")

^{:refer xt.lang.base-lib/eq-fn :added "4.0"}
(fact "creates a eq comparator function")

^{:refer xt.lang.base-lib/inc-fn :added "4.0"}
(fact "creates a increment function by closure")

^{:refer xt.lang.base-lib/identity :added "4.0"}
(fact "identity function"
  ^:hidden
  
  (!.js (k/identity 1))
  => 1

  (!.lua (k/identity 1))
  => 1

  (!.py (k/identity 1))
  => 1

  (!.R (k/identity 1))
  => 1)

^{:refer xt.lang.base-lib/noop :added "4.0"}
(fact "always a no op")

^{:refer xt.lang.base-lib/T :added "4.0"}
(fact "always true")

^{:refer xt.lang.base-lib/F :added "4.0"}
(fact "always false")

^{:refer xt.lang.base-lib/step-nil :added "4.0"}
(fact "nil step for fold")

^{:refer xt.lang.base-lib/step-thrush :added "4.0"}
(fact "thrush step for fold")

^{:refer xt.lang.base-lib/step-call :added "4.0"}
(fact "call step for fold")

^{:refer xt.lang.base-lib/step-push :added "4.0"}
(fact "step to push element into arr")

^{:refer xt.lang.base-lib/step-set-key :added "4.0"}
(fact "step to set key in object")

^{:refer xt.lang.base-lib/step-set-fn :added "4.0"}
(fact "creates a set key function"
  ^:hidden
  
  (!.js
   ((k/step-set-fn {} "a") 1))
  => {"a" 1}

  (!.lua
   ((k/step-set-fn {} "a") 1))
  => {"a" 1}

  (!.py
   ((k/step-set-fn {} "a") 1))
  => {"a" 1}
  
  (!.R
   ((k/step-set-fn {} "a") 1))
  => {"a" 1})

^{:refer xt.lang.base-lib/step-set-pair :added "4.0"}
(fact "step to set key value pair in object")

^{:refer xt.lang.base-lib/step-del-key :added "4.0"}
(fact "step to delete key in object")

^{:refer xt.lang.base-lib/starts-with? :added "4.0"}
(fact "check for starts with"
  ^:hidden

  (!.js
   (k/starts-with? "Foo Bar" "Foo"))
  => true
  
  (!.lua
   (k/starts-with? "Foo Bar" "Foo"))
  => true

  (!.py
   (k/starts-with? "Foo Bar" "Foo"))
  => true

  (!.R
   (k/starts-with? "Foo Bar" "Foo"))
  => true)

^{:refer xt.lang.base-lib/ends-with? :added "4.0"}
(fact "check for ends with"
  ^:hidden
  
  (!.js
   (k/ends-with? "Foo Bar" "Bar"))
  => true
  
  (!.lua
   (k/ends-with? "Foo Bar" "Bar"))
  => true

  (!.py
   (k/ends-with? "Foo Bar" "Bar"))
  => true

  (!.R
   (k/ends-with? "Foo Bar" "Bar"))
  => true)

^{:refer xt.lang.base-lib/capitalize :added "4.0"}
(fact "uppercases the first letter"
  ^:hidden
  
  (!.js
   (k/capitalize "hello"))
  => "Hello"

  (!.lua
   (k/capitalize "hello"))
  => "Hello"

  (!.py
   (k/capitalize "hello"))
  => "Hello"

  (!.R
   (k/capitalize "hello"))
  => "Hello")

^{:refer xt.lang.base-lib/decapitalize :added "4.0"}
(fact "lowercases the first letter"
  ^:hidden
  
  (!.js
   (k/decapitalize "HELLO"))
  => "hELLO"

  (!.lua
   (k/decapitalize "HELLO"))
  => "hELLO"

  (!.py
   (k/decapitalize "HELLO"))
  => "hELLO"

  (!.R
   (k/decapitalize "HELLO"))
  => "hELLO")

^{:refer xt.lang.base-lib/pad-left :added "4.0"}
(fact "pads string with n chars on left"
  ^:hidden
  
  (!.js
   (k/pad-left "000" 5 "-"))
  => "--000"

  (!.lua
   (k/pad-left "000" 5 "-"))
  => "--000"

  (!.py
   (k/pad-left "000" 5 "-"))
  => "--000"

  (!.R
   (k/pad-left "000" 5 "-"))
  => "--000")

^{:refer xt.lang.base-lib/pad-right :added "4.0"}
(fact "pads string with n chars on right"
  ^:hidden
  
  (!.js
   (k/pad-right "000" 5 "-"))
  => "000--"

  (!.lua
   (k/pad-right "000" 5 "-"))
  => "000--"

  (!.py
   (k/pad-right "000" 5 "-"))
  => "000--"

  (!.R
   (k/pad-right "000" 5 "-"))
  => "000--")

^{:refer xt.lang.base-lib/pad-lines :added "4.0"}
(fact "pad lines with starting chars"
  ^:hidden

  (!.js
   (k/pad-lines (k/join "\n"
                        ["hello"
                         "world"])
                2
                " "))
  => (std.string/|
      "  hello"
      "  world")

  (!.lua
   (k/pad-lines (k/join "\n"
                        ["hello"
                         "world"])
                2
                " "))
  => (std.string/|
      "  hello"
      "  world")

  (!.py
   (k/pad-lines (k/join "\n"
                        ["hello"
                         "world"])
                2
                " "))
  => (std.string/|
      "  hello"
      "  world")

  (!.R
   (k/pad-lines (k/join "\n"
                        ["hello"
                         "world"])
                2
                " "))
  => (std.string/|
      ""
      "  hello"
      "  world"))

^{:refer xt.lang.base-lib/mod-pos :added "4.0"}
(fact "gets the positive mod"
  ^:hidden
  
  (!.js
   [(mod -11 10)
    (k/mod-pos -11 10)])
  => [-1 9]

  (!.lua
   [(mod -11 10)
    (k/mod-pos -11 10)])
  => [9 9]

  (!.py
   [(mod -11 10)
    (k/mod-pos -11 10)])
  => [9 9]

  (!.R
   [(mod -11 10)
    (k/mod-pos -11 10)])
  => [9 9])

^{:refer xt.lang.base-lib/mod-offset :added "4.0"}
(fact "calculates the closet offset"
  ^:hidden
  
  (!.js
   [(k/mod-offset 20 280 360)
    (k/mod-offset 280 20 360)
    (k/mod-offset 280 -80 360)
    (k/mod-offset 20 -60 360)
    (k/mod-offset 60 30 360)])
  => [-100 100 0 -80 -30]

  (!.lua
   [(k/mod-offset 20 280 360)
    (k/mod-offset 280 20 360)
    (k/mod-offset 280 -80 360)
    (k/mod-offset 20 -60 360)
    (k/mod-offset 60 30 360)])
  => [-100 100 0 -80 -30]

  (!.py
   [(k/mod-offset 20 280 360)
    (k/mod-offset 280 20 360)
    (k/mod-offset 280 -80 360)
    (k/mod-offset 20 -60 360)
    (k/mod-offset 60 30 360)])
  => [-100 100 0 -80 -30]

  (!.R
   [(k/mod-offset 20 280 360)
    (k/mod-offset 280 20 360)
    (k/mod-offset 280 -80 360)
    (k/mod-offset 20 -60 360)
    (k/mod-offset 60 30 360)])
  => [-100 100 0 -80 -30])

^{:refer xt.lang.base-lib/gcd :added "4.0"}
(fact "greatest common denominator"
  ^:hidden
  
  (!.js
   (k/gcd 10 6))
  => 2

  (!.lua
   (k/gcd 10 6))
  => 2

  (!.py
   (k/gcd 10 6))
  => 2

  (!.R
   (k/gcd 10 6))
  => 2)

^{:refer xt.lang.base-lib/lcm :added "4.0"}
(fact "lowest common multiple"
  ^:hidden

  (!.js
   (k/lcm 10 6))
  => 30

  (!.lua
   (k/lcm 10 6))
  => 30
  
  (!.py
   (k/lcm 10 6))
  => 30.0

  (!.R
   (k/lcm 10 6))
  => 30)

^{:refer xt.lang.base-lib/mix :added "4.0"}
(fact "mixes two values with a fraction"
  ^:hidden
  
  (!.js
   (k/mix 100 20 0.1))
  => 92

  (!.lua
   (k/mix 100 20 0.1))
  => 92

  (!.py
   (k/mix 100 20 0.1 nil))
  => 92.0

  (!.R
   (k/mix 100 20 0.1 nil))
  => 92)

^{:refer xt.lang.base-lib/sign :added "4.0"}
(fact "gets the sign of "
  ^:hidden
  
  (!.js
   [(k/sign -10) (k/sign 10)])
  => [-1 1]
  
  (!.lua
   [(k/sign -10) (k/sign 10)])
  => [-1 1]
  
  (!.py
   [(k/sign -10) (k/sign 10)])
  => [-1 1]
  
  (!.R
   [(k/sign -10) (k/sign 10)])
  => [-1 1])

^{:refer xt.lang.base-lib/round :added "4.0"}
(fact "rounds to the nearest integer"
  ^:hidden

  (!.js
   [(k/round 0.9)
    (k/round 1.1)
    (k/round 1.49)
    (k/round 1.51)])
  => [1 1 1 2]

  (!.lua
   [(k/round 0.9)
    (k/round 1.1)
    (k/round 1.49)
    (k/round 1.51)])
  => [1 1 1 2]

  (!.py
   [(k/round 0.9)
    (k/round 1.1)
    (k/round 1.49)
    (k/round 1.51)])
  => [1 1 1 2]

  (!.R
   [(k/round 0.9)
    (k/round 1.1)
    (k/round 1.49)
    (k/round 1.51)])
  => [1 1 1 2])

^{:refer xt.lang.base-lib/clamp :added "4.0"}
(fact "clamps a value between min and max"
  ^:hidden
  
  (!.js [(k/clamp 0 5 6)
         (k/clamp 0 5 -1)
         (k/clamp 0 5 4)])
  => [5 0 4]

  (!.lua [(k/clamp 0 5 6)
         (k/clamp 0 5 -1)
         (k/clamp 0 5 4)])
  => [5 0 4]

  (!.py [(k/clamp 0 5 6)
         (k/clamp 0 5 -1)
         (k/clamp 0 5 4)])
  => [5 0 4]

  (!.R [(k/clamp 0 5 6)
         (k/clamp 0 5 -1)
         (k/clamp 0 5 4)])
  => [5 0 4])

^{:refer xt.lang.base-lib/bit-count :added "4.0"}
(fact "get the bit count"
  ^:hidden
  
  (!.js
   [(k/bit-count 16)
    (k/bit-count 10)
    (k/bit-count 3)
    (k/bit-count 7)])
  => [1 2 2 3]

  (!.lua
   [(k/bit-count 16)
    (k/bit-count 10)
    (k/bit-count 3)
    (k/bit-count 7)])
  => [1 2 2 3]

  (!.py
   [(k/bit-count 16)
    (k/bit-count 10)
    (k/bit-count 3)
    (k/bit-count 7)])
  => [1 2 2 3])

^{:refer xt.lang.base-lib/sym-full :added "4.0"}
(fact "creates a sym"
  ^:hidden
  
  (!.js (k/sym-full "hello" "world"))
  => "hello/world"

  (!.lua (k/sym-full "hello" "world"))
  => "hello/world"
  
  (!.py (k/sym-full "hello" "world"))
  => "hello/world"

  (!.R (k/sym-full "hello" "world"))
  => "hello/world")

^{:refer xt.lang.base-lib/sym-name :added "4.0"}
(fact "gets the name part of the sym"
  ^:hidden
  
  (!.js (k/sym-name "hello/world"))
  => "world"

  (!.lua (k/sym-name "hello/world"))
  => "world"

  (!.py (k/sym-name "hello/world"))
  => "world"

  (!.R (k/sym-name "hello/world"))
  => "world")

^{:refer xt.lang.base-lib/sym-ns :added "4.0"}
(fact "gets the namespace part of the sym"
  ^:hidden
  
  (!.js [(k/sym-ns "hello/world")
         (k/sym-ns "hello")])
  => ["hello" nil]
  
  (!.lua [(k/sym-ns "hello/world")
          (k/sym-ns "hello")])
  => ["hello"]
  
  (!.py [(k/sym-ns "hello/world")
         (k/sym-ns "hello")])
  => ["hello" nil]

  (!.R [(k/sym-ns "hello/world")
        (k/sym-ns "hello")])
  => ["hello" nil])

^{:refer xt.lang.base-lib/sym-pair :added "4.0"}
(fact "gets the sym pair"
  ^:hidden
  
  (!.js (k/sym-pair "hello/world"))
  => ["hello" "world"]

  (!.lua (k/sym-pair "hello/world"))
  => ["hello" "world"]

  (!.py (k/sym-pair "hello/world"))
  => ["hello" "world"]

  (!.R (k/sym-pair "hello/world"))
  => ["hello" "world"])

^{:refer xt.lang.base-lib/is-empty? :added "4.0"}
(fact "checks that array is empty"
  ^:hidden
  
  (!.js [(k/is-empty? nil)
         (k/is-empty? "")
         (k/is-empty? "123")
         (k/is-empty? [])
         (k/is-empty? [1 2 3])
         (k/is-empty? {})
         (k/is-empty? {:a 1 :b 2})])
  => [true true false true false true false]

  (!.lua [(k/is-empty? nil)
         (k/is-empty? "")
         (k/is-empty? "123")
         (k/is-empty? [])
         (k/is-empty? [1 2 3])
         (k/is-empty? {})
         (k/is-empty? {:a 1 :b 2})])
  => [true true false true false true false]

  (!.py [(k/is-empty? nil)
         (k/is-empty? "")
         (k/is-empty? "123")
         (k/is-empty? [])
         (k/is-empty? [1 2 3])
         (k/is-empty? {})
         (k/is-empty? {:a 1 :b 2})])
  => [true true false true false true false]

  (!.R [(k/is-empty? nil)
         (k/is-empty? "")
         (k/is-empty? "123")
         (k/is-empty? [])
         (k/is-empty? [1 2 3])
         (k/is-empty? {})
         (k/is-empty? {:a 1 :b 2})])
  => [true true false true false true false])

^{:refer xt.lang.base-lib/arr-lookup :added "4.0"}
(fact "constructs a lookup given keys"
  ^:hidden
  
  (!.js
   (k/arr-lookup ["a" "b" "c"]))
  => {"a" true, "b" true, "c" true}

  (!.lua
   (k/arr-lookup ["a" "b" "c"]))
  => {"a" true, "b" true, "c" true}

  (!.py
   (k/arr-lookup ["a" "b" "c"]))
  => {"a" true, "b" true, "c" true}

  (!.R
   (k/arr-lookup ["a" "b" "c"]))
  => {"a" true, "b" true, "c" true})

^{:refer xt.lang.base-lib/arr-every :added "4.0"}
(fact "checks that every element fulfills thet predicate"
  ^:hidden
  
  (!.js
   [(k/arr-every [1 2 3] km/odd?)
    (k/arr-every [1 3] km/odd?)])
  => [false true]

  (!.lua
   [(k/arr-every [1 2 3] km/odd?)
    (k/arr-every [1 3] km/odd?)])
  => [false true]

  (!.py
   [(k/arr-every [1 2 3] km/odd?)
    (k/arr-every [1 3] km/odd?)])
  => [false true]

  (!.R
   [(k/arr-every [1 2 3] km/odd?)
    (k/arr-every [1 3] km/odd?)])
  => [false true])

^{:refer xt.lang.base-lib/arr-some :added "4.0"}
(fact "checks that the array contains an element"
  ^:hidden
  
  (!.js
   [(k/arr-some [1 2 3] km/even?)
    (k/arr-some [1 3] km/even?)])
  => [true false]

  (!.lua
   [(k/arr-some [1 2 3] km/even?)
    (k/arr-some [1 3] km/even?)])
  => [true false]
  
  (!.py
   [(k/arr-some [1 2 3] km/even?)
    (k/arr-some [1 3] km/even?)])
  => [true false]

  (!.R
   [(k/arr-some [1 2 3] km/even?)
    (k/arr-some [1 3] km/even?)])
  => [true false])

^{:refer xt.lang.base-lib/arr-each :added "4.0"}
(fact "performs a function call for each element"
  ^:hidden
  
  (!.js
   (var a := [])
   (k/arr-each [1 2 3 4 5] (fn [e]
                             (x:arr-push a (+ 1 e))))
   a)
  => [2 3 4 5 6]
  
  (!.lua
   (var a := [])
   (k/arr-each [1 2 3 4 5] (fn [e]
                             (x:arr-push a (+ 1 e))))
   a)
  => [2 3 4 5 6]

  (!.py
   (var a := [])
   (k/arr-each [1 2 3 4 5] (fn [e]
                             (x:arr-push a (+ 1 e))))
   a)
  => [2 3 4 5 6]

  ^:fails
  (!.R
   (var a := [])
   (k/arr-each [1 2 3 4 5] (fn [e]
                             (x:arr-push a (+ 1 e))))
   a)
  => [])

^{:refer xt.lang.base-lib/arr-omit :added "4.0"}
(fact "emits index from new array"
  ^:hidden
  
  (!.js
   (k/arr-omit ["a" "b" "c" "d"]
               2))
  => ["a" "b" "d"]

  (!.lua
   (k/arr-omit ["a" "b" "c" "d"]
               2))
  => ["a" "b" "d"]

  (!.py
   (k/arr-omit ["a" "b" "c" "d"]
               2))
  => ["a" "b" "d"]

  (!.R
   (k/arr-omit ["a" "b" "c" "d"]
               2))
  => ["a" "b" "d"])

^{:refer xt.lang.base-lib/arr-reverse :added "4.0"}
(fact "reverses the array"
  ^:hidden
  
  (!.js
   (k/arr-reverse [1 2 3 4 5]))
  => [5 4 3 2 1]

  (!.lua
   (k/arr-reverse [1 2 3 4 5]))
  => [5 4 3 2 1]

  (!.py
   (k/arr-reverse [1 2 3 4 5]))
  => [5 4 3 2 1]

  (!.R
   (k/arr-reverse [1 2 3 4 5]))
  => [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-find :added "4.0"}
(fact "finds first index matching predicate"
  ^:hidden
  
  (!.js
   (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
  => 2

  (!.lua
   (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
  => 2

  (!.py
   (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
  => 2

  (!.R
   (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
  => 2)

^{:refer xt.lang.base-lib/arr-zip :added "4.0"}
(fact "zips two arrays together into a map"
  ^:hidden
  
  (!.js
   (k/arr-zip ["a" "b" "c"]
              [1 2 3]))
  => {"a" 1, "b" 2, "c" 3}

  (!.lua
   (k/arr-zip ["a" "b" "c"]
              [1 2 3]))
  => {"a" 1, "b" 2, "c" 3}

  (!.py
   (k/arr-zip ["a" "b" "c"]
              [1 2 3]))
  => {"a" 1, "b" 2, "c" 3}

  (!.R
   (k/arr-zip ["a" "b" "c"]
              [1 2 3]))
  => {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-map :added "4.0"}
(fact "maps a function across an array"
  ^:hidden
  
  (!.js
   (k/arr-map [1 2 3 4 5] km/inc))
  => [2 3 4 5 6]
  
  (!.lua
   (k/arr-map [1 2 3 4 5] km/inc))
  => [2 3 4 5 6]

  (!.py
   (k/arr-map [1 2 3 4 5] km/inc))
  => [2 3 4 5 6]

  (!.R
   (k/arr-map [1 2 3 4 5] km/inc))
  => [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-clone :added "4.0"}
(fact "clones an array"
  ^:hidden
  
  (!.js
   (k/arr-clone [1 2 3]))
  => [1 2 3]

  (!.lua
   (k/arr-clone [1 2 3]))
  => [1 2 3]

  (!.py
   (k/arr-clone [1 2 3]))
  => [1 2 3]

  (!.R
   (k/arr-clone [1 2 3]))
  => [1 2 3])

^{:refer xt.lang.base-lib/arr-append :added "4.0"}
(fact "appends to the end of an array"
  ^:hidden
  
  (!.js
   (var out := [1 2 3])
   (k/arr-append out [4 5])
   out)
  => [1 2 3 4 5]

  (!.lua
   (var out := [1 2 3])
   (k/arr-append out [4 5])
   out)
  => [1 2 3 4 5]

  (!.py
   (var out := [1 2 3])
   (k/arr-append out [4 5])
   out)
  => [1 2 3 4 5]

  (!.R
   (var out := [1 2 3])
   (k/arr-append out [4 5])
   out)
  => [1 2 3])

^{:refer xt.lang.base-lib/arr-slice :added "4.0"}
(fact "slices an array"
  ^:hidden

  (!.js
   (k/arr-slice [1 2 3 4 5] 1 3))
  => [2 3]
  
  (!.lua
   (k/arr-slice [1 2 3 4 5] 1 3))
  => [2 3]

  (!.py
   (k/arr-slice [1 2 3 4 5] 1 3))
  => [2 3]

  #_#_#_
  (!.R
   (k/arr-slice [1 2 3 4 5] 1 3))
  => [2 3])

^{:refer xt.lang.base-lib/arr-rslice :added "4.0"}
(fact "gets the reverse of a slice"
  ^:hidden
  
  (!.js
   (k/arr-rslice [1 2 3 4 5] 1 3))
  => [3 2]
  
  (!.lua
   (k/arr-rslice [1 2 3 4 5] 1 3))
  => [3 2]

  (!.py
   (k/arr-rslice [1 2 3 4 5] 1 3))
  => [3 2]

  (!.R
   (k/arr-rslice [1 2 3 4 5] 1 3))
  => [3 2])

^{:refer xt.lang.base-lib/arr-tail :added "4.0"}
(fact "gets the tail of the array"
  ^:hidden
  
  (!.js
   (k/arr-tail [1 2 3 4 5] 3))
  => [5 4 3]

  (!.lua
   (k/arr-tail [1 2 3 4 5] 3))
  => [5 4 3]

  (!.py
   (k/arr-tail [1 2 3 4 5] 3))
  => [5 4 3]

  (!.R
   (k/arr-tail [1 2 3 4 5] 3))
  => [5 4 3])

^{:refer xt.lang.base-lib/arr-mapcat :added "4.0"}
(fact "maps an array function, concatenting results"
  ^:hidden
  
  (!.js
   (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
  => [1 1 2 2 3 3]

  (!.lua
   (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
  => [1 1 2 2 3 3]

  (!.py
   (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
  => [1 1 2 2 3 3]

  (!.R
   (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
  => [1 1 2 2 3 3])

^{:refer xt.lang.base-lib/arr-partition :added "4.0"}
(fact "partitions an array into arrays of length n"
  ^:hidden
  
  (!.js
   (k/arr-partition [1 2 3 4 5 6 7 8 9 10]
                    3))
  => [[1 2 3] [4 5 6] [7 8 9] [10]]

  (!.lua
   (k/arr-partition [1 2 3 4 5 6 7 8 9 10]
                    3))
  => [[1 2 3] [4 5 6] [7 8 9] [10]]

  (!.py
   (k/arr-partition [1 2 3 4 5 6 7 8 9 10]
                    3))
  => [[1 2 3] [4 5 6] [7 8 9] [10]]

  (!.R
   (k/arr-partition [1 2 3 4 5 6 7 8 9 10]
                    3))
  => [[1 2 3] [4 5 6] [7 8 9] [10]])

^{:refer xt.lang.base-lib/arr-filter :added "4.0"}
(fact "applies a filter across an array"
  ^:hidden
  
  (!.js
   (k/arr-filter [1 2 3 4 5] km/odd?))
  => [1 3 5]  

  (!.lua
   (k/arr-filter [1 2 3 4 5] km/odd?))
  => [1 3 5]
  
  (!.py
   (k/arr-filter [1 2 3 4 5] km/odd?))
  => [1 3 5]

  (!.R
   (k/arr-filter [1 2 3 4 5] km/odd?))
  => [1 3 5])

^{:refer xt.lang.base-lib/arr-keep :added "4.0"}
(fact "keeps items in an array if output is not nil"
  ^:hidden

  (!.js
   (k/arr-keep [1 2 3 4 5] (fn:> [x]
                                 (:? (km/odd? x) x))))
  => [1 3 5]

  (!.lua
   (k/arr-keep [1 2 3 4 5] (fn:> [x]
                                 (:? (km/odd? x) x))))
  => [1 3 5]
  
  (!.py
   (k/arr-keep [1 2 3 4 5] (fn:> [x]
                                 (:? (km/odd? x) x))))
  => [1 3 5]

  (!.R
   (k/arr-keep [1 2 3 4 5] (fn:> [x]
                                 (:? (km/odd? x) x))))
  => [1 3 5])

^{:refer xt.lang.base-lib/arr-keepf :added "4.0"}
(fact "keeps items in an array with transform if predicate holds"
  ^:hidden
  
  (!.js
   (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
  => [1 3 5]

  (!.lua
   (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
  => [1 3 5]

  (!.py
   (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
  => [1 3 5]

  (!.R
   (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
  => [1 3 5])

^{:refer xt.lang.base-lib/arr-juxt :added "4.0"}
(fact "constructs a map given a array of pairs"
  ^:hidden
  
  (!.js
   (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]]
               km/first
               km/second))
  => {"a" 1, "b" 2, "c" 3}

  (!.lua
   (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]]
               km/first
               km/second))
  => {"a" 1, "b" 2, "c" 3}

  (!.py
   (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]]
               km/first
               km/second))
  => {"a" 1, "b" 2, "c" 3}

  (!.R
   (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]]
               km/first
               km/second))
  => {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-foldl :added "4.0"}
(fact "performs reduce on an array"
  ^:hidden

  (!.js
   (k/arr-foldl [1 2 3 4 5] km/add 0))
  => 15

  (!.lua
   (k/arr-foldl [1 2 3 4 5] km/add 0))
  => 15

  (!.py
   (k/arr-foldl [1 2 3 4 5] km/add 0))
  => 15

  (!.R
   (k/arr-foldl [1 2 3 4 5] km/add 0))
  => 15)

^{:refer xt.lang.base-lib/arr-foldr :added "4.0"}
(fact "performs right reduce"
  ^:hidden

  (!.js
   (k/arr-foldr [1 2 3 4 5] k/step-push []))
  => [5 4 3 2 1]

  (!.lua
   (k/arr-foldr [1 2 3 4 5] k/step-push []))
  => [5 4 3 2 1]

  (!.py
   (k/arr-foldr [1 2 3 4 5] k/step-push []))
  => [5 4 3 2 1]

  (!.R
   (k/arr-foldr [1 2 3 4 5] k/step-push []))
  => [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-pipel :added "4.0"}
(fact "thrushes an input through a function pipeline"
  ^:hidden
  
  (!.js
   (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 20

  (!.lua
   (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 20

  (!.py
   (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 20

  (!.R
   (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 20)

^{:refer xt.lang.base-lib/arr-piper :added "4.0"}
(fact "thrushes an input through a function pipeline from reverse"
  ^:hidden
  
  (!.js
   (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 110

  (!.lua
   (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 110

  (!.py
   (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 110

  (!.R
   (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
  => 110)

^{:refer xt.lang.base-lib/arr-group-by :added "4.0"}
(fact "groups elements by key and view functions"
  ^:hidden
  
  (!.js
   (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]]
                   km/first
                   km/second))
  => {"a" [1 2], "b" [3 4]}

  (!.lua
   (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]]
                   km/first
                   km/second))
  => {"a" [1 2], "b" [3 4]}

  (!.py
   (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]]
                   km/first
                   km/second))
  => {"a" [1 2], "b" [3 4]}


  (!.R
   (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]]
                   km/first
                   km/second))
  => {"a" [1 2], "b" [3 4]})

^{:refer xt.lang.base-lib/arr-range :added "4.0"}
(fact "creates a range array"
  ^:hidden
  
  (!.js
   [(k/arr-range 10)
    (k/arr-range [10])
    (k/arr-range [2 8])
    (k/arr-range [2 9 2])])
  => [[0 1 2 3 4 5 6 7 8 9]
      [0 1 2 3 4 5 6 7 8 9]
      [2 3 4 5 6 7]
      [2 4 6 8]]

  (!.lua
   [(k/arr-range 10)
    (k/arr-range [10])
    (k/arr-range [2 8])
    (k/arr-range [2 9 2])])
  => [[0 1 2 3 4 5 6 7 8 9]
      [0 1 2 3 4 5 6 7 8 9]
      [2 3 4 5 6 7]
      [2 4 6 8]]
  
  (!.py
   [(k/arr-range 10)
    (k/arr-range [10])
    (k/arr-range [2 8])
    (k/arr-range [2 9 2])])
  => [[0 1 2 3 4 5 6 7 8 9]
      [0 1 2 3 4 5 6 7 8 9]
      [2 3 4 5 6 7]
      [2 4 6 8]]
  
  (!.R
    [(k/arr-range 10)
     (k/arr-range [10])
     (k/arr-range [2 8])
     (k/arr-range [2 9 2])])
  => [[0 1 2 3 4 5 6 7 8 9]
      [0 1 2 3 4 5 6 7 8 9]
      [2 3 4 5 6 7]
      [2 4 6 8]])

^{:refer xt.lang.base-lib/arr-intersection :added "4.0"}
(fact "gets the intersection of two arrays"
  ^:hidden

  (!.js (k/arr-intersection ["a" "b" "c" "d"]
                            ["c" "d" "e" "f"]))
  => ["c" "d"]

  (!.lua (k/arr-intersection ["a" "b" "c" "d"]
                             ["c" "d" "e" "f"]))
  => ["c" "d"]

  (!.py (k/arr-intersection ["a" "b" "c" "d"]
                            ["c" "d" "e" "f"]))
  => ["c" "d"]

  (!.R (k/arr-intersection ["a" "b" "c" "d"]
                            ["c" "d" "e" "f"]))
  => ["c" "d"])

^{:refer xt.lang.base-lib/arr-difference :added "4.0"}
(fact "gets the difference of two arrays"
  ^:hidden

  (!.js (k/arr-difference ["a" "b" "c" "d"]
                          ["c" "d" "e" "f"]))
  => ["e" "f"]
  
  (!.lua (k/arr-difference ["a" "b" "c" "d"]
                           ["c" "d" "e" "f"]))
  => ["e" "f"]
  
  (!.py (k/arr-difference ["a" "b" "c" "d"]
                          ["c" "d" "e" "f"]))
  => ["e" "f"]

  (!.R (k/arr-difference ["a" "b" "c" "d"]
                          ["c" "d" "e" "f"]))
  => ["e" "f"])

^{:refer xt.lang.base-lib/arr-union :added "4.0"}
(fact "gets the union of two arrays"
  ^:hidden

  (set (!.js (k/arr-union ["a" "b" "c" "d"]
                         ["c" "d" "e" "f"])))
  => #{"d" "f" "e" "a" "b" "c"}

  (set (!.lua (k/arr-union ["a" "b" "c" "d"]
                         ["c" "d" "e" "f"])))
  => #{"d" "f" "e" "a" "b" "c"}

  (set (!.py (k/arr-union ["a" "b" "c" "d"]
                         ["c" "d" "e" "f"])))
  => #{"d" "f" "e" "a" "b" "c"}

  (set (!.R (k/arr-union ["a" "b" "c" "d"]
                         ["c" "d" "e" "f"])))
  => #{"d" "f" "e" "a" "b" "c"})

^{:refer xt.lang.base-lib/arr-sort :added "4.0"}
(fact "arr-sort using key function and comparator"
  ^:hidden
  
  (!.js
   [(k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< a b)))
    (k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< b a)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/first  (fn:> [a b] (x:arr-str-comp a b)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/second (fn:> [a b] (< a b)))])
  => [[1 2 3 4] [4 3 2 1]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]]
  
  
  (!.lua
   [(k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< a b)))
    (k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< b a)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/first  (fn:> [a b] (x:arr-str-comp a b)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/second (fn:> [a b] (< a b)))])
  => [[1 2 3 4]
      [4 3 2 1]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]]

  (!.py
   [(k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< a b)))
    (k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< b a)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/first  (fn:> [a b] (x:arr-str-comp a b)))
    (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2] ] k/second (fn:> [a b] (< a b)))])
  => [[1 2 3 4]
      [4 3 2 1]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]
      [["a" 1] ["b" 2] ["c" 3] ["d" 4]]])

^{:refer xt.lang.base-lib/arr-sorted-merge :added "4.0"}
(fact "performs a merge on two sorted arrays"
  ^:hidden
  
  (!.js
   [(k/arr-sorted-merge [1 2 3] [4 5 6] k/lt)
    (k/arr-sorted-merge [1 2 4] [3 5 6] k/lt)
    (k/arr-sorted-merge (k/arr-reverse [1 2 4])
                        (k/arr-reverse [3 5 6]) k/gt)])
  => [[1 2 3 4 5 6]
      [1 2 3 4 5 6]
      [6 5 4 3 2 1]]

  (!.lua
   [(k/arr-sorted-merge [1 2 3] [4 5 6] k/lt)
    (k/arr-sorted-merge [1 2 4] [3 5 6] k/lt)
    (k/arr-sorted-merge (k/arr-reverse [1 2 4])
                        (k/arr-reverse [3 5 6]) k/gt)])
  => [[1 2 3 4 5 6]
      [1 2 3 4 5 6]
      [6 5 4 3 2 1]]

  (!.py
   [(k/arr-sorted-merge [1 2 3] [4 5 6] k/lt)
    (k/arr-sorted-merge [1 2 4] [3 5 6] k/lt)
    (k/arr-sorted-merge (k/arr-reverse [1 2 4])
                        (k/arr-reverse [3 5 6]) k/gt)])
  => [[1 2 3 4 5 6]
      [1 2 3 4 5 6]
      [6 5 4 3 2 1]])

^{:refer xt.lang.base-lib/arr-shuffle :added "4.0"}
(fact "shuffles the array"
  ^:hidden
  
  (set (!.js
        (k/arr-shuffle [1 2 3 4 5])))
  => #{1 4 3 2 5}

  (set (!.lua
        (k/arr-shuffle [1 2 3 4 5])))
  => #{1 4 3 2 5}

  (set (!.py
        (k/arr-shuffle [1 2 3 4 5])))
  => #{1 4 3 2 5}

  (set (!.R
        (k/arr-shuffle [1 2 3 4 5])))
  => #{1 4 3 2 5})

^{:refer xt.lang.base-lib/arr-pushl :added "4.0"}
(fact "pushs an element into array"
  ^:hidden
  
  (!.js
   (k/arr-pushl [1 2 3 4] 5 4))
  => [2 3 4 5]

  (!.lua
   (k/arr-pushl [1 2 3 4] 5 4))
  => [2 3 4 5]

  (!.py
   (k/arr-pushl [1 2 3 4] 5 4))
  => [2 3 4 5]

  (!.R
   (k/arr-pushl [1 2 3 4] 5 4))
  => [2 3 4 5])

^{:refer xt.lang.base-lib/arr-pushr :added "4.0"}
(fact "pushs an element into array"
  ^:hidden
  
  (!.js
   (k/arr-pushr [1 2 3 4] 5 4))
  => [5 1 2 3]

  (!.lua
   (k/arr-pushr [1 2 3 4] 5 4))
  => [5 1 2 3]

  (!.py
   (k/arr-pushr [1 2 3 4] 5 4))
  => [5 1 2 3]

  (!.R
   (k/arr-pushr [1 2 3 4] 5 4))
  => [5 1 2 3])

^{:refer xt.lang.base-lib/arr-join :added "4.0"}
(fact "joins array with string"
  ^:hidden
  
  (!.js
   (k/arr-join ["1" "2" "3" "4"] " "))
  => "1 2 3 4"
  
  (!.lua
   (k/arr-join ["1" "2" "3" "4"] " "))
  => "1 2 3 4"

  (!.py
   (k/arr-join ["1" "2" "3" "4"] " "))
  => "1 2 3 4"

  (!.R
   (k/arr-join ["1" "2" "3" "4"] " "))
  => "1 2 3 4")

^{:refer xt.lang.base-lib/arr-interpose :added "4.0"}
(fact "puts element between array"
  ^:hidden
  
  (!.js
   (k/arr-interpose ["1" "2" "3" "4"] "XX"))
  => ["1" "XX" "2" "XX" "3" "XX" "4"]
  
  (!.lua
   (k/arr-interpose ["1" "2" "3" "4"] "XX"))
  => ["1" "XX" "2" "XX" "3" "XX" "4"]

  (!.py
   (k/arr-interpose ["1" "2" "3" "4"] "XX"))
  => ["1" "XX" "2" "XX" "3" "XX" "4"]

  (!.R
   (k/arr-interpose ["1" "2" "3" "4"] "XX"))
  => ["1" "XX" "2" "XX" "3" "XX" "4"])

^{:refer xt.lang.base-lib/arr-repeat :added "4.0"}
(fact "repeat function or value n times"
  ^:hidden
  
  (!.js
   [(k/arr-repeat "1" 4)
    (k/arr-repeat (k/inc-fn -1)
                  4)])
  => [["1" "1" "1" "1"]
      [0 1 2 3]]

  (!.lua
   [(k/arr-repeat "1" 4)
    (k/arr-repeat (k/inc-fn -1)
                  4)])
  => [["1" "1" "1" "1"]
      [0 1 2 3]])

^{:refer xt.lang.base-lib/arr-random :added "4.0"}
(fact "gets a random element from array"
  ^:hidden
  
  (!.js
   (k/arr-random [1 2 3 4]))
  => #{1 2 3 4}

  (!.lua
   (k/arr-random [1 2 3 4]))
  => #{1 2 3 4}

  (!.py
   (k/arr-random [1 2 3 4]))
  => #{1 2 3 4}

  (!.R
   (k/arr-random [1 2 3 4]))
  => #{1 2 3 4})

^{:refer xt.lang.base-lib/arr-normalise :added "4.0"}
(fact "normalises array elements to 1"
  ^:hidden

  (!.js
   (k/arr-normalise [1 2 3 4]))
  => [0.1 0.2 0.3 0.4]
  
  (!.lua
   (k/arr-normalise [1 2 3 4]))
  => [0.1 0.2 0.3 0.4]

  (!.py
   (k/arr-normalise [1 2 3 4]))
  => [0.1 0.2 0.3 0.4]

  (!.R
   (k/arr-normalise [1 2 3 4]))
  => [0.1 0.2 0.3 0.4])

^{:refer xt.lang.base-lib/arr-sample :added "4.0"}
(fact "samples array according to probability"
  ^:hidden
  
  (!.js
   (k/arr-sample ["left" "right" "up" "down"]
                 [0.1 0.2 0.3 0.4]))
  => string?

  (!.lua
   (k/arr-sample ["left" "right" "up" "down"]
                 [0.1 0.2 0.3 0.4]))
  => string?

  (!.py
   (k/arr-sample ["left" "right" "up" "down"]
                 [0.1 0.2 0.3 0.4]))
  => string?

  (!.R
   (k/arr-sample ["left" "right" "up" "down"]
                 [0.1 0.2 0.3 0.4]))
  => string?)

^{:refer xt.lang.base-lib/arrayify :added "4.0"}
(fact "makes something into an array"
  ^:hidden

  (!.js
   [(k/arrayify 1)
    (k/arrayify [1])])
  => [[1] [1]]

  (!.lua
   [(k/arrayify 1)
    (k/arrayify [1])])
  => [[1] [1]]

  (!.py
   [(k/arrayify 1)
    (k/arrayify [1])])
  => [[1] [1]]

  (comment
    (!.R
     [(k/arrayify 1)
      (k/arrayify [1])])
    => [[1] [1]]))

^{:refer xt.lang.base-lib/obj-empty? :added "4.0"}
(fact "checks that object is empty"
  ^:hidden
  
  (!.js
   [(k/obj-empty? {})
    (k/obj-empty? {:a 1})])
  => [true false]

  (!.lua
   [(k/obj-empty? {})
    (k/obj-empty? {:a 1})])
  => [true false]

  (!.py
   [(k/obj-empty? {})
    (k/obj-empty? {:a 1})])
  => [true false]

  (!.R
   [(k/obj-empty? {})
    (k/obj-empty? {:a 1})])
  => [true false])

^{:refer xt.lang.base-lib/obj-not-empty? :added "4.0"}
(fact "checks that object is not empty"
  ^:hidden
  
  (!.js
   [(k/obj-not-empty? {})
    (k/obj-not-empty? {:a 1})])
  => [false true]

  (!.lua
   [(k/obj-not-empty? {})
    (k/obj-not-empty? {:a 1})])
  => [false true]

  (!.py
   [(k/obj-not-empty? {})
    (k/obj-not-empty? {:a 1})])
  => [false true]

  (!.R
   [(k/obj-not-empty? {})
    (k/obj-not-empty? {:a 1})])
  => [false true])

^{:refer xt.lang.base-lib/obj-first-key :added "4.0"}
(fact "gets the first key"
  ^:hidden
  
  (!.js
   (k/obj-first-key {:a 1}))
  => "a"

  (!.lua
   (k/obj-first-key {:a 1}))
  => "a"

  (!.py
   (k/obj-first-key {:a 1}))
  => "a"

  (!.R
   (k/obj-first-key {:a 1}))
  => "a")

^{:refer xt.lang.base-lib/obj-first-val :added "4.0"}
(fact "gets the first val"
  ^:hidden
  
  (!.js
   (k/obj-first-val {:a 1}))
  => 1

  (!.lua
   (k/obj-first-val {:a 1}))
  => 1

  (!.py
   (k/obj-first-val {:a 1}))
  => 1

  (!.R
   (k/obj-first-val {:a 1}))
  => 1)

^{:refer xt.lang.base-lib/obj-keys :added "4.0"}
(fact "gets keys of an object"
  ^:hidden

  (set (!.js
        (k/obj-keys {:a 1 :b 2})))
  => #{"a" "b"}
  
  (set (!.lua
        (k/obj-keys {:a 1 :b 2})))
  => #{"a" "b"}

  (set (!.py
        (k/obj-keys {:a 1 :b 2})))
  => #{"a" "b"}

  (set (!.R
        (k/obj-keys {:a 1 :b 2})))
  => #{"a" "b"})

^{:refer xt.lang.base-lib/obj-vals :added "4.0"}
(fact "gets vals of an object"
  ^:hidden

  (set (!.js
        (k/obj-vals {:a 1 :b 2})))
  => #{1 2}
  
  (set (!.lua
        (k/obj-vals {:a 1 :b 2})))
  => #{1 2}

  (set (!.py
        (k/obj-vals {:a 1 :b 2})))
  => #{1 2}

  (set (!.R
        (k/obj-vals {:a 1 :b 2})))
  => #{1 2})

^{:refer xt.lang.base-lib/obj-pairs :added "4.0"}
(fact "creates entry pairs from object"
  ^:hidden
  
  (set (!.js
        (k/obj-pairs {:a 1 :b 2 :c 2})))
  => #{["c" 2] ["b" 2] ["a" 1]}
  
  (set (!.lua
        (k/obj-pairs {:a 1 :b 2 :c 3})))
  => #{["b" 2] ["a" 1] ["c" 3]}

  (set (!.py
        (k/obj-pairs {:a 1 :b 2 :c 3})))
  => #{["b" 2] ["a" 1] ["c" 3]}

  (set (!.R
        (k/obj-pairs {:a 1 :b 2 :c 3})))
  => #{["b" 2] ["a" 1] ["c" 3]})

^{:refer xt.lang.base-lib/obj-clone :added "4.0"}
(fact "clones an object"
  ^:hidden
  
  (!.js
   (k/obj-clone {:a 1 :b 2 :c 3}))
  => {"a" 1, "b" 2, "c" 3}

  (!.lua
   (k/obj-clone {:a 1 :b 2 :c 3}))
  => {"a" 1, "b" 2, "c" 3}

  (!.py
   (k/obj-clone {:a 1 :b 2 :c 3}))
  => {"a" 1, "b" 2, "c" 3}

  (!.R
   (k/obj-clone {:a 1 :b 2 :c 3}))
  => {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign :added "4.0"}
(fact "merges key value pairs from into another"
  ^:hidden
  
  (!.js
   (var out := {:a 1})
   (var rout := out)
   (k/obj-assign out {:b 2 :c 3})
   rout)
  => {"a" 1, "b" 2, "c" 3}

  (!.lua
   (var out := {:a 1})
   (var rout := out)
   (k/obj-assign out {:b 2 :c 3})
   rout)
  => {"a" 1, "b" 2, "c" 3}

  (!.py
   (var out := {:a 1})
   (var rout := out)
   (k/obj-assign out {:b 2 :c 3})
   rout)
  => {"a" 1, "b" 2, "c" 3}

  ^:fails
  (!.R
   (var out := {:a 1})
   (var cout (k/obj-assign out {:b 2 :c 3}))
   [out cout])
  => [{"a" 1} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/obj-assign-nested :added "4.0"}
(fact "merges objects at a nesting level"
  ^:hidden
  
  (!.js
   [(k/obj-assign-nested {:a 1}
                       {:b 2})
    (k/obj-assign-nested {:a {:b {:c 1}}}
                       {:a {:b {:d 1}}})])
  => [{"a" 1, "b" 2}
      {"a" {"b" {"d" 1, "c" 1}}}]

  (!.lua
   [(k/obj-assign-nested {:a 1}
                       {:b 2})
    (k/obj-assign-nested {:a {:b {:c 1}}}
                       {:a {:b {:d 1}}})])
  => [{"a" 1, "b" 2}
      {"a" {"b" {"d" 1, "c" 1}}}]

  (!.py
   [(k/obj-assign-nested {:a 1}
                       {:b 2})
    (k/obj-assign-nested {:a {:b {:c 1}}}
                       {:a {:b {:d 1}}})])
  => [{"a" 1, "b" 2}
      {"a" {"b" {"d" 1, "c" 1}}}]

  ^:comment
  (!.R
   "NOT SUPPORTED"))

^{:refer xt.lang.base-lib/obj-assign-with :added "4.0"}
(fact "merges second into first given a function"
  ^:hidden
  
  (!.js
   (k/obj-assign-with {:a {:b true}}
                      {:a {:c true}}
                      k/obj-assign))
  => {"a" {"b" true, "c" true}}
  
  (!.lua
   (k/obj-assign-with {:a {:b true}}
                      {:a {:c true}}
                      k/obj-assign))
  => {"a" {"b" true, "c" true}}
  
  (!.py
   (k/obj-assign-with {:a {:b true}}
                      {:a {:c true}}
                      k/obj-assign))
  => {"a" {"b" true, "c" true}})

^{:refer xt.lang.base-lib/obj-from-pairs :added "4.0"}
(fact "creates an object from pairs"
  ^:hidden
  
  (!.js
   (k/obj-from-pairs (k/obj-pairs {:a 1 :b 2 :c 3})))
  => {"a" 1, "b" 2, "c" 3}
  
  (!.lua
   (k/obj-from-pairs (k/obj-pairs {:a 1 :b 2 :c 3})))
  => {"a" 1, "b" 2, "c" 3}

  (!.py
   (k/obj-from-pairs (k/obj-pairs {:a 1 :b 2 :c 3})))
  => {"a" 1, "b" 2, "c" 3}

  (!.R
   (k/obj-from-pairs (k/obj-pairs {:a 1 :b 2 :c 3})))
  => {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-del :added "4.0"}
(fact "deletes multiple keys"
  ^:hidden
  
  (!.js (k/obj-del {:a 1 :b 2 :c 3}
                   ["a" "b"]))
  => {"c" 3}
  
  (!.lua (k/obj-del {:a 1 :b 2 :c 3}
                    ["a" "b"]))
  => {"c" 3}
  
  (!.py (k/obj-del {:a 1 :b 2 :c 3}
                   ["a" "b"]))
  => {"c" 3}

  (!.R (k/obj-del {:a 1 :b 2 :c 3}
                  ["a" "b"]))
  => {"c" 3})

^{:refer xt.lang.base-lib/obj-del-all :added "4.0"}
(fact "deletes all keys"
  ^:hidden
  
  (!.js (k/obj-del-all {:a 1 :b 2 :c 3}))
  => {}
  
  (!.lua (k/obj-del-all {:a 1 :b 2 :c 3}))
  => {}
  
  (!.py (k/obj-del-all {:a 1 :b 2 :c 3}))
  => {}

  (!.R (k/obj-del-all {:a 1 :b 2 :c 3}))
  => {})

^{:refer xt.lang.base-lib/obj-pick :added "4.0"}
(fact "select keys in object"
  ^:hidden

  (!.js (k/obj-pick {:a 1 :b 2 :c 3}
                    ["a" "b"]))
  => {"a" 1, "b" 2}

  (!.lua (k/obj-pick {:a 1 :b 2 :c 3}
                     ["a" "b"]))
  => {"a" 1, "b" 2}

  (!.py (k/obj-pick {:a 1 :b 2 :c 3}
                    ["a" "b"]))
  => {"a" 1, "b" 2}

  (!.R (k/obj-pick {:a 1 :b 2 :c 3}
                   ["a" "b"]))
  => {"a" 1, "b" 2})

^{:refer xt.lang.base-lib/obj-omit :added "4.0"}
(fact "new object with missing keys"
  ^:hidden
  
  (!.js (k/obj-omit {:a 1 :b 2 :c 3}
                      ["a" "b"]))
  => {"c" 3}

  (!.lua (k/obj-omit {:a 1 :b 2 :c 3}
                      ["a" "b"]))
  => {"c" 3}

  (!.py (k/obj-omit {:a 1 :b 2 :c 3}
                      ["a" "b"]))
  => {"c" 3}

  (!.R (k/obj-omit {:a 1 :b 2 :c 3}
                      ["a" "b"]))
  => {"c" 3})

^{:refer xt.lang.base-lib/obj-transpose :added "4.0"}
(fact "obj-transposes a map"
  ^:hidden
  
  (!.js (k/obj-transpose {:a "x" :b "y" :c "z"}))
  => {"z" "c", "x" "a", "y" "b"}

  (!.lua (k/obj-transpose {:a "x" :b "y" :c "z"}))
  => {"z" "c", "x" "a", "y" "b"}

  (!.py (k/obj-transpose {:a "x" :b "y" :c "z"}))
  => {"z" "c", "x" "a", "y" "b"}

  (!.R (k/obj-transpose {:a "x" :b "y" :c "z"}))
  => {"z" "c", "x" "a", "y" "b"})

^{:refer xt.lang.base-lib/obj-nest :added "4.0"}
(fact "creates a nested object"
  ^:hidden

  (!.js (k/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}}

  (!.lua (k/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}}

  (!.py (k/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}}

  (!.R (k/obj-nest ["a" "b"] 1))
  => {"a" {"b" 1}})

^{:refer xt.lang.base-lib/obj-map :added "4.0"}
(fact "maps a function across the values of an object"
  ^:hidden
  
  (!.js
   (k/obj-map {:a 1 :b 2 :c 3}
               km/inc))
  => {"a" 2, "b" 3, "c" 4}

  (!.lua
   (k/obj-map {:a 1 :b 2 :c 3}
               km/inc))
  => {"a" 2, "b" 3, "c" 4}

  (!.py
   (k/obj-map {:a 1 :b 2 :c 3}
               km/inc))
  => {"a" 2, "b" 3, "c" 4}

  (!.R
   (k/obj-map {:a 1 :b 2 :c 3}
               km/inc))
  => {"a" 2, "b" 3, "c" 4})

^{:refer xt.lang.base-lib/obj-filter :added "4.0"}
(fact "applies a filter across the values of an object"
  ^:hidden
  
  (!.js
   (k/obj-filter {:a 1 :b 2 :c 3}
                  km/odd?))
  => {"a" 1, "c" 3}

  (!.lua
   (k/obj-filter {:a 1 :b 2 :c 3}
                  km/odd?))
  => {"a" 1, "c" 3}

  (!.py
   (k/obj-filter {:a 1 :b 2 :c 3}
                  km/odd?))
  => {"a" 1, "c" 3}

  (!.R
   (k/obj-filter {:a 1 :b 2 :c 3}
                  km/odd?))
  => {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keep :added "4.0"}
(fact "applies a transform across the values of an object, keeping non-nil values"
  ^:hidden
  
  (!.js
   (k/obj-keep {:a 1 :b 2 :c 3}
               (fn:> [x] (:? (km/odd? x) x))
               ))
  => {"a" 1, "c" 3}

  (!.lua
   (k/obj-keep {:a 1 :b 2 :c 3}
               (fn:> [x] (:? (km/odd? x) x))
               ))
  => {"a" 1, "c" 3}

  (!.py
   (k/obj-keep {:a 1 :b 2 :c 3}
               (fn:> [x] (:? (km/odd? x) x))
               ))
  => {"a" 1, "c" 3}

  (!.R
   (k/obj-keep {:a 1 :b 2 :c 3}
               (fn:> [x] (:? (km/odd? x) x))
               ))
  => {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keepf :added "4.0"}
(fact  "applies a transform and filter across the values of an object"
  ^:hidden
  
  (!.js
   (k/obj-keepf {:a 1 :b 2 :c 3}
               km/odd? k/identity))
  => {"a" 1, "c" 3}

  (!.lua
   (k/obj-keepf {:a 1 :b 2 :c 3}
               km/odd? k/identity))
  => {"a" 1, "c" 3}

  (!.py
   (k/obj-keepf {:a 1 :b 2 :c 3}
               km/odd? k/identity))
  => {"a" 1, "c" 3}

  (!.R
   (k/obj-keepf {:a 1 :b 2 :c 3}
                km/odd? k/identity))
  => {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-intersection :added "4.0"}
(fact "finds the intersection between map lookups"
  ^:hidden
  
  (!.js
   (k/obj-intersection {:a true :b true}
                       {:c true :b true}))
  => ["b"]

  (!.lua
   (k/obj-intersection {:a true :b true}
                       {:c true :b true}))
  => ["b"]
  
  (!.py
   (k/obj-intersection {:a true :b true}
                       {:c true :b true}))
  => ["b"]

  (!.R
   (k/obj-intersection {:a true :b true}
                       {:c true :b true}))
  => ["b"])

^{:refer xt.lang.base-lib/obj-difference :added "4.0"}
(fact "finds the difference between two map lookups"
  ^:hidden
  
  (!.js
   [(k/obj-difference {:a true :b true}
                      {:c true :b true})
    (k/obj-difference {:c true :b true}
                      {:a true :b true})])
  => [["c"] ["a"]]

  (!.lua
   [(k/obj-difference {:a true :b true}
                      {:c true :b true})
    (k/obj-difference {:c true :b true}
                      {:a true :b true})])
  => [["c"] ["a"]]

  (!.py
   [(k/obj-difference {:a true :b true}
                      {:c true :b true})
    (k/obj-difference {:c true :b true}
                      {:a true :b true})])
  => [["c"] ["a"]]

  (!.R
   [(k/obj-difference {:a true :b true}
                      {:c true :b true})
    (k/obj-difference {:c true :b true}
                      {:a true :b true})])
  => [["c"] ["a"]])

^{:refer xt.lang.base-lib/obj-keys-nested :added "4.0"}
(fact "gets nested keys"
  ^:hidden
  
  (!.js
   (k/obj-keys-nested {:a {:b {:c 1
                               :d 2}
                           :e {:f 4
                               :g 5}}}
                      []))
  => [[["a" "b" "c"] 1]
      [["a" "b" "d"] 2]
      [["a" "e" "f"] 4]
      [["a" "e" "g"] 5]]

  (set (!.lua
        (k/obj-keys-nested {:a {:b {:c 1
                                    :d 2}
                                :e {:f 4
                                    :g 5}}}
                           [])))
  => #{[["a" "e" "g"] 5]
       [["a" "b" "d"] 2]
       [["a" "e" "f"] 4]
       [["a" "b" "c"] 1]}
  
  (!.py
   (k/obj-keys-nested {:a {:b {:c 1
                               :d 2}
                           :e {:f 4
                               :g 5}}}
                      []))
  => [[["a" "b" "c"] 1]
      [["a" "b" "d"] 2]
      [["a" "e" "f"] 4]
      [["a" "e" "g"] 5]])

^{:refer xt.lang.base-lib/to-flat :added "4.0"}
(fact "flattens pairs of object into array"
  ^:hidden
  
  (!.js [(k/from-flat (k/to-flat {:a 1 :b 2 :c 3})
                      k/step-set-key
                      {})
         (k/from-flat (k/to-flat (k/obj-pairs {:a 1 :b 2 :c 3})) 
                      k/step-set-key
                      {})])
  => [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}]
  
  (!.lua [(k/from-flat (k/to-flat {:a 1 :b 2 :c 3})
                       k/step-set-key
                       {})
          (k/from-flat (k/to-flat (k/obj-pairs {:a 1 :b 2 :c 3})) 
                       k/step-set-key
                       {})])
  => [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}]
  
  (!.py [(k/from-flat (k/to-flat {:a 1 :b 2 :c 3})
                      k/step-set-key
                      {})
         (k/from-flat (k/to-flat (k/obj-pairs {:a 1 :b 2 :c 3})) 
                      k/step-set-key
                      {})])
  => [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}]
  
  (!.py [(k/from-flat (k/to-flat {:a 1 :b 2 :c 3})
                      k/step-set-key
                      {})
         (k/from-flat (k/to-flat (k/obj-pairs {:a 1 :b 2 :c 3})) 
                      k/step-set-key
                      {})])
  => [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}]

  (!.R [(k/from-flat (k/to-flat {:a 1 :b 2 :c 3})
                     k/step-set-key
                     {})
        (k/from-flat (k/to-flat (k/obj-pairs {:a 1 :b 2 :c 3})) 
                      k/step-set-key
                      {})])
  => [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/from-flat :added "4.0"}
(fact "creates object from flattened pair array"
  ^:hidden
  
  (!.js (k/from-flat ["a" 1, "b" 2, "c" 3]
                           k/step-set-key
                           {}))
  => {"a" 1, "b" 2, "c" 3}


  (!.lua (k/from-flat ["a" 1, "b" 2, "c" 3]
                            k/step-set-key
                           {}))
  => {"a" 1, "b" 2, "c" 3}
  
  
  (!.py (k/from-flat ["a" 1, "b" 2, "c" 3]
                           k/step-set-key
                           {}))
  => {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/get-in :added "4.0"}
(fact "gets item in object"
  ^:hidden
  
  (!.js (k/get-in {:a {:b {:c 1}}}
                  ["a" "b"]))
  => {"c" 1}
  
  (!.lua (k/get-in {:a {:b {:c 1}}}
                   ["a" "b"]))
  => {"c" 1}

  (!.py (k/get-in {:a {:b {:c 1}}}
                  ["a" "b"]))
  => {"c" 1}

  (!.R (k/get-in {:a {:b {:c 1}}}
                 ["a" "b"]))
  => {"c" 1})

^{:refer xt.lang.base-lib/set-in :added "4.0"}
(fact "sets item in object"
  ^:hidden
  
  [(!.js (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "b"] 2)
         a)

   (!.js (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "d"] 2)
         a)]
  => [{"a" {"b" 2}}
      {"a" {"d" 2, "b" {"c" 1}}}]

  [(!.lua (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "b"] 2)
         a)

   (!.lua (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "d"] 2)
         a)]
  => [{"a" {"b" 2}}
      {"a" {"d" 2, "b" {"c" 1}}}]

  [(!.py (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "b"] 2)
         a)

   (!.py (var a {:a {:b {:c 1}}})
         (k/set-in a ["a" "d"] 2)
         a)]
  => [{"a" {"b" 2}}
      {"a" {"d" 2, "b" {"c" 1}}}])

^{:refer xt.lang.base-lib/memoize-key :added "4.0"}
(fact "memoize for functions of single argument"
  ^:hidden
  
  (!.js
   (var cache [])
   (var f (fn [v]
            (x:arr-push cache v)
            (return v)))
   (var mf (k/memoize-key f))
   [(mf 1) (mf 2) (mf 1) (mf 1) cache])
  => [1 2 1 1 [1 2]]

  (!.lua
   (var cache [])
   (var f (fn [v]
            (x:arr-push cache v)
            (return v)))
   (var mf (k/memoize-key f))
   
   [(mf 1) (mf 2) (mf 1) (mf 1) cache])
  => [1 2 1 1 [1 2]]

  (!.py
   (var cache [])
   (var f (fn [v]
            (x:arr-push cache v)
            (return v)))
   (var mf (k/memoize-key f))
   [(mf 1) (mf 2) (mf 1) (mf 1) cache])
  => [1 2 1 1 [1 2]])

^{:refer xt.lang.base-lib/not-empty? :added "4.0"}
(fact "checks that array is not empty"
  ^:hidden
  
  (!.js [(k/not-empty? nil)
         (k/not-empty? "")
         (k/not-empty? "123")
         (k/not-empty? [])
         (k/not-empty? [1 2 3])
         (k/not-empty? {})
         (k/not-empty? {:a 1 :b 2})])
  => [false false true false true false true]

  (!.lua [(k/not-empty? nil)
         (k/not-empty? "")
         (k/not-empty? "123")
         (k/not-empty? [])
         (k/not-empty? [1 2 3])
         (k/not-empty? {})
         (k/not-empty? {:a 1 :b 2})])
  => [false false true false true false true]

  (!.py [(k/not-empty? nil)
         (k/not-empty? "")
         (k/not-empty? "123")
         (k/not-empty? [])
         (k/not-empty? [1 2 3])
         (k/not-empty? {})
         (k/not-empty? {:a 1 :b 2})])
  => [false false true false true false true]

  (!.R [(k/not-empty? nil)
        (k/not-empty? "")
        (k/not-empty? "123")
        (k/not-empty? [])
        (k/not-empty? [1 2 3])
        (k/not-empty? {})
        (k/not-empty? {:a 1 :b 2})])
  => [false false true false true false true])

^{:refer xt.lang.base-lib/eq-nested-loop :added "4.0"}
(fact "switch for nested check")

^{:refer xt.lang.base-lib/eq-nested-obj :added "4.0"}
(fact "checking object equality")

^{:refer xt.lang.base-lib/eq-nested-arr :added "4.0"}
(fact "checking aray equality")

^{:refer xt.lang.base-lib/eq-nested :added "4.0"}
(fact "checking for nested equality"
  ^:hidden
  
  (!.js
   [(k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 1}}})
    (k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 2}}})
    (k/eq-nested 1 1)
    (k/eq-nested 1 2)
    (k/eq-nested [1] [1])
    (k/eq-nested [1] [2])
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 1}}]})
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 2}}]})])
  => [true false true false true false true false]

  (!.js
   (var out {:a {:b 1}})
   (k/set-in out ["a" "c"] out)
   [(k/eq-nested out (k/get-in out ["a" "c"]))
    (k/eq-nested out (k/get-in out ["a"]))])
  => [true false]
  
  
  (!.lua
   [(k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 1}}})
    (k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 2}}})
    (k/eq-nested 1 1)
    (k/eq-nested 1 2)
    (k/eq-nested [1] [1])
    (k/eq-nested [1] [2])
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 1}}]})
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 2}}]})])
  => [true false true false true false true false]

  (!.lua
   (var out {:a {:b 1}})
   (k/set-in out ["a" "c"] out)
   [(k/eq-nested out (k/get-in out ["a" "c"]))
    (k/eq-nested out (k/get-in out ["a"]))])
  => [true false]


  (!.py
   [(k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 1}}})
    (k/eq-nested {:a {:b {:c 1}}}
                  {:a {:b {:c 2}}})
    (k/eq-nested 1 1)
    (k/eq-nested 1 2)
    (k/eq-nested [1] [1])
    (k/eq-nested [1] [2])
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 1}}]})
    (k/eq-nested {:a [{:b {:c 1}}]}
                  {:a [{:b {:c 2}}]})])
  => [true false true false true false true false]

  (!.py
   (var out {:a {:b 1}})
   (k/set-in out ["a" "c"] out)
   [(k/eq-nested out (k/get-in out ["a" "c"]))
    (k/eq-nested out (k/get-in out ["a"]))])
  => [true false])

^{:refer xt.lang.base-lib/obj-diff :added "4.0"}
(fact "diffs only keys within map"
  ^:hidden
  
  (!.js
   (k/obj-diff {:a 1 :b 2}
               {:a 1 :c 2}))
  => {"c" 2}

  (!.lua
   (k/obj-diff {:a 1 :b 2}
               {:a 1 :c 2}))
  => {"c" 2}
  
  (!.py
   (k/obj-diff {:a 1 :b 2}
               {:a 1 :c 2}))
  => {"c" 2})

^{:refer xt.lang.base-lib/obj-diff-nested :added "4.0"}
(fact  "diffs nested keys within map"
  ^:hidden

  (!.js
   [(k/obj-diff-nested {:a 1 :b 2}
                       {:a 1 :c 2})
    (k/obj-diff-nested {:a 1 :b {:c 3}}
                       {:a 1 :b {:d 3}})
    (k/obj-diff-nested {:a 1 :b {:c {:d 3}}}
                       {:a 1 :b {:c {:e 3}}})])
  => [{"c" 2}
      {"b" {"d" 3}}
      {"b" {"c" {"e" 3}}}]
  
  (!.lua
   [(k/obj-diff-nested {:a 1 :b 2}
                       {:a 1 :c 2})
    (k/obj-diff-nested {:a 1 :b {:c 3}}
                       {:a 1 :b {:d 3}})
    (k/obj-diff-nested {:a 1 :b {:c {:d 3}}}
                       {:a 1 :b {:c {:e 3}}})])
  => [{"c" 2}
      {"b" {"d" 3}}
      {"b" {"c" {"e" 3}}}]

  (!.py
   [(k/obj-diff-nested {:a 1 :b 2}
                       {:a 1 :c 2})
    (k/obj-diff-nested {:a 1 :b {:c 3}}
                       {:a 1 :b {:d 3}})
    (k/obj-diff-nested {:a 1 :b {:c {:d 3}}}
                       {:a 1 :b {:c {:e 3}}})])
  => [{"c" 2}
      {"b" {"d" 3}}
      {"b" {"c" {"e" 3}}}])

^{:refer xt.lang.base-lib/sort :added "4.0"}
(fact "dumb version of arr-sort")

^{:refer xt.lang.base-lib/objify :added "4.0"}
(fact "decodes object if string"
  ^:hidden
  
  (!.js
   (k/objify "{}"))
  => {}

  (!.lua
   (k/objify "{}"))
  => {}
  
  (!.py
   (k/objify "{}"))
  => {}
  
  (!.R
   (k/objify "{}"))
  => {})

^{:refer xt.lang.base-lib/template-entry :added "4.0"}
(fact "gets data from a structure using template"
  ^:hidden
  
  (!.js
   (k/template-entry {:a 1
                      :b 2}
                     ["a"]
                     {}))
  => 1

  (!.lua
   (k/template-entry {:a 1
                      :b 2}
                     ["a"]
                     {}))
  => 1

  (!.py
   (k/template-entry {:a 1
                      :b 2}
                     ["a"]
                     {}))
  => 1

  (!.R
   (k/template-entry {:a 1
                      :b 2}
                     ["a"]
                     {}))
  => 1)

^{:refer xt.lang.base-lib/template-fn :added "4.0"}
(fact "gets data from a structure using template"
  ^:hidden
  
  (!.js
   ((k/template-fn ["a"])
    {:a 1
     :b 2}
    {}))
  => 1

  (!.py
   ((k/template-fn ["a"])
    {:a 1
     :b 2}
    {}))
  => 1

  (!.lua
   ((k/template-fn ["a"])
    {:a 1
     :b 2}
    {}))
  => 1
  
  (!.R
   ((k/template-fn ["a"])
    {:a 1
     :b 2}
    {}))
  => 1)

^{:refer xt.lang.base-lib/template-multi :added "4.0"}
(fact "gets data from a structure using template"
  ^:hidden
  
  (!.js
   ((k/template-multi [["c"]
                       ["a"]])
    {:a 1
     :b 2}
    {}))
  => 1

  (!.lua
   ((k/template-multi [["c"]
                       ["a"]])
    {:a 1
     :b 2}
    {}))
  => 1

  (!.py
   ((k/template-multi [["c"]
                       ["a"]])
    {:a 1
     :b 2}
    {}))
  => 1

  (!.R
   ((k/template-multi [["c"]
                       ["a"]])
    {:a 1
     :b 2}
    {}))
  => 1)

^{:refer xt.lang.base-lib/sort-by :added "4.0"
  :setup [(def +data+
            [{:time 0
              :name "a"}
             {:time 2
              :name "a"}
             {:time 2
              :name "b"}
             {:time 0
              :name "b"}])
          (def +out+
            [[{"name" "a", "time" 0}
              {"name" "a", "time" 2}
              {"name" "b", "time" 0}
              {"name" "b", "time" 2}]
             [{"name" "b", "time" 2}
              {"name" "b", "time" 0}
              {"name" "a", "time" 2}
              {"name" "a", "time" 0}]
             [{"name" "a", "time" 0}
              {"name" "b", "time" 0}
              {"name" "a", "time" 2}
              {"name" "b", "time" 2}]
             [{"name" "a", "time" 2}
              {"name" "b", "time" 2}
              {"name" "a", "time" 0}
              {"name" "b", "time" 0}]])]}
(fact "sorts arrow by comparator"
  ^:hidden
  
  (!.js
   [(k/sort-by (@! +data+)
               ["name" "time"])
    (k/sort-by (@! +data+)
               [["name" true] ["time" true]])
    (k/sort-by (@! +data+)
               ["time" "name"])
    (k/sort-by (@! +data+)
               [(fn:> [e] (- (. e time)))
                "name"])])
  
  => +out+

  (!.lua
   [(k/sort-by (@! +data+)
               ["name" "time"])
    (k/sort-by (@! +data+)
               [["name" true] ["time" true]])
    (k/sort-by (@! +data+)
               ["time" "name"])
    (k/sort-by (@! +data+)
               [(fn:> [e] (- (. e time)))
                "name"])])
  => +out+

  
  (!.py
   [(k/sort-by (@! +data+)
               ["name" "time"])
    (k/sort-by (@! +data+)
               [["name" true] ["time" true]])
    (k/sort-by (@! +data+)
               ["time" "name"])
    (k/sort-by (@! +data+)
               [(fn:> [e] (- (k/get-key e "time")))
                "name"])])
  => +out+)

^{:refer xt.lang.base-lib/sort-edges-build :added "4.0"}
(fact "builds an edge with links"
  ^:hidden

  (!.js
   (var out {})
   (k/sort-edges-build out ["a" "b"])
   out)
  => {"a" {"id" "a", "links" ["b"]},
      "b" {"id" "b", "links" []}}

  (!.lua
   (var out {})
   (k/sort-edges-build out ["a" "b"])
   out)
  => {"a" {"id" "a", "links" ["b"]},
      "b" {"id" "b", "links" {}}}
  
  (!.py
   (var out {})
   (k/sort-edges-build out ["a" "b"])
   out)
  => {"a" {"id" "a", "links" ["b"]},
      "b" {"id" "b", "links" []}})

^{:refer xt.lang.base-lib/sort-edges-visit :added "4.0"}
(fact "walks over the list of edges")

^{:refer xt.lang.base-lib/sort-edges :added "4.0"}
(fact "sort edges given a list"
  ^:hidden

  (!.js
   (k/sort-edges [["a" "b"]
                  ["b" "c"]
                  ["c" "d"]
                  ["d" "e"]]))
  => ["a" "b" "c" "d" "e"]
  
  (!.lua
   (k/sort-edges [["a" "b"]
                  ["b" "c"]
                  ["c" "d"]
                  ["d" "e"]]))
  => ["a" "b" "c" "d" "e"]

  (!.py
   (k/sort-edges [["a" "b"]
                  ["b" "c"]
                  ["c" "d"]
                  ["d" "e"]]))
  => ["a" "b" "c" "d" "e"])

^{:refer xt.lang.base-lib/sort-topo :added "4.0"}
(fact "sorts in topological order"
  ^:hidden
  
  (!.js
   (k/sort-topo
    [["a" ["b" "c"]]
     ["c" ["b"]]]))
  => ["b" "c" "a"]
  
  (!.lua
   (k/sort-topo
    [["a" ["b" "c"]]
     ["c" ["b"]]]))
  => ["b" "c" "a"]

  (!.py
   (k/sort-topo
    [["a" ["b" "c"]]
     ["c" ["b"]]]))
  => ["b" "c" "a"])

^{:refer xt.lang.base-lib/clone-shallow :added "4.0"}
(fact "shallow clones an object or array"
  ^:hidden
  
  (!.js
   [(k/clone-shallow "a")
    (k/clone-shallow ["a" "b"])
    (k/clone-shallow {"a" "b"})])
  => ["a" ["a" "b"] {"a" "b"}]

  (!.lua
   [(k/clone-shallow "a")
    (k/clone-shallow ["a" "b"])
    (k/clone-shallow {"a" "b"})])
  => ["a" ["a" "b"] {"a" "b"}]

  (!.py
   [(k/clone-shallow "a")
    (k/clone-shallow ["a" "b"])
    (k/clone-shallow {"a" "b"})])
  => ["a" ["a" "b"] {"a" "b"}]

  (!.R
   [(k/clone-shallow "a")
    (k/clone-shallow ["a" "b"])
    (k/clone-shallow {"a" "b"})])
  => ["a" ["a" "b"] {"a" "b"}])

^{:refer xt.lang.base-lib/clone-nested-loop :added "4.0"}
(fact "clone nested objects loop")

^{:refer xt.lang.base-lib/clone-nested :added "4.0"}
(fact "cloning nested objects"
  ^:hidden
  
  (!.js
   (k/clone-nested {:a [1 2 3 {:b [4 5 6]}]}))
  => {"a" [1 2 3 {"b" [4 5 6]}]}
  
  (!.js
   (do:> (let [input {:a {:b [1 2 3 {:c [4 5 6]}]}}
               output (k/clone-nested input)]
          (return [(k/eq-nested input output)
                   (=== input output)
                   (=== (x:get-key input  "a")
                        (x:get-key output "a"))
                   (=== (. input  ["a"] ["b"])
                        (. output ["a"] ["b"]))
                   (=== (. input  ["a"] ["b"] [3])
                        (. output ["a"] ["b"] [3]))]))))
  => [true false false false false]

  (!.lua
   (k/clone-nested {:a [1 2 3 {:b [4 5 6]}]}))
  => {"a" [1 2 3 {"b" [4 5 6]}]}
  
  (!.lua
   (var input := {:a {:b [1 2 3 {:c [4 5 6]}]}})
   (var output := (k/clone-nested input))
   (k/eq-nested input output))
  => true
  
  (!.py
   (k/clone-nested {:a [1 2 3 {:b [4 5 6]}]}))
  => {"a" [1 2 3 {"b" [4 5 6]}]}
  
  (!.py
   (var input := {:a {:b [1 2 3 {:c [4 5 6]}]}})
   (var output := (k/clone-nested input))
   (k/eq-nested input output))
  => true)

^{:refer xt.lang.base-lib/wrap-callback :added "4.0"}
(fact "returns a wrapped callback given map"
  ^:hidden
  
  (!.js
   ((k/wrap-callback {:success (fn [i] (return (* 2 i)))}
                     "success")
    1))
  => 2

  (!.lua
   ((k/wrap-callback {:success (fn [i] (return (* 2 i)))}
                     "success")
    1))
  => 2

  (!.py
   ((k/wrap-callback {:success (fn [i] (return (* 2 i)))}
                     "success")
    1))
  => 2)

^{:refer xt.lang.base-lib/walk :added "4.0"}
(fact "walks over object"
  ^:hidden
  
  (!.js
   (k/walk [1 {:a {:b 3}}]
           (fn [x]
             (return (:? (k/is-number? x)
                         (+ x 1)
                         x)))
           k/identity))
  => [2 {"a" {"b" 4}}]

  (!.lua
   (k/walk [1 {:a {:b 3}}]
           (fn [x]
             (return (:? (k/is-number? x)
                         (+ x 1)
                         x)))
           k/identity))
  => [2 {"a" {"b" 4}}]

  (!.py
   (k/walk [1 {:a {:b 3}}]
           (fn [x]
             (return (:? (k/is-number? x)
                         (+ x 1)
                         x)))
           k/identity))
  => [2 {"a" {"b" 4}}]

  (!.R
   (k/walk [1 {:a {:b 3}}]
           (fn [x]
             (return (:? (k/is-number? x)
                         (+ x 1)
                         x)))
           k/identity))
  => [2 {"a" {"b" 4}}])

^{:refer xt.lang.base-lib/get-data :added "4.0"
  :setup [(def +in+
            '{:a 1
             :b "hello"
             :c {:d [1 2 (fn:>)]
                 :e "hello"
                 :f {:g (fn:>)
                     :h 2}}})
          (def +out+ {"a" 1,
                      "b" "hello",
                      "c"
                      {"d" [1 2 "<function>"],
                       "f" {"g" "<function>", "h" 2},
                       "e" "hello"}})]}
(fact "gets only data (for use with json)"
  ^:hidden
  
  (!.js
   (k/get-data (@! +in+)))
  => +out+

  (!.lua
   (k/get-data (@! +in+)))
  => +out+

  (!.py
   (k/get-data (@! +in+)))
  => +out+

  (!.R
   (k/get-data (@! +in+)))
  => +out+)

^{:refer xt.lang.base-lib/get-spec :added "4.0"
  :setup [(def +in+
            '{:a 1
             :b "hello"
             :c {:d [1 2 (fn:>)]
                 :e "hello"
                 :f {:g (fn:>)
                     :h 2}}})
          (def +out+
            {"a" "number",
             "b" "string",
             "c"
             {"d" ["number" "number" "function"],
              "f" {"g" "function", "h" "number"},
              "e" "string"}})]}
(fact "creates a get-spec of a datastructure"
  ^:hidden

  (!.js
   (k/get-spec (@! +in+)))
  
  => +out+

  (!.lua
   (k/get-spec (@! +in+)))
  => +out+

  (!.py
   (k/get-spec (@! +in+)))
  => +out+

  (!.R
   (k/get-spec (@! +in+)))
  => +out+)

^{:refer xt.lang.base-lib/split-long :added "4.0"
  :setup [(def +s+ (apply str (repeat 5 "1234567890")))
          (def +out+ ["1234567890"
                      "1234567890"
                      "1234567890"
                      "1234567890"
                      "1234567890"])]}
(fact "splits a long string"
  ^:hidden
  
  (!.js
   (k/split-long (@! +s+)
                 10))
  => +out+

  (!.lua
   (k/split-long (@! +s+)
                 10))
  => +out+

  (!.py
   (k/split-long (@! +s+)
                 10))
  => +out+)

^{:refer xt.lang.base-lib/proto-spec :added "4.0"}
(fact "creates the spec map from interface definitions")

^{:refer xt.lang.base-lib/with-delay :added "4.0"}
(fact "sets a delay")

^{:refer xt.lang.base-lib/meta:info-fn :added "4.0"}
(fact "the function to get meta info")

^{:refer xt.lang.base-lib/meta:info :added "4.0"}
(fact "macro to inject meta info")

^{:refer xt.lang.base-lib/LOG! :added "4.0"}
(fact "logging with meta info")

^{:refer xt.lang.base-lib/trace-log :added "4.0"}
(fact "gets the current trace log"
  ^:hidden
  
  (!.js
   (k/trace-log))
  => vector?

  (!.lua
   (k/trace-log))
  => coll?

  (!.py
   (k/trace-log-clear))
  => vector?)

^{:refer xt.lang.base-lib/trace-log-clear :added "4.0"}
(fact "resets the trace log"
  ^:hidden
  
  (!.js
   (k/trace-log-clear))
  => []

  (!.lua
   (k/trace-log-clear))
  => {}

  (!.py
   (k/trace-log-clear))
  => [])

^{:refer xt.lang.base-lib/trace-log-add :added "4.0"}
(fact "adds an entry to the log"
  ^:hidden
  
  (!.js
   (k/trace-log-add "hello" "hello" {}))
  => number?

  (!.lua
   (k/trace-log-add "hello" "hello" {}))
  => number?

  (!.py
   (k/trace-log-add "hello" "hello" {}))
  => number?)

^{:refer xt.lang.base-lib/trace-filter :added "4.0"}
(fact "filters out traced entries"
  ^:hidden
  
  (!.js
   (k/trace-filter "hello"))
  => vector?

  (!.lua
   (k/trace-filter "hello"))
  => vector?

  (!.py
   (k/trace-filter "hello"))
  => vector?)

^{:refer xt.lang.base-lib/trace-last-entry :added "4.0"}
(fact "gets the last entry"
  ^:hidden
  
  (!.js
   (k/trace-last-entry "hello"))
  => map?

  (!.lua
   (k/trace-last-entry "hello"))
  => map?

  (!.py
   (k/trace-last-entry "hello"))
  => map?)

^{:refer xt.lang.base-lib/trace-data :added "4.0"}
(fact "gets the trace data"
  ^:hidden
  
  (!.js
   (k/trace-data "hello"))
  => vector?
  
  (!.lua
   (k/trace-data "hello"))
  => coll?
  
  (!.py
   (k/trace-data "hello"))
  => vector?)

^{:refer xt.lang.base-lib/trace-last :added "4.0"}
(fact "gets the last value"
  ^:hidden
  
  (!.js
   (k/trace-last "hello"))
  => "hello"
  
  (!.lua
   (k/trace-last "hello"))
  => "hello"

  (!.py
   (k/trace-last "hello"))
  => "hello")

^{:refer xt.lang.base-lib/TRACE! :added "4.0"}
(fact "performs a trace call"
  ^:hidden
  
  (!.js
   (k/TRACE! "hello" "hello")
   (k/trace-last-entry))
  => (contains {"tag" "hello", "time" integer?, "line" integer?, "column" integer?,
                "data" "hello", "ns" "xt.lang.base-lib-test"})

  (!.lua
   (k/TRACE! "hello" "hello")
   (k/trace-last-entry))
  => (contains {"tag" "hello", "time" integer?, "line" integer?, "column" integer?,
                "data" "hello", "ns" "xt.lang.base-lib-test"})

  (!.py
   (k/TRACE! "hello" "hello")
   (k/trace-last-entry nil))
  => (contains {"tag" "hello", "time" integer?, "line" integer?, "column" integer?,
                "data" "hello", "ns" "xt.lang.base-lib-test"}))

^{:refer xt.lang.base-lib/trace-run :added "4.0"}
(fact "run helper for `RUN!` macro")

^{:refer xt.lang.base-lib/RUN! :added "4.0"}
(fact "runs a form, saving trace forms"
  ^:hidden
  
  (!.js
   (k/RUN!
    (k/TRACE! 1)
    (k/TRACE! 2)))
  => (contains-in [{"data" 1}
                   {"data" 2}])
  
  (!.lua
   (k/RUN!
    (k/TRACE! 1)
    (k/TRACE! 2)))
  => (contains-in [{"data" 1}
                   {"data" 2}])
  
  ;; DOES NOT WORK
  (!.py
   (k/RUN!
    (k/TRACE! 1)
    (k/TRACE! 2)))
  => (contains-in [{"data" 1}
                   {"data" 2}]))

(comment)


^{:refer xt.lang.base-lib/path-fn :added "4.0"}
(fact "creates a function that accesses a path in an object")

^{:refer xt.lang.base-lib/eq-shallow :added "4.0"}
(fact "checks for shallow equality")

^{:refer xt.lang.base-lib/str-rand :added "4.0"}
(fact "generates a random string of length n")