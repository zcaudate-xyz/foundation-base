(ns hara.lang.rewrite.unpack-test
  (:use code.test)
  (:require [hara.lang.rewrite.unpack :as unpack]))

^{:refer hara.lang.rewrite.unpack/unpack-form? :added "4.0"}
(fact "checks whether a form is an unpacking expression"
  [(unpack/unpack-form? '(x:unpack xs))
   (unpack/unpack-form? '(x:unpack))
   (unpack/unpack-form? '(x:unpack xs ys))
   (unpack/unpack-form? '(call xs))
   (unpack/unpack-form? 'x:unpack)
   (unpack/unpack-form? nil)
   (unpack/unpack-form? '[x:unpack xs])]
  => [true false false false false false false])

^{:refer hara.lang.rewrite.unpack/any-unpack? :added "4.0"}
(fact "checks whether any argument is an unpacking expression"
  [(unpack/any-unpack? '((x:unpack xs) y))
   (unpack/any-unpack? '((x:unpack a) (x:unpack b)))
   (unpack/any-unpack? '(x y))
   (unpack/any-unpack? '())]
  => [true true false false])

^{:refer hara.lang.rewrite.unpack/rewrite-arg :added "4.0"}
(fact "rewrites a single argument, dispatching on unpacking forms"
  [(unpack/rewrite-arg '(x:unpack xs)
                       #(list 'expr %)
                       identity
                       #(list 'spread %))
   (unpack/rewrite-arg 'y
                       #(list 'expr %)
                       identity
                       #(list 'spread %))
   (unpack/rewrite-arg 'y
                       #(list 'expr %)
                       #(list 'scalar %)
                       #(list 'spread %))]
  => '[(spread (expr xs))
       (expr y)
       (scalar (expr y))])

^{:refer hara.lang.rewrite.unpack/rewrite-args :added "4.1"}
(fact "detects and rewrites unpack args with injected wrappers"
  [(unpack/unpack-form? '(x:unpack xs))
   (unpack/unpack-form? '(call xs))
   (unpack/any-unpack? '((x:unpack xs) y))
   (unpack/any-unpack? '(x y))
   (vec (unpack/rewrite-args '((x:unpack xs) y)
                             #(list 'expr %)
                             identity
                             #(list 'spread %)))
   (vec (unpack/rewrite-args '((x:unpack xs) y)
                             #(list 'expr %)
                             #(list 'scalar %)
                             #(list 'seq %)))]
  => [true
      false
      true
      false
      '[(spread (expr xs))
        (expr y)]
      '[(seq (expr xs))
        (scalar (expr y))]])