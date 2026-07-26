tahto/runtime/basic/impl_annex/process_ocaml_test.clj:1:(ns tahto.runtime.basic.impl-annex.process-ocaml-test
  (:use code.test)
tahto/runtime/basic/impl_annex/process_ocaml_test.clj:3:  (:require [tahto.runtime.basic.impl-annex.process-ocaml :refer :all]))

tahto/runtime/basic/impl_annex/process_ocaml_test.clj:5:^{:refer tahto.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"}
(fact "wraps a single form in an OCaml print wrapper"
  (transform-form '[(+ 1 2)] {})
  => '(:lines (:- "let () = print_int (" (+ 1 2) "); print_newline ()")))

tahto/runtime/basic/impl_annex/process_ocaml_test.clj:10:^{:refer tahto.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"
  :id test-transform-form-ocaml-preceding-forms}
(fact "keeps preceding forms and wraps only the last expression"
  (transform-form '[(def x 1) (def y 2) (+ x y)] {})
  => '(:lines (def x 1) (def y 2)
              (:- "let () = print_int (" (+ x y) "); print_newline ()")))

tahto/runtime/basic/impl_annex/process_ocaml_test.clj:17:^{:refer tahto.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"
  :id test-transform-form-ocaml-bare-form}
(fact "treats a bare symbol-headed form as a single expression"
  (transform-form '(+ 1 2) {})
  => '(:lines (:- "let () = print_int (" (+ 1 2) "); print_newline ()")))
