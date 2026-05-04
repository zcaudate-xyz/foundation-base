(ns hara.model.annex.spec-r.inline-do-test
  (:use code.test)
  (:require [hara.model.annex.spec-r.rewrite :as rewrite]))

^{:refer hara.model.annex.spec-r.rewrite/r-rewrite-stage :added "4.1"}
(fact "lowers inline do returns after stage rewriting"
  (rewrite/r-rewrite-stage
   '(return (do (print 1) (+ 1 2)))
   nil)
  => '(do*
        (print 1)
        (return (+ 1 2))))
