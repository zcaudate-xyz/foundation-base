(ns std.config.common-test
  (:use code.test)
  (:require [std.config.common :refer :all]))

^{:refer std.config.common/-resolve-directive :added "3.0"}
(fact "multimethod for resolving directives"
  (instance? clojure.lang.MultiFn -resolve-directive) => true)

^{:refer std.config.common/-resolve-type :added "3.0"}
(fact "utility method for resolve"
  (instance? clojure.lang.MultiFn -resolve-type) => true)
