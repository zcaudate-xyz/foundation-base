(ns std.lang.model.spec-elisp-test
  (:require [std.lang :as l]
            [std.lang.model.spec-elisp :refer :all])
  (:use code.test))

^{:refer std.lang.model.spec-elisp/emit-elisp :added "4.1"}
(fact "emits code into emacs lisp schema"
  (emit-elisp '(defn hello [x] (return (== x nil))) {})
  => "(defun hello (x) (equal x nil))")

(fact "emits elisp data structures"
  (emit-elisp {:a 1 :b [2 3]} {})
  => "(let ((__xt_tbl (make-hash-table :test (quote equal)))) (progn (puthash \"a\" 1 __xt_tbl) (puthash \"b\" [2 3] __xt_tbl) __xt_tbl))")

^{:refer std.lang.model.spec-elisp/+book+ :added "4.1"}
(fact "emits xtalk through the elisp backend"
  (l/emit-as :elisp '[(x:print (x:cat "a" "b"))])
  => "(princ (concat \"a\" \"b\"))")
