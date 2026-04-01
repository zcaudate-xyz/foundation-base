(ns
 xt.lang.base-lib-dart-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script- :dart {:runtime :basic, :require [[xt.lang.base-lib :as k] [xt.lang.base-macro :as km]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-lib/proto-create, :added "4.0"}
(fact
 "creates the prototype map"
 ^{:hidden true}
 (!.dart
  (var mt (k/proto-create {:hello (fn:> [v] (. v world)), :world "hello"}))
  (var a {})
  (k/set-proto a mt)
  (. a (hello)))
 =>
 "hello")

^{:refer xt.lang.base-lib/fn?, :added "4.0"}
(fact
 "checks if object is a function type"
 ^{:hidden true}
 (!.dart [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
 =>
 [true true false])

^{:refer xt.lang.base-lib/identity, :added "4.0"}
(fact "identity function" ^{:hidden true} (!.dart (k/identity 1)) => 1)

^{:refer xt.lang.base-lib/step-set-fn, :added "4.0"}
(fact "creates a set key function" ^{:hidden true} (!.dart ((k/step-set-fn {} "a") 1)) => {"a" 1})

^{:refer xt.lang.base-lib/starts-with?, :added "4.0"}
(fact "check for starts with" ^{:hidden true} (!.dart (k/starts-with? "Foo Bar" "Foo")) => true)

^{:refer xt.lang.base-lib/ends-with?, :added "4.0"}
(fact "check for ends with" ^{:hidden true} (!.dart (k/ends-with? "Foo Bar" "Bar")) => true)

^{:refer xt.lang.base-lib/capitalize, :added "4.0"}
(fact "uppercases the first letter" ^{:hidden true} (!.dart (k/capitalize "hello")) => "Hello")

^{:refer xt.lang.base-lib/decapitalize, :added "4.0"}
(fact "lowercases the first letter" ^{:hidden true} (!.dart (k/decapitalize "HELLO")) => "hELLO")

^{:refer xt.lang.base-lib/pad-left, :added "4.0"}
(fact
 "pads string with n chars on left"
 ^{:hidden true}
 (!.dart (k/pad-left "000" 5 "-"))
 =>
 "--000")

^{:refer xt.lang.base-lib/pad-right, :added "4.0"}
(fact
 "pads string with n chars on right"
 ^{:hidden true}
 (!.dart (k/pad-right "000" 5 "-"))
 =>
 "000--")

^{:refer xt.lang.base-lib/pad-lines, :added "4.0"}
(fact
 "pad lines with starting chars"
 ^{:hidden true}
 (!.dart (k/pad-lines (k/join "\n" ["hello" "world"]) 2 " "))
 =>
 (prose/| "  hello" "  world"))

^{:refer xt.lang.base-lib/mod-pos, :added "4.0"}
(fact "gets the positive mod" ^{:hidden true} (!.dart [(mod -11 10) (k/mod-pos -11 10)]) => [-1 9])

^{:refer xt.lang.base-lib/mod-offset, :added "4.0"}
(fact
 "calculates the closet offset"
 ^{:hidden true}
 (!.dart
  [(k/mod-offset 20 280 360)
   (k/mod-offset 280 20 360)
   (k/mod-offset 280 -80 360)
   (k/mod-offset 20 -60 360)
   (k/mod-offset 60 30 360)])
 =>
 [-100 100 0 -80 -30])

^{:refer xt.lang.base-lib/gcd, :added "4.0"}
(fact "greatest common denominator" ^{:hidden true} (!.dart (k/gcd 10 6)) => 2)

^{:refer xt.lang.base-lib/lcm, :added "4.0"}
(fact "lowest common multiple" ^{:hidden true} (!.dart (k/lcm 10 6)) => 30)

^{:refer xt.lang.base-lib/mix, :added "4.0"}
(fact "mixes two values with a fraction" ^{:hidden true} (!.dart (k/mix 100 20 0.1)) => 92)

^{:refer xt.lang.base-lib/sign, :added "4.0"}
(fact "gets the sign of " ^{:hidden true} (!.dart [(k/sign -10) (k/sign 10)]) => [-1 1])

^{:refer xt.lang.base-lib/round, :added "4.0"}
(fact
 "rounds to the nearest integer"
 ^{:hidden true}
 (!.dart [(k/round 0.9) (k/round 1.1) (k/round 1.49) (k/round 1.51)])
 =>
 [1 1 1 2])

^{:refer xt.lang.base-lib/clamp, :added "4.0"}
(fact
 "clamps a value between min and max"
 ^{:hidden true}
 (!.dart [(k/clamp 0 5 6) (k/clamp 0 5 -1) (k/clamp 0 5 4)])
 =>
 [5 0 4])

^{:refer xt.lang.base-lib/bit-count, :added "4.0"}
(fact
 "get the bit count"
 ^{:hidden true}
 (!.dart [(k/bit-count 16) (k/bit-count 10) (k/bit-count 3) (k/bit-count 7)])
 =>
 [1 2 2 3])

^{:refer xt.lang.base-lib/sym-full, :added "4.0"}
(fact "creates a sym" ^{:hidden true} (!.dart (k/sym-full "hello" "world")) => "hello/world")

^{:refer xt.lang.base-lib/sym-name, :added "4.0"}
(fact
 "gets the name part of the sym"
 ^{:hidden true}
 (!.dart (k/sym-name "hello/world"))
 =>
 "world")

^{:refer xt.lang.base-lib/sym-ns, :added "4.0"}
(fact
 "gets the namespace part of the sym"
 ^{:hidden true}
 (!.dart [(k/sym-ns "hello/world") (k/sym-ns "hello")])
 =>
 ["hello" nil])

^{:refer xt.lang.base-lib/sym-pair, :added "4.0"}
(fact "gets the sym pair" ^{:hidden true} (!.dart (k/sym-pair "hello/world")) => ["hello" "world"])

^{:refer xt.lang.base-lib/arr-lookup, :added "4.0"}
(fact
 "constructs a lookup given keys"
 ^{:hidden true}
 (!.dart (k/arr-lookup ["a" "b" "c"]))
 =>
 {"a" true, "b" true, "c" true})

^{:refer xt.lang.base-lib/arr-every, :added "4.0"}
(fact
 "checks that every element fulfills thet predicate"
 ^{:hidden true}
 (!.dart [(k/arr-every [1 2 3] km/odd?) (k/arr-every [1 3] km/odd?)])
 =>
 [false true])

^{:refer xt.lang.base-lib/arr-some, :added "4.0"}
(fact
 "checks that the array contains an element"
 ^{:hidden true}
 (!.dart [(k/arr-some [1 2 3] km/even?) (k/arr-some [1 3] km/even?)])
 =>
 [true false])

^{:refer xt.lang.base-lib/arr-each, :added "4.0"}
(fact
 "performs a function call for each element"
 ^{:hidden true}
 (!.dart (var a := []) (k/arr-each [1 2 3 4 5] (fn [e] (x:arr-push a (+ 1 e)))) a)
 =>
 [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-omit, :added "4.0"}
(fact
 "emits index from new array"
 ^{:hidden true}
 (!.dart (k/arr-omit ["a" "b" "c" "d"] 2))
 =>
 ["a" "b" "d"])

^{:refer xt.lang.base-lib/arr-reverse, :added "4.0"}
(fact "reverses the array" ^{:hidden true} (!.dart (k/arr-reverse [1 2 3 4 5])) => [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-find, :added "4.0"}
(fact
 "finds first index matching predicate"
 ^{:hidden true}
 (!.dart (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
 =>
 2)

^{:refer xt.lang.base-lib/arr-zip, :added "4.0"}
(fact
 "zips two arrays together into a map"
 ^{:hidden true}
 (!.dart (k/arr-zip ["a" "b" "c"] [1 2 3]))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-map, :added "4.0"}
(fact
 "maps a function across an array"
 ^{:hidden true}
 (!.dart (k/arr-map [1 2 3 4 5] km/inc))
 =>
 [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-clone, :added "4.0"}
(fact "clones an array" ^{:hidden true} (!.dart (k/arr-clone [1 2 3])) => [1 2 3])

^{:refer xt.lang.base-lib/arr-append, :added "4.0"}
(fact
 "appends to the end of an array"
 ^{:hidden true}
 (!.dart (var out := [1 2 3]) (k/arr-append out [4 5]) out)
 =>
 [1 2 3 4 5])

^{:refer xt.lang.base-lib/arr-slice, :added "4.0"}
(fact "slices an array" ^{:hidden true} (!.dart (k/arr-slice [1 2 3 4 5] 1 3)) => [2 3])

^{:refer xt.lang.base-lib/arr-rslice, :added "4.0"}
(fact
 "gets the reverse of a slice"
 ^{:hidden true}
 (!.dart (k/arr-rslice [1 2 3 4 5] 1 3))
 =>
 [3 2])

^{:refer xt.lang.base-lib/arr-tail, :added "4.0"}
(fact "gets the tail of the array" ^{:hidden true} (!.dart (k/arr-tail [1 2 3 4 5] 3)) => [5 4 3])

^{:refer xt.lang.base-lib/arr-mapcat, :added "4.0"}
(fact
 "maps an array function, concatenting results"
 ^{:hidden true}
 (!.dart (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
 =>
 [1 1 2 2 3 3])

^{:refer xt.lang.base-lib/arr-partition, :added "4.0"}
(fact
 "partitions an array into arrays of length n"
 ^{:hidden true}
 (!.dart (k/arr-partition [1 2 3 4 5 6 7 8 9 10] 3))
 =>
 [[1 2 3] [4 5 6] [7 8 9] [10]])

^{:refer xt.lang.base-lib/arr-filter, :added "4.0"}
(fact
 "applies a filter across an array"
 ^{:hidden true}
 (!.dart (k/arr-filter [1 2 3 4 5] km/odd?))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keep, :added "4.0"}
(fact
 "keeps items in an array if output is not nil"
 ^{:hidden true}
 (!.dart (k/arr-keep [1 2 3 4 5] (fn:> [x] (:? (km/odd? x) x))))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keepf, :added "4.0"}
(fact
 "keeps items in an array with transform if predicate holds"
 ^{:hidden true}
 (!.dart (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-juxt, :added "4.0"}
(fact
 "constructs a map given a array of pairs"
 ^{:hidden true}
 (!.dart (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]] km/first km/second))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-foldl, :added "4.0"}
(fact
 "performs reduce on an array"
 ^{:hidden true}
 (!.dart (k/arr-foldl [1 2 3 4 5] km/add 0))
 =>
 15)

^{:refer xt.lang.base-lib/arr-foldr, :added "4.0"}
(fact
 "performs right reduce"
 ^{:hidden true}
 (!.dart (k/arr-foldr [1 2 3 4 5] k/step-push []))
 =>
 [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-pipel, :added "4.0"}
(fact
 "thrushes an input through a function pipeline"
 ^{:hidden true}
 (!.dart (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 20)

^{:refer xt.lang.base-lib/arr-piper, :added "4.0"}
(fact
 "thrushes an input through a function pipeline from reverse"
 ^{:hidden true}
 (!.dart (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 110)

^{:refer xt.lang.base-lib/arr-group-by, :added "4.0"}
(fact
 "groups elements by key and view functions"
 ^{:hidden true}
 (!.dart (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]] km/first km/second))
 =>
 {"a" [1 2], "b" [3 4]})

^{:refer xt.lang.base-lib/arr-range, :added "4.0"}
(fact
 "creates a range array"
 ^{:hidden true}
 (!.dart [(k/arr-range 10) (k/arr-range [10]) (k/arr-range [2 8]) (k/arr-range [2 9 2])])
 =>
 [[0 1 2 3 4 5 6 7 8 9] [0 1 2 3 4 5 6 7 8 9] [2 3 4 5 6 7] [2 4 6 8]])

^{:refer xt.lang.base-lib/arr-intersection, :added "4.0"}
(fact
 "gets the intersection of two arrays"
 ^{:hidden true}
 (!.dart (k/arr-intersection ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["c" "d"])

^{:refer xt.lang.base-lib/arr-difference, :added "4.0"}
(fact
 "gets the difference of two arrays"
 ^{:hidden true}
 (!.dart (k/arr-difference ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["e" "f"])

^{:refer xt.lang.base-lib/arr-union, :added "4.0"}
(fact
 "gets the union of two arrays"
 ^{:hidden true}
 (set (!.dart (k/arr-union ["a" "b" "c" "d"] ["c" "d" "e" "f"])))
 =>
 #{"d" "f" "e" "a" "b" "c"})

^{:refer xt.lang.base-lib/arr-sort, :added "4.0"}
(fact
 "arr-sort using key function and comparator"
 ^{:hidden true}
 (!.dart
  [(k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< a b)))
   (k/arr-sort [3 4 1 2] k/identity (fn:> [a b] (< b a)))
   (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2]] k/first (fn:> [a b] (x:arr-str-comp a b)))
   (k/arr-sort [["c" 3] ["d" 4] ["a" 1] ["b" 2]] k/second (fn:> [a b] (< a b)))])
 =>
 [[1 2 3 4] [4 3 2 1] [["a" 1] ["b" 2] ["c" 3] ["d" 4]] [["a" 1] ["b" 2] ["c" 3] ["d" 4]]])

^{:refer xt.lang.base-lib/arr-sorted-merge, :added "4.0"}
(fact
 "performs a merge on two sorted arrays"
 ^{:hidden true}
 (!.dart
  [(k/arr-sorted-merge [1 2 3] [4 5 6] k/lt)
   (k/arr-sorted-merge [1 2 4] [3 5 6] k/lt)
   (k/arr-sorted-merge (k/arr-reverse [1 2 4]) (k/arr-reverse [3 5 6]) k/gt)])
 =>
 [[1 2 3 4 5 6] [1 2 3 4 5 6] [6 5 4 3 2 1]])

^{:refer xt.lang.base-lib/arr-shuffle, :added "4.0"}
(fact
 "shuffles the array"
 ^{:hidden true}
 (set (!.dart (k/arr-shuffle [1 2 3 4 5])))
 =>
 #{1 4 3 2 5})

^{:refer xt.lang.base-lib/arr-pushl, :added "4.0"}
(fact
 "pushs an element into array"
 ^{:hidden true}
 (!.dart (k/arr-pushl [1 2 3 4] 5 4))
 =>
 [2 3 4 5])

^{:refer xt.lang.base-lib/arr-pushr, :added "4.0"}
(fact
 "pushs an element into array"
 ^{:hidden true}
 (!.dart (k/arr-pushr [1 2 3 4] 5 4))
 =>
 [5 1 2 3])

^{:refer xt.lang.base-lib/arr-join, :added "4.0"}
(fact
 "joins array with string"
 ^{:hidden true}
 (!.dart (k/arr-join ["1" "2" "3" "4"] " "))
 =>
 "1 2 3 4")

^{:refer xt.lang.base-lib/arr-interpose, :added "4.0"}
(fact
 "puts element between array"
 ^{:hidden true}
 (!.dart (k/arr-interpose ["1" "2" "3" "4"] "XX"))
 =>
 ["1" "XX" "2" "XX" "3" "XX" "4"])

^{:refer xt.lang.base-lib/arr-repeat, :added "4.0"}
(fact
 "repeat function or value n times"
 ^{:hidden true}
 (!.dart [(k/arr-repeat "1" 4) (k/arr-repeat (k/inc-fn -1) 4)])
 =>
 [["1" "1" "1" "1"] [0 1 2 3]])

^{:refer xt.lang.base-lib/arr-random, :added "4.0"}
(fact
 "gets a random element from array"
 ^{:hidden true}
 (!.dart (k/arr-random [1 2 3 4]))
 =>
 #{1 4 3 2})

^{:refer xt.lang.base-lib/arr-normalise, :added "4.0"}
(fact
 "normalises array elements to 1"
 ^{:hidden true}
 (!.dart (k/arr-normalise [1 2 3 4]))
 =>
 [0.1 0.2 0.3 0.4])

^{:refer xt.lang.base-lib/arr-sample, :added "4.0"}
(fact
 "samples array according to probability"
 ^{:hidden true}
 (!.dart (k/arr-sample ["left" "right" "up" "down"] [0.1 0.2 0.3 0.4]))
 =>
 string?)

^{:refer xt.lang.base-lib/obj-empty?, :added "4.0"}
(fact
 "checks that object is empty"
 ^{:hidden true}
 (!.dart [(k/obj-empty? {}) (k/obj-empty? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.base-lib/obj-not-empty?, :added "4.0"}
(fact
 "checks that object is not empty"
 ^{:hidden true}
 (!.dart [(k/obj-not-empty? {}) (k/obj-not-empty? {:a 1})])
 =>
 [false true])

^{:refer xt.lang.base-lib/obj-first-key, :added "4.0"}
(fact "gets the first key" ^{:hidden true} (!.dart (k/obj-first-key {:a 1})) => "a")

^{:refer xt.lang.base-lib/obj-first-val, :added "4.0"}
(fact "gets the first val" ^{:hidden true} (!.dart (k/obj-first-val {:a 1})) => 1)

^{:refer xt.lang.base-lib/obj-vals, :added "4.0"}
(fact "gets vals of an object" ^{:hidden true} (set (!.dart (k/obj-vals {:a 1, :b 2}))) => #{1 2})

^{:refer xt.lang.base-lib/obj-pairs, :added "4.0"}
(fact
 "creates entry pairs from object"
 ^{:hidden true}
 (set (!.dart (k/obj-pairs {:a 1, :b 2, :c 2})))
 =>
 #{["c" 2] ["b" 2] ["a" 1]})

^{:refer xt.lang.base-lib/obj-clone, :added "4.0"}
(fact
 "clones an object"
 ^{:hidden true}
 (!.dart (k/obj-clone {:a 1, :b 2, :c 3}))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign, :added "4.0"}
(fact
 "merges key value pairs from into another"
 ^{:hidden true}
 (!.dart (var out := {:a 1}) (var rout := out) (k/obj-assign out {:b 2, :c 3}) rout)
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign-nested, :added "4.0"}
(fact
 "merges objects at a nesting level"
 ^{:comment true}
 (!.R "NOT SUPPORTED")
 ^{:hidden true}
 (!.dart
  [(k/obj-assign-nested {:a 1} {:b 2}) (k/obj-assign-nested {:a {:b {:c 1}}} {:a {:b {:d 1}}})])
 =>
 [{"a" 1, "b" 2} {"a" {"b" {"d" 1, "c" 1}}}])

^{:refer xt.lang.base-lib/obj-assign-with, :added "4.0"}
(fact
 "merges second into first given a function"
 ^{:hidden true}
 (!.dart (k/obj-assign-with {:a {:b true}} {:a {:c true}} k/obj-assign))
 =>
 {"a" {"b" true, "c" true}})

^{:refer xt.lang.base-lib/obj-from-pairs, :added "4.0"}
(fact
 "creates an object from pairs"
 ^{:hidden true}
 (!.dart (k/obj-from-pairs (k/obj-pairs {:a 1, :b 2, :c 3})))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-del, :added "4.0"}
(fact
 "deletes multiple keys"
 ^{:hidden true}
 (!.dart (k/obj-del {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-del-all, :added "4.0"}
(fact "deletes all keys" ^{:hidden true} (!.dart (k/obj-del-all {:a 1, :b 2, :c 3})) => {})

^{:refer xt.lang.base-lib/obj-pick, :added "4.0"}
(fact
 "select keys in object"
 ^{:hidden true}
 (!.dart (k/obj-pick {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.base-lib/obj-omit, :added "4.0"}
(fact
 "new object with missing keys"
 ^{:hidden true}
 (!.dart (k/obj-omit {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-transpose, :added "4.0"}
(fact
 "obj-transposes a map"
 ^{:hidden true}
 (!.dart (k/obj-transpose {:a "x", :b "y", :c "z"}))
 =>
 {"z" "c", "x" "a", "y" "b"})

^{:refer xt.lang.base-lib/obj-nest, :added "4.0"}
(fact "creates a nested object" ^{:hidden true} (!.dart (k/obj-nest ["a" "b"] 1)) => {"a" {"b" 1}})

^{:refer xt.lang.base-lib/obj-map, :added "4.0"}
(fact
 "maps a function across the values of an object"
 ^{:hidden true}
 (!.dart (k/obj-map {:a 1, :b 2, :c 3} km/inc))
 =>
 {"a" 2, "b" 3, "c" 4})

^{:refer xt.lang.base-lib/obj-filter, :added "4.0"}
(fact
 "applies a filter across the values of an object"
 ^{:hidden true}
 (!.dart (k/obj-filter {:a 1, :b 2, :c 3} km/odd?))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keep, :added "4.0"}
(fact
 "applies a transform across the values of an object, keeping non-nil values"
 ^{:hidden true}
 (!.dart (k/obj-keep {:a 1, :b 2, :c 3} (fn:> [x] (:? (km/odd? x) x))))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keepf, :added "4.0"}
(fact
 "applies a transform and filter across the values of an object"
 ^{:hidden true}
 (!.dart (k/obj-keepf {:a 1, :b 2, :c 3} km/odd? k/identity))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-intersection, :added "4.0"}
(fact
 "finds the intersection between map lookups"
 ^{:hidden true}
 (!.dart (k/obj-intersection {:a true, :b true} {:c true, :b true}))
 =>
 ["b"])

^{:refer xt.lang.base-lib/obj-keys-nested, :added "4.0"}
(fact
 "gets nested keys"
 ^{:hidden true}
 (!.dart (k/obj-keys-nested {:a {:b {:c 1, :d 2}, :e {:f 4, :g 5}}} []))
 =>
 [[["a" "b" "c"] 1] [["a" "b" "d"] 2] [["a" "e" "f"] 4] [["a" "e" "g"] 5]])

^{:refer xt.lang.base-lib/to-flat, :added "4.0"}
(fact
 "flattens pairs of object into array"
 ^{:hidden true}
 (!.dart
  [(k/from-flat (k/to-flat {:a 1, :b 2, :c 3}) k/step-set-key {})
   (k/from-flat (k/to-flat (k/obj-pairs {:a 1, :b 2, :c 3})) k/step-set-key {})])
 =>
 [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/from-flat, :added "4.0"}
(fact
 "creates object from flattened pair array"
 ^{:hidden true}
 (!.dart (k/from-flat ["a" 1 "b" 2 "c" 3] k/step-set-key {}))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/memoize-key, :added "4.0"}
(fact
 "memoize for functions of single argument"
 ^{:hidden true}
 (!.dart
  (var cache [])
  (var f (fn [v] (x:arr-push cache v) (return v)))
  (var mf (k/memoize-key f))
  [(mf 1) (mf 2) (mf 1) (mf 1) cache])
 =>
 [1 2 1 1 [1 2]])

^{:refer xt.lang.base-lib/objify, :added "4.0"}
(fact "decodes object if string" ^{:hidden true} (!.dart (k/objify "{}")) => {})

^{:refer xt.lang.base-lib/template-entry, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.dart (k/template-entry {:a 1, :b 2} ["a"] {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-fn, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.dart ((k/template-fn ["a"]) {:a 1, :b 2} {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-multi, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.dart ((k/template-multi [["c"] ["a"]]) {:a 1, :b 2} {}))
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
 ^{:hidden true}
 (!.dart
  [(k/sort-by (@! +data+) ["name" "time"])
   (k/sort-by (@! +data+) [["name" true] ["time" true]])
   (k/sort-by (@! +data+) ["time" "name"])
   (k/sort-by (@! +data+) [(fn:> [e] (- (. e time))) "name"])])
 =>
 +out+)

^{:refer xt.lang.base-lib/sort-edges-build, :added "4.0"}
(fact
 "builds an edge with links"
 ^{:hidden true}
 (!.dart (var out {}) (k/sort-edges-build out ["a" "b"]) out)
 =>
 {"a" {"id" "a", "links" ["b"]}, "b" {"id" "b", "links" []}})

^{:refer xt.lang.base-lib/sort-edges, :added "4.0"}
(fact
 "sort edges given a list"
 ^{:hidden true}
 (!.dart (k/sort-edges [["a" "b"] ["b" "c"] ["c" "d"] ["d" "e"]]))
 =>
 ["a" "b" "c" "d" "e"])

^{:refer xt.lang.base-lib/sort-topo, :added "4.0"}
(fact
 "sorts in topological order"
 ^{:hidden true}
 (!.dart (k/sort-topo [["a" ["b" "c"]] ["c" ["b"]]]))
 =>
 ["b" "c" "a"])

^{:refer xt.lang.base-lib/clone-shallow, :added "4.0"}
(fact
 "shallow clones an object or array"
 ^{:hidden true}
 (!.dart [(k/clone-shallow "a") (k/clone-shallow ["a" "b"]) (k/clone-shallow {"a" "b"})])
 =>
 ["a" ["a" "b"] {"a" "b"}])

^{:refer xt.lang.base-lib/clone-nested, :added "4.0"}
(fact
 "cloning nested objects"
 ^{:hidden true}
 (!.dart (k/clone-nested {:a [1 2 3 {:b [4 5 6]}]}))
 =>
 {"a" [1 2 3 {"b" [4 5 6]}]}
 (!.dart
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
 ^{:hidden true}
 (!.dart ((k/wrap-callback {:success (fn [i] (return (* 2 i)))} "success") 1))
 =>
 2)

^{:refer xt.lang.base-lib/walk, :added "4.0"}
(fact
 "walks over object"
 ^{:hidden true}
 (!.dart (k/walk [1 {:a {:b 3}}] (fn [x] (return (:? (k/is-number? x) (+ x 1) x))) k/identity))
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
(fact "gets only data (for use with json)" ^{:hidden true} (!.dart (k/get-data (@! +in+))) => +out+)

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
 ^{:hidden true}
 (!.dart (k/get-spec (@! +in+)))
 =>
 +out+)

^{:refer xt.lang.base-lib/split-long,
  :added "4.0",
  :setup
  [(def +s+ (apply str (repeat 5 "1234567890")))
   (def +out+ ["1234567890" "1234567890" "1234567890" "1234567890" "1234567890"])]}
(fact "splits a long string" ^{:hidden true} (!.dart (k/split-long (@! +s+) 10)) => +out+)

^{:refer xt.lang.base-lib/trace-log, :added "4.0"}
(fact "gets the current trace log" ^{:hidden true} (!.dart (k/trace-log)) => vector?)

^{:refer xt.lang.base-lib/trace-log-clear, :added "4.0"}
(fact "resets the trace log" ^{:hidden true} (!.dart (k/trace-log-clear)) => [])

^{:refer xt.lang.base-lib/trace-log-add, :added "4.0"}
(fact
 "adds an entry to the log"
 ^{:hidden true}
 (!.dart (k/trace-log-add "hello" "hello" {}))
 =>
 number?)

^{:refer xt.lang.base-lib/trace-filter, :added "4.0"}
(fact "filters out traced entries" ^{:hidden true} (!.dart (k/trace-filter "hello")) => vector?)

^{:refer xt.lang.base-lib/trace-last-entry, :added "4.0"}
(fact "gets the last entry" ^{:hidden true} (!.dart (k/trace-last-entry "hello")) => map?)

^{:refer xt.lang.base-lib/trace-data, :added "4.0"}
(fact "gets the trace data" ^{:hidden true} (!.dart (k/trace-data "hello")) => vector?)

^{:refer xt.lang.base-lib/trace-last, :added "4.0"}
(fact "gets the last value" ^{:hidden true} (!.dart (k/trace-last "hello")) => "hello")

^{:refer xt.lang.base-lib/TRACE!, :added "4.0"}
(fact
 "performs a trace call"
 ^{:hidden true}
 (!.dart (k/TRACE! "hello" "hello") (k/trace-last-entry))
 =>
 (contains
  {"tag" "hello",
   "time" integer?,
   "line" integer?,
   "column" integer?,
   "data" "hello",
   "ns" "xt.lang.base-lib-test"}))

^{:refer xt.lang.base-lib/RUN!, :added "4.0"}
(fact
 "runs a form, saving trace forms"
 ^{:hidden true}
 (!.dart (k/RUN! (k/TRACE! 1) (k/TRACE! 2)))
 =>
 (contains-in [{"data" 1} {"data" 2}]))

