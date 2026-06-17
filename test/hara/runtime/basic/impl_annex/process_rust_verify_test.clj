(ns hara.runtime.basic.impl_annex.process-rust-verify-test
  (:require [hara.lang :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :rust
  hara.runtime.basic.impl_annex.process-rust-verify-test
  {:runtime :verify})

^{:refer hara.runtime.basic.impl_annex.process-rust/CANARY :added "4.0"}
(fact "starts the rust verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/rust]))

(fact:global
 {:skip (not (env/program-exists? "rustc"))})

^{:refer hara.runtime.basic.impl_annex.process-rust/!.rs :added "4.0"}
(fact "validates a simple rust expression through the runtime"
  (do (defn.rs ^{:- [:i32]} test-expr [] (return (+ 1 2 3)))
      (string? (!.rs (test-expr))))
  => true)
