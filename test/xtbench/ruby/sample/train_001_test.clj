(ns xtbench.ruby.sample.train-001-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/for:array :added "4.1"
  :setup [(!.rb (+ 1 2 3 ))]}
(fact "iterates arrays in order"

  (!.rb
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])

(comment

  ;; need to test hara.seedgen.common-infile functions

  ;; seedgen-root
  ;; 

  (hara.seedgen.common-infile/seedgen-root)
  => :js

  
  ;; seedgen-list
  ;; 

  (hara.seedgen.common-infile/seedgen-list)
  => []
  
  
  )
