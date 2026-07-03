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
(fact "rewrites a form and wraps it for truthiness checks"
  (letfn [(rewrite-cond-expr [form]
            (condrw/rewrite-conditional-expression
             form
             #(condrw/rewrite-conditional-expression-list
               % identity rewrite-cond-expr (fn [x] (list 'expr x)))
             #(list 'expr %)
             (fn [source form] (list 'truthy source form))))]
    [(rewrite-cond-expr 'a)
     (rewrite-cond-expr '(or a b))
     (rewrite-cond-expr '(not a))
     (rewrite-cond-expr '(:? a b c))
     (rewrite-cond-expr '(f a))]
    => ['(truthy a a)
        '(truthy (or a b) (or (truthy a a) (truthy b b)))
        '(truthy (not a) (not (truthy a a)))
        '(truthy (:? a b c) (:? (truthy a a) (expr b) (expr c)))
        '(truthy (f a) (f (expr a)))]))