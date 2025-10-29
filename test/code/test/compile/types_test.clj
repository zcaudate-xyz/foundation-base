(ns code.test.compile.types-test
  (:use code.test)
  (:require [code.test.compile.types :refer :all]
            [code.test.base.runtime :as rt]))

^{:refer code.test.compile.types/map->Fact :added "3.0" :adopt true}
(fact "creates a fact object")

^{:refer code.test.compile.types/fact-invoke :added "3.0"
  :guard true}
(fact "invokes a fact object")

^{:refer code.test.compile.types/fact-display-info :added "4.0"}
(fact "displays detailed information about a given fact"))

^{:refer code.test.compile.types/fact-display :added "3.0"}
(fact "displays a general representation of a given fact"))

^{:refer code.test.compile.types/fact? :added "3.0"}
(fact "checks if object is a fact")

^{:refer code.test.compile.types/bench-single :added "3.0"}
(fact "runs a benchmark on a single function")

(comment
  (:throw (into {} (rt/get-fact (first (rt/list-facts)))))
  (fact-invoke (rt/get-fact (first (rt/list-facts))))
  => true)
