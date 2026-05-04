(ns hara.common.preprocess-input-test
  (:use code.test)
  (:require [hara.common.emit-preprocess :as preprocess] [hara.common.preprocess-base :as preprocess-base]
            [hara.common.preprocess-input :refer :all]))

^{:refer hara.common.preprocess-input/to-input-form :added "4.1"}
(fact "processes a form"
  (def hello 1)

  (to-input-form '(@! (+ 1 2 3)))
  => '(!:template (+ 1 2 3))

  (to-input-form '(-/Class$$new))
  => (any '(static-invoke -/Class "new")
          nil)

  (to-input-form '(Class$$new 1 2 3))
  => (any '(static-invoke Class "new" 1 2 3)
          nil)

  (to-input-form '@#'hello)
  => '(!:deref (var hara.common.preprocess-input-test/hello))

  (to-input-form '@(+ 1 2 3))
  => '(!:eval (+ 1 2 3))

  (to-input-form '(@.lua (do 1 2 3)))
  => '(!:lang {:lang :lua} (do 1 2 3)))

^{:refer hara.common.preprocess-input/to-input :added "4.1"}
(fact "converts a form to input (extracting deref forms)"
  (to-input '(do (~! [1 2 3 4])))
  => (throws)

  (binding [preprocess-base/*macro-splice* true]
    (to-input '(do (~! [1 2 3 4]))))
  => '(do 1 2 3 4))

^{:refer hara.common.preprocess-input/eval-template-forms :added "4.1"}
(fact "eagerly resolves template forms in persisted input"
  (def hello {:a 1})
  (def +hello+ {:b 2})

  (eval-template-forms '(do (!:template hello)
                            (!:template (+ 1 2 3))))
  => '(do {:a 1}
          6)

  (eval-template-forms '(do (!:eval +hello+)))
  => '(do {:b 2})

  (eval-template-forms '(do (!:template (+ a b))))
  => '(do (!:template (+ a b)))

  (eval-template-forms '(do (!:eval (+ 1 2 3))))
  => '(do (!:eval (+ 1 2 3))))
