(ns hara.lang.model.spec-scheme-test
  (:require [hara.lang :as l]
            [hara.lang.model.spec-scheme :refer :all])
  (:use code.test))

^{:refer hara.lang.model.spec-scheme/emit-scheme :added "4.0"}
(fact "emits code into scheme schema"
  (emit-scheme '(defn hello [x] (return (== x nil))) {})
  => "(define (hello x) (equal? x #f))")

(fact "emits scheme data structures"
  (emit-scheme {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash))) (begin (hash-set! __xt_tbl \"a\" 1) (hash-set! __xt_tbl \"b\" (vector 2 3)) __xt_tbl))")

^{:refer hara.lang.model.spec-scheme/+book+ :added "4.1"}
(fact "emits xtalk through the scheme backend"
  (l/emit-as :scheme '[(x:print (x:cat "a" "b"))])
  => "(begin (display (string-append \"a\" \"b\")) #f)")

(fact "emits named and empty lambdas through the scheme backend"
  [(emit-scheme '(fn named [x] (return x)) {})
   (emit-scheme '(fn []) {})]
  => ["(lambda (x) x)"
      "(lambda () (void))"])
