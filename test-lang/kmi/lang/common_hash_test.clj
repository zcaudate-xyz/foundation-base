(ns kmi.lang.common-hash-test
  (:require [std.json :as json]
            [hara.lang :as l])
  (:use code.test))

^{:seedgen/root {:all true :langs [:lua :python :dart]}}
(l/script- :js
  {:runtime :basic
   :require [[kmi.lang.common-hash :as hash]
             [xt.lang.common-iter :as it]
             [xt.lang.common-data :as k]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer kmi.lang.common-hash/hash-float :added "4.0"}
(fact "hashes a floating point"

  (!.js
   (hash/hash-float 0.1))
  => integer?)

^{:refer kmi.lang.common-hash/hash-string :added "4.0"}
(fact "hashes a string"

  (!.js
   (hash/hash-string "abc"))
  => 1192459)

^{:refer kmi.lang.common-hash/hash-iter :added "4.0"}
(fact "hashes an iterator"

  (!.js
   (hash/hash-iter
    (it/iter (k/arr-range [0 100]))
    hash/hash-native))
  => 3373073)

^{:refer kmi.lang.common-hash/hash-iter-unordered :added "4.0"}
(fact "hashes an unordered set"

  (!.js
   (== (hash/hash-iter-unordered
        (it/iter [1 2 3 4 5])
        hash/hash-native)
       (hash/hash-iter-unordered
        (it/iter [5 1 2 3 4])
        hash/hash-native)))
  => true)

^{:refer kmi.lang.common-hash/hash-integer :added "4.0"}
(fact "hashes an integer"

  (!.js
   [(hash/hash-integer 1)
    (hash/hash-integer 16777217)])
  => [1 1])

^{:refer kmi.lang.common-hash/hash-boolean :added "4.0"}
(fact "hashes a boolean"

  (!.js
   [(hash/hash-boolean true)
    (hash/hash-boolean false)])
  => [1 -1])

^{:refer kmi.lang.common-hash/native-type :added "4.1"}
(fact "returns the runtime-native type tag"

  (!.js
   [(hash/native-type nil)
    (hash/native-type "abc")
    (hash/native-type true)
    (hash/native-type 1)
    (hash/native-type [1 2])
    (hash/native-type hash/hash-string)
    (hash/native-type {})])
  => ["nil" "string" "boolean" "number" "array" "function" "object"])

^{:refer kmi.lang.common-hash/native-class :added "4.1"}
(fact "returns managed class tags for objects and native types otherwise"

  (!.js
   [(hash/native-class "abc")
    (hash/native-class {"::" "demo"})])
  => ["string" "demo"])

^{:refer kmi.lang.common-hash/hash-native :added "4.0"}
(fact "hashes a value"

  (!.js
   (hash/hash-native hash/hash-string))
  => integer?)
