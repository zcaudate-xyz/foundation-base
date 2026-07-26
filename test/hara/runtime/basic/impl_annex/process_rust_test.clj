tahto/runtime/basic/impl_annex/process_rust_test.clj:1:(ns tahto.runtime.basic.impl-annex.process-rust-test
tahto/runtime/basic/impl_annex/process_rust_test.clj:2:  (:require [tahto.runtime.basic.impl-annex.process-rust :refer :all]
            [std.lib.env :as env]
            [tahto.core :as l])
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

(fact:global {:skip (not (env/program-exists? "rustc")) :setup [(l/rt:restart)] :teardown [(l/rt:stop)]})

tahto/runtime/basic/impl_annex/process_rust_test.clj:23:^{:refer tahto.runtime.basic.impl-annex.process-rust/transform-form :added "4.0"}
(fact "transforms the rust form"

  (transform-form '[(+ 1 2 3)]
                  (l/rt :rust)
                  )
  => '(:- "fn main() {\n " (do ((:- "println!") "{}" (+ 1 2 3))) "\n}"))

tahto/runtime/basic/impl_annex/process_rust_test.clj:31:^{:refer tahto.runtime.basic.impl-annex.process-rust-test/CANARY-RUSTC :adopt true :added "4.0"}
(fact "evaluates rust code through the twostep runtime"

   (!.rs
    (+ 1 2 3))
   => 6)

tahto/runtime/basic/impl_annex/process_rust_test.clj:38:^{:refer tahto.runtime.basic.impl-annex.process-rust-test/CANARY-RUSTC :adopt true :added "4.0"
  :id test-canary-rustc-direct-pointer-calls}
(fact "twostep evaluates direct pointer calls in the script environment"

  [(-/add1 10)
   (-/twice-add1 10)
   (!.rs (-/twice-add1 10))]
  => [11 22 22])
