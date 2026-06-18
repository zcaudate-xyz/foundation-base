(ns xtbench.dart.sample.train-002-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(!.dt (+ 3 4 5))
                  (l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:seedgen/scaffold         {:python  {:suppress true}}}
(l/script+ [:db :postgres])

(def +a+ (inc 1))

(l/! :db (+ 1 2 3))

^{:refer xt.lang.spec-base/for:array :added "4.1"
  :setup [(!.dt (+ 1 2 3))
                   (def +a+ (+ 1 2 3))]
  :teardown [(!.dt (+ 1 2 3))]}
(fact "classifies root, derived, and scaffold forms"

  (!.dt               ;; this is foundation
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])
