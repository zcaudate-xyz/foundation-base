tahto/common/grammar_macro_test.clj:1:(ns tahto.common.grammar-macro-test
tahto/common/grammar_macro_test.clj:2:  (:require [tahto.common.grammar-macro :refer :all])
  (:use code.test))

tahto/common/grammar_macro_test.clj:5:^{:refer tahto.common.grammar-macro/tf-macroexpand :added "3.0"}
(fact "macroexpands the current form"
  (tf-macroexpand '(-> 1 (inc)))
  => '(inc 1))

tahto/common/grammar_macro_test.clj:10:^{:refer tahto.common.grammar-macro/tf-when :added "3.0"}
(fact "transforms `when` to branch"

  (tf-when '(when true :A :B :C))
  => '(br* (if true :A :B :C)))

tahto/common/grammar_macro_test.clj:16:^{:refer tahto.common.grammar-macro/tf-if :added "3.0"}
(fact "transforms `if` to branch"

  (tf-if '(if true :A :B))
  => '(br* (if true :A) (else :B)))

tahto/common/grammar_macro_test.clj:22:^{:refer tahto.common.grammar-macro/tf-cond :added "3.0"}
(fact "transforms `cond` to branch"
  (tf-cond '(cond true :A false :B))
  => '(br* (if true :A) (elseif false :B))

  (tf-cond '(cond true :A false :B :else :C))
  => '(br* (if true :A) (elseif false :B) (else :C)))

tahto/common/grammar_macro_test.clj:30:^{:refer tahto.common.grammar-macro/tf-let-bind :added "4.0"}
(fact "converts to a statement"

  (tf-let-bind '(let [#{x} 1 b 2]
                  (return (+ x b))))
  => '(do* (var #{x} := 1)
           (var b := 2)
           (return (+ x b))))

tahto/common/grammar_macro_test.clj:39:^{:refer tahto.common.grammar-macro/tf-case :added "4.0"}
(fact "transforms the case statement to switch representation"

  (tf-case '(case (type obj)
              :A (return A)
              :B (return B)
              (return X)))
  => '(switch
       [(type obj)]
       (case [:A] (return A))
       (case [:B] (return B))
       (default (return X))))

tahto/common/grammar_macro_test.clj:52:^{:refer tahto.common.grammar-macro/tf-lambda-arrow :added "4.0"}
(fact "generalized lambda transformation"

  (tf-lambda-arrow '(fn:> [e] e))
  => '(fn [e] (return e)))

tahto/common/grammar_macro_test.clj:58:^{:refer tahto.common.grammar-macro/tf-tcond :added "4.0"}
(fact "transforms the ternary cond"

  (tf-tcond '(:?> :a a :b b :else c))
  => '(:? :a [a (:? :b [b c])]))

tahto/common/grammar_macro_test.clj:64:^{:refer tahto.common.grammar-macro/tf-xor :added "4.0"}
(fact "transforms xor into boolean-normalized equality"

  (tf-xor '(xor a b))
  => '(== (not (not a)) (not (not b))))

tahto/common/grammar_macro_test.clj:70:^{:refer tahto.common.grammar-macro/tf-doto :added "4.0"}
(fact "basic transformation for `doto` syntax"

  (tf-doto '(doto sym
              (. a)
              (b)
              (. c)))
  => '(do (. sym a) (b sym) (. sym c)))

tahto/common/grammar_macro_test.clj:79:^{:refer tahto.common.grammar-macro/tf-do-arrow :added "4.0"}
(fact "do:> transformation"

  (tf-do-arrow '(do:>
                 1 2 (return 3)))
  => '((fn [] 1 2 (return 3))))

tahto/common/grammar_macro_test.clj:86:^{:refer tahto.common.grammar-macro/tf-forange :added "4.0"}
(fact "creates the forange form"

  (tf-forange '(forange [i 10] (print i)))
  => '(for [(var i 0) (< i 10) [(:= i (+ i 1))]] (print i))

  (tf-forange '(forange [i [10 3 -2]] (print i)))
  => '(for [(var i 10) (< i 3) [(:= i (- i 2))]] (print i)))
