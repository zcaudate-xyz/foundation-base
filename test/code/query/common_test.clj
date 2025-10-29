(ns code.query.common-test
  (:use [code.test :exclude [any]])
  (:require [code.query.common :refer :all]))

^{:refer code.query.common/any :added "3.0"}
(fact "returns true for any value"
  (any nil) => true
  (any '_) => true)

^{:refer code.query.common/none :added "3.0"}
(fact "returns false for any value"
  (none nil) => false
  (none '_) => false)

^{:refer code.query.common/expand-meta :added "3.0"}
(fact "separates out the meta into individual flags"

  (meta (expand-meta ^:? ()))
  => {:? true}

  (meta (expand-meta ^:+%? ()))
  => {:+ true, :? true, :% true})

^{:refer code.query.common/cursor? :added "3.0"}
(fact "checks if element is `|`"
  (cursor? '|) => true
  (cursor? '_) => false)

^{:refer code.query.common/insertion? :added "3.0"}
(fact "checks if element has an insert meta"
  (insertion? '^:+ a) => true
  (insertion? 'a) => false)

^{:refer code.query.common/deletion? :added "3.0"}
(fact "checks if element has a delete meta"
  (deletion? '^:- a) => true
  (deletion? 'a) => false)

^{:refer code.query.common/prewalk :added "3.0"}
(fact "applies a function to elements in a depth-first, pre-order traversal, modifying them eagerly"))

^{:refer code.query.common/remove-items :added "3.0"}
(fact "removes items from a form matching the predicate"
  (remove-items #{1} '(1 2 3 4))
  => '(2 3 4)

  (remove-items #{1} '(1 (1 (1 (1)))))
  => '(((()))))

^{:refer code.query.common/prepare-deletion :added "3.0"}
(fact "removes extraneous symbols for deletion walk"
  (prepare-deletion '(+ a 2))
  => '(+ a 2)

  (prepare-deletion '(+ ^:+ a | 2))
  => '(+ 2))

^{:refer code.query.common/prepare-insertion :added "3.0"}
(fact "removes extraneous symbols from a form in preparation for an insertion operation")
  (prepare-insertion '(+ a 2))
  => '(+ a 2)

  (prepare-insertion '(+ ^:+ a | ^:- b 2))
  => '(+ a 2))

^{:refer code.query.common/prepare-query :added "3.0"}
(fact "removes extraneous symbols for query walk"

  (prepare-query '(+ ^:+ a | ^:- b 2))
  => '(+ 2))

^{:refer code.query.common/find-index :added "3.0"}
(fact "returns the index of the first occurrence"
  (find-index #{2} '(1 2 3 4))
  => 1)

^{:refer code.query.common/finto :added "3.0"}
(fact "into but the right way for lists"
  (finto () '(1 2 3))
  => '(1 2 3))
