(ns
 xt.lang.base-lib-r-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script- :r {:runtime :basic, :require [[xt.lang.base-lib :as k] [xt.lang.base-macro :as km]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-lib/fn?, :added "4.0"}
(fact
 "checks if object is a function type"
 ^{:hidden true}
 (!.R [(k/fn? (fn:>)) (k/fn? k/first) (k/fn? 1)])
 =>
 [true true false])

^{:refer xt.lang.base-lib/identity, :added "4.0"}
(fact "identity function" ^{:hidden true} (!.R (k/identity 1)) => 1)

^{:refer xt.lang.base-lib/step-set-fn, :added "4.0"}
(fact "creates a set key function" ^{:hidden true} (!.R ((k/step-set-fn {} "a") 1)) => {"a" 1})

^{:refer xt.lang.base-lib/starts-with?, :added "4.0"}
(fact "check for starts with" ^{:hidden true} (!.R (k/starts-with? "Foo Bar" "Foo")) => true)

^{:refer xt.lang.base-lib/ends-with?, :added "4.0"}
(fact "check for ends with" ^{:hidden true} (!.R (k/ends-with? "Foo Bar" "Bar")) => true)

^{:refer xt.lang.base-lib/capitalize, :added "4.0"}
(fact "uppercases the first letter" ^{:hidden true} (!.R (k/capitalize "hello")) => "Hello")

^{:refer xt.lang.base-lib/decapitalize, :added "4.0"}
(fact "lowercases the first letter" ^{:hidden true} (!.R (k/decapitalize "HELLO")) => "hELLO")

^{:refer xt.lang.base-lib/pad-left, :added "4.0"}
(fact "pads string with n chars on left" ^{:hidden true} (!.R (k/pad-left "000" 5 "-")) => "--000")

^{:refer xt.lang.base-lib/pad-right, :added "4.0"}
(fact
 "pads string with n chars on right"
 ^{:hidden true}
 (!.R (k/pad-right "000" 5 "-"))
 =>
 "000--")

^{:refer xt.lang.base-lib/pad-lines, :added "4.0"}
(fact
 "pad lines with starting chars"
 ^{:hidden true}
 (!.R (k/pad-lines (k/join "\n" ["hello" "world"]) 2 " "))
 =>
 (prose/| "" "  hello" "  world"))

^{:refer xt.lang.base-lib/mod-pos, :added "4.0"}
(fact "gets the positive mod" ^{:hidden true} (!.R [(mod -11 10) (k/mod-pos -11 10)]) => [9 9])

^{:refer xt.lang.base-lib/mod-offset, :added "4.0"}
(fact
 "calculates the closet offset"
 ^{:hidden true}
 (!.R
  [(k/mod-offset 20 280 360)
   (k/mod-offset 280 20 360)
   (k/mod-offset 280 -80 360)
   (k/mod-offset 20 -60 360)
   (k/mod-offset 60 30 360)])
 =>
 [-100 100 0 -80 -30])

^{:refer xt.lang.base-lib/gcd, :added "4.0"}
(fact "greatest common denominator" ^{:hidden true} (!.R (k/gcd 10 6)) => 2)

^{:refer xt.lang.base-lib/lcm, :added "4.0"}
(fact "lowest common multiple" ^{:hidden true} (!.R (k/lcm 10 6)) => 30)

^{:refer xt.lang.base-lib/mix, :added "4.0"}
(fact "mixes two values with a fraction" ^{:hidden true} (!.R (k/mix 100 20 0.1 nil)) => 92)

^{:refer xt.lang.base-lib/sign, :added "4.0"}
(fact "gets the sign of " ^{:hidden true} (!.R [(k/sign -10) (k/sign 10)]) => [-1 1])

^{:refer xt.lang.base-lib/round, :added "4.0"}
(fact
 "rounds to the nearest integer"
 ^{:hidden true}
 (!.R [(k/round 0.9) (k/round 1.1) (k/round 1.49) (k/round 1.51)])
 =>
 [1 1 1 2])

^{:refer xt.lang.base-lib/clamp, :added "4.0"}
(fact
 "clamps a value between min and max"
 ^{:hidden true}
 (!.R [(k/clamp 0 5 6) (k/clamp 0 5 -1) (k/clamp 0 5 4)])
 =>
 [5 0 4])

^{:refer xt.lang.base-lib/sym-full, :added "4.0"}
(fact "creates a sym" ^{:hidden true} (!.R (k/sym-full "hello" "world")) => "hello/world")

^{:refer xt.lang.base-lib/sym-name, :added "4.0"}
(fact "gets the name part of the sym" ^{:hidden true} (!.R (k/sym-name "hello/world")) => "world")

^{:refer xt.lang.base-lib/sym-ns, :added "4.0"}
(fact
 "gets the namespace part of the sym"
 ^{:hidden true}
 (!.R [(k/sym-ns "hello/world") (k/sym-ns "hello")])
 =>
 ["hello" nil])

^{:refer xt.lang.base-lib/sym-pair, :added "4.0"}
(fact "gets the sym pair" ^{:hidden true} (!.R (k/sym-pair "hello/world")) => ["hello" "world"])

^{:refer xt.lang.base-lib/arr-lookup, :added "4.0"}
(fact
 "constructs a lookup given keys"
 ^{:hidden true}
 (!.R (k/arr-lookup ["a" "b" "c"]))
 =>
 {"a" true, "b" true, "c" true})

^{:refer xt.lang.base-lib/arr-every, :added "4.0"}
(fact
 "checks that every element fulfills thet predicate"
 ^{:hidden true}
 (!.R [(k/arr-every [1 2 3] km/odd?) (k/arr-every [1 3] km/odd?)])
 =>
 [false true])

^{:refer xt.lang.base-lib/arr-some, :added "4.0"}
(fact
 "checks that the array contains an element"
 ^{:hidden true}
 (!.R [(k/arr-some [1 2 3] km/even?) (k/arr-some [1 3] km/even?)])
 =>
 [true false])

^{:refer xt.lang.base-lib/arr-each, :added "4.0"}
(fact
 "performs a function call for each element"
 ^{:hidden true, :fails true}
 (!.R (var a := []) (k/arr-each [1 2 3 4 5] (fn [e] (x:arr-push a (+ 1 e)))) a)
 =>
 [])

^{:refer xt.lang.base-lib/arr-omit, :added "4.0"}
(fact
 "emits index from new array"
 ^{:hidden true}
 (!.R (k/arr-omit ["a" "b" "c" "d"] 2))
 =>
 ["a" "b" "d"])

^{:refer xt.lang.base-lib/arr-reverse, :added "4.0"}
(fact "reverses the array" ^{:hidden true} (!.R (k/arr-reverse [1 2 3 4 5])) => [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-find, :added "4.0"}
(fact
 "finds first index matching predicate"
 ^{:hidden true}
 (!.R (k/arr-find [1 2 3 4 5] (fn:> [x] (== x 3))))
 =>
 2)

^{:refer xt.lang.base-lib/arr-zip, :added "4.0"}
(fact
 "zips two arrays together into a map"
 ^{:hidden true}
 (!.R (k/arr-zip ["a" "b" "c"] [1 2 3]))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-map, :added "4.0"}
(fact
 "maps a function across an array"
 ^{:hidden true}
 (!.R (k/arr-map [1 2 3 4 5] km/inc))
 =>
 [2 3 4 5 6])

^{:refer xt.lang.base-lib/arr-clone, :added "4.0"}
(fact "clones an array" ^{:hidden true} (!.R (k/arr-clone [1 2 3])) => [1 2 3])

^{:refer xt.lang.base-lib/arr-append, :added "4.0"}
(fact
 "appends to the end of an array"
 ^{:hidden true}
 (!.R (var out := [1 2 3]) (k/arr-append out [4 5]) out)
 =>
 [1 2 3])

^{:refer xt.lang.base-lib/arr-rslice, :added "4.0"}
(fact "gets the reverse of a slice" ^{:hidden true} (!.R (k/arr-rslice [1 2 3 4 5] 1 3)) => [3 2])

^{:refer xt.lang.base-lib/arr-tail, :added "4.0"}
(fact "gets the tail of the array" ^{:hidden true} (!.R (k/arr-tail [1 2 3 4 5] 3)) => [5 4 3])

^{:refer xt.lang.base-lib/arr-mapcat, :added "4.0"}
(fact
 "maps an array function, concatenting results"
 ^{:hidden true}
 (!.R (k/arr-mapcat [1 2 3] (fn:> [k] [k k])))
 =>
 [1 1 2 2 3 3])

^{:refer xt.lang.base-lib/arr-partition, :added "4.0"}
(fact
 "partitions an array into arrays of length n"
 ^{:hidden true}
 (!.R (k/arr-partition [1 2 3 4 5 6 7 8 9 10] 3))
 =>
 [[1 2 3] [4 5 6] [7 8 9] [10]])

^{:refer xt.lang.base-lib/arr-filter, :added "4.0"}
(fact
 "applies a filter across an array"
 ^{:hidden true}
 (!.R (k/arr-filter [1 2 3 4 5] km/odd?))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keep, :added "4.0"}
(fact
 "keeps items in an array if output is not nil"
 ^{:hidden true}
 (!.R (k/arr-keep [1 2 3 4 5] (fn:> [x] (:? (km/odd? x) x))))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-keepf, :added "4.0"}
(fact
 "keeps items in an array with transform if predicate holds"
 ^{:hidden true}
 (!.R (k/arr-keepf [1 2 3 4 5] km/odd? k/identity))
 =>
 [1 3 5])

^{:refer xt.lang.base-lib/arr-juxt, :added "4.0"}
(fact
 "constructs a map given a array of pairs"
 ^{:hidden true}
 (!.R (k/arr-juxt [["a" 1] ["b" 2] ["c" 3]] km/first km/second))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/arr-foldl, :added "4.0"}
(fact "performs reduce on an array" ^{:hidden true} (!.R (k/arr-foldl [1 2 3 4 5] km/add 0)) => 15)

^{:refer xt.lang.base-lib/arr-foldr, :added "4.0"}
(fact
 "performs right reduce"
 ^{:hidden true}
 (!.R (k/arr-foldr [1 2 3 4 5] k/step-push []))
 =>
 [5 4 3 2 1])

^{:refer xt.lang.base-lib/arr-pipel, :added "4.0"}
(fact
 "thrushes an input through a function pipeline"
 ^{:hidden true}
 (!.R (k/arr-pipel [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 20)

^{:refer xt.lang.base-lib/arr-piper, :added "4.0"}
(fact
 "thrushes an input through a function pipeline from reverse"
 ^{:hidden true}
 (!.R (k/arr-piper [(fn:> [x] (* x 10)) (fn:> [x] (+ x 10))] 1))
 =>
 110)

^{:refer xt.lang.base-lib/arr-group-by, :added "4.0"}
(fact
 "groups elements by key and view functions"
 ^{:hidden true}
 (!.R (k/arr-group-by [["a" 1] ["a" 2] ["b" 3] ["b" 4]] km/first km/second))
 =>
 {"a" [1 2], "b" [3 4]})

^{:refer xt.lang.base-lib/arr-range, :added "4.0"}
(fact
 "creates a range array"
 ^{:hidden true}
 (!.R [(k/arr-range 10) (k/arr-range [10]) (k/arr-range [2 8]) (k/arr-range [2 9 2])])
 =>
 [[0 1 2 3 4 5 6 7 8 9] [0 1 2 3 4 5 6 7 8 9] [2 3 4 5 6 7] [2 4 6 8]])

^{:refer xt.lang.base-lib/arr-intersection, :added "4.0"}
(fact
 "gets the intersection of two arrays"
 ^{:hidden true}
 (!.R (k/arr-intersection ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["c" "d"])

^{:refer xt.lang.base-lib/arr-difference, :added "4.0"}
(fact
 "gets the difference of two arrays"
 ^{:hidden true}
 (!.R (k/arr-difference ["a" "b" "c" "d"] ["c" "d" "e" "f"]))
 =>
 ["e" "f"])

^{:refer xt.lang.base-lib/arr-union, :added "4.0"}
(fact
 "gets the union of two arrays"
 ^{:hidden true}
 (set (!.R (k/arr-union ["a" "b" "c" "d"] ["c" "d" "e" "f"])))
 =>
 #{"d" "f" "e" "a" "b" "c"})

^{:refer xt.lang.base-lib/arr-shuffle, :added "4.0"}
(fact "shuffles the array" ^{:hidden true} (set (!.R (k/arr-shuffle [1 2 3 4 5]))) => #{1 4 3 2 5})

^{:refer xt.lang.base-lib/arr-pushl, :added "4.0"}
(fact "pushs an element into array" ^{:hidden true} (!.R (k/arr-pushl [1 2 3 4] 5 4)) => [2 3 4 5])

^{:refer xt.lang.base-lib/arr-pushr, :added "4.0"}
(fact "pushs an element into array" ^{:hidden true} (!.R (k/arr-pushr [1 2 3 4] 5 4)) => [5 1 2 3])

^{:refer xt.lang.base-lib/arr-join, :added "4.0"}
(fact
 "joins array with string"
 ^{:hidden true}
 (!.R (k/arr-join ["1" "2" "3" "4"] " "))
 =>
 "1 2 3 4")

^{:refer xt.lang.base-lib/arr-interpose, :added "4.0"}
(fact
 "puts element between array"
 ^{:hidden true}
 (!.R (k/arr-interpose ["1" "2" "3" "4"] "XX"))
 =>
 ["1" "XX" "2" "XX" "3" "XX" "4"])

^{:refer xt.lang.base-lib/arr-random, :added "4.0"}
(fact
 "gets a random element from array"
 ^{:hidden true}
 (!.R (k/arr-random [1 2 3 4]))
 =>
 #{1 4 3 2})

^{:refer xt.lang.base-lib/arr-normalise, :added "4.0"}
(fact
 "normalises array elements to 1"
 ^{:hidden true}
 (!.R (k/arr-normalise [1 2 3 4]))
 =>
 [0.1 0.2 0.3 0.4])

^{:refer xt.lang.base-lib/arr-sample, :added "4.0"}
(fact
 "samples array according to probability"
 ^{:hidden true}
 (!.R (k/arr-sample ["left" "right" "up" "down"] [0.1 0.2 0.3 0.4]))
 =>
 string?)

^{:refer xt.lang.base-lib/obj-empty?, :added "4.0"}
(fact
 "checks that object is empty"
 ^{:hidden true}
 (!.R [(k/obj-empty? {}) (k/obj-empty? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.base-lib/obj-not-empty?, :added "4.0"}
(fact
 "checks that object is not empty"
 ^{:hidden true}
 (!.R [(k/obj-not-empty? {}) (k/obj-not-empty? {:a 1})])
 =>
 [false true])

^{:refer xt.lang.base-lib/obj-first-key, :added "4.0"}
(fact "gets the first key" ^{:hidden true} (!.R (k/obj-first-key {:a 1})) => "a")

^{:refer xt.lang.base-lib/obj-first-val, :added "4.0"}
(fact "gets the first val" ^{:hidden true} (!.R (k/obj-first-val {:a 1})) => 1)

^{:refer xt.lang.base-lib/obj-vals, :added "4.0"}
(fact "gets vals of an object" ^{:hidden true} (set (!.R (k/obj-vals {:a 1, :b 2}))) => #{1 2})

^{:refer xt.lang.base-lib/obj-pairs, :added "4.0"}
(fact
 "creates entry pairs from object"
 ^{:hidden true}
 (set (!.R (k/obj-pairs {:a 1, :b 2, :c 3})))
 =>
 #{["b" 2] ["a" 1] ["c" 3]})

^{:refer xt.lang.base-lib/obj-clone, :added "4.0"}
(fact
 "clones an object"
 ^{:hidden true}
 (!.R (k/obj-clone {:a 1, :b 2, :c 3}))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-assign, :added "4.0"}
(fact
 "merges key value pairs from into another"
 ^{:hidden true, :fails true}
 (!.R (var out := {:a 1}) (var cout (k/obj-assign out {:b 2, :c 3})) [out cout])
 =>
 [{"a" 1} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/obj-from-pairs, :added "4.0"}
(fact
 "creates an object from pairs"
 ^{:hidden true}
 (!.R (k/obj-from-pairs (k/obj-pairs {:a 1, :b 2, :c 3})))
 =>
 {"a" 1, "b" 2, "c" 3})

^{:refer xt.lang.base-lib/obj-del, :added "4.0"}
(fact
 "deletes multiple keys"
 ^{:hidden true}
 (!.R (k/obj-del {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-del-all, :added "4.0"}
(fact "deletes all keys" ^{:hidden true} (!.R (k/obj-del-all {:a 1, :b 2, :c 3})) => {})

^{:refer xt.lang.base-lib/obj-pick, :added "4.0"}
(fact
 "select keys in object"
 ^{:hidden true}
 (!.R (k/obj-pick {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"a" 1, "b" 2})

^{:refer xt.lang.base-lib/obj-omit, :added "4.0"}
(fact
 "new object with missing keys"
 ^{:hidden true}
 (!.R (k/obj-omit {:a 1, :b 2, :c 3} ["a" "b"]))
 =>
 {"c" 3})

^{:refer xt.lang.base-lib/obj-transpose, :added "4.0"}
(fact
 "obj-transposes a map"
 ^{:hidden true}
 (!.R (k/obj-transpose {:a "x", :b "y", :c "z"}))
 =>
 {"z" "c", "x" "a", "y" "b"})

^{:refer xt.lang.base-lib/obj-nest, :added "4.0"}
(fact "creates a nested object" ^{:hidden true} (!.R (k/obj-nest ["a" "b"] 1)) => {"a" {"b" 1}})

^{:refer xt.lang.base-lib/obj-map, :added "4.0"}
(fact
 "maps a function across the values of an object"
 ^{:hidden true}
 (!.R (k/obj-map {:a 1, :b 2, :c 3} km/inc))
 =>
 {"a" 2, "b" 3, "c" 4})

^{:refer xt.lang.base-lib/obj-filter, :added "4.0"}
(fact
 "applies a filter across the values of an object"
 ^{:hidden true}
 (!.R (k/obj-filter {:a 1, :b 2, :c 3} km/odd?))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keep, :added "4.0"}
(fact
 "applies a transform across the values of an object, keeping non-nil values"
 ^{:hidden true}
 (!.R (k/obj-keep {:a 1, :b 2, :c 3} (fn:> [x] (:? (km/odd? x) x))))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-keepf, :added "4.0"}
(fact
 "applies a transform and filter across the values of an object"
 ^{:hidden true}
 (!.R (k/obj-keepf {:a 1, :b 2, :c 3} km/odd? k/identity))
 =>
 {"a" 1, "c" 3})

^{:refer xt.lang.base-lib/obj-intersection, :added "4.0"}
(fact
 "finds the intersection between map lookups"
 ^{:hidden true}
 (!.R (k/obj-intersection {:a true, :b true} {:c true, :b true}))
 =>
 ["b"])

^{:refer xt.lang.base-lib/to-flat, :added "4.0"}
(fact
 "flattens pairs of object into array"
 ^{:hidden true}
 (!.R
  [(k/from-flat (k/to-flat {:a 1, :b 2, :c 3}) k/step-set-key {})
   (k/from-flat (k/to-flat (k/obj-pairs {:a 1, :b 2, :c 3})) k/step-set-key {})])
 =>
 [{"a" 1, "b" 2, "c" 3} {"a" 1, "b" 2, "c" 3}])

^{:refer xt.lang.base-lib/objify, :added "4.0"}
(fact "decodes object if string" ^{:hidden true} (!.R (k/objify "{}")) => {})

^{:refer xt.lang.base-lib/template-entry, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.R (k/template-entry {:a 1, :b 2} ["a"] {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-fn, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.R ((k/template-fn ["a"]) {:a 1, :b 2} {}))
 =>
 1)

^{:refer xt.lang.base-lib/template-multi, :added "4.0"}
(fact
 "gets data from a structure using template"
 ^{:hidden true}
 (!.R ((k/template-multi [["c"] ["a"]]) {:a 1, :b 2} {}))
 =>
 1)

^{:refer xt.lang.base-lib/clone-shallow, :added "4.0"}
(fact
 "shallow clones an object or array"
 ^{:hidden true}
 (!.R [(k/clone-shallow "a") (k/clone-shallow ["a" "b"]) (k/clone-shallow {"a" "b"})])
 =>
 ["a" ["a" "b"] {"a" "b"}])

^{:refer xt.lang.base-lib/walk, :added "4.0"}
(fact
 "walks over object"
 ^{:hidden true}
 (!.R (k/walk [1 {:a {:b 3}}] (fn [x] (return (:? (k/is-number? x) (+ x 1) x))) k/identity))
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
(fact "gets only data (for use with json)" ^{:hidden true} (!.R (k/get-data (@! +in+))) => +out+)

^{:refer xt.lang.base-lib/get-spec,
  :added "4.0",
  :setup
  [(def +in+ '{:a 1, :b "hello", :c {:d [1 2 (fn:>)], :e "hello", :f {:g (fn:>), :h 2}}})
   (def
    +out+
    {"a" "number",
     "b" "string",
     "c" {"d" ["number" "number" "function"], "f" {"g" "function", "h" "number"}, "e" "string"}})]}
(fact "creates a get-spec of a datastructure" ^{:hidden true} (!.R (k/get-spec (@! +in+))) => +out+)

