(ns xt.runtime.common-hash-test
  (:require [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.common-hash :as hash]
             [xt.lang.common-iter :as it]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.runtime.common-hash :as hash]
             [xt.lang.common-iter :as it]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.runtime.common-hash :as hash]
             [xt.lang.common-iter :as it]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.common-hash/hash-float :added "4.0"}
(fact "hashes a floating point"
  ^:hidden
  
  (!.js
   (hash/hash-float 0.1))
  => integer?
  
  (!.lua
   (hash/hash-float 0.1))
  => integer?
  
  (!.py
   (hash/hash-float 0.1))
  => integer?)

^{:refer xt.runtime.common-hash/hash-string :added "4.0"}
(fact "hashes a string"
  ^:hidden
  
  (!.js
   (hash/hash-string "abc"))
  => 1192459
  
  (!.lua
   (hash/hash-string "abc"))
  => 1192459
  
  (!.py
   (hash/hash-string "abc"))
  => 1192459)

^{:refer xt.runtime.common-hash/hash-iter :added "4.0"}
(fact "hashes an iterator"
  ^:hidden

  (!.js
   (hash/hash-iter
    (it/range [0 100])
    hash/hash-native))
  => 3373073

  (!.lua
   (hash/hash-iter
    (it/range [0 100])
    hash/hash-native))
  => 3373073

  (!.py
   (hash/hash-iter
    (it/range [0 100])
    hash/hash-native))
  => 3373073)

^{:refer xt.runtime.common-hash/hash-iter-unordered :added "4.0"}
(fact "hashes an unordered set"
  ^:hidden
  
  (!.js
   (== (hash/hash-iter-unordered
        (it/iter [1 2 3 4 5])
        hash/hash-native)
       (hash/hash-iter-unordered
        (it/iter [5 1 2 3 4])
        hash/hash-native)))
  => true
  
  (!.lua
   (== (hash/hash-iter-unordered
        (it/iter [1 2 3 4 5])
        hash/hash-native)
       (hash/hash-iter-unordered
        (it/iter [5 1 2 3 4])
        hash/hash-native)))
  => true)

^{:refer xt.runtime.common-hash/hash-integer :added "4.0"}
(fact "hashes an integer"
  ^:hidden

  (!.js
   [(hash/hash-integer 1)
    (hash/hash-integer 16777217)])
  => [1 1]

  (!.lua
   [(hash/hash-integer 1)
    (hash/hash-integer 16777217)])
  => [1 1]

  (!.py
   [(hash/hash-integer 1)
    (hash/hash-integer 16777217)])
  => [1 1])

^{:refer xt.runtime.common-hash/hash-boolean :added "4.0"}
(fact "hashes a boolean"
  ^:hidden

  (!.js
   [(hash/hash-boolean true)
    (hash/hash-boolean false)])
  => [1 -1]

  (!.lua
   [(hash/hash-boolean true)
    (hash/hash-boolean false)])
  => [1 -1]

  (!.py
   [(hash/hash-boolean true)
    (hash/hash-boolean false)])
  => [1 -1])

^{:refer xt.runtime.common-hash/hash-native :added "4.0"}
(fact "hashes a value"
  ^:hidden
  
  (!.js
   (hash/hash-native (fn:>)))
  => integer?
  
  (!.lua
   (hash/hash-native (fn:>)))
  => integer?

  (!.py
   (hash/hash-native (fn:>)))
  => integer?)


^{:refer xt.runtime.common-hash/native-type :added "4.1"}
(fact "returns the runtime-native type tag"
  ^:hidden

  (!.js
   [(hash/native-type nil)
    (hash/native-type "abc")
    (hash/native-type true)
    (hash/native-type 1)
    (hash/native-type [1 2])
    (hash/native-type (fn:>))
    (hash/native-type {})])
  => ["nil" "string" "boolean" "number" "array" "function" "object"]

  (!.lua
   [(hash/native-type nil)
    (hash/native-type "abc")
    (hash/native-type true)
    (hash/native-type 1)
    (hash/native-type [1 2])
    (hash/native-type (fn:>))
    (hash/native-type {})])
  => ["nil" "string" "boolean" "number" "array" "function" "object"]

  (!.py
   [(hash/native-type nil)
    (hash/native-type "abc")
    (hash/native-type true)
    (hash/native-type 1)
    (hash/native-type [1 2])
    (hash/native-type (fn:>))
    (hash/native-type {})])
  => ["nil" "string" "boolean" "number" "array" "function" "object"])

^{:refer xt.runtime.common-hash/native-class :added "4.1"}
(fact "returns managed class tags for objects and native types otherwise"
  ^:hidden

  (!.js
   [(hash/native-class "abc")
    (hash/native-class {"::" "demo"})])
  => ["string" "demo"]

  (!.lua
   [(hash/native-class "abc")
    (hash/native-class {"::" "demo"})])
  => ["string" "demo"]

  (!.py
   [(hash/native-class "abc")
    (hash/native-class {"::" "demo"})])
  => ["string" "demo"])
