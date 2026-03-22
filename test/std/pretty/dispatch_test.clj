(ns std.pretty.dispatch-test
  (:require [std.pretty :as printer]
            [std.pretty.dispatch :refer :all])
  (:use code.test))

^{:refer std.pretty.dispatch/chained-lookup :added "3.0"}
(fact "chains two or more lookups together"

  (chained-lookup
   (inheritance-lookup printer/clojure-handlers)
   (inheritance-lookup printer/java-handlers)))

^{:refer std.pretty.dispatch/inheritance-lookup :added "3.0"}
(fact "checks if items inherit from the handlers"

  ((inheritance-lookup printer/clojure-handlers)
   clojure.lang.Atom)
  => fn?

  ((inheritance-lookup printer/clojure-handlers)
   String)
  => nil)
