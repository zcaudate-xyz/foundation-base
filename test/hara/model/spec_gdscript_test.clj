(ns hara.model.spec-gdscript-test
  (:use code.test)
  (:require [hara.model.spec-gdscript :refer :all]))

^{:refer hara.model.spec-gdscript/gdscript-dot :added "4.1"}
(fact "emits gdscript dot access")

^{:refer hara.model.spec-gdscript/gdscript-var :added "4.1"}
(fact "emits gdscript variables")

^{:refer hara.model.spec-gdscript/gdscript-fn :added "4.1"}
(fact "emits gdscript functions")

^{:refer hara.model.spec-gdscript/tf-for-object :added "4.1"}
(fact "transforms for:object loops")

^{:refer hara.model.spec-gdscript/tf-for-array :added "4.1"}
(fact "transforms for:array loops")

^{:refer hara.model.spec-gdscript/tf-for-iter :added "4.1"}
(fact "transforms for:iter loops")

^{:refer hara.model.spec-gdscript/tf-for-index :added "4.1"}
(fact "transforms for:index loops")

^{:refer hara.model.spec-gdscript/gdscript-module-link :added "4.1"}
(fact "emits gdscript module links")

^{:refer hara.model.spec-gdscript/gdscript-module-export :added "4.1"}
(fact "emits gdscript module exports")
