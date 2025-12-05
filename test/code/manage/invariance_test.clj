(ns code.manage.invariance-test
  (:require [code.manage.invariance :refer :all]
            [code.test :refer :all]))

^{:refer code.manage.invariance/normalize :added "4.0"}
(fact "normalizes a block for invariance calculation"
  (-> (analyse-string "(defn a [x] (+ x 1))")
      first
      :norm)
  => (-> (analyse-string "(defn b [y] (- y 2))")
         first
         :norm)

  (-> (analyse-string "(defn a [x] (if x 1 2))")
      first
      :norm)
  => (partial not= (-> (analyse-string "(defn b [y] (map y 1 2))")
                       first
                       :norm)))

^{:refer code.manage.invariance/score :added "4.0"}
(fact "calculates a complexity score for a normalized node"
  (score (-> (analyse-string "(+ 1 2)") first :norm))
  => 4

  (score (-> (analyse-string "(if a b c)") first :norm))
  => 5)

^{:refer code.manage.invariance/structure-hash :added "4.0"}
(fact "calculates a hash for the normalized structure"
  (structure-hash (-> (analyse-string "(+ 1 2)") first :norm))
  => number?)
