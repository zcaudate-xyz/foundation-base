(ns rt.fortran.grammar-test
  (:use code.test)
  (:require [rt.fortran.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

(defn emit [form]
  (let [lib (std.lang.base.library/library:create
             {:snapshot
              {:fortran {:book rt.fortran.grammar/+book+}}})]
    (l/emit-as :fortran form {:library lib})))

^{:refer rt.fortran.grammar/fortran-type :added "4.1"}
(fact "formats fortran types"
  (fortran-type "int") => "INTEGER"
  (fortran-type "float") => "REAL"
  (fortran-type "unknown") => "UNKNOWN")

^{:refer rt.fortran.grammar/fortran-args :added "4.1"}
(fact "custom Fortran argument emission"
  (l/emit-as :fortran
    ['(defn my_sub [[int a] [float b]]
           (print a b))])
  => "SUBROUTINE my_sub (a, b) \n INTEGER :: a\nREAL :: b\nPRINT *, a , b; END SUBROUTINE my_sub")

^{:refer rt.fortran.grammar/fortran-decl :added "4.1"}
(fact "declaration helper"
  (l/emit-as :fortran
    ['(defn ^{:tag int} foo [[int x] [float y]]
         (return (+ x y)))])
  => "INTEGER FUNCTION foo (x, y) \n INTEGER :: x\nREAL :: y\nRETURN x + y; END FUNCTION foo")

^{:refer rt.fortran.grammar/fortran-defn :added "4.1"}
(fact "transforms defn to SUBROUTINE or FUNCTION"
  (l/emit-as :fortran
    ['(defn my_sub [[int a] [float b]]
           (print a b))])
  => "SUBROUTINE my_sub (a, b) \n INTEGER :: a\nREAL :: b\nPRINT *, a , b; END SUBROUTINE my_sub"

  (l/emit-as :fortran
    ['(defn ^{:tag float} my_func [[int x]]
           (:= my_func (* x 2.0))
           (return))])
  => "REAL FUNCTION my_func (x) \n INTEGER :: x\nmy_func = (x * 2.0);\nRETURN; END FUNCTION my_func")

^{:refer rt.fortran.grammar/fortran-defprogram :added "4.1"}
(fact "transforms defprogram to PROGRAM"
  (l/emit-as :fortran
    ['(program my_prog
           (print "Hello World"))])
  => "PROGRAM my_prog \n IMPLICIT NONE\n PRINT *, 'Hello World'; END PROGRAM my_prog")

^{:refer rt.fortran.grammar/fortran-def :added "4.1"}
(fact "transforms def to declaration and assignment"
  (l/emit-as :fortran
    ['(def ^{:tag int} x 10)
     '(def ^{:tag float} y)])
  => "INTEGER :: x = 10\n\nREAL :: y")

^{:refer rt.fortran.grammar/fortran-print :added "4.1"}
(fact "transforms print"
  (l/emit-as :fortran
    ['(print "Hello" 123)])
  => "PRINT *, 'Hello' , 123")

^{:refer rt.fortran.grammar/fortran-module :added "4.1"}
(fact "transforms module"
  (l/emit-as :fortran
    ['(module MyMod
        (def ^{:tag int} x 5))])
  => "MODULE MyMod \n IMPLICIT NONE\n INTEGER :: x = 5 END MODULE MyMod")

^{:refer rt.fortran.grammar/fortran-emit-if :added "4.1"}
(fact "custom if emission"
  (l/emit-as :fortran
    ['(if (== i 5)
        (print "Halfway")
        (print i))])
  => "IF (i == 5) THEN\n  PRINT *, 'Halfway'\nELSE\n  PRINT *, i\nEND IF")

^{:refer rt.fortran.grammar/fortran-emit-for :added "4.1"}
(fact "custom do loop emission"
  (l/emit-as :fortran
    ['(do:loop [i 1 10]
        (print i))])
  => "DO i = 1, 10\n  PRINT *, i\nEND DO")
