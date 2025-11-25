(ns std.lang.base.grammar-macro-test
  (:use code.test)
  (:require [std.lang.base.grammar-macro :refer :all]))

^{:refer std.lang.base.grammar-macro/tf-macroexpand :added "3.0"}
(fact "macroexpands the current form"
  "placeholder for tests")

^{:refer std.lang.base.grammar-macro/tf-when :added "3.0"}
(fact "transforms `when` to branch"

  (tf-when '(when true :A :B :C))
  => '(br* (if true :A :B :C)))

^{:refer std.lang.base.grammar-macro/tf-if :added "3.0"}
(fact "transforms `if` to branch"

  (tf-if '(if true :A :B))
  => '(br* (if true :A) (else :B)))

^{:refer std.lang.base.grammar-macro/tf-cond :added "3.0"}
(fact "transforms `cond` to branch"
  "placeholder for tests")

^{:refer std.lang.base.grammar-macro/tf-let-bind :added "4.0"}
(fact "converts to a statement"
  ^:hidden
  
  (tf-let-bind '(let [#{x} 1 b 2]
                  (return (+ x b))))
  => '(do* (var #{x} := 1)
           (var b := 2)
           (return (+ x b))))

^{:refer std.lang.base.grammar-macro/tf-case :added "4.0"}
(fact "transforms the case statement to switch representation"
  ^:hidden
  
  (tf-case '(case (type obj)
              :A (return A)
              :B (return B)
              (return X)))
  => '(switch
       [(type obj)]
       (case [:A] (return A))
       (case [:B] (return B))
       (default (return X))))

^{:refer std.lang.base.grammar-macro/tf-lambda-arrow :added "4.0"}
(fact "generalized lambda transformation"
  ^:hidden
  
  (tf-lambda-arrow '(fn:> [e] e))
  => '(fn [e] (return e)))

^{:refer std.lang.base.grammar-macro/tf-tcond :added "4.0"}
(fact "transforms the ternary cond"

  (tf-tcond '(:?> :a a :b b :else c))
  => '(:? :a [a (:? :b [b c])]))

^{:refer std.lang.base.grammar-macro/tf-xor :added "4.0"}
(fact "transforms to xor using ternary if"

  (tf-xor '(xor a b))
  => '(:? a b (not b)))

^{:refer std.lang.base.grammar-macro/tf-doto :added "4.0"}
(fact "basic transformation for `doto` syntax"
  ^:hidden

  (tf-doto '(doto sym
              (. a)
              (b)
              (. c)))
  => '(do (. sym a) (b sym) (. sym c)))

^{:refer std.lang.base.grammar-macro/tf-do-arrow :added "4.0"}
(fact "do:> transformation"

  (tf-do-arrow '(do:>
                 1 2 (return 3)))
  => '((quote ((fn [] 1 2 (return 3))))))

^{:refer std.lang.base.grammar-macro/tf-forange :added "4.0"}
(fact "creates the forange form"

  (tf-forange '(forange [i 10] (print i)))
  => '(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))

  (tf-forange '(forange [i [10 3 -2]] (print i)))
  => '(for [(var i 10) (< i 3) [(:= i (- i 2))]] (print i)))
