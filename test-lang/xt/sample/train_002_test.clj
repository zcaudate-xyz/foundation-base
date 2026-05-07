(ns xt.sample.train-002-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root         {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(!.js (+ 3 4 5))
          (!.lua (+ 1 2 3))
          (l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:seedgen/scaffold         {:python  {:suppress true}}}
(l/script+ [:db :postgres])

(def +a+ (inc 1))

(l/! :db (+ 1 2 3))

^{:refer xt.lang.spec-base/for:array :added "4.1"
  :setup [(!.js (+ 1 2 3))
          ^{:seedgen/derived   {:lang :lua}} ;; this is derived the meta is optional
             (!.lua (+ 1 2 3))
          (def +a+ (+ 1 2 3))]
  :teardown [(!.js (+ 1 2 3))]}
(fact "classifies root, derived, and scaffold forms"

  (!.js               ;; this is foundation
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3]

  (!.lua              ;; this is derived and can be removed
    (var out [])
    (xt/for:array [e [1 2 3 4]]
      (when (> e 3)
        (break))
      (xt/x:arr-push out e))
    out)
  => [1 2 3])
