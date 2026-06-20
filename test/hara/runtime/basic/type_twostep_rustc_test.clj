(ns hara.runtime.basic.type-twostep-rustc-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [hara.lang :as l]))

(l/script- :rust
  {:runtime :twostep})

(fact:global {:skip (not (env/program-exists? "rustc")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

(defn.rs ^{:- [:i32]}
  add-10
  [:i32 x]
  (return (+ x 10)))

(defn.rs ^{:- [:i32]}
  add-20
  [:i32 x]
  (return (+ x 20)))

(fact "rustc twostep can return values"
  [(!.rs
     (+ 1 2 3))

   (add-10 6)

   (!.rs
     (-/add-20 (-/add-10 6)))

   (!.rs
     (-/add-20 10))]
  => [6 16 36 30])
