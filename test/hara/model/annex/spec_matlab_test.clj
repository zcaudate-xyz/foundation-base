(ns hara.model.annex.spec-matlab-test
  (:require [hara.lang :as l]
            [hara.model.annex.spec-matlab :refer :all])
  (:use code.test))

(fact "Preliminary Checks"

  (l/emit-as :matlab '[[1 2 3 4]])
  => "[1, 2, 3, 4]"

  (l/emit-as :matlab '[(+ 1 2 3)])
  => "1 + 2 + 3"

  (l/emit-as :matlab '[(mod 10 3)])
  => "mod(10,3)"

  (l/emit-as :matlab '[{:a 1 :b 2}])
  => "struct(\"a\", 1, \"b\", 2)"

  (l/emit-as :matlab '[(not= 1 2)])
  => "1 ~= 2")

^{:refer hara.model.annex.spec-matlab/matlab-token-boolean :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-matlab/matlab-sym-str :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-matlab/matlab-module-name :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-matlab/matlab-qualified-name :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-matlab/tf-defn :added "4.1"}
(fact "TODO")
