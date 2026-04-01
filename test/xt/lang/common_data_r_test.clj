(ns
 xt.lang.common-data-r-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script- :r {:runtime :basic, :require [[xt.lang.common-data :as k] [xt.lang.base-macro :as km]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/is-empty?, :added "4.0"}
(fact
 "checks that array is empty"
 ^{:hidden true}
 (!.R
  [(k/is-empty? nil)
   (k/is-empty? "")
   (k/is-empty? "123")
   (k/is-empty? [])
   (k/is-empty? [1 2 3])
   (k/is-empty? {})
   (k/is-empty? {:a 1, :b 2})])
 =>
 [true true false true false true false])

^{:refer xt.lang.common-data/obj-keys, :added "4.0"}
(fact "gets keys of an object" ^{:hidden true} (set (!.R (k/obj-keys {:a 1, :b 2}))) => #{"a" "b"})

^{:refer xt.lang.common-data/obj-difference, :added "4.0"}
(fact
 "finds the difference between two map lookups"
 ^{:hidden true}
 (!.R
  [(k/obj-difference {:a true, :b true} {:c true, :b true})
   (k/obj-difference {:c true, :b true} {:a true, :b true})])
 =>
 [["c"] ["a"]])

^{:refer xt.lang.common-data/get-in, :added "4.0"}
(fact "gets item in object" ^{:hidden true} (!.R (k/get-in {:a {:b {:c 1}}} ["a" "b"])) => {"c" 1})

^{:refer xt.lang.common-data/not-empty?, :added "4.0"}
(fact
 "checks that array is not empty"
 ^{:hidden true}
 (!.R
  [(k/not-empty? nil)
   (k/not-empty? "")
   (k/not-empty? "123")
   (k/not-empty? [])
   (k/not-empty? [1 2 3])
   (k/not-empty? {})
   (k/not-empty? {:a 1, :b 2})])
 =>
 [false false true false true false true])

