(ns xt.sample.train-001-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root     {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array :added "4.1"
  :setup [(!.js (+ 1 2 3 ))]}
(fact "iterates arrays in order"
  
  (!.js
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])


(comment

  ;; need to test std.lang.seedgen.seed-infile functions

  ;; seedgen-root
  ;; 

  (std.lang.seedgen.seed-infile/seedgen-root)
  => :js

  
  ;; seedgen-list
  ;; 

  (std.lang.seedgen.seed-infile/seedgen-list)
  => []
  
  
  )
