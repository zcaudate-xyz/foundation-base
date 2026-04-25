(ns xtbench.dart.lang.common-sort-by-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.common-sort-by :as xtsb]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
 :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-sort-by/sort-by :added "4.1"}
(fact "sorts string keys lexicographically"

  (!.dt
   (xtsb/sort-by
    [{"id" "a" "name" "beta"}
     {"id" "b" "name" "alpha"}
     {"id" "c" "name" "gamma"}]
    ["name"]))
  => [{"id" "b" "name" "alpha"}
      {"id" "a" "name" "beta"}
      {"id" "c" "name" "gamma"}])

^{:refer xt.lang.common-sort-by/sort-by :added "4.1"}
(fact "sorts string keys lexicographically"

  (!.dt
   (xtsb/sort-by
    [{"id" "a" "name" "beta"}
     {"id" "b" "name" "alpha"}
     {"id" "c" "name" "gamma"}]
    ["name"]))
  => [{"id" "b" "name" "alpha"}
      {"id" "a" "name" "beta"}
      {"id" "c" "name" "gamma"}])

^{:refer xt.lang.common-sort-by/sort-by :added "4.1"}
(fact "sorts string keys lexicographically"

  (!.dt
   (xtsb/sort-by
    [{"id" "a" "name" "beta"}
     {"id" "b" "name" "alpha"}
     {"id" "c" "name" "gamma"}]
    ["name"]))
  => [{"id" "b" "name" "alpha"}
      {"id" "a" "name" "beta"}
      {"id" "c" "name" "gamma"}])

(comment
  (s/seedgen-langadd 'xt.lang.common-sort-by {:lang [:lua :python] :write true})
  (s/seedgen-langremove 'xt.lang.common-sort-by {:lang [:lua :python] :write true}))
