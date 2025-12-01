(ns rt.fortran.grammar-test
  (:use code.test)
  (:require [rt.fortran.grammar :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

(defn emit [form]
  (let [lib (std.lang.base.library/library:create
             {:snapshot
              {:fortran {:book rt.fortran.grammar/+book+}}})]
    (l/emit-script form {:lang :fortran :library lib})))

(fact "basic emission"
  (emit '(program my_prog
           (print "Hello World")))
  => "PROGRAM my_prog \n IMPLICIT NONE\n PRINT *, 'Hello World'; END PROGRAM my_prog")

(fact "subroutine emission"
  (emit '(defn my_sub [[int a] [float b]]
           (print a b)))
  => (std.string/join "\n" ["SUBROUTINE my_sub (a, b) "
                            " INTEGER :: a"
                            "REAL :: b"
                            "PRINT *, a , b; END SUBROUTINE my_sub"]))

(fact "function emission"
  (emit '(defn ^{:tag float} my_func [[int x]]
           (:= my_func (* x 2.0))
           (return)))
  => (std.string/join "\n" ["REAL FUNCTION my_func (x) "
                            " INTEGER :: x"
                            "my_func = (x * 2.0);"
                            "RETURN; END FUNCTION my_func"]))

(fact "control structures"
  (emit '(do:loop [i 1 10]
           (if (== i 5)
             (print "Halfway")
             (print i))))
  => "DO i = 1, 10\n  IF (i == 5) THEN\n    PRINT *, 'Halfway'\n  ELSE\n    PRINT *, i\n  END IF\nEND DO")
