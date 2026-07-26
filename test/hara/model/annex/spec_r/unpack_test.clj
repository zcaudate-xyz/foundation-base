tahto/model/annex/spec_r/unpack_test.clj:1:(ns tahto.model.annex.spec-r.unpack-test
  (:use code.test)
tahto/model/annex/spec_r/unpack_test.clj:3:  (:require [tahto.model.annex.spec-r.rewrite :as rewrite]))

tahto/model/annex/spec_r/unpack_test.clj:5:^{:refer tahto.model.annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "rewrites unpack invokes for R"
  (rewrite/r-rewrite-stage
   '(return (f (x:unpack xs) y))
   nil)
  => '(return
        (do.call f
                 (append
                  (append [] (as.list xs))
                  (list y)))))
