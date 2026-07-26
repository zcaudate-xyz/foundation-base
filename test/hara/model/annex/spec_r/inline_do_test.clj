tahto/model/annex/spec_r/inline_do_test.clj:1:(ns tahto.model.annex.spec-r.inline-do-test
  (:use code.test)
tahto/model/annex/spec_r/inline_do_test.clj:3:  (:require [tahto.model.annex.spec-r.rewrite :as rewrite]))

tahto/model/annex/spec_r/inline_do_test.clj:5:^{:refer tahto.model.annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "lowers inline do returns after stage rewriting"
  (rewrite/r-rewrite-stage
   '(return (do (print 1) (+ 1 2)))
   nil)
  => '(do*
        (print 1)
        (return (+ 1 2))))
