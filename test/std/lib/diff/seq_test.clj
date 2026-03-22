(ns std.lib.diff.seq-test
  (:require [std.lib.diff.seq :refer :all])
  (:use code.test))

^{:refer std.lib.diff.seq/diff :added "3.0"}
(fact "creates a diff of two sequences"

  (diff [1 2 3 4 5]
        [1 2 :a 4 5])
  => [2 [[:- 2 1] [:+ 2 [:a]]]]

  (diff [1 2 3 4 5]
        [1 :a 3 2 5])
  => [4 [[:- 1 1]
         [:+ 1 [:a]]
         [:- 3 1]
         [:+ 3 [2]]]])

^{:refer std.lib.diff.seq/patch :added "3.0"}
(fact "uses a diff to reconcile two sequences"

  (patch [1 2 3 4 5]
         [4 [[:- 1 1]
             [:+ 1 [:a]]
             [:- 3 1]
             [:+ 3 [2]]]])
  => [1 :a 3 2 5])

