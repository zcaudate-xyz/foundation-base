(ns hara.model.annex.spec-octave-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-octave :refer :all])
  (:use code.test))

(fact "Preliminary Checks"

  (l/emit-as :octave '[[1 2 3 4]])
  => "[1, 2, 3, 4]"

  (l/emit-as :octave '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :octave '[(mod 10 3)])
  => "mod(10,3)"

  (l/emit-as :octave '[{:a 1 :b 2}])
  => "struct(\"a\", 1, \"b\", 2)"

  (l/emit-as :octave '[(not= 1 2)])
  => "1 ~= 2")

^{:refer hara.model.annex.spec-octave/octave-token-boolean :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-octave/octave-sym-str :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-octave/octave-module-name :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-octave/octave-qualified-name :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-octave/tf-defn :added "4.1"}
(fact "TODO")