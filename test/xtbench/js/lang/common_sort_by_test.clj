(ns
 xtbench.js.lang.common-sort-by-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic, :require [[xt.lang.common-sort-by :as xtsb]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-sort-by/sort-by, :added "4.1"}
(fact
 "sorts records by a single ascending key"
 (!.js
  (xtsb/sort-by
   [{"id" "b", "rank" 2} {"id" "a", "rank" 1} {"id" "c", "rank" 3}]
   ["rank"]))
 =>
 [{"id" "a", "rank" 1} {"id" "b", "rank" 2} {"id" "c", "rank" 3}])

^{:refer xt.lang.common-sort-by/sort-by, :added "4.1"}
(fact
 "sorts by multiple keys and respects descending flags"
 (!.js
  (xtsb/sort-by
   [{"id" "a", "group" 1, "rank" 1}
    {"id" "b", "group" 1, "rank" 3}
    {"id" "c", "group" 2, "rank" 1}
    {"id" "d", "group" 1, "rank" 2}]
   ["group" ["rank" true]]))
 =>
 [{"id" "b", "group" 1, "rank" 3}
  {"id" "d", "group" 1, "rank" 2}
  {"id" "a", "group" 1, "rank" 1}
  {"id" "c", "group" 2, "rank" 1}])

^{:refer xt.lang.common-sort-by/sort-by, :added "4.1"}
(fact
 "sorts string keys lexicographically"
 (!.js
  (xtsb/sort-by
   [{"id" "a", "name" "beta"}
    {"id" "b", "name" "alpha"}
    {"id" "c", "name" "gamma"}]
   ["name"]))
 =>
 [{"id" "b", "name" "alpha"}
  {"id" "a", "name" "beta"}
  {"id" "c", "name" "gamma"}])
