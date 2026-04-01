(ns
 xt.lang.common-lib-rb-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script-
 :ruby
 {:runtime :basic, :require [[xt.lang.common-lib :as k] [xt.lang.base-macro :as km]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-lib/type-native, :added "4.0"}
(fact
 "gets the native type"
 ^{:hidden true}
 (!.rb
  [(k/type-native {})
   (k/type-native [1])
   (k/type-native (fn []))
   (k/type-native 1)
   (k/type-native "")
   (k/type-native true)])
 =>
 ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.common-lib/type-class, :added "4.0"}
(fact
 "gets the type of an object"
 ^{:hidden true}
 (!.rb
  [(k/type-class {})
   (k/type-class [1])
   (k/type-class (fn []))
   (k/type-class 1)
   (k/type-class "")
   (k/type-class true)])
 =>
 ["object" "array" "function" "number" "string" "boolean"])

^{:refer xt.lang.common-lib/arr?, :added "4.0"}
(fact
 "checks if object is an array"
 ^{:hidden true}
 (!.rb [(k/arr? [1 2 3]) (k/arr? {:a 1})])
 =>
 [true false])

^{:refer xt.lang.common-lib/obj?, :added "4.0"}
(fact
 "checks if object is a map type"
 ^{:hidden true}
 (!.rb [(k/obj? {:a 1}) (k/obj? [1 2 3])])
 =>
 [true false])

