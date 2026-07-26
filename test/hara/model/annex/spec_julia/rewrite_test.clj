tahto/model/annex/spec_julia/rewrite_test.clj:1:(ns tahto.model.annex.spec-julia.rewrite-test
  (:use code.test)
tahto/model/annex/spec_julia/rewrite_test.clj:3:  (:require [tahto.model.annex.spec-julia :refer :all]
tahto/model/annex/spec_julia/rewrite_test.clj:4:            [tahto.model.annex.spec-julia.rewrite :as rewrite]))

tahto/model/annex/spec_julia/rewrite_test.clj:6:^{:refer tahto.model.annex.spec-julia.rewrite/julia-rewrite-conditional-expression :added "4.1"}
(fact "rewrites julia conditional expressions")

tahto/model/annex/spec_julia/rewrite_test.clj:9:^{:refer tahto.model.annex.spec-julia.rewrite/julia-rewrite-expression :added "4.1"}
(fact "rewrites julia expressions")

tahto/model/annex/spec_julia/rewrite_test.clj:12:^{:refer tahto.model.annex.spec-julia.rewrite/julia-rewrite-statement :added "4.1"}
(fact "rewrites julia statements")

tahto/model/annex/spec_julia/rewrite_test.clj:15:^{:refer tahto.model.annex.spec-julia.rewrite/julia-rewrite-statements :added "4.1"}
(fact "rewrites julia statement blocks")

tahto/model/annex/spec_julia/rewrite_test.clj:18:^{:refer tahto.model.annex.spec-julia.rewrite/julia-rewrite-stage :added "4.1"}
(fact "rewrites unpack invokes for Julia"
  (rewrite/julia-rewrite-stage
   '(return (f (x:unpack xs) y))
   {:grammar +grammar+})
  => '(return (f (... xs) y)))
