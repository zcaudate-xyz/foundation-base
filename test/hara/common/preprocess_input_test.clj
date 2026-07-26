tahto/common/preprocess_input_test.clj:1:(ns tahto.common.preprocess-input-test
  (:use code.test)
tahto/common/preprocess_input_test.clj:3:  (:require [tahto.common.emit-preprocess :as preprocess] [tahto.common.preprocess-base :as preprocess-base]
tahto/common/preprocess_input_test.clj:4:            [tahto.common.preprocess-input :refer :all]))

tahto/common/preprocess_input_test.clj:6:^{:refer tahto.common.preprocess-input/to-input-form :added "4.1"}
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
tahto/common/preprocess_input_test.clj:22:  => '(!:deref (var tahto.common.preprocess-input-test/hello))

  (to-input-form '@(+ 1 2 3))
  => '(!:eval (+ 1 2 3))

  (to-input-form '(@.lua (do 1 2 3)))
  => '(!:lang {:lang :lua} (do 1 2 3)))

tahto/common/preprocess_input_test.clj:30:^{:refer tahto.common.preprocess-input/to-input :added "4.1"}
(fact "converts a form to input (extracting deref forms)"
  (to-input '(do (~! [1 2 3 4])))
  => (throws)

  (binding [preprocess-base/*macro-splice* true]
    (to-input '(do (~! [1 2 3 4]))))
  => '(do 1 2 3 4))

tahto/common/preprocess_input_test.clj:39:^{:refer tahto.common.preprocess-input/eval-template-forms :added "4.1"}
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
