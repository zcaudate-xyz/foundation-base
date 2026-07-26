tahto/runtime/basic/impl_annex/process_rust_verify_test.clj:1:(ns tahto.runtime.basic.impl_annex.process-rust-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :rust
tahto/runtime/basic/impl_annex/process_rust_verify_test.clj:8:  tahto.runtime.basic.impl_annex.process-rust-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl_annex/process_rust_verify_test.clj:11:^{:refer tahto.runtime.basic.impl_annex.process-rust/CANARY :added "4.0"}
(fact "starts the rust verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/rust]))

(fact:global
 {:skip (not (env/program-exists? "rustc"))})

tahto/runtime/basic/impl_annex/process_rust_verify_test.clj:19:^{:refer tahto.runtime.basic.impl_annex.process-rust/!.rs :added "4.0"}
(fact "validates a simple rust expression through the runtime"
  (do (defn.rs ^{:- [:i32]} test-expr [] (return (+ 1 2 3)))
      (string? (!.rs (test-expr))))
  => true)
