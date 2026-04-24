(ns std.lang.base.emit-assign-test
  (:require [std.lang.base.book :as b]
            [std.lang.base.emit-assign :as assign :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.library-snapshot-prep-test :as prep])
  (:use code.test))

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
(fact "assigns an inline form directly"

  (emit-def-assign-inline
   'j '[x.core/complex-fn (1)] +grammar+ {:lang :x
                                          :book +book-x+})
  => '(do* (var j := 1)
           (:= j (+ j (1)))))

^{:refer std.lang.base.emit-assign/emit-def-assign :added "3.0"}
(fact "emits a declare expression"

  (emit-def-assign :def-assign
                   {:raw "var"}
                   '(var :int i := 9, :const :int j := 10)
                   +grammar+
                   {})
  => "var int i = 9, const int j = 10"

  (emit-def-assign :def-assign
                   {:raw "var"}
                   '(var (:int i) 9)
                   +grammar+
                   {})
  => "var int i = 9")

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

  (assign/test-assign-loop '(var (:int a) 9)
                           +grammar+
                           {})
  => "int a = 9"


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
  => "(do* (var a := 1) (:= a (+ a 1)))"

  (assign/test-assign-loop '(var a := (hello 1 2))
                           (assoc-in +grammar+
                                     [:reserved 'hello]
                                     {:emit :macro
                                       :macro (fn [[_ x y]]
                                                (list 'do
                                                      (list 'var 'thread := (list '+ x y))
                                                      (list 'return 'thread)))})
                            {})
  => "(do* (var a := (+ 1 2)))")


^{:refer std.lang.base.emit-assign/assign-options :added "4.1"}
(fact "gets assignment options from reserved entries and metadata"
  (assign-options
   (with-meta '(hello 1)
     {:assign/fn (fn [sym]
                   (list 'var sym := 1))})
    {:reserved {'hello {:emit :macro
                        :assign/fn (fn [sym]
                                     (list 'var sym := 2))
                        :assign/inline 'x.core/identity-fn}}})
  => (contains {:assign/fn fn?
        :assign/inline x.core/identity-fn}

  (assign-options
   '(identity 1)
   +grammar+)
  => {})

^{:refer std.lang.base.emit-assign/assign-value :added "4.1"}
(fact "prepares assignment override payloads"
  (assign-value 'a
                (with-meta '(sym :as [1 2 3])
                  {:assign/fn (fn [sym]
                                (list sym :as [1 2 3]))})
                 +grammar+
                 {})
  => [:raw '(a :as [1 2 3])]

  (assign-value 'a
                (with-meta '(x.core/identity-fn 1)
                  {:assign/inline 'x.core/identity-fn})
                +grammar+
                {:lang :x
                 :snapshot +snap+})
  => [:inline '(do* (var a := 1))]

  (assign-value 'a
                '(hello 1 2)
                (assoc-in +grammar+
                          [:reserved 'hello]
                          {:emit :macro
                           :macro (fn [[_ x y]]
                                    (list 'do
                                          (list 'var 'thread := (list '+ x y))
                                          (list 'return 'thread)))})
                 {})
  => [:default '(do* (var a := (+ 1 2)))])

^{:refer std.lang.base.emit-assign/emit-def-assign-default :added "4.1"}
(fact "rewrites default assign forms from return bodies"
  (emit-def-assign-default
   'a
   '(do (var thread := (+ 1 2))
        (return thread)))
  => '(do* (var a := (+ 1 2)))

  (emit-def-assign-default
   'a
   '(do (hello)
        (return (+ 1 2))))
  => '(do* (hello)
           (var a := (+ 1 2)))))
