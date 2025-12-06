(ns code.test.checker.diff-test
  (:use code.test)
  (:require [code.test.checker.diff :refer :all]
            [code.test.checker.common :as common]
            [code.test.checker.collection :as coll]))

^{:refer code.test.checker.diff/checker-equal? :added "4.0"}
(fact "checker-equal?"
  (checker-equal? 1 1) => true
  (checker-equal? 1 2) => false
  (checker-equal? even? 2) => true
  (checker-equal? even? 1) => false)

^{:refer code.test.checker.diff/diff-map :added "4.0"}
(fact "diff-map"
  (diff-map {:a 1} {:a 1}) => nil
  (diff-map {:a 1} {:a 2}) => {:> {[:a] {:expect 1 :actual 2}}}
  (diff-map {:a 1} {}) => {:+ {[:a] 1}}
  (diff-map {} {:a 1}) => {:- {[:a] 1}}
  (diff-map {:a even?} {:a 2}) => nil
  (diff-map {:a even?} {:a 1}) => {:> {[:a] {:expect even? :actual 1}}})

^{:refer code.test.checker.diff/diff-seq :added "4.0"}
(fact "diff-seq"
  (diff-seq [1 2 3] [1 2 3]) => nil
  (diff-seq [1 2 3] [1 2 4]) => [[:- 2 1] [:+ 2 [4]]]
  (diff-seq [1] []) => [[:- 0 1]]
  (diff-seq [] [1]) => [[:+ 0 [1]]]

  (let [v1 (vec (range 20))
        v2 (concat (range 19) [99])]
    (diff-seq v1 v2))
  => [[:- 19 1] [:+ 19 [99]]])

^{:refer code.test.checker.diff/diff :added "4.0"}
(fact "diff"
  (keys (diff (coll/contains {:a 1}) {:a 2 :b 2}))
  => [:>]

  (keys (:> (diff (coll/contains {:a 1}) {:a 2 :b 2})))
  => [[:a]]

  (diff (coll/contains {:a 1}) {:a 1 :b 2})
  => nil

  (diff (coll/just {:a 1}) {:a 1 :b 2})
  => {:- {[:b] 2}})
