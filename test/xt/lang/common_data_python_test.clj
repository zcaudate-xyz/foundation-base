(ns
 xt.lang.common-data-python-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic, :require [[xt.lang.common-data :as k] [xt.lang.base-macro :as km]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/is-empty?, :added "4.0"}
(fact
 "checks that array is empty"
 ^{:hidden true}
 (!.py
  [(k/is-empty? nil)
   (k/is-empty? "")
   (k/is-empty? "123")
   (k/is-empty? [])
   (k/is-empty? [1 2 3])
   (k/is-empty? {})
   (k/is-empty? {:a 1, :b 2})])
 =>
 [true true false true false true false])

^{:refer xt.lang.common-data/arrayify, :added "4.0"}
(fact
 "makes something into an array"
 (comment (!.R [(k/arrayify 1) (k/arrayify [1])]) => [[1] [1]])
 ^{:hidden true}
 (!.py [(k/arrayify 1) (k/arrayify [1])])
 =>
 [[1] [1]])

^{:refer xt.lang.common-data/obj-keys, :added "4.0"}
(fact "gets keys of an object" ^{:hidden true} (set (!.py (k/obj-keys {:a 1, :b 2}))) => #{"a" "b"})

^{:refer xt.lang.common-data/obj-difference, :added "4.0"}
(fact
 "finds the difference between two map lookups"
 ^{:hidden true}
 (!.py
  [(k/obj-difference {:a true, :b true} {:c true, :b true})
   (k/obj-difference {:c true, :b true} {:a true, :b true})])
 =>
 [["c"] ["a"]])

^{:refer xt.lang.common-data/get-in, :added "4.0"}
(fact "gets item in object" ^{:hidden true} (!.py (k/get-in {:a {:b {:c 1}}} ["a" "b"])) => {"c" 1})

^{:refer xt.lang.common-data/set-in, :added "4.0"}
(fact
 "sets item in object"
 [(!.py (var a {:a {:b {:c 1}}}) (k/set-in a ["a" "b"] 2) a)
  (!.py (var a {:a {:b {:c 1}}}) (k/set-in a ["a" "d"] 2) a)]
 =>
 [{"a" {"b" 2}} {"a" {"d" 2, "b" {"c" 1}}}])

^{:refer xt.lang.common-data/not-empty?, :added "4.0"}
(fact
 "checks that array is not empty"
 ^{:hidden true}
 (!.py
  [(k/not-empty? nil)
   (k/not-empty? "")
   (k/not-empty? "123")
   (k/not-empty? [])
   (k/not-empty? [1 2 3])
   (k/not-empty? {})
   (k/not-empty? {:a 1, :b 2})])
 =>
 [false false true false true false true])

^{:refer xt.lang.common-data/eq-nested, :added "4.0"}
(fact
 "checking for nested equality"
 ^{:hidden true}
 (!.py
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
 (!.py
  (var out {:a {:b 1}})
  (k/set-in out ["a" "c"] out)
  [(k/eq-nested out (k/get-in out ["a" "c"])) (k/eq-nested out (k/get-in out ["a"]))])
 =>
 [true false])

^{:refer xt.lang.common-data/obj-diff, :added "4.0"}
(fact
 "diffs only keys within map"
 ^{:hidden true}
 (!.py (k/obj-diff {:a 1, :b 2} {:a 1, :c 2}))
 =>
 {"c" 2})

^{:refer xt.lang.common-data/obj-diff-nested, :added "4.0"}
(fact
 "diffs nested keys within map"
 ^{:hidden true}
 (!.py
  [(k/obj-diff-nested {:a 1, :b 2} {:a 1, :c 2})
   (k/obj-diff-nested {:a 1, :b {:c 3}} {:a 1, :b {:d 3}})
   (k/obj-diff-nested {:a 1, :b {:c {:d 3}}} {:a 1, :b {:c {:e 3}}})])
 =>
 [{"c" 2} {"b" {"d" 3}} {"b" {"c" {"e" 3}}}])

