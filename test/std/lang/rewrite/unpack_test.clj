(ns std.lang.rewrite.unpack-test
  (:use code.test)
  (:require [std.lang.rewrite.unpack :as unpack]))

^{:refer std.lang.rewrite.unpack/rewrite-args :added "4.1"}
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
