(ns hara.lang.rewrite.inline-do-test
  (:use code.test)
  (:require [hara.lang.rewrite.inline-do :as inline]))

^{:refer hara.lang.rewrite.inline-do/do-expression? :added "4.0"}
(fact "returns true for do and do* list forms"
  (inline/do-expression? '(do 1 2))
  => true)

^{:refer hara.lang.rewrite.inline-do/do-expression? :added "4.0"
  :id test-do-expression-star}
(fact "returns true for do* forms"
  (inline/do-expression? '(do* 1 2))
  => true)

^{:refer hara.lang.rewrite.inline-do/do-expression? :added "4.0"
  :id test-do-expression-non-do}
(fact "returns false for non-do forms"
  (inline/do-expression? '(if 1 2 3))
  => false)

^{:refer hara.lang.rewrite.inline-do/do-expression? :added "4.0"
  :id test-do-expression-non-list}
(fact "returns false for vectors and symbols"
  (inline/do-expression? '[do 1 2])
  => false

  (inline/do-expression? 'do)
  => false)

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do-list :added "4.0"}
(fact "rewrites a return wrapping a do expression"
  (inline/rewrite-inline-do-list '(return (do (step 1) 2)))
  => '(do* (step 1) (return 2)))

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do-list :added "4.0"
  :id test-rewrite-inline-do-list-star}
(fact "rewrites a return wrapping a do* expression"
  (inline/rewrite-inline-do-list '(return (do* (step 1) 2)))
  => '(do* (step 1) (return 2)))

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do-list :added "4.0"
  :id test-rewrite-inline-do-list-unchanged}
(fact "leaves plain do and non-return forms unchanged"
  (inline/rewrite-inline-do-list '(do (step 1) 2))
  => '(do (step 1) 2)

  (inline/rewrite-inline-do-list '(foo 1 2))
  => '(foo 1 2))

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do-list :added "4.0"
  :id test-rewrite-inline-do-list-nested}
(fact "recursively rewrites nested return-do forms"
  (inline/rewrite-inline-do-list '(foo (return (do a b))))
  => '(foo (do* a (return b))))

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do :added "4.1"}
(fact "rewrites inline do-return forms while preserving quoted forms"
  (inline/rewrite-inline-do
   '[(return (do (step 1) 2))
     (quote (return (do (step 3) 4)))])
  => '[(do* (step 1) (return 2))
       (quote (return (do (step 3) 4)))])

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do :added "4.0"
  :id test-rewrite-inline-do-nested-collections}
(fact "recursively rewrites forms inside nested collections"
  (inline/rewrite-inline-do
   '{:body (return (do (step 1) 2))})
  => '{:body (do* (step 1) (return 2))})
