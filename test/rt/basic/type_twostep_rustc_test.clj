(ns rt.basic.type-twostep-rustc-test
  (:use code.test)
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]))

(l/script- :rust
  {:runtime :twostep})

(def CANARY-RUSTC
  (common/program-exists? "rustc"))

(defn.rs ^{:- [:i32]}
  add-10
  [:i32 x]
  (return (+ x 10)))

(defn.rs ^{:- [:i32]}
  add-20
  [:i32 x]
  (return (+ x 20)))

(fact "rustc twostep can return values"
  (if CANARY-RUSTC
    [(!.rs
       (+ 1 2 3))

     (add-10 6)

     (!.rs
       (-/add-20 (-/add-10 6)))

     (!.rs
       (-/add-20 10))]
    :rustc-unavailable)
  => (any [6 16 36 30]
          :rustc-unavailable))
