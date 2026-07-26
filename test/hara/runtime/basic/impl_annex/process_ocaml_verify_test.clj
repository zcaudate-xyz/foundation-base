tahto/runtime/basic/impl_annex/process_ocaml_verify_test.clj:1:(ns tahto.runtime.basic.impl_annex.process-ocaml-verify-test
  (:require [tahto.core :as l]
            [std.lib.context.space :as space]
            [std.lib.env :as env])
  (:use code.test))

(l/script :ocaml
tahto/runtime/basic/impl_annex/process_ocaml_verify_test.clj:8:  tahto.runtime.basic.impl_annex.process-ocaml-verify-test
  {:runtime :verify})

tahto/runtime/basic/impl_annex/process_ocaml_verify_test.clj:11:^{:refer tahto.runtime.basic.impl_annex.process-ocaml/CANARY :added "4.0"}
(fact "starts the ocaml verify runtime in the test namespace"
  (space/space:rt-active (env/ns-sym))
  => (contains [:lang/ocaml]))

(fact:global
 {:skip (not (env/program-exists? "ocamlc"))})

tahto/runtime/basic/impl_annex/process_ocaml_verify_test.clj:19:^{:refer tahto.runtime.basic.impl_annex.process-ocaml/!.ml :added "4.0"}
(fact "validates a simple ocaml expression through the runtime"
  (do (defn.ml test-expr [] (+ 1 2 3))
      (string? (!.ml (test-expr))))
  => true)
