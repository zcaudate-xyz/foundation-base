(ns std.lang.model-annex.spec-r.unpack-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-r.rewrite :as rewrite]))

^{:refer std.lang.model-annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "rewrites unpack invokes for R"
  (rewrite/r-rewrite-stage
   '(return (f (x:unpack xs) y))
   nil)
  => '(return
        (do.call f
                 (append
                  (append [] (as.list xs))
                  (list y)))))
