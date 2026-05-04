(ns hara.model.annex.spec-fortran-test
  (:use code.test)
  (:require [hara.lang.base.impl :as impl]
            [hara.model.annex.spec-fortran :refer :all]))

^{:refer hara.model.annex.spec-fortran/fortran-type :added "4.1"}
(fact "formats fortran type keywords"
  (fortran-type 'int)    => "INTEGER"
  (fortran-type 'long)   => "INTEGER*8"
  (fortran-type 'float)  => "REAL"
  (fortran-type 'double) => "DOUBLE PRECISION"
  (fortran-type 'bool)   => "LOGICAL"
  (fortran-type 'str)    => "CHARACTER(LEN=*)"
  (fortran-type 'void)   => "")

^{:refer hara.model.annex.spec-fortran/fortran-args :added "4.1"}
(fact "emits Fortran argument list"
  (impl/emit-script '(defn ^int add [[^int x] [^int y]] (+ x y))
                    {:lang :fortran})
  => string?)

^{:refer hara.model.annex.spec-fortran/fortran-decl :added "4.1"}
(fact "generates Fortran type declarations for typed arguments"
  (seq (fortran-decl '([int x] [double y])))
  => seq?

  (count (fortran-decl '([int x] [double y])))
  => 2

  (count (fortran-decl '[x y]))
  => 0)

^{:refer hara.model.annex.spec-fortran/fortran-defn :added "4.1"}
(fact "transforms defn to SUBROUTINE or FUNCTION"
  (impl/emit-script '(defn hello [x] x) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "SUBROUTINE hello")))

^{:refer hara.model.annex.spec-fortran/fortran-defprogram :added "4.1"}
(fact "transforms defprogram to PROGRAM block"
  (impl/emit-script '(program main (print "hello")) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "PROGRAM main")))

^{:refer hara.model.annex.spec-fortran/fortran-def :added "4.1"}
(fact "transforms def to Fortran typed declaration"
  (let [form '(def ^int x 5)]
    (fortran-def form))
  => list?)

^{:refer hara.model.annex.spec-fortran/fortran-print :added "4.1"}
(fact "transforms print to PRINT *"
  (let [form '(print "hello" x)]
    (impl/emit-script form {:lang :fortran}))
  => (fn [s] (clojure.string/includes? s "PRINT *,")))

^{:refer hara.model.annex.spec-fortran/fortran-module :added "4.1"}
(fact "transforms module to MODULE block"
  (impl/emit-script '(module my-mod (def ^int x 1)) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "MODULE my-mod")))

^{:refer hara.model.annex.spec-fortran/fortran-emit-if :added "4.1"}
(fact "emits IF THEN ELSE block"
  (impl/emit-as :fortran ['(if (> x 0) x 0)])
  => (fn [s] (clojure.string/includes? s "IF (")))

^{:refer hara.model.annex.spec-fortran/fortran-emit-for :added "4.1"}
(fact "emits DO loop"
  (fortran-emit-for '(for [i 1 10] (print i)) +grammar+ {})
  => (fn [s] (clojure.string/includes? s "DO i =")))