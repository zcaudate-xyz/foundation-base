(ns hara.model.spec-elisp-test
  (:require [hara.lang :as l]
            [hara.model.spec-elisp :refer :all])
  (:use code.test))

(fact "emits elisp data structures"
  (emit-elisp {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash-table :test (quote equal)))) (progn (puthash \"a\" 1 __xt_tbl) (puthash \"b\" (vector 2 3) __xt_tbl) __xt_tbl))")

^{:refer hara.model.spec-elisp/+book+ :added "4.1"}
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

^{:refer hara.model.spec-elisp/elisp-tf-break :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-bsl :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-bsr :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-bxor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-band :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-bor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-mod :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-pow :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-xor :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-throw :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-for-array :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-for-object :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-for-iter :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-tf-for-index :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-expand :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-invoke :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-normalize-funcalls :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/elisp-transform :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/emit-elisp-coll :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/emit-elisp-map :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/emit-elisp-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-elisp/emit-elisp :added "4.1"}
(fact "emits code into emacs lisp schema"
  (emit-elisp '(defn hello [x] (return (== x nil))) {})
  => "(defun hello (x) (catch (quote __xt_return__) (throw (quote __xt_return__) (equal x nil))))")