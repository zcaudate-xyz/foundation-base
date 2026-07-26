tahto/model/annex/spec_fortran_test.clj:1:(ns tahto.model.annex.spec-fortran-test
  (:use code.test)
  (:require [tahto.core.impl :as impl]
tahto/model/annex/spec_fortran_test.clj:4:            [tahto.model.annex.spec-fortran :refer :all]))

tahto/model/annex/spec_fortran_test.clj:6:^{:refer tahto.model.annex.spec-fortran/fortran-type :added "4.1"}
(fact "formats fortran type keywords"
  (fortran-type 'int)    => "INTEGER"
  (fortran-type 'long)   => "INTEGER*8"
  (fortran-type 'float)  => "REAL"
  (fortran-type 'double) => "DOUBLE PRECISION"
  (fortran-type 'bool)   => "LOGICAL"
tahto/model/annex/spec_fortran_test.clj:13:  (fortran-type 'str)    => "CTAHTOCTER(LEN=*)"
  (fortran-type 'void)   => "")

tahto/model/annex/spec_fortran_test.clj:16:^{:refer tahto.model.annex.spec-fortran/fortran-args :added "4.1"}
(fact "emits Fortran argument list"
  (impl/emit-script '(defn ^int add [[^int x] [^int y]] (+ x y))
                    {:lang :fortran})
  => string?)

tahto/model/annex/spec_fortran_test.clj:22:^{:refer tahto.model.annex.spec-fortran/fortran-decl :added "4.1"}
(fact "generates Fortran type declarations for typed arguments"
  (seq (fortran-decl '([int x] [double y])))
  => seq?

  (count (fortran-decl '([int x] [double y])))
  => 2

  (count (fortran-decl '[x y]))
  => 0)

tahto/model/annex/spec_fortran_test.clj:33:^{:refer tahto.model.annex.spec-fortran/fortran-defn :added "4.1"}
(fact "transforms defn to SUBROUTINE or FUNCTION"
  (impl/emit-script '(defn hello [x] x) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "SUBROUTINE hello")))

tahto/model/annex/spec_fortran_test.clj:38:^{:refer tahto.model.annex.spec-fortran/fortran-defprogram :added "4.1"}
(fact "transforms defprogram to PROGRAM block"
  (impl/emit-script '(program main (print "hello")) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "PROGRAM main")))

tahto/model/annex/spec_fortran_test.clj:43:^{:refer tahto.model.annex.spec-fortran/fortran-def :added "4.1"}
(fact "transforms def to Fortran typed declaration"
  (let [form '(def ^int x 5)]
    (fortran-def form))
  => list?)

tahto/model/annex/spec_fortran_test.clj:49:^{:refer tahto.model.annex.spec-fortran/fortran-print :added "4.1"}
(fact "transforms print to PRINT *"
  (let [form '(print "hello" x)]
    (impl/emit-script form {:lang :fortran}))
  => (fn [s] (clojure.string/includes? s "PRINT *,")))

tahto/model/annex/spec_fortran_test.clj:55:^{:refer tahto.model.annex.spec-fortran/fortran-module :added "4.1"}
(fact "transforms module to MODULE block"
  (impl/emit-script '(module my-mod (def ^int x 1)) {:lang :fortran})
  => (fn [s] (clojure.string/includes? s "MODULE my-mod")))

tahto/model/annex/spec_fortran_test.clj:60:^{:refer tahto.model.annex.spec-fortran/fortran-emit-if :added "4.1"}
(fact "emits IF THEN ELSE block"
  (impl/emit-as :fortran ['(if (> x 0) x 0)])
  => (fn [s] (clojure.string/includes? s "IF (")))

tahto/model/annex/spec_fortran_test.clj:65:^{:refer tahto.model.annex.spec-fortran/fortran-emit-for :added "4.1"}
(fact "emits DO loop"
  (fortran-emit-for '(for [i 1 10] (print i)) +grammar+ {})
  => (fn [s] (clojure.string/includes? s "DO i =")))