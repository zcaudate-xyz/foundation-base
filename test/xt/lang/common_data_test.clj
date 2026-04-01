(ns
 xt.lang.common-data-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

^{:refer xt.lang.common-data/eq-nested-loop, :added "4.0"} (fact "switch for nested check")

^{:refer xt.lang.common-data/eq-nested-obj, :added "4.0"} (fact "checking object equality")

^{:refer xt.lang.common-data/eq-nested-arr, :added "4.0"} (fact "checking aray equality")

^{:refer xt.lang.common-data/path-fn, :added "4.0"}
(fact "creates a function that accesses a path in an object")

^{:refer xt.lang.common-data/eq-shallow, :added "4.0"} (fact "checks for shallow equality")

