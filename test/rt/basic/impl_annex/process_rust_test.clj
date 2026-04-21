(ns rt.basic.impl-annex.process-rust-test
  (:require [rt.basic.impl-annex.process-rust :refer :all]
            [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :rust
  {:runtime :twostep})

(defn.rs ^{:- [:i32]}
  add1
  [:i32 x]
  (return (+ x 1)))

(defn.rs ^{:- [:i32]}
  twice-add1
  [:i32 x]
  (return (+ (-/add1 x)
             (-/add1 x))))

(def CANARY-RUSTC
  (common/program-exists? "rustc"))

^{:refer rt.basic.impl-annex.process-rust/transform-form :added "4.0"}
(fact "transforms the rust form"

  (transform-form '[(+ 1 2 3)]
                  (l/rt :rust)
                  )
  => '(:- "fn main() {\n " (do ((:- "println!") "{}" (+ 1 2 3))) "\n}"))

^{:refer rt.basic.impl-annex.process-rust-test/CANARY-RUSTC :guard true :adopt true :added "4.0"}
(fact "evaluates rust code through the twostep runtime"

   (!.rs
    (+ 1 2 3))
   => 6)

^{:refer rt.basic.impl-annex.process-rust-test/CANARY-RUSTC :guard true :adopt true :added "4.0"}
(fact "twostep evaluates direct pointer calls in the script environment"

  [(-/add1 10)
   (-/twice-add1 10)
   (!.rs (-/twice-add1 10))]
  => [11 22 22])
