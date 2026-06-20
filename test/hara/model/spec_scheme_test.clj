(ns hara.model.spec-scheme-test
  (:require [hara.lang :as l]
            [hara.model.spec-scheme :refer :all])
  (:use code.test))

(fact "emits scheme data structures"
  (emit-scheme {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash))) (begin (hash-set! __xt_tbl \"a\" 1) (hash-set! __xt_tbl \"b\" (vector 2 3)) __xt_tbl))")

^{:refer hara.model.spec-scheme/+book+ :added "4.1"}
(fact "emits xtalk through the scheme backend"
  (l/emit-as :scheme '[(x:print (x:cat "a" "b"))])
  => "(begin (display (string-append \"a\" \"b\")) #f)")

(fact "emits named and empty lambdas through the scheme backend"
  [(emit-scheme '(fn named [x] (return x)) {})
   (emit-scheme '(fn []) {})]
  => ["(lambda (x) x)"
      "(lambda () (void))"])

^{:refer hara.model.spec-scheme/scheme-tf-break :added "4.1"}
(fact "transforms break forms")

^{:refer hara.model.spec-scheme/scheme-tf-bsl :added "4.1"}
(fact "left shifts bits")

^{:refer hara.model.spec-scheme/scheme-tf-bsr :added "4.1"}
(fact "right shifts bits")

^{:refer hara.model.spec-scheme/scheme-tf-bxor :added "4.1"}
(fact "computes bitwise XOR")

^{:refer hara.model.spec-scheme/scheme-tf-band :added "4.1"}
(fact "computes bitwise AND")

^{:refer hara.model.spec-scheme/scheme-tf-bor :added "4.1"}
(fact "computes bitwise OR")

^{:refer hara.model.spec-scheme/scheme-tf-mod :added "4.1"}
(fact "computes modulo")

^{:refer hara.model.spec-scheme/scheme-tf-pow :added "4.1"}
(fact "computes power")

^{:refer hara.model.spec-scheme/scheme-tf-xor :added "4.1"}
(fact "computes XOR")

^{:refer hara.model.spec-scheme/scheme-tf-throw :added "4.1"}
(fact "transforms throw forms")

^{:refer hara.model.spec-scheme/scheme-tf-for-array :added "4.1"}
(fact "transforms for:array loops")

^{:refer hara.model.spec-scheme/scheme-tf-for-object :added "4.1"}
(fact "transforms for:object loops")

^{:refer hara.model.spec-scheme/scheme-tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer hara.model.spec-scheme/scheme-tf-for-index :added "4.1"}
(fact "transforms for:index loops")

^{:refer hara.model.spec-scheme/scheme-expand :added "4.1"}
(fact "expands scheme forms")

^{:refer hara.model.spec-scheme/scheme-transform :added "4.1"}
(fact "transforms scheme forms")

^{:refer hara.model.spec-scheme/emit-scheme-coll :added "4.1"}
(fact "emits scheme collections")

^{:refer hara.model.spec-scheme/emit-scheme-map :added "4.1"}
(fact "emits scheme maps")

^{:refer hara.model.spec-scheme/emit-scheme-form :added "4.1"}
(fact "emits scheme forms")

^{:refer hara.model.spec-scheme/emit-scheme :added "4.0"}
(fact "emits code into scheme schema"
  (emit-scheme '(defn hello [x] (return (== x nil))) {})
  => "(define (hello x) (equal? x #f))")
