tahto/model/spec_scheme_test.clj:1:(ns tahto.model.spec-scheme-test
  (:require [tahto.core :as l]
tahto/model/spec_scheme_test.clj:3:            [tahto.model.spec-scheme :refer :all])
  (:use code.test))

(fact "emits scheme data structures"
  (emit-scheme {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash))) (begin (hash-set! __xt_tbl \"a\" 1) (hash-set! __xt_tbl \"b\" (vector 2 3)) __xt_tbl))")

tahto/model/spec_scheme_test.clj:10:^{:refer tahto.model.spec-scheme/+book+ :added "4.1"}
(fact "emits xtalk through the scheme backend"
  (l/emit-as :scheme '[(x:print (x:cat "a" "b"))])
  => "(begin (display (string-append \"a\" \"b\")) #f)")

(fact "emits named and empty lambdas through the scheme backend"
  [(emit-scheme '(fn named [x] (return x)) {})
   (emit-scheme '(fn []) {})]
  => ["(lambda (x) x)"
      "(lambda () (void))"])

tahto/model/spec_scheme_test.clj:21:^{:refer tahto.model.spec-scheme/scheme-tf-break :added "4.1"}
(fact "transforms break forms")

tahto/model/spec_scheme_test.clj:24:^{:refer tahto.model.spec-scheme/scheme-tf-bsl :added "4.1"}
(fact "left shifts bits")

tahto/model/spec_scheme_test.clj:27:^{:refer tahto.model.spec-scheme/scheme-tf-bsr :added "4.1"}
(fact "right shifts bits")

tahto/model/spec_scheme_test.clj:30:^{:refer tahto.model.spec-scheme/scheme-tf-bxor :added "4.1"}
(fact "computes bitwise XOR")

tahto/model/spec_scheme_test.clj:33:^{:refer tahto.model.spec-scheme/scheme-tf-band :added "4.1"}
(fact "computes bitwise AND")

tahto/model/spec_scheme_test.clj:36:^{:refer tahto.model.spec-scheme/scheme-tf-bor :added "4.1"}
(fact "computes bitwise OR")

tahto/model/spec_scheme_test.clj:39:^{:refer tahto.model.spec-scheme/scheme-tf-mod :added "4.1"}
(fact "computes modulo")

tahto/model/spec_scheme_test.clj:42:^{:refer tahto.model.spec-scheme/scheme-tf-pow :added "4.1"}
(fact "computes power")

tahto/model/spec_scheme_test.clj:45:^{:refer tahto.model.spec-scheme/scheme-tf-xor :added "4.1"}
(fact "computes XOR")

tahto/model/spec_scheme_test.clj:48:^{:refer tahto.model.spec-scheme/scheme-tf-throw :added "4.1"}
(fact "transforms throw forms")

tahto/model/spec_scheme_test.clj:51:^{:refer tahto.model.spec-scheme/scheme-tf-for-array :added "4.1"}
(fact "transforms for:array loops")

tahto/model/spec_scheme_test.clj:54:^{:refer tahto.model.spec-scheme/scheme-tf-for-object :added "4.1"}
(fact "transforms for:object loops")

tahto/model/spec_scheme_test.clj:57:^{:refer tahto.model.spec-scheme/scheme-tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

tahto/model/spec_scheme_test.clj:60:^{:refer tahto.model.spec-scheme/scheme-tf-for-index :added "4.1"}
(fact "transforms for:index loops")

tahto/model/spec_scheme_test.clj:63:^{:refer tahto.model.spec-scheme/scheme-expand :added "4.1"}
(fact "expands scheme forms")

tahto/model/spec_scheme_test.clj:66:^{:refer tahto.model.spec-scheme/scheme-transform :added "4.1"}
(fact "transforms scheme forms")

tahto/model/spec_scheme_test.clj:69:^{:refer tahto.model.spec-scheme/emit-scheme-coll :added "4.1"}
(fact "emits scheme collections")

tahto/model/spec_scheme_test.clj:72:^{:refer tahto.model.spec-scheme/emit-scheme-map :added "4.1"}
(fact "emits scheme maps")

tahto/model/spec_scheme_test.clj:75:^{:refer tahto.model.spec-scheme/emit-scheme-form :added "4.1"}
(fact "emits scheme forms")

tahto/model/spec_scheme_test.clj:78:^{:refer tahto.model.spec-scheme/emit-scheme :added "4.0"}
(fact "emits code into scheme schema"
  (emit-scheme '(defn hello [x] (return (== x nil))) {})
  => "(define (hello x) (equal? x #f))")
