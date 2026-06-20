(ns hara.model.spec-lisp-common-test
  (:use code.test)
  (:require [hara.model.spec-lisp-common :refer :all]))

^{:refer hara.model.spec-lisp-common/prepare-top-level :added "4.1"}
(fact "handles prepare top level")

^{:refer hara.model.spec-lisp-common/expand-form :added "4.1"}
(fact "handles expand form")

^{:refer hara.model.spec-lisp-common/parse-def-assign-bindings :added "4.1"}
(fact "parses def assign bindings")

^{:refer hara.model.spec-lisp-common/transform-form :added "4.1"}
(fact "handles transform form")
