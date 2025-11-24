(ns code.test.diff-test
  (:use code.test)
  (:require [code.test.diff :refer :all]
            [code.test.checker.common :as common]
            [code.test.checker.collection :as coll]))

(fact "checker-equal?"
  (checker-equal? 1 1) => true
  (checker-equal? 1 2) => false
  (checker-equal? even? 2) => true
  (checker-equal? even? 1) => false)

(fact "diff-map"
  (diff-map {:a 1} {:a 1}) => nil
  (diff-map {:a 1} {:a 2}) => {:> {:a 1}}
  (diff-map {:a 1} {}) => {:+ {:a 1}}
  (diff-map {} {:a 1}) => {:- {:a 1}}
  (diff-map {:a even?} {:a 2}) => nil
  (diff-map {:a even?} {:a 1}) => {:> {:a even?}})

(fact "diff-seq"
  (diff-seq [1 2 3] [1 2 3]) => nil
  (diff-seq [1 2 3] [1 2 4]) => [[:- 2 1] [:+ 2 [4]]]
  (diff-seq [1] []) => [[:- 0 1]]
  (diff-seq [] [1]) => [[:+ 0 [1]]])

(fact "diff contains"
  (keys (diff (coll/contains {:a 1}) {:a 2 :b 2}))
  => [:>]

  (keys (:> (diff (coll/contains {:a 1}) {:a 2 :b 2})))
  => [:a]

  (diff (coll/contains {:a 1}) {:a 1 :b 2})
  => nil)

(fact "diff just"
  (diff (coll/just {:a 1}) {:a 1 :b 2})
  => {:- {:b 2}})

(fact "diff large seq"
  (let [v1 (vec (range 20))
        v2 (concat (range 19) [99])]
    (diff-seq v1 v2))
  => [[:- 19 1] [:+ 19 [99]]])
