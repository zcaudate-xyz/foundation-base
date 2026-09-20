(ns lang.model.spec-gdscript-test
  (:use code.test)
  (:require [lang.model.annex.spec-gdscript :refer :all]))

^{:refer lang.model.annex.spec-gdscript/gdscript-dot :added "4.1"}
(fact "emits gdscript dot access")

^{:refer lang.model.annex.spec-gdscript/gdscript-var :added "4.1"}
(fact "emits gdscript variables")

^{:refer lang.model.annex.spec-gdscript/gdscript-fn :added "4.1"}
(fact "emits gdscript functions")

^{:refer lang.model.annex.spec-gdscript/tf-for-object :added "4.1"}
(fact "transforms for:object loops")

^{:refer lang.model.annex.spec-gdscript/tf-for-array :added "4.1"}
(fact "transforms for:array loops")

^{:refer lang.model.annex.spec-gdscript/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer lang.model.annex.spec-gdscript/tf-for-index :added "4.1"}
(fact "transforms for:index loops")

^{:refer lang.model.annex.spec-gdscript/gdscript-module-link :added "4.1"}
(fact "emits gdscript module links")

^{:refer lang.model.annex.spec-gdscript/gdscript-module-export :added "4.1"}
(fact "emits gdscript module exports")
