(ns tahto.model.spec-lisp-common-test
  (:use code.test)
  (:require [tahto.model.spec-lisp-common :refer :all]))

^{:refer tahto.model.spec-lisp-common/prepare-top-level :added "4.1"}
(fact "handles prepare top level")

^{:refer tahto.model.spec-lisp-common/expand-form :added "4.1"}
(fact "handles expand form")

^{:refer tahto.model.spec-lisp-common/parse-def-assign-bindings :added "4.1"}
(fact "parses def assign bindings")

^{:refer tahto.model.spec-lisp-common/transform-form :added "4.1"}
(fact "handles transform form")
