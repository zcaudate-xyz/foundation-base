tahto/model/spec_elisp_test.clj:1:(ns tahto.model.spec-elisp-test
  (:require [tahto.core :as l]
tahto/model/spec_elisp_test.clj:3:            [tahto.model.spec-elisp :refer :all])
  (:use code.test))

(fact "emits elisp data structures"
  (emit-elisp {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash-table :test (quote equal)))) (progn (puthash \"a\" 1 __xt_tbl) (puthash \"b\" (vector 2 3) __xt_tbl) __xt_tbl))")

tahto/model/spec_elisp_test.clj:10:^{:refer tahto.model.spec-elisp/+book+ :added "4.1"}
(fact "emits xtalk through the elisp backend"
  (l/emit-as :elisp '[(x:print (x:cat "a" "b"))])
  => "(progn (princ (concat \"a\" \"b\")) nil)")

(fact "emits named lambdas through the elisp backend"
  (emit-elisp '(fn named [x] (return x)) {})
  => "(lambda (x) (catch (quote __xt_return__) (throw (quote __xt_return__) x)))")

(fact "emits funcall for locally bound function values"
  (emit-elisp '(defn outer [pre-fn x] (return (pre-fn x))) {})
  => "(defun outer (pre-fn x) (catch (quote __xt_return__) (throw (quote __xt_return__) (funcall pre-fn x))))")

(fact "emits namespaced function refs as function values"
  (emit-elisp '(list xtt/eq-nested-obj xtt/eq-nested-arr) {})
  => "(list (symbol-function (quote eq-nested-obj)) (symbol-function (quote eq-nested-arr)))")

tahto/model/spec_elisp_test.clj:27:^{:refer tahto.model.spec-elisp/elisp-tf-break :added "4.1"}
(fact "transforms break forms")

tahto/model/spec_elisp_test.clj:30:^{:refer tahto.model.spec-elisp/elisp-tf-bsl :added "4.1"}
(fact "left shifts bits")

tahto/model/spec_elisp_test.clj:33:^{:refer tahto.model.spec-elisp/elisp-tf-bsr :added "4.1"}
(fact "right shifts bits")

tahto/model/spec_elisp_test.clj:36:^{:refer tahto.model.spec-elisp/elisp-tf-bxor :added "4.1"}
(fact "computes bitwise XOR")

tahto/model/spec_elisp_test.clj:39:^{:refer tahto.model.spec-elisp/elisp-tf-band :added "4.1"}
(fact "computes bitwise AND")

tahto/model/spec_elisp_test.clj:42:^{:refer tahto.model.spec-elisp/elisp-tf-bor :added "4.1"}
(fact "computes bitwise OR")

tahto/model/spec_elisp_test.clj:45:^{:refer tahto.model.spec-elisp/elisp-tf-mod :added "4.1"}
(fact "computes modulo")

tahto/model/spec_elisp_test.clj:48:^{:refer tahto.model.spec-elisp/elisp-tf-pow :added "4.1"}
(fact "computes power")

tahto/model/spec_elisp_test.clj:51:^{:refer tahto.model.spec-elisp/elisp-tf-xor :added "4.1"}
(fact "computes XOR")

tahto/model/spec_elisp_test.clj:54:^{:refer tahto.model.spec-elisp/elisp-tf-throw :added "4.1"}
(fact "transforms throw forms")

tahto/model/spec_elisp_test.clj:57:^{:refer tahto.model.spec-elisp/elisp-tf-for-array :added "4.1"}
(fact "transforms for:array loops")

tahto/model/spec_elisp_test.clj:60:^{:refer tahto.model.spec-elisp/elisp-tf-for-object :added "4.1"}
(fact "transforms for:object loops")

tahto/model/spec_elisp_test.clj:63:^{:refer tahto.model.spec-elisp/elisp-tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

tahto/model/spec_elisp_test.clj:66:^{:refer tahto.model.spec-elisp/elisp-tf-for-index :added "4.1"}
(fact "transforms for:index loops")

tahto/model/spec_elisp_test.clj:69:^{:refer tahto.model.spec-elisp/elisp-expand :added "4.1"}
(fact "expands elisp forms")

tahto/model/spec_elisp_test.clj:72:^{:refer tahto.model.spec-elisp/elisp-invoke :added "4.1"}
(fact "emits elisp invocations")

tahto/model/spec_elisp_test.clj:75:^{:refer tahto.model.spec-elisp/elisp-normalize-funcalls :added "4.1"}
(fact "normalizes elisp function calls")

tahto/model/spec_elisp_test.clj:78:^{:refer tahto.model.spec-elisp/elisp-transform :added "4.1"}
(fact "transforms elisp forms")

tahto/model/spec_elisp_test.clj:81:^{:refer tahto.model.spec-elisp/emit-elisp-coll :added "4.1"}
(fact "emits elisp collections")

tahto/model/spec_elisp_test.clj:84:^{:refer tahto.model.spec-elisp/emit-elisp-map :added "4.1"}
(fact "emits elisp maps")

tahto/model/spec_elisp_test.clj:87:^{:refer tahto.model.spec-elisp/emit-elisp-form :added "4.1"}
(fact "emits elisp forms")

tahto/model/spec_elisp_test.clj:90:^{:refer tahto.model.spec-elisp/emit-elisp :added "4.1"}
(fact "emits code into emacs lisp schema"
  (emit-elisp '(defn hello [x] (return (== x nil))) {})
  => "(defun hello (x) (catch (quote __xt_return__) (throw (quote __xt_return__) (equal x nil))))")
