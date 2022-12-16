(ns std.lang.base.emit-assign-test
  (:use code.test)
  (:require [std.lang.base.emit-assign :refer :all :as assign]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as b]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.library-snapshot-prep-test :as prep]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))


(def +x-code-complex-fn+
  (b/book-entry {:lang :x
                 :id 'complex-fn
                 :module 'x.core
                 :section :code
                 :form '(defn complex-fn [x]
                          (var a := 1)
                          (:= a (+ a x))
                          (return a))
                 :deps #{}
                 :namespace 'x.core
                 :declared false}))

(def +book-x+
  (-> (b/set-entry prep/+book-x+ +x-code-complex-fn+)
      second))

(def +snap+
  (snap/add-book prep/+snap+ +book-x+))

^{:refer std.lang.base.emit-assign/emit-def-assign-inline :added "4.0"}
(fact "assigns an inline form")

^{:refer std.lang.base.emit-assign/emit-def-assign :added "3.0"}
(fact "emits a declare expression"
  ^:hidden

  (emit-def-assign :def-assign
                   {:raw "var"}
                   '(var :int i := 9, :const :int j := 10)
                   +grammar+
                {})
  => "var int i = 9, const int j = 10")

^{:refer std.lang.base.emit-assign/test-assign-loop :adopt true :added "4.0"}
(fact "emit do"

  (assign/test-assign-loop '(var a 1)
                           +grammar+
                           {})
  => "a = 1"

  (assign/test-assign-loop '(var :int [] a)
                           +grammar+
                           {})
  => "int a[]"

  (assign/test-assign-loop '(var :int :* a)
                           +grammar+
                           {})
  => "int * a"
  
  
  (assign/test-assign-loop '(var :const a (+ b1 2))
                           +grammar+
                           {})
  => "const a = (+ b1 2)"
  
  
  (assign/test-assign-emit '(var a (+ 1 2))
                           +grammar+
                           {})
  => "a = 1 + 2"
  
  (assign/test-assign-emit '(var :const a (+ b1 2))
                           +grammar+
                           {})
  => "const a = b1 + 2")

^{:refer std.lang.base.emit-assign/test-assign-emit :added "4.0"}
(fact "emit assign forms"

  (assign/test-assign-loop (list 'var 'a := (with-meta ()
                                              {:assign/fn (fn [sym]
                                                            (list sym :as [1 2 3]))}))
                           +grammar+
                           {})
  => "(a :as [1 2 3])"

  (assign/test-assign-loop (list 'var 'a := (with-meta '(sym :as [1 2 3])
                                              {:assign/template 'sym}))
                           +grammar+
                           {})
  => "(a :as [1 2 3])"

  (assign/test-assign-loop (list 'var 'a := (with-meta '(x.core/identity-fn 1)
                                              {:assign/inline 'x.core/identity-fn}))
                           +grammar+
                           {:lang :x
                            :snapshot +snap+})
  => "(do* (var a := 1))"

  (assign/test-assign-loop (list 'var 'a := (with-meta '(x.core/complex-fn 1)
                                              {:assign/inline 'x.core/complex-fn}))
                           +grammar+
                           {:lang :x
                            :snapshot +snap+})
  => "(do* (var a := 1) (:= a (+ a 1)))")
