(ns std.lang.base.preprocess-input-test
  (:use code.test)
  (:require [std.lang.base.preprocess-base :as base]
            [std.lang.base.preprocess-input :refer :all]))

^{:refer std.lang.base.preprocess-input/to-input-form :added "4.1"}
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
  => '(!:deref (var std.lang.base.preprocess-input-test/hello))

  (to-input-form '@(+ 1 2 3))
  => '(!:eval (+ 1 2 3))

  (to-input-form '(@.lua (do 1 2 3)))
  => '(!:lang {:lang :lua} (do 1 2 3)))

^{:refer std.lang.base.preprocess-input/to-input :added "4.1"}
(fact "converts a form to input (extracting deref forms)"
  (to-input '(do (~! [1 2 3 4])))
  => (throws)

  (binding [base/*macro-splice* true]
    (to-input '(do (~! [1 2 3 4]))))
  => '(do 1 2 3 4))
