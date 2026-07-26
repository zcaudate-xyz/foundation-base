tahto/model/spec_lisp_common_test.clj:1:(ns tahto.model.spec-lisp-common-test
  (:use code.test)
tahto/model/spec_lisp_common_test.clj:3:  (:require [tahto.model.spec-lisp-common :refer :all]))

tahto/model/spec_lisp_common_test.clj:5:^{:refer tahto.model.spec-lisp-common/prepare-top-level :added "4.1"}
(fact "handles prepare top level")

tahto/model/spec_lisp_common_test.clj:8:^{:refer tahto.model.spec-lisp-common/expand-form :added "4.1"}
(fact "handles expand form")

tahto/model/spec_lisp_common_test.clj:11:^{:refer tahto.model.spec-lisp-common/parse-def-assign-bindings :added "4.1"}
(fact "parses def assign bindings")

tahto/model/spec_lisp_common_test.clj:14:^{:refer tahto.model.spec-lisp-common/transform-form :added "4.1"}
(fact "handles transform form")
