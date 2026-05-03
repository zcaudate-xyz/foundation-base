(ns hara.lang.rewrite.conditional-test
  (:use code.test)
  (:require [hara.lang.rewrite.conditional :as condrw]))

^{:refer hara.lang.rewrite.conditional/rewrite-conditional-expression-list :added "4.1"}
(fact "rewrites shared conditional-expression skeletons"
  [(condrw/rewrite-conditional-expression-list
    '(or a b)
    identity
    #(list 'test %)
    #(list 'expr %))
   (condrw/rewrite-conditional-expression-list
    '(:? a b c)
    identity
    #(list 'test %)
    #(list 'expr %))
   (condrw/rewrite-conditional-expression-list
    '((f x) a)
    identity
    #(list 'test %)
    #(list 'expr %))]
  => ['(or (test a) (test b))
       '(:? (test a) (expr b) (expr c))
       '((expr (f x)) (expr a))])


^{:refer hara.lang.rewrite.conditional/rewrite-conditional-expression :added "4.1"}
(fact "TODO")