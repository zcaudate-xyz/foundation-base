(ns xtbench.ruby.sample.train-002-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/scaffold         {:python  {:suppress true}}}
(l/script+ [:db :postgres])

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)
                  (!.rb (+ 3 4 5))]
  :teardown [(l/rt:stop)]})

(def +a+ (inc 1))

(l/! :db (+ 1 2 3))

^{:refer xt.lang.spec-base/for:array :added "4.1"
  :setup [(def +a+ (+ 1 2 3))
                   (!.rb (+ 1 2 3))]
  :teardown [(!.rb (+ 1 2 3))]}
(fact "iterates arrays in order"

  (!.rb               ;; this is foundation
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])
