(ns std.lang.model.spec-elisp-test
  (:require [std.lang :as l]
            [std.lang.model.spec-elisp :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-elisp/emit-elisp :added "4.1"}
(fact "emits code into emacs lisp schema"
  (emit-elisp '(defn hello [x] (return (== x nil))) {})
  => "(defun hello (x) (catch (quote __xt_return__) (throw (quote __xt_return__) (equal x nil))))")

(fact "emits elisp data structures"
  (emit-elisp {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash-table :test (quote equal)))) (progn (puthash \"a\" 1 __xt_tbl) (puthash \"b\" (vector 2 3) __xt_tbl) __xt_tbl))")

^{:refer std.lang.model.spec-elisp/+book+ :added "4.1"}
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
