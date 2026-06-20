(ns hara.model.spec-gdscript.rewrite-test
  (:use code.test)
  (:require [hara.model.spec-gdscript.rewrite :refer :all]))

^{:refer hara.model.spec-gdscript.rewrite/gdscript-rewrite-stage :added "4.1"}
(fact "rewrites gdscript stages")
