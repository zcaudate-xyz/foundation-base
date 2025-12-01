(ns js.core.util-test
  (:use code.test)
  (:require [js.core.util :refer :all]))

^{:refer js.core.util/pass-callback :added "4.0" :unchecked true}
(fact "node style callback")

^{:refer js.core.util/wrap-callback :added "4.0" :unchecked true}
(fact "wraps promise with node style callback")
