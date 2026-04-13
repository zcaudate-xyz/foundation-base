(ns xt.lang.common-sort-by-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-sort-by :as sort-by]]})

(fact:global
  {:setup [(l/rt:restart)]
   :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-sort-by/sort-by :added "4.1"}
(fact "compiles sort-by calls in lua runtime"
  (!.lua
   (var _ (fn [arr inputs]
            (sort-by/sort-by arr inputs)))
   true)
  => true)
