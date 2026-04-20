(ns
 xtbench.dart.runtime.common-hash-test
 (:require [std.json :as json] [std.lang :as l])
 (:use code.test))

(l/script-
 :dart
 {:runtime :twostep,
  :require
  [[xt.runtime.common-hash :as hash] [xt.lang.common-iter :as it]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.runtime.common-hash/hash-float, :added "4.0"}
(fact
 "hashes a floating point"
 ^{:hidden true}
 (!.dt (hash/hash-float 0.1))
 =>
 integer?)

^{:refer xt.runtime.common-hash/hash-string, :added "4.0"}
(fact
 "hashes a string"
 ^{:hidden true}
 (!.dt (hash/hash-string "abc"))
 =>
 1192459)

^{:refer xt.runtime.common-hash/hash-iter, :added "4.0"}
(fact
 "hashes an iterator"
 ^{:hidden true}
 (!.dt (hash/hash-iter (it/range [0 100]) hash/hash-native))
 =>
 3373073)

^{:refer xt.runtime.common-hash/hash-iter-unordered, :added "4.0"}
(fact
 "hashes an unordered set"
 ^{:hidden true}
 (!.dt
  (==
   (hash/hash-iter-unordered (it/iter [1 2 3 4 5]) hash/hash-native)
   (hash/hash-iter-unordered (it/iter [5 1 2 3 4]) hash/hash-native)))
 =>
 true)

^{:refer xt.runtime.common-hash/hash-integer, :added "4.0"}
(fact
 "hashes an integer"
 ^{:hidden true}
 (!.dt [(hash/hash-integer 1) (hash/hash-integer 16777217)])
 =>
 [1 1])

^{:refer xt.runtime.common-hash/hash-boolean, :added "4.0"}
(fact
 "hashes a boolean"
 ^{:hidden true}
 (!.dt [(hash/hash-boolean true) (hash/hash-boolean false)])
 =>
 [1 -1])

^{:refer xt.runtime.common-hash/hash-native, :added "4.0"}
(fact
 "hashes a value"
 ^{:hidden true}
 (!.dt (hash/hash-native (fn:>)))
 =>
 integer?)

^{:refer xt.runtime.common-hash/native-type, :added "4.1"}
(fact
 "returns the runtime-native type tag"
 ^{:hidden true}
 (!.dt
  [(hash/native-type nil)
   (hash/native-type "abc")
   (hash/native-type true)
   (hash/native-type 1)
   (hash/native-type [1 2])
   (hash/native-type (fn:>))
   (hash/native-type {})])
 =>
 ["nil" "string" "boolean" "number" "array" "function" "object"])

^{:refer xt.runtime.common-hash/native-class, :added "4.1"}
(fact
 "returns managed class tags for objects and native types otherwise"
 ^{:hidden true}
 (!.dt [(hash/native-class "abc") (hash/native-class {"::" "demo"})])
 =>
 ["string" "demo"])
