(ns xt.lang.base-lib-js-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.string.prose :as prose]))

(l/script- :js
  {:runtime :basic,
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-macro :as km]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-lib/proto-create, :added "4.0"}
(fact "creates the prototype map"
  (!.js
    (var mt (k/proto-create {:hello (fn:> [v] (. v world)), :world "hello"}))
    (var a {})
    (k/set-proto a mt)
    (. a (hello)))
  => "hello")

^{:refer xt.lang.base-lib/type-native, :added "4.0"}
(fact "gets the native type"
 
 (!.js
  [(k/type-native {})
   (k/type-native [1])
   (k/type-native (fn []))
   (k/type-native 1)
   (k/type-native "")
   (k/type-native true)])
 =>
 ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.base-lib/type-class, :added "4.0"}
(fact
 "gets the type of an object"
 
 (!.js
  [(k/type-class {})
   (k/type-class [1])
   (k/type-class (fn []))
   (k/type-class 1)
   (k/type-class "")
   (k/type-class true)])
 =>
 ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.base-lib/fn?, :added "4.0"}
(fact
 "checks if object is a function type"
 
 (!.js [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
 =>
 [true true false])

^{:refer xt.lang.base-lib/arr?, :added "4.0"}
(fact
 "checks if object is an array"
 
 (!.js [(k/arr? [1 2 3]) (k/arr? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.base-lib/obj?, :added "4.0"}
(fact
 "checks if object is a map type"
 
 (!.js [(k/obj? {:a 1}) (k/obj? [1 2 3])])
 =>
 [true false])

^{:refer xt.lang.base-lib/identity, :added "4.0"}
(fact "identity function"  (!.js (k/identity 1)) => 1)

^{:refer xt.lang.base-lib/step-set-fn, :added "4.0"}
(fact "creates a set key function"  (!.js ((k/step-set-fn {} "a") 1)) => {"a" 1})

^{:refer xt.lang.base-lib/starts-with?, :added "4.0"}
(fact "check for starts with"  (!.js (k/starts-with? "Foo Bar" "Foo")) => true)

^{:refer xt.lang.base-lib/ends-with?, :added "4.0"}
(fact "check for ends with"  (!.js (k/ends-with? "Foo Bar" "Bar")) => true)

^{:refer xt.lang.base-lib/capitalize, :added "4.0"}
(fact "uppercases the first letter"  (!.js (k/capitalize "hello")) => "Hello")

^{:refer xt.lang.base-lib/decapitalize, :added "4.0"}
(fact "lowercases the first letter"  (!.js (k/decapitalize "HELLO")) => "hELLO")

^{:refer xt.lang.base-lib/pad-left, :added "4.0"}
(fact "pads string with n chars on left"  (!.js (k/pad-left "000" 5 "-")) => "--000")

^{:refer xt.lang.base-lib/pad-right, :added "4.0"}
(fact
 "pads string with n chars on right"
 
 (!.js (k/pad-right "000" 5 "-"))
 =>
 "000--")

^{:refer xt.lang.base-lib/pad-lines, :added "4.0"}
(fact
 "pad lines with starting chars"
 
 (!.js (k/pad-lines (k/join "\n" ["hello" "world"]) 2 " "))
 =>
 (prose/| "  hello" "  world"))

^{:refer xt.lang.base-lib/mod-pos, :added "4.0"}
(fact "gets the positive mod"  (!.js [(mod -11 10) (k/mod-pos -11 10)]) => [-1 9])

^{:refer xt.lang.base-lib/mod-offset, :added "4.0"}
(fact
 "calculates the closet offset"
 
 (!.js
  [(k/mod-offset 20 280 360)
   (k/mod-offset 280 20 360)
   (k/mod-offset 280 -80 360)
   (k/mod-offset 20 -60 360)
   (k/mod-offset 60 30 360)])
 =>
 [-100 100 0 -80 -30])

^{:refer xt.lang.base-lib/gcd, :added "4.0"}
(fact "greatest common denominator"  (!.js (k/gcd 10 6)) => 2)

^{:refer xt.lang.base-lib/lcm, :added "4.0"}
(fact "lowest common multiple"  (!.js (k/lcm 10 6)) => 30)

^{:refer xt.lang.base-lib/mix, :added "4.0"}
(fact "mixes two values with a fraction"  (!.js (k/mix 100 20 0.1)) => 92)

^{:refer xt.lang.base-lib/sign, :added "4.0"}
(fact "gets the sign of "  (!.js [(k/sign -10) (k/sign 10)]) => [-1 1])

^{:refer xt.lang.base-lib/round, :added "4.0"}
(fact
 "rounds to the nearest integer"
 
 (!.js [(k/round 0.9) (k/round 1.1) (k/round 1.49) (k/round 1.51)])
 =>
 [1 1 1 2])

^{:refer xt.lang.base-lib/clamp, :added "4.0"}
(fact
 "clamps a value between min and max"
 
 (!.js [(k/clamp 0 5 6) (k/clamp 0 5 -1) (k/clamp 0 5 4)])
 =>
 [5 0 4])

^{:refer xt.lang.base-lib/bit-count, :added "4.0"}
(fact
 "get the bit count"
 
 (!.js [(k/bit-count 16) (k/bit-count 10) (k/bit-count 3) (k/bit-count 7)])
 =>
 [1 2 2 3])

^{:refer xt.lang.base-lib/sym-full, :added "4.0"}
(fact "creates a sym"  (!.js (k/sym-full "hello" "world")) => "hello/world")

^{:refer xt.lang.base-lib/sym-name, :added "4.0"}
(fact "gets the name part of the sym"  (!.js (k/sym-name "hello/world")) => "world")

^{:refer xt.lang.base-lib/sym-ns, :added "4.0"}
(fact
 "gets the namespace part of the sym"
 
 (!.js [(k/sym-ns "hello/world") (k/sym-ns "hello")])
 =>
 ["hello" nil])

^{:refer xt.lang.base-lib/sym-pair, :added "4.0"}
(fact "gets the sym pair"  (!.js (k/sym-pair "hello/world")) => ["hello" "world"])

^{:refer xt.lang.base-lib/is-empty?, :added "4.0"}
(fact
 "checks that array is empty"
 
 (!.js
  [(k/is-empty? nil)
   (k/is-empty? "")
   (k/is-empty? "123")
   (k/is-empty? [])
   (k/is-empty? [1 2 3])
   (k/is-empty? {})
   (k/is-empty? {:a 1, :b 2})])
 =>
 [true true false true false true false])

^{:refer xt.lang.base-lib/arr-lookup, :added "4.0"}
(fact
 "constructs a lookup given keys"
 
 (!.js (k/arr-lookup ["a" "b" "c"]))
 =>
 {"a" true, "b" true, "c" true})

^{:refer xt.lang.base-lib/arr-every, :added "4.0"}
(fact
 "checks that every element fulfills thet predicate"
 
 (!.js [(k/arr-every [1 2 3] km/odd?) (k/arr-every [1 3] km/odd?)])
 =>
 [false true])

^{:refer xt.lang.base-lib/arr-some, :added "4.0"}
(fact
 "checks that the array contains an element"
 
 (!.js [(k/arr-some [1 2 3] km/even?) (k/arr-some [1 3] km/even?)])
 =>
 [true false])

^{:refer xt.lang.base-lib/arr-each, :added "4.0"}
(fact
 "performs a function call for each element"
 
 (!.js (var a := []) (k/arr-each [1 2 3 4 5] (fn [e] (x:arr-push a (+ 1 e)))) a)
 =>
 [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-omit, :added "4.0"}
(fact
 "emits index from new array"
 
 (!.js (k/arr-omit ["a" "b" "c" "d"] 2))
 =>
 ["a" "b" "d"])

^{:refer xt.lang.base-lib/arr-reverse, :added "4.0"}
(fact "reverses the array"  (!.js (k/arr-reverse [1 2 3 4 5])) => [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-find, :added "4.0"}
(fact
 "finds first index matching predicate"
 
 (!.js (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
 =>
 2)

^{:refer xt.lang.base-lib/arr-zip, :added "4.0"}
(fact
 "zips two arrays together into a map"
 
 (!.js (k/arr-zip ["a" "b" "c"] [1 2 3]))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-map, :added "4.0"}
(fact
 "maps a function across an array"
 
 (!.js (k/arr-map [1 2 3 4 5] km/inc))
 =>
 [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-clone, :added "4.0"}
(fact "clones an array"  (!.js (k/arr-clone [1 2 3])) => [1 2 3])

^{:refer xt.lang.base-lib/arr-append, :added "4.0"}
(fact
 "appends to the end of an array"
 
 (!.js (var out := [1 2 3]) (k/arr-append out [4 5]) out)
 =>
 [1 2 3 4 5])

^{:refer xt.lang.base-lib/arr-slice, :added "4.0"}
(fact "slices an array"  (!.js (k/arr-slice [1 2 3 4 5] 1 3)) => [2 3])

^{:refer xt.lang.base-lib/arr-rslice, :added "4.0"}
(fact "gets the reverse of a slice"  (!.js (k/arr-rslice [1 2 3 4 5] 1 3)) => [3 2])

^{:refer xt.lang.base-lib/arr-tail, :added "4.0"}
(fact "gets the tail of the array"  (!.js (k/arr-tail [1 2 3 4 5] 3)) => [5 4 3])

^{:refer xt.lang.base-lib/arr-mapcat, :added "4.0"}
(fact
 "maps an array function, concatenting results"
 
 (!.js (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
 =>
 [1 1 2 2 3 3])

^{:refer xt.lang.base-lib/arr-partition, :added "4.0"}
(fact
 "partitions an array into arrays of length n"
 
 (!.js (k/arr-partition [1 2 3 4 5 6 7 8 9 10] 3))
 =>
 [[1 2 3] [4 5 6] [7 8 9] [10]])

^{:refer xt.lang.base-lib/arr-filter, :added "4.0"}
(fact
 "applies a filter across an array"
 
 (!.js (k/arr-filter [1 2 3 4 5] km/odd?))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keep, :added "4.0"}
(fact
 "keeps items in an array if output is not nil"
 
 (!.js (k/arr-keep [1 2 3 4 5] (fn:> [x] (:? (km/odd? x) x))))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keepf, :added "4.0"}
(fact
 "keeps items in an array with transform if predicate holds"
 
 (!.js (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-juxt, :added "4.0"}
(fact
 "constructs a map given a array of pairs"
 
 (!.js (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]] km/first km/second))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-foldl, :added "4.0"}
(fact "performs reduce on an array"  (!.js (k/arr-foldl [1 2 3 4 5] km/add 0)) => 15)

^{:refer xt.lang.base-lib/arr-foldr, :added "4.0"}
(fact
 "performs right reduce"
 
 (!.js (k/arr-foldr [1 2 3 4 5] k/step-push []))
 =>
 [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-pipel, :added "4.0"}
(fact
 "thrushes an input through a function pipeline"
 
 (!.js (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 20)

^{:refer xt.lang.base-lib/arr-piper, :added "4.0"}
(fact
 "thrushes an input through a function pipeline from reverse"
 
 (!.js (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 110)

^{:refer xt.lang.base-lib/arr-group-by, :added "4.0"}
(fact
 "groups elements by key and view functions"
 
 (!.js (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]] km/first km/second))
 =>
 {"a" [1 2], "b" [3 4]})

^{:refer xt.lang.base-lib/arr-range, :added "4.0"}
(fact
 "creates a range array"
 
 (!.js [(k/arr-range 10) (k/arr-range [10]) (k/arr-range [2 8]) (k/arr-range [2 9 2])])
 =>
 [[0 1 2 3 4 5 6 7 8 9] [0 1 2 3 4 5 6 7 8 9] [2 3 4 5 6 7] [2 4 6 8]])

^{:refer xt.lang.base-lib/arr-intersection, :added "4.0"}
(fact
 "gets the intersection of two arrays"
 
 (!.js (k/arr-intersection ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["c" "d"])

^{:refer xt.lang.base-lib/arr-difference, :added "4.0"}
(fact
 "gets the difference of two arrays"
 
 (!.js (k/arr-difference ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["e" "f"])

^{:refer xt.lang.base-lib/arr-union, :added "4.0"}
(fact
 "gets the union of two arrays"
 
 (set (!.js (k/arr-union ["a" "b" "c" "d"] ["c" "d" "e" "f"])))
 =>
 #{"d" "f" "e" "a" "b" "c"})

^{:refer xt.lang.base-lib/arr-sort, :added "4.0"}
(fact
 "arr-sort using key function and comparator"
 
 (!.js
  [(k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< a b)))
   (k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< b a)))
   (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2]] k/first (fn:> [a b] (x:arr-str-comp a b)))
   (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2]] k/second (fn:> [a b] (< a b)))])
 =>
 [[1 2 3 4] [4 3 2 1] [["a" 1] ["b" 2] ["c" 3] ["d" 4]] [["a" 1] ["b" 2] ["c" 3] ["d" 4]]])

^{:refer xt.lang.base-lib/arr-sorted-merge, :added "4.0"}
(fact
 "performs a merge on two sorted arrays"
 
 (!.js
  [(k/arr-sorted-merge [1 2 3] [4 5 6] k/lt)
   (k/arr-sorted-merge [1 2 4] [3 5 6] k/lt)
   (k/arr-sorted-merge (k/arr-reverse [1 2 4]) (k/arr-reverse [3 5 6]) k/gt)])
 =>
 [[1 2 3 4 5 6] [1 2 3 4 5 6] [6 5 4 3 2 1]])

^{:refer xt.lang.base-lib/arr-shuffle, :added "4.0"}
(fact "shuffles the array"  (set (!.js (k/arr-shuffle [1 2 3 4 5]))) => #{1 4 3 2 5})

^{:refer xt.lang.base-lib/arr-pushl, :added "4.0"}
(fact "pushs an element into array"  (!.js (k/arr-pushl [1 2 3 4] 5 4)) => [2 3 4 5])

^{:refer xt.lang.base-lib/arr-pushr, :added "4.0"}
(fact "pushs an element into array"  (!.js (k/arr-pushr [1 2 3 4] 5 4)) => [5 1 2 3])

^{:refer xt.lang.base-lib/arr-join, :added "4.0"}
(fact
 "joins array with string"
 
 (!.js (k/arr-join ["1" "2" "3" "4"] " "))
 =>
 "1 2 3 4")

^{:refer xt.lang.base-lib/arr-interpose, :added "4.0"}
(fact
 "puts element between array"
 
 (!.js (k/arr-interpose ["1" "2" "3" "4"] "XX"))
 =>
 ["1" "XX" "2" "XX" "3" "XX" "4"])

^{:refer xt.lang.base-lib/arr-repeat, :added "4.0"}
(fact
 "repeat function or value n times"
 
 (!.js [(k/arr-repeat "1" 4) (k/arr-repeat (k/inc-fn -1) 4)])
 =>
 [["1" "1" "1" "1"] [0 1 2 3]])

^{:refer xt.lang.base-lib/arr-random, :added "4.0"}
(fact
 "gets a random element from array"
 
 (!.js (k/arr-random [1 2 3 4]))
 =>
 #{1 4 3 2})

^{:refer xt.lang.base-lib/arr-normalise, :added "4.0"}
(fact
 "normalises array elements to 1"
 
 (!.js (k/arr-normalise [1 2 3 4]))
 =>
 [0.1 0.2 0.3 0.4])

^{:refer xt.lang.base-lib/arr-sample, :added "4.0"}
(fact
 "samples array according to probability"
 
 (!.js (k/arr-sample ["left" "right" "up" "down"] [0.1 0.2 0.3 0.4]))
 =>
 string?)

^{:refer xt.lang.base-lib/arrayify, :added "4.0"}
(fact
 "makes something into an array"
 (comment (!.R [(k/arrayify 1) (k/arrayify [1])]) => [[1] [1]])
 
 (!.js [(k/arrayify 1) (k/arrayify [1])])
 =>
 [[1] [1]])

^{:refer xt.lang.base-lib/obj-empty?, :added "4.0"}
(fact
 "checks that object is empty"
 
 (!.js [(k/obj-empty? {}) (k/obj-empty? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.base-lib/obj-not-empty?, :added "4.0"}
(fact
 "checks that object is not empty"
 
 (!.js [(k/obj-not-empty? {}) (k/obj-not-empty? {:a 1})])
 =>
 [false true])

^{:refer xt.lang.base-lib/obj-first-key, :added "4.0"}
(fact "gets the first key"  (!.js (k/obj-first-key {:a 1})) => "a")

^{:refer xt.lang.base-lib/obj-first-val, :added "4.0"}
(fact "gets the first val"  (!.js (k/obj-first-val {:a 1})) => 1)

^{:refer xt.lang.base-lib/obj-keys, :added "4.0"}
(fact "gets keys of an object"  (set (!.js (k/obj-keys {:a 1, :b 2}))) => #{"a" "b"})

^{:refer xt.lang.base-lib/obj-vals, :added "4.0"}
(fact "gets vals of an object"  (set (!.js (k/obj-vals {:a 1, :b 2}))) => #{1 2})

^{:refer xt.lang.base-lib/obj-pairs, :added "4.0"}
(fact
 "creates entry pairs from object"
 
 (set (!.js (k/obj-pairs {:a 1, :b 2, :c 2})))
 =>
 #{["c" 2] ["b" 2] ["a" 1]})

^{:refer xt.lang.base-lib/obj-clone, :added "4.0"}
(fact
 "clones an object"
 
 (!.js (k/obj-clone {:a 1, :b 2, :c 3}))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign, :added "4.0"}
(fact
 "merges key value pairs from into another"
 
 (!.js (var out := {:a 1}) (var rout := out) (k/obj-assign out {:b 2, :c 3}) rout)
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign-nested, :added "4.0"}
(fact "merges objects at a nesting level"
  
  (!.js
    [(k/obj-assign-nested {:a 1} {:b 2}) (k/obj-assign-nested {:a {:b {:c 1}}} {:a {:b {:d 1}}})])
  =>
  [{"a" 1, "b" 2} {"a" {"b" {"d" 1, "c" 1}}}])

^{:refer xt.lang.base-lib/obj-assign-with, :added "4.0"}
(fact
 "merges second into first given a function"
 
 (!.js (k/obj-assign-with {:a {:b true}} {:a {:c true}} k/obj-assign))
 =>
 {"a" {"b" true, "c" true}})

^{:refer xt.lang.base-lib/obj-from-pairs, :added "4.0"}
(fact
 "creates an object from pairs"
 
 (!.js (k/obj-from-pairs (k/obj-pairs {:a 1, :b 2, :c 3})))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-del, :added "4.0"}
(fact
 "deletes multiple keys"
 
 (!.js (k/obj-del {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-del-all, :added "4.0"}
(fact "deletes all keys"  (!.js (k/obj-del-all {:a 1, :b 2, :c 3})) => {})

^{:refer xt.lang.base-lib/obj-pick, :added "4.0"}
(fact
 "select keys in object"
 
 (!.js (k/obj-pick {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.base-lib/obj-omit, :added "4.0"}
(fact
 "new object with missing keys"
 
 (!.js (k/obj-omit {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-transpose, :added "4.0"}
(fact
 "obj-transposes a map"
 
 (!.js (k/obj-transpose {:a "x", :b "y", :c "z"}))
 =>
 {"z" "c", "x" "a", "y" "b"})

^{:refer xt.lang.base-lib/obj-nest, :added "4.0"}
(fact "creates a nested object"  (!.js (k/obj-nest ["a" "b"] 1)) => {"a" {"b" 1}})

^{:refer xt.lang.base-lib/obj-map, :added "4.0"}
(fact
 "maps a function across the values of an object"
 
 (!.js (k/obj-map {:a 1, :b 2, :c 3} km/inc))
 =>
 {"a" 2, "b" 3, "c" 4})

^{:refer xt.lang.base-lib/obj-filter, :added "4.0"}
(fact
 "applies a filter across the values of an object"
 
 (!.js (k/obj-filter {:a 1, :b 2, :c 3} km/odd?))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keep, :added "4.0"}
(fact
 "applies a transform across the values of an object, keeping non-nil values"
 
 (!.js (k/obj-keep {:a 1, :b 2, :c 3} (fn:> [x] (:? (km/odd? x) x))))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keepf, :added "4.0"}
(fact
 "applies a transform and filter across the values of an object"
 
 (!.js (k/obj-keepf {:a 1, :b 2, :c 3} km/odd? k/identity))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-intersection, :added "4.0"}
(fact
 "finds the intersection between map lookups"
 
 (!.js (k/obj-intersection {:a true, :b true} {:c true, :b true}))
 =>
 ["b"])

^{:refer xt.lang.base-lib/obj-difference, :added "4.0"}
(fact
 "finds the difference between two map lookups"
 
 (!.js
  [(k/obj-difference {:a true, :b true} {:c true, :b true})
   (k/obj-difference {:c true, :b true} {:a true, :b true})])
 =>
 [["c"] ["a"]])

^{:refer xt.lang.base-lib/obj-keys-nested, :added "4.0"}
(fact
 "gets nested keys"
 
 (!.js (k/obj-keys-nested {:a {:b {:c 1, :d 2}, :e {:f 4, :g 5}}} []))
 =>
 [[["a" "b" "c"] 1] [["a" "b" "d"] 2] [["a" "e" "f"] 4] [["a" "e" "g"] 5]])

^{:refer xt.lang.base-lib/to-flat, :added "4.0"}
(fact
 "flattens pairs of object into array"
 
 (!.js
  [(k/from-flat (k/to-flat {:a 1, :b 2, :c 3}) k/step-set-key {})
   (k/from-flat (k/to-flat (k/obj-pairs {:a 1, :b 2, :c 3})) k/step-set-key {})])
 =>
 [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/from-flat, :added "4.0"}
(fact
 "creates object from flattened pair array"
 
 (!.js (k/from-flat ["a" 1 "b" 2 "c" 3] k/step-set-key {}))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/get-in, :added "4.0"}
(fact "gets item in object"  (!.js (k/get-in {:a {:b {:c 1}}} ["a" "b"])) => {"c" 1})

^{:refer xt.lang.base-lib/set-in, :added "4.0"}
(fact
 "sets item in object"
 
 [(!.js (var a {:a {:b {:c 1}}}) (k/set-in a ["a" "b"] 2) a)
  (!.js (var a {:a {:b {:c 1}}}) (k/set-in a ["a" "d"] 2) a)]
 =>
 [{"a" {"b" 2}} {"a" {"d" 2, "b" {"c" 1}}}])

^{:refer xt.lang.base-lib/memoize-key, :added "4.0"}
(fact
 "memoize for functions of single argument"
 
 (!.js
  (var cache [])
  (var f (fn [v] (x:arr-push cache v) (return v)))
  (var mf (k/memoize-key f))
  [(mf 1) (mf 2) (mf 1) (mf 1) cache])
 =>
 [1 2 1 1 [1 2]])

^{:refer xt.lang.base-lib/not-empty?, :added "4.0"}
(fact
 "checks that array is not empty"
 
 (!.js
  [(k/not-empty? nil)
   (k/not-empty? "")
   (k/not-empty? "123")
   (k/not-empty? [])
   (k/not-empty? [1 2 3])
   (k/not-empty? {})
   (k/not-empty? {:a 1, :b 2})])
 =>
 [false false true false true false true])

^{:refer xt.lang.base-lib/eq-nested, :added "4.0"}
(fact
 "checking for nested equality"
 
 (!.js
  [(k/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 1}}})
   (k/eq-nested {:a {:b {:c 1}}} {:a {:b {:c 2}}})
   (k/eq-nested 1 1)
   (k/eq-nested 1 2)
   (k/eq-nested [1] [1])
   (k/eq-nested [1] [2])
   (k/eq-nested {:a [{:b {:c 1}}]} {:a [{:b {:c 1}}]})
   (k/eq-nested {:a [{:b {:c 1}}]} {:a [{:b {:c 2}}]})])
 =>
 [true false true false true false true false]
 (!.js
  (var out {:a {:b 1}})
  (k/set-in out ["a" "c"] out)
  [(k/eq-nested out (k/get-in out ["a" "c"])) (k/eq-nested out (k/get-in out ["a"]))])
 =>
 [true false])

^{:refer xt.lang.base-lib/obj-diff, :added "4.0"}
(fact
 "diffs only keys within map"
 
 (!.js (k/obj-diff {:a 1, :b 2} {:a 1, :c 2}))
 =>
 {"c" 2})

^{:refer xt.lang.base-lib/obj-diff-nested, :added "4.0"}
(fact
 "diffs nested keys within map"
 
 (!.js
  [(k/obj-diff-nested {:a 1, :b 2} {:a 1, :c 2})
   (k/obj-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
   (k/obj-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
 =>
 [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}])

^{:refer xt.lang.base-lib/objify, :added "4.0"}
(fact "decodes object if string"  (!.js (k/objify "{}")) => {})

^{:refer xt.lang.base-lib/template-entry, :added "4.0"}
(fact
 "gets data from a structure using template"
 
 (!.js (k/template-entry {:a 1, :b 2} ["a"] {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-fn, :added "4.0"}
(fact
 "gets data from a structure using template"
 
 (!.js ((k/template-fn ["a"]) {:a 1, :b 2} {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-multi, :added "4.0"}
(fact
 "gets data from a structure using template"
 
 (!.js ((k/template-multi [["c"] ["a"]]) {:a 1, :b 2} {}))
 =>
 1)

^{:refer xt.lang.base-lib/sort-by,
  :added "4.0",
  :setup
  [(def
    +data+
    [{:time 0, :name "a"} {:time 2, :name "a"} {:time 2, :name "b"} {:time 0, :name "b"}])
   (def
    +out+
    [[{"name" "a", "time" 0} {"name" "a", "time" 2} {"name" "b", "time" 0} {"name" "b", "time" 2}]
     [{"name" "b", "time" 2} {"name" "b", "time" 0} {"name" "a", "time" 2} {"name" "a", "time" 0}]
     [{"name" "a", "time" 0} {"name" "b", "time" 0} {"name" "a", "time" 2} {"name" "b", "time" 2}]
     [{"name" "a", "time" 2}
      {"name" "b", "time" 2}
      {"name" "a", "time" 0}
      {"name" "b", "time" 0}]])]}
(fact
 "sorts arrow by comparator"
 
 (!.js
  [(k/sort-by (@! +data+) ["name" "time"])
   (k/sort-by (@! +data+) [["name" true] ["time" true]])
   (k/sort-by (@! +data+) ["time" "name"])
   (k/sort-by (@! +data+) [(fn:> [e] (- (. e time))) "name"])])
 =>
 +out+)

^{:refer xt.lang.base-lib/sort-edges-build, :added "4.0"}
(fact
 "builds an edge with links"
 
 (!.js (var out {}) (k/sort-edges-build out ["a" "b"]) out)
 =>
 {"a" {"id" "a", "links" ["b"]}, "b" {"id" "b", "links" []}})

^{:refer xt.lang.base-lib/sort-edges, :added "4.0"}
(fact
 "sort edges given a list"
 
 (!.js (k/sort-edges [["a" "b"] ["b" "c"] ["c" "d"] ["d" "e"]]))
 =>
 ["a" "b" "c" "d" "e"])

^{:refer xt.lang.base-lib/sort-topo, :added "4.0"}
(fact
 "sorts in topological order"
 
 (!.js (k/sort-topo [["a" ["b" "c"]] ["c" ["b"]]]))
 =>
 ["b" "c" "a"])

^{:refer xt.lang.base-lib/clone-shallow, :added "4.0"}
(fact
 "shallow clones an object or array"
 
 (!.js [(k/clone-shallow "a") (k/clone-shallow ["a" "b"]) (k/clone-shallow {"a" "b"})])
 =>
 ["a" ["a" "b"] {"a" "b"}])

^{:refer xt.lang.base-lib/clone-nested, :added "4.0"}
(fact
 "cloning nested objects"
 
 (!.js (k/clone-nested {:a [1 2 3 {:b [4 5 6]}]}))
 =>
 {"a" [1 2 3 {"b" [4 5 6]}]}
 (!.js
  (do:>
   (let
    [input {:a {:b [1 2 3 {:c [4 5 6]}]}} output (k/clone-nested input)]
    (return
     [(k/eq-nested input output)
      (=== input output)
      (=== (x:get-key input "a") (x:get-key output "a"))
      (=== (. input ["a"] ["b"]) (. output ["a"] ["b"]))
      (=== (. input ["a"] ["b"] [3]) (. output ["a"] ["b"] [3]))]))))
 =>
 [true false false false false])

^{:refer xt.lang.base-lib/wrap-callback, :added "4.0"}
(fact
 "returns a wrapped callback given map"
 
 (!.js ((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1))
 =>
 2)

^{:refer xt.lang.base-lib/walk, :added "4.0"}
(fact
 "walks over object"
 
 (!.js (k/walk [1 {:a {:b 3}}] (fn [x] (return (:? (k/is-number? x) (+ x 1) x))) k/identity))
 =>
 [2 {"a" {"b" 4}}])

^{:refer xt.lang.base-lib/get-data,
  :added "4.0",
  :setup
  [(def +in+ '{:a 1, :b "hello", :c {:d [1 2 (fn:>)], :e "hello", :f {:g (fn:>), :h 2}}})
   (def
    +out+
    {"a" 1,
     "b" "hello",
     "c" {"d" [1 2 "<function>"], "f" {"g" "<function>", "h" 2}, "e" "hello"}})]}
(fact "gets only data (for use with json)"  (!.js (k/get-data (@! +in+))) => +out+)

^{:refer xt.lang.base-lib/get-spec,
  :added "4.0",
  :setup
  [(def +in+ '{:a 1, :b "hello", :c {:d [1 2 (fn:>)], :e "hello", :f {:g (fn:>), :h 2}}})
   (def
    +out+
    {"a" "number",
     "b" "string",
     "c" {"d" ["number" "number" "function"], "f" {"g" "function", "h" "number"}, "e" "string"}})]}
(fact
 "creates a get-spec of a datastructure"
 
 (!.js (k/get-spec (@! +in+)))
 =>
 +out+)

^{:refer xt.lang.base-lib/split-long,
  :added "4.0",
  :setup
  [(def +s+ (apply str (repeat 5 "1234567890")))
   (def +out+ ["1234567890" "1234567890" "1234567890" "1234567890" "1234567890"])]}
(fact "splits a long string"  (!.js (k/split-long (@! +s+) 10)) => +out+)

^{:refer xt.lang.base-lib/trace-log, :added "4.0"}
(fact "gets the current trace log"  (!.js (k/trace-log)) => vector?)

^{:refer xt.lang.base-lib/trace-log-clear, :added "4.0"}
(fact "resets the trace log"  (!.js (k/trace-log-clear)) => [])

^{:refer xt.lang.base-lib/trace-log-add, :added "4.0"}
(fact
 "adds an entry to the log"
 
 (!.js (k/trace-log-add "hello" "hello" {}))
 =>
 number?)

^{:refer xt.lang.base-lib/trace-filter, :added "4.0"}
(fact "filters out traced entries"  (!.js (k/trace-filter "hello")) => vector?)

^{:refer xt.lang.base-lib/trace-last-entry, :added "4.0"}
(fact "gets the last entry"  (!.js (k/trace-last-entry "hello")) => map?)

^{:refer xt.lang.base-lib/trace-data, :added "4.0"}
(fact "gets the trace data"  (!.js (k/trace-data "hello")) => vector?)

^{:refer xt.lang.base-lib/trace-last, :added "4.0"}
(fact "gets the last value"  (!.js (k/trace-last "hello")) => "hello")

^{:refer xt.lang.base-lib/TRACE!, :added "4.0"}
(fact
 "performs a trace call"
 
 (!.js (k/TRACE! "hello" "hello") (k/trace-last-entry))
 =>
 (contains {"tag" "hello",
            "time" integer?,
            "line" integer?,
            "column" integer?,
            "data" "hello",
            "ns" string?}))

^{:refer xt.lang.base-lib/RUN!, :added "4.0"}
(fact "runs a form, saving trace forms"
 
 (!.js (k/RUN! (k/TRACE! 1) (k/TRACE! 2)))
 =>
 (contains-in [{"data" 1} {"data" 2}]))

