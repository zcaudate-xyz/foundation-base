(ns std.lang.dev-test
  (:require [std.lang.dev :refer :all])
  (:use code.test))

^{:refer std.lang.dev/reload-specs :added "4.0"}
(fact "reloads the specs")
