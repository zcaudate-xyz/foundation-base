(ns tahto.model.spec-gdscript-test
  (:use code.test)
  (:require [tahto.model.spec-gdscript :refer :all]))

^{:refer tahto.model.spec-gdscript/gdscript-dot :added "4.1"}
(fact "emits gdscript dot access")

^{:refer tahto.model.spec-gdscript/gdscript-var :added "4.1"}
(fact "emits gdscript variables")

^{:refer tahto.model.spec-gdscript/gdscript-fn :added "4.1"}
(fact "emits gdscript functions")

^{:refer tahto.model.spec-gdscript/tf-for-object :added "4.1"}
(fact "transforms for:object loops")

^{:refer tahto.model.spec-gdscript/tf-for-array :added "4.1"}
(fact "transforms for:array loops")

^{:refer tahto.model.spec-gdscript/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer tahto.model.spec-gdscript/tf-for-index :added "4.1"}
(fact "transforms for:index loops")

^{:refer tahto.model.spec-gdscript/gdscript-module-link :added "4.1"}
(fact "emits gdscript module links")

^{:refer tahto.model.spec-gdscript/gdscript-module-export :added "4.1"}
(fact "emits gdscript module exports")
