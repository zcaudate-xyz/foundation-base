(ns
 xt.lang.common-lib-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

^{:refer xt.lang.common-lib/noop, :added "4.0"} (fact "always a no op")

^{:refer xt.lang.common-lib/T, :added "4.0"} (fact "always true")

^{:refer xt.lang.common-lib/F, :added "4.0"} (fact "always false")

