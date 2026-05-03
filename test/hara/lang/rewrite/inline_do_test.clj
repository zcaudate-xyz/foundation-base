(ns hara.lang.rewrite.inline-do-test
  (:use code.test)
  (:require [hara.lang.rewrite.inline-do :as inline]))

^{:refer hara.lang.rewrite.inline-do/do-expression? :added "4.1"}
(fact "TODO")

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do-list :added "4.1"}
(fact "TODO")

^{:refer hara.lang.rewrite.inline-do/rewrite-inline-do :added "4.1"}
(fact "rewrites inline do-return forms while preserving quoted forms"
  (inline/rewrite-inline-do
   '[(return (do (step 1) 2))
     (quote (return (do (step 3) 4)))])
  => '[(do* (step 1) (return 2))
       (quote (return (do (step 3) 4)))])