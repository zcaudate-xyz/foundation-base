(ns rt.basic.impl.process-scheme-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :scheme
  {:runtime :basic})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer rt.basic.impl.process-scheme/+scheme-basic-config+ :added "4.1"}
(fact "scheme basic runtime evaluates simple expressions"
  (!.scheme (+ 1 2))
  => 3)
