(ns hara.runtime.basic.impl-annex.process-ocaml-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-ocaml :refer :all]))

^{:refer hara.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"}
(fact "wraps a single form in an OCaml print wrapper"
  (transform-form '[(+ 1 2)] {})
  => '(:lines (:- "let () = print_int (" (+ 1 2) "); print_newline ()")))

^{:refer hara.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"
  :id test-transform-form-ocaml-preceding-forms}
(fact "keeps preceding forms and wraps only the last expression"
  (transform-form '[(def x 1) (def y 2) (+ x y)] {})
  => '(:lines (def x 1) (def y 2)
              (:- "let () = print_int (" (+ x y) "); print_newline ()")))

^{:refer hara.runtime.basic.impl-annex.process-ocaml/transform-form :added "4.1"
  :id test-transform-form-ocaml-bare-form}
(fact "treats a bare symbol-headed form as a single expression"
  (transform-form '(+ 1 2) {})
  => '(:lines (:- "let () = print_int (" (+ 1 2) "); print_newline ()")))
