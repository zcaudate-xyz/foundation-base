tahto/model/annex/spec_matlab_test.clj:1:(ns tahto.model.annex.spec-matlab-test
  (:require [tahto.core :as l]
tahto/model/annex/spec_matlab_test.clj:3:            [tahto.model.annex.spec-matlab :refer :all])
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

tahto/model/annex/spec_matlab_test.clj:23:^{:refer tahto.model.annex.spec-matlab/matlab-token-boolean :added "4.1"}
(fact "emits matlab boolean tokens")

tahto/model/annex/spec_matlab_test.clj:26:^{:refer tahto.model.annex.spec-matlab/matlab-sym-str :added "4.1"}
(fact "emits matlab symbol strings")

tahto/model/annex/spec_matlab_test.clj:29:^{:refer tahto.model.annex.spec-matlab/matlab-module-name :added "4.1"}
(fact "emits matlab module names")

tahto/model/annex/spec_matlab_test.clj:32:^{:refer tahto.model.annex.spec-matlab/matlab-qualified-name :added "4.1"}
(fact "emits matlab qualified names")

tahto/model/annex/spec_matlab_test.clj:35:^{:refer tahto.model.annex.spec-matlab/tf-defn :added "4.1"}
(fact "transforms function definitions")
