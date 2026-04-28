(ns std.lang.rewrite.conditional-test
  (:use code.test)
  (:require [std.lang.rewrite.conditional :as condrw]))

^{:refer std.lang.rewrite.conditional/rewrite-conditional-expression-list :added "4.1"}
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

^{:refer std.lang.rewrite.conditional/rewrite-conditional-expression :added "4.1"}
(fact "rewrites containers before applying a truthy wrapper"
  (condrw/rewrite-conditional-expression
   '[a {:b c}]
   identity
   #(if (symbol? %)
      (list 'expr %)
      %)
   (fn [_ form]
     (list 'truthy form)))
  => '(truthy [(expr a) {:b (expr c)}]))
