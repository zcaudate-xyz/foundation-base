(ns hara.model.annex.spec-julia.rewrite-test
  (:use code.test)
  (:require [hara.model.annex.spec-julia :refer :all]
            [hara.model.annex.spec-julia.rewrite :as rewrite]))

^{:refer hara.model.annex.spec-julia.rewrite/julia-rewrite-conditional-expression :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia.rewrite/julia-rewrite-expression :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia.rewrite/julia-rewrite-statement :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia.rewrite/julia-rewrite-statements :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-julia.rewrite/julia-rewrite-stage :added "4.1"}
(fact "rewrites unpack invokes for Julia"
  (rewrite/julia-rewrite-stage
   '(return (f (x:unpack xs) y))
   {:grammar +grammar+})
  => '(return (f (... xs) y)))