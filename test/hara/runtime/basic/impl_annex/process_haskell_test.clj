(ns hara.runtime.basic.impl-annex.process-haskell-test
  (:use code.test)
  (:require [hara.runtime.basic.impl-annex.process-haskell :refer :all]))

^{:refer hara.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"}
(fact "wraps a single expression in a Haskell main block"
  (transform-form '[(+ 1 2 3)] {})
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2 3)))))

^{:refer hara.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-preceding-forms}
(fact "keeps preceding forms before the main block"
  (transform-form '[(defn add [x y] (+ x y)) (add 1 2)] {})
  => '(:lines (defn add [x y] (+ x y)) (:% (:raw-str "main = print $\n") (:indent-body (add 1 2)))))

^{:refer hara.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-single-list}
(fact "wraps a single list form in a vector before transformation"
  (transform-form '(+ 1 2) {})
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2)))))

^{:refer hara.runtime.basic.impl-annex.process-haskell/transform-form :added "4.1"
  :id test-transform-form-haskell-opts}
(fact "ignores the opts argument"
  (transform-form '[(+ 1 2)] nil)
  => '(:lines (:% (:raw-str "main = print $\n") (:indent-body (+ 1 2)))))
