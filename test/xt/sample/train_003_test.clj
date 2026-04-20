(ns xt.sample.train-003-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:xtalk/template {:all true}}
(l/script- :postgres)

^{:xtalk/seed     {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-spec/for:array :added "4.1"}
(fact "iterates arrays in order"
  
  (!.js
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])
