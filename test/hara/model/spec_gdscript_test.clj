tahto/model/spec_gdscript_test.clj:1:(ns tahto.model.spec-gdscript-test
  (:use code.test)
tahto/model/spec_gdscript_test.clj:3:  (:require [tahto.model.spec-gdscript :refer :all]))

tahto/model/spec_gdscript_test.clj:5:^{:refer tahto.model.spec-gdscript/gdscript-dot :added "4.1"}
(fact "emits gdscript dot access")

tahto/model/spec_gdscript_test.clj:8:^{:refer tahto.model.spec-gdscript/gdscript-var :added "4.1"}
(fact "emits gdscript variables")

tahto/model/spec_gdscript_test.clj:11:^{:refer tahto.model.spec-gdscript/gdscript-fn :added "4.1"}
(fact "emits gdscript functions")

tahto/model/spec_gdscript_test.clj:14:^{:refer tahto.model.spec-gdscript/tf-for-object :added "4.1"}
(fact "transforms for:object loops")

tahto/model/spec_gdscript_test.clj:17:^{:refer tahto.model.spec-gdscript/tf-for-array :added "4.1"}
(fact "transforms for:array loops")

tahto/model/spec_gdscript_test.clj:20:^{:refer tahto.model.spec-gdscript/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

tahto/model/spec_gdscript_test.clj:23:^{:refer tahto.model.spec-gdscript/tf-for-index :added "4.1"}
(fact "transforms for:index loops")

tahto/model/spec_gdscript_test.clj:26:^{:refer tahto.model.spec-gdscript/gdscript-module-link :added "4.1"}
(fact "emits gdscript module links")

tahto/model/spec_gdscript_test.clj:29:^{:refer tahto.model.spec-gdscript/gdscript-module-export :added "4.1"}
(fact "emits gdscript module exports")
