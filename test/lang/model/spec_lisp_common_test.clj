(ns lang.model.spec-lisp-common-test
  (:use code.test)
  (:require [lang.model.spec-lisp-common :refer :all]))

^{:refer lang.model.spec-lisp-common/prepare-top-level :added "4.1"}
(fact "handles prepare top level")

^{:refer lang.model.spec-lisp-common/expand-form :added "4.1"}
(fact "handles expand form")

^{:refer lang.model.spec-lisp-common/parse-def-assign-bindings :added "4.1"}
(fact "parses def assign bindings")

^{:refer lang.model.spec-lisp-common/transform-form :added "4.1"}
(fact "handles transform form")
