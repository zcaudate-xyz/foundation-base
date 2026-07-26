tahto/runtime/basic/impl_annex/process_haskell_test.clj:1:(ns tahto.runtime.basic.impl-annex.process-haskell-test
  (:use code.test)
tahto/runtime/basic/impl_annex/process_haskell_test.clj:3:  (:require [tahto.runtime.basic.impl-annex.process-haskell :refer :all]))

tahto/runtime/basic/impl_annex/process_haskell_test.clj:5:^{:refer tahto.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"}
(fact "wraps a single expression in a Haskell main block"
  (transform-form '[(+ 1 2 3)] {})
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2 3)))))

tahto/runtime/basic/impl_annex/process_haskell_test.clj:10:^{:refer tahto.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-preceding-forms}
(fact "keeps preceding forms before the main block"
  (transform-form '[(defn add [x y] (+ x y)) (add 1 2)] {})
  => '(:lines (defn add [x y] (+ x y)) (:% (:raw-str "main = print $\n") (:indent-body (add 1 2)))))

tahto/runtime/basic/impl_annex/process_haskell_test.clj:16:^{:refer tahto.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-single-list}
(fact "wraps a single list form in a vector before transformation"
  (transform-form '(+ 1 2) {})
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2)))))

tahto/runtime/basic/impl_annex/process_haskell_test.clj:22:^{:refer tahto.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-opts}
(fact "ignores the opts argument"
  (transform-form '[(+ 1 2)] nil)
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2)))))
